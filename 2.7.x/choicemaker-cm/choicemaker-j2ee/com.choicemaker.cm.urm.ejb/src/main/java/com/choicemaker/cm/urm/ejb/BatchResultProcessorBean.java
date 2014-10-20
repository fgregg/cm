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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;

import com.choicemaker.cm.core.xmlconf.EmbeddedXmlConfigurator;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.urm.base.JobStatus;
import com.choicemaker.cm.urm.exceptions.ArgumentException;
import com.choicemaker.cm.urm.exceptions.CmRuntimeException;
import com.choicemaker.cm.urm.exceptions.ConfigException;
import com.choicemaker.cm.urm.exceptions.ModelException;
import com.choicemaker.cm.urm.exceptions.RecordCollectionException;





/**
 * @author emoussikaev
 * @version Revision: 2.5  Date: Jul 15, 2005 3:41:42 PM
 * @see
 */
public class BatchResultProcessorBean implements SessionBean {

	private static final long serialVersionUID = 1L;

	protected static Logger log = Logger.getLogger(BatchResultProcessorBean.class.getName());
	
	public final static String JMS_MRPS_PROCESSOR_QUEUE = "java:comp/env/jms/mrpsProcessorQueue";

//	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

	protected transient SessionContext sessionContext;
	protected static boolean initialized = false;
		
	public BatchResultProcessorBean() {
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
		try {
			if (!initialized) {
				EmbeddedXmlConfigurator.getInstance().embeddedInit(null);
				initialized = true;
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
			throw new CreateException(ex.toString());
		}

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

	public long startResultToMrpsConversion(
		long urmId,
		String mrpsUrl,
		String externalId)
		throws
			ModelException,
			RecordCollectionException,
			ConfigException,
			ArgumentException,
			CmRuntimeException,
			RemoteException {
		Properties defaults = new MrpsRequestConfiguration().getProperties();
		return startResultToMrpsConversion(urmId,mrpsUrl,externalId,defaults);
	}

	public long startResultToMrpsConversion(
		long jobId,
		// int batchSize,
		String mrpsUrl,
		String externalId,
		Properties optional)
					throws
						ModelException, 	
						RecordCollectionException,
						ConfigException,
						ArgumentException,
						CmRuntimeException, 
						RemoteException
	{
		log.fine("<<startResultToMrpsConversion");
		long batchJobId = -1;
		
		BatchJob bj = Single.getInst().findBatchJobById(em, jobId);
		batchJobId = bj.getId();
		
		if(batchJobId == -1){		
			UrmStepJob batchStep = Single.getInst().findStepJobByUrmAndIndex(jobId,BatchMatchAnalyzerBean.BATCH_MATCH_STEP_INDEX);					
			batchJobId  = batchStep.getStepJobId().longValue();		
			log.fine("batch job jd = "+batchJobId);		
			bj  = Single.getInst().findBatchJobById(em, batchJobId);
		}
				
		CmsJob oj = Single.getInst().createCmsJob(externalId, JobStatus.UNDEFINED_ID);
		oj.markAsStarted();
		
		URL url;
		try {
			url = new URL(mrpsUrl);
		} catch (MalformedURLException e) {
			log.severe(e.toString());
			throw new ArgumentException("invalid target url; "+e.toString()); 
		}
							
		String mrpsFilename = null;					
		if(  url.getProtocol().equals("file")){ 
			mrpsFilename = url.getFile();					
		}
		else
			throw new ArgumentException("invalid target url; protocol is not supported"); 
		
		MrpsRequestConfiguration configuration = new MrpsRequestConfiguration();
		configuration.setProperties(optional);

		MrpsRequest br =
			new MrpsRequest(
				oj.getId().longValue(),
				batchJobId,
				externalId,
				mrpsFilename,
				configuration);

		sendToMrpsProcessorBean(br);
		log.fine (">>startResultToMrpsConversion");
		return oj.getId().longValue();		
	}

										
	
	 
	private void sendToMrpsProcessorBean(MrpsRequest br) throws ConfigException, CmRuntimeException  {
		Queue queue = Single.getInst().getMessageQueue(JMS_MRPS_PROCESSOR_QUEUE);
		Single.getInst().sendMessage(queue, br);
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

	public String getVersion(Object context)
						throws  RemoteException {
		return Single.getInst().getVersion();					
	}											

}



	
