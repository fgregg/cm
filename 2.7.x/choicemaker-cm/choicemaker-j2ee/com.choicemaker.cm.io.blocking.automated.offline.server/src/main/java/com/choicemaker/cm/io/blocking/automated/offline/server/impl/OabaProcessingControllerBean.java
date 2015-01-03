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

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * This stateless EJB provides OABA, job-specific processing logs and a
 * stand-alone methods for creating and finding log entries.
 *
 * @author pcheung
 * @author rphall (migration to EJB3)
 */
@Stateless
public class OabaProcessingControllerBean {
	
	private static final Logger logger = Logger
			.getLogger(OabaProcessingControllerBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	public List<OabaProcessingLogEntry> findProcessingLogEntriesByJobId(long id) {
		Query query =
			em.createNamedQuery(OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID);
		query.setParameter(
				OabaProcessingJPA.PN_OABAPROCESSING_FIND_BY_JOBID_JOBID, id);
		@SuppressWarnings("unchecked")
		List<OabaProcessingLogEntry> entries = query.getResultList();
		return entries;
	}

	public OabaEventLog getProcessingLog(OabaJob job) {
		return new OabaJobEventLog(job, this);
	}

	/**
	 * This method sends a message to the UpdateStatusMDB message bean.
	 */
	public void updateOabaProcessingStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		if (job == null || !OabaJobEntity.isPersistent(job)) {
			throw new IllegalArgumentException("invalid OABA job");
		}
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("null timestamp");
		}
		if (jmsContext == null || oabaStatusTopic == null) {
			throw new IllegalStateException("null JMS instance data");
		}
		OabaProcessingLogEntry ope =
			new OabaProcessingLogEntry(job, event, info);
		em.persist(ope);
		OabaNotification data =
			new OabaNotification(ope);
		ObjectMessage message = jmsContext.createObjectMessage(data);
		JMSProducer sender = jmsContext.createProducer();
		logger.info(MessageBeanUtils.topicInfo("Sending", oabaStatusTopic, data));
		sender.send(oabaStatusTopic, message);
		logger.info(MessageBeanUtils.topicInfo("Sent", oabaStatusTopic, data));
	}

}
