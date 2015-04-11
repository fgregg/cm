package com.choicemaker.cm.args;

public interface MatchingConfiguration {

	enum MATCHING_CONFIGURATION_TYPE {
		deduplication, linkage;
	}

	public interface QuerySource {

		PersistableRecordSource getRecordSource();

		String getDatabaseConfiguration();

		boolean isDeduplicated();

		String getQueryToQueryBlockingConfiguration();

	}

	public interface ReferenceSource {

		PersistableRecordSource getRecordSource();

		String getDatabaseConfiguration();

		boolean isDeduplicated();

		String getQueryToReferenceBlockingConfiguration();

	}

	String getMatchingConfigurationName();

	MATCHING_CONFIGURATION_TYPE getMatchingConfigurationType();

	String getProbabilityModelName();

	QuerySource getQuerySource();

	ReferenceSource getReferenceSource();

}
