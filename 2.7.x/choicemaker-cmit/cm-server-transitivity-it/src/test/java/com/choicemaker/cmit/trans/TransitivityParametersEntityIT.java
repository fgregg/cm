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

import com.choicemaker.cm.args.AnalysisResultFormat;
import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.impl.TransitivityParametersEntity;
import com.choicemaker.cmit.trans.util.TransitivityDeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.FakePersistableRecordSource;
import com.choicemaker.cmit.utils.TestEntityCounts;

@RunWith(Arquillian.class)
public class TransitivityParametersEntityIT {

	private static final Logger logger = Logger
			.getLogger(TransitivityParametersEntityIT.class.getName());

	public static final boolean TESTS_AS_EJB_MODULE = false;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return TransitivityDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public final int MAX_TEST_ITERATIONS = 10;

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

	@EJB(beanName="OabaProcessingControllerBean")
	private ProcessingController oabaProcessingController;

	@EJB(beanName="TransitivityProcessingControllerBean")
	private ProcessingController transProcessingController;

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
			te.checkCounts(logger, em, utx, oabaJobController, null, null,
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
			new TestEntityCounts(logger, oabaJobController, null, null,
					transParamsController, oabaSettingsController,
					serverController, oabaProcessingController,
					opPropController, rsController, ridController);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(transParamsController != null);
	}

	@Test
	public void testPersistedValues() {
		final String METHOD = "testPersistedValues";

		// Create the OABA parameters of a parent job
		final Thresholds thresholds =
			EntityManagerUtils.createRandomThresholds();
		final PersistableRecordSource stage =
			new FakePersistableRecordSource(METHOD);
		final OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		final PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(METHOD, task);
		final String v1 = EntityManagerUtils.createExternalId(METHOD);
		OabaParameters oaba_p =
			new OabaParametersEntity(v1, thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		te.add(oaba_p);

		// Create a set of transitivity parameters
		final AnalysisResultFormat format =
			EntityManagerUtils.createRandomAnalysisFormat();
		final String graphName = EntityManagerUtils.createRandomGraphName();
		TransitivityParameters params =
			new TransitivityParametersEntity(oaba_p, format, graphName);
		te.add(params);

		// Save the parameters
		final long id1 = transParamsController.save(params).getId();

		// Get the parameters
		params = null;
		params = transParamsController.findTransitivityParameters(id1);

		// Check the values
		assertTrue(v1.equals(params.getModelConfigurationName()));
		assertTrue(thresholds.getDifferThreshold() == params.getLowThreshold());
		assertTrue(thresholds.getMatchThreshold() == params.getHighThreshold());
		assertTrue(format.equals(params.getAnalysisResultFormat()));
		assertTrue(graphName.equals(params.getGraphProperty().getName()));

		checkCounts();
	}

	protected TransitivityParametersEntity createTransitivityParameters(
			String tag, TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		// Create the OABA parameters of a parent job
		final Thresholds thresholds =
			EntityManagerUtils.createRandomThresholds();
		final PersistableRecordSource stage =
			new FakePersistableRecordSource(tag);
		final OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		final PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		final String v1 = EntityManagerUtils.createExternalId(tag);
		OabaParameters oaba_p =
			new OabaParametersEntity(v1, thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		te.add(oaba_p);

		// Create a set of transitivity parameters
		final AnalysisResultFormat format =
			EntityManagerUtils.createRandomAnalysisFormat();
		final String graphName = EntityManagerUtils.createRandomGraphName();
		TransitivityParametersEntity retVal =
			new TransitivityParametersEntity(oaba_p, format, graphName);
		te.add(retVal);
		return retVal;
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";

		final TransitivityParameters p1 =
			createTransitivityParameters(METHOD, te);
		assertTrue(te.contains(p1));
		final int h1 = p1.hashCode();

		final TransitivityParameters p2 = new TransitivityParametersEntity(p1);
		te.add(p2);
		assertTrue(p1 != p2);
		assertTrue(!p1.equals(p2));
		assertTrue(p1.hashCode() != p2.hashCode());

		final TransitivityParameters p1P = transParamsController.save(p1);
		assertTrue(p1 == p1P);
		assertTrue(p1P.isPersistent());
		assertTrue(h1 == p1.hashCode());
		assertTrue(te.contains(p1));

		checkCounts();
	}

}
