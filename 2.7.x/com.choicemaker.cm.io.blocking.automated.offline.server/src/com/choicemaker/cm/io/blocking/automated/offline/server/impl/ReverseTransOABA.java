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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;

/**
 * This bean handles the reverse translation of internal ids back to record ids.
 * 
 * @author pcheung
 *
 */
public class ReverseTransOABA implements MessageDrivenBean, MessageListener {

	private static final Logger log = Logger.getLogger(ReverseTransOABA.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + ReverseTransOABA.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;
	private transient QueueConnection connection = null;

	public void ejbCreate() {
//	log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
//	log.debug("...finished ejbCreate");
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc)
		throws EJBException {
			this.mdc = mdc;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		log.info("ReverseOABA In onMessage");
		
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();
				batchJob = configuration.findBatchJobById(data.jobID);
				
				//init values
				ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
				OABAConfiguration oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
//				Status status = data.status;
				IStatus status = configuration.getStatusLog(data);
				
				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					batchJob.markAsAborted();

					if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
						status.setStatus (IStatus.DONE_PROGRAM);
						oabaConfig.removeTempDir();
					}
				} else {
					
					String temp = (String) stageModel.properties().get("maxBlockSize");
					int maxBlock = Integer.parseInt(temp);

					//dedup is no longer needed. 
					
/*
					//need to recover the translator
					RecordIDTranslator translator = new RecordIDTranslator (oabaConfig.getTransIDFactory());
					translator.recover(); 

					//reverse translate
					IBlockSinkSourceFactory bFactory = oabaConfig.getBlockFactory();
					IBlockSink bSink = bFactory.getNextSink();
					IBlockSource source = bFactory.getSource(bSink);
					bSink = bFactory.getNextSink();
					
					IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
					IBlockSink osDedup = osFactory.getNextSink();
					osDedup = osFactory.getNextSink();
					IBlockSource source2 = osFactory.getSource(osDedup);
					osDedup = osFactory.getNextSink();
					
					ReverseTranslateService rtService = new ReverseTranslateService (translator,
						source, bSink, source2, osDedup, status);
					rtService.runService();
					log.info( "Done reverse translate " + rtService.getTimeElapsed());
					
					if (status.getStatus() <= IStatus.DONE_REVERSE_TRANSLATE_OVERSIZED) {
						//write block statistics
						source = bFactory.getSource(bSink);
						
						BlockStatistics stat2 = new BlockStatistics ( source, maxBlock, 10);
						stat2.writeStat();
					}
*/

					sendToUpdateStatus (data.jobID, 40);
				
					sendToChunk (data);
				}
				
			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	/** This method sends a message to the UpdateStatus message bean.
	 * 
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException {

		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;
		
		configuration.sendMessage(queue, data);
	} 


	/** This method sends a message to the DedupOABA message bean.
	 * 
	 * @param request
	 * @throws NamingException
	 */
	private void sendToChunk (StartData data) throws NamingException{
		Queue queue = configuration.getChunkMessageQueue();
		configuration.sendMessage(queue, data);
	} 


/*
	private void sendMessage (Queue queue, Serializable data) {
		QueueSession session = null;
		
		try {
			if (connection == null) {
				QueueConnectionFactory factory = configuration.getQueueConnectionFactory();
				connection = factory.createQueueConnection();
			}
			this.connection.start ();
			session = this.connection.createQueueSession (false, QueueSession.AUTO_ACKNOWLEDGE);
//			QueueSession session = this.connection.createQueueSession(true, 0);

			ObjectMessage message = session.createObjectMessage(data);
			QueueSender sender = session.createSender(queue);

			sender.send(message);


			log.debug ("Sending on queue '" + queue.getQueueName()) ;
			log.debug("connection " + connection);
			log.debug("session " + session);
			log.debug("message " + message);
			log.debug("sender " + sender);

		} catch (Exception ex) {
			log.error(ex.toString(),ex);
		} finally {
			try {
				if (session != null) session.close();
			} catch (JMSException ex) {
				log.error(ex.toString(),ex);
			}
		}
		
		log.debug ("...finished sendMessage");
	}
*/
	
}
