package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.batch.impl.BatchJobJPA;

/**
 * Java Persistence API (JPA) for OabaParameters beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * <li>DV -- Discriminator Value</li>
 * </ul>
 * 
 * @author rphall
 */
public interface AbstractParametersJPA {

	/** Default value when no jobId is assigned */
	public static final long INVALID_ID = 0;

	/** Name of the table that persists batch job data */
	String TABLE_NAME = "CMT_OABA_BATCH_PARAMS";

	String DISCRIMINATOR_COLUMN = "TYPE";

	String DV_ABSTRACT = "ABSTRACT";

	String DV_OABA = "OABA";

	String CN_ID = "ID";

	String CN_TYPE = DISCRIMINATOR_COLUMN;

	String CN_MODEL = "MODEL";

	String CN_MAX_SINGLE = "MAX_SINGLE";

	String CN_LOW_THRESHOLD = "LOW_THRESHOLD";

	String CN_HIGH_THRESHOLD = "HIGH_THRESHOLD";

	String CN_QUERY_RS = "STAGE_ID";

	String CN_QUERY_RS_TYPE = "STAGE_TYPE";

	String CN_REFERENCE_RS = "MASTER_ID";

	String CN_REFERENCE_RS_TYPE = "MASTER_TYPE";

	String CN_TASK = "TASK";

	/** Used by TransitivityParametersEntity */
	String CN_FORMAT = "FORMAT";

	/** Used by TransitivityParametersEntity */
	String CN_GRAPH = "GRAPH";

	String ID_GENERATOR_NAME = "BATCHPARAMS";

	String ID_GENERATOR_TABLE = BatchJobJPA.ID_GENERATOR_TABLE;

	String ID_GENERATOR_PK_COLUMN_NAME =
		BatchJobJPA.ID_GENERATOR_PK_COLUMN_NAME;

	String ID_GENERATOR_PK_COLUMN_VALUE = "BATCHPARAMS";

	String ID_GENERATOR_VALUE_COLUMN_NAME =
		BatchJobJPA.ID_GENERATOR_VALUE_COLUMN_NAME;

	/**
	 * Name of the query that finds all persistent parameter instances
	 */
	String QN_PARAMETERS_FIND_ALL = "abstractParametersFindAll";

	/** JPQL used to implement {@link #QN_PARAMETERS_FIND_ALL} */
	String JPQL_PARAMETERS_FIND_ALL =
		"Select p from AbstractParametersEntity p";

	/**
	 * Name of the query that finds all persistent OABA parameter instances
	 */
	String QN_OABAPARAMETERS_FIND_ALL = "oabaParametersFindAll";

	/** JPQL used to implement {@link #QN_OABAPARAMETERS_FIND_ALL} */
	String JPQL_OABAPARAMETERS_FIND_ALL =
		"Select p from OabaParametersEntity p";

}
