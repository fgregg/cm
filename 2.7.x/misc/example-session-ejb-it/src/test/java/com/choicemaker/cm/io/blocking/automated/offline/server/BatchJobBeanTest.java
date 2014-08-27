package com.choicemaker.cm.io.blocking.automated.offline.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.STATUS;

//import com.choicemaker.cm.io.blocking.automated.offline.server.BatchJobBean.k;

/*
 import javax.ejb.EJB;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.junit.Assert;
 */

@RunWith(Arquillian.class)
public class BatchJobBeanTest {

	private static final String MAVEN_COORDINATE_SEPARATOR = ":";

	public static final String PROJECT_POM = "pom.xml";

	public static final String DEPENDENCIES_POM =
		"src/test/dependencies/dependency-pom.xml";

	public static final String PERSISTENCE_CONFIGURATION =
		"src/test/resources/jboss/sqlserver/persistence.xml";

	public static final String EJB_MAVEN_GROUPID = "com.choicemaker.cm";

	public static final String EJB_MAVEN_ARTIFACTID =
		"com.choicemaker.cm.io.blocking.automated.offline.server";

	public static final String EJB_MAVEN_VERSION = "2.7.1-SNAPSHOT";

	public static final String EJB_MAVEN_COORDINATES = new StringBuilder()
			.append(EJB_MAVEN_GROUPID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_ARTIFACTID).append(MAVEN_COORDINATE_SEPARATOR)
			.append(EJB_MAVEN_VERSION).toString();

	public final int MAX_TEST_ITERATIONS = 10;

	private static final STATUS[] _nonterminal = EnumSet.of(STATUS.NEW,
			STATUS.QUEUED, STATUS.STARTED, STATUS.ABORT_REQUESTED).toArray(
			new STATUS[0]);

//	private static final STATUS[] _terminal = EnumSet.of(STATUS.COMPLETED,
//			STATUS.FAILED, STATUS.ABORTED, STATUS.CLEAR).toArray(new STATUS[0]);

	private final Random random = new Random(new Date().getTime());

	@EJB
	protected BatchJobController controller;

	private STATUS getRandomNonTerminalStatus() {
		int i = random.nextInt(_nonterminal.length);
		return _nonterminal[i];
	}

//	private STATUS getRandomTerminalStatus() {
//		int i = random.nextInt(_terminal.length);
//		return _terminal[i];
//	}

	@Test
	public void testBatchJobController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		Date now = new Date();
		BatchJobBean job = new BatchJobBean();
		Date now2 = new Date();

		assertTrue(0 == job.getId());

		assertTrue(STATUS.NEW.equals(job.getStatus()));

		Date d = job.getRequested();
		assertTrue(d != null);
		assertTrue(now.compareTo(d) <= 0);
		assertTrue(d.compareTo(now2) <= 0);

		Date d2 = job.getTimeStamp(STATUS.NEW);
		assertTrue(d.equals(d2));
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job
		BatchJobBean job = new BatchJobBean();
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
		BatchJobBean batchJob3 = controller.find(job.getId());
		assertTrue(batchJob3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		BatchJobBean job = new BatchJobBean();
		controller.save(job);
		assertTrue(job.getId() != 0);
		final long id = job.getId();
		controller.detach(job);

		assertTrue(job.getExternalId() == null);
		final String externalId = "external test id";
		job.setExternalId(externalId);
		controller.save(job);

		job = null;
		BatchJobBean batchJob2 = controller.find(id);
		assertTrue(id == batchJob2.getId());
		assertTrue(externalId.equals(batchJob2.getExternalId()));
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
			BatchJobBean job = new BatchJobBean();
			assertTrue(job.getId() == 0);
			controller.save(job);
			final long id = job.getId();
			assertTrue(id != 0);
			jobIds.add(id);
		}

		// Verify the number of jobs has increased
		List<BatchJobBean> jobs = controller.findAll();
		assertTrue(jobs != null);
		assertTrue(initialCount + MAX_TEST_ITERATIONS == jobs.size());

