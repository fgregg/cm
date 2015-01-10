package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for OabaProcessingEvent beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * <li>DV -- Discriminator Value</li>
 * </ul>
 * 
 * @author rphall
 */
public interface RecordIdTranslationJPA {

	/** Name of the table that persists pair-wise results */
	String TABLE_NAME = "CMT_RECORD_ID";

	/** Name of the column used to distinguish between pair types */
	String DISCRIMINATOR_COLUMN = "TYPE";

	/** Discriminator value column used to mark abstract types */
	String DV_ABSTRACT = "0";

	/** Discriminator value column used to mark integer types */
	String DV_INTEGER = "1";

	/** Discriminator value column used to mark long types */
	String DV_LONG = "2";

	/** Discriminator value column used to mark String types */
	String DV_STRING = "3";

	String ID_GENERATOR_NAME = "OABA_TRANSLATED_ID";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "TRANSLATED_ID";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	/**
	 * Column for generated persistence identifier
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	/**
	 * Discriminator column
	 * 
	 * @see #DISCRIMINATOR_COLUMN
	 */
	String CN_RECORD_TYPE = DISCRIMINATOR_COLUMN;

	/** Link to a batch job */
	String CN_JOB_ID = "JOB_ID";

	/** Record identifier */
	String CN_RECORD_ID = "RECORD_ID";

	/** Translated record identifier */
	String CN_TRANSLATED_ID = "TRANSLATED_ID";

	/** Record source -- master or staging */
	String CN_RECORD_SOURCE = "SOURCE";

	// -- Queries for translations of abstract record ids

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * (should be none)
	 */
	String QN_TRANSLATEDID_FIND_ALL = "oabaTranslatedIdFindAll";

	/** JPQL used to implement {@link #QN_TRANSLATEDID_FIND_ALL} */
	String JPQL_TRANSLATEDID_FIND_ALL =
		"SELECT o FROM AbstractRecordIdTranslationEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * for a particular OABA job (should be none)
	 */
	String QN_TRANSLATEDID_FIND_BY_JOBID = "oabaTranslatedIdFindByJobId";

	/** JPQL used to implement {@link #QN_TRANSLATEDID_FIND_BY_JOBID} */
	String JPQL_TRANSLATEDID_FIND_BY_JOBID =
		"SELECT o FROM AbstractRecordIdTranslationEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSLATEDID_FIND_BY_JOBID}
	 */
	String PN_TRANSLATEDID_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of integer record ids

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * (should be none)
	 */
	String QN_TRANSLATEDINTEGERID_FIND_ALL = "oabaTranslatedIdFindAll";

	/** JPQL used to implement {@link #QN_TRANSLATEDINTEGERID_FIND_ALL} */
	String JPQL_TRANSLATEDINTEGERID_FIND_ALL =
		"SELECT o FROM RecordIdIntegerTranslationEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * for a particular OABA job (should be none)
	 */
	String QN_TRANSLATEDINTEGERID_FIND_BY_JOBID = "oabaTranslatedIdFindByJobId";

	/** JPQL used to implement {@link #QN_TRANSLATEDINTEGERID_FIND_BY_JOBID} */
	String JPQL_TRANSLATEDINTEGERID_FIND_BY_JOBID =
		"SELECT o FROM RecordIdIntegerTranslationEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSLATEDINTEGERID_FIND_BY_JOBID}
	 */
	String PN_TRANSLATEDINTEGERID_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of long record ids

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * (should be none)
	 */
	String QN_TRANSLATEDLONGID_FIND_ALL = "oabaTranslatedIdFindAll";

	/** JPQL used to implement {@link #QN_TRANSLATEDLONGID_FIND_ALL} */
	String JPQL_TRANSLATEDLONGID_FIND_ALL =
		"SELECT o FROM RecordIdLongTranslationEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * for a particular OABA job (should be none)
	 */
	String QN_TRANSLATEDLONGID_FIND_BY_JOBID = "oabaTranslatedIdFindByJobId";

	/** JPQL used to implement {@link #QN_TRANSLATEDLONGID_FIND_BY_JOBID} */
	String JPQL_TRANSLATEDLONGID_FIND_BY_JOBID =
		"SELECT o FROM RecordIdLongTranslationEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSLATEDLONGID_FIND_BY_JOBID}
	 */
	String PN_TRANSLATEDLONGID_FIND_BY_JOBID_JOBID = "jobId";

	// -- Queries for translations of String record ids

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * (should be none)
	 */
	String QN_TRANSLATEDSTRINGID_FIND_ALL = "oabaTranslatedIdFindAll";

	/** JPQL used to implement {@link #QN_TRANSLATEDSTRINGID_FIND_ALL} */
	String JPQL_TRANSLATEDSTRINGID_FIND_ALL =
		"SELECT o FROM RecordIdStringTranslationEntity o "
				+ "ORDER BY o.jobId, o.translatedId";

	/**
	 * Name of the query that finds all persistent, abstract translation entries
	 * for a particular OABA job (should be none)
	 */
	String QN_TRANSLATEDSTRINGID_FIND_BY_JOBID = "oabaTranslatedIdFindByJobId";

	/** JPQL used to implement {@link #QN_TRANSLATEDSTRINGID_FIND_BY_JOBID} */
	String JPQL_TRANSLATEDSTRINGID_FIND_BY_JOBID =
		"SELECT o FROM RecordIdStringTranslationEntity o "
				+ "WHERE o.jobId = :jobId ORDER BY o.translatedId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSLATEDSTRINGID_FIND_BY_JOBID}
	 */
	String PN_TRANSLATEDSTRINGID_FIND_BY_JOBID_JOBID = "jobId";

}
