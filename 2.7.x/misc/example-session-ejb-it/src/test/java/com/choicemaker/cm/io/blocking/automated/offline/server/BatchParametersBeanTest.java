package com.choicemaker.cm.io.blocking.automated.offline.server;

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

@RunWith(Arquillian.class)
public class BatchParametersBeanTest {

	private static final String PROJECT_POM = DeploymentUtils.PROJECT_POM;

	private static final String DEPENDENCIES_POM =
		DeploymentUtils.DEPENDENCIES_POM;

	public final int MAX_TEST_ITERATIONS = 10;

	final protected Random random = new Random(new Date().getTime());

	protected float getRandomThreshold() {
		return random.nextFloat();
	}

	@EJB
	protected BatchParametersController controller;

	@Test
	public void testBatchParametersController() {
		assertTrue(controller != null);
	}

	@Test
	public void testConstruction() {
		BatchParametersBean params = new BatchParametersBean();
		assertTrue(0 == params.getId());
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params
		BatchParametersBean params = new BatchParametersBean();
		assertTrue(params.getId() == 0);

		// Save the params
		controller.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		BatchParametersBean batchParameters2 = controller.find(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		controller.delete(batchParameters2);
		BatchParametersBean batchParameters3 = controller.find(params.getId());
		assertTrue(batchParameters3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		BatchParametersBean params = new BatchParametersBean();
		controller.save(params);
		assertTrue(params.getId() != 0);
		final long id = params.getId();
		controller.detach(params);

		assertTrue(params.getHighThreshold() == 0f);
		final float highThreshold = getRandomThreshold();
		params.setHighThreshold(highThreshold);
		controller.save(params);

		params = null;
		BatchParametersBean batchParameters2 = controller.find(id);
		assertTrue(id == batchParameters2.getId());
		assertTrue(highThreshold == batchParameters2.getHighThreshold());
		controller.delete(batchParameters2);

		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic parameter sets and verify equality
		BatchParametersBean params1 = new BatchParametersBean();
		BatchParametersBean params2 = new BatchParametersBean();
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());

		// Change something on one of the parameter sets and verify inequality
		params1.setLowThreshold(getRandomThreshold());
		assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		assertTrue(!params1.equals(params2));
		assertTrue(params1.hashCode() != params2.hashCode());

		// Restore equality
		params2.setLowThreshold(params1.getLowThreshold());
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());

		// Verify non-persistent parameters is not equal to persistent
		// parameters
		params1 = controller.save(params1);
		assertTrue(!params1.equals(params2));
		assertTrue(params1.hashCode() != params2.hashCode());

		// Verify that equality of persisted parameter sets is set only by
		// persistence id
		controller.detach(params1);
		params2 = controller.find(params1.getId());
		controller.detach(params2);
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());

		params1.setLowThreshold(getRandomThreshold());
		assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());
	}

	// @Test
	// public void testGetId() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetStageModel() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMasterModel() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMaxSingle() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetLowThreshold() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetHighThreshold() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetStageRs() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testGetMasterRs() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testEqualsObject() {
	// fail("Not yet implemented");
	// }

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(BatchParametersBeanTest.class);
		testClasses.add(BatchParametersController.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, testClasses);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

}
