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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.AbaStatisticsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SqlRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.LoggingUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;

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
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private SqlRecordSourceController sqlRSController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private OperationalPropertyController propController;

	@EJB
	private AbaStatisticsController statsController;

	@Inject
	private JMSContext jmsContext;

	protected JMSContext getJmsContext() {
		return jmsContext;
	}

	// -- Accessors

	protected final OabaJobController getJobController() {
		return jobController;
	}

	protected final OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	protected final OabaParametersController getParametersController() {
		return paramsController;
	}

	protected final ProcessingController getProcessingController() {
		return processingController;
	}

	protected final ServerConfigurationController getServerController() {
		return serverController;
	}

	protected final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	protected final SqlRecordSourceController getSqlRecordSourceController() {
		return sqlRSController;
	}

	protected final RecordIdController getRecordIdController() {
		return ridController;
	}

	protected final OperationalPropertyController getPropertyController() {
		return propController;
	}

	protected final AbaStatisticsController getAbaStatisticsController() {
		return statsController;
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
				batchJob = getJobController().findBatchJob(jobId);
				OabaParameters oabaParams =
					getParametersController().findOabaParametersByBatchJobId(
							jobId);
				OabaSettings oabaSettings =
					getSettingsController().findOabaSettingsByJobId(jobId);
				ProcessingEventLog processingLog =
					getProcessingController().getProcessingLog(batchJob);
				ServerConfiguration serverConfig =
					getServerController().findServerConfigurationByJobId(jobId);

				if (batchJob == null /* FIXME || dbParams == null */
						|| oabaParams == null || oabaSettings == null
						|| serverConfig == null) {
					String s0 = "Null configuration info for job " + jobId;
					String s =
						LoggingUtils.buildDiagnostic(s0, batchJob, oabaParams,
								oabaSettings, serverConfig);
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

				if (BatchJobStatus.ABORT_REQUESTED.equals(batchJob.getStatus())) {
					abortProcessing(batchJob, processingLog);
				} else {
					processOabaMessage(oabaMsg, batchJob, oabaParams,
							oabaSettings, processingLog, serverConfig, model);
					updateOabaProcessingStatus(batchJob, getCompletionEvent(),
							new Date(), null);
					notifyProcessingCompleted(oabaMsg);
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			String msg0 = throwableToString(e);
			getLogger().severe(msg0);
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		getJmsTrace()
				.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected String throwableToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(throwable.toString());
		throwable.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	protected void abortProcessing(BatchJob batchJob,
			ProcessingEventLog processingLog) {
		MessageBeanUtils.stopJob(batchJob, getPropertyController(),
				processingLog);
	}

	protected void updateOabaProcessingStatus(BatchJob job,
			BatchProcessingEvent event, Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

	// -- Abstract call-back methods

	protected abstract Logger getLogger();

	protected abstract Logger getJmsTrace();

	protected abstract void processOabaMessage(OabaJobMessage data,
			BatchJob batchJob, OabaParameters oabaParams,
			OabaSettings oabaSettings, ProcessingEventLog processingLog,
			ServerConfiguration serverConfig, ImmutableProbabilityModel model)
			throws BlockingException;

	protected abstract BatchProcessingEvent getCompletionEvent();

	protected abstract void notifyProcessingCompleted(OabaJobMessage data);

}
