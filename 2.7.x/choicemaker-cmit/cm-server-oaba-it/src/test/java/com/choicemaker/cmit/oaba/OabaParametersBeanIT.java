package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class OabaParametersBeanIT {

	private static final Logger logger = Logger
			.getLogger(OabaParametersBeanIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
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
	protected OabaParametersController2 prmController;

	private int initialOabaParamsCount;
	private int initialOabaJobCount;
//	private int initialTransitivityJobCount;

	@Before
	public void setUp() {
		initialOabaParamsCount = prmController.findAllBatchParameters().size();
		initialOabaJobCount = prmController.findAllBatchJobs().size();
//		initialTransitivityJobCount = prmController.findAllTransitivityJobs().size();
	}

	@After
	public void tearDown() {
		int finalBatchParamsCount = prmController.findAllBatchParameters().size();
		assertTrue(initialOabaParamsCount == finalBatchParamsCount);

		int finalBatchJobCount = prmController.findAllBatchJobs().size();
		assertTrue(initialOabaJobCount == finalBatchJobCount);
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
		final String METHOD = "testPersistFindRemove";
		TestEntities te = new TestEntities();

		// Create a params
		OabaParametersEntity params =
			prmController.createBatchParameters(METHOD, te);

		// Save the params
		prmController.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		OabaParameters batchParameters2 =
			prmController.find(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		prmController.delete(batchParameters2);
		OabaParameters batchParameters3 = prmController.find(params.getId());
		assertTrue(batchParameters3 == null);
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		TestEntities te = new TestEntities();

		// Create two generic parameter sets, only one of which is persistent,
		// and verify inequality
		OabaParametersEntity params1 = prmController.createBatchParameters(METHOD,te);
		OabaParametersEntity params2 = new OabaParametersEntity(params1);
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
		final String METHOD = "testStageModel";
		TestEntities te = new TestEntities();

		// Create a params and set a value
		OabaParametersEntity template = prmController.createBatchParameters(METHOD,te);
		final String v1 = prmController.createRandomModelConfigurationName(METHOD);
		OabaParameters params = new OabaParametersEntity(
				v1,
				template.getLowThreshold(),
				template.getHighThreshold(),
				template.getStageRs(),
				template.getMasterRs()
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
	public void testThresholds() {
		final String METHOD = "testThresholds";
		TestEntities te = new TestEntities();

		// Create parameters with known values
		OabaParametersEntity template = prmController.createBatchParameters(METHOD,te);
		final Thresholds t = prmController.createRandomThresholds();
		OabaParameters params = new OabaParametersEntity(
				template.getModelConfigurationName(),
				t.getDifferThreshold(),
				t.getMatchThreshold(),
				template.getStageRs(),
				template.getMasterRs()
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
