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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;

/**
 * This is a wrapper that takes an entity bean and make it looks like IStatus. 
 * 
 * @author pcheung
 *
 */
public class StatusLogWrapper implements IStatus {
	
	private StatusLog statusLog;
	
	public StatusLogWrapper (StatusLog statusLog) {
		this.statusLog = statusLog;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IStatus#setStatus(int)
	 */
	public void setStatus(int stat) throws BlockingException {
		try {
			statusLog.setStatusId(new Integer (stat));
			statusLog.setInfo(null);
		} catch (RemoteException e) {
			throw new BlockingException (e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IStatus#getStatus()
	 */
	public int getStatus() throws BlockingException {
		try {
			return statusLog.getStatusId().intValue();
		} catch (RemoteException e) {
			throw new BlockingException (e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IStatus#setStatus(int, java.lang.String)
	 */
	public void setStatus(int stat, String info) throws BlockingException {
		try {
			statusLog.setStatusId(new Integer (stat));
			statusLog.setInfo(info);
		} catch (RemoteException e) {
			throw new BlockingException (e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IStatus#getAdditionalInfo()
	 */
	public String getAdditionalInfo() throws BlockingException {
		try {
			return statusLog.getInfo();
		} catch (RemoteException e) {
			throw new BlockingException (e.toString());
		}
	}

}
