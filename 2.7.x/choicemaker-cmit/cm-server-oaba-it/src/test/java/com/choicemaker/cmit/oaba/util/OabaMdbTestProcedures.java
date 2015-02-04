package com.choicemaker.cmit.oaba.util;

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

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
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
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cmit.oaba.AbstractOabaMdbTest;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
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
		final OabaProcessingPhase oabaPhase = test.getOabaProcessingPhase();
		final boolean isIntermediateExpected = oabaPhase.isIntermediateExpected;
		final boolean isUpdateExpected = oabaPhase.isUpdateExpected;
		final Queue listeningQueue = test.getResultQueue();
		final JMSConsumer statusListener = test.getOabaStatusConsumer();
		validateDestinations(oabaPhase, listeningQueue);

		final WellKnownTestConfiguration c = test.getTestConfiguration(linkage);

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

		final String modelId = c.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getImmutableModelInstance(modelId);
		OabaSettings oabaSettings =
			test.getSettingsController().findDefaultOabaSettings(model);
		if (oabaSettings == null) {
			// Creates generic settings and saves them
			oabaSettings = new OabaSettingsEntity();
			oabaSettings = test.getSettingsController().save(oabaSettings);
			te.add(oabaSettings);
		}
		assertTrue(oabaSettings != null);

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

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging, master, c.getOabaTask());
		te.add(bp);

		final OabaService batchQuery = test.getOabaService();
		long jobId = NONPERSISTENT_ID;
		try {
			switch (linkage) {
			case STAGING_DEDUPLICATION:
				logger.info(tag
						+ ": invoking BatchQueryService.startDeduplication");
				jobId =
					batchQuery.startDeduplication(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from BatchQueryService.startDeduplication");
				break;
			case STAGING_TO_MASTER_LINKAGE:
			case MASTER_TO_MASTER_LINKAGE:
				logger.info(tag + ": invoking BatchQueryService.startLinkage");
				jobId =
					batchQuery.startLinkage(externalId, bp, oabaSettings,
							serverConfiguration);
				logger.info(tag + ": returned jobId '" + jobId
						+ "' from BatchQueryService.startLinkage");
				break;
			default:
				fail("Unexpected linkage type: " + linkage);
			}
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}

		final OabaJobController jobController = test.getJobController();
		assertTrue(jobId != NONPERSISTENT_ID);
		OabaJob oabaJob = jobController.findOabaJob(jobId);
		assertTrue(oabaJob != null);
		te.add(oabaJob);
		assertTrue(externalId != null
				&& externalId.equals(oabaJob.getExternalId()));

		// Find the persistent OabaParameters object created by the call to
		// BatchQueryService.startLinkage...
		final OabaParametersController paramsController =
			test.getParamsController();
		OabaParameters params =
			paramsController.findOabaParametersByJobId(jobId);
		te.add(params);

		// Validate that the job parameters are correct
		assertTrue(params != null);
		assertTrue(params.getLowThreshold() == bp.getLowThreshold());
		assertTrue(params.getHighThreshold() == bp.getHighThreshold());
		if (OabaLinkageType.STAGING_DEDUPLICATION == linkage) {
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
			OabaNotification oabaNotification = null;
			if (oabaPhase == OabaProcessingPhase.INTERMEDIATE
					|| oabaPhase == OabaProcessingPhase.INITIAL) {
				oabaNotification =
					JmsUtils.receiveLatestOabaNotification(oabaJob, LOG_SOURCE,
							statusListener, SHORT_TIMEOUT_MILLIS);
			} else if (oabaPhase == OabaProcessingPhase.FINAL) {
				oabaNotification =
				// JmsUtils.receiveFinalOabaNotification(LOG_SOURCE, jmsContext,
				// oabaStatusTopic, VERY_LONG_TIMEOUT_MILLIS);
					JmsUtils.receiveFinalOabaNotification(oabaJob, LOG_SOURCE,
							statusListener, LONG_TIMEOUT_MILLIS);
			} else {
				throw new Error("unexpected phase: " + oabaPhase);
			}
			assertTrue(oabaNotification != null);
			assertTrue(oabaNotification.getJobId() == jobId);
			final float expectPercentDone = test.getResultPercentComplete();
			assertTrue(oabaNotification.getJobPercentComplete() == expectPercentDone);
		}

		// Find the entry in the processing history updated by the OABA
		final OabaProcessingController processingController =
			test.getProcessingController();
		OabaEventLog processingEntry =
			processingController.getProcessingLog(oabaJob);
		// te.add(processingEntry);

		// Validate that processing entry is correct for this stage of the OABA
		assertTrue(processingEntry != null);
		final int expectedEventId = test.getResultEventId();
		assertTrue(processingEntry.getCurrentOabaEventId() == expectedEventId);

		// Check that the working directory contains what it should
		assertTrue(test.isWorkingDirectoryCorrectAfterProcessing(linkage,
				oabaJob, bp, oabaSettings, serverConfiguration));

		// Check the number of test entities that were created
		test.checkCounts();

		logger.exiting(LOG_SOURCE, tag);
	}

	public static void validateDestinations(OabaProcessingPhase oabaPhase,
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
