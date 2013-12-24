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

/**
 * @author pcheung
 *
 */
public interface StatusLog extends EJBObject{

	void setJobId(Long id) throws RemoteException;
	Long getJobId() throws RemoteException;

	/** For CMP only */
	void setStatusId(Integer statusId) throws RemoteException;
	Integer getStatusId() throws RemoteException;

	/** For CMP only */
	void setInfo(String info) throws RemoteException;
	String getInfo() throws RemoteException;
}
