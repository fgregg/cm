/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
//import javax.jms.Topic;
//import javax.jms.TopicConnectionFactory;

/**
 * This object contains method to get JMS and EJB objects from the J2EE server.
 *
 * @author pcheung
 * @author rphall (migration to EJB3)
 *
 */
@Stateless
public class OabaProcessingControllerBean implements Serializable {

//	private static final Logger logger = Logger
//			.getLogger(OabaProcessingControllerBean.class.getName());

	/* As of 2010-03-10 */
	static final long serialVersionUID = 271;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	public OabaJobProcessing createPersistentProcessingLogForBatchJob(
			BatchJob oabaJob) {
		if (oabaJob == null) {
			throw new IllegalArgumentException("null batch job");
		}
		if (!OabaJobEntity.isPersistent(oabaJob)) {
			throw new IllegalArgumentException("non-persistent batch job");
		}
		final long jobId = oabaJob.getId();
		List<OabaProcessingEntity> entries = findProcessingLogsByJobId(jobId);
		assert entries == null || entries.size() <= 1;

		OabaJobProcessing retVal;
		if (entries == null || entries.size() == 0) {
			retVal = new OabaProcessingEntity(jobId);
			em.persist(retVal);
		} else {
			retVal = entries.get(0);
		}
		assert retVal != null;
		assert OabaProcessingEntity.isPersistent(retVal);

		return retVal;
	}

	private List<OabaProcessingEntity> findProcessingLogsByJobId(long id) {
		Query query =
			em.createNamedQuery(OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID);
		query.setParameter(
				OabaProcessingJPA.PN_OABAPROCESSING_FIND_BY_JOBID_JOBID, id);
		@SuppressWarnings("unchecked")
		List<OabaProcessingEntity> entries = query.getResultList();
		if (entries != null && entries.size() > 1) {
			throw new IllegalStateException(
					"multiple processing entries for job id: " + id);
		}
		return entries;
	}

	public OabaJobProcessing findProcessingLogByJobId(long id) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		if (OabaJobEntity.INVALID_ID == id) {
			throw new IllegalArgumentException("non-persistent job id: " + id);
		}
		List<OabaProcessingEntity> entries = findProcessingLogsByJobId(id);
		if (entries == null || entries.size() == 0) {
			throw new IllegalStateException(
					"no processing entries for job id: " + id);
		}
		assert entries.size() == 1;
		OabaJobProcessing retVal = entries.get(0);
		return retVal;
	}

	public OabaProcessing getProcessingLog(OabaJobMessage data) {
		OabaJobProcessing retVal = findProcessingLogByJobId(data.jobID);
		return retVal;
	}

	public OabaProcessing getProcessingLog(long jobID) {
		OabaJobProcessing retVal = findProcessingLogByJobId(jobID);
		return retVal;
	}

}
