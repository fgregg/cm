package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

public interface BatchJobJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCHJOB";

	/** Name of the audit table that records status timestamps */
	String AUDIT_TABLE_NAME = "CMT_OABA_BATCHJOB_AUDIT";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "TYPE";

	/** Value of the discriminator column used to mark BatchJob types (and not sub-types) */
	String DISCRIMINATOR_VALUE = "OABA";

	// -- Column names
	String CN_ID = "ID";
	String CN_BPARENT_ID = "BPARENT_ID";
	String CN_URM_ID = "URM_ID";
	String CN_TRANSACTION_ID = "TRANSACTION_ID";
	String CN_EXTERNAL_ID = "EXTERNAL_ID";
	String CN_DESCRIPTION = "DESCRIPTION";
	String CN_FRACTION_COMPLETE = "FRACTION_COMPLETE";
	String CN_STATUS = "STATUS";
	String CN_TIMESTAMP = "TIMESTAMP";
	String CN_AUDIT_JOIN = "BATCHJOB_ID";

	/** Name of a query that finds all persistent batch job instances */
	String QN_BATCHJOB_FIND_ALL =
			"batchJobFindAll";

	/** EQL statement used to find all persistent batch job instances */
	String EQL_BATCHJOB_FIND_ALL =
			"Select job from BatchJobBean job";

}
