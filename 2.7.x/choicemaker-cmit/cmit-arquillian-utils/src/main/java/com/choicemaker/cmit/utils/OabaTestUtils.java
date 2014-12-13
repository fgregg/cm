package com.choicemaker.cmit.utils;

import static com.choicemaker.cm.batch.BatchJob.INVALID_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_INIT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_INIT;
import static com.choicemaker.cmit.utils.JmsUtils.VERY_LONG_TIMEOUT_MILLIS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;

public class OabaTestUtils {

	private static final Logger logger = Logger.getLogger(OabaTestUtils.class
			.getName());

	/**
	 * @deprecated use <code>testOabaProcessing</code> instead
	 */
	public static void testInitialOabaProcessing(final String LOG_SOURCE,
			final String tag, final String externalId, final OabaParameters bp,
			final OabaSettings oabaSettings,
			final ServerConfiguration serverConfiguration,
			final OabaService batchQuery,
			final OabaJobControllerBean jobController,
			final OabaParametersControllerBean paramsController,
			final OabaProcessingControllerBean processingController,
			final JMSContext jmsContext, final Queue listeningQueue,
			final Queue updateQueue, final EntityManager em,
			final UserTransaction utx) {
		logger.entering(LOG_SOURCE, tag);
		final int expectedEventId = EVT_INIT;
		final int expectPercentDone = PCT_INIT;
		testOabaProcessing(LOG_SOURCE, tag, externalId, bp, oabaSettings,
				serverConfiguration, batchQuery, jobController,
				paramsController, processingController, jmsContext,
				listeningQueue, updateQueue, em, utx, expectedEventId,
				expectPercentDone, OabaProcessingPhase.INITIAL);
	}

	/**
	 * @deprecated use <code>testOabaProcessing</code> instead
	 */
	public static void testIntermediateOabaProcessing(final String LOG_SOURCE,
			final String tag, final String externalId, final OabaParameters bp,
			final OabaSettings oabaSettings,
			final ServerConfiguration serverConfiguration,
			final OabaService batchQuery,
			final OabaJobControllerBean jobController,
			final OabaParametersControllerBean paramsController,
			final OabaProcessingControllerBean processingController,
			final JMSContext jmsContext, final Queue listeningQueue,
			final Queue updateQueue, final EntityManager em,
			final UserTransaction utx, final int expectedEventId,
			final int expectPercentDone) {
		logger.entering(LOG_SOURCE, tag);
		testOabaProcessing(LOG_SOURCE, tag, externalId, bp, oabaSettings,
				serverConfiguration, batchQuery, jobController,
				paramsController, processingController, jmsContext,
				listeningQueue, updateQueue, em, utx, expectedEventId,
				expectPercentDone, OabaProcessingPhase.INTERMEDIATE);
	}

	/**
	 * @deprecated use <code>testOabaProcessing</code> instead
	 */
	public static void testFinalOabaProcessing(final String LOG_SOURCE,
			final String tag, final String externalId, final OabaParameters bp,
			final OabaSettings oabaSettings,
			final ServerConfiguration serverConfiguration,
			final OabaService batchQuery,
			final OabaJobControllerBean jobController,
			final OabaParametersControllerBean paramsController,
			final OabaProcessingControllerBean processingController,
			final JMSContext jmsContext, final Queue updateQueue,
			final EntityManager em, final UserTransaction utx) {
		logger.entering(LOG_SOURCE, tag);
		final int expectedEventId = EVT_DONE_OABA;
		final int expectPercentDone = PCT_DONE_OABA;
		testOabaProcessing(LOG_SOURCE, tag, externalId, bp, oabaSettings,
				serverConfiguration, batchQuery, jobController,
				paramsController, processingController, jmsContext, null,
				updateQueue, em, utx, expectedEventId, expectPercentDone,
				OabaProcessingPhase.FINAL);
	}

