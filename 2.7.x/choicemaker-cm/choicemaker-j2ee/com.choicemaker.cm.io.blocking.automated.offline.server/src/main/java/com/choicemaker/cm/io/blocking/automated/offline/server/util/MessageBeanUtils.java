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
package com.choicemaker.cm.io.blocking.automated.offline.server.util;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This object contains common message bean utilities such as cancelling an OABA job.
 * 
 * @author pcheung
 *
 */
public class MessageBeanUtils {

	private static final Logger log = Logger.getLogger(MessageBeanUtils.class.getName());
	
	
	/** This method stops the BatchJob by setting the status to aborted, and removes the
	 * temporary directory for the job.
	 * 
	 * @param batchJob
	 * @param status
	 * @param oabaConfig
	 * @throws RemoteException
	 * @throws BlockingException
	 */
	public static void stopJob (BatchJob batchJob, IStatus status, 
		OABAConfiguration oabaConfig) throws RemoteException, BlockingException {
		
		batchJob.markAsAborted();
					
		if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
			status.setStatus (IStatus.DONE_PROGRAM);
				
			log.info("Removing Temporary directory.");
			oabaConfig.removeTempDir();
		}
	}

}
