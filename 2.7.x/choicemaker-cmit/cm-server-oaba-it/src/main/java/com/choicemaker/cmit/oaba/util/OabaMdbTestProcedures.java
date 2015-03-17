package com.choicemaker.cmit.oaba.util;

import static com.choicemaker.cmit.utils.JmsUtils.LONG_TIMEOUT_MILLIS;
import static com.choicemaker.cmit.utils.JmsUtils.SHORT_TIMEOUT_MILLIS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchProcessingNotification;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cmit.oaba.AbstractOabaMdbTest;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.BatchProcessingPhase;
import com.choicemaker.cmit.utils.OabaTestUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;

/**
 * Standardized procedures for testing intermediate stages of OABA processing
 * (which are implemented as message-driven beans).
 * 
 * @author rphall
 */
public class OabaMdbTestProcedures {

	private static final Logger logger = Logger
			.getLogger(OabaMdbTestProcedures.class.getName());

	public static boolean isValidConfigurationClass(Class<?> c) {
		boolean retVal = false;
		if (c != null && WellKnownTestConfiguration.class.isAssignableFrom(c)) {
			retVal = true;
		}
		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> T createTestConfiguration(
			Class<T> c, OabaLinkageType task, CMPluginRegistry registry) {

		if (!isValidConfigurationClass(c)) {
			String msg = "invalid configuration class: " + c;
			throw new IllegalArgumentException(msg);
		}
		if (registry == null) {
			throw new IllegalArgumentException("null registry");
		}

		T retVal = null;
		try {
			Class<T> cWKTC = (Class<T>) c;
			retVal = cWKTC.newInstance();
			retVal.initialize(task, registry);
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> void testLinkageProcessing(
			AbstractOabaMdbTest<T> test) throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		String TEST = "testStartOABALinkage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		testOabaProcessing(OabaLinkageType.STAGING_TO_MASTER_LINKAGE, TEST,
				test, externalID);

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	public static <T extends WellKnownTestConfiguration> void testDeduplicationProcessing(
			AbstractOabaMdbTest<T> test) throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		String TEST = "testStartOABAStage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		testOabaProcessing(OabaLinkageType.STAGING_DEDUPLICATION, TEST, test,
				externalID);

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	protected static <T extends WellKnownTestConfiguration> void testOabaProcessing(
			final OabaLinkageType linkage, final String tag,
			final AbstractOabaMdbTest<T> test, final String externalId) {

		// Preconditions
		if (linkage == null || tag == null || test == null
				|| externalId == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String LOG_SOURCE = test.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = test.getTestEntityCounts();
		final Queue listeningQueue = test.getResultQueue();
		final JMSConsumer statusListener = test.getOabaStatusConsumer();

		// Compute the OABA processing context
		final BatchProcessingPhase oabaPhase = test.getProcessingPhase();
		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		final boolean isUpdateExpected = oabaPhase.isUpdateExpected;

		// Validate the OABA processing context
		validateDestinations(oabaPhase, listeningQueue);

		// Start the OABA processing job
		BatchJob batchJob =
			OabaTestUtils.startOabaJob(linkage, tag, test, externalId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalId != null
				&& externalId.equals(batchJob.getExternalId()));

		// Find the OABA parameters associated with the job
		final long jobId = batchJob.getId();
		final OabaParametersController paramsController =
			test.getOabaParamsController();
		OabaParameters params =
			paramsController.findOabaParametersByJobId(jobId);
		te.add(params);

		// Check the job results
		final JMSContext jmsContext = test.getJmsContext();
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
						listeningQueue, LONG_TIMEOUT_MILLIS);
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
			logger.info("Checking oabaStatusTopic");
			BatchProcessingNotification oabaNotification = null;
			if (oabaPhase == BatchProcessingPhase.INTERMEDIATE
					|| oabaPhase == BatchProcessingPhase.INITIAL) {
				oabaNotification =
					JmsUtils.receiveLatestBatchProcessingNotification(batchJob,
							LOG_SOURCE, statusListener, SHORT_TIMEOUT_MILLIS);
			} else if (oabaPhase == BatchProcessingPhase.FINAL) {
				oabaNotification =
					JmsUtils.receiveFinalBatchProcessingNotification(batchJob,
							LOG_SOURCE, statusListener, LONG_TIMEOUT_MILLIS);
			} else {
				throw new Error("unexpected phase: " + oabaPhase);
			}
			assertTrue(oabaNotification != null);
			assertTrue(oabaNotification.getJobId() == jobId);
			final float expectPercentDone = test.getResultPercentComplete();
			assertTrue(oabaNotification.getJobPercentComplete() == expectPercentDone);
		}

		// Find the entry in the processing history updated by the OABA
		final ProcessingController processingController =
			test.getOabaProcessingController();
		ProcessingEventLog processingEntry =
			processingController.getProcessingLog(batchJob);

		// Validate that processing entry is correct for this stage of the OABA
		assertTrue(processingEntry != null);
		final int expectedEventId = test.getResultEventId();
		assertTrue(processingEntry.getCurrentProcessingEventId() == expectedEventId);

		// Check that the working directory contains what it should
		assertTrue(test.isWorkingDirectoryCorrectAfterProcessing(batchJob));

		// Check the number of test entities that were created
		test.checkCounts();

		logger.exiting(LOG_SOURCE, tag);
	}

	public static void validateDestinations(BatchProcessingPhase oabaPhase,
			Queue listeningQueue) {
		if (oabaPhase == null) {
			throw new IllegalArgumentException("null OABA processing phase");
		}
		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		assertTrue(isUpdateExpected);
		if (isIntermediateExpected /* && isUpdateExpected */) {
			if (listeningQueue == null) {
				throw new IllegalArgumentException(
						"intermediate-result queue is null");
			}
		} else {
			assertTrue(!isIntermediateExpected /* && isUpdateExpected */);
			if (listeningQueue != null) {
				String msg =
					"Ignoring intermediate-result queue -- "
							+ "final results expected from status topic";
				logger.warning(msg);
			}
		}
	}

	private OabaMdbTestProcedures() {
	}

}
