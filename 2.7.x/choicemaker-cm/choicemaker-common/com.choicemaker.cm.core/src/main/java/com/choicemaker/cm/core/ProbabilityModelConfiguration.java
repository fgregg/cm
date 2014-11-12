package com.choicemaker.cm.core;

public interface ProbabilityModelConfiguration {

	/**
	 * Name of a required plugin attribute that specifies a named blocking configuration
	 * in the record-layout schema of the model.
	 */
	String AN_BLOCKING_CONFIGURATION = "blockingConfiguration";

	/**
	 * Name of a required plugin attribute that specifies a named database configuration
	 * in the record-layout schema of the model.
	 */

	String AN_DATABASE_CONFIGURATION = "databaseConfiguration";

	ImmutableProbabilityModel getProbabilityModel();
	
	String getDatabaseConfigurationName();
	
	String getBlockingConfigurationName();
	
}
