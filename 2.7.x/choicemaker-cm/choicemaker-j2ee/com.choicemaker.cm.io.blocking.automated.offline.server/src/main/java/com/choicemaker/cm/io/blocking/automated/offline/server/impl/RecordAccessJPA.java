package com.choicemaker.cm.io.blocking.automated.offline.server.impl;


public interface RecordAccessJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_RECORD_ACCESS";

	/**
	 * Generated id column.
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	/** The name of the model used for record access */
	String CN_MODELNAME = "MODEL_NAME";

	/** Description of the database accessor */
	String CN_DB_TYPE = "DB_TYPE";

	/** The fully qualified name of the database accessor */
	String CN_DB_ACCESSOR = "DB_ACCESSOR";

	/** The name of a database configuration used to retrieve query records */
	String CN_QUERY_CONFIGURATION = "QUERY";

	/** The name of a database configuration used to retrieve reference records */
	String CN_REFERENCE_CONFIGURATION = "REFERENCE";

	/**
	 * The name of a blocking configuration used with query and reference
	 * records
	 */
	String CN_BLOCKING_CONFIGURATION = "BLOCKING";

	String ID_GENERATOR_NAME = "OABA_BATCHJOB";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "BATCHJOB";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

}
