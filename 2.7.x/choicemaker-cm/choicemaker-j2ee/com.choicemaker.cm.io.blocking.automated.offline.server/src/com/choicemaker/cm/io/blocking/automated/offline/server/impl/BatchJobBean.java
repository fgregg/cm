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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;

import org.apache.log4j.Category;

import com.choicemaker.autonumber.AutoNumberFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJobHome;

/**
 * A BatchJobBean tracks the progress of a (long-running) batch
 * process. A successful request goes through a sequence of states: NEW, QUEUED,
 * STARTED, and COMPLETED. A request may be aborted at any point, in which
 * case it goes through the ABORT_REQUESTED and the ABORT states.</p>
 *
 * A long-running process should provide some indication that it is making
 * progress. Since the process is handling a finite array of records, it
 * should be able to estimate the number of records completed. It can provide
 * this estimate as a fraction between 0.00 and 1.00 (inclusive) by updating
 * the getFractionComplete() fild.</p>
 *
 * 
 */
public abstract class BatchJobBean implements EntityBean {

	private static final long serialVersionUID = 1L;

	private static Category log = Category.getInstance(BatchJobBean.class.getName());

	/**
	 * These status values are copied from
	 * com.choicemaker.demo.batch.ejb.base.BatchJob
	 * to avoid importing the ejb interface package
	 * into the implementation package.
	 */
	static String STATUS_NEW = "NEW";
	static String STATUS_QUEUED = "QUEUED";
	static String STATUS_STARTED = "STARTED";
	static String STATUS_COMPLETED = "COMPLETED";
	static String STATUS_ABORT_REQUESTED = "ABORT_REQUESTED";
	static String STATUS_ABORTED = "ABORTED";
	static String STATUS_FAILED = "FAILED";

//	private EntityContext ctx;

	// CMP fields

	/** For CMP only */
	public abstract void setId(Long id);
	public abstract Long getId();

	/** For CMP only */
	public abstract void setExternalId(String externalId);
	public abstract String getExternalId();

	/** For CMP only */
	public abstract void setTransactionId(Long id);
	public abstract Long getTransactionId();

	/** For CMP only */
	public abstract void setType(String type);
	public abstract String getType();
	
	/** For CMP only */
	public abstract void setDescription(String description);
	public abstract String getDescription();

	/** For CMP only; use markAsXxx() methods instead */
	public abstract void setStatus(String status);
	public abstract String getStatus();

	/** For CMP only: use updateFractionCompleted(float) instead */
	public abstract void setFractionComplete(int i);
	public abstract int getFractionComplete();

	/** For CMP only */
	public abstract void setRequested(Date date);
	public abstract Date getRequested();

	/** For CMP only; use markAsQueued() method instead */
	public abstract void setQueued(Date queued);
	public abstract Date getQueued();

	/** For CMP only; use markAsStarted() method instead */
	public abstract void setStarted(Date started);
	public abstract Date getStarted();

	/** For CMP only; use markAsUpdated() method instead */
	public abstract void setUpdated(Date updated);
	public abstract Date getUpdated();

	/** For CMP only; use markAsCompleted() method instead */
	public abstract void setCompleted(Date completed);
	public abstract Date getCompleted();
	
	/** For CMP only; use markAsFailed() method instead */
	public abstract void setFailed(Date completed);
	public abstract Date getFailed();	

	/** For CMP only; use markAsAbortRequested() method instead */
	public abstract void setAbortRequested(Date abortRequested);
	public abstract Date getAbortRequested();

	/** For CMP only; use markAsAborted() method instead */
	public abstract void setAborted(Date aborted);
	public abstract Date getAborted();

	// Business methods

	public void markAsQueued() {
		if (BatchJob.STATUS_NEW.equals(getStatus())) {
			setQueued(new Date());
			setStatus(STATUS_QUEUED);
		} else {
			String msg =
				getId()
					+ ", '"
					+ getExternalId()
					+ "': markAsQueued ignored (status == '"
					+ getStatus()
					+ "'";
			log.warn(msg);
		}
	}

