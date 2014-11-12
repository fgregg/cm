package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * Java Persistence API (JPA) for BatchJob beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface BatchJobJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCHJOB";

	/** Name of the audit table that records status time stamps */
	String AUDIT_TABLE_NAME = "CMT_OABA_BATCHJOB_AUDIT";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "TYPE";

	/**
	 * Value of the discriminator column used to mark BatchJob types (and not
	 * sub-types)
	 */
	String DISCRIMINATOR_VALUE = "OABA";

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

	/** Required link to the id of some persistent instance of batch parameters */
	String CN_BPARAMS_ID = "BPARAMS_ID";

	/** Optional link to an owing URM job */
	String CN_URM_ID = "URM_ID";

	/** Internally defined transaction id that links several related batch jobs */
	String CN_TRANSACTION_ID = "TRANSACTION_ID";

	/** Externally defined transcation id that links several related batch jobs */
	String CN_EXTERNAL_ID = "EXTERNAL_ID";

	/** Optional job description */
	String CN_DESCRIPTION = "DESCRIPTION";

	/**
	 * A numerical estimate between 0 and 100 for how close a running job is to
	 * completion.
	 */
	String CN_FRACTION_COMPLETE = "FRACTION_COMPLETE";

	/**
	 * One of 8 possible values:
	 * <ul>
	 * <li>{@link BatchJob#STATUS_NEW}</li>
	 * <li>{@link BatchJob#STATUS_QUEUED}</li>
	 * <li>{@link BatchJob#STATUS_STARTED}</li>
	 * <li>{@link BatchJob#STATUS_COMPLETED}</li>
	 * <li>{@link BatchJob#STATUS_FAILED}</li>
	 * <li>{@link BatchJob#STATUS_ABORT_REQUESTED}</li>
	 * <li>{@link BatchJob#STATUS_ABORTED}</li>
	 * <li>{@link BatchJob#STATUS_CLEAR}</li>
	 * </ul>
	 */
	String CN_STATUS = "STATUS";

	/** Timestamp column of the audit table */
	String CN_TIMESTAMP = "TIMESTAMP";

	/** Join column of the audit table */
	String CN_AUDIT_JOIN = "BATCHJOB_ID";

	String ID_GENERATOR_NAME = "OABA_BATCHJOB";

	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";

	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";

	String ID_GENERATOR_PK_COLUMN_VALUE = "OABA_BATCHJOB";

	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	/** Name of the query that finds all persistent batch job instances */
	String QN_BATCHJOB_FIND_ALL = "batchJobFindAll";

	/** JPQL used to implement {@link #QN_BATCHJOB_FIND_ALL} */
	String JPQL_BATCHJOB_FIND_ALL = "Select job from BatchJobBean job";

}
