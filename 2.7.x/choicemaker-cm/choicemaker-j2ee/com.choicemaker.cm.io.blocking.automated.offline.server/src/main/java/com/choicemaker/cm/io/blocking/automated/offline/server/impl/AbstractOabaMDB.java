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
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
//import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;

/**
 * Common functionality of message driven beans that perform OABA processing
 * steps.
 *
 * @author rphall
 *
 */
public abstract class AbstractOabaMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;

	// -- Instance data

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private OabaProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private OperationalPropertyController propController;

	// This member will go away, but for now accessible from sub-classes
	@Inject
	private JMSContext jmsContext;

	protected JMSContext getJmsContext() {
		return jmsContext;
	}

	// -- Accessors

	protected final OabaJobControllerBean getJobController() {
		return jobController;
	}

	protected final OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	protected final OabaParametersControllerBean getParametersController() {
		return paramsController;
	}

	protected final OabaProcessingController getProcessingController() {
		return processingController;
	}

	protected final ServerConfigurationController getServerController() {
		return serverController;
	}

	protected final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	protected final RecordIdController getRecordIdController() {
		return ridController;
	}

	protected final OperationalPropertyController getPropertyController() {
		return propController;
	}

	// -- Template methods

	@Override
	public void onMessage(Message inMessage) {
		getJmsTrace().info(
				"Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage oabaMsg = null;
		OabaJob oabaJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				oabaMsg = (OabaJobMessage) msg.getObject();

				final long jobId = oabaMsg.jobID;
				oabaJob = getJobController().findOabaJob(jobId);
				OabaParameters oabaParams =
					getParametersController().findBatchParamsByJobId(jobId);
				OabaSettings oabaSettings =
					getSettingsController().findOabaSettingsByJobId(jobId);
				OabaEventLog processingLog =
					getProcessingController().getProcessingLog(oabaJob);
				ServerConfiguration serverConfig =
					getServerController().findServerConfigurationByJobId(jobId);
				if (oabaJob == null || oabaParams == null
						|| oabaSettings == null || serverConfig == null) {
					String s =
						"Unable to find a job, parameters, settings or server configuration for "
								+ jobId;
					getLogger().severe(s);
					throw new IllegalStateException(s);
				}
				final String modelConfigId =
					oabaParams.getModelConfigurationName();
				ImmutableProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					getLogger().severe(s);
					throw new IllegalArgumentException(s);
				}

				if (BatchJobStatus.ABORT_REQUESTED.equals(oabaJob.getStatus())) {
					abortProcessing(oabaJob, processingLog);
				} else {
					processOabaMessage(oabaMsg, oabaJob, oabaParams,
							oabaSettings, processingLog, serverConfig, model);
					updateOabaProcessingStatus(oabaJob, getCompletionEvent(),
							new Date(), null);
					notifyProcessingCompleted(oabaMsg);
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			getLogger().severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
		}
		getJmsTrace()
				.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected void abortProcessing(OabaJob oabaJob, OabaEventLog processingLog) {
		MessageBeanUtils.stopJob(oabaJob, getPropertyController(),
				processingLog);
	}

	protected void updateOabaProcessingStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

	// -- Abstract call-back methods

	protected abstract Logger getLogger();

	protected abstract Logger getJmsTrace();

	protected abstract void processOabaMessage(OabaJobMessage data,
			OabaJob oabaJob, OabaParameters params, OabaSettings oabaSettings,
			OabaEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException;

	protected abstract OabaEvent getCompletionEvent();

	protected abstract void notifyProcessingCompleted(OabaJobMessage data);

}
