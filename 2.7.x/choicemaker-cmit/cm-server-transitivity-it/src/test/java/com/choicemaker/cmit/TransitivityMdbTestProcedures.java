package com.choicemaker.cmit;

import static com.choicemaker.cm.batch.BatchJob.INVALID_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatchMDB;
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

	public static <T extends WellKnownTestConfiguration> void testLinkageTransitivity(
			AbstractTransitivityMdbTest<T> test)
			throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		String TEST = "testStartOABALinkage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		testTransitivityProcessing(OabaLinkageType.STAGING_TO_MASTER_LINKAGE,
				TEST, test, externalID);

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	public static <T extends WellKnownTestConfiguration> void testDeduplicationTransitivity(
			AbstractTransitivityMdbTest<T> test)
			throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		String TEST = "testDeduplicationTransitivity";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		testTransitivityProcessing(OabaLinkageType.STAGING_DEDUPLICATION, TEST,
				test, externalID);

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	protected static <T extends WellKnownTestConfiguration> void testTransitivityProcessing(
			final OabaLinkageType linkage, final String tag,
			final AbstractTransitivityMdbTest<T> test, final String externalId) {

		// Preconditions
		if (linkage == null || tag == null || test == null
				|| externalId == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String LOG_SOURCE = test.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = test.getTestEntityCounts();
		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final float expectedCompletion = test.getResultPercentComplete();

		final WellKnownTestConfiguration c = test.getTestConfiguration(linkage);
		final AnalysisResultFormat format = c.getTransitivityResultFormat();
		final String graphPropertyName = c.getTransitivityGraphProperty();

		final PersistableRecordSource staging =
				test.getRecordSourceController().save(c.getStagingRecordSource());
			assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);
			te.add(staging);

			final PersistableRecordSource master;
			if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
				master = null;
			} else {
				master =
					test.getRecordSourceController()
							.save(c.getMasterRecordSource());
				assertTrue(master.getId() != PersistableRecordSource.NONPERSISTENT_ID);
				te.add(master);
			}

		final OabaParameters bp =
				new OabaParametersEntity(c.getModelConfigurationName(), c
						.getThresholds().getDifferThreshold(), c.getThresholds()
						.getMatchThreshold(), staging, master, c.getOabaTask());
			te.add(bp);

			TransitivityParameters transParams =
					new TransitivityParametersEntity(bp, format, graphPropertyName);

			final String hostName =
					ServerConfigurationControllerBean.computeHostName();
				logger.info("Computed host name: " + hostName);
				final DefaultServerConfiguration dsc =
					test.getServerController().findDefaultServerConfiguration(hostName);
				ServerConfiguration serverConfiguration = null;
				if (dsc != null) {
					long id = dsc.getServerConfigurationId();
					logger.info("Default server configuration id: " + id);
					serverConfiguration =
						test.getServerController().findServerConfiguration(id);
				}
				if (serverConfiguration == null) {
					logger.info("No default server configuration for: " + hostName);
					serverConfiguration =
						test.getServerController().computeGenericConfiguration();
					try {
						serverConfiguration =
							test.getServerController().save(serverConfiguration);
					} catch (ServerConfigurationException e) {
						fail("Unable to save server configuration: " + e.toString());
					}
					te.add(serverConfiguration);
				}
				logger.info(ServerConfigurationEntity.dump(serverConfiguration));
				assertTrue(serverConfiguration != null);

//		// Do the test
//		testTransitivityProcessing(test.getSourceName(), TEST, externalId,
//				transParams, oabaJob, serverConfiguration,
//				test.getTransitivityService(), test.getJobController(),
//				test.getParamsController(), test.getProcessingController(),
//				test.getJmsContext(), resultQueue,
//				test.getTransitivityStatusTopic(), test.getEm(), test.getUtx(),
//				expectedEventId, expectedCompletion,
//				test.getOabaProcessingPhase());
//
//		test.getLogger().exiting(test.getSourceName(), TEST);
//	}
//
//	public static void testTransitivityProcessing(final String LOG_SOURCE,
//			final String tag, final String externalId,
//			final TransitivityParameters bp, final OabaJob oabaJob,
//			final ServerConfiguration serverConfiguration,

			// FIXME STUBBED
			final OabaJob oabaJob = null;
			// END FIXME

			final TransitivityService transitivityService = test.getTransitivityService();
			final OabaJobController jobController = test.getJobController();
			final OabaParametersController paramsController = test.getParamsController();
			final OabaProcessingController processingController = test.getProcessingController();
			final JMSContext jmsContext = test.getJmsContext();
			final Queue listeningQueue = resultQueue;
			final Topic transStatusUpdate = test.getTransitivityStatusTopic();
			final EntityManager em = test.getEm();
			final UserTransaction utx = test.getUtx();
			final float expectPercentDone = expectedCompletion;
			final OabaProcessingPhase oabaPhase = test.getOabaProcessingPhase();
