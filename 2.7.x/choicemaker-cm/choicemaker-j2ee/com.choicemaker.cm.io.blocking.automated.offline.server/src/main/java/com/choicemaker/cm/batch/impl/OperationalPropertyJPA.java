package com.choicemaker.cm.batch.impl;

public interface OperationalPropertyJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OPERATION_PROPERTY";

	/**
	 * Generated persistence key.
	 * 
	 * @see #ID_GENERATOR_NAME
	 */
	String CN_ID = "ID";

	/** Required link to a batch job */
	String CN_JOB_ID = "JOB_ID";

	/** Name of the property */
	String CN_NAME = "NAME";

	/** Value of the property */
	String CN_VALUE = "VALUE";

	String ID_GENERATOR_NAME = "OPERATIONAL_PROPERTY";
	String ID_GENERATOR_TABLE = "CMT_SEQUENCE";
	String ID_GENERATOR_PK_COLUMN_NAME = "SEQ_NAME";
	String ID_GENERATOR_PK_COLUMN_VALUE = "OP_PROP";
	String ID_GENERATOR_VALUE_COLUMN_NAME = "SEQ_COUNT";

	/**
	 * Name of a query that selects operational properties by job id and
	 * property name
	 */
	String QN_OPPROP_FIND_BY_JOB_PNAME = "opPropFindByJobPname";

	/** JPQL used to implement {@link #QN_SERVERCONFIG_FIND_BY_NAME} */
	String JPQL_OPPROP_FIND_BY_JOB_PNAME =
		"Select ope from OperationalPropertyEntity ope "
				+ "where ope.jobId = :jobId and ope.name = :name";

	/**
	 * Name of the parameter used to specify the jobId of
	 * {@link #JPQL_OPPROP_FIND_BY_JOB_PNAME}
	 */
	String PN_OPPROP_FIND_BY_JOB_PNAME_P1 = "jobId";

	/**
	 * Name of the parameter used to specify the property name of
	 * {@link #JPQL_OPPROP_FIND_BY_JOB_PNAME}
	 */
	String PN_OPPROP_FIND_BY_JOB_PNAME_P2 = "name";

	/**
	 * Name of a query that selects operational properties by job id and
	 * property name
	 */
	String QN_OPPROP_FINDALL_BY_JOB = "opPropFindAllByJob";

	/** JPQL used to implement {@link #QN_SERVERCONFIG_FINDALL_BY_NAME} */
	String JPQL_OPPROP_FINDALL_BY_JOB =
		"Select ope from OperationalPropertyEntity ope where ope.jobId = :jobId";

	/**
	 * Name of the parameter used to specify the jobId of
	 * {@link #JPQL_OPPROP_FINDALL_BY_JOB}
	 */
	String PN_OPPROP_FINDALL_BY_JOB_P1 = "jobId";

}