package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import com.choicemaker.cm.batch.impl.BatchJobJPA;


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
public interface OabaJobJPA extends BatchJobJPA {

	/**
	 * Value of the discriminator column used to mark BatchJob types (and not
	 * sub-types)
	 */
	String DISCRIMINATOR_VALUE = "OABA";

	/** Name of the query that finds all persistent batch job instances */
	String QN_OABAJOB_FIND_ALL = "oabaJobFindAll";

	/** JPQL used to implement {@link #QN_OABAJOB_FIND_ALL} */
	String JPQL_OABAJOB_FIND_ALL = "Select job from OabaJobEntity job";

}
