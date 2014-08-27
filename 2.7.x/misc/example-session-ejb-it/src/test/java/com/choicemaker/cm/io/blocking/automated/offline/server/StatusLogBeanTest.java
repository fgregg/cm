package com.choicemaker.cm.io.blocking.automated.offline.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class StatusLogBeanTest {

	private static final String PROJECT_POM = DeploymentUtils.PROJECT_POM;

	private static final String DEPENDENCIES_POM =
		DeploymentUtils.DEPENDENCIES_POM;

	public final int MAX_TEST_ITERATIONS = 10;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(StatusLogBeanTest.class);
		testClasses.add(StatusLogController.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, testClasses);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

	@EJB
	protected StatusLogController controller;

	@Test
	public void testBatchParametersController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		StatusLogBean statusEntry = new StatusLogBean();
		assertTrue(0 == statusEntry.getJobId());
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a statusEntry
		StatusLogBean statusEntry = new StatusLogBean();
		assertTrue(statusEntry.getJobId() == 0);

		// Save the statusEntry
		controller.save(statusEntry);
		assertTrue(statusEntry.getJobId() != 0);

		// Find the statusEntry
		StatusLogBean statusEntry2 = controller.find(statusEntry.getJobId());
		assertTrue(statusEntry.getJobId() == statusEntry2.getJobId());
		assertTrue(statusEntry.equals(statusEntry2));

		// Delete the statusEntry
		controller.delete(statusEntry2);
		StatusLogBean statusEntry3 = controller.find(statusEntry.getJobId());
		assertTrue(statusEntry3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		StatusLogBean statusEntry = new StatusLogBean();
		controller.save(statusEntry);
		assertTrue(statusEntry.getJobId() != 0);
		final long id = statusEntry.getJobId();
		controller.detach(statusEntry);

		assertTrue(statusEntry.getInfo() == null);
		final String randomInfo = new Date().toString();
		statusEntry.setInfo(randomInfo);
		controller.save(statusEntry);

		statusEntry = null;
		StatusLogBean statusEntry2 = controller.find(id);
		assertTrue(id == statusEntry2.getJobId());
		assertTrue(randomInfo == statusEntry2.getInfo());
		controller.delete(statusEntry2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic parameter sets and verify equality
		StatusLogBean statusEntry1 = new StatusLogBean();
		StatusLogBean statusEntry2 = new StatusLogBean();
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());

		// Change something on one of the parameter sets and verify inequality
		statusEntry1.setInfo(new Date().toString());
		assertTrue(!statusEntry1.getInfo().equals(statusEntry2.getInfo()));
		assertTrue(!statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() != statusEntry2.hashCode());

		// Restore equality
		statusEntry2.setInfo(statusEntry1.getInfo());
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());

		// Verify non-persistent status is not equal to persistent status
		statusEntry1 = controller.save(statusEntry1);
		assertTrue(!statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() != statusEntry2.hashCode());

		// Verify that equality of persisted parameter sets is set only by
		// persistence id
		controller.detach(statusEntry1);
		statusEntry2 = controller.find(statusEntry1.getJobId());
		controller.detach(statusEntry2);
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());

		statusEntry1.setInfo("nonsense");
		assertTrue(!statusEntry1.getInfo().equals(statusEntry2.getInfo()));
		assertTrue(statusEntry1.equals(statusEntry2));
		assertTrue(statusEntry1.hashCode() == statusEntry2.hashCode());
	}

}
