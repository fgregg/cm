package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REC_VAL;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
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
import com.choicemaker.cm.args.OabaTaskType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaUpdateMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BlockingOABA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatus;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.TestEntities;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class StartOabaIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	/** A short time-out for receiving messages (1 sec) */
	public static final long SHORT_TIMEOUT_MILLIS = 1000;

	/** A long time-out for receiving messages (10 sec) */
	public static final long LONG_TIMEOUT_MILLIS = 20000;

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * BlockingOABA, SingleRecordMatch and UpdateStatus message beans. This
	 * allows other classes to attach to the block, singleRecordMatch and
	 * updateStatus queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses =
			new Class<?>[] {
					BlockingOABA.class, SingleRecordMatch.class,
					UpdateStatus.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final String LOG_SOURCE = StartOabaIT.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(StartOabaIT.class
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

	@Resource(name = "DefaultManagedExecutorService")
	ManagedExecutorService executor;

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

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

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
	public void testManagedExecutor() {
		assertTrue(setupOK);
		assertTrue(executor != null);
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
	public void testBlockQueue() {
		assertTrue(setupOK);
		assertTrue(blockQueue != null);
	}

	@Test
	@InSequence(2)
	public void testUpdateQueue() {
		assertTrue(setupOK);
		assertTrue(updateQueue != null);
	}

	@Test
	@InSequence(2)
	public void testSingleMatchQueue() {
		assertTrue(setupOK);
		assertTrue(singleMatchQueue != null);
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
		OabaJobMessage oabaJobMessage = null;
		do {
			oabaJobMessage = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", blockQueue, oabaJobMessage));
		} while (oabaJobMessage != null);
	}

	@Test
	@InSequence(4)
	public void clearUpdateQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(updateQueue);
		Object o = null;
		// OabaUpdateMessage updateData = null;
		do {
			o = receiveUpdateData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", updateQueue, o));
			if (o != null && !(o instanceof OabaUpdateMessage)) {
				logger.severe("Wrong message type on update queue: " + o == null ? null
						: o.getClass().getName());
			}
		} while (o != null);
	}

	@Test
	@InSequence(4)
	public void clearSingleMatchQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(singleMatchQueue);
		OabaJobMessage oabaJobMessage = null;
		do {
			oabaJobMessage = receiveStartData(consumer);
			logger.finest(JmsUtils.queueInfo("Clearing: ", singleMatchQueue, oabaJobMessage));
		} while (oabaJobMessage != null);
	}

	@Test
	@InSequence(5)
	public void testStartDeduplication()
			throws ServerConfigurationException {
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
		testStartLinkage(TEST, externalID, bp);

		logExiting(TEST);
	}

	@Test
	@InSequence(6)
	public void testStartLinkage()
			throws ServerConfigurationException {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		logEntering(TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		// The master record source should must be null in this set of batch
		// parameters in order to test the startOABAStage(..) method
		final PersistableRecordSource MASTER = null;
		final OabaTaskType TASK = OabaTaskType.STAGING_DEDUPLICATION;
		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), c.getStagingRecordSource(), MASTER,
					TASK);
		testStartLinkage(TEST, externalID, bp);

		logExiting(TEST);
	}

	public void testStartLinkage(final String tag, final String externalId,
			final OabaParameters bp)
			throws ServerConfigurationException {

		if (externalId == null || bp == null) {
			throw new IllegalArgumentException("null argument");
		}

		TestEntities te = new TestEntities();

		final long jobId;
		if (bp.getMasterRs() == null) {
			logger.info(tag + ": invoking OabaService.startDeduplication");
			jobId =
				batchQuery.startDeduplication(externalId, bp.getStageRs(),
						bp.getLowThreshold(), bp.getHighThreshold(),
						bp.getModelConfigurationName());
			logger.info(tag + ": returned from OabaService.startDeduplication");

		} else {
			logger.info(tag + ": invoking OabaService.startLinkage");
			jobId =
				batchQuery.startLinkage(externalId, bp.getStageRs(),
						bp.getMasterRs(), bp.getLowThreshold(),
						bp.getHighThreshold(), bp.getModelConfigurationName());
			logger.info(tag + ": returned from OabaService.startLinkage");
		}
		assertTrue(OabaJobEntity.INVALID_ID != jobId);
		OabaJob oabaJob = jobController.find(jobId);
		assertTrue(oabaJob != null);
		te.add(oabaJob);
		assertTrue(externalId != null
				&& externalId.equals(oabaJob.getExternalId()));

		// Find the persistent OabaParameters object created by the call to
		// OabaService.startOABA...
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

		// Check that the startOABA method completed and sent out a message
		// on the blocking queue
		logger.info("Checking blockQueue");
		JMSConsumer consumer = jmsContext.createConsumer(blockQueue);
		OabaJobMessage oabaJobMessage = receiveStartData(consumer, LONG_TIMEOUT_MILLIS);
		logger.info(JmsUtils.queueInfo("Received from: ", blockQueue, oabaJobMessage));
		if (oabaJobMessage == null) {
			fail("did not receive data from blocking queue");
		}
		assertTrue(oabaJobMessage.jobID == jobId);

		// Find the persistent OabaProcessing object updated by the StartOABA
		// message driven bean
		OabaJobProcessing processingEntry =
			processingController.findProcessingLogByJobId(jobId);
		te.add(processingEntry);

		// Validate that OabaProcessing entry is correct for this stage
		// em.getEntityManagerFactory().getCache().evictAll();
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == EVT_DONE_REC_VAL);

		// Check that the startOABA method sent out a message on the update
		// queue
		logger.info("Checking updateQueue");
		consumer = jmsContext.createConsumer(updateQueue);
		Object o = receiveUpdateData(consumer, SHORT_TIMEOUT_MILLIS);
		logger.info(JmsUtils.queueInfo("Received from: ", blockQueue, o));
		if (o == null) {
			fail("did not receive data from update queue");
		}
		if (!(o instanceof OabaUpdateMessage)) {
			fail("Received wrong type from update queue: " + o == null ? null
					: o.getClass().getName());
		}
		OabaUpdateMessage oabaUpdateMessage = (OabaUpdateMessage) o;
		assertTrue(oabaUpdateMessage.getJobID() == jobId);
		assertTrue(oabaUpdateMessage.getPercentComplete() == OabaProcessing.PCT_DONE_REC_VAL);

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

	// public OabaUpdateMessage receiveUpdateData(JMSConsumer consumer) {
	public Object receiveUpdateData(JMSConsumer consumer) {
		return receiveUpdateData(consumer, SHORT_TIMEOUT_MILLIS);
	}

	// public OabaUpdateMessage receiveUpdateData(JMSConsumer consumer, long timeOut) {
	public Object receiveUpdateData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveUpdateData(" + timeOut + ")";
		logEntering(METHOD);
		if (consumer == null) {
			throw new IllegalArgumentException("null consumer");
		}
		// OabaUpdateMessage retVal = null;
		Object retVal = null;
		try {
			retVal = consumer.receiveBody(Object.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logExiting(METHOD);
		return retVal;
	}

}