	public void markAsStarted() {
		if (BatchJob.STATUS_QUEUED.equals(getStatus())) {
			setStarted(new Date());
			setStatus(STATUS_STARTED);
		} else {
			String msg =
				getId()
					+ ", '"
					+ getExternalId()
					+ "': markAsQueued ignored (status == '"
					+ getStatus()
					+ "'";
			log.warn(msg);
		}
	}

	/** This method is called when the job is restarted.  This method doesn't check id the
	 * status is current queued.
	 *
	 */
	public void markAsReStarted() {
		setStarted(new Date());
		setStatus(STATUS_STARTED);
	}

	public void updateFractionCompleted(int fractionCompleted) {

		if (fractionCompleted < 0 || fractionCompleted > 100) {
			String msg =
				"invalid fractionCompleted == '" + fractionCompleted + "'";
			throw new IllegalArgumentException(msg);
		}

		if (BatchJob.STATUS_STARTED.equals(getStatus())) {
			setFractionComplete(fractionCompleted);
			setUpdated(new Date());
		} else {
			String msg =
				getId()
					+ ", '"
					+ getExternalId()
					+ "': updateFractionCompleted ignored (status == '"
					+ getStatus()
					+ "'";
			log.warn(msg);
		}

		return;
	} // updateFractionCompleted(float)

	public void markAsCompleted() {
		if (BatchJob.STATUS_STARTED.equals(getStatus())) {
			setFractionComplete(100);
			Date date = new Date();
			setUpdated(date);
			setCompleted(date);
			setStatus(STATUS_COMPLETED);
			
			//publish status
			//publishStatus ();
			
		} else {
			String msg =
				getId()
					+ ", '"
					+ getExternalId()
					+ "': markAsCompleted ignored (status == '"
					+ getStatus()
					+ "'";
			log.warn(msg);
		}
	}
	
	public void markAsFailed() {
		if (BatchJob.STATUS_STARTED.equals(getStatus())) {
			Date date = new Date();
			setUpdated(date);
			setFailed(date);
			setStatus(STATUS_FAILED);
		} else {
			String msg = getId() + ", '" + getExternalId() + 
				"': markAsFailed ignored (status == '" + getStatus() + "'";
			log.warn(msg);
		}
	}

	public void markAsAbortRequested() {
		if ((!STATUS_COMPLETED.equals(getStatus()))
			&& (!STATUS_FAILED.equals(getStatus()))
			&& (!STATUS_ABORTED.equals(getStatus()))) {
			setAbortRequested(new Date());
			setStatus(STATUS_ABORT_REQUESTED);
		}
		return;
	} // markAsAbortRequested

	public void markAsAborted() {
		if ((!STATUS_COMPLETED.equals(getStatus()))
			&& (!STATUS_FAILED.equals(getStatus()))
			&& (!STATUS_ABORTED.equals(getStatus()))) {

			if (!BatchJob.STATUS_ABORT_REQUESTED.equals(getStatus())) {
				markAsAbortRequested();
			}
			setAborted(new Date());
			setStatus(STATUS_ABORTED);
	
		}
		return;
	}


	public boolean shouldStop () {
		if (getStatus().equals(STATUS_ABORT_REQUESTED) || getStatus().equals(STATUS_ABORTED)) return true;
		else return false;
	}


	public Long ejbCreate(String externalId) throws CreateException {
		Integer nextId = AutoNumberFactory.getNextInteger(BatchJobHome.AUTONUMBER_IDENTIFIER);
		Long longId = new Long(nextId.longValue());
		setId(longId);
		setExternalId(externalId);
		setStatus(BatchJob.STATUS_NEW);
		setRequested(new Date());
		return null;
	}

	public void ejbPostCreate(String externalId) { }

	public Long ejbCreate(String externalId, String type) throws CreateException {
		Long retVal = ejbCreate(externalId);
		setType(type);
		return retVal;
	}

	public void ejbPostCreate(String externalId, String type) { }

	// EJB callbacks

//	public void setEntityContext(EntityContext context) {
//		ctx = context;
//	}
//
//	public void unsetEntityContext() {
//		ctx = null;
//	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void ejbRemove() {
		log.debug("Removing " + getExternalId());
	}

	public void ejbStore() {
	}

	public void ejbLoad() {
	}

} // BatchJobBean

