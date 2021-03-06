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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_BLOCKING_FIELD_COUNT;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_RECORD_ID_TYPE;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
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
public class StartOabaMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger.getLogger(StartOabaMDB.class
			.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ StartOabaMDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Override
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage data = null;
		BatchJob batchJob = null;

		getLogger().info("StartOabaMDB In onMessage");

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				batchJob = getJobController().findBatchJob(jobId);

				OabaParameters oabaParams =
					getParametersController().findOabaParametersByBatchJobId(
							jobId);
				OabaSettings oabaSettings =
					getSettingsController().findOabaSettingsByJobId(jobId);
				ProcessingEventLog processingEntry =
					getProcessingController().getProcessingLog(batchJob);
				if (batchJob == null || oabaParams == null || oabaSettings == null) {
					String s =
						"Unable to find a job, parameters or settings for "
								+ jobId;
					getLogger().severe(s);
					throw new IllegalArgumentException(s);
				}
				final String modelConfigId = oabaParams.getModelConfigurationName();
				ImmutableProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					getLogger().severe(s);
					throw new IllegalArgumentException(s);
				}

				// update status to mark as start
				batchJob.markAsStarted();

				getLogger().info("Job id: " + jobId);
				getLogger().info(
						"Model configuration: "
								+ oabaParams.getModelConfigurationName());
				getLogger().info(
						"Differ threshold: " + oabaParams.getLowThreshold());
				getLogger().info(
						"Match threshold: " + oabaParams.getHighThreshold());
				getLogger().info(
						"Staging record source id: " + oabaParams.getQueryRsId());
				getLogger().info(
						"Staging record source type: "
								+ oabaParams.getQueryRsType());
				getLogger()
						.info("Master record source id: "
								+ oabaParams.getReferenceRsId());
				getLogger().info(
						"Master record source type: "
								+ oabaParams.getReferenceRsType());
				getLogger()
						.info("Linkage type: " + oabaParams.getOabaLinkageType());

				// check to see if there are a lot of records in stage.
				// if not use single record matching instead of batch.
				getLogger()
						.info("Checking whether to use single- or batched-record blocking...");
				getLogger().info(
						"OabaSettings maxSingle: "
								+ oabaSettings.getMaxSingle());

				ISerializableRecordSource staging = null;
				ISerializableRecordSource master = null;
				try {
					staging = getRecordSourceController().getStageRs(oabaParams);
					master = getRecordSourceController().getMasterRs(oabaParams);
				} catch (Exception e) {
					throw new BlockingException(e.toString());
				}
				assert staging != null;

				if (!isMoreThanThreshold(staging, model,
						oabaSettings.getMaxSingle())) {
					getLogger().info("Using single record matching");
					sendToSingleRecordMatching(data);

				} else {
					getLogger().info("Using batch record matching");

					MutableRecordIdTranslator<?> translator =
						getRecordIdController()
								.createMutableRecordIdTranslator(batchJob);

					// create rec_id, val_id files
					String blockingConfiguration =
						oabaParams.getBlockingConfiguration();
					String queryConfiguration =
						this.getParametersController()
								.getQueryDatabaseConfiguration(oabaParams);
					String referenceConfiguration =
							this.getParametersController()
									.getReferenceDatabaseConfiguration(oabaParams);
					RecValSinkSourceFactory recvalFactory =
						OabaFileUtils.getRecValFactory(batchJob);
					RecValService3 rvService =
						new RecValService3(staging, master, model,
								blockingConfiguration, queryConfiguration,
								referenceConfiguration, recvalFactory,
								getRecordIdController(), translator,
								processingEntry, batchJob);
					rvService.runService();
					getLogger().info(
							"Done creating rec_id, val_id files: "
									+ rvService.getTimeElapsed());

					ImmutableRecordIdTranslator<?> immutableTranslator =
						getRecordIdController().toImmutableTranslator(
								translator);
					final RECORD_ID_TYPE recordIdType =
						immutableTranslator.getRecordIdType();
					getPropertyController().setJobProperty(batchJob,
							PN_RECORD_ID_TYPE, recordIdType.name());

					final int numBlockFields = rvService.getNumBlockingFields();
					getPropertyController().setJobProperty(batchJob,
							PN_BLOCKING_FIELD_COUNT,
							String.valueOf(numBlockFields));

					// create the validator after rvService
					// Validator validator = new Validator (true, translator);
					ValidatorBase validator =
						new ValidatorBase(true, immutableTranslator);
					// FIXME move this parameter to a persistent operational
					// object
					data.validator = validator;

					updateOabaProcessingStatus(batchJob,
							OabaProcessingEvent.DONE_REC_VAL, new Date(), null);
					sendToBlocking(data);
				}

			} else {
				getLogger().warning(
						"wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			String msg0 = throwableToString(e);
			log.severe(msg0);
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	protected String throwableToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println(throwable.toString());
		throwable.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	/**
	 * This method checks to see if the number of records in the
	 * RECORD_SOURCE_ROLE is greater than the threshold.
	 *
	 * @param rs
	 *            - RECORD_SOURCE_ROLE
	 * @param accessProvider
	 *            - Probability Model of this RECORD_SOURCE_ROLE
	 * @param threshold
	 *            - The number of records threshold
	 * @return boolean - true if the RECORD_SOURCE_ROLE contains more than the
	 *         threshold
	 * @throws OABABlockingException
	 */
	private boolean isMoreThanThreshold(RecordSource rs,
			ImmutableProbabilityModel model, int threshold)
			throws BlockingException {

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
		getLogger().info(
				"Checking if the number of records is more than the maxSingle threshold: "
						+ threshold);
		if (threshold <= 0) {
			getLogger().info("The threshold shortcuts further checking");
			retVal = true;
		} else {
			getLogger().info("Record source: " + rs);
			getLogger().info("Model: " + model);
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
					getLogger().info("Number of records: " + count + "+");
					retVal = true;
				} else {
					getLogger().info("Number of records: " + count);
				}
				rs.close();
			} catch (IOException ex) {
				throw new BlockingException(ex.toString(), ex);
			}
		}
		String msg =
			"The number of records " + (retVal ? "exceeds" : "does not exceed")
					+ " the maxSingle threshold: " + threshold;
		getLogger().info(msg);

		return retVal;
	}

	private void sendToBlocking(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(), blockQueue,
				getLogger());
	}

	private void sendToSingleRecordMatching(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(), singleMatchQueue,
				getLogger());
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJmsTrace() {
		return jmsTrace;
	}

	@Override
	protected void processOabaMessage(OabaJobMessage data, BatchJob batchJob,
			OabaParameters oabaParams, OabaSettings oabaSettings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {
		// Does nothing in this class. Instead, onMessage is overridden
		// and this callback is bypassed.
	}

	@Override
	protected OabaProcessingEvent getCompletionEvent() {
		return OabaProcessingEvent.DONE_REC_VAL;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		// Does nothing in this class. Instead, notifications are
		// sent via sendToSingleRecordMatching(..) and sendToBlocking(..)
	}

}
