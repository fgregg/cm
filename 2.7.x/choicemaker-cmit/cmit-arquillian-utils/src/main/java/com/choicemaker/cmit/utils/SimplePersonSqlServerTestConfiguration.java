package com.choicemaker.cmit.utils;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.choicemaker.cm.core.IProbabilityModelManager;
import com.choicemaker.cm.core.IRecordSourceSerializationRegistry;
import com.choicemaker.cm.core.IRecordSourceSerializer;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.core.base.DefaultProbabilityModelManager;
import com.choicemaker.cm.core.base.DefaultRecordSourceSerializationRegistry;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.db.base.ISerializableDbRecordSource;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

public class SimplePersonSqlServerTestConfiguration implements
		WellKnownTestConfiguration {

	private static Logger logger = Logger
			.getLogger(SimplePersonSqlServerTestConfiguration.class.getName());

	public static final String DEFAULT_DATASOURCE_JNDI_NAME =
		"/choicemaker/urm/jdbc/ChoiceMakerBlocking";
	
	public static final String DEFAULT_STAGING_SQL =
			"SELECT RECORD_ID AS ID FROM SIMPLE_PERSON WHERE TYPE='S'";

	public static final String DEFAULT_MASTER_SQL =
			"SELECT RECORD_ID AS ID FROM SIMPLE_PERSON WHERE TYPE='M'";

	public static final String DEFAULT_MODEL_CONFIGURATION_ID =
		"com.choicemaker.cm.simplePersonMatching.Model1";

	public static final ImmutableThresholds DEFAULT_THRESHOLDS =
		new Thresholds(0.20f, 0.80f);

	public static final int DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD = 1000;

	public static final boolean DEFAULT_TRANSITIVITY_ANALYSIS = true;

	public static final int DEFAULT_RECORD_BUFFER_SIZE = 100000;

	private final String dataSourceJndiName;
	private final String stagingSQL;
	private final String masterSQL;
	private final String modelConfigurationId;
	private final ImmutableThresholds thresholds;
	private final int maxSingle;
	private final boolean runTransitivityAnalysis;
	private final List<String> invalidConditions = new LinkedList<>();

	@EJB
	private EjbPlatform e2service;

	private boolean isInitialized;
	private boolean isValid;

	private ImmutableProbabilityModel model;
	private SerialRecordSource staging;
	private SerialRecordSource master;

	public SimplePersonSqlServerTestConfiguration() {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL, DEFAULT_MODEL_CONFIGURATION_ID,
				DEFAULT_THRESHOLDS, DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD,
				DEFAULT_TRANSITIVITY_ANALYSIS);
	}

	public SimplePersonSqlServerTestConfiguration(boolean runTransitivity) {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL, DEFAULT_MODEL_CONFIGURATION_ID,
				DEFAULT_THRESHOLDS, DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD,
				runTransitivity);
	}

	public SimplePersonSqlServerTestConfiguration(String dsJndiName,
			String stagingSQL, String masterSQL,
			String mci, ImmutableThresholds t, int maxSingle,
			boolean runTransitivity) {
		if (dsJndiName == null || dsJndiName.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank JNDI name for data source");

		}
		if (stagingSQL == null || stagingSQL.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank SQL for staging source");

		}
		if (masterSQL == null || masterSQL.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank SQL for master source");

		}
		if (mci == null || mci.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank model configuratian id");
		}
		if (t == null) {
			throw new IllegalArgumentException("null thresolds");
		}
		if (maxSingle < 0) {
			throw new IllegalArgumentException(
					"negative single-record matching threshold: " + maxSingle);
		}
		this.dataSourceJndiName = dsJndiName;
		this.stagingSQL = stagingSQL;
		this.masterSQL = masterSQL;
		this.modelConfigurationId = mci;
		this.thresholds = t;
		this.maxSingle = maxSingle;
		this.runTransitivityAnalysis = runTransitivity;
		assert isInitialized == false;
	}

	/**
	 * One-time initialization. If initialization fails, the instance is left in
	 * an invalid state.
	 */
	@PostConstruct
	protected void initialize() {
		// Standard messages
		final String NULL_PLATFORM = "null e2service";
		final String NULL_MODEL = "null model";
		final String NULL_STAGING = "null staging record source";
		final String NULL_MASTER = "null master record source";

		if (!isInitialized) {
			if (this.e2service == null) {
				isValid = false;
				invalidConditions.add(NULL_PLATFORM);
				invalidConditions.add(NULL_MODEL);
				invalidConditions.add(NULL_STAGING);
				invalidConditions.add(NULL_MASTER);
			} else {
				this.model = lookupModelByPluginId(modelConfigurationId);
				if (model == null) {
					isValid = false;
					invalidConditions.add(NULL_MODEL);
					invalidConditions.add(NULL_STAGING);
					invalidConditions.add(NULL_MASTER);
				} else {
					try {
						this.staging = synthesizeStagingRecordSource(model);
						if (staging == null) {
							isValid = false;
							invalidConditions.add(NULL_STAGING);
						}
					} catch (Exception e) {
						isValid = false;
						invalidConditions.add(e.toString());
					}
					try {
						this.master = synthesizeMasterRecordSource(model);
						if (master == null) {
							isValid = false;
							invalidConditions.add(NULL_MASTER);
						}
					} catch (Exception e) {
						isValid = false;
						invalidConditions.add(e.toString());
					}
				}
			}
		}
		isInitialized = true;
	}

	/**
	 * Checks whether initialization has left the instance in a valid state.
	 * 
	 * @throws IllegalStateException
	 *             if the instance is not initialized or is not in a valid state
	 */
	protected void invariant() {
		if (!isInitialized) {
			throw new IllegalStateException("not initialized");
		}
		if (!isValid) {
			throw new IllegalStateException("invalid state: "
					+ this.invalidConditions);
		}
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public boolean isValid() {
		return isValid;
	}

	@Override
	public SerialRecordSource getStagingRecordSource() {
		invariant();
		return staging;
	}

	@Override
	public SerialRecordSource getMasterRecordSource() {
		invariant();
		return master;
	}

	@Override
	public ImmutableThresholds getThresholds() {
		return thresholds;
	}

	@Override
	public String getModelConfigurationName() {
		return modelConfigurationId;
	}

	@Override
	public int getSingleRecordMatchingThreshold() {
		return maxSingle;
	}

	@Override
	public boolean getTransitivityAnalysisFlag() {
		return runTransitivityAnalysis;
	}

	public CMPluginRegistry getPluginRegistry() {
		CMPluginRegistry retVal = e2service.getPluginRegistry();
		return retVal;
	}

	private ImmutableProbabilityModel lookupModelByPluginId(
			String modelConfigurationId2) {
		IProbabilityModelManager pmm =
			DefaultProbabilityModelManager.getInstance();
		ImmutableProbabilityModel retVal =
			pmm.getImmutableModelInstance(modelConfigurationId2);
		return retVal;
	}

	private static SerialRecordSource synthesizeRecordSource(
			ImmutableProbabilityModel model, String dsJndiName, String sql)
			throws Exception {
		Context jndiContext = new InitialContext();
		DataSource ds = (DataSource) jndiContext.lookup(dsJndiName);
		Connection conn = ds.getConnection();
		String connUrl = conn.getMetaData().getURL();
		logger.fine("DB connection URL: " + connUrl);
		conn.close();

		// Configure the record source buffer size, dbConfig name and model name
		final String bufferSize = Integer.toString(DEFAULT_RECORD_BUFFER_SIZE);
		final String dbConfig =
			(String) model.properties().get(
					ImmutableProbabilityModel.PN_DATABASE_CONFIGURATION);
		final String modelName = model.getModelName();

		// Create a serializable record source
		IRecordSourceSerializationRegistry registry2 =
			DefaultRecordSourceSerializationRegistry.getInstance();
		IRecordSourceSerializer serializer =
			registry2.getRecordSourceSerializer(connUrl);
		Properties properties = new Properties();
		properties
				.setProperty(
						ISerializableDbRecordSource.PN_DATASOURCE_JNDI_NAME,
						dsJndiName);
		properties.setProperty(ISerializableDbRecordSource.PN_MODEL_NAME,
				modelName);
		properties.setProperty(ISerializableDbRecordSource.PN_DATABASE_CONFIG,
				dbConfig);
		properties.setProperty(ISerializableDbRecordSource.PN_SQL_QUERY, sql);
		properties.setProperty(ISerializableDbRecordSource.PN_BUFFER_SIZE,
				bufferSize);
		SerialRecordSource retVal =
			serializer.getSerializableRecordSource(properties);
		return retVal;
	}

	private SerialRecordSource synthesizeStagingRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return synthesizeRecordSource(model, dataSourceJndiName, stagingSQL);
	}

	private SerialRecordSource synthesizeMasterRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return synthesizeRecordSource(model, dataSourceJndiName, masterSQL);
	}

}
