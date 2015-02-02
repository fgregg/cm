package com.choicemaker.cmit.trans;

import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.TransitivityTestController;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;

@RunWith(Arquillian.class)
public class TransitivityParametersEntityIT {

	private static final Logger logger = Logger
			.getLogger(TransitivityParametersEntityIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = true;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public final int MAX_TEST_ITERATIONS = 10;

	@EJB
	protected TransitivityTestController transTestController;

	@Resource
	private UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private OabaJobController oabaJobController;

	@EJB
	private TransitivityParametersController transParamsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaProcessingController oabaProcessingController;

	@EJB
	private OabaService oabaService;

	@EJB
	private OperationalPropertyController opPropController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private ServerConfigurationController serverController;

	TestEntityCounts te;

	final protected Random random = new Random(new Date().getTime());

	protected float getRandomThreshold() {
		return random.nextFloat();
	}

	public void checkCounts() {
		if (te != null) {
			te.checkCounts(logger, em, utx, oabaJobController,
					transParamsController, oabaSettingsController,
					serverController, oabaProcessingController,
					opPropController, rsController, ridController);
		} else {
			throw new Error("Counts not initialized");
		}
	}

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaJobController,
					transParamsController, oabaSettingsController,
					serverController, oabaProcessingController,
					opPropController, rsController, ridController);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(transParamsController != null);
		assertTrue(transTestController != null);
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";

		// Create parameters
		TransitivityParameters p =
			transTestController.createTransitivityParameters(METHOD, te);
		assertTrue(TransitivityParametersEntity.isPersistent(p));
		assertTrue(p.getId() != TransitivityParametersEntity.NONPERSISTENT_ID);
		final long id0 = p.getId();

		// Save the parameters
		transParamsController.save(p);
		assertTrue(p.getId() == id0);

		// Find the parameters
		TransitivityParameters p2 =
			transParamsController.findTransitivityParameters(p.getId());
		assertTrue(p.getId() == p2.getId());
		assertTrue(p.equals(p2));

		// Delete the parameters
		transParamsController.delete(p2);
		TransitivityParameters p3 =
			transParamsController.findTransitivityParameters(p.getId());
		assertTrue(p3 == null);

		checkCounts();
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";

		final TransitivityParameters p0 =
			transTestController.createTransitivityParameters(METHOD, te);
		assertTrue(TransitivityParametersEntity.isPersistent(p0));
		assertTrue(te.contains(p0));
		final int h0 = p0.hashCode();

		final TransitivityParameters p1 = new TransitivityParametersEntity(p0);
		final int h1 = p1.hashCode();
		assertTrue(h0 != h1);
		assertTrue(!p1.equals(p0));
		assertTrue(!TransitivityParametersEntity.isPersistent(p1));
		te.add(p1);
		assertTrue(te.contains(p1));

		final TransitivityParameters p2 = new TransitivityParametersEntity(p1);
		assertTrue(p1 != p2);
		assertTrue(p1.equals(p2));
		assertTrue(p1.hashCode() == p2.hashCode());
		te.add(p2);
		assertTrue(te.contains(p2));

		final TransitivityParameters p1P = transParamsController.save(p1);
		assertTrue(p1 != p1P);
		assertTrue(!p1P.equals(p0));
		assertTrue(TransitivityParametersEntity.isPersistent(p1P));

		// BUG p1 is lost in te because hashCode changes
		// See http://links.rph.cx/1CSqJ5p for a discussion and a proposed fix
		assertTrue(!te.contains(p1));
		// ENDBUG

		assertTrue(!te.contains(p1P));
		te.add(p1P);

		final TransitivityParameters p2P = transParamsController.save(p2);
		assertTrue(p2 != p2P);
		assertTrue(!p2P.equals(p0));
		assertTrue(p1P.getId() != p2P.getId());
		assertTrue(!p1P.equals(p2P));
		assertTrue(p1P.hashCode() != p2P.hashCode());

		// BUG p2 is lost in te because hashCode changes
		assertTrue(!te.contains(p2));
		// ENDBUG

		assertTrue(!te.contains(p2P));
		te.add(p2P);

		checkCounts();
	}

	@Test
	public void testStageModel() {
		// FIXME STUBBED
		// final String METHOD = "testStageModel";
		//
		// // Create a params and set a value
		// OabaParametersEntity template =
		// transTestController.createTransitivityParameters(METHOD, te);
		// final String v1 =
		// transTestController.createRandomModelConfigurationName(METHOD);
		// OabaParameters params =
		// new OabaParametersEntity(v1, template.getLowThreshold(),
		// template.getHighThreshold(), template.getStageRsId(),
		// template.getStageRsType(), template.getMasterRsId(),
		// template.getMasterRsType(), template.getOabaLinkageType());
		// te.add(params);

		// FIXME stubbed
		// // Save the params
		// final long id1 = transParamsController.save(params).getId();
		//
		// // Get the params
		// params = null;
		// params = transParamsController.findOabaParameters(id1);

		// FIXME STUBBED
		// // Check the value
		// assertTrue(v1.equals(params.getStageModel()));
		// assertTrue(v1.equals(params.getMasterModel()));
		// assertTrue(v1.equals(params.getModelConfigurationName()));
		//
		// checkCounts();
	}

	@Test
	public void testThresholds() {
		// FIXME STUBBED
		// final String METHOD = "testThresholds";
		//
		// // Create parameters with known values
		// OabaParametersEntity template =
		// transTestController.createTransitivityParameters(METHOD, te);
		// final Thresholds t = transTestController.createRandomThresholds();
		// OabaParameters params =
		// new OabaParametersEntity(template.getModelConfigurationName(),
		// t.getDifferThreshold(), t.getMatchThreshold(),
		// template.getStageRsId(), template.getStageRsType(),
		// template.getMasterRsId(), template.getMasterRsType(),
		// template.getOabaLinkageType());
		// te.add(params);

		// FIXME stubbed
		// // Save the params
		// final long id1 = transParamsController.save(params).getId();
		//
		// // Get the params
		// params = null;
		// params = transParamsController.findOabaParameters(id1);

		// FIXME STUBBED
		// // Check the value
		// assertTrue(t.getDifferThreshold() == params.getLowThreshold());
		// assertTrue(t.getMatchThreshold() == params.getHighThreshold());
		//
		// checkCounts();
	}

}
