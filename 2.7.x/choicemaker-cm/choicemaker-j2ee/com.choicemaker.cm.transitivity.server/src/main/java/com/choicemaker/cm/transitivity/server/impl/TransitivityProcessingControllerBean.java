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
package com.choicemaker.cm.transitivity.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean.PN_OABA_EVENT_ORDERBY_DEBUGGING;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventJPA.PN_TRANSPROCESSING_DELETE_BY_JOBID_JOBID;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventJPA.PN_TRANSPROCESSING_FIND_BY_JOBID_JOBID;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventJPA.QN_TRANSPROCESSING_DELETE_BY_JOBID;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventJPA.QN_TRANSPROCESSING_FIND_ALL;
import static com.choicemaker.cm.transitivity.server.impl.TransitivityProcessingEventJPA.QN_TRANSPROCESSING_FIND_BY_JOBID;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
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

import com.choicemaker.cm.transitivity.core.TransitivityEvent;
import com.choicemaker.cm.transitivity.core.TransitivityEventLog;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityProcessingController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityProcessingEvent;

/**
 * This stateless EJB provides OABA, job-specific processing logs and a
 * stand-alone methods for creating and finding log entries.
 *
 * @author pcheung
 * @author rphall (migration to EJB3)
 */
@Stateless
public class TransitivityProcessingControllerBean implements TransitivityProcessingController {

	private static final Logger logger = Logger
			.getLogger(TransitivityProcessingControllerBean.class.getName());

	// Don't use this directly; use isOrderByDebuggingRequested() instead
	static Boolean _isOrderByDebuggingRequested = null;

	static List<TransitivityProcessingEventEntity> findProcessingLogEntriesByJobId(
			EntityManager em, long id) {
		Query query = em.createNamedQuery(QN_TRANSPROCESSING_FIND_BY_JOBID);
		query.setParameter(PN_TRANSPROCESSING_FIND_BY_JOBID_JOBID, id);
		@SuppressWarnings("unchecked")
		List<TransitivityProcessingEventEntity> entries = query.getResultList();
		if (entries == null) {
			entries = Collections.emptyList();
		}
		return entries;
	}

	static List<TransitivityProcessingEventEntity> findAllTransitivityProcessingEvents(
			EntityManager em) {
		Query query = em.createNamedQuery(QN_TRANSPROCESSING_FIND_ALL);
		@SuppressWarnings("unchecked")
		List<TransitivityProcessingEventEntity> entries = query.getResultList();
		if (entries == null) {
			entries = Collections.emptyList();
		}
		return entries;
	}

	static int deleteProcessingLogEntriesByJobId(EntityManager em, long id) {
		Query query = em.createNamedQuery(QN_TRANSPROCESSING_DELETE_BY_JOBID);
		query.setParameter(PN_TRANSPROCESSING_DELETE_BY_JOBID_JOBID, id);
		int deletedCount = query.executeUpdate();
		return deletedCount;
	}

	static TransitivityProcessingEvent updateStatus(EntityManager em, TransitivityJob job,
			TransitivityEvent event, Date timestamp, String info) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !job.isPersistent()) {
			throw new IllegalArgumentException("invalid OABA job");
		}
		if (event == null) {
			throw new IllegalArgumentException("null event");
		}
		if (timestamp == null) {
			throw new IllegalArgumentException("null timestamp");
		}
		TransitivityProcessingEventEntity ope =
			new TransitivityProcessingEventEntity(job, event, info);
		em.persist(ope);
		return ope;
	}

	static void updateStatusWithNotification(EntityManager em,
			JMSContext jmsContext, Topic oabaStatusTopic, TransitivityJob job,
			TransitivityEvent event, Date timestamp, String info) {
		if (jmsContext == null) {
			throw new IllegalStateException("null JMS context");
		}
		if (oabaStatusTopic == null) {
			throw new IllegalStateException("null JMS topic");
		}
		TransitivityProcessingEvent ope =
			updateStatus(em, job, event, new Date(), info);
		TransitivityNotification data = new TransitivityNotification(ope);
		ObjectMessage message = jmsContext.createObjectMessage(data);
		JMSProducer sender = jmsContext.createProducer();
		logger.info(MessageBeanUtils
				.topicInfo("Sending", oabaStatusTopic, data));
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
			logger.info("TransitivityEventLog order-by debugging is enabled");
		}
		return retVal;
	}

	static TransitivityProcessingEvent getCurrentTransitivityProcessingEvent(EntityManager em,
			TransitivityJob TransitivityJob) {
		List<TransitivityProcessingEventEntity> entries =
			TransitivityProcessingControllerBean.findProcessingLogEntriesByJobId(em,
					TransitivityJob.getId());
		final TransitivityProcessingEvent retVal;
		if (entries == null || entries.isEmpty()) {
			retVal = null;
		} else {
			retVal = entries.get(0);
			if (isOrderByDebuggingRequested()) {
				final Date mostRecent = retVal.getEventTimestamp();
				if (entries.size() > 1) {
					for (int i = 1; i < entries.size(); i++) {
						final TransitivityProcessingEvent e2 = entries.get(i);
						final Date d2 = e2.getEventTimestamp();
						if (mostRecent.compareTo(d2) < 0) {
							String summary =
								"Invalid TransitivityProcessingEvent ordering";
							String msg =
								TransitivityProcessingControllerBean
										.createOrderingDetailMesssage(summary,
												retVal, e2);
							logger.severe(msg);
							throw new IllegalStateException(summary);

						} else if (mostRecent.compareTo(d2) == 0) {
							// Timestamps by themselves are ambigous
							// if events are very close together, but
							// may be disambiguated by ordering of ids
							String summary =
								"Ambiguous TransitivityProcessingEvent timestamps";
							String msg =
								TransitivityProcessingControllerBean
										.createOrderingDetailMesssage(summary,
												retVal, e2);
							logger.fine(msg);
						}
					}
				}
			}
		}
		return retVal;
	}

	static String createOrderingDetailMesssage(String summary,
			TransitivityProcessingEvent e1, TransitivityProcessingEvent e2) {
		final String INDENT = "   ";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(summary);
		pw.println(INDENT + "Most recent: " + e1.getTransitivityEvent() + " " + e1);
		pw.println(INDENT + " Subsequent: " + e2.getTransitivityEvent() + " " + e2);
		return sw.toString();
	}

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	@Override
	public List<TransitivityProcessingEventEntity> findTransitivityProcessingEventsByJobId(
			long id) {
		return findProcessingLogEntriesByJobId(em, id);
	}

	@Override
	public List<TransitivityProcessingEventEntity> findAllTransitivityProcessingEvents() {
		return findAllTransitivityProcessingEvents(em);
	}

	@Override
	public int deleteTransitivityProcessingEventsByJobId(long id) {
		return deleteProcessingLogEntriesByJobId(em, id);
	}

	@Override
	public TransitivityEventLog getProcessingLog(TransitivityJob job) {
		return new TransitivityProcessingLog(em, job);
	}

	@Override
	public void updateStatusWithNotification(TransitivityJob job, TransitivityEvent event,
			Date timestamp, String info) {
		updateStatusWithNotification(em, jmsContext, oabaStatusTopic, job,
				event, timestamp, info);
	}

	@Override
	public TransitivityEvent getCurrentTransitivityEvent(TransitivityJob TransitivityJob) {
		TransitivityEvent retVal = null;
		TransitivityProcessingEvent ope = getCurrentTransitivityProcessingEvent(em, TransitivityJob);
		if (ope != null) {
			retVal = ope.getTransitivityEvent();
		}
		return retVal;
	}

}
