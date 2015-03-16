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
package com.choicemaker.cm.transitivity.server.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.transitivity.core.TransitivityProcessingEvent;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityConfigurationController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivitySettingsController;
import com.choicemaker.cm.transitivity.server.util.LoggingUtils;

/**
 * Common functionality of message driven beans that perform OABA processing
 * steps.
 *
 * @author rphall
 *
 */
public abstract class AbstractTransitivityMDB implements MessageListener,
		Serializable {

	private static final long serialVersionUID = 271L;

	// -- Instance data

	@EJB
	private OabaJobController oabaJobController;

	@EJB
	private TransitivityJobController transJobController;

	@EJB
	private TransitivitySettingsController settingsController;

	@EJB
	private TransitivityParametersController paramsController;

	@EJB(beanName = "TransitivityProcessingControllerBean")
	private ProcessingController processingController;

	@EJB
	private TransitivityConfigurationController serverController;

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

	protected final OabaJobController getOabaJobController() {
		return oabaJobController;
	}

	protected final TransitivityJobController getTransitivityJobController() {
		return transJobController;
	}

	protected final TransitivitySettingsController getSettingsController() {
		return settingsController;
	}

	protected final TransitivityParametersController getParametersController() {
		return paramsController;
	}

	protected final ProcessingController getProcessingController() {
		return processingController;
	}

	protected final TransitivityConfigurationController getServerController() {
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
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				oabaMsg = (OabaJobMessage) msg.getObject();

				final long jobId = oabaMsg.jobID;
				batchJob = getTransitivityJobController().findTransitivityJob(jobId);
				TransitivityParameters transParams =
					getParametersController()
							.findTransitivityParametersByJobId(jobId);
				OabaSettings settings =
					getSettingsController().findSettingsByTransitivityJobId(jobId);
				ProcessingEventLog processingLog =
					getProcessingController().getProcessingLog(batchJob);
				ServerConfiguration serverConfig =
					getServerController().findConfigurationByTransitivityJobId(jobId);

				if (batchJob == null || transParams == null
						|| settings == null || serverConfig == null) {
					String s0 =
							"Unable to find a job, parameters, settings or server configuration for "
									+ jobId;
					String s = LoggingUtils.buildDiagnostic(s0, batchJob, transParams, settings, serverConfig);
					getLogger().severe(s);
					throw new IllegalStateException(s);
				}

				final String modelConfigId =
					transParams.getModelConfigurationName();
				ImmutableProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					getLogger().severe(s);
					throw new IllegalArgumentException(s);
				}

				if (BatchJobStatus.ABORT_REQUESTED.equals(batchJob.getStatus())) {
					abortProcessing(batchJob, processingLog);

				} else {
					processOabaMessage(oabaMsg, batchJob, transParams,
							settings, processingLog, serverConfig, model);
					updateTransivitityProcessingStatus(batchJob,
							getCompletionEvent(), new Date(), null);
					notifyProcessingCompleted(oabaMsg);
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			getLogger().severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		getJmsTrace()
				.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected void abortProcessing(BatchJob batchJob,
			ProcessingEventLog processingLog) {
		MessageBeanUtils.stopJob(batchJob, getPropertyController(),
				processingLog);
	}

	protected void updateTransivitityProcessingStatus(BatchJob job,
			TransitivityProcessingEvent event, Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

	// -- Abstract call-back methods

	protected abstract Logger getLogger();

	protected abstract Logger getJmsTrace();

	protected abstract void processOabaMessage(OabaJobMessage data,
			BatchJob batchJob, TransitivityParameters params,
			OabaSettings oabaSettings, ProcessingEventLog processingLog,
			ServerConfiguration serverConfig, ImmutableProbabilityModel model)
			throws BlockingException;

	protected abstract TransitivityProcessingEvent getCompletionEvent();

	protected abstract void notifyProcessingCompleted(OabaJobMessage data);

}
