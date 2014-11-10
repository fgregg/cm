package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REC_VAL;
import static com.choicemaker.cmit.oaba.util.OabaConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaConstants.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.utils.DeploymentUtils.createEAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolvePom;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.core.ISerializableDbRecordSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BlockingOABA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatus;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.TestEntities;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class StartOabaIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public static final String REGEX_EJB_DEPENDENCIES =
		"com.choicemaker.cm.io.blocking.automated.offline.server.*.jar"
				+ "|com.choicemaker.e2.ejb.*.jar";

	/** A short time-out for receiving messages (1 sec) */
	public static final long SHORT_TIMEOUT_MILLIS = 1000;

	/** A long time-out for receiving messages (10 sec) */
	public static final long LONG_TIMEOUT_MILLIS = 20000;

	public static final String[] removedPaths() {
		Class<?>[] removedClasses =
			new Class<?>[] {
					BlockingOABA.class, SingleRecordMatch.class,
					UpdateStatus.class };
		Set<String> removedPaths = new LinkedHashSet<>();
		for (Class<?> c : removedClasses) {
			String path = "/" + c.getName().replace('.', '/') + ".class";
			removedPaths.add(path);
		}
		String[] retVal = removedPaths.toArray(new String[removedPaths.size()]);
		return retVal;
	}

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * BlockingOABA, SingleRecordMatch and UpdateStatus message beans. This
	 * allows other classes to attach to the block, singleRecordMatch and
	 * updateStatus queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);

		File[] libs = resolveDependencies(pom);

		// Filter the OABA server and E2Plaform JARs from the dependencies
		final Pattern p = Pattern.compile(REGEX_EJB_DEPENDENCIES);
		Set<File> ejbJARs = new LinkedHashSet<>();
		List<File> filteredLibs = new LinkedList<>();
		for (File lib : libs) {
			String name = lib.getName();
			Matcher m = p.matcher(name);
			if (m.matches()) {
				boolean isAdded = ejbJARs.add(lib);
				if (!isAdded) {
					String path = lib.getAbsolutePath();
					throw new RuntimeException("failed to add (duplicate?): "
							+ path);
				}
			} else {
				filteredLibs.add(lib);
			}
		}
		File[] libs2 = filteredLibs.toArray(new File[filteredLibs.size()]);

		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs2, TESTS_AS_EJB_MODULE);

		// Filter the targeted paths from the EJB JARs
		for (File ejb : ejbJARs) {
			JavaArchive filteredEJB =
				ShrinkWrap.createFromZipFile(JavaArchive.class, ejb);
			for (String path : removedPaths()) {
				filteredEJB.delete(path);
			}
			retVal.addAsModule(filteredEJB);
		}

		return retVal;
	}

	private static String queueInfo(String tag, Queue q, Object d) {
		String queueName;
		try {
			queueName = q.getQueueName();
		} catch (JMSException x) {
			queueName = "unknown";
		}
		StringBuilder sb =
			new StringBuilder(tag).append("queue: '").append(queueName)
					.append("', data: '").append(d).append("'");
		return sb.toString();
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
	protected BatchQueryService batchQuery;

	@EJB
	protected TransitivityJobController controller;

	// FAILS
	// @Resource(lookup = "NONSENSEchoicemaker/urm/jms/blockQueue")
	// @Resource(lookup = "nonsense/urm/jms/blockQueue")
	// @Resource(lookup = "nonsense/urm/jms/blockQueue")
	// WORKS
	// @Resource(lookup = "java:jboss/exported/choicemaker/urm/jms/blockQueue")
	// @Resource(lookup = "/choicemaker/urm/jms/blockQueue")
	// CURRENT
	@Resource(lookup = "choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Inject
	JMSContext jmsContext;

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
	private int initialTransitivityJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() {
		final String METHOD = "setUp";
		logEntering(METHOD);
		setupOK = true;
		try {
			initialBatchParamsCount =
				controller.findAllBatchParameters().size();
			initialBatchJobCount = controller.findAllBatchJobs().size();
			initialTransitivityJobCount =
				controller.findAllTransitivityJobs().size();
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

			int finalBatchParamsCount =
				controller.findAllBatchParameters().size();
			String alert = "initialBatchParamsCount != finalBatchParamsCount";
			assertTrue(alert, initialBatchParamsCount == finalBatchParamsCount);

			int finalBatchJobCount = controller.findAllBatchJobs().size();
			alert = "initialBatchJobCount != finalBatchJobCount";
			assertTrue(alert, initialBatchJobCount == finalBatchJobCount);

			int finalTransJobCount =
				controller.findAllTransitivityJobs().size();
			alert = "initialTransitivityJobCount != finalTransJobCount";
			assertTrue(initialTransitivityJobCount == finalTransJobCount);

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
		StartData startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(queueInfo("Clearing: ", blockQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(4)
	public void clearUpdateQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(updateQueue);
		Object o = null;
//		UpdateData updateData = null;
		do {
			o = receiveUpdateData(consumer);
			logger.finest(queueInfo("Clearing: ", updateQueue, o));
			if (o != null && !(o instanceof UpdateData)) {
				logger.severe("Wrong message type on update queue: " + o == null ? null : o.getClass().getName());
			}
		} while (o != null);
	}

	@Test
	@InSequence(4)
	public void clearSingleMatchQueue() {
		assertTrue(setupOK);
		JMSConsumer consumer = jmsContext.createConsumer(singleMatchQueue);
		StartData startData = null;
		do {
			startData = receiveStartData(consumer);
			logger.finest(queueInfo("Clearing: ", singleMatchQueue, startData));
		} while (startData != null);
	}

	@Test
	@InSequence(5)
	public void testStartOABALinkage() {
		assertTrue(setupOK);
		String TEST = "testStartOABALinkage";
		logEntering(TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		final BatchParameters bp =
				new BatchParametersBean(c.getModelConfigurationName(),
						c.getSingleRecordMatchingThreshold(), c.getThresholds()
								.getDifferThreshold(), c.getThresholds()
								.getMatchThreshold(), c.getStagingRecordSource(),
						c.getMasterRecordSource(), c.getTransitivityAnalysisFlag());
		testStartOABA(TEST, externalID, bp);

		logExiting(TEST);
	}

	@Test
	@InSequence(6)
	public void testStartOABAStage() {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		logEntering(TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		// The master record source should must be null in this set of batch
		// parameters in order to test the startOABAStage(..) method
		final ISerializableDbRecordSource MASTER = null;
		final BatchParameters bp =
			new BatchParametersBean(c.getModelConfigurationName(),
					c.getSingleRecordMatchingThreshold(), c.getThresholds()
							.getDifferThreshold(), c.getThresholds()
							.getMatchThreshold(), c.getStagingRecordSource(),
							MASTER, c.getTransitivityAnalysisFlag());
		testStartOABA(TEST, externalID, bp);

		logExiting(TEST);
	}

	public void testStartOABA(final String tag, final String externalId,
			final BatchParameters bp) {

		if (externalId == null || bp == null) {
			throw new IllegalArgumentException("null argument");
		}

		TestEntities te = new TestEntities();

		final long jobId;
		if (bp.getMasterRs() == null) {
			logger.info(tag + ": invoking BatchQueryService.startOABAStage");
			jobId =
				batchQuery.startOABAStage(externalId, bp.getStageRs(),
						bp.getLowThreshold(), bp.getHighThreshold(),
						bp.getModelConfigurationName(), bp.getMaxSingle(),
						bp.getTransitivity());
			logger.info(tag + ": returned from BatchQueryService.startOABA");
		} else {
			logger.info(tag + ": invoking BatchQueryService.startOABAStage");
			jobId =
				batchQuery.startOABA(externalId, bp.getStageRs(),
						bp.getMasterRs(), bp.getLowThreshold(),
						bp.getHighThreshold(), bp.getModelConfigurationName(),
						bp.getMaxSingle(), bp.getTransitivity());
			logger.info(tag + ": returned from BatchQueryService.startOABA");
		}
		assertTrue(BatchJobBean.INVALID_BATCHJOB_ID != jobId);
		BatchJob batchJob = em.find(BatchJobBean.class, jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalId != null
				&& externalId.equals(batchJob.getExternalId()));

		// Find the persistent BatchParameters object created by the call to
		// BatchQueryService.startOABA...
		BatchParameters params =
			EJBConfiguration.getInstance().findBatchParamsByJobId(em, jobId);
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
		assertTrue(params.getMaxSingle() == bp.getMaxSingle());
		assertTrue(params.getTransitivity() == bp.getTransitivity());

		// Check that the startOABA method completed and sent out a message
		// on the blocking queue
		logger.info("Checking blockQueue");
		JMSConsumer consumer = jmsContext.createConsumer(blockQueue);
		StartData startData = receiveStartData(consumer, LONG_TIMEOUT_MILLIS);
		logger.info(queueInfo("Received from: ", blockQueue, startData));
		if (startData == null) {
			fail("did not receive data from blocking queue");
		}
		assertTrue(startData.jobID == jobId);

		// Find the persistent OabaProcessing object updated by the StartOABA
		// message driven bean
		OabaBatchJobProcessing processingEntry =
			EJBConfiguration.getInstance().findProcessingLogByJobId(em, jobId);
		te.add(processingEntry);

		// Validate that OabaProcessing entry is correct for this stage
//		em.getEntityManagerFactory().getCache().evictAll();
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == EVT_DONE_REC_VAL);

		// Check that the startOABA method sent out a message on the update
		// queue
		logger.info("Checking updateQueue");
		consumer = jmsContext.createConsumer(updateQueue);
		Object o = 
				receiveUpdateData(consumer, SHORT_TIMEOUT_MILLIS);
		logger.info(queueInfo("Received from: ", blockQueue, o));
		if (o == null) {
			fail("did not receive data from update queue");
		}
		if (!(o instanceof UpdateData)) {
			fail("Received wrong type from update queue: " + o == null ? null
					: o.getClass().getName());
		}
		UpdateData updateData = (UpdateData) o;
		assertTrue(updateData.getJobID() == jobId);
		assertTrue(updateData.getPercentComplete() == OabaProcessing.PCT_DONE_REC_VAL);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}

		logExiting(tag);
	}

	public StartData receiveStartData(JMSConsumer consumer) {
		return receiveStartData(consumer, SHORT_TIMEOUT_MILLIS);
	}

	public StartData receiveStartData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveStartData(" + timeOut + ")";
		logEntering(METHOD);
		StartData retVal = null;
		try {
			retVal = consumer.receiveBody(StartData.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logExiting(METHOD);
		return retVal;
	}

//	public UpdateData receiveUpdateData(JMSConsumer consumer) {
	public Object receiveUpdateData(JMSConsumer consumer) {
		return receiveUpdateData(consumer, SHORT_TIMEOUT_MILLIS);
	}

//	public UpdateData receiveUpdateData(JMSConsumer consumer, long timeOut) {
	public Object receiveUpdateData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveUpdateData(" + timeOut + ")";
		logEntering(METHOD);
		if (consumer == null) {
			throw new IllegalArgumentException("null consumer");
		}
//		UpdateData retVal = null;
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
