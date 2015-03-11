package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobProcessingEvent;
import com.choicemaker.cm.batch.ProcessingEventLog;

/**
 * OabaProcessingLog restricts a logging context to a specific OABA job by
 * attaching an OABA job identifier to each OabaEvent instance that it records
 * or retrieves.
 *
 * @author rphall
 */
public class OabaProcessingLog implements ProcessingEventLog {

	private static final Logger logger = Logger
			.getLogger(OabaProcessingLog.class.getName());

	private final EntityManager em;
	private final BatchJob batchJob;

	public OabaProcessingLog(EntityManager em, BatchJob job) {
		if (em == null) {
			throw new IllegalArgumentException("null EntityManager");
		}
		if (job == null || !job.isPersistent()) {
			throw new IllegalArgumentException("invalid OABA job: " + job);
		}
		this.em = em;
		this.batchJob = job;
	}

	protected BatchJobProcessingEvent getCurrentOabaProcessingEvent() {
		return OabaProcessingControllerBean.getCurrentBatchProcessingEvent(em,
				batchJob);
	}

	@Override
	public ProcessingEvent getCurrentProcessingEvent() {
		BatchJobProcessingEvent ope = getCurrentOabaProcessingEvent();
		ProcessingEvent retVal = ope.getProcessingEvent();
		return retVal;
	}

	@Override
	public int getCurrentProcessingEventId() {
		return getCurrentProcessingEvent().getEventId();
	}

	@Override
	public String getCurrentProcessingEventInfo() {
		BatchJobProcessingEvent ope = getCurrentOabaProcessingEvent();
		String retVal = ope.getEventInfo();
		return retVal;
	}

	@Override
	public void setCurrentProcessingEvent(ProcessingEvent event) {
		setCurrentProcessingEvent(event, null);
	}

	@Override
	public void setCurrentProcessingEvent(ProcessingEvent event, String info) {
		logger.info("OABA processing event: " + event + " (job "
				+ this.batchJob.getId() + ")");
		OabaProcessingControllerBean.updateStatus(em, batchJob, event,
				new Date(), info);
	}

	@Override
	public String toString() {
		return "OabaProcessingLog [jobId=" + batchJob.getId() + "]";
	}

}
