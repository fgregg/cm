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

import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;

public interface UrmJob extends EJBObject {//CmsJob {

	static String URMSTEP_MATCHING = "MATCHING STEP";
	static String URMSTEP_TRANSOABA = "MATCH ANALYSIS STEP";
	static String URMSTEP_SERIALIZING = "SERIALIZATION STEP";
	
// CMP fields -------------------------------------------------------------------------
    void setId(Long id) throws RemoteException;
    Long getId() throws RemoteException;
   
    void setExternalId(String externalId) throws RemoteException;
    String getExternalId() throws RemoteException;
   
    void setStatus(String status) throws RemoteException;
    String getStatus() throws RemoteException;
    
	void setCurStepJobId(Long id) throws RemoteException;
	Long getCurStepJobId() throws RemoteException;    
	
	void setCurStepIndex(Long index) throws RemoteException;
	Long getCurStepIndex() throws RemoteException;    

	void setQueryRs(SerializableRecordSource rs) throws RemoteException;
	SerializableRecordSource getQueryRs() throws RemoteException;
	
	void setMasterRs(SerializableRecordSource rs) throws RemoteException;
	SerializableRecordSource getMasterRs() throws RemoteException;	
	
	void setSerializationType(String id)throws RemoteException;
	String getSerializationType()throws RemoteException;

	void setGroupMatchType(String id)throws RemoteException;
	String getGroupMatchType()throws RemoteException;	

	void setLowerThreshold(Float lth) throws RemoteException;
	Float getLowerThreshold() throws RemoteException;
	
	void setUpperThreshold(Float lth) throws RemoteException;
	Float getUpperThreshold() throws RemoteException;
	
	void setModelName(String name) throws RemoteException;
	String getModelName() throws RemoteException;	

	void setStartDate(Date d)throws RemoteException;
	Date getStartDate()throws RemoteException;
	/** For CMP only; */
	void setFinishDate(Date completed) throws RemoteException;
	Date getFinishDate() throws RemoteException;
	/** For CMP only; */
	void setAbortRequestDate(Date abortRequested) throws RemoteException;
	Date getAbortRequestDate() throws RemoteException;
	
	void setStepDescription(String descr)throws RemoteException;
	String getStepDescription()throws RemoteException;

	void setStepStartDate(Date d)throws RemoteException;
	Date getStepStartDate()throws RemoteException;

	void setErrorDescription(String error)throws RemoteException;
	String getErrorDescription()throws RemoteException;
	
// Business methods -------------------------------------------------------------------
	void markAsMatching() throws RemoteException;
	void markAsTransOABA() throws RemoteException;
	void markAsSerializing() throws RemoteException;
	
	void markAsCompleted() throws RemoteException;
	void markAsFailed() throws RemoteException;
	boolean markAsAbortRequested() throws RemoteException;
	void markAsAborted() throws RemoteException;
	boolean markAbortedIfRequested() throws RemoteException;

	boolean isAbortRequested() throws RemoteException;
	boolean isStarted() throws RemoteException;
	
	JobStatus getJobStatus() throws RemoteException;
	
	Long moveToNextStep(Long completedStepJobId, boolean isAbortCheckRequired) throws CmRuntimeException,ConfigException, RemoteException;
	void updateCurStepJobId(Long id, Long step) throws RemoteException;
	
} // UrmJob


