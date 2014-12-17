package com.choicemaker.cmit.trans;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Random;

import javax.ejb.EJB;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobControllerBean;
import com.choicemaker.cm.transitivity.server.impl.TransitivityJobEntity;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cm.transitivity.server.impl.TransitivitySettingsEntity;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.trans.util.TransitivityTestController;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class TransitivityJobEntityIT {
	
//	private static final Logger logger = Logger
//			.getLogger(TransitivityJobEntityIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

//	private static final Logger logger = Logger
//			.getLogger(TransitivityJobEntityIT.class.getName());
//
//	public static final int MAX_TEST_ITERATIONS = 10;
//
//	private static final String[] _statusValues =
//		new String[] {
//				STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_COMPLETED,
//				STATUS_FAILED, STATUS_ABORT_REQUESTED, STATUS_ABORTED,
//				STATUS_CLEAR };

	// private static final String[] _nonterminal = new String[] {
	// STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_ABORT_REQUESTED };
	//
	// private String getRandomNonTerminalStatus() {
	// int i = random.nextInt(_nonterminal.length);
	// return _nonterminal[i];
	// }

//	private final Random random = new Random(new Date().getTime());

	@EJB
	protected TransitivityJobControllerBean tjController;

	@EJB
	protected TransitivityTestController testController;

	private final Random random = new Random(new Date().getTime());

//	private int initialOabaParamsCount;
//	private int initialOabaJobCount;
//	private int initialCount;

	@Before
	public void setUp() {
//		initialOabaParamsCount = tjController.findAllTransitivityParameters().size();
//		initialOabaJobCount = tjController.findAllOabaJobs().size();
//		initialCount = tjController.findAllTransitivityJobs().size();
	}

	@After
	public void tearDown() {
//		int finalOabaParamsCount = tjController.findAllTransitivityParameters().size();
//		assertTrue(initialOabaParamsCount == finalOabaParamsCount);
//
//		int finalOabaJobCount = tjController.findAllOabaJobs().size();
//		assertTrue(initialOabaJobCount == finalOabaJobCount);
//
//		int finalTransJobCount = tjController.findAllTransitivityJobs().size();
//		assertTrue(initialCount == finalTransJobCount);
	}

	@Test
	public void testTransitivityJobController() {
		assertTrue(tjController != null);
	}

	@Test
	public void testConstruction() {
//		final String METHOD = "testConstruction";
//		final TestEntities te = new TestEntities();
//
//		OabaJob batchJob = tjController.createPersistentOabaJobBean(METHOD, te);
//		final Date now = new Date();
//		TransitivityJob job =
//			tjController.createEphemeralTransitivityJob(METHOD, te, batchJob);
//		final Date now2 = new Date();
//
//		// Check that primary hasn't been set
//		assertTrue(0 == job.getId());
//
//		// Check that the predecessor and parameters have been set
//		assertTrue(job.getBatchParentId() == batchJob.getId());
//		long paramsId = batchJob.getParametersId();
//		assertTrue(paramsId != BatchJobJPA.INVALID_ID);
//		assertTrue(paramsId == job.getParametersId());
//
//		// Check the status and associated timestamps
//		assertTrue(STATUS_NEW.equals(job.getStatus()));
//		Date d2 = job.getTimeStamp(STATUS_NEW);
//		assertTrue(d2 != null);
//		assertTrue(now.compareTo(d2) <= 0);
//		assertTrue(d2.compareTo(now2) <= 0);
//		Date d = job.getRequested();
//		assertTrue(d2.equals(d));
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testPersistFindRemove() {
//		final String METHOD = "testPersistFindRemove";
//		final TestEntities te = new TestEntities();
//
//		// Create a job
//		TransitivityJob job = tjController.createEphemeralTransitivityJob(METHOD, te);
//		assertTrue(job.getId() == 0);
//
//		// Save the job
//		tjController.save(job);
//		assertTrue(job.getId() != 0);
//
//		// Find the job
//		TransitivityJob batchJob2 = tjController.findTransitivityJob(job.getId());
//		assertTrue(job.getId() == batchJob2.getId());
//		assertTrue(job.equals(batchJob2));
//
//		// Remove test entities from database
//		tjController.removeTestEntities(te);
//
//		// Delete the job
//		TransitivityJob batchJob3 = tjController.findTransitivityJob(job.getId());
//		assertTrue(batchJob3 == null);
	}

	/**
	 * Tests merging of a modified transitivity job back into the database, but
	 * also tests get/setDescription methods.
	 */
	@Test
	public void testMerge() {
//		final String METHOD = "testMerge";
//		final TestEntities te = new TestEntities();
//
//		TransitivityJob job = tjController.createEphemeralTransitivityJob(METHOD, te);
//		assertTrue(null == job.getDescription());
//		final String description = "some job description";
//		job.setDescription(description);
//		assertTrue(description.equals(job.getDescription()));
//
//		tjController.save(job);
//		final long id = job.getId();
//		tjController.detach(job);
//
//		job = null;
//		TransitivityJob job2 = tjController.findTransitivityJob(id);
//		assertTrue(id == job2.getId());
//		assertTrue(description.equals(job2.getDescription()));
//
//		final String description2 = "some new job description";
//		assertTrue(!description2.equals(description));
//		job2.setDescription(description2);
//		tjController.save(job2);
//
//		job2 = null;
//		TransitivityJob job3 = tjController.findTransitivityJob(id);
//		assertTrue(id == job3.getId());
//		assertTrue(description2.equals(job3.getDescription()));
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testFindAll() {
//		final String METHOD = "testFindAll";
//		final TestEntities te = new TestEntities();
//
//		List<Long> jobIds = new LinkedList<>();
//		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
//			// Create and save a job
//			TransitivityJob job =
//				tjController.createEphemeralTransitivityJob(METHOD, te);
//			tjController.save(job);
//			long id = job.getId();
//			assertTrue(!jobIds.contains(id));
//			jobIds.add(id);
//		}
//
//		// Verify the number of jobs has increased
//		List<TransitivityJob> jobs = tjController.findAllTransitivityJobs();
//		assertTrue(jobs != null);
//		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());
//
//		// Find the jobs that have been created
//		boolean isFound = false;
//		for (long jobId : jobIds) {
//			for (TransitivityJob job : jobs) {
//				if (jobId == job.getId()) {
//					isFound = true;
//					break;
//				}
//			}
//			assertTrue(isFound);
//		}
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testFindAllByParentId() {
//		final String METHOD = "testFindAllByParentId";
//		final TestEntities te = new TestEntities();
//
//		final OabaJob batchJob = tjController.createPersistentOabaJobBean(METHOD, te);
//		final long batchJobId = batchJob.getId();
//		Set<Long> jobIds = new HashSet<>();
//		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
//			// Create and save a job
//			TransitivityJob job =
//				tjController.createEphemeralTransitivityJob(METHOD, te, batchJob);
//			tjController.save(job);
//			final long id = job.getId();
//			assertTrue(!jobIds.contains(id));
//			jobIds.add(id);
//		}
//
//		// Verify the number of jobs has increased
//		List<TransitivityJob> jobs = tjController.findAllTransitivityJobs();
//		assertTrue(jobs != null);
//		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());
//
//		// Find the jobs by querying
//		jobs = tjController.findAllByParentId(batchJobId);
//		assertTrue(jobs != null);
//		assertTrue(jobs.size() == jobIds.size());
//		for (TransitivityJob job : jobs) {
//			assertTrue(jobIds.contains(job.getId()));
//		}
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testNoNullStatusTimestamp() {
//		final TestEntities te = new TestEntities();
//
//		int countNullStatus = 0;
//		int countNullTimestamp = 0;
//		int count = 0;
//		for (TransitivityJob job : tjController.findAllTransitivityJobs()) {
//			++count;
//			String batchJobStatus = job.getStatus();
//			if (batchJobStatus == null) {
//				logger.severe(job.getId() + ": " + count + ": null status");
//				++countNullStatus;
//			}
//			Date ts = job.getTimeStamp(batchJobStatus);
//			if (ts == null) {
//				String msg =
//					job.getId() + ": " + count
//							+ ": null timestamp for status '" + batchJobStatus
//							+ "'";
//				logger.severe(msg);
//				++countNullTimestamp;
//			}
//		}
//		if ((countNullStatus) != 0) {
//			fail("Null status: " + countNullStatus + " out of " + count);
//		}
//		if ((countNullTimestamp) != 0) {
//			fail("Null timestamp: " + countNullTimestamp + " out of " + count);
//		}
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testFractionComplete() {
//		final String METHOD = "testFractionComplete";
//		final TestEntities te = new TestEntities();
//
//		// Record a timestamp before a transition is made
//		Date before = new Date();
//
//		// 1. Create a job and mark it as running (a.k.a. 'STARTED')
//		TransitivityJob job = tjController.createEphemeralTransitivityJob(METHOD, te);
//		job.markAsQueued();
//		job.markAsStarted();
//
//		// Record a timestamp after a transition is made
//		Date after = new Date();
//
//		// Check the timestamp and the percentage complete
//		assertTrue(job.getFractionComplete() == BatchJob.MIN_PERCENTAGE_COMPLETED);
//		final String sts = job.getStatus();
//		assertTrue(BatchJob.STATUS_STARTED.equals(sts));
//		Date d = job.getTimeStamp(sts);
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		Date d2 = job.getStarted();
//		assertTrue(d.equals(d2));
//
//		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
//
//			final int v1 =
//				random.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1);
//			before = new Date();
//			job.setFractionComplete(v1);
//			after = new Date();
//
//			// Check the percentage, status and timestamp
//			assertTrue(job.getFractionComplete() == v1);
//			assertTrue(job.getStatus().equals(sts));
//			Date d3 = job.getTimeStamp(job.getStatus());
//			assertTrue(d3 != null);
//			assertTrue(before.compareTo(d3) <= 0);
//			assertTrue(after.compareTo(d3) >= 0);
//
//			// Save the job
//			final long id1 = tjController.save(job).getId();
//
//			// Retrieve the job and re-check the percentage, status and
//			// timestamp
//			job = null;
//			job = tjController.findTransitivityJob(id1);
//			assertTrue(job != null);
//			assertTrue(job.getFractionComplete() == v1);
//			assertTrue(job.getStatus().equals(sts));
//			assertTrue(job.getTimeStamp(sts).equals(d3));
//		}
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testEqualsHashCode() {
//		final String METHOD = "testEqualsHashCode";
//		final TestEntities te = new TestEntities();
//
//		// Create two generic jobs and verify equality
//		String extId = EntityManagerUtils.createExternalId(METHOD);
//		final OabaJob batchJob = tjController.createPersistentOabaJobBean(METHOD, te);
//		TransitivityJob job1 =
//			tjController.createEphemeralTransitivityJob(te, batchJob, extId);
//		assertTrue(te.contains(job1));
//		TransitivityJob job2 = new TransitivityJobEntity(job1);
//		te.add(job2);
//
//		//  Verify equality of ephemeral instances
//		assertTrue(job1.equals(job2));
//		assertTrue(job1.hashCode() == job2.hashCode());
//
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
//
//		// Verify a non-persistent job is not equal to a persistent job
//		job1 = tjController.save(job1);
//		assertTrue(!job1.equals(job2));
//		assertTrue(job1.hashCode() != job2.hashCode());
//
//		// Verify that equality of persisted jobs is set only by persistence id
//		tjController.detach(job1);
//		job2 = tjController.findTransitivityJob(job1.getId());
//		tjController.detach(job2);
//		assertTrue(job1.equals(job2));
//		assertTrue(job1.hashCode() == job2.hashCode());
//
//		job1.setFractionComplete(job1.getFractionComplete() + 1);
//		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
//		assertTrue(job1.equals(job2));
//		assertTrue(job1.hashCode() == job2.hashCode());
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testStateMachineMainSequence() {
//		final String METHOD = "testStateMachineMainSequence";
//		final TestEntities te = new TestEntities();
//
//		// 1. Create a job and check the status
//		Date before = new Date();
//		TransitivityJob job = tjController.createEphemeralTransitivityJob(METHOD, te);
//		Date after = new Date();
//
//		// Check the status and timestamp
//		assertTrue(job.getStatus().equals(STATUS_NEW));
//		Date d = job.getTimeStamp(job.getStatus());
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		Date d2 = job.getRequested();
//		assertTrue(d.equals(d2));
//
//		// Transitions out of sequence should be ignored
//		job.markAsCompleted();
//		assertTrue(job.getStatus().equals(STATUS_NEW));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//		job.markAsStarted();
//		assertTrue(job.getStatus().equals(STATUS_NEW));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//
//		// 2. Queue the job
//		before = new Date();
//		job.markAsQueued();
//		after = new Date();
//		assertTrue(job.getStatus().equals(STATUS_QUEUED));
//		d = job.getTimeStamp(job.getStatus());
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		d2 = job.getQueued();
//		assertTrue(d.equals(d2));
//
//		// Transitions out of sequence should be ignored
//		job.markAsCompleted();
//		assertTrue(job.getStatus().equals(STATUS_QUEUED));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//
//		// 3. Start the job
//		before = new Date();
//		job.markAsStarted();
//		after = new Date();
//		assertTrue(job.getStatus().equals(STATUS_STARTED));
//		d = job.getTimeStamp(job.getStatus());
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		d2 = job.getStarted();
//		assertTrue(d.equals(d2));
//
//		// Transitions out of sequence should be ignored
//		job.markAsQueued();
//		assertTrue(job.getStatus().equals(STATUS_STARTED));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//
//		// 4. Update the percentage complete
//		job.setFractionComplete(random
//				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
//		assertTrue(job.getStatus().equals(STATUS_STARTED));
//
//		// 5. Mark the job as completed
//		before = new Date();
//		job.markAsCompleted();
//		after = new Date();
//		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
//		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);
//		d = job.getTimeStamp(job.getStatus());
//		assertTrue(d != null);
//		assertTrue(before.compareTo(d) <= 0);
//		assertTrue(after.compareTo(d) >= 0);
//		d2 = job.getCompleted();
//		assertTrue(d.equals(d2));
//
//		// Transitions out of sequence should be ignored
//		job.markAsQueued();
//		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//		job.markAsStarted();
//		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//		job.markAsAbortRequested();
//		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
//		assertTrue(job.getTimeStamp(job.getStatus()).equals(d));
//
//		tjController.removeTestEntities(te);
	}

	@Test
	public void testStatus() {
//		final String METHOD = "testStatus";
//		final TestEntities te = new TestEntities();
//
//		for (String sts : _statusValues) {
//			TransitivityJob job =
//				tjController.createEphemeralTransitivityJob(METHOD, te);
//			assertTrue(BatchJob.STATUS_NEW.equals(job.getStatus()));
//			job.setStatus(sts);
//			assertTrue(sts.equals(job.getStatus()));
//
//			// Save the job
//			final long id1 = tjController.save(job).getId();
//			job = null;
//
//			// Retrieve the job
//			job = tjController.findTransitivityJob(id1);
//
//			// Check the value
//			assertTrue(sts.equals(job.getStatus()));
//		}
//
//		tjController.removeTestEntities(te);
	}

	public void testTimestamp(final String sts) {
		if (sts == null) {
			throw new IllegalArgumentException("null status");
		}
		final String METHOD = "testTimestamp";
		final TestEntities te = new TestEntities();

		final Date before = new Date();
		TransitivityJob job = createEphemeralTransitivityJob(METHOD, te);
		final Date after = new Date();
		job.setStatus(sts);

		// Check the expected status and timestamp
		assertTrue(sts.equals(job.getStatus()));
		final Date d = job.getTimeStamp(sts);
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(d.compareTo(after) <= 0);

		// Save the job
		final long id = save(job).getId();

		// Find the job and verify the expected status and timestamp
		job = null;
		job = tjController.findTransitivityJob(id);
		assertTrue(sts.equals(job.getStatus()));
		assertTrue(d.equals(job.getTimeStamp(sts)));

		testController.removeTestEntities(te);
	}

	protected TransitivityJob save(TransitivityJob job) {
		// TODO stub
		throw new Error("not yet implemented");
	}

	protected TransitivityJob createEphemeralTransitivityJob(String tag,
			TestEntities te) {
		// TODO stub
		throw new Error("not yet implemented");
	}

	@Test
	protected void testTimestamps() {
//		for (String sts : _statusValues) {
//			testTimestamp(sts);
//		}
	}

	/** Creates an ephemeral instance of TransitivityParametersEntity */
	protected TransitivityParametersEntity
	createEphemeralTransitivityParameters(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		ISerializableRecordSource stage = EntityManagerUtils.createFakeSerialRecordSource(tag);
		ISerializableRecordSource master;
		if (random.nextBoolean()) {
			master = EntityManagerUtils.createFakeSerialRecordSource(tag);
		} else {
			master = null;
		}
		// File workingDir =
		// ServerConfigurationControllerBean.computeGenericLocation();
		TransitivityParametersEntity retVal =
			new TransitivityParametersEntity(
					EntityManagerUtils.createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of TransitivityParametersEntity An
	 * externalId for the returned TransitivityJob is synthesized using the
	 * specified tag.
	 */
	protected TransitivityParametersEntity createPersistentTransitivityParameters(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		TransitivityParametersEntity retVal =
			createEphemeralTransitivityParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of TransitivitySettingsEntity */
	protected TransitivitySettingsEntity createEphemeralTransitivitySettings(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		ISerializableRecordSource stage = EntityManagerUtils.createFakeSerialRecordSource(tag);
		ISerializableRecordSource master;
		if (random.nextBoolean()) {
			master = EntityManagerUtils.createFakeSerialRecordSource(tag);
		} else {
			master = null;
		}
		// File workingDir =
		// ServerConfigurationControllerBean.computeGenericLocation();
		TransitivitySettingsEntity retVal =
			new TransitivitySettingsEntity(
					EntityManagerUtils.createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of TransitivitySettingsEntity An externalId
	 * for the returned TransitivityJob is synthesized using the specified tag.
	 */
	protected TransitivitySettingsEntity createPersistentTransitivitySettings(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		TransitivitySettingsEntity retVal =
			createEphemeralTransitivitySettings(tag, te);
		em.persist(retVal);
		return retVal;
	}

//	protected TransitivityJobEntity createEphemeralTransitivityJob(
//			EntityManager em, String tag, TestEntities te, OabaJob job) {
//		if (te == null) {
//			throw new IllegalArgumentException("null test entities");
//		}
//		if (job == null) {
//			throw new IllegalArgumentException("null batch job");
//		}
//		if (!te.contains(job)) {
//			logger.warning("Adding batchJob '" + job
//					+ "' to test entities that will be removed from database");
//			te.add(job);
//		}
//		String extId = EntityManagerUtils.createExternalId(tag);
//		TransitivityParametersEntity params =
//			em.find(TransitivityParametersEntity.class, job.getTransitivityParametersId());
//		if (params == null) {
//			throw new IllegalArgumentException("non-persistent parameters");
//		}
//		if (!te.contains(params)) {
//			logger.warning("Adding batchJob '" + job
//					+ "' to test entities that will be removed from database");
//			te.add(params);
//		}
//		TransitivityJobEntity retVal =
//			new TransitivityJobEntity(params, job, extId);
//		te.add((TransitivityJob)retVal);
//		return retVal;
//	}

	protected TransitivityJobEntity createEphemeralTransitivityJob(
			ServerConfiguration sc, EntityManager em, String tag,
			TestEntities te) {
		throw new Error("not yet implemented");
//		TransitivityParametersEntity params =
//			createPersistentTransitivityParameters(em, tag, te);
//		OabaJobEntity job = createPersistentOabaJobBean(sc, em, tag, te);
//		TransitivityJobEntity retVal = new TransitivityJobEntity(params, job);
//		te.add(retVal);
//		return retVal;
	}

	protected TransitivityJobEntity createEphemeralTransitivityJob(
			EntityManager em, TestEntities te, OabaJob job, String extId) {
		throw new Error("not yet implemented");
//		TransitivityParametersEntity params =
//			createPersistentTransitivityParameters(em, null, te);
//		TransitivityJobEntity retVal =
//			new TransitivityJobEntity(params, job, extId);
//		if (te == null) {
//			throw new IllegalArgumentException("null test entities");
//		}
//		te.add(retVal);
//		return retVal;
	}

}
