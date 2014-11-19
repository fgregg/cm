package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for DefaultAbaSettings beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface OabaSettingsJPA {

	String DISCRIMINATOR_VALUE = "OABA";

	String CN_MAX_BLOCKSIZE = "MAX_BLOCKSIZE";
	String CN_MAX_CHUNKSIZE = "MAX_CHUNKSIZE";
	String CN_MAX_OVERSIZE = "MAX_OVERSIZE";
	String CN_MIN_FIELDS = "MIN_FIELDS";
	String CN_INTERVAL = "INTERVAL";

	String ID_GENERATOR_NAME = "ABA_SETTINGS";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "ABA_SETTINGS";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	 /** Name of the query that finds all persistent batch job instances */
	 String QN_OABA_FIND_ALL = "oabaSettingsFindAll";
	
	 /** JPQL used to implement {@link #QN_OABA_FIND_ALL} */
	 String JPQL_OABA_FIND_ALL = "Select oaba from OabaSettingsBean oaba";

}
