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
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BatchParametersBean;
import com.choicemaker.cmit.utils.DeploymentUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class BatchParametersBeanIT {

	private static final Logger logger = Logger
			.getLogger(BatchParametersBeanIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);
		File[] libs = resolveDependencies(pom);
		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal =
			DeploymentUtils.createEAR(tests, libs, TESTS_AS_EJB_MODULE);
		return retVal;
	}

	public final int MAX_TEST_ITERATIONS = 10;

	final protected Random random = new Random(new Date().getTime());

	protected float getRandomThreshold() {
		return random.nextFloat();
	}

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	protected BatchParametersController prmController;

	private int initialBatchParamsCount;
	private int initialBatchJobCount;
//	private int initialTransitivityJobCount;

	@Before
	public void setUp() {
		initialBatchParamsCount = prmController.findAllBatchParameters().size();
		initialBatchJobCount = prmController.findAllBatchJobs().size();
//		initialTransitivityJobCount = prmController.findAllTransitivityJobs().size();
	}

	@After
	public void tearDown() {
		int finalBatchParamsCount = prmController.findAllBatchParameters().size();
		assertTrue(initialBatchParamsCount == finalBatchParamsCount);

		int finalBatchJobCount = prmController.findAllBatchJobs().size();
		assertTrue(initialBatchJobCount == finalBatchJobCount);
//
//		int finalTransJobCount = prmController.findAllTransitivityJobs().size();
//		assertTrue(initialTransitivityJobCount == finalTransJobCount);
	}

	@Test
	public void testBatchParametersController() {
		assertTrue(prmController != null);
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "XXX";
		TestEntities te = new TestEntities();

		// Create a params
		BatchParametersBean params =
			prmController.createBatchParameters(METHOD, te);

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
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "XXX";
		TestEntities te = new TestEntities();

		// Create two generic parameter sets, only one of which is persistent,
		// and verify inequality
		BatchParametersBean params1 = prmController.createBatchParameters(METHOD,te);
		BatchParametersBean params2 = new BatchParametersBean(params1);
		te.add(params2);
		assertTrue(!params1.equals(params2));
		assertTrue(params1.hashCode() != params2.hashCode());

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testStageModel() {
		final String METHOD = "XXX";
		TestEntities te = new TestEntities();

		// Create a params and set a value
		BatchParametersBean template = prmController.createBatchParameters(METHOD,te);
		final String v1 = prmController.createRandomModelConfigurationName(METHOD);
		BatchParametersBean params = new BatchParametersBean(
				v1,
				template.getMaxSingle(),
				template.getLowThreshold(),
				template.getHighThreshold(),
				template.getStageRs(),
				template.getMasterRs(),
				template.getTransitivity()
				);
		te.add(params);

		// Save the params
		final long id1 = prmController.save(params).getId();

		// Get the params
		params = null;
		params = prmController.find(id1);

		// Check the value
		assertTrue(v1.equals(params.getStageModel()));
		assertTrue(v1.equals(params.getMasterModel()));
		assertTrue(v1.equals(params.getModelConfigurationName()));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testMaxSingle() {
		final String METHOD = "XXX";
		TestEntities te = new TestEntities();

		// Create parameters with a known value
		BatchParametersBean template =
			prmController.createBatchParameters(METHOD, te);
		final int v1 = random.nextInt();
		BatchParametersBean params =
			new BatchParametersBean(template.getModelConfigurationName(), v1,
					template.getLowThreshold(), template.getHighThreshold(),
					template.getStageRs(), template.getMasterRs(),
					template.getTransitivity());
		te.add(params);

		// Save the params
		final long id1 = prmController.save(params).getId();

		// Get the params
		params = null;
		params = prmController.find(id1);

		// Check the value
		final int v2 = params.getMaxSingle();
		assertTrue(v1 == v2);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

	@Test
	public void testThresholds() {
		final String METHOD = "XXX";
		TestEntities te = new TestEntities();

		// Create parameters with known values
		BatchParametersBean template = prmController.createBatchParameters(METHOD,te);
		final Thresholds t = prmController.createRandomThresholds();
		BatchParametersBean params = new BatchParametersBean(
				template.getModelConfigurationName(),
				template.getMaxSingle(),
				t.getDifferThreshold(),
				t.getMatchThreshold(),
				template.getStageRs(),
				template.getMasterRs(),
				template.getTransitivity()
				);
		te.add(params);

		// Save the params
		final long id1 = prmController.save(params).getId();

		// Get the params
		params = null;
		params = prmController.find(id1);

		// Check the value
		assertTrue(t.getDifferThreshold() == params.getLowThreshold());
		assertTrue(t.getMatchThreshold() == params.getHighThreshold());

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
	}

}
