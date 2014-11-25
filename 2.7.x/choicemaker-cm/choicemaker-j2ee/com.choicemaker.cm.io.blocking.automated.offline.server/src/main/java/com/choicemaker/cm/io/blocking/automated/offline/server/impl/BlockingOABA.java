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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
//import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;

/**
 * This message bean performs the OABA blocking and oversized trimming.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/blockQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class BlockingOABA implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(BlockingOABA.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ BlockingOABA.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@Resource(lookup = "java:/choicemaker/urm/jms/dedupQueue")
	private Queue dedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	JMSContext jmsContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage data = null;
		OabaJob oabaJob = null;

		log.info("BlockingOABA In onMessage");

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

				if (BatchJob.STATUS_ABORT_REQUESTED
						.equals(oabaJob.getStatus())) {
					MessageBeanUtils.stopJob(oabaJob, processingEntry);

				} else {

					// Start blocking
					final int maxBlock = oabaSettings.getMaxBlockSize();
					final int maxOversized = oabaSettings.getMaxOversized();
					final int minFields = oabaSettings.getMinFields();
					final BlockGroup bGroup =
						new BlockGroup(
								OabaFileUtils.getBlockGroupFactory(oabaJob),
								maxBlock);
					final IBlockSink osSpecial =
						OabaFileUtils.getOversizedFactory(oabaJob)
								.getNextSink();
					OABABlockingService blockingService =
						new OABABlockingService(maxBlock, bGroup, OabaFileUtils
								.getOversizedGroupFactory(oabaJob), osSpecial,
								null, OabaFileUtils.getRecValFactory(oabaJob),
								data.numBlockFields, data.validator,
								processingEntry, oabaJob, minFields,
								maxOversized);
					blockingService.runService();

					log.info("num Blocks " + blockingService.getNumBlocks());
					log.info("num OS " + blockingService.getNumOversized());
					log.info("Done Blocking: "
							+ blockingService.getTimeElapsed());

					// clean up
					blockingService = null;
					System.gc();

					sendToUpdateStatus(data.jobID,
							OabaProcessing.PCT_DONE_OVERSIZED_TRIMMING);
					sendToDedup(data);

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

	private void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext, updateQueue, log);
	}

	private void sendToDedup(OabaJobMessage data) throws NamingException,
			JMSException {
		MessageBeanUtils.sendStartData(data, jmsContext, dedupQueue, log);
	}

}
