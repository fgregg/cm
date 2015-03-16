package com.choicemaker.cm.transitivity.server.impl;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobProcessingEvent;
import com.choicemaker.cm.batch.ProcessingEventLog;

/**
 * TransitivityProcessingLog restricts a logging context to a specific
 * Transitivity job by attaching an Transitivity job identifier to each
 * TransitivityEvent instance that it records or retrieves.
 *
 * @author rphall
 */
public class TransitivityProcessingLog implements ProcessingEventLog {

	private static final Logger logger = Logger
			.getLogger(TransitivityProcessingLog.class.getName());

	private static final String LOG_SOURCE = 
			TransitivityProcessingLog.class.getSimpleName();

	private final EntityManager em;
	private final BatchJob batchJob;

	public TransitivityProcessingLog(EntityManager em, BatchJob job) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !job.isPersistent()) {
			throw new IllegalArgumentException("invalid OABA job: " + job);
		}
		this.em = em;
		this.batchJob = job;
	}

	protected BatchJobProcessingEvent getCurrentTransitivityProcessingEvent() {
		return TransitivityProcessingControllerBean
				.getCurrentBatchProcessingEvent(em, batchJob);
	}

	@Override
	public ProcessingEvent getCurrentProcessingEvent() {
		BatchJobProcessingEvent ope =
			getCurrentTransitivityProcessingEvent();
		ProcessingEvent retVal = ope.getProcessingEvent();
		return retVal;
	}

	@Override
	public int getCurrentProcessingEventId() {
		return getCurrentProcessingEvent().getEventId();
	}

	@Override
	public String getCurrentProcessingEventInfo() {
		BatchJobProcessingEvent ope =
			getCurrentTransitivityProcessingEvent();
		String retVal = ope.getEventInfo();
		return retVal;
	}

	@Override
	public void setCurrentProcessingEvent(ProcessingEvent event) {
		setCurrentProcessingEvent(event, null);
	}

	@Override
	public void setCurrentProcessingEvent(ProcessingEvent event, String info) {
		logger.info(LOG_SOURCE + ".setCurrentProcessingEvent: " + event + " (job "
				+ this.batchJob.getId() + ")");
		TransitivityProcessingControllerBean.updateStatus(em, batchJob, event,
				new Date(), info);
	}

	@Override
	public String toString() {
		return "TransitivityProcessingLog [jobId=" + batchJob.getId() + "]";
	}

}
