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
import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.apache.log4j.Logger;

import com.choicemaker.autonumber.AutoNumberFactory;
import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;

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
public abstract class UrmJobBean implements EntityBean {//extends CmsJobBean {

	private static final long serialVersionUID = 1L;
	public static final Long UNDEFINED_STEP_INDEX = new Long(-1);
	protected static Logger log = Logger.getLogger(UrmJobBean.class.getName());
	protected EntityContext ctx;

	// CMP fields
	public abstract void setId(Long id) ;
	public abstract Long getId() ;

	public abstract void setExternalId(String externalId) ;
	public abstract String getExternalId() ;	

	public abstract void setStatus(String status);
	public abstract String getStatus();
	
	public abstract void setCurStepJobId(Long id) ;
	public abstract Long getCurStepJobId();    
	
	public abstract void setCurStepIndex(Long index);
	public abstract Long getCurStepIndex();    	

	public abstract void setStartDate(Date d);
	public abstract Date getStartDate();

	public abstract void setFinishDate(Date completed) ;
	public abstract Date getFinishDate() ;
	
	public abstract void setAbortRequestDate(Date abortRequested) ;
	public abstract Date getAbortRequestDate() ;

	public abstract void setStepDescription(String descr);
	public abstract String getStepDescription();

	public abstract void setStepStartDate(Date d);
	public abstract Date getStepStartDate();
	
	public abstract void setErrorDescription(String error);
	public abstract String getErrorDescription();
	
	public abstract void setSerializationType(String id);
	public abstract String getSerializationType();

	public abstract void setGroupMatchType(String id);
	public abstract String getGroupMatchType();

	public abstract void setQueryRs(SerialRecordSource rs);
	public abstract SerialRecordSource getQueryRs();
	
	public abstract void setMasterRs(SerialRecordSource rs);
	public abstract SerialRecordSource getMasterRs();
	
	public abstract void setLowerThreshold(Float lth);
	public abstract Float getLowerThreshold();
	
	public abstract void setUpperThreshold(Float lth) ;
	public abstract Float getUpperThreshold() ;
	
	public abstract void setModelName(String name) ;
	public abstract String getModelName() ;	
		

// Business methods ------------------------------------------------------------------------------	
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
   
    public void updateCurStepJobId(Long id, Long step) 
    {
    	if(id == null)
			setCurStepIndex(step);
		else {
			if(getCurStepIndex().equals(step) )
			setCurStepJobId(id);	
		}
    	
    }
   
	public Long moveToNextStep(Long completedStepJobId, boolean isAbortCheckRequired) throws CmRuntimeException,ConfigException,RemoteException 
	{	
		UrmStepJob usj = Single.getInst().createUrmStepJob(getId(),getCurStepIndex());
		usj.setStepJobId(completedStepJobId);
	 	if(isAbortCheckRequired && markAbortedIfRequested()){	
			log.info("job "+getId()+" is successfully aborted after "+getStepDescription()+" step with id "+completedStepJobId);
			return null;
	 	}
		setCurStepJobId(JobStatus.UNDEFINED_ID_OBJECT);	
		Long step= new Long(getCurStepIndex().longValue()+1);
		updateCurStepJobId(null, step);		
		return step;						
	}
   
	public void markAsMatching() {
		if (JobStatus.STATUS_NEW.equals(getStatus())) {
			setStatus(JobStatus.STATUS_STARTED);
			setStartDate(new Date());
			setStepDescription(UrmJob.URMSTEP_MATCHING);
		} else 
			logInvalidStatusUpdate(UrmJob.URMSTEP_MATCHING);
	}

	public void markAsTransOABA(){
		if (JobStatus.STATUS_STARTED.equals(getStatus())
			&& UrmJob.URMSTEP_MATCHING.equals(getStepDescription())) {
			setStepDescription(UrmJob.URMSTEP_TRANSOABA);
		}	
		else 
			logInvalidStatusUpdate(UrmJob.URMSTEP_TRANSOABA);
	}
	
	public void markAsSerializing(){
		if (JobStatus.STATUS_STARTED.equals(getStatus())
			&& UrmJob.URMSTEP_TRANSOABA.equals(getStepDescription())) {
			setStepDescription(UrmJob.URMSTEP_SERIALIZING);
		}	
		else 
			logInvalidStatusUpdate(UrmJob.URMSTEP_SERIALIZING);
	}

	public void markAsFailed(){
		if (!JobStatus.STATUS_COMPLETED.equals(getStatus())
			&&!JobStatus.STATUS_ABORT_REQUESTED.equals(getStatus())
			&&!JobStatus.STATUS_ABORTED.equals(getStatus())) {
			setStatus(JobStatus.STATUS_FAILED);
			//TODO set errorDesription
			setFinishDate(new Date());	
			log.debug("job "+getId()+" is failed, external id "+ getExternalId());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_FAILED);
	}
	
	public void markAsCompleted() {
		if (JobStatus.STATUS_STARTED.equals(getStatus())
			&& UrmJob.URMSTEP_SERIALIZING.equals(getStepDescription())) {
			setStatus(JobStatus.STATUS_COMPLETED);
			setStepDescription("");
			setFinishDate(new Date());
		} else 
			logInvalidStatusUpdate(JobStatus.STATUS_COMPLETED);
	}

	public boolean markAsAbortRequested() {
		if ((!JobStatus.STATUS_COMPLETED.equals(getStatus()))
			&& (!JobStatus.STATUS_ABORT_REQUESTED.equals(getStatus()))
			&& (!JobStatus.STATUS_ABORTED.equals(getStatus()))
			&& (!JobStatus.STATUS_FAILED.equals(getStatus()))
		) {
			setAbortRequestDate(new Date());
			setStatus(JobStatus.STATUS_ABORT_REQUESTED);
			log.debug("job "+getId()+"aborted is requested, external id "+ getExternalId());
			return true;
		}
		return false;
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
	
	public boolean markAbortedIfRequested() {
		if(isAbortRequested()){
			setFinishDate(new Date());
			setStatus(JobStatus.STATUS_ABORTED);
			log.debug("job "+getId()+" is aborted, external id "+ getExternalId());
	   		return true;
		}
		else
	   		return false;
	}
	
	public boolean isAbortRequested() {
		return JobStatus.STATUS_ABORT_REQUESTED.equals(getStatus()); 
	}
	
	public boolean isStarted() {
		return JobStatus.STATUS_STARTED.equals(getStatus()); 
	}	
	
	public JobStatus getJobStatus(){
		JobStatus js = new JobStatus (	getId().longValue(),
										getStatus(),
										getStartDate(),
										getFinishDate());
		js.setAbortRequestDate(this.getAbortRequestDate());
		js.setErrorDescription("");//TODO
		js.setStepId(-1);//TODO
		js.setFractionComplete(10);//TODO
		js.setStepDescription(getStepDescription());
		js.setStepStartDate(new Date());//TODO
		js.setTrackingId(this.getExternalId());	
		return js;								
	}

	public Long ejbCreate(String externalId) throws CreateException {
		Integer nextId = AutoNumberFactory.getNextInteger(UrmJobHome.AUTONUMBER_IDENTIFIER);
		Long batchId = new Long(nextId.longValue());
		this.setId(batchId);
		this.setExternalId(externalId);
		this.setCurStepIndex(UNDEFINED_STEP_INDEX);
		this.setCurStepJobId(JobStatus.UNDEFINED_ID_OBJECT);
		setStatus(JobStatus.STATUS_NEW);
		log.info("Created new ChoiceMaker Server Job"+batchId);
		return batchId;
	}
	
	public void ejbPostCreate(String externalId) {
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
