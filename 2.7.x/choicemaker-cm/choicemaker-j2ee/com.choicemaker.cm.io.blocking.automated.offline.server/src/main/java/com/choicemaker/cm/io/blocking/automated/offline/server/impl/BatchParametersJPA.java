package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

public interface BatchParametersJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCH_PARAMS";

	// -- Column names
	String CN_ID = "ID";
	String CN_STAGE_MODEL = "STAGE_MODEL";
	String CN_MASTER_MODEL = "MASTER_MODEL";
	String CN_MAX_SINGLE = "MAX_SINGLE";
	String CN_LOW_THRESHOLD = "LOW_THRESHOLD";
	String CN_HIGH_THRESHOLD = "HIGH_THRESHOLD";
	String CN_STAGE_RS = "STAGE_RS";
	String CN_MASTER_RS = "MASTER_RS";

	/** Name of a query that finds all persistent batch job instances */
	String QN_BATCHPARAMETERS_FIND_ALL =
			"batchParametersFindAll";

	/** EQL statement used to find all persistent batch job instances */
	String EQL_BATCHJOB_FIND_ALL =
			"Select params from BatchParametersBean params";

}
