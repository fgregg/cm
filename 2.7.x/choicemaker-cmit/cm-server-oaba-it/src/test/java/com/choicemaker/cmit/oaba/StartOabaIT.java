package com.choicemaker.cmit.oaba;

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
import java.util.concurrent.Callable;
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
//	@Resource(lookup = "NONSENSEchoicemaker/urm/jms/blockQueue")
//	@Resource(lookup = "nonsense/urm/jms/blockQueue")
//	@Resource(lookup = "nonsense/urm/jms/blockQueue")
//	WORKS
//	@Resource(lookup = "java:jboss/exported/choicemaker/urm/jms/blockQueue")
//	@Resource(lookup = "/choicemaker/urm/jms/blockQueue")
//	CURRENT
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

	// @Test
	// public void testHello() {
	// System.out.println("HELLO ORIG");
	// assertTrue(setupOK);
	// for (int i=0; i<10; i++) {
	// System.out.println("HELLO ORIG " + i);
	// }
	// }

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
		UpdateData updateData = null;
		do {
			updateData = receiveUpdateData(consumer);
			logger.finest(queueInfo("Clearing: ", updateQueue, updateData));
		} while (updateData != null);
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

		TestEntities te = new TestEntities();
		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(e2service.getPluginRegistry());
		final BatchParameters bp =
			new BatchParametersBean(c.getModelConfigurationName(),
					c.getSingleRecordMatchingThreshold(), c.getThresholds()
							.getDifferThreshold(), c.getThresholds()
							.getMatchThreshold(), c.getStagingRecordSource(),
					c.getMasterRecordSource(), c.getTransitivityAnalysisFlag());
		logger.info(TEST + ": invoking BatchQueryService.startOABA");
		long jobId =
			batchQuery.startOABA(externalID, bp.getStageRs(), bp.getMasterRs(),
					bp.getLowThreshold(), bp.getHighThreshold(),
					bp.getModelConfigurationName(), bp.getMaxSingle(),
					bp.getTransitivity());
		logger.info(TEST + ": returned from BatchQueryService.startOABA");
		assertTrue(BatchParametersBean.INVALID_PARAMSID != jobId);
		BatchJob batchJob = em.find(BatchJobBean.class, jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalID != null
				&& externalID.equals(batchJob.getExternalId()));

		logger.info("Checking blockQueue");
		JMSConsumer consumer = jmsContext.createConsumer(blockQueue);
		StartData startData = receiveStartData(consumer, LONG_TIMEOUT_MILLIS);
		logger.info(queueInfo("Received from: ", blockQueue, startData));
		if (startData == null) {
			fail("did not receive start data");
		}
		assertTrue(startData.jobID == jobId);

		BatchParameters params =
			EJBConfiguration.getInstance().findBatchParamsByJobId(em, jobId);
		assertTrue(params != null);
		te.add(params);
		assertTrue(params.getLowThreshold() == bp.getLowThreshold());
		assertTrue(params.getHighThreshold() == bp.getHighThreshold());
		assertTrue(params.getMasterRs() != null
				&& params.getMasterRs().equals(bp.getMasterRs()));
		assertTrue(params.getStageRs() != null
				&& params.getStageRs().equals(bp.getStageRs()));
		assertTrue(params.getModelConfigurationName() != null
				&& params.getModelConfigurationName().equals(
						bp.getModelConfigurationName()));
		assertTrue(params.getMaxSingle() == bp.getMaxSingle());
		assertTrue(params.getTransitivity() == bp.getTransitivity());

		OabaBatchJobProcessing processingEntry =
			EJBConfiguration.getInstance().findProcessingLogByJobId(em, jobId);
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEvent() == OabaProcessing.DONE_REC_VAL);
		te.add(processingEntry);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}

		logExiting(TEST);
	}

	// @Test
	// @InSequence(6)
	// public void testStartOABAStage() {
	// assertTrue(setupOK);
	// String TEST = "testStartOABAStage";
	// TestEntities te = new TestEntities();
	// final String externalID = EntityManagerUtils.createExternalId(TEST);
	// final SimplePersonSqlServerTestConfiguration c =
	// new SimplePersonSqlServerTestConfiguration();
	// c.initialize(this.e2service.getPluginRegistry());
	//
	// // The master record source should must be null in this set of batch
	// // parameters since it will be compared against the parameters created
	// // by the startOABAStage, which will create another set of parameters
	// // (in which the master records is automatically set to null)
	// final SerializableRecordSource MASTER_SOURCE = null;
	// final BatchParameters bp =
	// new BatchParametersBean(c.getModelConfigurationName(),
	// c.getSingleRecordMatchingThreshold(), c.getThresholds()
	// .getDifferThreshold(), c.getThresholds()
	// .getMatchThreshold(), c.getStagingRecordSource(),
	// MASTER_SOURCE, c.getTransitivityAnalysisFlag());
	//
	// long jobId =
	// batchQuery.startOABAStage(externalID, bp.getStageRs(),
	// bp.getLowThreshold(), bp.getHighThreshold(),
	// bp.getModelConfigurationName(), bp.getMaxSingle(),
	// bp.getTransitivity());
	// assertTrue(BatchParametersBean.INVALID_PARAMSID != jobId);
	// BatchJob batchJob = em.find(BatchJobBean.class, jobId);
	// assertTrue(batchJob != null);
	// te.add(batchJob);
	// assertTrue(externalID != null
	// && externalID.equals(batchJob.getExternalId()));
	//
	// StartData startData = receiveBlockData();
	// if (startData == null) {
	// fail("did not receive start data");
	// }
	// assertTrue(startData.jobID == jobId);
	//
	// BatchParameters params =
	// EJBConfiguration.getInstance().findBatchParamsByJobId(em, jobId);
	// assertTrue(params != null);
	// assertTrue(params != null);
	// te.add(params);
	// assertTrue(params.getLowThreshold() == bp.getLowThreshold());
	// assertTrue(params.getHighThreshold() == bp.getHighThreshold());
	// // assertTrue(params.getMasterRs() != null &&
	// // params.getMasterRs().equals(bp.getMasterRs()));
	// assertTrue(params.getStageRs() != null
	// && params.getStageRs().equals(bp.getStageRs()));
	// assertTrue(params.getModelConfigurationName() != null
	// && params.getModelConfigurationName().equals(
	// bp.getModelConfigurationName()));
	// assertTrue(params.getMaxSingle() == bp.getMaxSingle());
	// assertTrue(params.getTransitivity() == bp.getTransitivity());
	//
	// OabaBatchJobProcessing processingEntry =
	// EJBConfiguration.getInstance().findProcessingLogByJobId(em, jobId);
	// assertTrue(processingEntry != null);
	// assertTrue(processingEntry.getCurrentProcessingEvent() ==
	// OabaProcessing.INIT);
	// te.add(processingEntry);
	//
	// try {
	// te.removePersistentObjects(em, utx);
	// } catch (Exception x) {
	// logger.severe(x.toString());
	// fail(x.toString());
	// }
	// }

	static class CallableStartData implements Callable<StartData> {
		private final JMSConsumer consumer;
		private final long timeOut;

		public CallableStartData(JMSConsumer c, long to) {
			if (c == null) {
				throw new IllegalArgumentException("null consumer");
			}
			if (to < 0) {
				throw new IllegalArgumentException("negative timeout: " + to);
			}
			this.consumer = c;
			this.timeOut = to;
		}

		public StartData call() {
			StartData retVal = null;
			try {
				retVal = consumer.receiveBody(StartData.class, timeOut);
			} catch (Exception x) {
				fail(x.toString());
			}
			return retVal;
		}

	}

	public StartData receiveStartData(JMSConsumer consumer) {
		return receiveStartData(consumer, SHORT_TIMEOUT_MILLIS);
	}

	public StartData receiveStartData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveStartData(" + timeOut + ")";
		logEntering(METHOD);
		StartData retVal = null;
		// CallableStartData csd = new CallableStartData(consumer, timeOut);
		// Future<StartData> fsd = executor.submit(csd);
		// try {
		// retVal = fsd.get();
		// } catch (InterruptedException | ExecutionException x) {
		// fail(x.toString());
		// }
		try {
			retVal = consumer.receiveBody(StartData.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logExiting(METHOD);
		return retVal;
	}

	public UpdateData receiveUpdateData(JMSConsumer consumer) {
		return receiveUpdateData(consumer, SHORT_TIMEOUT_MILLIS);
	}

	public UpdateData receiveUpdateData(JMSConsumer consumer, long timeOut) {
		final String METHOD = "receiveUpdateData(" + timeOut + ")";
		logEntering(METHOD);
		if (consumer == null) {
			throw new IllegalArgumentException("null consumer");
		}
		UpdateData retVal = null;
		try {
			retVal = consumer.receiveBody(UpdateData.class, timeOut);
		} catch (Exception x) {
			fail(x.toString());
		}
		logExiting(METHOD);
		return retVal;
	}

}
