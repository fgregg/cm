package com.choicemaker.cmit.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.core.IProbabilityModelManager;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.DefaultProbabilityModelManager;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.db.sqlserver.SQLServerSerializableParallelSerialRecordSource;
import com.choicemaker.e2.CMPluginRegistry;

public class SimplePersonSqlServerTestConfiguration implements
		WellKnownTestConfiguration {

	private static Logger logger = Logger
			.getLogger(SimplePersonSqlServerTestConfiguration.class.getName());

	public static final String DEFAULT_DATASOURCE_JNDI_NAME =
		// "/choicemaker/urm/jdbc/ChoiceMakerBlocking";
		"/choicemaker/urm/jdbc/ChoiceMakerEjb";

	public static final String DEFAULT_STAGING_SQL =
		"SELECT RECORD_ID AS ID FROM PERSON WHERE LINKAGE_ROLE='S'";

	public static final String DEFAULT_MASTER_SQL =
		"SELECT RECORD_ID AS ID FROM PERSON WHERE LINKAGE_ROLE='M'";

	public static final String DEFAULT_MODEL_CONFIGURATION_ID =
		"com.choicemaker.cm.simplePersonMatching.Model1";

	public static final ImmutableThresholds DEFAULT_THRESHOLDS =
		new Thresholds(0.20f, 0.80f);

	/** Always do batch matching */
	public static final int DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD = 0;

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

	private boolean isInitialized;
	private boolean isValid;

	// private CMPluginRegistry pluginRegistry;
	private ImmutableProbabilityModel model;
	private ISerializableDbRecordSource staging;
	private ISerializableDbRecordSource master;

	public SimplePersonSqlServerTestConfiguration() {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_STAGING_SQL,
				DEFAULT_MASTER_SQL, DEFAULT_MODEL_CONFIGURATION_ID,
				DEFAULT_THRESHOLDS, DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD,
				DEFAULT_TRANSITIVITY_ANALYSIS);
	}

	public SimplePersonSqlServerTestConfiguration(boolean runTransitivity) {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_STAGING_SQL,
				DEFAULT_MASTER_SQL, DEFAULT_MODEL_CONFIGURATION_ID,
				DEFAULT_THRESHOLDS, DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD,
				runTransitivity);
	}

	public SimplePersonSqlServerTestConfiguration(String dsJndiName,
			String stagingSQL, String masterSQL, String mci,
			ImmutableThresholds t, int maxSingle, boolean runTransitivity) {
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
	 * One-time initialization.
	 * 
	 * @throws IllegalStateException
	 *             if initialization fails
	 */
	// @PostConstruct
	public void initialize(CMPluginRegistry registry) {
		// Standard messages
		final String NULL_REGISTRY = "null plugin registry";
		final String NULL_MODEL = "null model";
		final String NULL_STAGING = "null staging record source";
		final String NULL_MASTER = "null master record source";

		if (!isInitialized) {
			isValid = true;
			if (registry == null) {
				isValid = false;
				invalidConditions.add(NULL_REGISTRY);
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
						this.staging = createStagingRecordSource(model);
						if (staging == null) {
							isValid = false;
							invalidConditions.add(NULL_STAGING);
						}
					} catch (Exception e) {
						isValid = false;
						invalidConditions.add(e.toString());
					}
					try {
						this.master = createMasterRecordSource(model);
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
		if (!invalidConditions.isEmpty()) {
			String msg = "Initialization failed: " + invalidConditions;
			logger.severe(msg);
			throw new IllegalStateException(msg);
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
	public ISerializableDbRecordSource getStagingRecordSource() {
		invariant();
		return staging;
	}

	@Override
	public ISerializableDbRecordSource getMasterRecordSource() {
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

	private ImmutableProbabilityModel lookupModelByPluginId(
			String modelConfigurationId2) {
		IProbabilityModelManager pmm =
			DefaultProbabilityModelManager.getInstance();
		ImmutableProbabilityModel retVal =
			pmm.getImmutableModelInstance(modelConfigurationId2);
		return retVal;
	}

	private static ISerializableDbRecordSource createRecordSource(
			ImmutableProbabilityModel model, String dsJndiName, String sql)
			throws Exception {

		final String dbConfig = model.getDatabaseConfigurationName();
		ISerializableDbRecordSource retVal =
			new SQLServerSerializableParallelSerialRecordSource(dsJndiName,
					model.getModelName(), dbConfig, sql);
		return retVal;
	}

	private ISerializableDbRecordSource createStagingRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return createRecordSource(model, dataSourceJndiName, stagingSQL);
	}

	private ISerializableDbRecordSource createMasterRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return createRecordSource(model, dataSourceJndiName, masterSQL);
	}

}
