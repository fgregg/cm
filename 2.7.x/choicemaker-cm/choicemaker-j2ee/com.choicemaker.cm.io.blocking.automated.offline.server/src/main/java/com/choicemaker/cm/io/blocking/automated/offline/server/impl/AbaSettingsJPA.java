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
public interface AbaSettingsJPA {

	/** Name of the table that persists ABA settings */
	String TABLE_NAME = "CMT_ABA_SETTINGS";

	String DISCRIMINATOR_COLUMN = "TYPE";

	String DISCRIMINATOR_VALUE = "ABA";

	String CN_ID = "ID";
	String CN_TYPE = DISCRIMINATOR_COLUMN;

	String CN_LIMIT_BLOCKSET = "LIMIT_BLOCKSET";
	String CN_LIMIT_SINGLESET = "LIMIT_SINGLESET";
	String CN_LIMIT_SINGLETABLE = "LIMIT_SINGLETABLE";

	String ID_GENERATOR_NAME = "ABA_SETTINGS";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "ABA_SETTINGS";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	 /** Name of the query that finds all persistent ABA settings */
	 String QN_ABA_FIND_ALL = "abaSettingsFindAll";
	
	 /** JPQL used to implement {@link #QN_ABA_FIND_ALL} */
	 String JPQL_ABA_FIND_ALL = "Select aba from AbaSettingsBean aba";

}
