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
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
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

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

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
		OabaJobMessage data = null;
		OabaJob oabaJob = null;

		log.info("DedupOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				oabaJob = jobController.find(jobId);
				OabaParameters params =
					paramsController.findBatchParamsByJobId(jobId);
				OabaSettings oabaSettings =
						settingsController.findOabaSettingsByJobId(jobId);
				OabaProcessing processingEntry =
						processingController.findProcessingLogByJobId(jobId);
				if (oabaJob == null || params == null || oabaSettings == null) {
					String s = "Unable to find a job, parameters or settings for " + jobId;
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				final String modelConfigId = params.getModelConfigurationName();
				ImmutableProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No model corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(oabaJob.getStatus())) {
					MessageBeanUtils.stopJob (oabaJob, processingEntry);

				} else {
					
					// Handle regular blocking sets
					final int maxBlock = oabaSettings.getMaxBlockSize();
					final int interval = oabaSettings.getInterval();
					final BlockGroup bGroup =
							new BlockGroup(
									OabaFileUtils.getBlockGroupFactory(oabaJob),
									maxBlock);
					BlockDedupService4 dedupService =
						new BlockDedupService4(
								bGroup,
								OabaFileUtils
										.getBigBlocksSinkSourceFactory(oabaJob),
								OabaFileUtils
										.getTempBlocksSinkSourceFactory(oabaJob),
								OabaFileUtils.getSuffixTreeSink(oabaJob),
								maxBlock, processingEntry, oabaJob, interval);
					dedupService.runService();
					log.info( "Done block dedup " + dedupService.getTimeElapsed());
					log.info ("Blocks In " + dedupService.getNumBlocksIn());
					log.info ("Blocks Out " + dedupService.getNumBlocksOut());
					log.info ("Tree Out " + dedupService.getNumTreesOut());

					// Handle oversized blocking sets
					final IBlockSink osSpecial =
							OabaFileUtils.getOversizedFactory(oabaJob)
									.getNextSink();
					final IBlockSinkSourceFactory osFactory =
						OabaFileUtils.getOversizedFactory(oabaJob);
					final IBlockSource osSource = osFactory.getSource(osSpecial);
					final IBlockSink osDedup = osFactory.getNextSink();

					OversizedDedupService osDedupService =
						new OversizedDedupService (osSource, osDedup,
						OabaFileUtils.getOversizedTempFactory(oabaJob),
						processingEntry, oabaJob);
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

		} catch (Exception e) {
			log.severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
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
	private void sendToChunk (OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, jmsContext, chunkQueue, log);
	}


}
