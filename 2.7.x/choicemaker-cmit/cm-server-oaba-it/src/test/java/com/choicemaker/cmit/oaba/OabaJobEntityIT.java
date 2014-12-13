package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.args.AbaSettings.DEFAULT_LIMIT_PER_BLOCKING_SET;
import static com.choicemaker.cm.args.AbaSettings.DEFAULT_LIMIT_SINGLE_BLOCKING_SET;
import static com.choicemaker.cm.args.AbaSettings.DEFAULT_SINGLE_TABLE_GRACE_LIMIT;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_INTERVAL;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_BLOCKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_CHUNKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_MATCHES;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_OVERSIZED;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MIN_FIELDS;
import static com.choicemaker.cm.batch.BatchJob.STATUS_ABORTED;
import static com.choicemaker.cm.batch.BatchJob.STATUS_ABORT_REQUESTED;
import static com.choicemaker.cm.batch.BatchJob.STATUS_CLEAR;
import static com.choicemaker.cm.batch.BatchJob.STATUS_COMPLETED;
import static com.choicemaker.cm.batch.BatchJob.STATUS_FAILED;
import static com.choicemaker.cm.batch.BatchJob.STATUS_NEW;
import static com.choicemaker.cm.batch.BatchJob.STATUS_QUEUED;
import static com.choicemaker.cm.batch.BatchJob.STATUS_STARTED;
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

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.oaba.util.OabaTestController;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class OabaJobEntityIT {

	private static final Logger logger = Logger.getLogger(OabaJobEntityIT.class
			.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final int MAX_MAX_SINGLE = 1000;

	public static final int MAX_TEST_ITERATIONS = 10;

	private static final String[] _statusValues =
		new String[] {
				STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_COMPLETED,
				STATUS_FAILED, STATUS_ABORT_REQUESTED, STATUS_ABORTED,
				STATUS_CLEAR };

	private static final String[] _nonterminal = new String[] {
			STATUS_NEW, STATUS_QUEUED, STATUS_STARTED, STATUS_ABORT_REQUESTED };

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

	private final Random random = new Random(new Date().getTime());

	private int initialOabaParamsCount;
	private int initialOabaJobCount;

	// private int initialTransitivityJobCount;

	private String getRandomNonTerminalStatus() {
		int i = random.nextInt(_nonterminal.length);
		return _nonterminal[i];
	}

	@Before
	public void setUp() {
		initialOabaParamsCount = oabaTestController.findAllOabaParameters().size();
		initialOabaJobCount = oabaTestController.findAllOabaJobs().size();
	}

	@After
	public void tearDown() {
		int finalOabaParamsCount = oabaTestController.findAllOabaParameters().size();
		assertTrue(initialOabaParamsCount == finalOabaParamsCount);

		int finalOabaJobCount = oabaTestController.findAllOabaJobs().size();
		assertTrue(initialOabaJobCount == finalOabaJobCount);
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
		OabaJob job = createEphemeralOabaJob(METHOD, te);
		Date now2 = new Date();

		assertTrue(0 == job.getId());

		assertTrue(STATUS_NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(STATUS_NEW);
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
		OabaJob job = createEphemeralOabaJob(METHOD, te);
		assertTrue(job.getId() == 0);

		// Save the job
		oabaController.save(job);
		assertTrue(job.getId() != 0);

		// Find the job
		OabaJob batchJob2 = oabaController.find(job.getId());
		assertTrue(job.getId() == batchJob2.getId());
		assertTrue(job.equals(batchJob2));

		// Delete the job
		oabaController.delete(batchJob2);
		OabaJob batchJob3 = oabaController.find(job.getId());
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
			OabaJob job = createEphemeralOabaJob(METHOD, te);
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
		OabaJob job = createEphemeralOabaJob(te, extId);
		assertTrue(extId.equals(job.getExternalId()));

		// Save the job
		final long id1 = oabaController.save(job).getId();
		te.add(job);

		// Retrieve the job
		job = null;
		job = oabaController.find(id1);

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
		OabaJob job = createEphemeralOabaJob(METHOD, te);
		final long v1 = random.nextLong();
		job.setDescription("" + v1);

		// Save the job
		final long id1 = oabaController.save(job).getId();
		te.add(job);
		job = null;

		// Get the job
		job = oabaController.find(id1);

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
		OabaJob job = createEphemeralOabaJob(METHOD, te);
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
		job = oabaController.find(id1);

		// Check the value
		assertTrue(description2.equals(job.getDescription()));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testPercentageComplete() {
		final String METHOD = "testPercentageComplete";
		final TestEntities te = new TestEntities();

		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the percentage complete
		OabaJob job = createEphemeralOabaJob(METHOD, te);

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getFractionComplete() == OabaJob.MIN_PERCENTAGE_COMPLETED);
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
			final long id1 = oabaController.save(job).getId();
			job = null;

			// Retrieve the job
			job = oabaController.find(id1);

			// Re-check the percentage, status and timestamp
			v2 = job.getFractionComplete();
			assertTrue(v2 == v1);

			assertTrue(job.getStatus().equals(sts));

			d = job.getTimeStamp(job.getStatus());
			assertTrue(d != null);
			assertTrue(before.compareTo(d) <= 0);
			assertTrue(after.compareTo(d) >= 0);

			// Remove the job and the number of remaining jobs
			oabaController.delete(job);
		}

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		final TestEntities te = new TestEntities();

		// Create two ephemeral jobs
		String exId = EntityManagerUtils.createExternalId(METHOD);
		OabaJob job1 = createEphemeralOabaJob(te, exId);
		assertTrue(te.contains(job1));
		OabaJob job2 = new OabaJobEntity(job1);
		te.add(job2);

		// Verify equality of ephemeral instances
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
		job1 = oabaController.save(job1);
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());

		// Verify that equality of persisted jobs is set only by persistence id
		oabaController.detach(job1);
		job2 = oabaController.find(job1.getId());
		oabaController.detach(job2);
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

		job1.setFractionComplete(job1.getFractionComplete() + 1);
		assertTrue(job1.getFractionComplete() != job2.getFractionComplete());
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());

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
		OabaJob job = createEphemeralOabaJob(METHOD, te);
		// oabaTestController.save(job);

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
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// oabaTestController.save(job);

		// 4. Update the percentage complete
		job.setFractionComplete(random
				.nextInt(BatchJob.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(STATUS_STARTED));
		// oabaTestController.save(job);

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		assertTrue(job.getFractionComplete() == BatchJob.MAX_PERCENTAGE_COMPLETED);
		// oabaTestController.save(job);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(STATUS_COMPLETED));

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

		for (String sts : _statusValues) {
			OabaJob job = createEphemeralOabaJob(METHOD, te);
			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = oabaController.save(job).getId();
			job = null;

			// Retrieve the job
			job = oabaController.find(id1);

			// Check the value
			assertTrue(sts.equals(job.getStatus()));

			// Remove the job and the number of remaining jobs
			oabaController.delete(job);
		}

		for (String sts : _statusValues) {
			OabaJob job = createEphemeralOabaJob(METHOD, te);
			job.setStatus(sts);
			assertTrue(sts.equals(job.getStatus()));

			// Save the job
			final long id1 = oabaController.save(job).getId();
			te.add(job);
			job = null;

			// Retrieve the job
			job = oabaController.find(id1);

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

	public void testTimestamp(String sts) {
		final String METHOD = "testTimestamp";
		final TestEntities te = new TestEntities();

		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		OabaJob job = createEphemeralOabaJob(METHOD, te);
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
		final long id = oabaController.save(job).getId();

		// Find the job and verify the expected status and timestamp
		job = null;
		job = oabaController.find(id);
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
		for (String sts : _statusValues) {
			testTimestamp(sts);
		}
	}

	public OabaJob createEphemeralOabaJob(String tag, TestEntities te) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		OabaJob retVal = null;
		try {
			utx.begin();
			retVal = createEphemeralOabaJob(sc, em, tag, te);
			utx.commit();
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	public OabaJob createEphemeralOabaJob(TestEntities te, String extId) {
		ServerConfiguration sc = getDefaultServerConfiguration();
		OabaJob retVal = null;
		try {
			utx.begin();
			retVal = createEphemeralOabaJob(sc, em, te, extId);
			utx.commit();
		} catch (Exception x) {
			fail(x.toString());
		}
		assertTrue(retVal != null);
		return retVal;
	}

	public ServerConfiguration getDefaultServerConfiguration() {
		String hostName = ServerConfigurationControllerBean.computeHostName();
		final boolean computeFallback = true;
		ServerConfiguration retVal =
			serverController.getDefaultConfiguration(hostName, computeFallback);
		assert retVal != null;
		assert retVal.getId() != ServerConfigurationControllerBean.INVALID_ID;
		return retVal;
	}

	/**
	 * Creates an ephemeral instance of OabaParametersEntity. An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	protected OabaJobEntity createEphemeralOabaJob(ServerConfiguration sc,
			EntityManager em, String tag, TestEntities te) {
		return createEphemeralOabaJob(sc, em, te, EntityManagerUtils.createExternalId(tag));
	}

	/**
	 * Creates an ephemeral instance of OabaParametersEntity. The specified
	 * externalId is assigned without alteration to the returned OabaJob.
	 */
	protected OabaJobEntity createEphemeralOabaJob(ServerConfiguration sc,
			EntityManager em, TestEntities te, String extId) {
		final String METHOD = "createEphemeralOabaJob";
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		OabaParametersEntity params =
			createPersistentOabaParameters(em, METHOD, te);
		OabaSettingsEntity settings =
			createPersistentOabaSettings(em, METHOD, te);
		OabaJobEntity retVal = new OabaJobEntity(params, settings, sc, extId);
		te.add(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of OabaParametersEntity */
	public OabaParametersEntity createEphemeralOabaParameters(
			String tag, TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		PersistableRecordSource stage = EntityManagerUtils.createFakePersistableRecordSource(tag);
		OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
				EntityManagerUtils.createFakePersistableRecordSource(tag, task);
		OabaParametersEntity retVal =
			new OabaParametersEntity(EntityManagerUtils.createRandomModelConfigurationName(tag),
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaParametersEntity An externalId for
	 * the returned OabaJob is synthesized using the specified tag.
	 */
	public OabaParametersEntity createPersistentOabaParameters(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaParametersEntity retVal = createEphemeralOabaParameters(tag, te);
		em.persist(retVal);
		return retVal;
	}

	/** Creates an ephemeral instance of OabaSettingsEntity */
	public OabaSettingsEntity createEphemeralOabaSettings(String tag,
			TestEntities te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		int limPerBlockingSet = random.nextInt(DEFAULT_LIMIT_PER_BLOCKING_SET);
		int limSingleBlockingSet =
			random.nextInt(DEFAULT_LIMIT_SINGLE_BLOCKING_SET);
		int singleTableGraceLimit =
			random.nextInt(DEFAULT_SINGLE_TABLE_GRACE_LIMIT);
		int maxSingle = random.nextInt(MAX_MAX_SINGLE);
		int maxBlockSize = random.nextInt(DEFAULT_MAX_BLOCKSIZE);
		int maxChunkSize = random.nextInt(DEFAULT_MAX_CHUNKSIZE);
		int maxMatches = random.nextInt(DEFAULT_MAX_MATCHES);
		int maxOversized = random.nextInt(DEFAULT_MAX_OVERSIZED);
		int minFields = random.nextInt(DEFAULT_MIN_FIELDS);
		int interval = random.nextInt(DEFAULT_INTERVAL);
		OabaSettingsEntity retVal =
			new OabaSettingsEntity(limPerBlockingSet, limSingleBlockingSet,
					singleTableGraceLimit, maxSingle, maxBlockSize, maxMatches,
					maxChunkSize, maxOversized, minFields, interval);
		te.add(retVal);
		return retVal;
	}

	/**
	 * Creates a persistent instance of OabaSettingsEntity An externalId for the
	 * returned OabaJob is synthesized using the specified tag.
	 */
	public OabaSettingsEntity createPersistentOabaSettings(
			EntityManager em, String tag, TestEntities te) {
		if (em == null) {
			throw new IllegalArgumentException("null entity manager");
		}
		OabaSettingsEntity retVal = createEphemeralOabaSettings(tag, te);
		em.persist(retVal);
		return retVal;
	}

}
