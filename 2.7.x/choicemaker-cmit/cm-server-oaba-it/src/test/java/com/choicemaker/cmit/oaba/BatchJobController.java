package com.choicemaker.cmit.oaba;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean.NamedQuery;

/**
 * Currently, the OABA server doesn't define any Enterprise Java bean whose sole
 * job is to persist BatchJob instances. However, some OABA EJBs do use the
 * EJBConfiguration help class to manage job persistence. For these EJBs,
 * persistence is always done within a container-defined transaction. Since the
 * EJBConfiguration class is not an EJB, direct tests of this class must use
 * local transactions. This controller class allows one to test BatchJob
 * persistence under container-managed transactions, separately from whatever
 * else in going on OABA-defined EJBs.
 * 
 * @author rphall
 */
@Stateless
public class BatchJobController {

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public BatchJobBean save(BatchJobBean batchJob) {
		if (batchJob.getId() == 0) {
			em.persist(batchJob);
		} else {
			batchJob = em.merge(batchJob);
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
		// for (CMP_AuditEvent e : batchJob.getTimeStamps()) {
		// em.remove(e);
		// }
		em.remove(batchJob);
		em.flush();
	}

	public void detach(BatchJob batchJob) {
		em.detach(batchJob);
	}

}
