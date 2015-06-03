package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.args.OabaSettings.DEFAULT_INTERVAL;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_BLOCKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_CHUNKSIZE;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_MATCHES;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MAX_OVERSIZED;
import static com.choicemaker.cm.args.OabaSettings.DEFAULT_MIN_FIELDS;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.j2ee.MutableProbabilityModelStub;
import com.choicemaker.cmit.utils.j2ee.TestEntityCounts;

@RunWith(Arquillian.class)
public class SettingsControllerBeanIT {

	public static final int MAX_MAX_SINGLE = 1000;

	public static final boolean TESTS_AS_EJB_MODULE = false;

	protected static Random random = new Random(new Date().getTime());

	protected AbaSettings randomAbaSettings() {
		AbaSettings retVal =
			new AbaSettingsEntity(
					random.nextInt(AbaSettings.DEFAULT_LIMIT_PER_BLOCKING_SET),
					random.nextInt(AbaSettings.DEFAULT_LIMIT_SINGLE_BLOCKING_SET),
					random.nextInt(AbaSettings.DEFAULT_SINGLE_TABLE_GRACE_LIMIT));
		return retVal;
	}

	protected OabaSettings randomOabaSettings() {
		OabaSettings retVal =
			new OabaSettingsEntity(randomAbaSettings(),
					random.nextInt(MAX_MAX_SINGLE),
					random.nextInt(DEFAULT_MAX_BLOCKSIZE),
					random.nextInt(DEFAULT_MAX_CHUNKSIZE),
					random.nextInt(DEFAULT_MAX_MATCHES),
					random.nextInt(DEFAULT_MAX_OVERSIZED),
					random.nextInt(DEFAULT_MIN_FIELDS),
					random.nextInt(DEFAULT_INTERVAL));
		return retVal;
	}

	protected String randomString() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final String LOG_SOURCE = SettingsControllerBeanIT.class
			.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(SettingsControllerBeanIT.class.getName());

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB(beanName = "OabaJobControllerBean")
	private OabaJobController oabaController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ProcessingController processingController;

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

