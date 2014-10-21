package com.choicemaker.cmit.oaba;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean.NamedQuery;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;

/**
 * An EJB used to test BatchParameter beans within container-defined
 * transactions; see {@link BatchJobController} as an example of a similar
 * controller.
 * 
 * @author rphall
 */
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
		BatchParametersBean batchParameters =
			em.find(BatchParametersBean.class, id);
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
