package com.choicemaker.demo.it.persist0;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.demo.persist0.StatusLog;
import com.choicemaker.demo.persist0.StatusLogBean;
import com.choicemaker.demo.persist0.StatusLogBean.NamedQuery;

@Stateless
public class StatusLogController {
	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public StatusLogBean save(StatusLogBean statusLog) {
		if (statusLog.getJobId() == 0) {
			em.persist(statusLog);
		} else {
			statusLog = em.merge(statusLog);
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

	public void detach(StatusLog statusLog) {
		em.detach(statusLog);
	}

}
