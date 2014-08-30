package com.choicemaker.cmit.io.blocking.automated.offline.server;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParametersBean.NamedQuery;

@Stateless
public class BatchParametersController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchParametersBean save(BatchParametersBean batchParameters) {
		if (batchParameters.getId() == 0) {
			em.persist(batchParameters);
		} else {
			batchParameters = em.merge(batchParameters);
		}
		return batchParameters;
	}

	public BatchParametersBean find(long id) {
		BatchParametersBean batchParameters = em.find(BatchParametersBean.class, id);
		return batchParameters;
	}

	public List<BatchParametersBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<BatchParametersBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<BatchParametersBean>();
		}
		return entries;
	}

	public void delete(BatchParametersBean batchParameters) {
		batchParameters = em.merge(batchParameters);
		em.remove(batchParameters);
		em.flush();
	}

	public void detach(BatchParameters batchParameters) {
		em.detach(batchParameters);
	}

}
