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
public interface AbstractPairResultJPA {

	/** Name of the table that persists pair-wise results */
	String TABLE_NAME = "CMT_OABA_PAIRS";

	/** Name of the column used to distinguish between pair types */
	String DISCRIMINATOR_COLUMN = "PAIR_TYPE";

	/** Default value of the discriminator column used to mark pair types */
	String DISCRIMINATOR_VALUE = "BATCH";

	String ID_GENERATOR_NAME = "OABA_PAIR";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "OABA_PAIR";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

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
	String CN_PAIR_TYPE = DISCRIMINATOR_COLUMN;

	/** Link to a batch job */
	String CN_JOB_ID = "JOB_ID";

	/** Name of the query that finds all persistent status entries */
	String QN_PAIR_FIND_ALL = "oabaProcessingFindAll";

	/** JPQL used to implement {@link #QN_PAIR_FIND_ALL} */
	String JPQL_PAIR_FIND_ALL =
		"Select o from OabaProcessingEventEntity o";

	/**
	 * Name of the query that finds all persistent status entries for a
	 * particular OABA job, ordered by descending timestamp
	 */
	String QN_PAIR_FIND_BY_JOBID = "oabaProcessingFindByJobId";

	/** JPQL used to implement {@link #QN_PAIR_FIND_BY_JOBID} */
	String JPQL_PAIR_FIND_BY_JOBID =
		"SELECT o FROM OabaProcessingEventEntity o WHERE o.jobId = :jobId "
				+ "ORDER BY o.eventTimestamp DESC, o.id DESC";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_PAIR_FIND_BY_JOBID}
	 */
	String PN_PAIR_FIND_BY_JOBID_JOBID = "jobId";

}
