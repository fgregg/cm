package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cmit.OabaTestController;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.BatchJobUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class OabaJobEntityIT {

	private static final Logger logger = Logger.getLogger(OabaJobEntityIT.class
			.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	private final static String LOG_SOURCE = OabaJobEntityIT.class
			.getSimpleName();

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final int MAX_SINGLE_LIMIT = 1000;

	public static final int MAX_TEST_ITERATIONS = 10;

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	private OabaJobControllerBean oabaController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	protected OabaTestController oabaTestController;

	private int initialOabaJobCount;
	private int initialOabaParamsCount;

	private final Random random = new Random(new Date().getTime());

	@Before
	public void setUp() {
		initialOabaJobCount = oabaTestController.findAllOabaJobs().size();
		initialOabaParamsCount = oabaTestController.findAllOabaParameters().size();
	}

	@After
	public void tearDown() {
		String METHOD = "tearDown";
		logger.entering(LOG_SOURCE, METHOD);
		if (!TestEntities.isTestObjectRetentionRequested()) {
			int finalOabaJobCount = oabaTestController.findAllOabaJobs().size();
			assertTrue(initialOabaJobCount == finalOabaJobCount);

			int finalOabaParamsCount =
				oabaTestController.findAllOabaParameters().size();
			assertTrue(initialOabaParamsCount == finalOabaParamsCount);
		} else {
			logger.info("Skipping check of final object counts");
		}
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(oabaController != null);
		assertTrue(serverController != null);
		assertTrue(oabaTestController != null);
	}

	@Test
	public void testConstruction() {
		final String METHOD = "testConstruction";
		final TestEntities te = new TestEntities();

		Date now = new Date();
		OabaJobEntity job = createEphemeralOabaJobEntity(te, METHOD, true);
		Date now2 = new Date();

		assertTrue(0 == job.getId());

		assertTrue(BatchJobStatus.NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(BatchJobStatus.NEW);
		assertTrue(d.equals(d2));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";
		final TestEntities te = new TestEntities();

		// Create a job
		OabaJob job = createEphemeralOabaJobEntity(te, METHOD, true);
		assertTrue(job.getId() == 0);

		// Save the job
		oabaController.save(job);
		assertTrue(job.getId() != 0);

		// Find the job
		OabaJob batchJob2 = oabaController.findOabaJob(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		oabaController.delete(batchJob2);
		OabaJob batchJob3 = oabaController.findOabaJob(job.getId());
		assertTrue(batchJob3 == null);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testFindAll() {
		final String METHOD = "testFindAll";
		final TestEntities te = new TestEntities();

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			OabaJob job = createEphemeralOabaJobEntity(te, METHOD, true);
			assertTrue(job.getId() == 0);
			oabaController.save(job);
			te.add(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<OabaJob> jobs = oabaController.findAll();
		assertTrue(jobs != null);

		// Find the jobs
		boolean isFound = false;
		for (long jobId : jobIds) {
			for (OabaJob job : jobs) {
				if (jobId == job.getId()) {
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testExternalId() {
		final String METHOD = "testExternalId";
		final TestEntities te = new TestEntities();

		// Create a job and set a value
		String extId = EntityManagerUtils.createExternalId(METHOD);
		boolean isTag = false;
		OabaJob job = createEphemeralOabaJobEntity(te, extId, isTag);
		assertTrue(extId.equals(job.getExternalId()));

		// Save the job
		final long id1 = oabaController.save(job).getId();
		te.add(job);

		// Retrieve the job
		job = null;
		job = oabaController.findOabaJob(id1);

		// Check the value
		final String v2 = job.getExternalId();
		assertTrue(extId.equals(v2));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testTransactionId() {
		final String METHOD = "testTransactionId";
		final TestEntities te = new TestEntities();

		// Create a job and set a value
		OabaJob job = createEphemeralOabaJobEntity(te, METHOD, true);
		final long v1 = random.nextLong();
		job.setDescription("" + v1);

		// Save the job
		final long id1 = oabaController.save(job).getId();
		te.add(job);
		job = null;

		// Get the job
		job = oabaController.findOabaJob(id1);

		// Check the value
		final long v2 = Long.parseLong(job.getDescription());
		assertTrue(v1 == v2);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	/**
	 * Tests get/setDescription and merging a detached entity with an existing
	 * database entry
	 */
	@Test
	public void testMergeDescription() {
		final String METHOD = "testMergeDescription";
		final TestEntities te = new TestEntities();

		// Create a job and set a value
		OabaJob job = createEphemeralOabaJobEntity(te, METHOD, true);
		assertTrue(job.getDescription() == null);
		final String description = "Description: " + new Date().toString();
		job.setDescription(description);
		assertTrue(description.equals(job.getDescription()));

		// Save the job
		final long id1 = oabaController.save(job).getId();
		te.add(job);

		// Detach the job and modify the description
		oabaController.detach(job);
		assertTrue(description.equals(job.getDescription()));
		final String description2 = "Description 2: " + new Date().toString();
		job.setDescription(description2);
		oabaController.save(job);

		// Get the job
		job = null;
		job = oabaController.findOabaJob(id1);

		// Check the value
		assertTrue(description2.equals(job.getDescription()));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

//	@Test
//	public void testPercentageComplete() {
//		final String METHOD = "testPercentageComplete";
//		final TestEntities te = new TestEntities();
//
//		// Record a timestamp before a transition is made
//		Date before = new Date();
//
//		// 1. Create a job and check the percentage complete
//		OabaJob job = createEphemeralOabaJob(te, METHOD, true);
//
//		// Record a timestamp after a transition is made
//		Date after = new Date();
//
//		// Check the status and timestamp
////		assertTrue(job.getFractionComplete() == OabaJob.MIN_PERCENTAGE_COMPLETED);
//		Date d = job.getTimeStamp(job.getStatus());
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		Date d2 = job.getRequested();
//		assertTrue(d.equals(d2));
//
//		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
//
//			final String sts = BatchJobUtils.getRandomNonTerminalStatus();
//			final int v1 =
//				random.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1);
//			job.setStatus(sts);
//			before = new Date();
////			job.setFractionComplete(v1);
//			after = new Date();
//
//			// Check the percentage, status and timestamp
////			int v2 = job.getFractionComplete();
//			assertTrue(v2 == v1);
//
//			assertTrue(job.getStatus().equals(sts));
//
//			d = job.getTimeStamp(job.getStatus());
//			assertTrue(d != null);
//			assertTrue(before.compareTo(d) <= 0);
//			assertTrue(after.compareTo(d) >= 0);
//
//			// Save the job
//			final long id1 = oabaController.save(job).getId();
//			job = null;
//
//			// Retrieve the job
//			job = oabaController.findOabaJob(id1);
//
//			// Re-check the percentage, status and timestamp
////			v2 = job.getFractionComplete();
////			assertTrue(v2 == v1);
//
//			assertTrue(job.getStatus().equals(sts));
//
//			d = job.getTimeStamp(job.getStatus());
//			assertTrue(d != null);
//			assertTrue(before.compareTo(d) <= 0);
//			assertTrue(after.compareTo(d) >= 0);
//
//			// Remove the job and the number of remaining jobs
//			oabaController.delete(job);
//		}
//
//		try {
//			te.removePersistentObjects(em, utx);
//		} catch (Exception x) {
//			logger.severe(x.toString());
//			fail(x.toString());
//		}
//	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		final TestEntities te = new TestEntities();

		// Create two ephemeral jobs
		String exId = EntityManagerUtils.createExternalId(METHOD);
		boolean isTag = false;
		OabaJob job1 = createEphemeralOabaJobEntity(te, exId, isTag);
		assertTrue(te.contains(job1));
		OabaJob job2 = new OabaJobEntity(job1);
		te.add(job2);

		// Verify equality of ephemeral instances
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

//		// Change percentComplete on one of the jobs and verify inequality
//		job1.setFractionComplete(50);
//		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
//		assertTrue(!job1.equals(job2));
//		assertTrue(job1.hashCode() != job2.hashCode());
//
//		// Restore equality
//		job2.setFractionComplete(job1.getFractionComplete());
//		assertTrue(job1.equals(job2));
//		assertTrue(job1.hashCode() == job2.hashCode());

		// Verify a non-persistent job is not equal to a persistent job
		job1 = oabaController.save(job1);
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

		// Verify that equality of persisted jobs is set only by persistence id
		oabaController.detach(job1);
		job2 = oabaController.findOabaJob(job1.getId());
		oabaController.detach(job2);
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

//		job1.setFractionComplete(job1.getFractionComplete() + 1);
//		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
//		assertTrue(job1.equals(job2));
//		assertTrue(job1.hashCode() == job2.hashCode());

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testStateMachineMainSequence() {
		final String METHOD = "testStateMachineMainSequence";
		final TestEntities te = new TestEntities();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		OabaJob job = createEphemeralOabaJobEntity(te, METHOD, true);
		// oabaTestController.save(job);

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getStatus().equals(BatchJobStatus.NEW));
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.NEW));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.NEW));

		// 2. Queue the job
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.QUEUED));
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.PROCESSING));
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.PROCESSING));
		// oabaTestController.save(job);

//		// 4. Update the percentage complete
//		job.setFractionComplete(random
//				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
//		assertTrue(job.getStatus().equals(BatchJobStatus.PROCESSING));
//		// oabaTestController.save(job);

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
//		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testStatus() {
		final String METHOD = "testStatus";
		final TestEntities te = new TestEntities();

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			OabaJobEntity entity = createEphemeralOabaJobEntity(te, METHOD, true);
			entity.setStatus(sts);
			assertTrue(sts.equals(entity.getStatus()));

			// Save the job
			final long id1 = oabaController.save(entity).getId();
			entity = null;

			// Retrieve the job
			OabaJob job = oabaController.findOabaJob(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and the number of remaining jobs
			oabaController.delete(job);
		}

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			final OabaJobEntity entity = createEphemeralOabaJobEntity(te, METHOD, true);
			entity.setStatus(sts);
			assertTrue(sts.equals(entity.getStatus()));

			// Save the job
			final long id1 = oabaController.save(entity).getId();
			te.add(entity);

			// Retrieve the job
			final OabaJob job = oabaController.findOabaJob(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));
		}

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	public void testTimestamp(BatchJobStatus sts) {
		final String METHOD = "testTimestamp";
		final TestEntities te = new TestEntities();

		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		final OabaJobEntity entity = createEphemeralOabaJobEntity(te, METHOD, true);
		entity.setStatus(sts);

		// Record a timestamp after the status is set
		final Date now2 = new Date();

		// Check that the expected value of the status
		final BatchJobStatus batchJobStatus = entity.getStatus();
		assert (sts.equals(batchJobStatus));

		// Check the expected timestamp of the status
		final Date ts = entity.getTimeStamp(sts);
		assertTrue(ts != null);
		assertTrue(now.compareTo(ts) <= 0);
		assertTrue(ts.compareTo(now2) <= 0);

		// Save the job
		final long id = oabaController.save(entity).getId();

		// Find the job and verify the expected status and timestamp
		final OabaJob job = oabaController.findOabaJob(id);
		assertTrue(batchJobStatus.equals(job.getStatus()));
		assertTrue(ts.equals(job.getTimeStamp(sts)));

		// Clean up the test DB
		oabaController.delete(job);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testTimestamps() {
		for (BatchJobStatus sts : BatchJobStatus.values()) {
			testTimestamp(sts);
		}
	}

	protected OabaJobEntity createEphemeralOabaJobEntity(TestEntities te, String tag,
			boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		return BatchJobUtils.createEphemeralOabaJobEntity(MAX_SINGLE_LIMIT, utx, sc,
				em, te, tag, isTag);
	}

	protected ServerConfiguration getDefaultServerConfiguration() {
		return BatchJobUtils.getDefaultServerConfiguration(serverController);
	}

}
