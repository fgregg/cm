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
package com.choicemaker.cm.io.blocking.automated.offline.server.ejb;

import java.util.Date;

import com.choicemaker.cm.core.IControl;

/**
 * @author rphall
 *
 */
public interface BatchJob extends IControl {
	
	// -- Default identifier values

	/** Default id value for non-persistent batch jobs */
	long INVALID_BATCHJOB_ID = 0;

	/** Default value of batchParentId for top-level jobs */
	long NO_BATCH_PARENT = INVALID_BATCHJOB_ID;

	/** Default URM job id for batch jobs not controlled by a URM job */
	long INVALID_URMJOB_ID = INVALID_BATCHJOB_ID;
	
	// -- Status

	/** Minimum valid value for fractionComplete (inclusive) */
	int MIN_PERCENTAGE_COMPLETED = 0;
	
	/** Maximum valid value for fractionComplete (inclusive) */
	int MAX_PERCENTAGE_COMPLETED = 100;
	
	String STATUS_NEW = "NEW";
	String STATUS_QUEUED = "QUEUED";
	String STATUS_STARTED = "STARTED";
	String STATUS_COMPLETED = "COMPLETED";
	String STATUS_FAILED = "FAILED";
	String STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
	String STATUS_ABORTED = "ABORTED";
	String STATUS_CLEAR = "STATUS_CLEAR";

	// -- Deprecated constants

	String DEFAULT_EJB_REF_NAME = "ejb/BatchJob";
	String DEFAULT_JNDI_COMP_NAME = "java:comp/env/" + DEFAULT_EJB_REF_NAME;

	// -- Accessors

	long getId();

	long getBatchParentId();
	
	long getUrmId();

	long getTransactionId();

	String getExternalId();

	String getDescription();

	String getStatus();

	Date getTimeStamp(String status);

	int getFractionComplete();

	Date getRequested();

	Date getQueued();

	Date getStarted();

	Date getCompleted();

	Date getFailed();

	Date getAbortRequested();

	Date getAborted();

	// -- Modifiers

	void setExternalId(String externalId);

	void setDescription(String description);

	/** Use markAsXxx() methods instead */
	void setStatus(String status);

	void setFractionComplete(int i);

	// -- State machine

	void markAsQueued();

	void markAsStarted();

	/**
	 * This method is called when the job is restarted. This method doesn't
	 * check if the status is currently queued.
	 */
	void markAsReStarted();

	void markAsCompleted();

	void markAsFailed();

	void markAsAbortRequested();

	void markAsAborted();

	// -- Business methods

	boolean shouldStop();

} // BatchJob
