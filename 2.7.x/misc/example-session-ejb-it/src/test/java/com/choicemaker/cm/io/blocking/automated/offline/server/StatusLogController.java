package com.choicemaker.cm.io.blocking.automated.offline.server;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.server.StatusLogBean.NamedQuery;

@Stateless
public class StatusLogController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public StatusLogBean save(StatusLogBean statusLog) {
		if (statusLog.getJobId() == 0) {
			em.persist(statusLog);
		} else {
			em.merge(statusLog);
		}
		return statusLog;
	}

	public StatusLogBean find(long jobId) {
		StatusLogBean statusLog = em.find(StatusLogBean.class, jobId);
		return statusLog;
	}

	public List<StatusLogBean> findAll() {
		Query query = em.createNamedQuery(NamedQuery.FIND_ALL.name);
		@SuppressWarnings("unchecked")
		List<StatusLogBean> entries = query.getResultList();
		if (entries == null) {
			entries = new ArrayList<StatusLogBean>();
		}
		return entries;
	}

	public void delete(StatusLogBean statusLog) {
		statusLog = em.merge(statusLog);
		em.remove(statusLog);
		em.flush();
	}

	public void detach(StatusLogBean statusLog) {
		em.detach(statusLog);
	}

}
