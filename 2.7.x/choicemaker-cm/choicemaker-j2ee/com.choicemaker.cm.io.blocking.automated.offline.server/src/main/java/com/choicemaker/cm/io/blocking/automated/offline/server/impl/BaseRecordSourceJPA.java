package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

/**
 * Java Persistence API (JPA) for persistable record source beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface BaseRecordSourceJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_RECORD_SOURCE";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "TYPE";

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

	String ID_GENERATOR_NAME = "RECORD_SOURCE";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "RECORD_SOURCE";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

}
