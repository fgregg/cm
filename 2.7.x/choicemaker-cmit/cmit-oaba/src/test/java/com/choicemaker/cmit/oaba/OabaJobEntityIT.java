package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.j2ee.BatchJobUtils;
import com.choicemaker.cmit.utils.j2ee.EntityManagerUtils;
import com.choicemaker.cmit.utils.j2ee.TestEntityCounts;

@RunWith(Arquillian.class)
public class OabaJobEntityIT {

	private static final Logger logger = Logger.getLogger(OabaJobEntityIT.class
			.getName());

	public static final boolean TESTS_AS_EJB_MODULE = false;

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

	@EJB(beanName = "OabaJobControllerBean")
	private OabaJobController oabaController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private OabaService oabaService;

	@EJB
	private OperationalPropertyController opPropController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private ServerConfigurationController serverController;

	TestEntityCounts te;

	private final Random random = new Random(new Date().getTime());

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
	}

	public void checkCounts() {
		if (te != null) {
			te.checkCounts(logger, em, utx, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
		} else {
			throw new Error("Counts not initialized");
		}
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(oabaController != null);
		assertTrue(serverController != null);
	}

	@Test
	public void testConstruction() {
		final String METHOD = "testConstruction";

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

		checkCounts();
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";

		// Create a job
		BatchJob job = createEphemeralOabaJobEntity(te, METHOD, true);
		assertTrue(!job.isPersistent());

		// Save the job
		oabaController.save(job);
		assertTrue(job.isPersistent());

		// Find the job
		BatchJob batchJob2 = oabaController.findOabaJob(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		oabaController.delete(batchJob2);
		BatchJob batchJob3 = oabaController.findOabaJob(job.getId());
		assertTrue(batchJob3 == null);

		checkCounts();
	}

	@Test
	public void testFindAll() {
		final String METHOD = "testFindAll";

		List<Long> jobIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a job
			BatchJob job = createEphemeralOabaJobEntity(te, METHOD, true);
			assertTrue(!job.isPersistent());
			oabaController.save(job);
			te.add(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<BatchJob> jobs = oabaController.findAll();
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

		checkCounts();
	}

	@Test
	public void testExternalId() {
		final String METHOD = "testExternalId";

		// Create a job and set a value
		String extId = EntityManagerUtils.createExternalId(METHOD);
		boolean isTag = false;
		BatchJob job = createEphemeralOabaJobEntity(te, extId, isTag);
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

		checkCounts();
	}

	@Test
	public void testTransactionId() {
		final String METHOD = "testTransactionId";

		// Create a job and set a value
		BatchJob job = createEphemeralOabaJobEntity(te, METHOD, true);
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

		checkCounts();
	}

	/**
	 * Tests get/setDescription and merging a detached entity with an existing
	 * database entry
	 */
	@Test
	public void testMergeDescription() {
		final String METHOD = "testMergeDescription";

		// Create a job and set a value
		BatchJob job = createEphemeralOabaJobEntity(te, METHOD, true);
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

		checkCounts();
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";

		// Create two ephemeral jobs
		String exId = EntityManagerUtils.createExternalId(METHOD);
		boolean isTag = false;
		BatchJob job1 = createEphemeralOabaJobEntity(te, exId, isTag);
		assertTrue(te.contains(job1));
		BatchJob job2 = new OabaJobEntity(job1);
		te.add(job2);

		// Verify inequality of ephemeral instances
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

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

		checkCounts();
	}

	@Test
	public void testStateMachineMainSequence() {
		final String METHOD = "testStateMachineMainSequence";

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		BatchJob job = createEphemeralOabaJobEntity(te, METHOD, true);
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

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(BatchJobStatus.COMPLETED));

		checkCounts();
	}

	@Test
	public void testStatus() {
		final String METHOD = "testStatus";

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			OabaJobEntity entity =
				createEphemeralOabaJobEntity(te, METHOD, true);
			entity.setStatus(sts);
			assertTrue(sts.equals(entity.getStatus()));

			// Save the job
			final long id1 = oabaController.save(entity).getId();
			entity = null;

			// Retrieve the job
			BatchJob job = oabaController.findOabaJob(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and the number of remaining jobs
			oabaController.delete(job);
		}

		for (BatchJobStatus sts : BatchJobStatus.values()) {
			final OabaJobEntity entity =
				createEphemeralOabaJobEntity(te, METHOD, true);
			entity.setStatus(sts);
			assertTrue(sts.equals(entity.getStatus()));

			// Save the job
			final long id1 = oabaController.save(entity).getId();
			te.add(entity);

			// Retrieve the job
			final BatchJob job = oabaController.findOabaJob(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));
		}

		checkCounts();
	}

	public void testTimestamp(BatchJobStatus sts) {
		final String METHOD = "testTimestamp";

		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		final OabaJobEntity entity =
			createEphemeralOabaJobEntity(te, METHOD, true);
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
		final BatchJob job = oabaController.findOabaJob(id);
		assertTrue(batchJobStatus.equals(job.getStatus()));
		assertTrue(ts.equals(job.getTimeStamp(sts)));

		// Clean up the test DB
		oabaController.delete(job);

		checkCounts();
	}

	@Test
	public void testTimestamps() {
		for (BatchJobStatus sts : BatchJobStatus.values()) {
			testTimestamp(sts);
		}
	}

	protected OabaJobEntity createEphemeralOabaJobEntity(TestEntityCounts te,
			String tag, boolean isTag) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		if (sc == null) {
			sc = serverController.computeGenericConfiguration();
		}
		return BatchJobUtils.createEphemeralOabaJobEntity(MAX_SINGLE_LIMIT,
				utx, sc, em, te, tag, isTag);
	}

	protected ServerConfiguration getDefaultServerConfiguration() {
		return BatchJobUtils.getDefaultServerConfiguration(serverController);
	}

}
