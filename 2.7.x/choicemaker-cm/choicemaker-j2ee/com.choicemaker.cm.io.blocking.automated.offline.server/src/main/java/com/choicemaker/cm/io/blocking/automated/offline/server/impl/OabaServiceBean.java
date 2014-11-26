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
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.FinderException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
//import javax.naming.InitialContext;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;

/**
 * @author pcheung
 *
 */
@Stateless
@SuppressWarnings("rawtypes")
public class OabaServiceBean implements OabaService {

	private static final long serialVersionUID = 271L;

	private static final String SOURCE_CLASS = OabaServiceBean.class
			.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(OabaServiceBean.class.getName());

	@EJB
	private OabaJobControllerBean jobController;
	
	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OabaProcessingControllerBean processingController;

	@Resource(name = "jms/startQueue",
			lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue queue;

	@Inject
	private JMSContext context;

	protected static void logStartParameters(String externalID,
			OabaParameters bp, OabaSettings oaba, ServerConfiguration sc) {

		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);

		pw.println("External id: " + externalID);
		if (bp == null) {
			pw.println("null batch parameters");
		} else {
			if (bp.getMasterRs() == null) {
				pw.println("Deduping a single record source");
			} else {
				pw.println("Matching a staging source against a master source");
			}
			pw.println("Staging record source: " + bp.getStageRs());
			pw.println("Master record source: " + bp.getMasterRs());
			pw.println("DIFFER threshold: " + bp.getLowThreshold());
			pw.println("MATCH threshold: " + bp.getHighThreshold());
			pw.println("Model configuration id: "
					+ bp.getModelConfigurationName());
		}

		if (oaba == null) {
			pw.println("null OABA settings");
		} else {
			// FIXME better logging
			pw.println("Threshold for batched-record blocking: "
					+ oaba.getMaxSingle());
			pw.println(oaba.toString());
		}

		if (sc == null) {
			pw.println("null server configuration");
		} else {
			// FIXME better logging
			pw.println(sc.toString());
		}

		String msg = sw.toString();
		logger.info(msg);
	}

