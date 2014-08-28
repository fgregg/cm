package com.choicemaker.cmit.io.blocking.automated.offline.server;

import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.DEPENDENCIES_POM;
import static com.choicemaker.cmit.io.blocking.automated.offline.server.BatchDeploymentUtils.EJB_MAVEN_COORDINATES;
import static com.choicemaker.cmit.utils.DeploymentUtils.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.PROJECT_POM;
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

import com.choicemaker.cm.io.blocking.automated.offline.server.BatchParametersBean;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class BatchParametersBeanTest {

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		List<Class<?>> testClasses = new ArrayList<>();
		testClasses.add(BatchParametersBeanTest.class);
		testClasses.add(BatchParametersController.class);
		JavaArchive ejb1 =
			DeploymentUtils.createEjbJar(PROJECT_POM, EJB_MAVEN_COORDINATES,
					testClasses, PERSISTENCE_CONFIGURATION);

		File[] deps = DeploymentUtils.createTestDependencies(DEPENDENCIES_POM);

		EnterpriseArchive retVal = DeploymentUtils.createEarArchive(ejb1, deps);
		return retVal;
	}

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

	@Test
	public void testStageModel() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params and set a value
		BatchParametersBean params = new BatchParametersBean();
		final String v1 = new Date().toString();
		params.setStageModel(v1);

		// Save the params
		final long id1 = controller.save(params).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		params = null;

		// Retrieve the params
		params = controller.find(id1);

		// Check the value
		final String v2 = params.getStageModel();
		assertTrue(v1.equals(v2));

		// Remove the params and check the number of remaining entries
		controller.delete(params);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMasterModel() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params and set a value
		BatchParametersBean params = new BatchParametersBean();
		final String v1 = new Date().toString();
		params.setMasterModel(v1);

		// Save the params
		final long id1 = controller.save(params).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		params = null;

		// Retrieve the params
		params = controller.find(id1);

		// Check the value
		final String v2 = params.getMasterModel();
		assertTrue(v1.equals(v2));

		// Remove the params and check the number of remaining entries
		controller.delete(params);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testMaxSingle() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params and set a value
		BatchParametersBean params = new BatchParametersBean();
		final int v1 = random.nextInt();
		params.setMaxSingle(v1);

		// Save the params
		final long id1 = controller.save(params).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		params = null;

		// Get the params
		params = controller.find(id1);

		// Check the value
		final int v2 = params.getMaxSingle();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		controller.delete(params);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testLowThreshold() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params and set a value
		BatchParametersBean params = new BatchParametersBean();
		final float v1 = getRandomThreshold();
		params.setLowThreshold(v1);

		// Save the params
		final long id1 = controller.save(params).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		params = null;

		// Get the params
		params = controller.find(id1);

		// Check the value
		final float v2 = params.getLowThreshold();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		controller.delete(params);
		assertTrue(initialCount == controller.findAll().size());
	}

	@Test
	public void testHighThreshold() {
		// Count existing jobs
		final int initialCount = controller.findAll().size();

		// Create a params and set a value
		BatchParametersBean params = new BatchParametersBean();
		final float v1 = getRandomThreshold();
		params.setHighThreshold(v1);

		// Save the params
		final long id1 = controller.save(params).getId();
		assertTrue(initialCount + 1 == controller.findAll().size());
		params = null;

		// Get the params
		params = controller.find(id1);

		// Check the value
		final float v2 = params.getHighThreshold();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		controller.delete(params);
		assertTrue(initialCount == controller.findAll().size());
	}

	// @Test
	// public void testStageRs() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testMasterRs() {
	// fail("Not yet implemented");
	// }

}
