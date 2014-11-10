package com.choicemaker.cmit.oaba;

import static com.choicemaker.cmit.oaba.util.OabaConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaConstants.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.utils.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolvePom;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaBatchJobProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cmit.utils.DeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class EJBConfigurationIT {

	private static final Logger logger = Logger.getLogger(EJBConfigurationIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

	// @BeforeClass public static void setUpBeforeClass() throws Exception {}
	// @AfterClass public static void tearDownAfterClass() throws Exception {}

	@PersistenceUnit(unitName = "oaba-local")
	private EntityManagerFactory emf;

	private EJBConfiguration ejbc;
	private int initialBatchParamsCount;
	private int initialBatchJobCount;
	private int initialTransitivityJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() throws Exception {
		System.out.println("setUp");
		setupOK = true;
		try {
			this.ejbc = EJBConfiguration.getInstance();
			assertTrue(this.ejbc != null);

			final EntityManager em = emf.createEntityManager();
			initialBatchParamsCount =
				EntityManagerUtils.findAllBatchParameters(em).size();
			initialBatchJobCount =
				EntityManagerUtils.findAllBatchJobs(em).size();
			initialTransitivityJobCount =
				EntityManagerUtils.findAllTransitivityJobs(em).size();
			initialOabaProcessingCount =
				EntityManagerUtils.findAllOabaProcessing(em).size();
		} catch (Exception x) {
			logger.severe(x.toString());
			setupOK = false;
		}
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("tearDown");
		this.ejbc = null;

		try {
			final EntityManager em = emf.createEntityManager();
			int finalBatchParamsCount =
				EntityManagerUtils.findAllBatchParameters(em).size();
			assertTrue(initialBatchParamsCount == finalBatchParamsCount);

			int finalBatchJobCount = EntityManagerUtils.findAllBatchJobs(em).size();
			assertTrue(initialBatchJobCount == finalBatchJobCount);

			int finalTransJobCount =
				EntityManagerUtils.findAllTransitivityJobs(em).size();
			assertTrue(initialTransitivityJobCount == finalTransJobCount);

			int finalOabaProcessingCount =
				EntityManagerUtils.findAllOabaProcessing(em).size();
			assertTrue(initialOabaProcessingCount == finalOabaProcessingCount);
		} catch (Exception x) {
			logger.severe(x.toString());
		} catch (AssertionError x) {
			logger.severe(x.toString());
		}
	}

	@Test
	public void testEBJConfiguration() {
		assertTrue(setupOK);
		System.out.println("testEBJConfiguration");
		assertTrue(ejbc != null);
	}

	@Test
	public void testGetEntityManager() {
		assertTrue(setupOK);
		System.out.println("testGetEntityManager");
		assertTrue(emf != null);
	}

	@Test
	public void testGetInitialContext() {
		assertTrue(setupOK);
		System.out.println("testGetInitialContext");
		try {
			Context ic = ejbc.getInitialContext();
			assertTrue(ic != null);
		} catch (NamingException e) {
			fail(e.toString());
		}
	}

	/*
	 * @Test public void testGetTopicConnectionFactory() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetStatusTopic() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransStatusTopic() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetStartMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetBlockingMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetDedupMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetChunkMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetMatchingMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetMatchDedupMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetMatchDedupEachMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetMatchSchedulerMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetMatcherMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransMatchSchedulerMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransMatcherMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransMatchDedupMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransMatchDedupEachMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransitivityMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetUpdateMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetUpdateTransMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetSingleMatchMessageQueue() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testSendMessage() { fail("Not yet implemented"); }
	 */

	@Test
	public void testCreateFindRemoveBatchJob() {
		assertTrue(setupOK);
		System.out.println("testCreateFindRemoveBatchJob");
		final String METHOD = "testCreateFindRemove";
		final TestEntities te = new TestEntities();
		final EntityManager em = emf.createEntityManager();
		assertTrue(em != null);
		em.getTransaction().begin();

		// Count existing jobs
		final int initialCount = ejbc.findAllBatchJobs(em).size();

		// Create a job
		final BatchParameters params =
			EntityManagerUtils.createPersistentBatchParameters(em, METHOD, te);
		final String extId = "EXT ID: " + new Date().toString();
		BatchJob job = ejbc.createBatchJob(em, params, extId);
		assertTrue(job != null);
		assertTrue(job.getId() != 0);

		// Recount the jobs
		final int intermediateCount = ejbc.findAllBatchJobs(em).size();
		assertTrue(intermediateCount == initialCount + 1);

		// Find the job
		BatchJob job2 =
			ejbc.findBatchJobById(em, BatchJobBean.class, job.getId());
		assertTrue(job.getId() == job2.getId());
		assertTrue(job.equals(job2));

		// Delete the job
		ejbc.deleteBatchJob(em, job2);
		BatchJob job3 =
			ejbc.findBatchJobById(em, BatchJobBean.class, job.getId());
		assertTrue(job3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == ejbc.findAllBatchJobs(em).size());

		EntityManagerUtils.removeTestEntities(em, te);
		em.getTransaction().commit();
		em.close();
	}

	/*
	 * @Test public void testCreateBatchParameters() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetConnection() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testFindBatchParamsById() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testFindTransitivityJobById() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetTransitivityJob() { fail("Not yet implemented");
	 * }
	 * 
	 * @Test public void testGetDataSource() { fail("Not yet implemented"); }
	 */

	@Test
	public void testProcessingLog() {
		assertTrue(setupOK);
		System.out.println("testProcessingLog");
		final String METHOD = "testCreateFindRemove";
		EntityManager em = null;
		
		try {
			em = emf.createEntityManager();

			final TestEntities te = new TestEntities();
			assertTrue(em != null);
			em.getTransaction().begin();

			// Create a new processing entry
			BatchJob job =
				EntityManagerUtils.createPersistentBatchJobBean(em, METHOD, te);
			Date before = new Date();
			OabaBatchJobProcessing objp = ejbc.createProcessingLog(em, job.getId());
			te.add(objp);
			Date after = new Date();

			assertTrue(OabaBatchJobProcessing.INVALID_ID != objp.getId());

			Date ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_INIT == objp.getCurrentProcessingEventId());
			assertTrue(null == objp.getAdditionalInfo());

			// Set the status and additional info of the entry
			final String info0 = "nonsense0";
			before = new Date();
			objp.setCurrentProcessingEvent(OabaEvent.ALLOCATE_CHUNKS, info0);
			after = new Date();

			ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_ALLOCATE_CHUNKS == objp
					.getCurrentProcessingEventId());
			assertTrue(info0 == objp.getAdditionalInfo());

			// Change the status and additional info
			before = new Date();
			objp.setCurrentProcessingEvent(OabaEvent.BLOCK_BY_ONE_COLUMN);
			after = new Date();

			ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_BLOCK_BY_ONE_COLUMN == objp
					.getCurrentProcessingEventId());
			assertTrue(null == objp.getAdditionalInfo());

			// Change the status and additional info yet again
			final String info1 = "nonsense1";
			before = new Date();
			objp.setCurrentProcessingEvent(OabaEvent.CREATE_CHUNK_IDS, info1);
			after = new Date();

			ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_CREATE_CHUNK_IDS == objp
					.getCurrentProcessingEventId());
			assertTrue(info1 == objp.getAdditionalInfo());

			// Commit the transaction
			em.getTransaction().commit();
			objp = null;

			// Verify the processing entry was preserved
			em.getTransaction().begin();
			objp = ejbc.findProcessingLogByJobId(em, job.getId());
			assertTrue(objp != null);

			ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_CREATE_CHUNK_IDS == objp
					.getCurrentProcessingEventId());
			assertTrue(info1 == objp.getAdditionalInfo());

			// Commit the transaction
			em.getTransaction().commit();

			// Verify a second entry is not created
			em.getTransaction().begin();
			final long id = objp.getId();
			objp = null;
			objp = ejbc.createProcessingLog(em, job.getId());
			assertTrue(objp != null);
			assertTrue(id == objp.getId());

			ts = objp.getTimestamp();
			assertTrue(before.compareTo(ts) <= 0);
			assertTrue(after.compareTo(ts) >= 0);

			assertTrue(job.getId() == objp.getJobId());
			assertTrue(OabaProcessing.EVT_CREATE_CHUNK_IDS == objp
					.getCurrentProcessingEventId());
			assertTrue(info1 == objp.getAdditionalInfo());

			// Clean up
			EntityManagerUtils.removeTestEntities(em, te);
			em.getTransaction().commit();
			em.close();			
		} catch(Exception x) {
			logger.severe(x.toString());
		} finally {
			if (em != null ) {
				try {
					if (em.isOpen()) {
						em.close();
					}
				} catch (Exception e) {
					logger.severe(e.toString());
				}
			}
		}
	}

}
