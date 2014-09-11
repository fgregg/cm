package com.choicemaker.cmit.persist0;

import static com.choicemaker.cmit.persist0.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PROJECT_POM;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.persist0.BatchJob;
import com.choicemaker.cm.persist0.BatchJobBean;
import com.choicemaker.cm.persist0.BatchJobStatus;
import com.choicemaker.cm.persist0.TransitivityJobBean;
import com.choicemaker.cmit.utils0.DeploymentUtils;

@RunWith(Arquillian.class)
public class TransitivityJobBeanTest {

	private static final BatchJobStatus[] _nonterminal = EnumSet.of(
			BatchJobStatus.NEW, BatchJobStatus.QUEUED, BatchJobStatus.STARTED,
			BatchJobStatus.ABORT_REQUESTED).toArray(new BatchJobStatus[0]);

	private BatchJobStatus getRandomNonTerminalStatus() {
		int i = random.nextInt(_nonterminal.length);
		return _nonterminal[i];
	}

	private static final Logger logger = Logger
			.getLogger(TransitivityJobBeanTest.class.getName());

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(TransitivityJobBeanTest.class);
		testClasses.add(TransitivityJobController.class);

		JavaArchive ejb =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(PROJECT_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb, deps);
		return retVal;
	}

	public static final int MAX_TEST_ITERATIONS = 10;

	private final Random random = new Random(new Date().getTime());

	@EJB
	protected TransitivityJobController controller;

	@Test
	public void testBatchJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		Date now = new Date();
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		Date now2 = new Date();

		assertTrue(0 == job.getId());

		assertTrue(BatchJobStatus.NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(BatchJobStatus.NEW);
		assertTrue(d.equals(d2));
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		assertTrue(job.getId() == 0);

		// Save the job
		controller.save(job);
		assertTrue(job.getId() != 0);

		// Find the job
		TransitivityJobBean batchJob2 = controller.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		controller.delete(batchJob2);
		BatchJob batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		final String externalId = "EXT ID: " + new Date().toString();
		TransitivityJobBean job = new TransitivityJobBean(externalId);
		controller.save(job);
		assertTrue(job.getId() != 0);
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
		controller.delete(batchJob2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testFindAll() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			TransitivityJobBean job =
				new TransitivityJobBean("EXT ID: " + new Date().toString());
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
			for (BatchJob job : jobs) {
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
			controller.delete(job);
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
		for (BatchJob job : controller.findAll()) {
			++count;
			BatchJobStatus batchJobStatus = job.getStatus();
			if (batchJobStatus == null) {
				logger.severe(job.getId() + ": " + count + ": null status");
				++countNullStatus;
			}
			Date ts = job.getTimeStamp(batchJobStatus);
			if (ts == null) {
				String msg =
					job.getId() + ": " + count
							+ ": null timestamp for status '" + batchJobStatus
							+ " [" + batchJobStatus.ordinal() + "]'";
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
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		final String v1 = new Date().toString();
		job.setExternalId(v1);

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Retrieve the job
		job = controller.find(id1);

		// Check the value
		final String v2 = job.getExternalId();
		assertTrue(v1.equals(v2));

		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testTransactionId() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		final long v1 = random.nextLong();
		job.setDescription("" + v1);

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		final long v2 = Long.parseLong(job.getDescription());
		assertTrue(v1 == v2);

		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testType() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		assertTrue(TransitivityJobBean.TABLE_DISCRIMINATOR
				.equals(job.getType()));

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		assertTrue(TransitivityJobBean.TABLE_DISCRIMINATOR
				.equals(job.getType()));

		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testDescription() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		final String v1 = new Date().toString();
		job.setDescription(v1);

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		final String v2 = job.getDescription();
		assertTrue(v1.equals(v2));

		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testPercentageComplete() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the percentage complete
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getPercentageComplete() == BatchJob.MIN_PERCENTAGE_COMPLETED);
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {

			final BatchJobStatus sts = getRandomNonTerminalStatus();
			final int v1 =
				random.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1);
			job.setStatus(sts);
			before = new Date();
			job.setPercentageComplete(v1);
			after = new Date();

			// Check the percentage, status and timestamp
			int v2 = job.getPercentageComplete();
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
			v2 = job.getPercentageComplete();
			assertTrue(v2 == v1);

			assertTrue(job.getStatus().equals(sts));

			d = job.getTimeStamp(job.getStatus());
			assertTrue(d != null);
			assertTrue(before.compareTo(d) <= 0);
			assertTrue(after.compareTo(d) >= 0);

			// Remove the job and the number of remaining jobs
			controller.delete(job);
		}

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic jobs and verify equality
		String exId = "EXT ID: " + new Date().toString();
		String trID = BatchJobBean.randomTransactionId();
		TransitivityJobBean job1 = new TransitivityJobBean(exId, trID);
		TransitivityJobBean job2 = new TransitivityJobBean(exId, trID);
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

		// Change percentComplete on one of the jobs and verify inequality
		job1.setPercentageComplete(50);
		assertTrue(job1.getPercentageComplete() != job2.getPercentageComplete());
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

		// Restore equality
		job2.setPercentageComplete(job1.getPercentageComplete());
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

		job1.setPercentageComplete(job1.getPercentageComplete() + 1);
		assertTrue(job1.getPercentageComplete() != job2.getPercentageComplete());
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());
	}

	@Test
	public void testStateMachineMainSequence() {
		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());

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

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.STARTED));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.STARTED));

		// 4. Update the percentage complete
		job.setPercentageComplete(random
				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(BatchJobStatus.STARTED));

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		assertTrue(job.getPercentageComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));

	}

	@Test
	public void testStatus() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			TransitivityJobBean job =
				new TransitivityJobBean("EXT ID: " + new Date().toString());
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

			// Remove the job and the number of remaining jobs
			controller.delete(job);
			assertTrue(initialCount == controller.findAll().size());
		}

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			TransitivityJobBean job =
				new TransitivityJobBean("EXT ID: " + new Date().toString());
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

			// Remove the job and the number of remaining jobs
			controller.delete(job);
			assertTrue(initialCount == controller.findAll().size());
		}

		assertTrue(initialCount == controller.findAll().size());
	}

	public void testTimestamp(BatchJobStatus sts) {
		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		TransitivityJobBean job =
			new TransitivityJobBean("EXT ID: " + new Date().toString());
		job.setStatus(sts);

		// Record a timestamp after the status is set
		final Date now2 = new Date();

		// Check that the expected value of the status
		final BatchJobStatus batchJobStatus = job.getStatus();
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
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			testTimestamp(sts);
		}

		assertTrue(initialCount == controller.findAll().size());
	}

}
