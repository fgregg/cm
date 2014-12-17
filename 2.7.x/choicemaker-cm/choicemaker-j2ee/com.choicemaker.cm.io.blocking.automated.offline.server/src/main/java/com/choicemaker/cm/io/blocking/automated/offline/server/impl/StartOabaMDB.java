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

import java.io.IOException;
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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.services.RecValService3;

/**
 * This message bean is the first step of the OABA. It creates rec_id, val_id
 * files using internal id translation.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/startQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class StartOabaMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(StartOabaMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ StartOabaMDB.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private PersistableRecordSourceController rsController;

	@Resource(lookup = "java:/choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

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

		log.info("StartOabaMDB In onMessage");

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				oabaJob = jobController.findOabaJob(jobId);
				OabaParameters params =
					paramsController.findBatchParamsByJobId(jobId);
				OabaSettings oabaSettings =
					oabaSettingsController.findOabaSettingsByJobId(jobId);
				OabaProcessing processingEntry =
					processingController.findProcessingLogByJobId(jobId);
				if (oabaJob == null || params == null || oabaSettings == null) {
					String s =
						"Unable to find a job, parameters or settings for "
								+ jobId;
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				final String modelConfigId = params.getModelConfigurationName();
				IProbabilityModel stageModel =
					PMManager.getModelInstance(modelConfigId);
				if (stageModel == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}

				// update status to mark as start
				oabaJob.markAsStarted();

				log.info("Job id: " + jobId);
				log.info("Model configuration: " + params.getModelConfigurationName());
				log.info("Differ threshold: " + params.getLowThreshold());
				log.info("Match threshold: " + params.getHighThreshold());
				log.info("Staging record source id: " + params.getStageRsId());
				log.info("Staging record source type: " + params.getStageRsType());
				log.info("Master record source id: " + params.getMasterRsId());
				log.info("Master record source type: " + params.getMasterRsType());
				log.info("Linkage type: " + params.getOabaLinkageType());

				// check to see if there are a lot of records in stage.
				// if not use single record matching instead of batch.
				log.info("Checking whether to use single- or batched-record blocking...");
				log.info("OabaSettings maxSingle: " + oabaSettings.getMaxSingle());
				final ISerializableRecordSource staging =
						rsController.getStageRs(params);
					final ISerializableRecordSource master =
						rsController.getMasterRs(params);
				if (!isMoreThanThreshold(staging, stageModel,
						oabaSettings.getMaxSingle())) {
					log.info("Using single record matching");
					sendToSingleRecordMatching(data);

				} else {
					log.info("Using batch record matching");

					RecordIDSinkSourceFactory idFactory =
						OabaFileUtils.getTransIDFactory(oabaJob);
					RecordIDTranslator2 translator =
						new RecordIDTranslator2(idFactory);

					// create rec_id, val_id files
					RecValSinkSourceFactory recvalFactory =
						OabaFileUtils.getRecValFactory(oabaJob);
					RecValService3 rvService =
						new RecValService3(staging, master, stageModel,
								recvalFactory, translator, processingEntry,
								oabaJob);
					rvService.runService();

					// FIXME move these parameters to a persistent object
					data.stageType = rvService.getStageType();
					data.masterType = rvService.getMasterType();
					data.numBlockFields = rvService.getNumBlockingFields();

					log.info("Done creating rec_id, val_id files: "
							+ rvService.getTimeElapsed());

					// create the validator after rvService
					// Validator validator = new Validator (true, translator);
					ValidatorBase validator =
						new ValidatorBase(true, translator);
					// FIXME move this parameter to a persistent operational
					// object
					data.validator = validator;

					sendToUpdateStatus(jobId, OabaProcessing.PCT_DONE_REC_VAL);
					sendToBlocking(data);
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

	/**
	 * This method checks to see if the number of records in the RecordSource is
	 * greater than the threshold.
	 *
	 * @param rs
	 *            - RecordSource
	 * @param accessProvider
	 *            - Probability Model of this RecordSource
	 * @param threshold
	 *            - The number of records threshold
	 * @return boolean - true if the RecordSource contains more than the
	 *         threshold
	 * @throws OABABlockingException
	 */
	private boolean isMoreThanThreshold(RecordSource rs,
			IProbabilityModel model, int threshold) throws BlockingException {
		
		// Preconditions need to be checked on this method, even though it is
		// private, because the arguments haven't been validated before it
		// is invoked. The arguments are derived from a persistent object,
		// BatchParameters, which may have been saved with invalid fields.
		if (model == null) {
			throw new IllegalArgumentException("null modelId");
		}
		if (rs == null) {
			throw new IllegalArgumentException("null record source");
		}

		boolean retVal = false;
		log.info("Checking if the number of records is more than the maxSingle threshold: "
				+ threshold);
		if (threshold <= 0) {
			log.info("The threshold shortcuts further checking");
			retVal = true;
		} else {
			log.info("Record source: " + rs);
			log.info("Model: " + model);
			try {
				rs.setModel(model);
				rs.open();
				int count = 1;
				while (count <= threshold && rs.hasNext()) {
					rs.getNext();
					count++;
				}
				if (rs.hasNext()) {
					++count;
					log.info("Number of records: " + count + "+");
					retVal = true;
				} else {
					log.info("Number of records: " + count);
				}
				rs.close();
			} catch (IOException ex) {
				throw new BlockingException(ex.toString(), ex);
			}
		}
		String msg =
			"The number of records " + (retVal ? "exceeds" : "does not exceed")
					+ " the maxSingle threshold: " + threshold;
		log.info(msg);

		return retVal;
	}

	private void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToBlocking(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, jmsContext, blockQueue, log);
	}

	private void sendToSingleRecordMatching(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, jmsContext, singleMatchQueue, log);
	}

}
