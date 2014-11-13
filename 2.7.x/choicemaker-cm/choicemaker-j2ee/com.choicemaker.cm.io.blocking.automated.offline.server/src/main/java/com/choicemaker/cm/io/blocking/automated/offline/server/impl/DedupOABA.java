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

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService4;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;

/**
 * This bean handles the deduping of blocks and oversized blocks.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/dedupQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class DedupOABA implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(DedupOABA.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ DedupOABA.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Resource
	private MessageDrivenContext mdc;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/chunkQueue")
	private Queue chunkQueue;

	@Inject
	JMSContext jmsContext;

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		log.info("DedupOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				final long jobId = data.jobID;
				batchJob =
					configuration.findBatchJobById(em, BatchJobBean.class,
							data.jobID);

				//init values
				BatchParameters params =
					configuration.findBatchParamsByJobId(em, batchJob.getId());
				final String modelConfigId = params.getModelConfigurationName();
				IProbabilityModel stageModel =
					PMManager.getModelInstance(modelConfigId);
				if (stageModel == null) {
					String s =
						"No model corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				OABAConfiguration oabaConfig =
					new OABAConfiguration(params.getModelConfigurationName(),
							jobId);

				// get the status
				OabaProcessing processingEntry =
					configuration.getProcessingLog(em, data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					MessageBeanUtils.stopJob (batchJob, processingEntry, oabaConfig);

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
						maxBlock, processingEntry, batchJob, interval);
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
						processingEntry, batchJob);
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
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}


	/** This method sends a message to the DedupOABA message bean.
	 *
	 * @param request
	 */
	private void sendToChunk (StartData data) {
		MessageBeanUtils.sendStartData(data, jmsContext, chunkQueue, log);
	}


}
