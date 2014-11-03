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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService4;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;

/**
 * This bean handles the deduping of blocks and oversized blocks.
 *
 * @author pcheung
 *
 */
public class DedupOABA implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(DedupOABA.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + DedupOABA.class.getName());

	@PersistenceContext (unitName = "oaba")
	EntityManager em;

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;

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
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		log.info("DedupOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				batchJob = configuration.findBatchJobById(em, BatchJobBean.class, data.jobID);

				//init values
				ImmutableProbabilityModel stageModel = PMManager.getModelInstance(data.modelConfigurationName);
				OABAConfiguration oabaConfig = new OABAConfiguration (data.modelConfigurationName, data.jobID);
//				Status status = data.status;
				OabaProcessing status = configuration.getProcessingLog(em, data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					MessageBeanUtils.stopJob (batchJob, status, oabaConfig);

				} else {
					String temp = (String) stageModel.properties().get("maxBlockSize");
					int maxBlock = Integer.parseInt(temp);

					temp = (String) stageModel.properties().get("interval");
					int interval = Integer.parseInt(temp);

					//using BlockGroup to speed up dedup later
					BlockGroup bGroup = new BlockGroup (oabaConfig.getBlockGroupFactory(), maxBlock);
					BlockDedupService4 dedupService = new BlockDedupService4 (bGroup,
						oabaConfig.getBigBlocksSinkSourceFactory(),
						oabaConfig.getTempBlocksSinkSourceFactory(),
						oabaConfig.getSuffixTreeSink(),
						maxBlock, status, batchJob, interval);
					dedupService.runService();
					log.info( "Done block dedup " + dedupService.getTimeElapsed());
					log.info ("Blocks In " + dedupService.getNumBlocksIn());
					log.info ("Blocks Out " + dedupService.getNumBlocksOut());
					log.info ("Tree Out " + dedupService.getNumTreesOut());


					//start oversized dedup
					IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
					IBlockSink osSpecial = osFactory.getNextSink();
					IBlockSource osSource = osFactory.getSource(osSpecial);
					IBlockSink osDedup = osFactory.getNextSink();

					OversizedDedupService osDedupService =
						new OversizedDedupService (osSource, osDedup,
						oabaConfig.getOversizedTempFactory(),
						status, batchJob);
					osDedupService.runService();
					log.info( "Done oversized dedup " + osDedupService.getTimeElapsed());
					log.info ("Num OS Before " + osDedupService.getNumBlocksIn());
					log.info ("Num OS After Exact " + osDedupService.getNumAfterExact());
					log.info ("Num OS Done " + osDedupService.getNumBlocksOut());
					sendToUpdateStatus (data.jobID, 30);

					sendToChunk (data);
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			log.severe(e.toString());
			assert batchJob != null;
			batchJob.markAsFailed();
		} catch (Exception e) {
			log.severe(e.toString());
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
	private void sendToChunk (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getChunkMessageQueue();
		configuration.sendMessage(queue, data);
	}


}
