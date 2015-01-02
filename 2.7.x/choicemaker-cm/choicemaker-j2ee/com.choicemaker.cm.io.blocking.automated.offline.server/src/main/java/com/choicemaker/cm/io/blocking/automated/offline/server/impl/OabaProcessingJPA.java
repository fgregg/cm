package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for OabaProcessingEvent beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface OabaProcessingJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_PROCESSING";

	/** Name of the column used to distinguish between job types */
	String DISCRIMINATOR_COLUMN = "JOB_TYPE";

	/**
	 * Value of the discriminator column used to mark OabaProcessingEvent
	 * types (and not sub-types)
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

	/** Link to a batch job */
	String CN_JOB_ID = "JOB_ID";

	/**
	 * A value defined by
	 * {@link com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing
	 * OabaProcessing}
	 */
	String CN_EVENT_ID = "EVENT_ID";

	/**
	 * Free-form information.
	 */
	// FIXME: this column sometimes stores operational parameters
	String CN_INFO = "INFO";

	String CN_TIMESTAMP = "TIMESTAMP";

	String ID_GENERATOR_NAME = "OABA_PROCESSING";

	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";

	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";

	String ID_GENERATOR_PK_COLUMN_VALUE = "OABA_PROCESSING";

	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	/** Name of the query that finds all persistent status entries */
	String QN_OABAPROCESSING_FIND_ALL = "oabaProcessingFindAll";

	/** JPQL used to implement {@link #QN_OABAPROCESSING_FIND_ALL} */
	String JPQL_OABAPROCESSING_FIND_ALL =
		"Select o from OabaProcessingLogEntry o";

	/**
	 * Name of the query that finds all persistent status entries for a
	 * particular OABA job, ordered by descending timestamp
	 */
	String QN_OABAPROCESSING_FIND_BY_JOBID = "oabaProcessingFindByJobId";

	/** JPQL used to implement {@link #QN_OABAPROCESSING_FIND_BY_JOBID} */
	String JPQL_OABAPROCESSING_FIND_BY_JOBID =
		"SELECT o FROM OabaProcessingLogEntry o WHERE o.jobId = :jobId "
				+ "ORDER BY o.eventTimestamp DESC, o.id DESC";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_OABAPROCESSING_FIND_BY_JOBID}
	 */
	String PN_OABAPROCESSING_FIND_BY_JOBID_JOBID = "jobId";

}
