package com.choicemaker.cmit.persist0;

import static com.choicemaker.cmit.persist0.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils0.DeploymentUtils.PROJECT_POM;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.persist0.OfflineMatchingBean;
import com.choicemaker.cm.persist0.StatusLog;
import com.choicemaker.cm.persist0.StatusLogBean;
import com.choicemaker.cmit.utils0.DeploymentUtils;

@RunWith(Arquillian.class)
public class StatusLogBeanTest {

	public final int MAX_TEST_ITERATIONS = 10;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(StatusLogBeanTest.class);
		testClasses.add(StatusLogController.class);
		testClasses.add(OfflineMatchingController.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(PROJECT_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

	private final Random random = new Random(new Date().getTime());

	@EJB
	protected OfflineMatchingController jobController;
	@EJB
	protected StatusLogController stsController;

	@Test
	public void testBatchParametersController() {
		assertTrue(jobController != null);
		assertTrue(stsController != null);
	}

	@Test
	public void testConstruction() {
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);
		assertTrue(job.getId() == statusEntry.getJobId());
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		// Create a statusEntry
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		assertTrue(statusEntry.getJobId() == job.getId());

		// Save the statusEntry
		stsController.save(statusEntry);
		assertTrue(statusEntry.getJobId() != 0);

		// Find the statusEntry
		StatusLogBean statusEntry2 = stsController.find(statusEntry.getJobId());
		assertTrue(statusEntry.getJobId() == statusEntry2.getJobId());
		assertTrue(statusEntry.equals(statusEntry2));

		// Delete the statusEntry
		stsController.delete(statusEntry2);
		StatusLog statusEntry3 = stsController.find(statusEntry.getJobId());
		assertTrue(statusEntry3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == stsController.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		stsController.save(statusEntry);
		assertTrue(statusEntry.getJobId() != 0);
		final long id = statusEntry.getJobId();
		stsController.detach(statusEntry);

		assertTrue(statusEntry.getInfo() == null);
		final String randomInfo = new Date().toString();
		statusEntry.setInfo(randomInfo);
		stsController.save(statusEntry);

		statusEntry = null;
		StatusLogBean statusEntry2 = stsController.find(id);
		assertTrue(id == statusEntry2.getJobId());
		assertTrue(randomInfo == statusEntry2.getInfo());
		stsController.delete(statusEntry2);

		assertTrue(initialCount == stsController.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic parameter sets and verify equality
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry1 = new StatusLogBean(job);
		StatusLog statusEntry2 = new StatusLogBean(job);
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());

		// Change something on one of the parameter sets and verify equality
		statusEntry1.setInfo(new Date().toString());
		assertTrue(!statusEntry1.getInfo().equals(statusEntry2.getInfo()));
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());

		// // Restore equality
		// statusEntry2.setInfo(statusEntry1.getInfo());
		// assertTrue(statusEntry1.equals(statusEntry2));
		// assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());
		//
		// // Verify non-persistent status is not equal to persistent status
		// statusEntry1 = stsController.save(statusEntry1);
		// assertTrue(!statusEntry1.equals(statusEntry2));
		// assertTrue(statusEntry1.hashCode() != statusEntry2.hashCode());
		//
		// // Verify that equality of persisted parameter sets is set only by
		// // persistence id
		// stsController.detach(statusEntry1);
		// statusEntry2 = stsController.find(statusEntry1.getJobId());
		// stsController.detach(statusEntry2);
		// assertTrue(statusEntry1.equals(statusEntry2));
		// assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());
		//
		// statusEntry1.setInfo("nonsense");
		// assertTrue(!statusEntry1.getInfo().equals(statusEntry2.getInfo()));
		// assertTrue(statusEntry1.equals(statusEntry2));
		// assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());
	}

	@Test
	public void testJobType() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		// Create a statusEntry and set a value
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		final String v1 = new Date().toString();
		statusEntry.setJobType(v1);

		// Save the statusEntry
		final long id1 = stsController.save(statusEntry).getJobId();
		assertTrue(initialCount + 1 == stsController.findAll().size());
		statusEntry = null;

		// Retrieve the statusEntry
		statusEntry = stsController.find(id1);

		// Check the value
		final String v2 = statusEntry.getJobType();
		assertTrue(v1.equals(v2));

		// Remove the statusEntry and the number of remaining jobs
		stsController.delete(statusEntry);
		assertTrue(initialCount == stsController.findAll().size());
	}

	@Test
	public void testStatusId() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		// Create a statusEntry and set a value
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		final int v1 = random.nextInt();
		statusEntry.setStatusId(v1);

		// Save the statusEntry
		final long id1 = stsController.save(statusEntry).getJobId();
		assertTrue(initialCount + 1 == stsController.findAll().size());
		statusEntry = null;

		// Get the statusEntry
		statusEntry = stsController.find(id1);

		// Check the value
		final int v2 = statusEntry.getStatusId();
		assertTrue(v1 == v2);

		// Remove the statusEntry and the number of remaining jobs
		stsController.delete(statusEntry);
		assertTrue(initialCount == stsController.findAll().size());
	}

	@Test
	public void testVersion() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		// Create a statusEntry and set a value
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		final int v1 = random.nextInt();
		statusEntry.setVersion(v1);

		// Save the statusEntry
		final long id1 = stsController.save(statusEntry).getJobId();
		assertTrue(initialCount + 1 == stsController.findAll().size());
		statusEntry = null;

		// Get the statusEntry
		statusEntry = stsController.find(id1);

		// Check the value
		final int v2 = statusEntry.getVersion();
		assertTrue(v1 == v2);

		// Remove the statusEntry and the number of remaining jobs
		stsController.delete(statusEntry);
		assertTrue(initialCount == stsController.findAll().size());
	}

	@Test
	public void testInfo() {
		// Count existing jobs
		final int initialCount = stsController.findAll().size();

		// Create a statusEntry and set a value
		OfflineMatchingBean job =
			new OfflineMatchingBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		StatusLogBean statusEntry = new StatusLogBean(job);

		final String v1 = new Date().toString();
		statusEntry.setInfo(v1);

		// Save the statusEntry
		final long id1 = stsController.save(statusEntry).getJobId();
		assertTrue(initialCount + 1 == stsController.findAll().size());
		statusEntry = null;

		// Retrieve the statusEntry
		statusEntry = stsController.find(id1);

		// Check the value
		final String v2 = statusEntry.getInfo();
		assertTrue(v1.equals(v2));

		// Remove the statusEntry and the number of remaining jobs
		stsController.delete(statusEntry);
		assertTrue(initialCount == stsController.findAll().size());
	}

}
