package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.NamedQuery;

@Stateless
public class BatchJobController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchJobBean save(BatchJobBean batchJob) {
		if (batchJob.getId() == 0) {
			em.persist(batchJob);
		} else {
			em.merge(batchJob);
		}
		return batchJob;
	}

	public BatchJobBean find(long id) {
		BatchJobBean batchJob = em.find(BatchJobBean.class, id);
		return batchJob;
	}

	public List<BatchJobBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<BatchJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<BatchJobBean>();
		}
		return entries;
	}

	public void delete(BatchJobBean batchJob) {
		batchJob = em.merge(batchJob);
		em.remove(batchJob);
		em.flush();
	}

	public void detach(BatchJobBean batchJob) {
		em.detach(batchJob);
	}

}
