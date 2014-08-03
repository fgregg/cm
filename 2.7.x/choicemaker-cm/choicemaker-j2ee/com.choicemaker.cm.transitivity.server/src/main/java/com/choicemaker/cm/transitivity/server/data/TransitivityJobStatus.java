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
package com.choicemaker.cm.transitivity.server.data;

import java.io.Serializable;
import java.util.Date;

/**
 * This object tells the status of the transitivity job id.
 *
 * @author pcheung
 *
 */
public class TransitivityJobStatus implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 985937165348317321L;

	protected long jobId;
	protected String status;
	protected Date startDate;
	protected Date finishDate;

	public TransitivityJobStatus() {
		// 2014-04-24 rphall: Commented out unused local variable.
//		String str = new java.sql.Timestamp(startDate.getTime() ).toString();
	}

	public TransitivityJobStatus(long jobId, String status, Date startDate, Date finishDate) {
		setJobId(jobId);
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


}
