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
package com.choicemaker.cm.urm.base;

import java.io.Serializable;
import java.util.Date;
/**
 * Represents the status of the matching process(job).
 * 
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 1, 2005 1:04:14 PM
 * @see
 */
public class JobStatus implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -7342639451565648690L;

	public static final long UNDEFINED_ID = -1;
	public static final Long UNDEFINED_ID_OBJECT = new Long(UNDEFINED_ID);

	public static final String STATUS_QUEUED = "QUEUED";
	public static final String STATUS_NEW = "NEW";
	public static final String STATUS_STARTED = "STARTED";
	public static final String STATUS_FAILED = "FAILED";
	public static final String STATUS_COMPLETED = "COMPLETED";
	public static final String STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
	public static final String STATUS_ABORTED = "ABORTED";

	protected long jobId;
	protected String trackingId;
	protected long stepId;
	protected String status;
	protected Date startDate;
	protected Date finishDate;
	protected Date abortRequestDate;
	protected int fractionComplete;
	
	protected String stepDescription;	
	protected Date stepStartDate;
	
	protected String errorDescription;

	/**
	 * 
	 */
	public JobStatus() {
	
	}
	
	public JobStatus(   long 	jobId,						
						String 	status, 
						Date 		startDate,
						Date 		finishDate) {
							
		this.jobId = jobId;
		this.status = status; 
		this.startDate = startDate;
		this.finishDate = finishDate;
	}


	/**
	 * @return
	 */
	public long getJobId() {
		return jobId;
	}

	/**
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return
	 */
	public String getStatus() {
		return status;
	}


	/**
	 * @return
	 */
	public String getStepDescription() {
		return stepDescription;
	}

	/**
	 * @return
	 */
	public Date getFinishDate() {
		return finishDate;
	}

	/**
	 * @param string
	 */
	public void setStepDescription(String string) {
		stepDescription = string;
	}

	/**
	 * @param date
	 */
	public void setFinishDate(Date date) {
		finishDate = date;
	}

	/**
	 * @param l
	 */
	public void setJobId(long l) {
		jobId = l;
	}

	/**
	 * @param date
	 */
	public void setStartDate(Date date) {
		startDate = date;
	}

	/**
	 * @param string
	 */
	public void setStatus(String string) {
		status = string;
	}

	/**
	 * @return
	 */
	public Date getAbortRequestDate() {
		return abortRequestDate;
	}

	/**
	 * @return
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @return
	 */
	public long getStepId() {
		return stepId;
	}

	/**
	 * @return
	 */
	public Date getStepStartDate() {
		return stepStartDate;
	}

	/**
	 * @param date
	 */
	public void setAbortRequestDate(Date date) {
		abortRequestDate = date;
	}

	/**
	 * @param string
	 */
	public void setErrorDescription(String string) {
		errorDescription = string;
	}

	/**
	 * @param l
	 */
	public void setStepId(long l) {
		stepId = l;
	}

	/**
	 * @param date
	 */
	public void setStepStartDate(Date date) {
		stepStartDate = date;
	}

	/**
	 * @return
	 */
	public int getFractionComplete() {
		return fractionComplete;
	}

	/**
	 * @param string
	 */
	public void setFractionComplete(int fc) {
		fractionComplete = fc;
	}

	/**
	 * @return
	 */
	public String getTrackingId() {
		return trackingId;
	}

	/**
	 * @param string
	 */
	public void setTrackingId(String string) {
		trackingId = string;
	}

}
