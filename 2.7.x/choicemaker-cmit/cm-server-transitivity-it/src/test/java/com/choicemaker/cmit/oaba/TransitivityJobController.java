package com.choicemaker.cmit.oaba;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobJPA;

/**
 * An EJB used to test TransitivityJob beans within container-defined
 * transactions; see {@link BatchJobController} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
@Stateless
public class TransitivityJobController {

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchJobBean createBatchJobBean(String extId) {
		BatchJobBean retVal = new BatchJobBean(extId);
		em.persist(retVal);
		return retVal;
	}
	
	public TransitivityJobBean save(TransitivityJobBean job) {
		if (job.getId() == 0) {
			em.persist(job);
		} else {
			em.merge(job);
		}
		return job;
	}

	public TransitivityJobBean find(long id) {
		TransitivityJobBean job = em.find(TransitivityJobBean.class, id);
		return job;
	}

	public List<TransitivityJobBean> findAll() {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public List<TransitivityJobBean> findAllByParentId(long batchJobId) {
		Query query =
			em.createNamedQuery(TransitivityJobJPA.QN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID);
		query.setParameter(TransitivityJobJPA.PN_TRANSITIVITY_FIND_ALL_BY_PARENT_ID_BPARENTID, batchJobId);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public void deleteTransitivityJobAndParent(TransitivityJob job) {
		if (job != null) {
			final long parentId = job.getBatchParentId();
			assert parentId != BatchJob.INVALID_BATCHJOB_ID;

			if (BatchJobBean.isPersistent(job)) {
				final long jobId = job.getId();
				TransitivityJob persistentJob = em.find(TransitivityJobBean.class, jobId);
				if (persistentJob != null) {
					em.remove(persistentJob);
				}
			}
			
			BatchJob parent = em.find(BatchJobBean.class, parentId);
			if (parent != null) {
				em.remove(parent);
			}
		}
	}

	public void detach(TransitivityJob job) {
		em.detach(job);
	}

}
