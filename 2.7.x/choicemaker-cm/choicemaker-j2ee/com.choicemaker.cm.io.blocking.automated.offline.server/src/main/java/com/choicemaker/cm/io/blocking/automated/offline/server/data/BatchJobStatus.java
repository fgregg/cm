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
package com.choicemaker.cm.io.blocking.automated.offline.server.data;

import java.io.Serializable;
import java.util.Date;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * The status of a batch job
 *
 * @author pcheung
 *
 */
public class BatchJobStatus implements Serializable {

	static final long serialVersionUID = 8006471060836073376L;

	private final long jobId;
	private final long bparentId;
	private final long urmId;
	private final long transactionId;
	private final String externalId;
	private final String description;
	private final int fractionComplete;
	private final String status;
	private final Date statusDate;

	public BatchJobStatus(BatchJob job) {
		this.jobId = job.getId();
		this.bparentId = job.getBatchParentId();
		this.urmId = job.getUrmId();
		this.transactionId = job.getTransactionId();
		this.externalId = job.getExternalId();
		this.description = job.getDescription();
		this.fractionComplete = job.getFractionComplete();
		this.status = job.getStatus();
		this.statusDate = job.getTimeStamp(this.status);
	}

	public long getJobId() {
		return jobId;
	}

	public String getDescription() {
		return description;
	}

	public String getStatus() {
		return status;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	long getBatchParentId() {
		return bparentId;
	}
	
	long getUrmId() {
		return urmId;
	}

	public long getTransactionId () {
		return transactionId;
	}

	String getExternalId() {
		return externalId;
	}

	int getFractionComplete() {
		return fractionComplete;
	}

}
