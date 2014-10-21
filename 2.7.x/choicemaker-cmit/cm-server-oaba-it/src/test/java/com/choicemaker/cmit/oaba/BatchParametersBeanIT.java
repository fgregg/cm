package com.choicemaker.cmit.oaba;

import static com.choicemaker.cmit.oaba.util.OabaConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaConstants.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.utils.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolvePom;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.Random;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchJobBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cmit.utils.DeploymentUtils;

@RunWith(Arquillian.class)
public class BatchParametersBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static EnterpriseArchive createEarArchive2() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH,
					PERSISTENCE_CONFIGURATION, DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

	public final int MAX_TEST_ITERATIONS = 10;

	final protected Random random = new Random(new Date().getTime());

	protected float getRandomThreshold() {
		return random.nextFloat();
	}

	@EJB
	protected BatchJobController jobController;

	@EJB
	protected BatchParametersController prmController;

	@Test
	public void testBatchParametersController() {
		assertTrue(jobController != null);
		assertTrue(prmController != null);
	}

	@Test
	public void testConstruction() {
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		assertTrue(job.getId() == params.getId());
	}

	@Test
	public void testPersistFindRemove() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		assertTrue(params.getId() == job.getId());

		// Save the params
		prmController.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		BatchParametersBean batchParameters2 =
			prmController.find(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		prmController.delete(batchParameters2);
		BatchParameters batchParameters3 = prmController.find(params.getId());
		assertTrue(batchParameters3 == null);

		// Check that the number of existing jobs equals the initial count
		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testMerge() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		prmController.save(params);
		assertTrue(params.getId() != 0);
		final long id = params.getId();
		prmController.detach(params);

		assertTrue(params.getHighThreshold() == 0f);
		final float highThreshold = getRandomThreshold();
		params.setHighThreshold(highThreshold);
		prmController.save(params);

		params = null;
		BatchParametersBean batchParameters2 = prmController.find(id);
		assertTrue(id == batchParameters2.getId());
		assertTrue(highThreshold == batchParameters2.getHighThreshold());
		prmController.delete(batchParameters2);

		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testEqualsHashCode() {
		// Create two generic parameter sets and verify equality
		BatchJobBean job1 =
			new BatchJobBean("EXT ID: " + new Date().toString());
		jobController.save(job1);
		BatchParametersBean params1 = new BatchParametersBean(job1);
		BatchParametersBean params2 = new BatchParametersBean(job1);
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());

		// Change something on one of the parameter sets and verify equality
		params1.setLowThreshold(getRandomThreshold());
		assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		assertTrue(params1.equals(params2));
		assertTrue(params1.hashCode() == params2.hashCode());

		// // Restore equality
		// params2.setLowThreshold(params1.getLowThreshold());
		// assertTrue(params1.equals(params2));
		// assertTrue(params1.hashCode() == params2.hashCode());
		//
		// // Verify non-persistent parameters is not equal to persistent
		// // parameters
		// params1 = prmController.save(params1);
		// assertTrue(!params1.equals(params2));
		// assertTrue(params1.hashCode() != params2.hashCode());
		//
		// // Verify that equality of persisted parameter sets is set only by
		// // persistence id
		// prmController.detach(params1);
		// params2 = prmController.find(params1.getId());
		// prmController.detach(params2);
		// assertTrue(params1.equals(params2));
		// assertTrue(params1.hashCode() == params2.hashCode());
		//
		// params1.setLowThreshold(getRandomThreshold());
		// assertTrue(params1.getLowThreshold() != params2.getLowThreshold());
		// assertTrue(params1.equals(params2));
		// assertTrue(params1.hashCode() == params2.hashCode());
	}

	@Test
	public void testStageModel() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params and set a value
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		final String v1 = new Date().toString();
		params.setStageModel(v1);

		// Save the params
		final long id1 = prmController.save(params).getId();
		assertTrue(initialCount + 1 == prmController.findAll().size());
		params = null;

		// Retrieve the params
		params = prmController.find(id1);

		// Check the value
		final String v2 = params.getStageModel();
		assertTrue(v1.equals(v2));

		// Remove the params and check the number of remaining entries
		prmController.delete(params);
		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testMasterModel() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params and set a value
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		final String v1 = new Date().toString();
		params.setMasterModel(v1);

		// Save the params
		final long id1 = prmController.save(params).getId();
		assertTrue(initialCount + 1 == prmController.findAll().size());
		params = null;

		// Retrieve the params
		params = prmController.find(id1);

		// Check the value
		final String v2 = params.getMasterModel();
		assertTrue(v1.equals(v2));

		// Remove the params and check the number of remaining entries
		prmController.delete(params);
		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testMaxSingle() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params and set a value
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		final int v1 = random.nextInt();
		params.setMaxSingle(v1);

		// Save the params
		final long id1 = prmController.save(params).getId();
		assertTrue(initialCount + 1 == prmController.findAll().size());
		params = null;

		// Get the params
		params = prmController.find(id1);

		// Check the value
		final int v2 = params.getMaxSingle();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		prmController.delete(params);
		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testLowThreshold() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params and set a value
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		final float v1 = getRandomThreshold();
		params.setLowThreshold(v1);

		// Save the params
		final long id1 = prmController.save(params).getId();
		assertTrue(initialCount + 1 == prmController.findAll().size());
		params = null;

		// Get the params
		params = prmController.find(id1);

		// Check the value
		final float v2 = params.getLowThreshold();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		prmController.delete(params);
		assertTrue(initialCount == prmController.findAll().size());
	}

	@Test
	public void testHighThreshold() {
		// Count existing jobs
		final int initialCount = prmController.findAll().size();

		// Create a params and set a value
		BatchJobBean job =
			new BatchJobBean("EXT ID: " + new Date().toString());
		job = jobController.save(job);
		BatchParametersBean params = new BatchParametersBean(job);
		final float v1 = getRandomThreshold();
		params.setHighThreshold(v1);

		// Save the params
		final long id1 = prmController.save(params).getId();
		assertTrue(initialCount + 1 == prmController.findAll().size());
		params = null;

		// Get the params
		params = prmController.find(id1);

		// Check the value
		final float v2 = params.getHighThreshold();
		assertTrue(v1 == v2);

		// Remove the params and the number of remaining entries
		prmController.delete(params);
		assertTrue(initialCount == prmController.findAll().size());
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
