package com.choicemaker.cmit.testconfigs;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.core.base.ImmutableThresholds;

/**
 * Same test as {@link SimplePersonSqlServerTestConfiguration}, but with a
 * Xxx threshold that triggers single-record-mode matching
 *
 * @author rphall
 */
public class SimplePersonSqlServerSRMConfiguration extends SimplePersonSqlServerTestConfiguration {

	/** Never do batch matching */
	public static final int MAX_SINGLE_RECORD_MATCHING_THRESHOLD = Integer.MAX_VALUE;

	public SimplePersonSqlServerSRMConfiguration() {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_DATABASE_CONFIGURATION,
				DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL,
				DEFAULT_MODEL_CONFIGURATION_ID, DEFAULT_THRESHOLDS,
				MAX_SINGLE_RECORD_MATCHING_THRESHOLD,
				DEFAULT_TRANSITIVITY_ANALYSIS,
				DEFAULT_TRANSITIVITY_RESULT_FORMAT,
				DEFAULT_TRANSITIVITY_GRAPH_PROPERTY);
	}

	public SimplePersonSqlServerSRMConfiguration(boolean runTransitivity) {
		this(DEFAULT_DATASOURCE_JNDI_NAME, DEFAULT_DATABASE_CONFIGURATION,
				DEFAULT_STAGING_SQL, DEFAULT_MASTER_SQL,
				DEFAULT_MODEL_CONFIGURATION_ID, DEFAULT_THRESHOLDS,
				MAX_SINGLE_RECORD_MATCHING_THRESHOLD, runTransitivity,
				DEFAULT_TRANSITIVITY_RESULT_FORMAT,
				DEFAULT_TRANSITIVITY_GRAPH_PROPERTY);
	}

	public SimplePersonSqlServerSRMConfiguration(String dsJndiName,
			String dbConfig, String stagingSQL, String masterSQL, String mci,
			ImmutableThresholds t, int maxSingle, boolean runTransitivity,
			AnalysisResultFormat arf, String gpn) {
		super(dsJndiName,
			dbConfig, stagingSQL, masterSQL, mci,
			t, maxSingle, runTransitivity,
			arf, gpn);
	}

}
