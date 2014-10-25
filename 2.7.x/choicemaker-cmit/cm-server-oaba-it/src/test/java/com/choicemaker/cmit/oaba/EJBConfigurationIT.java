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

import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class EJBConfigurationIT {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH,
					PERSISTENCE_CONFIGURATION, DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

//	@BeforeClass public static void setUpBeforeClass() throws Exception {}
//	@AfterClass public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {
		this.ejbc = EJBConfiguration.getInstance();
		assertTrue(this.ejbc != null);
	}

	@After
	public void tearDown() throws Exception {
		this.ejbc = null;
	}
	
	@PersistenceUnit(unitName = "oaba-local")
	private EntityManagerFactory emf;

	private EJBConfiguration ejbc;

	@Test
	public void testEBJConfiguration() {
		assertTrue(ejbc != null);
	}

	@Test
	public void testGetEntityManager() {
		assertTrue(emf != null);
	}

	@Test
	public void testGetInitialContext() {
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
	public void testCreateFindRemove() {
		final EntityManager em = emf.createEntityManager();
		assertTrue(em != null);
		em.getTransaction().begin();

		// Count existing jobs
		final int initialCount = ejbc.findAllBatchJobs(em).size();

		// Create a job
		final String extId = "EXT ID: " + new Date().toString();
		BatchJob job = ejbc.createBatchJob(em, extId);
		assertTrue(job != null);
		assertTrue(job.getId() != 0);

		// Recount the jobs
		final int intermediateCount = ejbc.findAllBatchJobs(em).size();
		assertTrue(intermediateCount == initialCount + 1);

		// Find the job
		BatchJob job2 = ejbc.findBatchJobById(em, BatchJobBean.class, job.getId());
		assertTrue(job.getId() == job2.getId());
		assertTrue(job.equals(job2));

		// Delete the job
		ejbc.deleteBatchJob(em, job2);
		BatchJob job3 = ejbc.findBatchJobById(em, BatchJobBean.class, job.getId());
		assertTrue(job3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == ejbc.findAllBatchJobs(em).size());

		em.getTransaction().commit();
		em.close();
	}

	/*
	 * @Test public void testCreateStatusLog() { fail("Not yet implemented"); }
	 * 
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
	 * @Test public void testFindStatusLogById() { fail("Not yet implemented");
	 * }
	 * 
	 * @Test public void testGetDataSource() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetStatusLogStartData() {
	 * fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetStatusLogLong() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testCreateNewStatusLog() { fail("Not yet implemented");
	 * }
	 */

}
