package com.choicemaker.cmit.trans.util;

import static com.choicemaker.cm.args.BatchProcessing.EVT_DONE;
import static com.choicemaker.cm.args.BatchProcessing.PCT_DONE;
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
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.BatchProcessingNotification;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.trans.AbstractTransitivityMdbTest;
import com.choicemaker.cmit.utils.BatchProcessingPhase;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaTestUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.TransitivityTestParameters;
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

	public static <T extends WellKnownTestConfiguration> BatchJob runOabaJob(
			final TransitivityTestParameters tp,
			final OabaLinkageType task, final String tag,
			final String externalId, final String LOG_SOURCE,
			final TestEntityCounts te) throws Exception {

		final String M = "TransitivityMdbTestProcedures.runOabaJob";
		final BatchJob batchJob =
			OabaTestUtils.startOabaJob(task, tag, tp, externalId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		final long jobId = batchJob.getId();

		// Wait for the job to send a final processing notification
		logger.info(M + ": Checking oabaStatusTopic");
		final JMSConsumer statusListener = tp.getOabaStatusConsumer();
		BatchProcessingNotification oabaNotification =
			JmsUtils.receiveFinalBatchProcessingNotification(batchJob,
					LOG_SOURCE, statusListener, LONG_TIMEOUT_MILLIS);
		assertTrue(oabaNotification != null);
		assertTrue(oabaNotification.getJobId() == jobId);
		assertTrue(oabaNotification.getEventId() == EVT_DONE);
		assertTrue(oabaNotification.getJobPercentComplete() == PCT_DONE);

		// Get a fresh copy of the OABA job from the database
		BatchJob retVal = tp.getEm().find(OabaJobEntity.class, jobId);
		assertTrue(retVal != null);
		String m = M + ": OABA job " + jobId + " status: " + retVal.getStatus();
		logger.info(m);

		return retVal;
	}

	public static <T extends WellKnownTestConfiguration> void testTransitivityProcessing(
			final AbstractTransitivityMdbTest<T> ta, final OabaLinkageType task)
			throws Exception {

		// Preconditions
		if (ta == null || task == null) {
			throw new IllegalArgumentException("null argument");
		}

		final String tag = "testLinkageTransitivity";
		final TransitivityTestParameters tp = ta.getTestParameters(task);
		final String LOG_SOURCE = tp.getSourceName();
		logger.entering(LOG_SOURCE, tag);

		final TestEntityCounts te = ta.getTestEntityCounts();
		final String extId = EntityManagerUtils.createExternalId(tag);

		// Run an OABA job for subsequent transitivity analysis
		final BatchJob oabaJob =
			runOabaJob(tp, task, tag, extId, LOG_SOURCE, te);
		assertTrue(oabaJob != null);
		final long oabaJobId = oabaJob.getId();
		assertTrue(te.contains(oabaJob));
		logger.info("OABA job status: " + oabaJob.getStatus());
		assertTrue(oabaJob.getStatus() == BatchJobStatus.COMPLETED);

		// Find the OABA parameters associated with the job
		final OabaParametersController oabaParamsController =
			ta.getOabaParamsController();
		final OabaParameters oabaParams =
			oabaParamsController.findOabaParametersByBatchJobId(oabaJobId);
		te.add(oabaParams);

		// Set up parameters for transitivity analysis
		final WellKnownTestConfiguration wktc = ta.getTestConfiguration(task);
		final AnalysisResultFormat arf = wktc.getTransitivityResultFormat();
		final String gpn = wktc.getTransitivityGraphProperty();
		final TransitivityParametersEntity transParams =
			new TransitivityParametersEntity(oabaParams, arf, gpn);
		te.add((TransitivityParameters) transParams);

		// Configure settings from transitivity analysis
		OabaSettingsController sc = tp.getSettingsController();
		OabaSettings settings = sc.findOabaSettingsByJobId(oabaJobId);

		// Configure the server for transitivity analysis
		final String hostName =
			ServerConfigurationControllerBean.computeHostName();
		logger.info("Computed host name: " + hostName);
		final DefaultServerConfiguration dsc =
				tp.getServerController().findDefaultServerConfiguration(hostName);
		ServerConfiguration serverConfiguration = null;
		if (dsc != null) {
			long id = dsc.getServerConfigurationId();
			logger.info("Default server configuration id: " + id);
			serverConfiguration =
					tp.getServerController().findServerConfiguration(id);
		}
		if (serverConfiguration == null) {
			logger.info("No default server configuration for: " + hostName);
			serverConfiguration =
					tp.getServerController().computeGenericConfiguration();
			try {
				serverConfiguration =
						tp.getServerController().save(serverConfiguration);
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
			logger.info(tag
					+ ": invoking TransitivityService.startTransitivity");
			jobId =
				transitivityService.startTransitivity(extId, transParams,
						oabaJob, settings, serverConfiguration);
			logger.info(tag
					+ ": returned from TransitivityService.startTransitivity");
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(jobId != NONPERSISTENT_ID);

		// Find the transitivity job
		final TransitivityJobController transJobController =
			ta.getTransJobController();
		BatchJob transJob = transJobController.findTransitivityJob(jobId);
		assertTrue(transJob != null);
		te.add(transJob);
		assertTrue(extId != null && extId.equals(transJob.getExternalId()));

		// Compute context for expected results
		final BatchProcessingPhase transPhase = tp.getProcessingPhase();
		final boolean isIntermediateExpected =
				transPhase.isIntermediateExpected;
		final boolean isUpdateExpected = transPhase.isUpdateExpected;
		final Queue listeningQueue = ta.getResultQueue();
		final JMSConsumer transListener = tp.getTransitivityStatusConsumer();
		logger.info(LOG_SOURCE + "." + tag + ": transPhase: "
				+ transPhase);
		logger.info(LOG_SOURCE + "." + tag + ": isIntermediateExpected: "
				+ isIntermediateExpected);
		logger.info(LOG_SOURCE + "." + tag + ": isUpdateExpected: "
				+ isUpdateExpected);
		logger.info(LOG_SOURCE + "." + tag + ": listeningQueue: "
				+ listeningQueue);
		logger.info(LOG_SOURCE + "." + tag + ": transListener: "
				+ transListener);

		// Check that the listening queue is valid for the current
		// phase of the transitivity analysis
		validateDestinations(transPhase, listeningQueue);

		// Check the job results
		final JMSContext jmsContext = tp.getJmsContext();
		if (isIntermediateExpected) {
			// Check that transitivity analysis completed and sent out a
			// message on the intermediate result queue
			assertTrue(listeningQueue != null);
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
			assertTrue("jobID:" + startData.jobID + ", jobId:" + jobId, startData.jobID == jobId);
		}
		if (isUpdateExpected) {
			// Check that transitivity analysis sent out an expected status
			// on the update queue
			logger.info("Checking transStatusTopic");
			BatchProcessingNotification transNotification = null;
			if (transPhase == BatchProcessingPhase.INTERMEDIATE
					|| transPhase == BatchProcessingPhase.INITIAL) {
				transNotification =
					JmsUtils.receiveLatestBatchProcessingNotification(transJob,
							LOG_SOURCE, transListener, SHORT_TIMEOUT_MILLIS);
			} else if (transPhase == BatchProcessingPhase.FINAL) {
				transNotification =
					JmsUtils.receiveFinalBatchProcessingNotification(transJob,
							LOG_SOURCE, transListener, LONG_TIMEOUT_MILLIS);
			} else {
				throw new Error("unexpected phase: " + transPhase);
			}
			assertTrue(transNotification != null);
			assertTrue(transNotification.getJobId() == jobId);
			final float e = ta.getResultPercentComplete();
			final float c = transNotification.getJobPercentComplete();
			assertTrue("computed: " + c + ", expected: " + e, c == e);
		}

		// Find the entry in the processing history updated by Transitivity
		final ProcessingController processingController =
			tp.getTransitivityProcessingController();
		ProcessingEventLog processingEntry =
			processingController.getProcessingLog(transJob);

		// Validate that processing entry is correct
		assertTrue(processingEntry != null);
		final int c = processingEntry.getCurrentProcessingEventId();
		final int e = ta.getResultEventId();
		assertTrue("currentEventId: " + c + ", expectedId; " + e, c == e);

		// Check that the working directory contains what it should
		assertTrue(ta.isWorkingDirectoryCorrectAfterProcessing(transJob));

		// Check the number of test entities that were created
		ta.checkCounts();

		logger.exiting(LOG_SOURCE, tag);
	}

	public static void validateDestinations(BatchProcessingPhase transPhase,
			Queue listeningQueue) {
		// STUBBED
		if (transPhase == null) {
			throw new IllegalArgumentException(
					"null transitivity analysis phase");
		}
	}

	private TransitivityMdbTestProcedures() {
	}

}
