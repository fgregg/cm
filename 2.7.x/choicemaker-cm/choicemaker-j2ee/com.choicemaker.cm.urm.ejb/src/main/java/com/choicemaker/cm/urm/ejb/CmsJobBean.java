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
package com.choicemaker.cm.urm.ejb;

//import org.jboss.varia.autonumber.AutoNumberFactory;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.apache.log4j.Logger;

import com.choicemaker.autonumber.AutoNumberFactory;
import com.choicemaker.cm.urm.base.JobStatus;




/**
 * Represents a ChoiceMaker serialization job?
 */
public abstract class CmsJobBean implements EntityBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	protected static Logger log = Logger.getLogger(CmsJobBean.class);


	protected EntityContext ctx;

	// CMP fields
	public abstract void setId(Long id) ;
	public abstract Long getId() ;
	/** For CMP only */
	public abstract void setTransactionId(Long transactionId) ;
	public abstract Long getTransactionId() ;	
	/** For CMP only */
	public abstract void setExternalId(String externalId) ;
	public abstract String getExternalId() ;
	/** For CMP only; */
	public abstract void setStatus(String status) ;
	public abstract String getStatus() ;	
//	public abstract void setStepDescription(String descr);
//	public abstract String getStepDescription();
	public abstract void setErrorDescription(String error);
	public abstract String getErrorDescription();

	public abstract void setStartDate(Date d);
	public abstract Date getStartDate();
	
//	public abstract void setStepStartDate(Date d);
//	public abstract Date getStepStartDate();

	public abstract void setFinishDate(Date completed) ;
	public abstract Date getFinishDate() ;
	/** For CMP only; */
	public abstract void setAbortRequestDate(Date abortRequested) ;
	public abstract Date getAbortRequestDate() ;

	public abstract void setFractionComplete(int i) ;
	public abstract int getFractionComplete() ;
	
	// Business methods
	/** This method publishes the status to a topic queue.
	 * 
	 * @param status
	 */


	String logInvalidStatusUpdate(String newStatus){
		String msg = "job ("
				+ getId()
				+ ","
				+ getExternalId()
				+ ") failed update status to '"
				+ newStatus
				+ "'. Current status '"
				+ getStatus()
				+ "'";
		log.warn(msg);
		return msg;
	}
	
	public void updateStepInfo(int fractionComplete)  {
		if (JobStatus.STATUS_STARTED.equals(getStatus())) {
			if(fractionComplete != -1)
				setFractionComplete(fractionComplete);
//			setStepDescription(descr);
//			setStepStartDate(new Date());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_STARTED);
	
	}

	public void markAsStarted() {
		if (JobStatus.STATUS_NEW.equals(getStatus())) {
			setStatus(JobStatus.STATUS_STARTED);
			setFractionComplete(1);
			setStartDate(new Date());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_STARTED);
	}
	
	public void markAsFailed(){
		if (!JobStatus.STATUS_COMPLETED.equals(getStatus())
			&&!JobStatus.STATUS_ABORT_REQUESTED.equals(getStatus())
			&&!JobStatus.STATUS_ABORTED.equals(getStatus())) {
			setStatus(JobStatus.STATUS_FAILED);
			setFinishDate(new Date());	
			log.debug("job "+getId()+" is failed, external id "+ getExternalId());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_STARTED);
	}
	
	public void markAsCompleted() {
		if (JobStatus.STATUS_STARTED.equals(getStatus())) {
			setStatus(JobStatus.STATUS_COMPLETED);
			setFractionComplete(100);
			setFinishDate(new Date());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_COMPLETED);
	}

	public void markAsAbortRequested() {
		if ((!JobStatus.STATUS_COMPLETED.equals(getStatus()))
			&& (!JobStatus.STATUS_ABORTED.equals(getStatus()))
			&& (!JobStatus.STATUS_FAILED.equals(getStatus()))
		) {
			setAbortRequestDate(new Date());
			setStatus(JobStatus.STATUS_ABORT_REQUESTED);
			log.debug("job "+getId()+"aborted is requested, external id "+ getExternalId());
		}
		return;
	} // markAsAbortRequested

	public void markAsAborted() {
		if ((!JobStatus.STATUS_COMPLETED.equals(getStatus()))
			&& (!JobStatus.STATUS_ABORTED.equals(getStatus()))
			&& (!JobStatus.STATUS_FAILED.equals(getStatus()))) {	
			setFinishDate(new Date());
			setStatus(JobStatus.STATUS_ABORTED);	
			log.debug("job "+getId()+" is aborted, external id "+ getExternalId());
		}
		return;
	}
	
	public boolean isAbortRequested() {
		return JobStatus.STATUS_ABORT_REQUESTED.equals(getStatus()); 
	}

	public Long ejbCreate(String externalId, long transId) throws CreateException {
		Integer nextId = AutoNumberFactory.getNextInteger(CmsJobHome.AUTONUMBER_IDENTIFIER);
		Long batchId = new Long(nextId.longValue());
		setId(batchId);
		setExternalId(externalId);
		setTransactionId(new Long(transId));
		setStatus(JobStatus.STATUS_NEW);
		setFractionComplete(0);
		log.info("Created new ChoiceMaker Server Job"+batchId);
		return batchId;
	}
	
	public void ejbPostCreate(String externalId, long transId) {
	}
	
	// EJB callbacks

	public void setEntityContext(EntityContext context) {
		ctx = context;
	}

	public void unsetEntityContext() {
		ctx = null;
	}

	public void ejbActivate() {
	}

	public void ejbPassivate() {
	}

	public void ejbRemove() {
		log.info("Removing " + getExternalId());
	}

	public void ejbStore() {
	}

	public void ejbLoad() {
	}

}

