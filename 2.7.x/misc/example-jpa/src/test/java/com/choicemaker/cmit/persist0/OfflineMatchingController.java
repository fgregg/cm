package com.choicemaker.cmit.persist0;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.persist0.BatchJob;
import com.choicemaker.cm.persist0.OfflineMatchingBean;
import com.choicemaker.cm.persist0.OfflineMatchingBean.NamedQuery;

@Stateless
public class OfflineMatchingController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public OfflineMatchingBean save(OfflineMatchingBean batchJob) {
		if (batchJob.getId() == 0) {
			em.persist(batchJob);
		} else {
			batchJob = em.merge(batchJob);
		}
		return batchJob;
	}

	public OfflineMatchingBean find(long id) {
		OfflineMatchingBean batchJob = em.find(OfflineMatchingBean.class, id);
		return batchJob;
	}

	public List<OfflineMatchingBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<OfflineMatchingBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<OfflineMatchingBean>();
		}
		return entries;
	}

	public void delete(OfflineMatchingBean batchJob) {
		batchJob = em.merge(batchJob);
//		for (CMP_AuditEvent e : batchJob.getTimeStamps()) {
//			em.remove(e);
//		}
		em.remove(batchJob);
		em.flush();
	}

	public void detach(BatchJob batchJob) {
		em.detach(batchJob);
	}

}
