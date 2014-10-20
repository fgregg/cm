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
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;



/**
 * TODO
 * @version  $Revision: 1.5 $ $Date: 2010/10/27 22:19:50 $
 * @author   rphall
 */
public class TransOabaListener extends WorkflowControlListener{

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransOabaListener.class.getName());

//	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

	/**
	 * Constructor, which is public and takes no arguments.
	 */
	public TransOabaListener() {
    	log.fine("TransOabaListener constructor");
	}
	protected boolean isAbortCheckRequired() {return true;}
		
	protected long getUrmJobId(long jobId) 
								throws NamingException,RemoteException,JMSException,ConfigException,
								CmRuntimeException,SQLException,CreateException,ArgumentException,ModelException {

//		TransitivityJob job = Single.getInst().findTransJobById(jobId);
		BatchJob job = Single.getInst().findBatchJobById(em,jobId);
		long urmJobId = job.getTransactionId();		

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
						
		urmJob.markAsSerializing();
		TransSerializer ts = Single.getInst().getTransSerializer();
						
		if( urmJob.markAbortedIfRequested())
			throw new JobAlreadyAbortedException();

		long sid = 	ts.startSerialization(urmJob.getId().longValue(), 
				prevStepId,
				urmJob.getGroupMatchType(),
				urmJob.getSerializationType(),
				urmJob.getModelName(),
				urmJob.getExternalId());
		log.fine("transitivity processing and serialization is started");					
		return sid;
	} 
	
	public void abortJobStep(long id) throws ConfigException, CmRuntimeException {
		log.fine("transitivity processing and serialization abort is ignored");
		//TODO: abort serialization step
	}

}
