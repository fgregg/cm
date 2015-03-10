package com.choicemaker.cm.transitivity.server.impl;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.choicemaker.cm.transitivity.core.TransitivityEvent;
import com.choicemaker.cm.transitivity.core.TransitivityEventLog;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityProcessingEvent;

/**
 * TransitivityProcessingLog restricts a logging context to a specific
 * Transitivity job by attaching an Transitivity job identifier to each
 * TransitivityEvent instance that it records or retrieves.
 *
 * @author rphall
 */
public class TransitivityProcessingLog implements TransitivityEventLog {

	private static final Logger logger = Logger
			.getLogger(TransitivityProcessingLog.class.getName());

	private final EntityManager em;
	private final TransitivityJob oabaJob;

	public TransitivityProcessingLog(EntityManager em, TransitivityJob job) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !job.isPersistent()) {
			throw new IllegalArgumentException("invalid OABA job: " + job);
		}
		this.em = em;
		this.oabaJob = job;
	}

	protected TransitivityProcessingEvent getCurrentTransitivityProcessingEvent() {
		return TransitivityProcessingControllerBean
				.getCurrentTransitivityProcessingEvent(em, oabaJob);
	}

	@Override
	public TransitivityEvent getCurrentTransitivityEvent() {
		TransitivityProcessingEvent ope =
			getCurrentTransitivityProcessingEvent();
		TransitivityEvent retVal = ope.getTransitivityEvent();
		return retVal;
	}

	@Override
	public int getCurrentTransitivityEventId() {
		return getCurrentTransitivityEvent().eventId;
	}

	@Override
	public String getCurrentTransitivityEventInfo() {
		TransitivityProcessingEvent ope =
			getCurrentTransitivityProcessingEvent();
		String retVal = ope.getEventInfo();
		return retVal;
	}

	@Override
	public void setCurrentTransitivityEvent(TransitivityEvent event) {
		setCurrentTransitivityEvent(event, null);
	}

	@Override
	public void setCurrentTransitivityEvent(TransitivityEvent event, String info) {
		logger.info("OABA processing event: " + event + " (job "
				+ this.oabaJob.getId() + ")");
		TransitivityProcessingControllerBean.updateStatus(em, oabaJob, event,
				new Date(), info);
	}

	@Override
	public String toString() {
		return "TransitivityProcessingLog [jobId=" + oabaJob.getId() + "]";
	}

}
