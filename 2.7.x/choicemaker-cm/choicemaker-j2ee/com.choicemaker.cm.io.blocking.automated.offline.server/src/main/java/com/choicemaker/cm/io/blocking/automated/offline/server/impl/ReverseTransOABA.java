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

import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
//import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
//import com.choicemaker.cm.core.base.PMManager;

/**
 * This bean handles the reverse translation of internal ids back to record ids.
 *
 * @author pcheung
 *
 */
public class ReverseTransOABA implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ReverseTransOABA.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + ReverseTransOABA.class.getName());

	@PersistenceContext (unitName = "oaba")
	EntityManager em;

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
//	private transient OABAConfiguration oabaConfig = null;
//	private transient QueueConnection connection = null;

	public void ejbCreate() {
//	log.fine("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.severe(e.toString());
		}
//	log.fine("...finished ejbCreate");
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
				batchJob = configuration.findBatchJobById(em, BatchJobBean.class, data.jobID);

				//init values
				// 2014-04-24 rphall: Commented out unused local variable.
				// Any side effects?
//				ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
				OABAConfiguration oabaConfig = new OABAConfiguration (data.modelConfigurationName, data.jobID);
//				Status status = data.status;
				OabaProcessing status = configuration.getProcessingLog(em, data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					batchJob.markAsAborted();

					if (batchJob.getDescription().equals(BatchJob.STATUS_CLEAR)) {
						status.setCurrentProcessingEvent (OabaProcessing.DONE_OABA);
						oabaConfig.removeTempDir();
					}
				} else {

					// 2014-04-24 rphall: Commented out unused local variable.
//					String temp = (String) stageModel.properties().get("maxBlockSize");
//					int maxBlock = Integer.parseInt(temp);

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

					if (status.getStatus() <= OabaProcessing.DONE_REVERSE_TRANSLATE_OVERSIZED) {
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
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.severe(e.toString());
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


			log.fine ("Sending on queue '" + queue.getQueueName()) ;
			log.fine("connection " + connection);
			log.fine("session " + session);
			log.fine("message " + message);
			log.fine("sender " + sender);

		} catch (Exception ex) {
			log.severe(ex.toString());
		} finally {
			try {
				if (session != null) session.close();
			} catch (JMSException ex) {
				log.severe(ex.toString());
			}
		}

		log.fine ("...finished sendMessage");
	}
*/

}
