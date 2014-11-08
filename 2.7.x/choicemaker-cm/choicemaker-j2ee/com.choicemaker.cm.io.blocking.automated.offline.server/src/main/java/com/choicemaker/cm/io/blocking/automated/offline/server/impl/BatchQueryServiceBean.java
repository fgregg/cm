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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
//import javax.naming.InitialContext;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryService;

/**
 * @author pcheung
 *
 */
@Stateless
@SuppressWarnings("rawtypes")
public class BatchQueryServiceBean implements BatchQueryService {

	private static final long serialVersionUID = 271L;
	private static final String SOURCE_CLASS = BatchQueryServiceBean.class
			.getSimpleName();
	private static final Logger logger = Logger
			.getLogger(BatchQueryServiceBean.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Resource
	protected SessionContext sc;

	@Resource(name = "jms/startQueue",
			lookup = "java:/choicemaker/urm/jms/startQueue")
	protected Queue queue;

	@Inject
	protected JMSContext context;

	private transient EJBConfiguration configuration = null;

	@PostConstruct
	public void init() {
		this.configuration = EJBConfiguration.getInstance();
	}

	/**
	 * Validates parameters to
	 * {@link #startOABAStage(String, SerializableRecordSource, float, float, String, int, boolean)
	 * startOABAStage}
	 * 
	 * @throws IllegalArgumentException
	 *             if any parameter is invalid
	 */
	public static void validateStartParameters(String externalID,
			SerializableRecordSource staging, float lowThreshold,
			float highThreshold, String modelConfigurationName, int maxSingle,
			boolean runTransitivity) {

		// Create an empty list of invalid parameters
		List<String> validityErrors = new LinkedList<>();
		assert validityErrors.isEmpty();

		if (staging == null) {
			validityErrors.add("null staging record source");
		}
		if (lowThreshold < 0f || lowThreshold > 1.0f) {
			validityErrors.add("invalid DIFFER threshold: " + lowThreshold);
		}
		if (highThreshold < lowThreshold) {
			validityErrors.add("MATCH threshold (" + highThreshold
					+ ") less than DIFFER threshold (" + lowThreshold + ")");
		}
		if (highThreshold > 1.0f) {
			validityErrors.add("invalid MATCH threshold: " + highThreshold);
		}
		if (modelConfigurationName == null
				|| modelConfigurationName.trim().isEmpty()) {
			validityErrors.add("null or blank model configuration name");
		}
		if (maxSingle < 0) {
			validityErrors.add("invalid threshold for single record matching: "
					+ maxSingle);
		}

		if (!validityErrors.isEmpty()) {
			String msg =
				"Invalid parameters to BatchQueryService.startOABA: "
						+ validityErrors.toString();
			logger.severe(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	protected void validateAndLogStartParameters(String externalID,
			SerializableRecordSource staging, SerializableRecordSource master,
			float lowThreshold, float highThreshold,
			String modelConfigurationId, int maxSingle, boolean runTransitivity) {

		validateStartParameters(externalID, staging, lowThreshold,
				highThreshold, modelConfigurationId, maxSingle, runTransitivity);

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if (master == null) {
			pw.println("Deduping a single record source");
		} else {
			pw.println("Matching a staging source against a master source");
		}
		pw.println("External id: " + externalID);
		pw.println("Staging record source: " + staging);
		pw.println("Master record source: " + master);
		pw.println("DIFFER threshold: " + lowThreshold);
		pw.println("MATCH threshold: " + highThreshold);
		pw.println("Model configuration id: " + modelConfigurationId);
		pw.println("Threshold for batched-record blocking: " + maxSingle);
		pw.println("Run transitivity analysis? "
				+ (runTransitivity ? "YES" : "NO"));
		String msg = sw.toString();
		logger.info(msg);
	}

	@Override
	public long startOABAStage(String externalID, SerializableRecordSource staging,
			float lowThreshold, float highThreshold,
			String modelConfigurationId, int maxSingle, boolean runTransitivity) {
		return startOABA(externalID, staging, null, lowThreshold,
				highThreshold, modelConfigurationId, maxSingle, runTransitivity);
	}

	@Override
	public long startOABA(String externalID, SerializableRecordSource staging,
			SerializableRecordSource master, float lowThreshold, float highThreshold,
			String modelConfigurationId, int maxSingle, boolean runTransitivity) {

		validateAndLogStartParameters(externalID, staging, master,
				lowThreshold, highThreshold, modelConfigurationId, maxSingle,
				runTransitivity);

		final String METHOD = "startOABA";
		logger.entering(SOURCE_CLASS, METHOD);

		// Save the job parameters
		final BatchParameters batchParams =
			new BatchParametersBean(modelConfigurationId, maxSingle,
					lowThreshold, highThreshold, staging, master,
					runTransitivity);
		em.persist(batchParams);

		// Create the batch job
		BatchJob batchJob =
			configuration.createBatchJob(em, batchParams, externalID);
		final long retVal = batchJob.getId();
		assert BatchJobBean.INVALID_BATCHJOB_ID != retVal;

		// Create a new processing entry
		OabaProcessing processing =
			configuration.createProcessingLog(em, retVal);

		// Log the job info
		logger.fine("BatchJob: " + batchJob.toString());
		logger.fine("BatchParameters: " + batchParams.toString());
		logger.fine("Processing entry: " + processing.toString());

		// Mark the job as queued and start processing by the StartOABA EJB
		batchJob.markAsQueued();
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
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);
		batchJob.markAsAbortRequested();

		// set status as done, so it won't continue during the next run
		if (cleanStatus) {
			batchJob.setDescription(BatchJob.STATUS_CLEAR);
		}

		return 0;
	}

	public BatchJobStatus getStatus(long jobID) {
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);
		BatchJobStatus status = new BatchJobStatus(batchJob);
		return status;
	}

	public String checkStatus(long jobID) {
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);
		return batchJob.getStatus();
	}

	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		BatchParameters batchParams =
			configuration.findBatchParamsByJobId(em, jobID);
		String modelConfigName = batchParams.getStageModel();
		OABAConfiguration o = new OABAConfiguration(modelConfigName, jobID);
		return o.removeTempDir();
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
		BatchJob job =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);

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
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);
		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			String fileName = batchJob.getDescription();
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
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, jobID);
		if (!batchJob.getStatus().equals(BatchJob.STATUS_COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			String fileName = batchJob.getDescription();
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
		StartData data = new StartData(jobID);
		ObjectMessage message = context.createObjectMessage(data);
		JMSProducer sender = context.createProducer();
		logger.finest(queueInfo("Sending: ", queue, data));
		sender.send(queue, message);
		logger.finest(queueInfo("Sent: ", queue, data));
	}

	private static String queueInfo(String tag, Queue q, StartData d) {
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
