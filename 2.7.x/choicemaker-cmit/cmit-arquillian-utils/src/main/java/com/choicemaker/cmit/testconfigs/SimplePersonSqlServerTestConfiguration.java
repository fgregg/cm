package com.choicemaker.cmit.testconfigs;

import static com.choicemaker.cm.args.WellKnownGraphPropertyNames.GPN_SCM;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.args.PersistentObject;
import com.choicemaker.cm.batch.impl.AbstractPersistentObject;
import com.choicemaker.cm.core.IProbabilityModelManager;
import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.DefaultProbabilityModelManager;
import com.choicemaker.cm.core.base.ImmutableThresholds;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.db.sqlserver.SQLServerSerializableParallelSerialRecordSource;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;

public class SimplePersonSqlServerTestConfiguration implements
		WellKnownTestConfiguration {

	private static Logger logger = Logger
			.getLogger(SimplePersonSqlServerTestConfiguration.class.getName());

	public static final String RECORD_SOURCE_CLASS_NAME =
		"com.choicemaker.cm.io.db.sqlserver.SQLServerSerializableParallelSerialRecordSource";

	public static final String DATABASE_ACCESSOR_PLUGIN =
		"com.choicemaker.cm.io.db.sqlserver.sqlServerDatabaseAccessor";

	public static final String DEFAULT_DATASOURCE_JNDI_NAME =
	// "/choicemaker/urm/jdbc/ChoiceMakerBlocking";
		"/choicemaker/urm/jdbc/ChoiceMakerEjb";

	public static final String DEFAULT_DATABASE_CONFIGURATION = "default";

	public static final String DEFAULT_BLOCKING_CONFIGURATION =
		"defaultAutomated";

	public static final String DEFAULT_STAGING_SQL =
		"SELECT RECORD_ID AS ID FROM PERSON WHERE LINKAGE_ROLE='S'";

	public static final String DEFAULT_MASTER_SQL =
		"SELECT RECORD_ID AS ID FROM PERSON WHERE LINKAGE_ROLE='M'";

	public static final OabaLinkageType DEFAULT_OABA_TASK =
		OabaLinkageType.STAGING_TO_MASTER_LINKAGE;

	public static final String DEFAULT_MODEL_CONFIGURATION_ID =
		"com.choicemaker.cm.simplePersonMatching.Model1";

	public static final ImmutableThresholds DEFAULT_THRESHOLDS =
		new Thresholds(0.20f, 0.80f);

	/** Always do batch matching */
	public static final int DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD = 0;

	public static final boolean DEFAULT_TRANSITIVITY_ANALYSIS = true;

	public static final AnalysisResultFormat DEFAULT_TRANSITIVITY_RESULT_FORMAT =
		AnalysisResultFormat.SORT_BY_HOLD_GROUP;

	public static final String DEFAULT_TRANSITIVITY_GRAPH_PROPERTY = GPN_SCM;

	public static final int DEFAULT_RECORD_BUFFER_SIZE = 100000;

	private final String dataSourceJndiName;
	private final boolean queryRsIsDeduplicated = false;
	private final String queryDatabaseConfiguration;
	private final String blockingConfiguration;
	private final String querySQL;
	private final String referenceSQL;
	private final String referenceDatabaseConfiguration;
	private final String modelConfigurationId;
	private final ImmutableThresholds thresholds;
	private final int maxSingle;
	private final boolean runTransitivityAnalysis;
	private final AnalysisResultFormat transitivityResultFormat;
	private final String transitivityGraphProperty;

	// Read-only values that are set once during initialization
	private boolean isInitialized;
	private boolean isValid;
	private OabaLinkageType task;
	private ImmutableProbabilityModel model;
	private ISerializableDbRecordSource staging;
	private ISerializableDbRecordSource master;

	public SimplePersonSqlServerTestConfiguration() {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_DATABASE_CONFIGURATION,
				DEFAULT_BLOCKING_CONFIGURATION,
				DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL,
				DEFAULT_MODEL_CONFIGURATION_ID, DEFAULT_THRESHOLDS,
				DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD,
				DEFAULT_TRANSITIVITY_ANALYSIS,
				DEFAULT_TRANSITIVITY_RESULT_FORMAT,
				DEFAULT_TRANSITIVITY_GRAPH_PROPERTY);
	}

	public SimplePersonSqlServerTestConfiguration(boolean runTransitivity) {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_DATABASE_CONFIGURATION,
				DEFAULT_BLOCKING_CONFIGURATION,
				DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL,
				DEFAULT_MODEL_CONFIGURATION_ID, DEFAULT_THRESHOLDS,
				DEFAULT_SINGLE_RECORD_MATCHING_THRESHOLD, runTransitivity,
				DEFAULT_TRANSITIVITY_RESULT_FORMAT,
				DEFAULT_TRANSITIVITY_GRAPH_PROPERTY);
	}

	public SimplePersonSqlServerTestConfiguration(String dsJndiName,
			String dbConfig, String blkConf, String stagingSQL, String masterSQL, String mci,
			ImmutableThresholds t, int maxSingle, boolean runTransitivity,
			AnalysisResultFormat arf, String gpn) {
		if (dsJndiName == null || dsJndiName.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank JNDI name for data source");

		}
		if (dbConfig == null || dbConfig.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank database configuration");

		}
		if (blkConf == null || blkConf.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank blocking configuration");

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
					"null or blank modelId configuratian id");
		}
		if (t == null) {
			throw new IllegalArgumentException("null thresolds");
		}
		if (maxSingle < 0) {
			throw new IllegalArgumentException(
					"negative single-record matching threshold: " + maxSingle);
		}
		if (arf == null) {
			throw new IllegalArgumentException(
					"null transitivity result format");
		}
		if (gpn == null || gpn.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"null or blank transitivity graph property");
		}
		this.dataSourceJndiName = dsJndiName;
		this.queryDatabaseConfiguration = dbConfig;
		this.blockingConfiguration = blkConf;
		this.querySQL = stagingSQL;
		this.referenceDatabaseConfiguration = dbConfig;
		this.referenceSQL = masterSQL;
		this.modelConfigurationId = mci;
		this.thresholds = t;
		this.maxSingle = maxSingle;
		this.runTransitivityAnalysis = runTransitivity;
		this.transitivityResultFormat = arf;
		this.transitivityGraphProperty = gpn;
		assert isInitialized == false;
	}

	/**
	 * One-time initialization that sets:
	 * <ul>
	 * <li>the linkage type</li>
	 * <li>the probability model</li>
	 * <li>the staging record source</li>
	 * <li>the master record source</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException
	 *             if either constructor argument is null
	 * @throws IllegalStateException
	 *             if initialization fails
	 */
	@Override
	public void initialize(OabaLinkageType task, CMPluginRegistry registry) {
		// Standard messages
		final String NULL_LINKAGE = "null linkage task";
		final String NULL_REGISTRY = "null plugin registry";
		final String NULL_MODEL = "null modelId";
		final String NULL_STAGING = "null staging record source";
		final String NULL_MASTER = "null master record source";

		if (!isInitialized) {
			final List<String> invalidConditions = new LinkedList<>();
			// Check preconditions
			isValid = true;
			if (task == null) {
				isValid = false;
				invalidConditions.add(NULL_LINKAGE);
			} else {
				this.task = task;
			}
			if (registry == null) {
				isValid = false;
				invalidConditions.add(NULL_REGISTRY);
			}
			if (!invalidConditions.isEmpty()) {
				String msg = "Initialization failed: " + invalidConditions;
				logger.severe(msg);
				throw new IllegalArgumentException(msg);
			}

			// Check state
			this.model = lookupModelByPluginId(modelConfigurationId);
			if (getModel() == null) {
				isValid = false;
				invalidConditions.add(NULL_MODEL);
				invalidConditions.add(NULL_STAGING);
				invalidConditions.add(NULL_MASTER);
			} else {
				try {
					this.staging = createStagingRecordSource(getModel());
					if (staging == null) {
						isValid = false;
						invalidConditions.add(NULL_STAGING);
					}
				} catch (Exception e) {
					isValid = false;
					invalidConditions.add(e.toString());
				}
				try {
					this.master = createMasterRecordSource(getModel());
					if (master == null) {
						isValid = false;
						invalidConditions.add(NULL_MASTER);
					}
				} catch (Exception e) {
					isValid = false;
					invalidConditions.add(e.toString());
				}
			}
			if (!invalidConditions.isEmpty()) {
				String msg = "Initialization failed: " + invalidConditions;
				logger.severe(msg);
				throw new IllegalStateException(msg);
			}

			// Mark as initialized
			isInitialized = true;
		}
		assert isInitialized;
	}

	/**
	 * Checks whether initialization has left the instance in a valid state.
	 *
	 * @throws IllegalStateException
	 *             if the instance is not initialized or is not in a valid state
	 */
	protected void invariant() {
		if (!isInitialized()) {
			throw new IllegalStateException("not initialized");
		}
		if (!isValid()) {
			throw new IllegalStateException("invalid state");
		}
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public boolean isValid() {
		return isValid;
	}

	@Override
	public ISerializableDbRecordSource getSerializableStagingRecordSource() {
		invariant();
		return staging;
	}

	@Override
	public ISerializableDbRecordSource getSerializableMasterRecordSource() {
		invariant();
		return master;
	}

	@Override
	public PersistableRecordSource getQueryRecordSource() {
		invariant();
		return new PersistableSqlRecordSource() {

			private static final long serialVersionUID = 271L;

			private AbstractPersistentObject delegate =
				new AbstractPersistentObject() {

					@Override
					public long getId() {
						return PersistentObject.NONPERSISTENT_ID;
					}

				};

			@Override
			public long getId() {
				return delegate.getId();
			}

			@Override
			public String getType() {
				return PersistableSqlRecordSource.TYPE;
			}

			@Override
			public String getDatabaseReader() {
				return RECORD_SOURCE_CLASS_NAME;
			}

			@Override
			public String getDataSource() {
				return DEFAULT_DATASOURCE_JNDI_NAME;
			}

			@Override
			public String getSqlSelectStatement() {
				return DEFAULT_STAGING_SQL;
			}

			@Override
			public String getModelId() {
				return DEFAULT_MODEL_CONFIGURATION_ID;
			}

			@Override
			public String getDatabaseConfiguration() {
				return DEFAULT_DATABASE_CONFIGURATION;
			}

			@Override
			public String getDatabaseAccessor() {
				return null;
			}

			@Override
			public String getUUID() {
				return delegate.getUUID();
			}

			@Override
			public int getOptLock() {
				return delegate.getOptLock();
			}

			@Override
			public boolean isPersistent() {
				return delegate.isPersistent();
			}

		};
	}

	@Override
	public PersistableRecordSource getReferenceRecordSource() {
		invariant();
		return new PersistableSqlRecordSource() {

			private static final long serialVersionUID = 271L;

			private AbstractPersistentObject delegate =
				new AbstractPersistentObject() {

					@Override
					public long getId() {
						return PersistentObject.NONPERSISTENT_ID;
					}

				};

			@Override
			public long getId() {
				return delegate.getId();
			}

			@Override
			public String getType() {
				return PersistableSqlRecordSource.TYPE;
			}

			@Override
			public String getDatabaseReader() {
				return RECORD_SOURCE_CLASS_NAME;
			}

			@Override
			public String getDataSource() {
				return DEFAULT_DATASOURCE_JNDI_NAME;
			}

			@Override
			public String getSqlSelectStatement() {
				return DEFAULT_MASTER_SQL;
			}

			@Override
			public String getModelId() {
				return DEFAULT_MODEL_CONFIGURATION_ID;
			}

			@Override
			public String getDatabaseConfiguration() {
				return DEFAULT_DATABASE_CONFIGURATION;
			}

			@Override
			public String getDatabaseAccessor() {
				return DATABASE_ACCESSOR_PLUGIN;
			}

			@Override
			public String getUUID() {
				return delegate.getUUID();
			}

			@Override
			public int getOptLock() {
				return delegate.getOptLock();
			}

			@Override
			public boolean isPersistent() {
				return delegate.isPersistent();
			}

		};
	}

	@Override
	public OabaLinkageType getOabaTask() {
		invariant();
		return task;
	}

	@Override
	public ImmutableProbabilityModel getModel() {
		return model;
	}

	@Override
	public String getQueryDatabaseConfiguration() {
		return queryDatabaseConfiguration;
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

	@Override
	public AnalysisResultFormat getTransitivityResultFormat() {
		return transitivityResultFormat;
	}

	@Override
	public String getTransitivityGraphProperty() {
		return transitivityGraphProperty;
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
			ImmutableProbabilityModel model, String dsJndiName,
			String dbConfig, String sql) throws Exception {

		ISerializableDbRecordSource retVal =
			new SQLServerSerializableParallelSerialRecordSource(dsJndiName,
					model.getModelName(), dbConfig, sql);
		return retVal;
	}

	private ISerializableDbRecordSource createStagingRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return createRecordSource(model, dataSourceJndiName,
				queryDatabaseConfiguration, querySQL);
	}

	private ISerializableDbRecordSource createMasterRecordSource(
			ImmutableProbabilityModel model) throws Exception {
		return createRecordSource(model, dataSourceJndiName,
				queryDatabaseConfiguration, referenceSQL);
	}

	@Override
	public boolean isQueryRsDeduplicated() {
		return queryRsIsDeduplicated;
	}

	@Override
	public String getBlockingConfiguration() {
		return blockingConfiguration;
	}

	@Override
	public String getReferenceDatabaseConfiguration() {
		return referenceDatabaseConfiguration;
	}

}
