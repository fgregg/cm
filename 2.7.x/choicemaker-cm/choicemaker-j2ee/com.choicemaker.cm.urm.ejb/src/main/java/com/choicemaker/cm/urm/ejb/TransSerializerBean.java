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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.Queue;

import org.apache.log4j.Logger;

import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;





/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 15, 2005 3:41:42 PM
 * @see
 */
public class TransSerializerBean implements SessionBean {

	private static final long serialVersionUID = 1L;

	protected static Logger log = Logger.getLogger(TransSerializerBean.class);
	
	public final static String JMS_TRANS_SERIALIZATION_QUEUE = "java:comp/env/jms/transSerializationQueue";

	protected transient SessionContext sessionContext;
		
	public TransSerializerBean() {
		super();
	}
	
	/**
	 * 
	 */

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {

	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}
	
	public void ejbCreate() throws CreateException, RemoteException {

	} // ejbCreate()

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
			this.sessionContext = sessionContext;
	}

	public long  startSerialization(
						long  transactionId,
						long	batchJobId,
						String  groupMatchType,
						String  serializationType,
						String modelName, 
						String externalId) 
					throws
						ModelException, 	
						ConfigException,
						ArgumentException,
						CmRuntimeException, 
						RemoteException
	{
		log.debug("<<startSerialization");
		CmsJob oj = Single.getInst().createCmsJob(externalId, transactionId);
		oj.markAsStarted();
		TransSerializeData tsd = new TransSerializeData();
		tsd.ownId = oj.getId().longValue();
		tsd.batchId = batchJobId;
		tsd.transId = transactionId;
		tsd.externalId = externalId;
		tsd.groupMatchType = groupMatchType;
		tsd.serializationType = serializationType; 
		
		sendToSerializer(tsd);
		log.debug (">>startSerialization");
		return tsd.ownId;		
	}

										
	
	 
	private void sendToSerializer(TransSerializeData d) throws ConfigException, CmRuntimeException  {
		Queue queue = Single.getInst().getMessageQueue(JMS_TRANS_SERIALIZATION_QUEUE);
		Single.getInst().sendMessage(queue, d);
		log.info ("msg sent to transSerializationQueue ");
	}


	/**
	 * Aborts the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 					abortJob(
									long jobId
									)
								throws	ArgumentException,
										ConfigException,
										CmRuntimeException, 
										RemoteException

	{
		boolean res = false;//abortBatchJob(jobID,false);
		//TODO: implement 
		return res;
	}

	/**
	 * Suspends the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 		suspendJob(
							long jobID 
						)
						throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException

	{
		boolean res = false;//abortBatchJob(jobID,false);
		//TODO: implement 
		return res;
	}

	/**
	 * Resumess the matching process with the given job ID.
	 * 
	 * @param   jobID		Job ID.
	 
	 * @return  Job ID.
	 * @throws  RemoteException
	 */	
	public boolean 		resumeJob(
							long jobID 
						)
						throws	ArgumentException,
								ConfigException,
								CmRuntimeException, 
								RemoteException

	{
		boolean res = false;//resumeBatchJob(jobID);
		//TODO: implement 
		return res;
	}

	/**
		 * Cleans serialized data related to the process with the give job ID (including the file with matching results).
		 * 
		 * @param   jobID		Job ID.
		 * @return  Job ID.
		 * @throws  RemoteException
		*/	
		public boolean					cleanJob (
											long jobID
										)
										throws	ArgumentException,
												CmRuntimeException,
												ConfigException, 
												RemoteException
		
		{
			return false;
		}

	public String getVersion(Object context)
						throws  RemoteException {
		return Single.getInst().getVersion();					
	}											

}



	
