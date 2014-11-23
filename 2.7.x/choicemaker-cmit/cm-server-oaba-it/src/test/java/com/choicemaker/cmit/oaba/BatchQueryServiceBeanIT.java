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
import java.util.Random;
import java.util.Set;
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

import com.choicemaker.cm.batch.impl.BatchJobJPA;
import com.choicemaker.cm.core.SerializableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.StartOABA;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.cmit.utils.TestEntities;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class BatchQueryServiceBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public static final String REGEX_EJB_DEPENDENCIES =
			"com.choicemaker.cm.io.blocking.automated.offline.server.*.jar"
					+ "|com.choicemaker.e2.ejb.*.jar";

	public static final long QUEUE_RECEIVE_TIMEOUT = 1000;

	public static final String[] removedPaths() {
		Class<?>[] removedClasses = new Class<?>[] { StartOABA.class };
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
	 * StartOABA message bean. This allows another class to attach to the
	 * startQueue for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);

		File[] libs = resolveDependencies(pom);

		// Filter the OABA server JAR from the dependencies
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
		final EnterpriseArchive retVal =
			createEAR(tests, libs2, TESTS_AS_EJB_MODULE);

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

	private static final Logger logger = Logger
			.getLogger(BatchQueryServiceBeanIT.class.getName());

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

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
	EjbPlatform e2service;

	@EJB
	protected OabaService batchQuery;

	@EJB
	protected TestController controller;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Inject
	JMSContext jmsContext;

//	private final Random random = new Random(new Date().getTime());

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() {
		setupOK = true;
		try {
			initialBatchParamsCount =
				controller.findAllOabaParameters().size();
			initialBatchJobCount = controller.findAllOabaJobs().size();
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
				controller.findAllOabaParameters().size();
			assertTrue(initialBatchParamsCount == finalBatchParamsCount);

			int finalBatchJobCount = controller.findAllOabaJobs().size();
			assertTrue(initialBatchJobCount == finalBatchJobCount);

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
		OabaJobMessage oabaJobMessage = null;
		do {
			oabaJobMessage = receiveStartData();
		} while (oabaJobMessage != null);
	}

	public static OabaParameters createRandomStartData(Random random,
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
		OabaParameters retVal =
			new OabaParametersEntity(modelConfigName,
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), staging, master);
		return retVal;
	}

	@Test
	@InSequence(5)
	public void testStartLinkage()
			throws ServerConfigurationException {
		assertTrue(setupOK);
		String TEST = "testStartOABALinkage";
		TestEntities te = new TestEntities();

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), c.getStagingRecordSource(),
					c.getMasterRecordSource());
		
		final long jobId =
			batchQuery.startLinkage(externalID, bp.getStageRs(), bp.getMasterRs(),
					bp.getLowThreshold(), bp.getHighThreshold(),
					bp.getModelConfigurationName());
		assertTrue(BatchJobJPA.INVALID_ID != jobId);

		OabaJob oabaJob = jobController.find(jobId);
		assertTrue(oabaJob != null);
		te.add(oabaJob);
		assertTrue(externalID != null
				&& externalID.equals(oabaJob.getExternalId()));

		OabaJobMessage oabaJobMessage = receiveStartData();
		if (oabaJobMessage == null) {
			fail("did not receive start data");
		}
		assertTrue(oabaJobMessage.jobID == jobId);

		OabaParameters params = paramsController.findBatchParamsByJobId(jobId);
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

		OabaJobProcessing processingEntry =
			processingController.findProcessingLogByJobId(jobId);
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == OabaProcessing.EVT_INIT);
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
	public void testStartDeduplication()
			throws ServerConfigurationException {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		TestEntities te = new TestEntities();
		
		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), c.getStagingRecordSource(),
					c.getMasterRecordSource());
		
		final long jobId =
			batchQuery.startDeduplication(externalID, bp.getStageRs(),
					bp.getLowThreshold(), bp.getHighThreshold(),
					bp.getModelConfigurationName());
		assertTrue(BatchJobJPA.INVALID_ID != jobId);

		OabaJob oabaJob = jobController.find(jobId);
		assertTrue(oabaJob != null);
		te.add(oabaJob);
		assertTrue(externalID != null
				&& externalID.equals(oabaJob.getExternalId()));

		OabaJobMessage oabaJobMessage = receiveStartData();
		if (oabaJobMessage == null) {
			fail("did not receive start data");
		}
		assertTrue(oabaJobMessage.jobID == jobId);

		OabaParameters params = paramsController.findBatchParamsByJobId(jobId);
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

		OabaJobProcessing processingEntry =
			processingController.findProcessingLogByJobId(jobId);
		assertTrue(processingEntry != null);
		assertTrue(processingEntry.getCurrentProcessingEventId() == OabaProcessing.EVT_INIT);
		te.add(processingEntry);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	public OabaJobMessage receiveStartData() {
		OabaJobMessage retVal = null;
		try {
			retVal =
				jmsContext.createConsumer(startQueue).receiveBody(
						OabaJobMessage.class, QUEUE_RECEIVE_TIMEOUT);
		} catch (Exception x) {
			fail(x.toString());
		}
		return retVal;
	}

}
