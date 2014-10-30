package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_ABORTED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_ABORT_REQUESTED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_CLEAR;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_COMPLETED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_FAILED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_NEW;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_QUEUED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_STARTED;
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

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cmit.utils.DeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class BatchJobBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

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

	// There's a 'race' condition on slow machines that may be avoided
	// by keeping the number of loop iterations low.
	// See blog posting http://links.rph.cx/ZWGCYD
	//
	// public static final int MAX_TEST_ITERATIONS = 10;
	public static final int MAX_TEST_ITERATIONS = 1;

	private static final String[] _statusValues =
		new String[] {
				STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_COMPLETED,
				STATUS_FAILED, STATUS_ABORT_REQUESTED, STATUS_ABORTED,
				STATUS_CLEAR };

	private static final String[] _nonterminal = new String[] {
			STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_ABORT_REQUESTED };

	@EJB
	protected BatchJobController controller;

	private final Random random = new Random(new Date().getTime());

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
//	private int initialTransitivityJobCount;

	private String getRandomNonTerminalStatus() {
		int i = random.nextInt(_nonterminal.length);
		return _nonterminal[i];
	}

	@Before
	public void setUp() {
		initialBatchParamsCount = controller.findAllBatchParameters().size();
		initialBatchJobCount = controller.findAllBatchJobs().size();
	}

	@After
	public void tearDown() {
		int finalBatchParamsCount = controller.findAllBatchParameters().size();
		assertTrue(initialBatchParamsCount == finalBatchParamsCount);

		int finalBatchJobCount = controller.findAllBatchJobs().size();
		assertTrue(initialBatchJobCount == finalBatchJobCount);
	}

	@Test
	public void testBatchJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		final String METHOD = "testConstruction";
		final TestEntities te = new TestEntities();

		Date now = new Date();
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		Date now2 = new Date();

		assertTrue(0 == job.getId());

		assertTrue(STATUS_NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(STATUS_NEW);
		assertTrue(d.equals(d2));
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";
		final TestEntities te = new TestEntities();

		// Create a job
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		assertTrue(job.getId() == 0);

		// Save the job
		controller.save(job);
		assertTrue(job.getId() != 0);

		// Find the job
		BatchJobBean batchJob2 = controller.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		controller.delete(batchJob2);
		BatchJob batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);
	}

	@Test
	public void testFindAll() {
		final String METHOD = "testFindAll";
		final TestEntities te = new TestEntities();

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
			assertTrue(job.getId() == 0);
			controller.save(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<BatchJobBean> jobs = controller.findAll();
		assertTrue(jobs != null);

		// Find the jobs
		boolean isFound = false;
		for (long jobId : jobIds) {
			for (BatchJob job : jobs) {
				if (jobId == job.getId()) {
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}
	}

	@Test
	public void testExternalId() {
		final String METHOD = "testExternalId";
		final TestEntities te = new TestEntities();

		// Create a job and set a value
		String extId = EntityManagerUtils.createExternalId(METHOD);
		BatchJobBean job = controller.createEphemeralBatchJob(te, extId);
		assertTrue(extId.equals(job.getExternalId()));

		// Save the job
		final long id1 = controller.save(job).getId();

		// Retrieve the job
		job = null;
		job = controller.find(id1);

		// Check the value
		final String v2 = job.getExternalId();
		assertTrue(extId.equals(v2));
	}

	@Test
	public void testTransactionId() {
		final String METHOD = "testTransactionId";
		final TestEntities te = new TestEntities();

		// Create a job and set a value
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		final long v1 = random.nextLong();
		job.setDescription("" + v1);

		// Save the job
		final long id1 = controller.save(job).getId();
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		final long v2 = Long.parseLong(job.getDescription());
		assertTrue(v1 == v2);
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
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		assertTrue(job.getDescription() == null);
		final String description = "Description: " + new Date().toString();
		job.setDescription(description);
		assertTrue(description.equals(job.getDescription()));

		// Save the job
		final long id1 = controller.save(job).getId();

		// Detach the job and modify the description
		controller.detach(job);
		assertTrue(description.equals(job.getDescription()));
		final String description2 = "Description 2: " + new Date().toString();
		job.setDescription(description2);
		controller.save(job);

		// Get the job
		job = null;
		job = controller.find(id1);

		// Check the value
		assertTrue(description2.equals(job.getDescription()));
	}

	@Test
	public void testPercentageComplete() {
		final String METHOD = "testPercentageComplete";
		final TestEntities te = new TestEntities();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the percentage complete
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getFractionComplete() == BatchJob.MIN_PERCENTAGE_COMPLETED);
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {

			final String sts = getRandomNonTerminalStatus();
			final int v1 =
				random.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1);
			job.setStatus(sts);
			before = new Date();
			job.setFractionComplete(v1);
			after = new Date();

			// Check the percentage, status and timestamp
			int v2 = job.getFractionComplete();
			assertTrue(v2 == v1);

			assertTrue(job.getStatus().equals(sts));

			d = job.getTimeStamp(job.getStatus());
			assertTrue(d != null);
			assertTrue(before.compareTo(d) <= 0);
			assertTrue(after.compareTo(d) >= 0);

			// Save the job
			final long id1 = controller.save(job).getId();
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Re-check the percentage, status and timestamp
			v2 = job.getFractionComplete();
			assertTrue(v2 == v1);

			assertTrue(job.getStatus().equals(sts));

			d = job.getTimeStamp(job.getStatus());
			assertTrue(d != null);
			assertTrue(before.compareTo(d) <= 0);
			assertTrue(after.compareTo(d) >= 0);

			// Remove the job and the number of remaining jobs
			controller.delete(job);
		}
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		final TestEntities te = new TestEntities();

		// Create two ephemeral jobs
		String exId = EntityManagerUtils.createExternalId(METHOD);
		BatchJobBean job1 = controller.createEphemeralBatchJob(te, exId);
		assertTrue(te.contains(job1));
		BatchJobBean job2 = new BatchJobBean(job1, job1.getExternalId());
		te.add(job2);

		//  Verify equality of ephemeral instances
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

		// Change percentComplete on one of the jobs and verify inequality
		job1.setFractionComplete(50);
		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

		// Restore equality
		job2.setFractionComplete(job1.getFractionComplete());
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

		// Verify a non-persistent job is not equal to a persistent job
		job1 = controller.save(job1);
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

		// Verify that equality of persisted jobs is set only by persistence id
		controller.detach(job1);
		job2 = controller.find(job1.getId());
		controller.detach(job2);
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

		job1.setFractionComplete(job1.getFractionComplete() + 1);
		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());
	}

	@Test
	public void testStateMachineMainSequence() {
		final String METHOD = "testStateMachineMainSequence";
		final TestEntities te = new TestEntities();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		// controller.save(job);

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getStatus().equals(STATUS_NEW));
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_NEW));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_NEW));

		// 2. Queue the job
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));
		// controller.save(job);

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// controller.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// controller.save(job);

		// 4. Update the percentage complete
		job.setFractionComplete(random
				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// controller.save(job);

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);
		// controller.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
	}

	@Test
	public void testStatus() {
		final String METHOD = "testStatus";
		final TestEntities te = new TestEntities();

		for (String sts : _statusValues) {
			BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = controller.save(job).getId();
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and the number of remaining jobs
			controller.delete(job);
		}

		for (String sts : _statusValues) {
			BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = controller.save(job).getId();
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));
		}
	}

	public void testTimestamp(String sts) {
		final String METHOD = "testTimestamp";
		final TestEntities te = new TestEntities();

		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		BatchJobBean job = controller.createEphemeralBatchJob(METHOD, te);
		job.setStatus(sts);

		// Record a timestamp after the status is set
		final Date now2 = new Date();

		// Check that the expected value of the status
		final String batchJobStatus = job.getStatus();
		assert (sts.equals(batchJobStatus));

		// Check the expected timestamp of the status
		final Date ts = job.getTimeStamp(sts);
		assertTrue(ts != null);
		assertTrue(now.compareTo(ts) <= 0);
		assertTrue(ts.compareTo(now2) <= 0);

		// Save the job
		final long id = controller.save(job).getId();

		// Find the job and verify the expected status and timestamp
		job = null;
		job = controller.find(id);
		assertTrue(batchJobStatus.equals(job.getStatus()));
		assertTrue(ts.equals(job.getTimeStamp(sts)));

		// Clean up the test DB
		controller.delete(job);
	}

	@Test
	public void testTimestamps() {
		for (String sts : _statusValues) {
			testTimestamp(sts);
		}
	}

}