	@Before
	public void setUp() throws Exception {
		te =
			new TestEntityCounts(logger, oabaController, paramsController,
					oabaSettingsController, serverController,
					processingController, opPropController, rsController,
					ridController);
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

	@Test
	@InSequence(1)
	public void testEntityManager() {
		assertTrue(em != null);
	}

	@Test
	@InSequence(1)
	public void testUserTransaction() {
		assertTrue(utx != null);
	}

	@Test
	@InSequence(50)
	public void testPersistFindRemoveAba() {
		final String METHOD = "testPersistFindRemove";
		logger.entering(LOG_SOURCE, METHOD);

		// Create a configuration
		final AbaSettings aba0 = randomAbaSettings();
		assertTrue(aba0.getId() == AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!AbaSettingsEntity.isPersistent(aba0));

		// Save the configuration
		long id = AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID;
		AbaSettings aba1 = null;
		aba1 = oabaSettingsController.save(aba0);
		te.add(aba1);
		assertTrue(aba1 != null);
		id = aba1.getId();
		assertTrue(AbaSettingsEntity.isPersistent(aba1));
		assertTrue(id != AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		final long abaId1 = id;

		// Find the configuration
		AbaSettings aba2 = null;
		aba2 = oabaSettingsController.findAbaSettings(abaId1);
		assertTrue(aba2 != null);
		assertTrue(aba2.getId() == abaId1);

		checkCounts();
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@Test
	@InSequence(50)
	public void testPersistFindRemoveOaba() {
		final String METHOD = "testPersistFindRemove";
		logger.entering(LOG_SOURCE, METHOD);

		// Create a configuration
		final OabaSettings oaba0 = randomOabaSettings();
		assertTrue(oaba0.getId() == OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!OabaSettingsEntity.isPersistent(oaba0));

		// Save the configuration
		long id = OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID;
		OabaSettings oaba1 = null;
		oaba1 = oabaSettingsController.save(oaba0);
		te.add(oaba1);
		assertTrue(oaba1 != null);
		id = oaba1.getId();
		assertTrue(OabaSettingsEntity.isPersistent(oaba1));
		assertTrue(id != OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		final long oabaId1 = id;

		// Find the configuration
		OabaSettings oaba2 = null;
		oaba2 = oabaSettingsController.findOabaSettings(oabaId1);
		assertTrue(oaba2 != null);
		assertTrue(oaba2.getId() == oabaId1);

		checkCounts();
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@Test
	public void testSetGetDefaultAbaConfiguration() {
		final String METHOD = "testSetGetDefaultConfigurationString";
		logger.entering(LOG_SOURCE, METHOD);

		// Verify that no default is returned for a non-existent modelId
		// with non-existent database and blocking configurations
		MutableProbabilityModelStub mpm = new MutableProbabilityModelStub();
		final String m0 = mpm.getModelName();
		final String d0 = randomString();
		final String b0 = randomString();
		final AbaSettings aba0 =
			oabaSettingsController.findDefaultAbaSettings(m0, d0, b0);
		assertTrue(aba0 == null);

		// Create random ABA settings, save them and set them as a default
		final AbaSettings aba1 = randomAbaSettings();
		te.add(aba1);
		final AbaSettings aba2 = oabaSettingsController.save(aba1);
		te.add(aba2);
		final DefaultSettings dsb2 =
			oabaSettingsController
					.setDefaultAbaConfiguration(mpm, d0, b0, aba2);
		te.add(dsb2);

		// Retrieve the default and check the settings
		final AbaSettings aba3 =
			oabaSettingsController.findDefaultAbaSettings(m0, d0, b0);
		te.add(aba3);
		assertTrue(aba3 != null);
		assertTrue(aba3.getLimitPerBlockingSet() == aba1
				.getLimitPerBlockingSet());
		assertTrue(aba3.getLimitSingleBlockingSet() == aba1
				.getLimitSingleBlockingSet());
		assertTrue(aba3.getSingleTableBlockingSetGraceLimit() == aba1
				.getSingleTableBlockingSetGraceLimit());

		// Clean up the database
		checkCounts();
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@Test
	public void testSetGetDefaultOabaConfiguration() {
		final String METHOD = "testSetGetDefaultConfigurationString";
		logger.entering(LOG_SOURCE, METHOD);

		// Verify that no default is returned for a non-existent modelId
		// with non-existent datoabase and blocking configurations
		MutableProbabilityModelStub mpm = new MutableProbabilityModelStub();
		final String m0 = mpm.getModelName();
		final String d0 = randomString();
		final String b0 = randomString();
		final OabaSettings oaba0 =
			oabaSettingsController.findDefaultOabaSettings(m0, d0, b0);
		assertTrue(oaba0 == null);

		// Create random OABA settings, save them and set them as a default
		final OabaSettings oaba1 = randomOabaSettings();
		te.add(oaba1);
		final OabaSettings oaba2 = oabaSettingsController.save(oaba1);
		te.add(oaba2);
		final DefaultSettings dsb2 =
			oabaSettingsController.setDefaultOabaConfiguration(mpm, d0, b0,
					oaba2);
		te.add(dsb2);

		// Retrieve the default and check the settings
		final OabaSettings oaba3 =
			oabaSettingsController.findDefaultOabaSettings(m0, d0, b0);
		te.add(oaba3);
		assertTrue(oaba3 != null);
		assertTrue(oaba3.getLimitPerBlockingSet() == oaba1
				.getLimitPerBlockingSet());
		assertTrue(oaba3.getLimitSingleBlockingSet() == oaba1
				.getLimitSingleBlockingSet());
		assertTrue(oaba3.getSingleTableBlockingSetGraceLimit() == oaba1
				.getSingleTableBlockingSetGraceLimit());
		assertTrue(oaba3.getMaxBlockSize() == oaba1.getMaxBlockSize());
		assertTrue(oaba3.getMaxChunkSize() == oaba1.getMaxChunkSize());
		assertTrue(oaba3.getMaxOversized() == oaba1.getMaxOversized());
		assertTrue(oaba3.getMinFields() == oaba1.getMinFields());
		assertTrue(oaba3.getInterval() == oaba1.getInterval());

		// Clean up the database
		checkCounts();
		logger.exiting(LOG_SOURCE, METHOD);
	}

}
