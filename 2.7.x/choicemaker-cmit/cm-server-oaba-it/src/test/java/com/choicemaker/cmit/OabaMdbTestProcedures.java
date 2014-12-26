package com.choicemaker.cmit;

import static com.choicemaker.cm.batch.BatchJob.INVALID_ID;
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
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.TestEntities;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;

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
			Class<T> c, CMPluginRegistry registry) {

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
			retVal.initialize(registry);
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
		
		assertTrue(test.isSetupOK());
		String TEST = "testStartOABALinkage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final WellKnownTestConfiguration c = test.getTestConfiguration();

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			test.getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		prs = c.getMasterRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource master =
			test.getRecordSourceController().save(prs);
		assertTrue(master.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging, master, c.getOabaTask());
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(test.getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(test
					.getServerController());

		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final int expectedCompletion = test.getResultPercentComplete();

		testOabaProcessing(test.getSourceName(),
				TEST, externalID, bp, oabaSettings, serverConfiguration,
				test.getOabaService(), test.getJobController(),
				test.getParamsController(),
				test.getProcessingController(), test.getJmsContext(),
				resultQueue, test.getUpdateQueue(), test.getEm(),
				test.getUtx(), expectedEventId, expectedCompletion,
				test.getOabaProcessingPhase());

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	public static <T extends WellKnownTestConfiguration> void testDeduplicationProcessing(
			AbstractOabaMdbTest<T> test) throws ServerConfigurationException {
		if (test == null) {
			throw new IllegalArgumentException("null argument");
		}

		assertTrue(test.isSetupOK());
		String TEST = "testStartOABAStage";
		test.getLogger().entering(test.getSourceName(), TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final WellKnownTestConfiguration c = test.getTestConfiguration();

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging =
			test.getRecordSourceController().save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging);
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(test.getSettingsController(),
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(test
					.getServerController());

		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final int expectedCompletion = test.getResultPercentComplete();

		testOabaProcessing(test.getSourceName(),
				TEST, externalID, bp, oabaSettings, serverConfiguration,
				test.getOabaService(), test.getJobController(),
				test.getParamsController(),
				test.getProcessingController(), test.getJmsContext(),
				resultQueue, test.getUpdateQueue(), test.getEm(),
				test.getUtx(), expectedEventId, expectedCompletion,
				test.getOabaProcessingPhase());

		test.getLogger().exiting(test.getSourceName(), TEST);
	}

	protected static void testOabaProcessing(final String LOG_SOURCE,
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
		OabaJob batchJob = jobController.findOabaJob(jobId);
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

	private OabaMdbTestProcedures() {
	}

}
