package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for OabaBatchProcessingEvent beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface OabaPairResultJPA {

	/** Name of the table that persists pair-wise results */
	String TABLE_NAME = "CMT_OABA_PAIRS";

	/** Name of the column used to distinguish between pair types */
	String DISCRIMINATOR_COLUMN = "PAIR_TYPE";

	/** Discriminator value column used to mark abstract pair types */
	String DV_ABSTRACT = "0";

	/** Discriminator value column used to mark integer pair types */
	String DV_INTEGER = "1";

	/** Discriminator value column used to mark long pair types */
	String DV_LONG = "2";

	/** Discriminator value column used to mark String pair types */
	String DV_STRING = "3";

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

	// -- Queries for translations of abstract record ids

	/**
	 * Name of the query that finds all persistent, abstract pair entries
	 * (should be none)
	 */
	String QN_PAIRRESULT_FIND_ALL = "oabaPairResultAbstractFindAll";

	/** JPQL used to implement {@link #QN_PAIRRESULT_FIND_ALL} */
	String JPQL_PAIRRESULT_FIND_ALL =
		"SELECT o FROM AbstractPairResultEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, abstract pair entries for a
	 * particular OABA job (should be none)
	 */
	String QN_PAIRRESULT_FIND_BY_JOBID = "oabaPairResultAbstractFindByJobId";

	/** JPQL used to implement {@link #QN_PAIRRESULT_FIND_BY_JOBID} */
	String JPQL_PAIRRESULT_FIND_BY_JOBID =
		"SELECT o FROM AbstractPairResultEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_PAIRRESULT_FIND_BY_JOBID}
	 */
	String PN_PAIRRESULT_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of integer record ids

	/**
	 * Name of the query that finds all persistent, Integer-id pair entries
	 */
	String QN_PAIRRESULTINTEGER_FIND_ALL = "oabaPairResultIntegerFindAll";

	/** JPQL used to implement {@link #QN_PAIRRESULTINTEGER_FIND_ALL} */
	String JPQL_PAIRRESULTINTEGER_FIND_ALL =
		"SELECT o FROM OabaPairResultIntegerEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, Integer-id pair entries for
	 * a particular OABA job
	 */
	String QN_PAIRRESULTINTEGER_FIND_BY_JOBID =
		"oabaPairResultIntegerFindByJobId";

	/** JPQL used to implement {@link #QN_PAIRRESULTINTEGER_FIND_BY_JOBID} */
	String JPQL_PAIRRESULTINTEGER_FIND_BY_JOBID =
		"SELECT o FROM OabaPairResultIntegerEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_PAIRRESULTINTEGER_FIND_BY_JOBID}
	 */
	String PN_PAIRRESULTINTEGER_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of long record ids

	/**
	 * Name of the query that finds all persistent, Long-id pair entries
	 */
	String QN_PAIRRESULTLONG_FIND_ALL = "oabaPairResultLongFindAll";

	/** JPQL used to implement {@link #QN_PAIRRESULTLONG_FIND_ALL} */
	String JPQL_PAIRRESULTLONG_FIND_ALL =
		"SELECT o FROM OabaPairResultLongEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, Long-id pair entries for a
	 * particular OABA job
	 */
	String QN_PAIRRESULTLONG_FIND_BY_JOBID = "oabaPairResultLongFindByJobId";

	/** JPQL used to implement {@link #QN_PAIRRESULTLONG_FIND_BY_JOBID} */
	String JPQL_PAIRRESULTLONG_FIND_BY_JOBID =
		"SELECT o FROM OabaPairResultLongEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_PAIRRESULTLONG_FIND_BY_JOBID}
	 */
	String PN_PAIRRESULTLONG_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of String record ids

	/**
	 * Name of the query that finds all persistent, String-id pair entries
	 */
	String QN_PAIRRESULTSTRING_FIND_ALL = "oabaPairResultStringFindAll";

	/** JPQL used to implement {@link #QN_PAIRRESULTSTRING_FIND_ALL} */
	String JPQL_PAIRRESULTSTRING_FIND_ALL =
		"SELECT o FROM OabaPairResultStringEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, String-id pair entries for a
	 * particular OABA job
	 */
	String QN_PAIRRESULTSTRING_FIND_BY_JOBID =
		"oabaPairResultStringFindByJobId";

	/** JPQL used to implement {@link #QN_PAIRRESULTSTRING_FIND_BY_JOBID} */
	String JPQL_PAIRRESULTSTRING_FIND_BY_JOBID =
		"SELECT o FROM OabaPairResultStringEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_PAIRRESULTSTRING_FIND_BY_JOBID}
	 */
	String PN_PAIRRESULTSTRING_FIND_BY_JOBID_JOBID = "jobId";

}
