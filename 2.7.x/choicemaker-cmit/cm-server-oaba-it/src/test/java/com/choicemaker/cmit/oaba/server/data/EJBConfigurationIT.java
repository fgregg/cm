package com.choicemaker.cmit.oaba.server.data;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

//@RunWith(Arquillian.class)
public class EJBConfigurationIT {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

//	@Deployment
//	public static EnterpriseArchive createEarArchive() {
//		List<Class<?>> testClasses = new ArrayList<>();
//		testClasses.add(EJBConfigurationIT.class);
//		testClasses.add(OabaUtils.class);
//
//		JavaArchive ejb =
//			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
//					testClasses, PERSISTENCE_CONFIGURATION);
//
//		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);
//
//		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
//		return retVal;
//	}

//	private EJBConfiguration ejbc;

//	@Before
//	public void setUp() throws Exception {
//		this.ejbc = EJBConfiguration.getInstance();
//		assertTrue(this.ejbc != null);
//	}
//
//	@After
//	public void tearDown() throws Exception {
//		this.ejbc = null;
//	}
	
	@Test
	public void testEBJConfiguration() {
//		assertTrue(ejbc != null);
	}

//	@Test
//	public void testGetEntityManager() {
//		EntityManager em = ejbc.getEntityManager();
//		assertTrue(em != null);
//	}
//
//	@Test
//	public void testGetInitialContext() {
//		try {
//			Context ic = ejbc.getInitialContext();
//			assertTrue(ic != null);
//		} catch (NamingException e) {
//			fail(e.toString());
//		}
//	}

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

//	@Test
//	public void testCreateFindRemove() {
//		// Count existing jobs
//		final int initialCount = ejbc.findAllBatchJobs().size();
//
//		// Create a job
//		final String extId = "EXT ID: " + new Date().toString();
//		BatchJob job = ejbc.createBatchJob(extId);
//		assertTrue(job != null);
//		assertTrue(job.getId() != 0);
//
//		// Recount the jobs
//		final int intermediateCount = ejbc.findAllBatchJobs().size();
//		assertTrue(intermediateCount == initialCount + 1);
//
//		// Find the job
//		BatchJob job2 = ejbc.findBatchJobById(job.getId());
//		assertTrue(job.getId() == job2.getId());
//		assertTrue(job.equals(job2));
//
//		// Delete the job
//		ejbc.deleteBatchJob(job2);
//		BatchJob job3 = ejbc.findBatchJobById(job.getId());
//		assertTrue(job3 == null);
//
//		// Check that the number of existing jobs equals the initial count
//		assertTrue(initialCount == ejbc.findAllBatchJobs().size());
//	}

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
