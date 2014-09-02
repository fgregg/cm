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
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.jms.JMSException;
import javax.naming.NamingException;

import java.util.logging.Logger;

import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;

/**
 * @version  $Revision: 1.5 $ $Date: 2010/10/27 22:19:50 $
 */
public class TransSerializerListener extends WorkflowControlListener{

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransSerializerListener.class.getName());

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public TransSerializerListener() {
    	log.fine("TransSerializerListener constructor");
	}
	protected boolean isAbortCheckRequired() {return false;}

	protected long getUrmJobId(long stepJobId) 
								throws NamingException,RemoteException,JMSException,ConfigException,
								CmRuntimeException,SQLException,CreateException,ArgumentException,ModelException {
		
		CmsJob stepJob = Single.getInst().findCmsJobById(stepJobId);
		long urmJobId = stepJob.getTransactionId().longValue();
		return urmJobId; 
	}
	
			
	protected long startStep(UrmJob urmJob, long prevStepId) 
									throws JobAlreadyAbortedException, 
									NamingException,
									RemoteException,
									JMSException,
									ConfigException,
									CmRuntimeException,
									SQLException,
									CreateException,
									ArgumentException,
									ModelException 
	{
		//no further step
		urmJob.markAsCompleted();
		return JobStatus.UNDEFINED_ID;
	} 
	
	public void abortJobStep(long id) throws ConfigException, CmRuntimeException {
		//no abort allowed at this point
	}

}

