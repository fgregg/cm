package com.choicemaker.cmit.transitivity.server;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.persist.TransitivityJob;
import com.choicemaker.cm.persist.TransitivityJobBean;
import com.choicemaker.cm.persist.TransitivityJobBean.NamedQuery;

@Stateless
public class TransitivityJobController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public TransitivityJobBean save(TransitivityJobBean batchJob) {
		if (batchJob.getId() == 0) {
			em.persist(batchJob);
		} else {
			em.merge(batchJob);
		}
		return batchJob;
	}

	public TransitivityJobBean find(long id) {
		TransitivityJobBean batchJob = em.find(TransitivityJobBean.class, id);
		return batchJob;
	}

	public List<TransitivityJobBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<TransitivityJobBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<TransitivityJobBean>();
		}
		return entries;
	}

	public void delete(TransitivityJobBean batchJob) {
		batchJob = em.merge(batchJob);
		em.remove(batchJob);
		em.flush();
	}

	public void detach(TransitivityJob batchJob) {
		em.detach(batchJob);
	}

}
