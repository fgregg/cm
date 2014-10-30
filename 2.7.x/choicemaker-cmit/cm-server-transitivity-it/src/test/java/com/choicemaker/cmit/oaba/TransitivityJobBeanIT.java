package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_ABORTED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_ABORT_REQUESTED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_CLEAR;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_COMPLETED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_FAILED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_NEW;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_QUEUED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob.STATUS_STARTED;
import static com.choicemaker.cmit.transitivity.util.TransitivityConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.transitivity.util.TransitivityConstants.PERSISTENCE_CONFIGURATION;
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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

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
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class TransitivityJobBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

	private static final Logger logger = Logger
			.getLogger(TransitivityJobBeanIT.class.getName());

	public static final int MAX_TEST_ITERATIONS = 10;

	private static final String[] _statusValues =
		new String[] {
				STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_COMPLETED,
				STATUS_FAILED, STATUS_ABORT_REQUESTED, STATUS_ABORTED,
				STATUS_CLEAR };

	// private static final String[] _nonterminal = new String[] {
	// STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_ABORT_REQUESTED };
	//
	// private String getRandomNonTerminalStatus() {
	// int i = random.nextInt(_nonterminal.length);
	// return _nonterminal[i];
	// }

	private final Random random = new Random(new Date().getTime());

	@EJB
	protected TransitivityJobController controller;

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
	private int initialCount;

	@Before
	public void setUp() {
		initialBatchParamsCount = controller.findAllBatchParameters().size();
		initialBatchJobCount = controller.findAllBatchJobs().size();
		initialCount = controller.findAllTransitivityJobs().size();
	}

	@After
	public void tearDown() {
		int finalBatchParamsCount = controller.findAllBatchParameters().size();
		assertTrue(initialBatchParamsCount == finalBatchParamsCount);

		int finalBatchJobCount = controller.findAllBatchJobs().size();
		assertTrue(initialBatchJobCount == finalBatchJobCount);

		int finalTransJobCount = controller.findAllTransitivityJobs().size();
		assertTrue(initialCount == finalTransJobCount);
	}

	@Test
	public void testTransitivityJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		final String METHOD = "testConstruction";
		final TestEntities te = new TestEntities();

		BatchJobBean batchJob = controller.createBatchJobBean(METHOD, te);
		final Date now = new Date();
		TransitivityJobBean job =
			controller.createTransitivityJob(METHOD, te, batchJob);
		final Date now2 = new Date();

		// Check that primary hasn't been set
		assertTrue(0 == job.getId());

		// Check that the predecessor and parameters have been set
		assertTrue(job.getBatchParentId() == batchJob.getId());
		long paramsId = batchJob.getBatchParametersId();
		assertTrue(paramsId != BatchParametersBean.INVALID_PARAMSID);
		assertTrue(paramsId == job.getBatchParametersId());

		// Check the status and associated timestamps
		assertTrue(STATUS_NEW.equals(job.getStatus()));
		Date d2 = job.getTimeStamp(STATUS_NEW);
		assertTrue(d2 != null);
		assertTrue(now.compareTo(d2) <= 0);
		assertTrue(d2.compareTo(now2) <= 0);
		Date d = job.getRequested();
		assertTrue(d2.equals(d));

		controller.removeTestEntities(te);
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";
		final TestEntities te = new TestEntities();

		// Create a job
		TransitivityJobBean job = controller.createTransitivityJob(METHOD, te);
		assertTrue(job.getId() == 0);

		// Save the job
		controller.save(job);
		assertTrue(job.getId() != 0);

		// Find the job
		TransitivityJobBean batchJob2 = controller.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Remove test entities from database
		controller.removeTestEntities(te);

		// Delete the job
		TransitivityJob batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);
	}

	/**
	 * Tests merging of a modified transitivity job back into the database, but
	 * also tests get/setDescription methods.
	 */
	@Test
	public void testMerge() {
		final String METHOD = "testMerge";
		final TestEntities te = new TestEntities();

		TransitivityJobBean job = controller.createTransitivityJob(METHOD, te);
		assertTrue(null == job.getDescription());
		final String description = "some job description";
		job.setDescription(description);
		assertTrue(description.equals(job.getDescription()));

		controller.save(job);
		final long id = job.getId();
		controller.detach(job);

		job = null;
		TransitivityJobBean job2 = controller.find(id);
		assertTrue(id == job2.getId());
		assertTrue(description.equals(job2.getDescription()));

		final String description2 = "some new job description";
		assertTrue(!description2.equals(description));
		job2.setDescription(description2);
		controller.save(job2);

		job2 = null;
		TransitivityJobBean job3 = controller.find(id);
		assertTrue(id == job3.getId());
		assertTrue(description2.equals(job3.getDescription()));

		controller.removeTestEntities(te);
	}

	@Test
	public void testFindAll() {
		final String METHOD = "testFindAll";
		final TestEntities te = new TestEntities();

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			TransitivityJobBean job =
				controller.createTransitivityJob(METHOD, te);
			controller.save(job);
			long id = job.getId();
			assertTrue(!jobIds.contains(id));
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<TransitivityJobBean> jobs = controller.findAllTransitivityJobs();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs that have been created
		boolean isFound = false;
		for (long jobId : jobIds) {
			for (TransitivityJob job : jobs) {
				if (jobId == job.getId()) {
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}

		controller.removeTestEntities(te);
	}

	@Test
	public void testFindAllByParentId() {
		final String METHOD = "testFindAllByParentId";
		final TestEntities te = new TestEntities();

		final BatchJobBean batchJob = controller.createBatchJobBean(METHOD, te);
		final long batchJobId = batchJob.getId();
		Set<Long> jobIds = new HashSet<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			TransitivityJobBean job =
				controller.createTransitivityJob(METHOD, te, batchJob);
			controller.save(job);
			final long id = job.getId();
			assertTrue(!jobIds.contains(id));
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<TransitivityJobBean> jobs = controller.findAllTransitivityJobs();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs by querying
		jobs = controller.findAllByParentId(batchJobId);
		assertTrue(jobs != null);
		assertTrue(jobs.size() == jobIds.size());
		for (TransitivityJob job : jobs) {
			assertTrue(jobIds.contains(job.getId()));
		}

		controller.removeTestEntities(te);
	}

	@Test
	public void testNoNullStatusTimestamp() {
		final TestEntities te = new TestEntities();

		int countNullStatus = 0;
		int countNullTimestamp = 0;
		int count = 0;
		for (TransitivityJob job : controller.findAllTransitivityJobs()) {
			++count;
			String batchJobStatus = job.getStatus();
			if (batchJobStatus == null) {
				logger.severe(job.getId() + ": " + count + ": null status");
				++countNullStatus;
			}
			Date ts = job.getTimeStamp(batchJobStatus);
			if (ts == null) {
				String msg =
					job.getId() + ": " + count
							+ ": null timestamp for status '" + batchJobStatus
							+ "'";
				logger.severe(msg);
				++countNullTimestamp;
			}
		}
		if ((countNullStatus) != 0) {
			fail("Null status: " + countNullStatus + " out of " + count);
		}
		if ((countNullTimestamp) != 0) {
			fail("Null timestamp: " + countNullTimestamp + " out of " + count);
		}

		controller.removeTestEntities(te);
	}

	@Test
	public void testFractionComplete() {
		final String METHOD = "testFractionComplete";
		final TestEntities te = new TestEntities();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and mark it as running (a.k.a. 'STARTED')
		TransitivityJobBean job = controller.createTransitivityJob(METHOD, te);
		job.markAsQueued();
		job.markAsStarted();

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the timestamp and the percentage complete
		assertTrue(job.getFractionComplete() == BatchJob.MIN_PERCENTAGE_COMPLETED);
		final String sts = job.getStatus();
		assertTrue(BatchJob.STATUS_STARTED.equals(sts));
		Date d = job.getTimeStamp(sts);
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getStarted();
		assertTrue(d.equals(d2));

		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {

			final int v1 =
				random.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1);
			before = new Date();
			job.setFractionComplete(v1);
			after = new Date();

			// Check the percentage, status and timestamp
			assertTrue(job.getFractionComplete() == v1);
			assertTrue(job.getStatus().equals(sts));
			Date d3 = job.getTimeStamp(job.getStatus());
			assertTrue(d3 != null);
			assertTrue(before.compareTo(d3) <= 0);
			assertTrue(after.compareTo(d3) >= 0);

			// Save the job
			final long id1 = controller.save(job).getId();

			// Retrieve the job and re-check the percentage, status and
			// timestamp
			job = null;
			job = controller.find(id1);
			assertTrue(job != null);
			assertTrue(job.getFractionComplete() == v1);
			assertTrue(job.getStatus().equals(sts));
			assertTrue(job.getTimeStamp(sts).equals(d3));
		}

		controller.removeTestEntities(te);
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		final TestEntities te = new TestEntities();

		// Create two generic jobs and verify equality
		String extId = controller.createExternalId(METHOD);
		final BatchJobBean batchJob = controller.createBatchJobBean(METHOD, te);
		TransitivityJobBean job1 =
			controller.createTransitivityJob(te, batchJob, extId);
		TransitivityJobBean job2 =
			controller.createTransitivityJob(te, batchJob, extId);
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
		controller.removeTestEntities(te);
	}

	@Test
	public void testStateMachineMainSequence() {
		final String METHOD = "testStateMachineMainSequence";
		final TestEntities te = new TestEntities();

		// 1. Create a job and check the status
		Date before = new Date();
		TransitivityJobBean job = controller.createTransitivityJob(METHOD, te);
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
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_NEW));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));

		// 2. Queue the job
		before = new Date();
		job.markAsQueued();
		after = new Date();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));
		d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		d2 = job.getQueued();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));

		// 3. Start the job
		before = new Date();
		job.markAsStarted();
		after = new Date();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		d2 = job.getStarted();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));

		// 4. Update the percentage complete
		job.setFractionComplete(random
				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(STATUS_STARTED));

		// 5. Mark the job as completed
		before = new Date();
		job.markAsCompleted();
		after = new Date();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);
		d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		d2 = job.getCompleted();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));

		controller.removeTestEntities(te);
	}

	@Test
	public void testStatus() {
		final String METHOD = "testStatus";
		final TestEntities te = new TestEntities();

		for (String sts : _statusValues) {
			TransitivityJobBean job =
				controller.createTransitivityJob(METHOD, te);
			assertTrue(BatchJob.STATUS_NEW.equals(job.getStatus()));
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

		controller.removeTestEntities(te);
	}

	public void testTimestamp(final String sts) {
		if (sts == null) {
			throw new IllegalArgumentException("null status");
		}
		final String METHOD = "testTimestamp";
		final TestEntities te = new TestEntities();

		final Date before = new Date();
		TransitivityJobBean job = controller.createTransitivityJob(METHOD, te);
		final Date after = new Date();
		job.setStatus(sts);

		// Check the expected status and timestamp
		assertTrue(sts.equals(job.getStatus()));
		final Date d = job.getTimeStamp(sts);
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(d.compareTo(after) <= 0);

		// Save the job
		final long id = controller.save(job).getId();

		// Find the job and verify the expected status and timestamp
		job = null;
		job = controller.find(id);
		assertTrue(sts.equals(job.getStatus()));
		assertTrue(d.equals(job.getTimeStamp(sts)));

		controller.removeTestEntities(te);
	}

	@Test
	public void testTimestamps() {
		for (String sts : _statusValues) {
			testTimestamp(sts);
		}
	}

}
