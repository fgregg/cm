package com.choicemaker.cmit.trans.util;

import static com.choicemaker.cm.batch.impl.AbstractPersistentObject.NONPERSISTENT_ID;
import static com.choicemaker.cmit.utils.JmsUtils.LONG_TIMEOUT_MILLIS;
import static com.choicemaker.cmit.utils.JmsUtils.SHORT_TIMEOUT_MILLIS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.oaba.MatchDedupMdbProcessing;
import com.choicemaker.cmit.trans.AbstractTransitivityMdbTest;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.OabaTestUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;

public class TransitivityMdbTestProcedures {

	private static final Logger logger = Logger
			.getLogger(TransitivityMdbTestProcedures.class.getName());

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
	
	public static OabaJob runOabaJob(
			final OabaLinkageType task,
			final String tag,
			final String externalId,
			final String LOG_SOURCE,
			final TestEntityCounts te
			) {
		final MatchDedupMdbProcessing oabaTest = new MatchDedupMdbProcessing();

		final OabaJob oabaJob =
			OabaTestUtils.startOabaJob(task, tag, oabaTest, externalId);
		assertTrue(oabaJob != null);
		final long jobId = oabaJob.getId();
		te.add(oabaJob);

		logger.info("Checking oabaStatusTopic");
		final OabaProcessingPhase oabaPhase = oabaTest.getProcessingPhase();
		final JMSConsumer statusListener = oabaTest.getStatusConsumer();
		OabaNotification oabaNotification = null;
		if (oabaPhase == OabaProcessingPhase.FINAL) {
			oabaNotification =
				JmsUtils.receiveFinalOabaNotification(oabaJob, LOG_SOURCE,
						statusListener, LONG_TIMEOUT_MILLIS);
		} else {
			throw new Error("unexpected phase: " + oabaPhase);
		}
		assertTrue(oabaNotification != null);
		assertTrue(oabaNotification.getJobId() == jobId);
		final float expectPercentDone = oabaTest.getResultPercentComplete();
		assertTrue(oabaNotification.getJobPercentComplete() == expectPercentDone);

		return oabaJob;
	}

