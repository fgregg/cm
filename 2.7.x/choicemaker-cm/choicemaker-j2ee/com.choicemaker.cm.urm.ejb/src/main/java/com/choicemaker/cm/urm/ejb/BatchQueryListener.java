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

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.SerialRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityOABAService;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;



/**
 * TODO
 * @version  $Revision: 1.7 $ $Date: 2010/10/27 22:19:50 $
 * @author   rphall
 */
public class BatchQueryListener extends WorkflowControlListener{

	private static final Logger log = Logger.getLogger(BatchQueryListener.class.getName());

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public BatchQueryListener() {
    	log.debug("constructor");
	}
	
	protected boolean isAbortCheckRequired() {return true;}


	protected long getUrmJobId(long batchJobId) 
								throws NamingException,RemoteException,JMSException,ConfigException,
								CmRuntimeException,SQLException,CreateException,ArgumentException,ModelException {

		BatchJob batchJob = Single.getInst().findBatchJobById(batchJobId);
		long urmJobId = batchJob.getTransactionId().longValue();
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
									CreateException
									//,ArgumentException,ModelException 
									{
						
		urmJob.markAsTransOABA();
		SerialRecordSource qrs = urmJob.getQueryRs();
		SerialRecordSource mrs = urmJob.getMasterRs();

		TransitivityOABAService ts = Single.getInst().getTransitivityOABAService();
		float lth = urmJob.getLowerThreshold().floatValue();
		float uth = urmJob.getUpperThreshold().floatValue();
		String modelName = urmJob.getModelName();
		
		if( urmJob.markAbortedIfRequested())
			throw new JobAlreadyAbortedException();
			
		long tid = ts.startTransitivity(prevStepId);
		log.debug("transitivity preprocessing is started");
		return tid;
	} 
	
	public void abortJobStep(long id) throws ConfigException, CmRuntimeException {
		TransitivityOABAService ts = Single.getInst().getTransitivityOABAService();
		log.debug("transitivity preprocessing abort is issued");
		//TODO: abort transitivity service
	}

}