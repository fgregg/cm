package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.args.PersistableSqlRecordSource;
import com.choicemaker.cm.batch.impl.BatchJobJPA;


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
public interface SqlRecordSourceJPA extends BatchJobJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_SQL_RECORDSOURCE";

	/** Name of the column used to distinguish between batch jobs and sub-types */
	String DISCRIMINATOR_COLUMN = "TYPE";

	/**
	 * Value of the discriminator column used to mark OabaJob types (and not
	 * sub-types)
	 */
	String DISCRIMINATOR_VALUE = PersistableSqlRecordSource.TYPE;

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
	
	String CN_CLASSNAME = "CLASS";

	String CN_DATASOURCE = "DATA_SOURCE";

	String CN_SQL = "SQL";

	String CN_MODEL = "MODEL";

	String CN_DBCONFIG = "DBCONFIG";

	/** Name of the query that finds all persistent batch job instances */
	String QN_SQLRS_FIND_ALL = "sqlRecordSourceFindAll";

	/** JPQL used to implement {@link #QN_SQLRS_FIND_ALL} */
	String JPQL_SQLRS_FIND_ALL = "Select rs from SqlRecordSourceEntity rs";

}
