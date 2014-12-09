package com.choicemaker.cm.batch.impl;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

public interface BatchJobJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCHJOB";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "TYPE";

	/**
	 * Value of the discriminator column used to mark abstract BatchJob types.
	 * None should be marked if sub-classes are set up correctly with their own
	 * discriminator values.
	 */
	String DISCRIMINATOR_VALUE = "BATCH";

	/** Default value when no jobId is assigned */
	public static final long INVALID_ID = 0;

	/**
	 * Generated id column.
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	/**
	 * Discriminator column
	 * 
	 * @see #DISCRIMINATOR_COLUMN
	 */
	String CN_TYPE = DISCRIMINATOR_COLUMN;
	
	/** Optional link to predecessor batch job */
	String CN_BPARENT_ID = "BPARENT_ID";
	
	/** Internally defined transaction id that links several related batch jobs */
	String CN_TRANSACTION_ID = "TRANSACTION_ID";
	
	/** Externally defined transaction id that links several related batch jobs */
	String CN_EXTERNAL_ID = "EXTERNAL_ID";
	
	/** Required link to the id of some persistent instance of parameters */
	String CN_PARAMS_ID = "PARAMS_ID";

	/** Required link to the id of some persistent instance of settings */
	String CN_SETTINGS_ID = "SETTINGS_ID";
	
	/** Required link to the id of some persistent server configuration */
	String CN_SERVER_ID = "SERVER_ID";
	
	/** Optional link to an owning URM job */
	String CN_URM_ID = "URM_ID";

	/** Optional job description */
	String CN_DESCRIPTION = "DESCRIPTION";
	
	/** Absolute path to the working directory for a job */
	String CN_WORKING_DIRECTORY = "WORKING_DIR";

	/** Join column of the audit table */
	String CN_AUDIT_JOIN = "BATCHJOB_ID";

	/** Name of the audit table that records status time stamps */
	String AUDIT_TABLE_NAME = "CMT_OABA_BATCHJOB_AUDIT";

	/**
	 * A numerical estimate between 0 and 100 for how close a running job is to
	 * completion.
	 */
	String CN_FRACTION_COMPLETE = "FRACTION_COMPLETE";
	/**
	 * One of 8 possible values:
	 * <ul>
	 * <li>{@link OabaJob#STATUS_NEW}</li>
	 * <li>{@link OabaJob#STATUS_QUEUED}</li>
	 * <li>{@link OabaJob#STATUS_STARTED}</li>
	 * <li>{@link OabaJob#STATUS_COMPLETED}</li>
	 * <li>{@link OabaJob#STATUS_FAILED}</li>
	 * <li>{@link OabaJob#STATUS_ABORT_REQUESTED}</li>
	 * <li>{@link OabaJob#STATUS_ABORTED}</li>
	 * <li>{@link OabaJob#STATUS_CLEAR}</li>
	 * </ul>
	 */
	String CN_STATUS = "STATUS";
	
	/** Timestamp column of the audit table */
	String CN_TIMESTAMP = "TIMESTAMP";

	String ID_GENERATOR_NAME = "OABA_BATCHJOB";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "BATCHJOB";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

}