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
	String CN_MODEL = "MODEL";
	String CN_MAX_SINGLE = "MAX_SINGLE";
	String CN_LOW_THRESHOLD = "LOW_THRESHOLD";
	String CN_HIGH_THRESHOLD = "HIGH_THRESHOLD";
	String CN_STAGE_RS = "STAGE_ID";
	String CN_STAGE_RS_TYPE = "STAGE_TYPE";
	String CN_MASTER_RS = "MASTER_ID";
	String CN_MASTER_RS_TYPE = "MASTER_TYPE";
	String CN_TASK = "TASK";

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
	String JPQL_BATCHPARAMETERS_FIND_ALL =
			"Select params from OabaParametersEntity params";

}