		// Find the jobs
		boolean isFound = false;
		for (long jobId : jobIds) {
			for (BatchJobBean job : jobs) {
				if (jobId == job.getId()) {
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}

		// Remove the job
		for (long id : jobIds) {
			BatchJobBean job = controller.find(id);
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
		for (BatchJobBean job : controller.findAll()) {
			++count;
			STATUS status = job.getStatus();
			if (status == null) {
				++countNullStatus;
			}
			Date ts = job.getTimeStamp(status);
			if (ts == null) {
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
		BatchJobBean job = new BatchJobBean();
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
		BatchJobBean job = new BatchJobBean();
		final long v1 = random.nextLong();
		job.setTransactionId(v1);

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		final long v2 = job.getTransactionId();
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
		BatchJobBean job = new BatchJobBean();
		assertTrue(BatchJobBean.TABLE_DISCRIMINATOR.equals(job.getType()));

		// Save the job
		final long id1 = controller.save(job).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		job = null;

		// Get the job
		job = controller.find(id1);

		// Check the value
		assertTrue(BatchJobBean.TABLE_DISCRIMINATOR.equals(job.getType()));

		// Remove the job and the number of remaining jobs
		controller.delete(job);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testDescription() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a job and set a value
		BatchJobBean job = new BatchJobBean();
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
		BatchJobBean job = new BatchJobBean();

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getPercentageComplete() == BatchJobBean.MIN_PERCENTAGE_COMPLETED);
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {

			final STATUS sts = getRandomNonTerminalStatus();
			final int v1 =
				random.nextInt(BatchJobBean.MAX_PERCENTAGE_COMPLETED + 1);
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
		BatchJobBean job1 = new BatchJobBean();
		BatchJobBean job2 = new BatchJobBean();
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());
		
		// Change something on one of the jobs and verify inequality
		job1.setDescription(new Date().toString());
		assertTrue(!job1.getDescription().equals(job2.getDescription()));
		assertTrue(!job1.equals(job2));
		assertTrue(job1.hashCode() != job2.hashCode());
		
		// Restore equality
		job2.setDescription(job1.getDescription());
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
		
		job1.setDescription(new Date().toString());
		assertTrue(!job1.getDescription().equals(job2.getDescription()));
		assertTrue(job1.equals(job2));
		assertTrue(job1.hashCode() == job2.hashCode());
	}

	@Test
	public void testStateMachineMainSequence() {
		// Record a timestamp before a transition is made
		Date before = new Date();

		// 1. Create a job and check the status
		BatchJobBean job = new BatchJobBean();

		// Record a timestamp after a transition is made
		Date after = new Date();

		// Check the status and timestamp
		assertTrue(job.getStatus().equals(STATUS.NEW));
		Date d = job.getTimeStamp(job.getStatus());
		assertTrue(d != null);
		assertTrue(before.compareTo(d) <= 0);
		assertTrue(after.compareTo(d) >= 0);
		Date d2 = job.getRequested();
		assertTrue(d.equals(d2));

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS.NEW));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS.NEW));

