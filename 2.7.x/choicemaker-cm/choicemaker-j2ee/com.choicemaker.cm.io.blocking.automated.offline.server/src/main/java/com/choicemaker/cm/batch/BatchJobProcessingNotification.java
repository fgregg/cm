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
package com.choicemaker.cm.batch;

import java.io.Serializable;
import java.util.Date;

/**
 * This is the data object that gets passed to the UpdateStatusMDB message bean.
 * 
 * @author pcheung
 *
 */
public abstract class BatchJobProcessingNotification implements Serializable {

	static final long serialVersionUID = 271;

	private final long jobId;
	private final String jobType;
	private final String jobStatus;
	private final int jobPercentComplete;
	private final long eventId;
	protected long getJobId() {
		return jobId;
	}

	protected String getJobType() {
		return jobType;
	}

	protected String getJobStatus() {
		return jobStatus;
	}

	protected int getJobPercentComplete() {
		return jobPercentComplete;
	}

	protected long getEventId() {
		return eventId;
	}

	protected String getEventType() {
		return eventType;
	}

	protected Date getEventTimestamp() {
		return eventTimestamp;
	}

	private final String eventType;
	private final Date eventTimestamp;
	
	protected BatchJobProcessingNotification(
			long jobId,
			String jobType,
			String jobStatus,
			int percentComplete,
			long eventId,
			String eventType,
			Date timestamp
			) {
		this.jobId = jobId;
		this.jobType = jobType;
		this.jobStatus = jobStatus;
		this.jobPercentComplete = percentComplete;
		this.eventId = eventId;
		this.eventType = eventType;
		this.eventTimestamp = timestamp;
	}

}
