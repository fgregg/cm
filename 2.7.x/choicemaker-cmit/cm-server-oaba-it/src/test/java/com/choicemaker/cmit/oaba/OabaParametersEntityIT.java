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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.OabaTestController;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class OabaParametersEntityIT {

	private static final Logger logger = Logger
			.getLogger(OabaParametersEntityIT.class.getName());

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
	protected OabaParametersControllerBean paramsController;

	@EJB
	protected OabaTestController oabaTestControllerBean;

	private int initialOabaParamsCount;
	private int initialOabaJobCount;

	@Before
	public void setUp() {
		initialOabaParamsCount = oabaTestControllerBean.findAllOabaParameters().size();
		initialOabaJobCount = oabaTestControllerBean.findAllOabaJobs().size();
	}

	@After
	public void tearDown() {
		int finalBatchParamsCount =
				oabaTestControllerBean.findAllOabaParameters().size();
		assertTrue(initialOabaParamsCount == finalBatchParamsCount);

		int finalBatchJobCount = oabaTestControllerBean.findAllOabaJobs().size();
		assertTrue(initialOabaJobCount == finalBatchJobCount);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(paramsController != null);
		assertTrue(oabaTestControllerBean != null);
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";
		TestEntities te = new TestEntities();

		// Create a params
		OabaParametersEntity params =
			oabaTestControllerBean.createBatchParameters(METHOD, te);

		// Save the params
		paramsController.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		OabaParameters batchParameters2 = paramsController.find(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		paramsController.delete(batchParameters2);
		OabaParameters batchParameters3 = paramsController.find(params.getId());
		assertTrue(batchParameters3 == null);
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";
		TestEntities te = new TestEntities();

		// Create two generic parameter sets, only one of which is persistent,
		// and verify inequality
		OabaParametersEntity params1 =
			oabaTestControllerBean.createBatchParameters(METHOD, te);
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
		OabaParametersEntity template =
			oabaTestControllerBean.createBatchParameters(METHOD, te);
		final String v1 =
			oabaTestControllerBean.createRandomModelConfigurationName(METHOD);
		OabaParameters params =
			new OabaParametersEntity(v1, template.getLowThreshold(),
					template.getHighThreshold(), template.getStageRsId(),
					template.getStageRsType(), template.getMasterRsId(),
					template.getMasterRsType(), template.getOabaLinkageType());
		te.add(params);

		// Save the params
		final long id1 = paramsController.save(params).getId();

		// Get the params
		params = null;
		params = paramsController.find(id1);

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
		OabaParametersEntity template =
			oabaTestControllerBean.createBatchParameters(METHOD, te);
		final Thresholds t = oabaTestControllerBean.createRandomThresholds();
		OabaParameters params =
			new OabaParametersEntity(template.getModelConfigurationName(),
					t.getDifferThreshold(), t.getMatchThreshold(),
					template.getStageRsId(), template.getStageRsType(),
					template.getMasterRsId(), template.getMasterRsType(),
					template.getOabaLinkageType());
		te.add(params);

		// Save the params
		final long id1 = paramsController.save(params).getId();

		// Get the params
		params = null;
		params = paramsController.find(id1);

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
