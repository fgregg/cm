package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for OabaJob beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface ServerConfigurationJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_SERVER_CONFIG";

	/**
	 * Generated id column.
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	String CN_CONFIGNAME = "CONFIG_NAME";

	String CN_UUID = "UUID";

	String CN_HOSTNAME = "HOST_NAME";

	String CN_MAXTHREADS = "MAX_THREADS";

	String CN_MAXCHUNKSIZE = "MAX_CHUNK_SIZE";

	String CN_MAXCHUNKCOUNT = "MAX_CHUNK_COUNT";

	String CN_FILE = "FILE_URI";

	String ID_GENERATOR_NAME = "SERVER_CONFIG";

	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";

	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";

	String ID_GENERATOR_PK_COLUMN_VALUE = "SERVER_CONFIG";

	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	/** Name of the query that finds all persistent server configurations */
	String QN_SERVERCONFIG_FIND_ALL = "serverConfigFindAll";

	/** JPQL used to implement {@link #QN_OABAJOB_FIND_ALL} */
	String JPQL_SERVERCONFIG_FIND_ALL =
		"Select sc from ServerConfigurationEntity sc";

	/**
	 * Name of the query that finds all persistent server configurations with a
	 * specified host name
	 */
	String QN_SERVERCONFIG_FIND_BY_HOSTNAME = "serverConfigFindByHostName";

	/** JPQL used to implement {@link #QN_OABAJOB_FIND_ALL} */
	String JPQL_SERVERCONFIG_FIND_BY_HOSTNAME =
		"Select sc from ServerConfigurationEntity sc where sc.hostName = :hostName";

	/**
	 * Name of the parameter used to specify the hostName of
	 * {@link #JPQL_SERVERCONFIG_FIND__BY_HOSTNAME}
	 */
	String PN_SERVERCONFIG_FIND_BY_HOSTNAME_P1 = "hostName";

	/**
	 * Name of the query that finds a persistent server configuration by a
	 * specified configuration name
	 */
	String QN_SERVERCONFIG_FIND_BY_NAME = "serverConfigFindByName";

	/** JPQL used to implement {@link #QN_SERVERCONFIG_FIND_BY_NAME} */
	String JPQL_SERVERCONFIG_FIND_BY_NAME =
		"Select sc from ServerConfigurationEntity sc where sc.name = :name";

	/**
	 * Name of the parameter used to specify the name of
	 * {@link #JPQL_SERVERCONFIG_FIND_BY_NAME}
	 */
	String PN_SERVERCONFIG_FIND_BY_NAME_P1 = "name";

	// /**
	// * Name of the query that finds all persistent server configurations
	// marked
	// * by {@link ServerConfiguration#ANY_HOST ANY_HOST}
	// */
	// String QN_SERVERCONFIG_FIND_ANY_HOST = "serverConfigFindAnyHost";
	//
	// /** JPQL used to implement {@link #QN_SERVERCONFIG_FIND_ANY_HOST} */
	// String JPQL_SERVERCONFIG_FIND_ANY_HOST =
	// "Select sc from ServerConfigurationEntity sc where sc.name = '"
	// + ServerConfiguration.ANY_HOST + "'";

}
