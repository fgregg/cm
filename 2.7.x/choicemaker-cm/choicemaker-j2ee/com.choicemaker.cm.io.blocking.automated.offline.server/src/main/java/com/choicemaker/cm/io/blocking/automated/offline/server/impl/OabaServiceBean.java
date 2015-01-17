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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CLEAR_RESOURCES;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_OABA_CACHED_RESULTS_FILE;

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
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchListSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.impl.MatchRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
//import javax.naming.InitialContext;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;

/**
 * @author pcheung
 *
 */
@Stateless
@SuppressWarnings("rawtypes")
public class OabaServiceBean implements OabaService {

	// private static final long serialVersionUID = 271L;

	private static final String SOURCE_CLASS = OabaServiceBean.class
			.getSimpleName();

	private static final Logger logger = Logger.getLogger(OabaServiceBean.class
			.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OperationalPropertyController propController;

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
		pw.println(OabaParametersEntity.dump(bp));
		pw.println(OabaSettingsEntity.dump(oaba));
		pw.println(ServerConfigurationEntity.dump(sc));
		String msg = sw.toString();
		logger.info(msg);
	}

	/**
	 * Validates parameters to
	 * {@link #startOABAStage(String, PersistableRecordSource, float, float, String, int, boolean)
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
			OabaLinkageType type = bp.getOabaLinkageType();
			if (type == null) {
				validityErrors.add("null task type");
			}
			if (bp.getStageRsType() == null) {
				validityErrors.add("null staging source type");
			}
			if (OabaLinkageType.STAGING_DEDUPLICATION == type) {
				if (bp.getMasterRsId() != null || bp.getMasterRsType() != null) {
					validityErrors
							.add("non-null master source parameter for a de-duplication task");
				}
			} else {
				if (bp.getMasterRsId() == null || bp.getMasterRsType() == null) {
					validityErrors
							.add("null master source parameter for a linkage task");
				}
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
				validityErrors.add("null or blank modelId configuration name");
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
	public long startDeduplication(String externalID, OabaParameters bp,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration)
			throws ServerConfigurationException {
		if (bp == null) {
			throw new IllegalArgumentException("null batch parameters");
		}

		OabaParameters submittedParams;
		final OabaLinkageType task = bp.getOabaLinkageType();
		if (bp.getMasterRsId() == null && bp.getMasterRsType() == null
				&& OabaLinkageType.STAGING_DEDUPLICATION == task) {
			submittedParams = bp;
		} else {
			submittedParams =
				new OabaParametersEntity(bp.getModelConfigurationName(),
						bp.getLowThreshold(), bp.getHighThreshold(),
						bp.getStageRsId(), bp.getStageRsType(), null, null,
						OabaLinkageType.STAGING_DEDUPLICATION);
		}

		return startLinkage(externalID, submittedParams, oabaSettings,
				serverConfiguration);
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
			jobController.createPersistentOabaJob(externalID, batchParams,
					oabaSettings, serverConfiguration);
		final long retVal = oabaJob.getId();
		assert BatchJob.INVALID_ID != retVal;

		// Mark the job as queued and start processing by the StartOabaMDB EJB
		oabaJob.markAsQueued();
		sendToStartOABA(retVal);

		logger.exiting(SOURCE_CLASS, METHOD, retVal);
		return retVal;
	}

	@Override
	public int abortJob(long jobID) {
		return abortBatch(jobID, true);
	}

	@Override
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
		OabaJob oabaJob = jobController.findOabaJob(jobID);
		if (oabaJob == null) {
			String msg = "No OABA job found: " + jobID;
			logger.warning(msg);
		} else {
			oabaJob.markAsAbortRequested();
			propController.setJobProperty(oabaJob, PN_CLEAR_RESOURCES,
					String.valueOf(cleanStatus));
		}
		return 0;
	}

	@Override
	public OabaJob getOabaJob(long jobId) {
		OabaJob oabaJob = jobController.findOabaJob(jobId);
		return oabaJob;
	}

	@Override
	public String checkStatus(long jobID) {
		BatchJob oabaJob = jobController.findOabaJob(jobID);
		return oabaJob.getStatus().name();
	}

	@Override
	public boolean removeDir(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {
		OabaJob job = jobController.findOabaJob(jobID);
		return OabaFileUtils.removeTempDir(job);
	}

	/**
	 * This method tries to resume a stop job.
	 *
	 * @param jobID
	 *            - job id of the job you want to resume
	 * @return int = 1 if OK, or -1 if failed
	 */
	@Override
	public int resumeJob(long jobID) {

		OabaJob job = jobController.findOabaJob(jobID);

		final String _clearResources =
			propController.getJobProperty(job, PN_CLEAR_RESOURCES);
		boolean clearResources = Boolean.valueOf(_clearResources);

		boolean isCompleted = job.getStarted().equals(BatchJobStatus.COMPLETED);

		int ret;
		if (!isCompleted && !clearResources) {
			logger.info("Resuming job " + jobID);
			job.markAsReStarted();
			sendToStartOABA(jobID);
			ret = 1;

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
	@Override
	public MatchListSource getMatchList(long jobID) throws RemoteException,
			CreateException, NamingException, JMSException, FinderException {

		MatchListSource mls = null;

		// check to make sure the job is completed
		OabaJob oabaJob = jobController.findOabaJob(jobID);
		if (!oabaJob.getStatus().equals(BatchJobStatus.COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			final String cachedResultsFileName =
				propController.getJobProperty(oabaJob,
						PN_OABA_CACHED_RESULTS_FILE);
			logger.info("Cached OABA results file: " + cachedResultsFileName);

			MatchRecordSource mrs =
				new MatchRecordSource(cachedResultsFileName,
						EXTERNAL_DATA_FORMAT.STRING);
			mls = new MatchListSource(mrs);
		}

		return mls;
	}

	@Override
	public IMatchRecord2Source getMatchRecordSource(long jobID)
			throws RemoteException, CreateException, NamingException,
			JMSException, FinderException {

		MatchRecord2Source mrs = null;

		// check to make sure the job is completed
		OabaJob oabaJob = jobController.findOabaJob(jobID);
		if (!oabaJob.getStatus().equals(BatchJobStatus.COMPLETED)) {
			throw new IllegalStateException("The job has not completed.");
		} else {
			final String cachedResultsFileName =
				propController.getJobProperty(oabaJob,
						PN_OABA_CACHED_RESULTS_FILE);
			logger.info("Cached OABA results file: " + cachedResultsFileName);
			mrs =
				new MatchRecord2Source(cachedResultsFileName,
						EXTERNAL_DATA_FORMAT.STRING);
		}

		return mrs;
	}

	/**
	 * This method sends a message to the StartOabaMDB message bean.
	 *
	 * @param jobID
	 *            the id of the job to be processed
	 */
	private void sendToStartOABA(long jobID) {
		OabaJobMessage data = new OabaJobMessage(jobID);
		MessageBeanUtils.sendStartData(data, context, queue, logger);
	}

}
