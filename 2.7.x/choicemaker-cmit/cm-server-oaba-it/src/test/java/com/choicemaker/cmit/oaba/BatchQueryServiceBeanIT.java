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
import static com.choicemaker.cmit.utils.EntityManagerUtils.MAX_MAX_SINGLE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
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

import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchQueryService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.StartOABA;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class BatchQueryServiceBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public static final String REGEX_OABA_SERVER =
		"com.choicemaker.cm.io.blocking.automated.offline.server.*.jar";

	public static final long QUEUE_RECEIVE_TIMEOUT = 1000;

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * StartOABA message bean. This allows another class to attach to the
	 * startQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);

		File[] libs = resolveDependencies(pom);

		// Filter the OABA server JAR from the dependencies
		final Pattern p = Pattern.compile(REGEX_OABA_SERVER);
		File oabaServerJAR = null;
		List<File> filteredLibs = new LinkedList<>();
		for (File lib : libs) {
			String name = lib.getName();
			Matcher m = p.matcher(name);
			if (m.matches()) {
				if (oabaServerJAR == null) {
					oabaServerJAR = lib;
				} else {
					String firstPath = oabaServerJAR.getAbsolutePath();
					String secondPath = lib.getAbsolutePath();
					throw new RuntimeException(
							"multiple OABA server JAR files: " + firstPath
									+ ", " + secondPath);
				}
			} else {
				filteredLibs.add(lib);
			}
		}
		File[] libs2 = filteredLibs.toArray(new File[filteredLibs.size()]);

		// Filter the BatchParamtersBean class from the oaba Server JAR
		final String path =
			"/" + StartOABA.class.getName().replace('.', '/') + ".class";
		JavaArchive filteredOabaServerJAR =
			ShrinkWrap.createFromZipFile(JavaArchive.class, oabaServerJAR);
		filteredOabaServerJAR.delete(path);

		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs2, TESTS_AS_EJB_MODULE);
		retVal.addAsModule(filteredOabaServerJAR);
		return retVal;
	}

	private static final Logger logger = Logger
			.getLogger(BatchQueryServiceBeanIT.class.getName());

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	protected BatchQueryService batchQuery;

	@EJB
	protected TransitivityJobController controller;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Inject
	JMSContext jmsContext;

	private final Random random = new Random(new Date().getTime());

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
	private int initialTransitivityJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() {
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
	}

	@After
	public void tearDown() {
		try {

			int finalBatchParamsCount =
				controller.findAllBatchParameters().size();
			assertTrue(initialBatchParamsCount == finalBatchParamsCount);

			int finalBatchJobCount = controller.findAllBatchJobs().size();
			assertTrue(initialBatchJobCount == finalBatchJobCount);

			int finalTransJobCount =
				controller.findAllTransitivityJobs().size();
			assertTrue(initialTransitivityJobCount == finalTransJobCount);

			int finalOabaProcessingCount =
				controller.findAllOabaProcessing().size();
			assertTrue(initialOabaProcessingCount == finalOabaProcessingCount);

		} catch (Exception x) {
			logger.severe(x.toString());
		} catch (AssertionError x) {
			logger.severe(x.toString());
		}
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
	public void testBatchQuery() {
		assertTrue(setupOK);
		assertTrue(startQueue != null);
	}

	@Test
	@InSequence(2)
	public void testQueue() {
		assertTrue(setupOK);
		assertTrue(batchQuery != null);
	}

	@Test
	@InSequence(3)
	public void testJmsContext() {
		assertTrue(setupOK);
		assertTrue(jmsContext != null);
	}

	@Test
	@InSequence(4)
	public void clearStartQueue() {
		assertTrue(setupOK);
		StartData startData = null;
		do {
			startData = receiveStartData();
		} while (startData != null);
	}

	public static BatchParameters createRandomStartData(Random random,
			String tag) {
		if (random == null) {
			random = new Random();
		}
		String STAGING = tag == null ? "STAGING" : tag + "_STAGING";
		String MASTER = tag == null ? "MASTER" : tag + "_MASTER";
		final SerializableRecordSource staging =
			EntityManagerUtils.createFakeSerialRecordSource(STAGING);
		final SerializableRecordSource master =
			EntityManagerUtils.createFakeSerialRecordSource(MASTER);
		final Thresholds thresholds =
			EntityManagerUtils.createRandomThresholds();
		final String modelConfigName = UUID.randomUUID().toString();
		final int maxSingle = random.nextInt(MAX_MAX_SINGLE);
		final boolean runTransitivity = random.nextBoolean();
		BatchParameters retVal =
			new BatchParametersBean(modelConfigName, maxSingle,
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), staging, master,
					runTransitivity);
		return retVal;
	}

	@Test
	@InSequence(5)
	public void testStartOABALinkage() {
		assertTrue(setupOK);
		String TEST = "testStartOABALinkage";
		TestEntities te = new TestEntities();
		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final BatchParameters bp = createRandomStartData(random, TEST);
		long jobId =
			batchQuery.startOABA(externalID, bp.getStageRs(), bp.getMasterRs(),
					bp.getLowThreshold(), bp.getHighThreshold(),
					bp.getModelConfigurationName(), bp.getMaxSingle(),
					bp.getTransitivity());
		assertTrue(BatchParametersBean.INVALID_PARAMSID != jobId);
		BatchJob batchJob = em.find(BatchJobBean.class, jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalID != null
				&& externalID.equals(batchJob.getExternalId()));

		StartData startData = receiveStartData();
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
		assertTrue(processingEntry.getCurrentProcessingEvent() == OabaProcessing.INIT);
		te.add(processingEntry);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	@InSequence(6)
	public void testStartOABAStage() {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		TestEntities te = new TestEntities();
		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final BatchParameters bp = createRandomStartData(random, TEST);
		long jobId =
			batchQuery.startOABAStage(externalID, bp.getStageRs(),
					bp.getLowThreshold(), bp.getHighThreshold(),
					bp.getModelConfigurationName(), bp.getMaxSingle(),
					bp.getTransitivity());
		assertTrue(BatchParametersBean.INVALID_PARAMSID != jobId);
		BatchJob batchJob = em.find(BatchJobBean.class, jobId);
		assertTrue(batchJob != null);
		te.add(batchJob);
		assertTrue(externalID != null
				&& externalID.equals(batchJob.getExternalId()));

		StartData startData = receiveStartData();
		if (startData == null) {
			fail("did not receive start data");
		}
		assertTrue(startData.jobID == jobId);

		BatchParameters params =
			EJBConfiguration.getInstance().findBatchParamsByJobId(em, jobId);
		assertTrue(params != null);
		assertTrue(params != null);
		te.add(params);
		assertTrue(params.getLowThreshold() == bp.getLowThreshold());
		assertTrue(params.getHighThreshold() == bp.getHighThreshold());
		// assertTrue(params.getMasterRs() != null &&
		// params.getMasterRs().equals(bp.getMasterRs()));
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
		assertTrue(processingEntry.getCurrentProcessingEvent() == OabaProcessing.INIT);
		te.add(processingEntry);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	public StartData receiveStartData() {
		StartData retVal = null;
		try {
			retVal =
				jmsContext.createConsumer(startQueue).receiveBody(
						StartData.class, QUEUE_RECEIVE_TIMEOUT);
		} catch (Exception x) {
			fail(x.toString());
		}
		return retVal;
	}

}
