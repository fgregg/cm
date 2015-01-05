package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingEvent;

/**
 * OabaProcessingLog restricts a logging context to a specific OABA job by
 * attaching an OABA job identifier to each OabaEvent instance that it records
 * or retrieves.
 *
 * @author rphall
 */
public class OabaProcessingLog implements OabaEventLog {

	private static final Logger logger = Logger.getLogger(OabaProcessingLog.class
			.getName());

	private final EntityManager em;
	private final OabaJob oabaJob;

	public OabaProcessingLog(EntityManager em, OabaJob job) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !OabaJobEntity.isPersistent(job)) {
			throw new IllegalArgumentException("invalid OABA job: " + job);
		}
		this.em = em;
		this.oabaJob = job;
	}

	protected OabaProcessingEvent getCurrentOabaProcessingEvent() {
		return OabaProcessingControllerBean.getCurrentOabaProcessingEvent(em,
				oabaJob);
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
		OabaProcessingControllerBean.updateStatus(em, oabaJob, event,
				new Date(), info);
	}

	@Override
	public String toString() {
		return "OabaProcessingLog [jobId=" + oabaJob.getId() + "]";
	}

}
