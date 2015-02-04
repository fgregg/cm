package com.choicemaker.cmit.oaba;

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

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.core.base.Thresholds;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.FakePersistableRecordSource;
import com.choicemaker.cmit.utils.TestEntityCounts;

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

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	private OabaJobController oabaController;

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaProcessingController processingController;

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
			te.checkCounts(logger, em, utx, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
		} else {
			throw new Error("Counts not initialized");
		}
	}

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
	}

	@Test
	public void testPrerequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(paramsController != null);
	}

	@Test
	public void testPersistFindRemove() {
		final String METHOD = "testPersistFindRemove";

		// Create a params
		OabaParametersEntity params = createBatchParameters(METHOD, te);

		// Save the params
		paramsController.save(params);
		assertTrue(params.getId() != 0);

		// Find the params
		OabaParameters batchParameters2 =
			paramsController.findOabaParameters(params.getId());
		assertTrue(params.getId() == batchParameters2.getId());
		assertTrue(params.equals(batchParameters2));

		// Delete the params
		paramsController.delete(batchParameters2);
		OabaParameters batchParameters3 =
			paramsController.findOabaParameters(params.getId());
		assertTrue(batchParameters3 == null);

		checkCounts();
	}

	protected OabaParametersEntity createBatchParameters(String tag,
			TestEntityCounts te) {
		if (te == null) {
			throw new IllegalArgumentException("null test entities");
		}
		Thresholds thresholds = EntityManagerUtils.createRandomThresholds();
		PersistableRecordSource stage = new FakePersistableRecordSource(tag);
		OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(tag, task);
		String modelConfig =
			EntityManagerUtils.createRandomModelConfigurationName(tag);
		OabaParametersEntity retVal =
			new OabaParametersEntity(modelConfig,
					thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		paramsController.save(retVal);
		te.add(retVal);
		return retVal;
	}

	@Test
	public void testEqualsHashCode() {
		final String METHOD = "testEqualsHashCode";

		OabaParametersEntity p1 = createBatchParameters(METHOD, te);
		assertTrue(te.contains(p1));
		final int h1 = p1.hashCode();

		OabaParametersEntity p2 = new OabaParametersEntity(p1);
		te.add(p2);
		assertTrue(!p1.equals(p2));
		assertTrue(h1 != p2.hashCode());

		final OabaParameters p1P = paramsController.save(p1);
		assertTrue(p1 == p1P);
		assertTrue(p1.isPersistent());
		assertTrue(h1 == p1.hashCode());
		assertTrue(te.contains(p1));

		checkCounts();
	}

	@Test
	public void testPersistedValues() {
		final String METHOD = "testPersistedValues";

		// Create a set of parameters
		final Thresholds thresholds =
			EntityManagerUtils.createRandomThresholds();
		final PersistableRecordSource stage =
			new FakePersistableRecordSource(METHOD);
		final OabaLinkageType task = EntityManagerUtils.createRandomOabaTask();
		final PersistableRecordSource master =
			EntityManagerUtils.createFakeMasterRecordSource(METHOD, task);
		final String v1 = EntityManagerUtils.createExternalId(METHOD);
		OabaParameters params =
			new OabaParametersEntity(v1, thresholds.getDifferThreshold(),
					thresholds.getMatchThreshold(), stage, master, task);
		te.add(params);

		// Save the parameters
		final long id1 = paramsController.save(params).getId();

		// Get the parameters
		params = null;
		params = paramsController.findOabaParameters(id1);

		// Check the values
		assertTrue(v1.equals(params.getStageModel()));
		assertTrue(v1.equals(params.getMasterModel()));
		assertTrue(v1.equals(params.getModelConfigurationName()));
		assertTrue(thresholds.getDifferThreshold() == params.getLowThreshold());
		assertTrue(thresholds.getMatchThreshold() == params.getHighThreshold());

		checkCounts();
	}

}