		// 2. Queue the job
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS.QUEUED));

		// Transitions out of sequence should be ignored
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS.QUEUED));

		// 3. Start the job
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS.STARTED));

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS.STARTED));

		// 4. Update the percentage complete
		job.setPercentageComplete(random
				.nextInt(BatchJobBean.MAX_PERCENTAGE_COMPLETED + 1));
		assertTrue(job.getStatus().equals(STATUS.STARTED));

		// 5. Mark the job as completed
		job.markAsCompleted();
		assertTrue(job.getStatus().equals(STATUS.COMPLETED));
		assertTrue(job.getPercentageComplete() == BatchJobBean.MAX_PERCENTAGE_COMPLETED);

		// Transitions out of sequence should be ignored
		job.markAsQueued();
		assertTrue(job.getStatus().equals(STATUS.COMPLETED));
		job.markAsStarted();
		assertTrue(job.getStatus().equals(STATUS.COMPLETED));
		job.markAsAbortRequested();
		assertTrue(job.getStatus().equals(STATUS.COMPLETED));

	}

	@Test
	public void testStatusAsString() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (STATUS sts : STATUS.values()) {
			BatchJobBean job = new BatchJobBean();
			job.setStatus(sts);
			assertTrue(sts.name().equals(job.getStatusAsString()));

			// Save the job
			final long id1 = controller.save(job).getId();
			assertTrue(initialCount + 1 == controller.findAll().size());
			job = null;

			// Retrieve the job
			job = controller.find(id1);

			// Check the value
			assertTrue(sts.name().equals(job.getStatusAsString()));

			// Remove the job and the number of remaining jobs
			controller.delete(job);
			assertTrue(initialCount == controller.findAll().size());
		}

		for (STATUS sts : STATUS.values()) {
			BatchJobBean job = new BatchJobBean();
			job.setStatusAsString(sts.name());
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

	public void testTimestamp(STATUS sts) {
		// Record a timestamp before status is set
		final Date now = new Date();

		// Set the status
		BatchJobBean job = new BatchJobBean();
		job.setStatus(sts);

		// Record a timestamp after the status is set
		final Date now2 = new Date();

		// Check that the expected value of the status
		final STATUS status = job.getStatus();
		assert (sts.equals(status));

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
		assertTrue(status.equals(job.getStatus()));
		assertTrue(ts.equals(job.getTimeStamp(sts)));

		// Clean up the test DB
		controller.delete(job);
	}

	@Test
	public void testTimestamps() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		for (STATUS sts : STATUS.values()) {
			testTimestamp(sts);
		}

		assertTrue(initialCount == controller.findAll().size());
	}

	public static JavaArchive createEjbJar() {
		// Create a copy of the EJB jar
		// PomEquippedResolveStage pom =
		// Maven.resolver().loadPomFromFile(PROJECT_POM);
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(PROJECT_POM);
		File jarFile =
			pom.resolve(EJB_MAVEN_COORDINATES).withoutTransitivity()
					.asSingleFile();
		JavaArchive retVal =
			ShrinkWrap.create(ZipImporter.class, "ejb.jar").importFrom(jarFile)
					.as(JavaArchive.class);

		// Flag this JAR to CDI as containing injectable beans
		retVal.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Add persistence configuration
		File f = new File(PERSISTENCE_CONFIGURATION);
		assertTrue(f.exists());
		FileAsset fileAsset = new FileAsset(f);
		retVal.addAsManifestResource(fileAsset, "persistence.xml");

		// Add test classes to JAR
		retVal.addClass(BatchJobBeanTest.class);
		retVal.addClass(BatchJobController.class);

		// Print the JAR contents
		System.out.println();
		System.out.println("EJB JAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

	public static File[] createTestDependencies() {

		// Break out steps for easier debugging
		File f0 = new File(DEPENDENCIES_POM);
		assertTrue(f0.exists());
		PomEquippedResolveStage pom =
			Maven.resolver().loadPomFromFile(DEPENDENCIES_POM);
		pom = pom.importDependencies(ScopeType.COMPILE);
		MavenStrategyStage mss = pom.resolve();
		assertTrue(mss != null);
		MavenFormatStage mfs = mss.withTransitivity();
		assertTrue(mfs != null);

		File[] retVal = mfs.asFile();

		// Print the dependencies
		System.out.println();
		System.out.println("Test dependencies:");
		for (File f : retVal) {
			System.out.println(f.getAbsolutePath());
		}
		System.out.println();

		return retVal;
	}

	@Deployment
	public static EnterpriseArchive createEarArchive() {

		// Create the EAR
		EnterpriseArchive retVal = ShrinkWrap.create(EnterpriseArchive.class);

		// Create and add the EJB
		JavaArchive ejb1 = createEjbJar();
		retVal.addAsModule(ejb1);
		// retVal.addAsLibrary(ejb1);

		// Add the EJB dependencies
		try {
			File[] deps = createTestDependencies();
			retVal.addAsLibraries(deps);
		} catch (Exception x) {
			String msg =
				"WARNING: failed to add test dependencies: " + x.toString();
			System.out.println(msg);
		}

		// Print the EAR contents
		System.out.println();
		System.out.println("Deployment EAR:");
		System.out.println(retVal.toString(true));
		System.out.println();
		return retVal;
	}

}
