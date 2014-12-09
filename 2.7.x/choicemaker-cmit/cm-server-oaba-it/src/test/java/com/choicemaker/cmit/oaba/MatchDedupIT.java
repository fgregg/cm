package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.batch.BatchJob.INVALID_ID;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_OABA;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatus;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.TestEntities;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class MatchDedupIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	/** A short time-out for receiving messages (1 sec) */
	public static final long SHORT_TIMEOUT_MILLIS = 1000;

	/** A long time-out for receiving messages (10 sec) */
	public static final long LONG_TIMEOUT_MILLIS = 20000;

	/** A long time-out for receiving messages (5 min) */
	public static final long VERY_LONG_TIMEOUT_MILLIS = 300000;

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * DedeupOABA and UpdateStatus message beans. This allows other classes to
	 * attach to the chunk and update queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = {
				UpdateStatus.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final String LOG_SOURCE = MatchDedupIT.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(MatchDedupIT.class
			.getName());

	/**
	 * Workaround for logger.entering(String,String) not showing up in JBOSS
	 * server log
	 */
	private static void logEntering(String method) {
		logger.info("Entering " + LOG_SOURCE + "." + method);
	}

	/**
	 * Workaround for logger.exiting(String,String) not showing up in JBOSS
	 * server log
	 */
	private static void logExiting(String method) {
		logger.info("Exiting " + LOG_SOURCE + "." + method);
	}

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	EjbPlatform e2service;

	@EJB
	private OabaJobControllerBean jobController;
	
	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	protected OabaService batchQuery;

	@EJB
	protected TestController controller;

	@Resource(lookup = "choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "choicemaker/urm/jms/chunkQueue")
	private Queue chunkQueue;

	@Resource(lookup = "choicemaker/urm/jms/dedupQueue")
	private Queue dedupQueue;

	@Resource(lookup = "choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Resource(lookup = "choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	JMSContext jmsContext;

	private int initialOabaParamsCount;
	private int initialOabaJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() {
		final String METHOD = "setUp";
		logEntering(METHOD);
		setupOK = true;
		try {
			initialOabaParamsCount =
					controller.findAllOabaParameters().size();
				initialOabaJobCount = controller.findAllOabaJobs().size();
				initialOabaProcessingCount =
					controller.findAllOabaProcessing().size();
		} catch (Exception x) {
			logger.severe(x.toString());
			setupOK = false;
		}
		logExiting(METHOD);
	}

	@After
	public void tearDown() {
		final String METHOD = "tearDown";
		logEntering(METHOD);
		try {

			int finalOabaParamsCount =
				controller.findAllOabaParameters().size();
			String alert = "initialOabaParamsCount != finalOabaParamsCount";
			assertTrue(alert, initialOabaParamsCount == finalOabaParamsCount);

			int finalOabaJobCount = controller.findAllOabaJobs().size();
			alert = "initialOabaJobCount != finalOabaJobCount";
			assertTrue(alert, initialOabaJobCount == finalOabaJobCount);

			int finalOabaProcessingCount =
				controller.findAllOabaProcessing().size();
			alert = "initialOabaProcessingCount != finalOabaProcessingCount";
			assertTrue(alert,
					initialOabaProcessingCount == finalOabaProcessingCount);

		} catch (Exception x) {
			logger.severe(x.toString());
		} catch (AssertionError x) {
			logger.severe(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	@InSequence(1)
	public void testEntityManager() {
		assertTrue(setupOK);
		assertTrue(em != null);
	}

	@Test
	@InSequence(1)
	public void testUserTransaction() {
		assertTrue(setupOK);
		assertTrue(utx != null);
	}

	@Test
	@InSequence(1)
	public void testE2Service() {
		assertTrue(setupOK);
		assertTrue(e2service != null);
	}

	@Test
	@InSequence(1)
	public void testBatchQuery() {
		assertTrue(setupOK);
		assertTrue(batchQuery != null);
	}

	@Test
	@InSequence(1)
	public void testTransitivityController() {
		assertTrue(setupOK);
		assertTrue(controller != null);
	}

	@Test
	@InSequence(2)
	public void testChunkQueue() {
		assertTrue(setupOK);
		assertTrue(chunkQueue != null);
	}

	@Test
	@InSequence(2)
	public void testDedupQueue() {
		assertTrue(setupOK);
		assertTrue(dedupQueue != null);
	}

	@Test
	@InSequence(2)
	public void testMatchDedupQueue() {
		assertTrue(setupOK);
		assertTrue(this.matchDedupQueue != null);
	}

	@Test
	@InSequence(2)
	public void testMatchSchedulerQueue() {
		assertTrue(setupOK);
		assertTrue(matchSchedulerQueue != null);
	}

	@Test
	@InSequence(2)
	public void testUpdateQueue() {
		assertTrue(setupOK);
		assertTrue(updateQueue != null);
	}

	@Test
	@InSequence(3)
	public void testJmsContext() {
		assertTrue(setupOK);
		assertTrue(jmsContext != null);
	}

	@Test
	@InSequence(4)
	public void clearBlockQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(blockQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", blockQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearStartQueue() {
		assertTrue(setupOK);
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(startQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearUpdateQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(updateQueue);
		OabaUpdateMessage updateMessage = null;
		do {
			updateMessage = receiveUpdateMessage(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", updateQueue, updateMessage));
		} while (updateMessage != null);
	}

	@Test
	@InSequence(4)
	public void clearSingleMatchQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(singleMatchQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", singleMatchQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearChunkQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(chunkQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.info(JmsUtils.queueInfo("Clearing: ", chunkQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearDedupQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(dedupQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", dedupQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearMatchDedupeQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(matchDedupQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", matchDedupQueue,
					startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearMatchSchedulerQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(matchSchedulerQueue);
		OabaJobMessage startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", matchSchedulerQueue,
					startData));
		} while (startData != null);
	}

	@Test
	@InSequence(5)
	public void testStartLinkage() {
		assertTrue(setupOK);
		String TEST = "testStartOABALinkage";
		logEntering(TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), c.getStagingRecordSource(),
					c.getMasterRecordSource(), c.getOabaTask());
		testStartOABA(TEST, externalID, bp);

		logExiting(TEST);
	}

	@Test
	@InSequence(6)
	public void testStartDeduplication() {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		logEntering(TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		// The master record source should must be null in this set of batch
		// parameters in order to test the startDeduplication(..) method
		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), c.getStagingRecordSource());
		testStartOABA(TEST, externalID, bp);

		logExiting(TEST);
	}

	public void testStartOABA(final String tag, final String externalId,
			final OabaParameters bp) {

		if (externalId == null || bp == null) {
			throw new IllegalArgumentException("null argument");
		}

		TestEntities te = new TestEntities();
		te.add(bp);

		long jobId = INVALID_ID;
		if (bp.getMasterRs() == null) {
			logger.info(tag + ": invoking BatchQueryService.startDeduplication");
			try {
				jobId =
					batchQuery.startDeduplication(externalId, bp.getStageRs(),
							bp.getLowThreshold(), bp.getHighThreshold(),
							bp.getModelConfigurationName());
			} catch (ServerConfigurationException e) {
				fail(e.toString());
			}
			logger.info(tag + ": returned from BatchQueryService.startLinkage");
		} else {
			logger.info(tag + ": invoking BatchQueryService.startDeduplication");
			try {
				jobId =
					batchQuery.startLinkage(externalId, bp.getStageRs(),
							bp.getMasterRs(), bp.getLowThreshold(),
							bp.getHighThreshold(), bp.getModelConfigurationName());
			} catch (ServerConfigurationException e) {
				fail(e.toString());
			}
			logger.info(tag + ": returned from BatchQueryService.startLinkage");
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
		if (bp.getMasterRs() == null) {
			assertTrue(params.getMasterRs() == null);
		} else {
			assertTrue(params.getMasterRs() != null
					&& params.getMasterRs().equals(bp.getMasterRs()));
		}
		assertTrue(params.getStageRs() != null
				&& params.getStageRs().equals(bp.getStageRs()));
		assertTrue(params.getModelConfigurationName() != null
				&& params.getModelConfigurationName().equals(
						bp.getModelConfigurationName()));

		// Check that the startLinkage method sent out a message on the update
		// queue
		logger.info("Checking updateQueue");
		JMSConsumer consumer = jmsContext.createConsumer(updateQueue);
		OabaUpdateMessage updateMessage =
			receiveDoneMatchingMessage(consumer, VERY_LONG_TIMEOUT_MILLIS);
		assertTrue(updateMessage != null);
		assertTrue(updateMessage.getJobID() == jobId);
		assertTrue(updateMessage.getPercentComplete() == PCT_DONE_OABA);

		// Find the persistent OabaProcessing object updated by the StartOABA
		// message driven bean
		OabaJobProcessing processingEntry =
				processingController.findProcessingLogByJobId(jobId);
		te.add(processingEntry);

		// Validate that OabaProcessing entry is correct for this stage
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == EVT_DONE_OABA);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}

		logExiting(tag);
	}

	public OabaJobMessage receiveStartData(JMSConsumer consumer) {
		return receiveStartData(consumer, SHORT_TIMEOUT_MILLIS);
	}

	public OabaJobMessage receiveStartData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveStartData(" + timeOut + ")";
		logEntering(METHOD);
		OabaJobMessage retVal = null;
		try {
			retVal = consumer.receiveBody(OabaJobMessage.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logExiting(METHOD);
		return retVal;
	}

	public OabaUpdateMessage receiveUpdateMessage(JMSConsumer consumer) {
		return receiveUpdateMessage(consumer, SHORT_TIMEOUT_MILLIS);
	}

	public OabaUpdateMessage receiveDoneMatchingMessage(JMSConsumer consumer,
			long timeOut) {
		final String METHOD = "receiveLatestUpdateMessage(" + timeOut + ")";
		logEntering(METHOD);
		OabaUpdateMessage retVal = null;
		OabaUpdateMessage msg = null;
		do {
			msg = receiveUpdateMessage(consumer, timeOut);
			if (msg != null) {
				retVal = msg;
			}
			if (msg.getPercentComplete() == PCT_DONE_OABA) {
				break;
			}
		} while (msg != null);
		logExiting(METHOD);
		return retVal;
	}

	public OabaUpdateMessage receiveUpdateMessage(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveUpdateMessage(" + timeOut + ")";
		logEntering(METHOD);
		if (consumer == null) {
			throw new IllegalArgumentException("null consumer");
		}
		Object o = null;
		try {
			o = consumer.receiveBody(Object.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logger.info(JmsUtils.queueInfo("Received from: ", updateQueue, o));
		if (o != null && !(o instanceof OabaUpdateMessage)) {
			fail("Received wrong type from update queue: "
					+ o.getClass().getName());
		}
		OabaUpdateMessage retVal = (OabaUpdateMessage) o;
		logExiting(METHOD);
		return retVal;
	}

}
