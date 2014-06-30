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

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.choicemaker.cm.core.base.SerialRecordSource;

/**
 * @author pcheung
 *
 */
public interface BatchParameters extends EJBObject {

	// CMP fields
	/** For CMP only */
	void setId(Long id) throws RemoteException;
	Long getId() throws RemoteException;

	/** For CMP only */
	void setStageModel(String stageModel) throws RemoteException;
	String getStageModel() throws RemoteException;

	/** For CMP only */
	void setMasterModel(String masterModel) throws RemoteException;
	String getMasterModel() throws RemoteException;

	/** For CMP only */
	void setMaxSingle(Integer ms) throws RemoteException;
	Integer getMaxSingle() throws RemoteException;
	
	/** For CMP only */
	void setLowThreshold(Float low) throws RemoteException;
	Float getLowThreshold() throws RemoteException;

	/** For CMP only */
	void setHighThreshold(Float low) throws RemoteException;
	Float getHighThreshold() throws RemoteException;
	
	/** For CMP only */
	void setStageRs(SerialRecordSource rs) throws RemoteException;
	SerialRecordSource getStageRs() throws RemoteException;
	
	/** For CMP only */
	void setMasterRs(SerialRecordSource rs) throws RemoteException;
	SerialRecordSource getMasterRs() throws RemoteException;
	

}
