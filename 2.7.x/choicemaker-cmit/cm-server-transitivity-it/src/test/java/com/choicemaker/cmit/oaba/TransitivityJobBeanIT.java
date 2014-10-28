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
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.TransitivityJobBean;
import com.choicemaker.cmit.utils.DeploymentUtils;

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
		EnterpriseArchive retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
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

	private static final String[] _nonterminal = new String[] {
			STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_ABORT_REQUESTED };

	private String getRandomNonTerminalStatus() {
		int i = random.nextInt(_nonterminal.length);
		return _nonterminal[i];
	}

	private final Random random = new Random(new Date().getTime());

	@EJB
	protected TransitivityJobController controller;

	@Test
	public void testTransitivityJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		final Date now = new Date();
		String extId = "EXT ID: " + now;
		BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		final Date now2 = new Date();

		assertTrue(0 == job.getId());
		assertTrue(job.getBatchParentId() == batchJob.getId());

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
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job
		String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		assertTrue(job.getId() == 0);
		assertTrue(job.getBatchParentId() == batchJob.getId());

		// Save the job
		controller.save(job);
		assertTrue(job.getId() != 0);
		assertTrue(job.getBatchParentId() == batchJob.getId());

		// Find the job
		TransitivityJobBean batchJob2 = controller.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		controller.deleteTransitivityJobAndParent(batchJob2);
		TransitivityJob batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		final String externalId = "EXT ID: " + new Date().toString();
		BatchJobBean batchJob = controller.createBatchJobBean(externalId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		controller.save(job);
		assertTrue(job.getId() != 0);
		assertTrue(job.getBatchParentId() == batchJob.getId());

		final long id = job.getId();
		controller.detach(job);

		assertTrue(externalId.equals(job.getExternalId()));
		final String externalId2 = "external test id";
		job.setExternalId(externalId2);
		controller.save(job);

		job = null;
		TransitivityJobBean batchJob2 = controller.find(id);
		assertTrue(id == batchJob2.getId());
		assertTrue(externalId2.equals(batchJob2.getExternalId()));
		controller.deleteTransitivityJobAndParent(batchJob2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testFindAll() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			String extId = "EXT ID: " + new Date().toString();
			final BatchJobBean batchJob = controller.createBatchJobBean(extId);
			TransitivityJobBean job = new TransitivityJobBean(batchJob);
			assertTrue(job.getId() == 0);
			controller.save(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<TransitivityJobBean> jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs
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

		// Remove the job
		for (long id : jobIds) {
			TransitivityJobBean job = controller.find(id);
			controller.deleteTransitivityJobAndParent(job);
		}

		jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount == jobs.size());
	}

	@Test
	public void testFindAllByParentId() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		final String extId0 = "BATCH EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId0);
		final long batchJobId = batchJob.getId();
		Set<Long> jobIds = new HashSet<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			String extId = "TRANS EXT ID: " + new Date().toString() + " " + i;
			TransitivityJobBean job = new TransitivityJobBean(batchJob, extId);
			assertTrue(job.getId() == 0);
			controller.save(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}
		// Implicit check that all jobIds are distinct
		assertTrue(jobIds.size() == MAX_TEST_ITERATIONS);

		// Verify the number of jobs has increased
		List<TransitivityJobBean> jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs
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

		// Find the jobs by querying
		jobs = controller.findAllByParentId(batchJobId);
		assertTrue(jobs != null);
		assertTrue(jobs.size() == jobIds.size());
		for (TransitivityJob job : jobs) {
			assertTrue(jobIds.contains(job.getId()));
		}

		// Remove the jobs
		for (long id : jobIds) {
			TransitivityJobBean job = controller.find(id);
			controller.deleteTransitivityJobAndParent(job);
		}

		jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount == jobs.size());
	}

	@Test
	public void testNoNullStatusTimestamp() {
		int countNullStatus = 0;
		int countNullTimestamp = 0;
		int count = 0;
		for (TransitivityJob job : controller.findAll()) {
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
	}

	@Test
	public void testExternalId() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		final String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		assertTrue(extId.equals(job.getExternalId()));

		// Save the job
		final long id0 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Retrieve the job
		job = controller.find(id0);

		// Check the value
		final String v0 = job.getExternalId();
		assertTrue(extId.equals(v0));

		// Change the value
		final String v1 = new Date().toString();
		assertTrue(!v1.equals(extId));
		job.setExternalId(v1);
		assertTrue(v1.equals(job.getExternalId()));

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Retrieve the job
		job = controller.find(id1);

		// Check the value
		final String v2 = job.getExternalId();
		assertTrue(v1.equals(v2));

		// Remove the job and check the number of remaining jobs
		controller.deleteTransitivityJobAndParent(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testDescription() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		final String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		assertTrue(job.getDescription() == null);
		final String v1 = new Date().toString();
		job.setDescription(v1);
		assertTrue(v1.equals(job.getDescription()));

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		final String v2 = job.getDescription();
		assertTrue(v1.equals(v2));

		// Remove the job and check the number of remaining jobs
		controller.deleteTransitivityJobAndParent(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testFractionComplete() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the percentage complete
		final String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);

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
			assertTrue(initialCount + 1 == controller.findAll().size());
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

			// Remove the job and check the number of remaining jobs
			controller.deleteTransitivityJobAndParent(job);
		}

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic jobs and verify equality
		String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job1 = new TransitivityJobBean(batchJob);
		TransitivityJobBean job2 = new TransitivityJobBean(batchJob);
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
		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		final String extId = "EXT ID: " + new Date().toString();
		final BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);

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

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_STARTED));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_STARTED));

		// 4. Update the percentage complete
		job.setFractionComplete(random
				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(STATUS_STARTED));

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);

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
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (String sts : _statusValues) {
			final String extId = "EXT ID: " + new Date().toString();
			final BatchJobBean batchJob = controller.createBatchJobBean(extId);
			TransitivityJobBean job = new TransitivityJobBean(batchJob);
			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = controller.save(job).getId();
			assertTrue(initialCount + 1 == controller.findAll().size());
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and check the number of remaining jobs
			controller.deleteTransitivityJobAndParent(job);
			assertTrue(initialCount == controller.findAll().size());
		}

		for (String sts : _statusValues) {
			final String extId = "EXT ID: " + new Date().toString();
			BatchJobBean batchJob = controller.createBatchJobBean(extId);
			TransitivityJobBean job = new TransitivityJobBean(batchJob);
			assertTrue(BatchJob.STATUS_NEW.equals(job.getStatus()));

			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = controller.save(job).getId();
			assertTrue(initialCount + 1 == controller.findAll().size());
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and check the number of remaining jobs
			controller.deleteTransitivityJobAndParent(job);
			assertTrue(initialCount == controller.findAll().size());
		}

		assertTrue(initialCount == controller.findAll().size());
	}

	public void testTimestamp(String sts) {
		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		final String extId = "EXT ID: " + new Date().toString();
		BatchJobBean batchJob = controller.createBatchJobBean(extId);
		TransitivityJobBean job = new TransitivityJobBean(batchJob);
		assertTrue(BatchJob.STATUS_NEW.equals(job.getStatus()));

		job.setStatus(sts);
		assertTrue(sts.equals(job.getStatus()));

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
		controller.deleteTransitivityJobAndParent(job);
	}

	@Test
	public void testTimestamps() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (String sts : _statusValues) {
			testTimestamp(sts);
		}

		assertTrue(initialCount == controller.findAll().size());
	}

}