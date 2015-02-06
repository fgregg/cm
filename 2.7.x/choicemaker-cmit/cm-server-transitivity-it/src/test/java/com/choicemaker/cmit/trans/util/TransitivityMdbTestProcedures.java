package com.choicemaker.cmit.trans.util;

import static com.choicemaker.cm.batch.impl.AbstractPersistentObject.NONPERSISTENT_ID;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaNotification;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.trans.AbstractTransitivityMdbTest;
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

	public static <T extends WellKnownTestConfiguration> void testTransitivityProcessing(
			final AbstractTransitivityMdbTest<T> test, final OabaJob oabaJob) {

		// Preconditions
		if (oabaJob == null || test == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String tag = "testLinkageTransitivity";
		test.getLogger().entering(test.getSourceName(), tag);

		final String externalId = EntityManagerUtils.createExternalId(tag);

		final String LOG_SOURCE = test.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = test.getTestEntityCounts();
		final Queue resultQueue = test.getResultQueue();
		final int expectedEventId = test.getResultEventId();
		final float expectedCompletion = test.getResultPercentComplete();

		final long oabaJobId = oabaJob.getId();
		final OabaParametersController oabaParamsController = test.getOabaParamsController();
		final OabaParameters oabaParams = oabaParamsController.findOabaParametersByJobId(oabaJobId);
		final OabaLinkageType linkage = oabaParams.getOabaLinkageType();
		final WellKnownTestConfiguration c = test.getTestConfiguration(linkage);
		final AnalysisResultFormat format = c.getTransitivityResultFormat();
		final String graphPropertyName = c.getTransitivityGraphProperty();

		final PersistableRecordSource staging =
			test.getRecordSourceController().save(c.getStagingRecordSource());
		assertTrue(staging.isPersistent());
		te.add(staging);

		final PersistableRecordSource master;
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
			master = null;
		} else {
			master =
				test.getRecordSourceController()
						.save(c.getMasterRecordSource());
			assertTrue(master.isPersistent());
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

		final TransitivityService transitivityService =
			test.getTransitivityService();
		final OabaJobController jobController = test.getOabaJobController();
		final OabaParametersController paramsController =
			test.getOabaParamsController();
		final OabaProcessingController processingController =
			test.getProcessingController();
		final JMSContext jmsContext = test.getJmsContext();
		final Queue listeningQueue = resultQueue;
		final Topic transStatusUpdate = test.getTransitivityStatusTopic();
		final EntityManager em = test.getEm();
		final UserTransaction utx = test.getUtx();
		final float expectPercentDone = expectedCompletion;
		final OabaProcessingPhase oabaPhase = test.getOabaProcessingPhase();

		validateQueue(oabaPhase, listeningQueue);

		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		// final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		final boolean isDeduplication =
			OabaLinkageType.STAGING_DEDUPLICATION == bp.getOabaLinkageType();

		// final TestEntityCounts te = test.getTestEntityCounts();
		te.add(bp);

		// FIXME STUBBED
		final TransitivityJobController transJobController = null;
		// FIXME STUBBED
		long jobId = NONPERSISTENT_ID;
		try {
			logger.info(tag + ": invoking BatchQueryService.startDeduplication");
			jobId =
				transitivityService.startTransitivity(externalId, transParams,
						oabaJob, serverConfiguration);
			logger.info(tag
					+ ": returned from BatchQueryService.startDeduplication");
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(jobId != NONPERSISTENT_ID);

		TransitivityJob transJob = transJobController.findTransitivityJob(jobId);
		assertTrue(transJob != null);
		te.add(transJob);
		assertTrue(externalId != null
				&& externalId.equals(transJob.getExternalId()));

		// Find the persistent OabaParameters object created by the call to
		// BatchQueryService.startLinkage...
		OabaParameters params =
			paramsController.findOabaParametersByJobId(jobId);
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

		// FIXME STUBBED
//		// Find the entry in the processing history updated by the OABA
//		OabaEventLog processingEntry =
//			processingController.getProcessingLog(transJob);
//		// te.add(processingEntry);
//
//		// Validate that processing entry is correct for this stage of the OABA
//		assertTrue(processingEntry != null);
//		assertTrue(processingEntry.getCurrentOabaEventId() == expectedEventId);

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

		long jobId = NONPERSISTENT_ID;
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

		// FIXME STUBBED
//		// Find the entry in the processing history updated by the OABA
//		OabaEventLog processingEntry =
//			processingController.getProcessingLog(batchJob);
//		assertTrue(processingEntry != null);
//		assertTrue(processingEntry.getCurrentOabaEventId() == EVT_DONE_OABA);

		return batchJob;
	}

	private TransitivityMdbTestProcedures() {
	}

}
