package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.batch.impl.BatchJobJPA;

/**
 * Java Persistence API (JPA) for OabaParameters beans.<br/>
 * Prefixes:<ul>
 * <li> JPQL -- Java Persistence Query Language</li>
 * <li> QN -- Query Name</li>
 * <li> CN -- Column Name</li>
 * </ul>
 * @author rphall
 */
public interface OabaParametersJPA {

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCH_PARAMS";

	String CN_ID = "ID";
	String CN_STAGE_MODEL = "STAGE_MODEL";
	String CN_MASTER_MODEL = "MASTER_MODEL";
	String CN_MAX_SINGLE = "MAX_SINGLE";
	String CN_LOW_THRESHOLD = "LOW_THRESHOLD";
	String CN_HIGH_THRESHOLD = "HIGH_THRESHOLD";
	String CN_STAGE_RS = "STAGE_RS";
	String CN_MASTER_RS = "MASTER_RS";

	String ID_GENERATOR_NAME = "OABA_BATCHPARAMS";

	String ID_GENERATOR_TABLE = BatchJobJPA.ID_GENERATOR_TABLE;

	String ID_GENERATOR_PK_COLUMN_NAME = BatchJobJPA.ID_GENERATOR_PK_COLUMN_NAME;

	String ID_GENERATOR_PK_COLUMN_VALUE = "OABA_BATCHPARAMS";

	String ID_GENERATOR_VALUE_COLUMN_NAME = BatchJobJPA.ID_GENERATOR_VALUE_COLUMN_NAME;
	
	/**
	 * Name of the query that finds all persistent batch parameter instances
	 */
	String QN_BATCHPARAMETERS_FIND_ALL =
			"batchParametersFindAll";

	/** JPQL used to implement {@link #QN_BATCHPARAMETERS_FIND_ALL} */
	String JPQL_BATCHJOB_FIND_ALL =
			"Select params from OabaParametersEntity params";

//	/**
//	 * Name of the query that finds the parameters associated with a particular
//	 * OabaJob
//	 */
//	String QN_BATCHPARAMETERS_FIND_BY_JOB_ID =
//			"batchParametersFindByJobId";
//
//	/** JPQL used to implement {@link #QN_BATCHPARAMETERS_FIND_ALL} */
//	String JPQL_BATCHPARAMETERS_FIND_BY_JOB_ID =
//			"Select p from OabaParametersEntity p where p.jobId = :jobId";
//
//	/**
//	 * Name of the parameter used to specify the jobId parameter of
//	 * {@link #JPQL_BATCHPARAMETERS_FIND_BY_JOB_ID}
//	 */
//	String PN_BATCHPARAMETERS_FIND_BY_JOBID_P1 = "jobId";

}