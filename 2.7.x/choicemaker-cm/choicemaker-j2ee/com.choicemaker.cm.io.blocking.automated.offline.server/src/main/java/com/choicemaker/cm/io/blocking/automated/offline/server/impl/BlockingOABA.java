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
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;

/**
 * This message bean performs the OABA blocking and oversized trimming.
 *
 * @author pcheung
 *
 */
public class BlockingOABA implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(BlockingOABA.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + BlockingOABA.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;

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
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		log.info("BlockingOABA In onMessage");

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
					MessageBeanUtils.stopJob (batchJob, status, oabaConfig);

				} else {

					String temp = (String) stageModel.properties().get("maxBlockSize");
					int maxBlock = Integer.parseInt(temp);

					temp = (String) stageModel.properties().get("maxOversized");
					int maxOversized = Integer.parseInt(temp);

					temp = (String) stageModel.properties().get("minFields");
					int minFields = Integer.parseInt(temp);

					//using BlockGroup to speed up dedup later
					BlockGroup bGroup = new BlockGroup (oabaConfig.getBlockGroupFactory(), maxBlock);

					IBlockSink osSpecial =  oabaConfig.getOversizedFactory().getNextSink();

					//Start blocking
					OABABlockingService blockingService = new OABABlockingService (maxBlock, bGroup,
						oabaConfig.getOversizedGroupFactory(),
						osSpecial, null, oabaConfig.getRecValFactory(), data.numBlockFields,
						data.validator, status, batchJob, minFields, maxOversized);
					blockingService.runService();
					log.info ("num Blocks " + blockingService.getNumBlocks());
					log.info ("num OS " + blockingService.getNumOversized());

					log.info("Done Blocking: " + blockingService.getTimeElapsed() );

					//clean up
					blockingService = null;
					System.gc();

					sendToUpdateStatus (data.jobID, 20);

					sendToDedup (data);

				}

			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			assert batchJob != null;
			try {
				batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	/** This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
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
	private void sendToDedup (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getDedupMessageQueue();
		configuration.sendMessage(queue, data);
	}


}