	/**
	 * Validates parameters to
	 * {@link #startOABAStage(String, SerializableRecordSource, float, float, String, int, boolean)
	 * startOABAStage}
	 * 
	 * @throws IllegalArgumentException
	 *             if any parameter is invalid
	 */
	protected static void validateStartParameters(String externalID,
			OabaParameters bp, OabaSettings oaba, ServerConfiguration sc) {

		// Create an empty list of invalid parameters
		List<String> validityErrors = new LinkedList<>();
		assert validityErrors.isEmpty();

		if (bp == null) {
			validityErrors.add("null batch parameters");
		} else {
			if (bp.getStageRs() == null) {
				validityErrors.add("null staging record source");
			}
			if (bp.getLowThreshold() < 0f || bp.getLowThreshold() > 1.0f) {
				validityErrors.add("invalid DIFFER threshold: "
						+ bp.getLowThreshold());
			}
			if (bp.getHighThreshold() < bp.getLowThreshold()) {
				validityErrors.add("MATCH threshold (" + bp.getHighThreshold()
						+ ") less than DIFFER threshold ("
						+ bp.getLowThreshold() + ")");
			}
			if (bp.getHighThreshold() > 1.0f) {
				validityErrors.add("invalid MATCH threshold: "
						+ bp.getHighThreshold());
			}
			if (bp.getModelConfigurationName() == null
					|| bp.getModelConfigurationName().trim().isEmpty()) {
				validityErrors.add("null or blank model configuration name");
			}

		}

		if (oaba == null) {
			validityErrors.add("null OABA settings");
		} else {
			// FIXME better checking
			if (oaba.getMaxSingle() < 0) {
				validityErrors
						.add("invalid threshold for single record matching: "
								+ oaba.getMaxSingle());
			}
		}

		if (sc == null) {
			validityErrors.add("null server configuration");
		} else {
			// FIXME better checking
		}

		if (!validityErrors.isEmpty()) {
			String msg =
				"Invalid parameters to OabaService.startOABA: "
						+ validityErrors.toString();
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	@Override
	@Deprecated
	public long startDeduplication(String externalID,
			SerializableRecordSource staging, float lowThreshold,
			float highThreshold, String modelConfigurationId)
			throws ServerConfigurationException {
		return startLinkage(externalID, staging, null, lowThreshold,
				highThreshold, modelConfigurationId);
	}

	@Override
	public long startDeduplication(String externalID,
			OabaParameters batchParams, OabaSettings oabaSettings,
			ServerConfiguration serverConfiguration)
			throws ServerConfigurationException {
		if (batchParams == null) {
			throw new IllegalArgumentException("null batch parameters");
		}

		OabaParameters submittedParams;
		if (batchParams.getMasterRs() == null) {
			submittedParams = batchParams;
		} else {
			final SerializableRecordSource NULL_MASTER_RS = null;
			submittedParams =
				new OabaParametersEntity(
						batchParams.getModelConfigurationName(),
						batchParams.getLowThreshold(),
						batchParams.getHighThreshold(),
						batchParams.getStageRs(), NULL_MASTER_RS);
		}
		assert submittedParams.getMasterRs() == null;

		return startLinkage(externalID, submittedParams, oabaSettings,
				serverConfiguration);
	}

	@Override
	@Deprecated
	public long startLinkage(String externalID,
			SerializableRecordSource staging, SerializableRecordSource master,
			float lowThreshold, float highThreshold, String modelConfigurationId)
			throws ServerConfigurationException {

		final String METHOD = "startOABA";
		logger.entering(SOURCE_CLASS, METHOD);

		// Create the job parameters for this job
		final OabaParameters batchParams =
			new OabaParametersEntity(modelConfigurationId, lowThreshold,
					highThreshold, staging, master);

		// Get the default OABA settings, ignoring the maxSingle value.
		// If no default settings exist, create them using the maxSingle value.
		ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelConfigurationId);
		OabaSettings oabaSettings =
			settingsController.findDefaultOabaSettings(model);
		if (oabaSettings == null) {
			// Creates generic settings and saves them
			oabaSettings = new OabaSettingsEntity();
			oabaSettings = settingsController.save(oabaSettings);
		}

		// Get the default server configuration for this host (or guess at it)
		final String hostName =
			ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration sc =
			serverController.getDefaultConfiguration(hostName, computeFallback);

		return startLinkage(externalID, batchParams, oabaSettings, sc);
	}

	@Override
	public long startLinkage(String externalID, OabaParameters batchParams,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration)
			throws ServerConfigurationException {

		final String METHOD = "startOABA";
		logger.entering(SOURCE_CLASS, METHOD);

		logStartParameters(externalID, batchParams, oabaSettings,
				serverConfiguration);
		validateStartParameters(externalID, batchParams, oabaSettings,
				serverConfiguration);

		// Create and persist the job and its associated objects
		BatchJob oabaJob =
			jobController.createPersistentBatchJob(externalID, batchParams,
					oabaSettings, serverConfiguration);
		final long retVal = oabaJob.getId();
		assert OabaJobEntity.INVALID_ID != retVal;

		// Mark the job as queued and start processing by the StartOABA EJB
		oabaJob.markAsQueued();
		sendToStartOABA(retVal);

		logger.exiting(SOURCE_CLASS, METHOD, retVal);
		return retVal;
	}

	public int abortJob(long jobID) {
		return abortBatch(jobID, true);
	}

	public int suspendJob(long jobID) {
		return abortBatch(jobID, false);
	}

	/**
	 * This method aborts a job. If cleanStatus is true, then the aborted job
	 * will not be recoverable.
	 *
	 */
	private int abortBatch(long jobID, boolean cleanStatus) {
		logger.info("aborting job " + jobID + " " + cleanStatus);
		OabaJob oabaJob = jobController.find(jobID);
		if (oabaJob == null) {
			String msg = "No OABA job found: " + jobID;
			logger.warning(msg);
		} else {
			oabaJob.markAsAbortRequested();
			// FIXME HACK!
			// set status as done, so it won't continue during the next run
			if (cleanStatus) {
				oabaJob.setDescription(BatchJob.STATUS_CLEAR);
			}
			// END FIXME
		}
		return 0;
	}

	public BatchJobStatus getStatus(long jobID) {
		OabaJob oabaJob = jobController.find(jobID);
		BatchJobStatus status = new BatchJobStatus(oabaJob);
		return status;
	}

	public String checkStatus(long jobID) {
		BatchJob oabaJob = jobController.find(jobID);
		return oabaJob.getStatus();
	}

	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {
		OabaJob job = jobController.find(jobID);
		return OabaFileUtils.removeTempDir(job);
	}

	/**
	 * This method tries to resume a stop job.
	 *
	 * @param jobID
	 *            - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 */
	public int resumeJob(long jobID) {
		int ret = 1;
		OabaJob job = jobController.find(jobID);
		if (!job.getStarted().equals(BatchJob.STATUS_COMPLETED)
				&& !job.getDescription().equals(BatchJob.STATUS_CLEAR)) {

			job.markAsReStarted();
			sendToStartOABA(jobID);

		} else {
			logger.warning("Could not resume job " + jobID);
			ret = -1;
		}

		return ret;
	}

	/**
	 * This method returns the MatchCandidate List Source for the job ID.
	 *
	 * @param jobID
	 * @return MatchListSource - return a source from which to read MatchList
	 *         objects.
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 * @throws JMSException
	 * @throws FinderException
	 */
	public MatchListSource getMatchList(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		MatchListSource mls = null;

		// check to make sure the job is completed
		OabaJob oabaJob = jobController.find(jobID);
		if (!oabaJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			String fileName = oabaJob.getDescription();
			MatchRecordSource mrs =
				new MatchRecordSource(fileName, Constants.STRING);
			mls = new MatchListSource(mrs);
		}

		return mls;
	}

	public IMatchRecord2Source getMatchRecordSource(long jobID)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException {

		MatchRecord2Source mrs = null;

		// check to make sure the job is completed
		OabaJob oabaJob = jobController.find(jobID);
		if (!oabaJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			String fileName = oabaJob.getDescription();
			mrs = new MatchRecord2Source(fileName, Constants.STRING);
		}

		return mrs;
	}

	/**
	 * This method sends a message to the StartOABA message bean.
	 *
	 * @param jobID
	 *            the id of the job to be processed
	 */
	private void sendToStartOABA(long jobID) {
		OabaJobMessage data = new OabaJobMessage(jobID);
		ObjectMessage message = context.createObjectMessage(data);
		JMSProducer sender = context.createProducer();
		logger.finest(queueInfo("Sending: ", queue, data));
		sender.send(queue, message);
		logger.finest(queueInfo("Sent: ", queue, data));
	}

	private static String queueInfo(String tag, Queue q, OabaJobMessage d) {
		String queueName;
		try {
			queueName = q.getQueueName();
		} catch (JMSException x) {
			queueName = "unknown";
		}
		StringBuilder sb =
			new StringBuilder(tag).append("queue: '").append(queueName)
					.append("', data: '").append(d).append("'");
		return sb.toString();
	}

}