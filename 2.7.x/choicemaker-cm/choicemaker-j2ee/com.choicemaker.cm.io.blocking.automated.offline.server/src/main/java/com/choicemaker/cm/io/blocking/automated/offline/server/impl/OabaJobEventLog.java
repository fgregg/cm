package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.PN_OABAPROCESSING_FIND_BY_JOBID_JOBID;
import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingJPA.QN_OABAPROCESSING_FIND_BY_JOBID;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;

/**
 * OabaJobEventLog restricts a logging context to a specific OABA job by
 * attaching an OABA job identifier to each OabaEvent instance that it records
 * or retrieves.
 *
 * @author rphall
 */
public class OabaJobEventLog implements OabaEventLog {

	private static final Logger logger = Logger.getLogger(OabaJobEventLog.class
			.getName());

	/**
	 * The name of a system property that can be set to "true" to turn on
	 * redundant order-by checking.
	 */
	public static final String PN_OABA_EVENT_ORDERBY_DEBUGGING =
		"OabaEventLogOrderByDebugging";

	// Don't use this directly; use isOrderByDebuggingRequested() instead
	private static Boolean _isOrderByDebuggingRequested = null;

	/**
	 * Checks the system property {@link #PN_SANITY_CHECK}
	 * and caches the result
	 */
	protected static boolean isOrderByDebuggingRequested() {
		if (_isOrderByDebuggingRequested == null) {
			String pn = PN_OABA_EVENT_ORDERBY_DEBUGGING;
			String defaultValue = Boolean.FALSE.toString();
			String value =
				System.getProperty(pn,defaultValue);
			_isOrderByDebuggingRequested = Boolean.valueOf(value);
		}
		boolean retVal = _isOrderByDebuggingRequested.booleanValue();
		if (retVal) {
			logger.info("OabaEventLog order-by debugging is enabled");
		}
		return retVal;
	}

	private final EntityManager em;

	private final OabaJob oabaJob;

	public OabaJobEventLog(EntityManager em, OabaJob job) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		if (job == null || !OabaJobEntity.isPersistent(job)) {
			throw new IllegalArgumentException("invalid OABA job: " + job);
		}
		this.em = em;
		this.oabaJob = job;
	}

	protected OabaProcessingEvent getCurrentOabaProcessingEvent() {
		Query query = em.createNamedQuery(QN_OABAPROCESSING_FIND_BY_JOBID);
		query.setParameter(PN_OABAPROCESSING_FIND_BY_JOBID_JOBID,
				oabaJob.getId());
		@SuppressWarnings("unchecked")
		List<OabaProcessingLogEntry> entries = query.getResultList();
		final OabaProcessingEvent retVal;
		if (entries.isEmpty()) {
			retVal = null;
		} else {
			retVal = entries.get(0);
			final Date mostRecent = retVal.getEventTimestamp();
			if (isOrderByDebuggingRequested()) {
				if (entries.size() > 1) {
					for (int i = 1; i < entries.size(); i++) {
						final OabaProcessingEvent e2 = entries.get(i);
						final Date d2 = e2.getEventTimestamp();
						if (mostRecent.compareTo(d2) < 0) {
							String summary =
								"Invalid OabaProcessingEvent ordering";
							String msg =
								createOrderingDetailMesssage(summary, retVal,
										e2);
							logger.severe(msg);
							throw new IllegalStateException(summary);
						} else if (mostRecent.compareTo(d2) == 0) {
							String summary =
								"Ambiguous OabaProcessingEvent timestamps";
							String msg =
								createOrderingDetailMesssage(summary, retVal,
										e2);
							logger.warning(msg);
						}
					}
				}
			}
		}
		return retVal;
	}

	private String createOrderingDetailMesssage(String summary,
			OabaProcessingEvent e1, OabaProcessingEvent e2) {
		final String INDENT = "   ";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(summary);
		pw.println(INDENT + "Most recent: " + e1.getOabaEvent() + " " +  e1);
		pw.println(INDENT + " Subsequent: " + e2.getOabaEvent() + " " +  e2);
		return sw.toString();
	}

	@Override
	public OabaEvent getCurrentOabaEvent() {
		OabaProcessingEvent ope = getCurrentOabaProcessingEvent();
		OabaEvent retVal = ope.getOabaEvent();
		return retVal;
	}

	@Override
	public int getCurrentOabaEventId() {
		return getCurrentOabaEvent().eventId;
	}

	@Override
	public String getCurrentOabaEventInfo() {
		OabaProcessingEvent ope = getCurrentOabaProcessingEvent();
		String retVal = ope.getEventInfo();
		return retVal;
	}

	@Override
	public void setCurrentOabaEvent(OabaEvent event) {
		setCurrentOabaEvent(event, null);
	}

	@Override
	public void setCurrentOabaEvent(OabaEvent event, String info) {
		logger.info("OABA processing event: " + event + " (job "
				+ this.oabaJob.getId() + ")");
		OabaProcessingLogEntry ope =
			new OabaProcessingLogEntry(oabaJob, event, info);
		em.persist(ope);
	}

	@Override
	public String toString() {
		return "OabaJobEventLog [jobId=" + oabaJob.getId() + "]";
	}

}