	public static void testOabaProcessing(final String LOG_SOURCE,
			final String tag, final String externalId, final OabaParameters bp,
			final OabaSettings oabaSettings,
			final ServerConfiguration serverConfiguration,
			final OabaService batchQuery,
			final OabaJobControllerBean jobController,
			final OabaParametersControllerBean paramsController,
			final OabaProcessingControllerBean processingController,
			final JMSContext jmsContext, final Queue listeningQueue,
			final Queue updateQueue, final EntityManager em,
			final UserTransaction utx, final int expectedEventId,
			final int expectPercentDone, final OabaProcessingPhase oabaPhase) {
		logger.entering(LOG_SOURCE, tag);

		// Preconditions
		if (externalId == null || bp == null || LOG_SOURCE == null
				|| tag == null || oabaSettings == null
				|| serverConfiguration == null || batchQuery == null
				|| jobController == null || paramsController == null
				|| processingController == null || jmsContext == null
				|| updateQueue == null || em == null || utx == null
				|| oabaPhase == null) {
			throw new IllegalArgumentException("null argument");
		}
		validateQueues(oabaPhase, listeningQueue, updateQueue);

		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		final boolean isDeduplication =
			OabaLinkageType.STAGING_DEDUPLICATION == bp.getOabaLinkageType();

		TestEntities te = new TestEntities();
		te.add(bp);

		long jobId = INVALID_ID;
		try {
			final OabaLinkageType linkage = bp.getOabaLinkageType();
			switch (linkage) {
			case STAGING_DEDUPLICATION:
				logger.info(tag
						+ ": invoking BatchQueryService.startDeduplication");
				jobId =
					batchQuery.startDeduplication(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag
						+ ": returned from BatchQueryService.startDeduplication");
				break;
			case STAGING_TO_MASTER_LINKAGE:
			case MASTER_TO_MASTER_LINKAGE:
				logger.info(tag + ": invoking BatchQueryService.startLinkage");
				jobId =
					batchQuery.startLinkage(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag
						+ ": returned from BatchQueryService.startLinkage");
				break;
			default:
				fail("Unexpected linkage type: " + linkage);
			}
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(INVALID_ID != jobId);
		OabaJob batchJob = jobController.find(jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalId != null
				&& externalId.equals(batchJob.getExternalId()));

		// Find the persistent OabaParameters object created by the call to
		// BatchQueryService.startLinkage...
		OabaParameters params = paramsController.findBatchParamsByJobId(jobId);
		te.add(params);

		// Validate that the job parameters are correct
		assertTrue(params != null);
		assertTrue(params.getLowThreshold() == bp.getLowThreshold());
		assertTrue(params.getHighThreshold() == bp.getHighThreshold());
		if (isDeduplication) {
			assertTrue(params.getMasterRsId() == null);
			assertTrue(params.getMasterRsType() == null);
		} else {
			assertTrue(params.getMasterRsId() != null
					&& params.getMasterRsId().equals(bp.getMasterRsId()));
			assertTrue(params.getMasterRsType() != null
					&& params.getMasterRsType().equals(bp.getMasterRsType()));
		}
		assertTrue(params.getStageRsId() == bp.getStageRsId());
		assertTrue(params.getStageRsType() != null
				&& params.getStageRsType().equals(bp.getStageRsType()));
		assertTrue(params.getModelConfigurationName() != null
				&& params.getModelConfigurationName().equals(
						bp.getModelConfigurationName()));

		if (isIntermediateExpected) {
			// Check that OABA processing completed and sent out a
			// message on the intermediate result queue
			assert listeningQueue != null;
			String listeningQueueName;
			try {
				listeningQueueName = listeningQueue.getQueueName();
			} catch (JMSException x) {
				logger.warning(x.toString());
				listeningQueueName = "listeningQueue";
			}
			logger.info("Checking " + listeningQueueName);
			OabaJobMessage startData =
				JmsUtils.receiveStartData(LOG_SOURCE, jmsContext,
						listeningQueue, JmsUtils.LONG_TIMEOUT_MILLIS);
			logger.info(JmsUtils.queueInfo("Received from: ", listeningQueue,
					startData));
			if (startData == null) {
				fail("did not receive data from " + listeningQueueName);
			}
			assertTrue(startData.jobID == jobId);
		}
		if (isUpdateExpected) {
			// Check that OABA processing sent out an expected status
			// on the update queue
			logger.info("Checking updateQueue");
			OabaUpdateMessage updateMessage = null;
			if (oabaPhase == OabaProcessingPhase.INTERMEDIATE) {
				updateMessage =
					JmsUtils.receiveLatestUpdateMessage(LOG_SOURCE, jmsContext,
							updateQueue, JmsUtils.SHORT_TIMEOUT_MILLIS);
			} else if (oabaPhase == OabaProcessingPhase.FINAL) {
				updateMessage =
					JmsUtils.receiveFinalUpdateMessage(LOG_SOURCE, jmsContext,
							updateQueue, VERY_LONG_TIMEOUT_MILLIS);
			} else {
				throw new Error("unexpected phase: " + oabaPhase);
			}
			assertTrue(updateMessage != null);
			assertTrue(updateMessage.getJobID() == jobId);
			assertTrue(updateMessage.getPercentComplete() == expectPercentDone);
		}

		// Find the entry in the processing history updated by the OABA
		OabaJobProcessing processingEntry =
			processingController.findProcessingLogByJobId(jobId);
		te.add(processingEntry);

		// Validate that processing entry is correct for this stage of the OABA
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == expectedEventId);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}

		logger.exiting(LOG_SOURCE, tag);
	}

	public static void validateQueues(OabaProcessingPhase oabaPhase,
			Queue listeningQueue, Queue updateQueue) {
		if (oabaPhase == null) {
			throw new IllegalArgumentException("null OABA processing phase");
		}
		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		if (isIntermediateExpected && !isUpdateExpected) {
			if (listeningQueue == null) {
				throw new IllegalArgumentException(
						"intermediate-result queue is null");
			}
			if (updateQueue != null) {
				String msg =
					"Ignoring update queue -- results expected only "
							+ "from intermediate-result queue";
				logger.warning(msg);
			}
		} else if (isIntermediateExpected && isUpdateExpected) {
			if (listeningQueue == null) {
				throw new IllegalArgumentException(
						"intermediate-result queue is null");
			}
			if (updateQueue == null) {
				throw new IllegalArgumentException("update queue is null");
			}
		} else if (!isIntermediateExpected && isUpdateExpected) {
			if (listeningQueue != null) {
				String msg =
					"Ignoring intermediate-result queue -- "
							+ "final results expected from update queue";
				logger.warning(msg);
			}
			if (updateQueue == null) {
				throw new IllegalArgumentException("update queue is null");
			}
		} else {
			String msg =
				"unexpected: !isIntermediateExpected && !isUpdateExpected";
			throw new Error(msg);
		}
	}

	private OabaTestUtils() {
	}

}