//			) {
//		logger.entering(LOG_SOURCE, tag);

		// Preconditions
		if (externalId == null || bp == null || LOG_SOURCE == null
				|| tag == null || oabaJob == null
				|| serverConfiguration == null || transitivityService == null
				|| jobController == null || paramsController == null
				|| processingController == null || jmsContext == null
				|| transStatusUpdate == null || em == null || utx == null
				|| oabaPhase == null) {
			throw new IllegalArgumentException("null argument");
		}
		validateQueue(oabaPhase, listeningQueue);

		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		// final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		final boolean isDeduplication =
			OabaLinkageType.STAGING_DEDUPLICATION == bp.getOabaLinkageType();

//		final TestEntityCounts te = test.getTestEntityCounts();
		te.add(bp);

		long jobId = INVALID_ID;
		try {
//			final OabaLinkageType linkage = bp.getOabaLinkageType();
			switch (linkage) {
			case TRANSITIVITY_ANALYSIS:
				logger.info(tag
						+ ": invoking BatchQueryService.startDeduplication");
				jobId =
					transitivityService.startTransitivity(externalId, transParams,
							oabaJob, serverConfiguration);
				logger.info(tag
						+ ": returned from BatchQueryService.startDeduplication");
				break;
			default:
				fail("Unexpected linkage type: " + linkage);
			}
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(INVALID_ID != jobId);
		OabaJob batchJob = jobController.findOabaJob(jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalId != null
				&& externalId.equals(batchJob.getExternalId()));

		// Find the persistent OabaParameters object created by the call to
		// BatchQueryService.startLinkage...
		OabaParameters params = paramsController.findOabaParametersByJobId(jobId);
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

		// Check that OABA processing sent out an expected status
		// on the status topic
		logger.info("Checking updateQueue");
		OabaNotification updateMessage = null;
		if (oabaPhase == OabaProcessingPhase.INTERMEDIATE) {
			// FIXME
			// updateMessage =
			// JmsUtils.receiveLatestUpdateMessage(LOG_SOURCE, jmsContext,
			// updateQueue, JmsUtils.SHORT_TIMEOUT_MILLIS);
		} else if (oabaPhase == OabaProcessingPhase.FINAL) {
			// FIXME
			// updateMessage =
			// JmsUtils.receiveFinalUpdateMessage(LOG_SOURCE, jmsContext,
			// updateQueue, VERY_LONG_TIMEOUT_MILLIS);
		} else {
			throw new Error("unexpected phase: " + oabaPhase);
		}
		assertTrue(updateMessage != null);
		assertTrue(updateMessage.getJobId() == jobId);
		assertTrue(updateMessage.getJobPercentComplete() == expectPercentDone);

		// Find the entry in the processing history updated by the OABA
		OabaEventLog processingEntry =
			processingController.getProcessingLog(batchJob);
		// te.add(processingEntry);

		// Validate that processing entry is correct for this stage of the OABA
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentOabaEventId() == expectedEventId);

		// Check the number of test entities that were created
		test.checkCounts();

		logger.exiting(LOG_SOURCE, tag);
	}

	private static void validateQueue(OabaProcessingPhase oabaPhase,
			Queue listeningQueue) {
		// TODO Auto-generated method stub

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

	protected static OabaJob doOabaProcessing(final String LOG_SOURCE,
			final String tag, final String externalId, final OabaParameters bp,
			final OabaSettings oabaSettings,
			final ServerConfiguration serverConfiguration,
			final OabaService batchQuery,
			final OabaJobController jobController,
			final OabaProcessingController processingController,
			final JMSContext jmsContext, final Topic statusTopic) {
		logger.entering(LOG_SOURCE, tag);

		// Preconditions
		if (externalId == null || bp == null || LOG_SOURCE == null
				|| tag == null || oabaSettings == null
				|| serverConfiguration == null || batchQuery == null
				|| jobController == null || processingController == null
				|| jmsContext == null || statusTopic == null) {
			throw new IllegalArgumentException("null argument");
		}

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
		OabaJob batchJob = jobController.findOabaJob(jobId);

		// Check that OABA processing sent out an expected status
		// on the update queue
		logger.info("Checking updateQueue");
		OabaNotification updateMessage = null;
		// FIXME
		// updateMessage =
		// JmsUtils.receiveFinalUpdateMessage(LOG_SOURCE, jmsContext,
		// statusTopic, VERY_LONG_TIMEOUT_MILLIS);
		assertTrue(updateMessage != null);
		assertTrue(updateMessage.getJobId() == jobId);
		assertTrue(updateMessage.getJobPercentComplete() == PCT_DONE_OABA);

		// Find the entry in the processing history updated by the OABA
		OabaEventLog processingEntry =
			processingController.getProcessingLog(batchJob);
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentOabaEventId() == EVT_DONE_OABA);

		return batchJob;
	}

	private TransitivityMdbTestProcedures() {
	}

}
