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

	/**
	 * Name of a required plugin attribute that specifies the id of an existing
	 * database abstraction plugin
	 */
	String AN_DATABASE_ABSTRACTION = "databaseAbstraction";

	/**
	 * Name of a required plugin attribute that specifies the id of an existing
	 * database accessor plugin
	 */
	String AN_DATABASE_ACCESSOR = "databaseAccessor";

	// Deprecated property names

	/** @deprecated */
	String PN_BLOCKING_CONFIGURATION = AN_BLOCKING_CONFIGURATION;

	/** @deprecated */
	String PN_DB_CONFIGURATION = "dbConfiguration";

	/** @deprecated */
	String PN_DATABASE_ABSTRACTION =
			ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEABSTRACTION;

	/** @deprecated */
	String PN_DATABASE_ACCESSOR =
			ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;

	/** @deprecated */
	String PN_LIMITPERBLOCKINGSET = "limitPerBlockingSet";

	/** @deprecated */
	String PN_SINGLETABLEBLOCKINGSETGRACELIMIT = "limitSingleBlockingSet";

	/** @deprecated */
	String PN_LIMITSINGLEBLOCKINGSET = "limitPerBlockingSet";

	ImmutableProbabilityModel getProbabilityModel();
	
	String getDatabaseConfigurationName();
	
	String getBlockingConfigurationName();
	
	String getDatabaseAccessorName();

}
