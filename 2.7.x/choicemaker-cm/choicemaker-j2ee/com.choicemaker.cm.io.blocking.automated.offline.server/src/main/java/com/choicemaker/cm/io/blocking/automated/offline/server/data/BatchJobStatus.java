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

/**
 * This object tells the status of the job id.
 *
 * @author pcheung
 *
 */
public class BatchJobStatus implements Serializable {

	static final long serialVersionUID = 8006471060836073376L;

	protected long jobId;
	protected long transactionId;
	protected String description;
	protected String status;
	protected Date startDate;
	protected Date finishDate;

	public BatchJobStatus() {
	}

	public BatchJobStatus(long jobId, long transactionId,
		String desc, String status, Date startDate, Date finishDate) {

		setJobId(jobId);
		setTransactionId (transactionId);
		setDescription(desc);
		setStatus(status);
		setStartDate(startDate);
		setFinishDate(finishDate);
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public long getTransactionId () {
		return transactionId;
	}

	public void setTransactionId (long l) {
		transactionId = l;
	}

}
