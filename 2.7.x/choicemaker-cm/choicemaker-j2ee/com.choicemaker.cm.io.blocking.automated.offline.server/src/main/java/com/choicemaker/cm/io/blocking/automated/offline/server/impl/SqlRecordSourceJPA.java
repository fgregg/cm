package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.args.PersistableSqlRecordSource;

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
public interface SqlRecordSourceJPA extends BaseRecordSourceJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_SQL_RECORDSOURCE";

	/**
	 * Value of the discriminator column used to mark BatchJob types (and not
	 * sub-types)
	 */
	String DISCRIMINATOR_VALUE = PersistableSqlRecordSource.TYPE;

	String CN_CM_IO_CLASS = "CLASS";

	String CN_DATASOURCE = "DATA_SOURCE";

	String CN_SQL = "SQL";

	String CN_MODEL = "MODEL";

	String CN_DBCONFIG = "DBCONFIG";

	/** Name of the query that finds all persistent batch job instances */
	String QN_SQLRS_FIND_ALL = "sqlRecordSourceFindAll";

	/** JPQL used to implement {@link #QN_SQLRS_FIND_ALL} */
	String JPQL_SQLRS_FIND_ALL = "Select rs from SqlRecordSourceEntity rs";

}
