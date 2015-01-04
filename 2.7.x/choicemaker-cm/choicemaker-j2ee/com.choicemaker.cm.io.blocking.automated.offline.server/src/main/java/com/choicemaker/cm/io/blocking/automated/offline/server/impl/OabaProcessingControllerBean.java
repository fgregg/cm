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

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;

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

	/**
	 * The name of a system property that can be set to "true" to turn on
	 * redundant order-by checking.
	 */
	public static final String PN_OABA_EVENT_ORDERBY_DEBUGGING =
		"OabaEventLogOrderByDebugging";

	// Don't use this directly; use isOrderByDebuggingRequested() instead
	static Boolean _isOrderByDebuggingRequested = null;

	static List<OabaProcessingLogEntry> findProcessingLogEntriesByJobId(
			EntityManager em, long id) {
		Query query =
			em.createNamedQuery(OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID);
		query.setParameter(
				OabaProcessingJPA.PN_OABAPROCESSING_FIND_BY_JOBID_JOBID, id);
		@SuppressWarnings("unchecked")
		List<OabaProcessingLogEntry> entries = query.getResultList();
		return entries;
	}

	static OabaProcessingEvent updateStatus(EntityManager em, OabaJob job,
			OabaEvent event, Date timestamp, String info) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !OabaJobEntity.isPersistent(job)) {
			throw new IllegalArgumentException("invalid OABA job");
		}
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("null timestamp");
		}
		OabaProcessingLogEntry ope =
			new OabaProcessingLogEntry(job, event, info);
		em.persist(ope);
		return ope;
	}

	static void updateStatusWithNotification(EntityManager em,
			JMSContext jmsContext, Topic oabaStatusTopic, OabaJob job,
			OabaEvent event, Date timestamp, String info) {
		if (jmsContext == null) {
			throw new IllegalStateException("null JMS context");
		}
		if (oabaStatusTopic == null) {
			throw new IllegalStateException("null JMS topic");
		}
		OabaProcessingEvent ope =
			updateStatus(em, job, event, new Date(), info);
		OabaNotification data =
			new OabaNotification(ope);
		ObjectMessage message = jmsContext.createObjectMessage(data);
		JMSProducer sender = jmsContext.createProducer();
		logger.info(MessageBeanUtils.topicInfo("Sending", oabaStatusTopic, data));
		sender.send(oabaStatusTopic, message);
		logger.info(MessageBeanUtils.topicInfo("Sent", oabaStatusTopic, data));
	}

	/**
	 * Checks the system property {@link #PN_SANITY_CHECK} and caches the result
	 */
	static boolean isOrderByDebuggingRequested() {
		if (_isOrderByDebuggingRequested == null) {
			String pn = PN_OABA_EVENT_ORDERBY_DEBUGGING;
			String defaultValue = Boolean.FALSE.toString();
			String value = System.getProperty(pn, defaultValue);
			_isOrderByDebuggingRequested = Boolean.valueOf(value);
		}
		boolean retVal = _isOrderByDebuggingRequested.booleanValue();
		if (retVal) {
			logger.info("OabaEventLog order-by debugging is enabled");
		}
		return retVal;
	}

	static OabaProcessingEvent getCurrentOabaProcessingEvent(EntityManager em, OabaJob oabaJob) {
		List<OabaProcessingLogEntry> entries =
			OabaProcessingControllerBean.findProcessingLogEntriesByJobId(em,
					oabaJob.getId());
		final OabaProcessingEvent retVal;
		if (entries.isEmpty()) {
			retVal = null;
		} else {
			retVal = entries.get(0);
			if (isOrderByDebuggingRequested()) {
				final Date mostRecent = retVal.getEventTimestamp();
				if (entries.size() > 1) {
					for (int i = 1; i < entries.size(); i++) {
						final OabaProcessingEvent e2 = entries.get(i);
						final Date d2 = e2.getEventTimestamp();
						if (mostRecent.compareTo(d2) < 0) {
							String summary =
								"Invalid OabaProcessingEvent ordering";
							String msg =
								OabaProcessingControllerBean.createOrderingDetailMesssage(summary, retVal,
										e2);
							logger.severe(msg);
							throw new IllegalStateException(summary);
						} else if (mostRecent.compareTo(d2) == 0) {
							String summary =
								"Ambiguous OabaProcessingEvent timestamps";
							String msg =
								OabaProcessingControllerBean.createOrderingDetailMesssage(summary, retVal,
										e2);
							logger.warning(msg);
						}
					}
				}
			}
		}
		return retVal;
	}

	static String createOrderingDetailMesssage(String summary,
			OabaProcessingEvent e1, OabaProcessingEvent e2) {
		final String INDENT = "   ";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(summary);
		pw.println(INDENT + "Most recent: " + e1.getOabaEvent() + " " + e1);
		pw.println(INDENT + " Subsequent: " + e2.getOabaEvent() + " " + e2);
		return sw.toString();
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	public List<OabaProcessingLogEntry> findProcessingLogEntriesByJobId(long id) {
		return findProcessingLogEntriesByJobId(em,id);
	}

	public OabaEventLog getProcessingLog(OabaJob job) {
		return new OabaJobEventLog(em, job);
	}

	public void updateStatusWithNotification(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		updateStatusWithNotification(em, jmsContext, oabaStatusTopic, job, event,
				timestamp, info);
	}

	public OabaEvent getCurrentOabaEvent(OabaJob oabaJob) {
		OabaEvent retVal = null;
		OabaProcessingEvent ope = getCurrentOabaProcessingEvent(em,oabaJob);
		if (ope != null) {
			retVal = ope.getOabaEvent();
		}
		return retVal;
	}

}
