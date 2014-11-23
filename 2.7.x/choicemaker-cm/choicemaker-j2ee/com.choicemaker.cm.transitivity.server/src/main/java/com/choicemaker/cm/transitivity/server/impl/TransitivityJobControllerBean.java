package com.choicemaker.cm.transitivity.server.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * An EJB used to test TransitivityJob beans within container-defined
 * transactions; see {@link OabaJobControllerBean} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class TransitivityJobControllerBean {

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public TransitivityJobEntity save(TransitivityJobEntity job) {
		if (job == null) {
			throw new IllegalArgumentException("null job");
		}
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public TransitivityJobEntity findTransitivityJob(long id) {
		TransitivityJobEntity job = em.find(TransitivityJobEntity.class, id);
		return job;
	}

	public List<TransitivityJobEntity> findAllTransitivityJobs() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJobEntity> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJobEntity>();
		}
		return retVal;
	}

	public List<TransitivityJobEntity> findAllByParentId(long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(
				TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID,
				batchJobId);
		@SuppressWarnings("unchecked")
		List<TransitivityJobEntity> retVal = query.getResultList();
		if (retVal == null) {
			retVal = new ArrayList<TransitivityJobEntity>();
		}
		return retVal;
	}

	public void detach(TransitivityJob job) {
		em.detach(job);
	}

}