	@SuppressWarnings("unused")
	public static <T extends WellKnownTestConfiguration> void testTransitivityProcessing(
			final AbstractTransitivityMdbTest<T> ta,
			final OabaLinkageType task) {

		// Preconditions
		if (ta == null || task == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String tag = "testLinkageTransitivity";
		final String LOG_SOURCE = ta.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = ta.getTestEntityCounts();
		final String extId = EntityManagerUtils.createExternalId(tag);
		final MatchDedupMdbProcessing oabaProcessing = new MatchDedupMdbProcessing();

		// Run an OabaJob for subsequent transitivity analysis
		final OabaJob oabaJob =
				runOabaJob(task, tag, extId, LOG_SOURCE, te);
		assertTrue(oabaJob != null);
		final long oabaJobId = oabaJob.getId();
		assertTrue(te.contains(oabaJob));
		assertTrue(oabaJob.getStatus() == BatchJobStatus.COMPLETED);

		// Find the OABA parameters associated with the job
		final OabaParametersController oabaParamsController =
			ta.getOabaParamsController();
		final OabaParameters oabaParams =
				oabaParamsController.findOabaParametersByJobId(oabaJobId);
		te.add(oabaParams);

		// Set up parameters for transitivity analysis
		final WellKnownTestConfiguration c = ta.getTestConfiguration(task);
		final AnalysisResultFormat arf = c.getTransitivityResultFormat();
		final String gpn = c.getTransitivityGraphProperty();
		final TransitivityParametersEntity transParams =
			new TransitivityParametersEntity(oabaParams, arf, gpn);
		te.add((TransitivityParameters) transParams);
		
		// Configure the server for transitivity analysis
		final String hostName =
			ServerConfigurationControllerBean.computeHostName();
		logger.info("Computed host name: " + hostName);
		final DefaultServerConfiguration dsc =
			ta.getServerController().findDefaultServerConfiguration(hostName);
		ServerConfiguration serverConfiguration = null;
		if (dsc != null) {
			long id = dsc.getServerConfigurationId();
			logger.info("Default server configuration id: " + id);
			serverConfiguration =
				ta.getServerController().findServerConfiguration(id);
		}
		if (serverConfiguration == null) {
			logger.info("No default server configuration for: " + hostName);
			serverConfiguration =
				ta.getServerController().computeGenericConfiguration();
			try {
				serverConfiguration =
					ta.getServerController().save(serverConfiguration);
			} catch (ServerConfigurationException e) {
				fail("Unable to save server configuration: " + e.toString());
			}
			te.add(serverConfiguration);
		}
		logger.info(ServerConfigurationEntity.dump(serverConfiguration));
		assertTrue(serverConfiguration != null);

		// Perform transitivity analysis
		final TransitivityService transitivityService =
			ta.getTransitivityService();
		long jobId = NONPERSISTENT_ID;
		try {
			logger.info(tag + ": invoking BatchQueryService.startDeduplication");
			jobId =
				transitivityService.startTransitivity(extId, transParams,
						oabaJob, serverConfiguration);
			logger.info(tag
					+ ": returned from BatchQueryService.startDeduplication");
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(jobId != NONPERSISTENT_ID);

		// Find the transitivity job
		final TransitivityJobController transJobController = ta.getTransJobController();
		TransitivityJob transJob = transJobController.findTransitivityJob(jobId);
		assertTrue(transJob != null);
		te.add(transJob);
		assertTrue(extId != null && extId.equals(transJob.getExternalId()));

		// Compute context for expected results
		final OabaProcessingPhase transPhase = ta.getProcessingPhase();
		final boolean isIntermediateExpected = transPhase.isIntermediateExpected;
		final boolean isUpdateExpected = transPhase.isUpdateExpected;
		final Queue listeningQueue = ta.getResultQueue();
		final JMSConsumer statusListener = ta.getStatusConsumer();
		validateDestinations(transPhase, listeningQueue);

		// Check the job results
		final JMSContext jmsContext = ta.getJmsContext();
		if (isIntermediateExpected) {
			// Check that transitivity analysis completed and sent out a
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
			// Check that transitivity analysis sent out an expected status
			// on the update queue
			logger.info("Checking oabaStatusTopic");
			OabaNotification oabaNotification = null;
			if (transPhase == OabaProcessingPhase.INTERMEDIATE
					|| transPhase == OabaProcessingPhase.INITIAL) {
				oabaNotification =
					JmsUtils.receiveLatestOabaNotification(transJob, LOG_SOURCE,
							statusListener, SHORT_TIMEOUT_MILLIS);
			} else if (transPhase == OabaProcessingPhase.FINAL) {
				oabaNotification =
					JmsUtils.receiveFinalOabaNotification(transJob, LOG_SOURCE,
							statusListener, LONG_TIMEOUT_MILLIS);
			} else {
				throw new Error("unexpected phase: " + transPhase);
			}
			assertTrue(oabaNotification != null);
			assertTrue(oabaNotification.getJobId() == jobId);
			final float expectPercentDone = ta.getResultPercentComplete();
			assertTrue(oabaNotification.getJobPercentComplete() == expectPercentDone);
		}

		// Find the entry in the processing history updated by Transitivity
		// FIXME STUBBED
//		final OabaProcessingController processingController =
//			ta.getProcessingController();
//		OabaEventLog processingEntry =
//			processingController.getProcessingLog(transJob);
//		// te.add(processingEntry);
//
//		// Validate that processing entry is correct for this stage of the OABA
//		assertTrue(processingEntry != null);
//		final int expectedEventId = ta.getResultEventId();
//		assertTrue(processingEntry.getCurrentOabaEventId() == expectedEventId);

		// Check that the working directory contains what it should
		assertTrue(ta.isWorkingDirectoryCorrectAfterProcessing(transJob));

		// Check the number of test entities that were created
		ta.checkCounts();

		logger.exiting(LOG_SOURCE, tag);
	}

//	private static void validateQueue(OabaProcessingPhase transPhase,
//			Queue listeningQueue) {
//		// TODO Auto-generated method stub
//
//	}

	public static void validateDestinations(OabaProcessingPhase transPhase,
			Queue listeningQueue) {
		if (transPhase == null) {
			throw new IllegalArgumentException("null transitivity analysis phase");
		}
		// FIXME stubbed
//		final boolean isIntermediateExpected = transPhase.isIntermediateExpected;
//		final boolean isUpdateExpected = transPhase.isUpdateExpected;
//		if (isIntermediateExpected && !isUpdateExpected) {
//			if (listeningQueue == null) {
//				throw new IllegalArgumentException(
//						"intermediate-result queue is null");
//			}
//			if (updateQueue != null) {
//				String msg =
//					"Ignoring update queue -- results expected only "
//							+ "from intermediate-result queue";
//				logger.warning(msg);
//			}
//		} else if (isIntermediateExpected && isUpdateExpected) {
//			if (listeningQueue == null) {
//				throw new IllegalArgumentException(
//						"intermediate-result queue is null");
//			}
//			if (updateQueue == null) {
//				throw new IllegalArgumentException("update queue is null");
//			}
//		} else if (!isIntermediateExpected && isUpdateExpected) {
//			if (listeningQueue != null) {
//				String msg =
//					"Ignoring intermediate-result queue -- "
//							+ "final results expected from update queue";
//				logger.warning(msg);
//			}
//			if (updateQueue == null) {
//				throw new IllegalArgumentException("update queue is null");
//			}
//		} else {
//			String msg =
//				"unexpected: !isIntermediateExpected && !isUpdateExpected";
//			throw new Error(msg);
//		}
	}

//	protected static OabaJob doOabaProcessing(final String LOG_SOURCE,
//			final String tag, final String externalId, final OabaParameters bp,
//			final OabaSettings oabaSettings,
//			final ServerConfiguration serverConfiguration,
//			final OabaService batchQuery,
//			final OabaJobController jobController,
//			final OabaProcessingController processingController,
//			final JMSContext jmsContext, final Topic statusTopic) {
//		logger.entering(LOG_SOURCE, tag);
//
//		// Preconditions
//		if (externalId == null || bp == null || LOG_SOURCE == null
//				|| tag == null || oabaSettings == null
//				|| serverConfiguration == null || batchQuery == null
//				|| jobController == null || processingController == null
//				|| jmsContext == null || statusTopic == null) {
//			throw new IllegalArgumentException("null argument");
//		}
//
//		long jobId = NONPERSISTENT_ID;
//		try {
//			final OabaLinkageType linkage = bp.getOabaLinkageType();
//			switch (linkage) {
//			case STAGING_DEDUPLICATION:
//				logger.info(tag
//						+ ": invoking BatchQueryService.startDeduplication");
//				jobId =
//					batchQuery.startDeduplication(externalId, bp, oabaSettings,
//							serverConfiguration);
//				logger.info(tag
//						+ ": returned from BatchQueryService.startDeduplication");
//				break;
//			case STAGING_TO_MASTER_LINKAGE:
//			case MASTER_TO_MASTER_LINKAGE:
//				logger.info(tag + ": invoking BatchQueryService.startLinkage");
//				jobId =
//					batchQuery.startLinkage(externalId, bp, oabaSettings,
//							serverConfiguration);
//				logger.info(tag
//						+ ": returned from BatchQueryService.startLinkage");
//				break;
//			default:
//				fail("Unexpected linkage type: " + linkage);
//			}
//		} catch (ServerConfigurationException e) {
//			fail(e.toString());
//		}
//		OabaJob batchJob = jobController.findOabaJob(jobId);
//
//		// Check that transitivity analysis sent out an expected status
//		// on the update queue
//		logger.info("Checking updateQueue");
//		OabaNotification updateMessage = null;
//		// FIXME
//		// updateMessage =
//		// JmsUtils.receiveFinalUpdateMessage(LOG_SOURCE, jmsContext,
//		// statusTopic, VERY_LONG_TIMEOUT_MILLIS);
//		assertTrue(updateMessage != null);
//		assertTrue(updateMessage.getJobId() == jobId);
//		assertTrue(updateMessage.getJobPercentComplete() == PCT_DONE_OABA);
//
//		// FIXME STUBBED
////		// Find the entry in the processing history updated by the OABA
////		OabaEventLog processingEntry =
////			processingController.getProcessingLog(batchJob);
////		assertTrue(processingEntry != null);
////		assertTrue(processingEntry.getCurrentOabaEventId() == EVT_DONE_OABA);
//
//		return batchJob;
//	}

	private TransitivityMdbTestProcedures() {
	}

}
