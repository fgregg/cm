package com.choicemaker.cm.transitivity.server.impl;

/**
 * Java Persistence API (JPA) for TransitivityBatchProcessingEvent beans.<br/>
 * Prefixes:
 * <ul>
 * <li>JPQL -- Java Persistence Query Language</li>
 * <li>QN -- Query Name</li>
 * <li>CN -- Column Name</li>
 * </ul>
 * 
 * @author rphall
 */
public interface TransitivityProcessingEventJPA {

	/**
	 * Value of the discriminator column used to mark TransitivityBatchProcessingEvent types
	 * (and not sub-types)
	 */
	String DISCRIMINATOR_VALUE = "TRANS";

	/** Name of the query that finds all persistent status entries */
	String QN_TRANSPROCESSING_FIND_ALL = "transProcessingFindAll";

	/** JPQL used to implement {@link #QN_TRANSPROCESSING_FIND_ALL} */
	String JPQL_TRANSPROCESSING_FIND_ALL =
		"Select o from TransitivityProcessingEventEntity o";

	/**
	 * Name of the query that finds all persistent status entries for a
	 * particular Transitivity job, ordered by descending timestamp
	 */
	String QN_TRANSPROCESSING_FIND_BY_JOBID = "transProcessingFindByJobId";

	/** JPQL used to implement {@link #QN_TRANSPROCESSING_FIND_BY_JOBID} */
	String JPQL_TRANSPROCESSING_FIND_BY_JOBID =
		"SELECT o FROM TransitivityProcessingEventEntity o WHERE o.jobId = :jobId "
				+ "ORDER BY o.eventTimestamp DESC, o.id DESC";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSPROCESSING_FIND_BY_JOBID}
	 */
	String PN_TRANSPROCESSING_FIND_BY_JOBID_JOBID = "jobId";

	/**
	 * Name of the query that deletes all persistent status entries for a
	 * particular Transitivity job
	 */
	String QN_TRANSPROCESSING_DELETE_BY_JOBID = "transProcessingDeleteByJobId";

	/** JPQL used to implement {@link #QN_TRANSPROCESSING_DELETE_BY_JOBID} */
	String JPQL_TRANSPROCESSING_DELETE_BY_JOBID =
		"DELETE FROM TransitivityProcessingEventEntity o WHERE o.jobId = :jobId";

	/**
	 * Name of the parameter used to specify the jobId parameter of
	 * {@link #QN_TRANSPROCESSING_DELETE_BY_JOBID}
	 */
	String PN_TRANSPROCESSING_DELETE_BY_JOBID_JOBID = "jobId";

}
