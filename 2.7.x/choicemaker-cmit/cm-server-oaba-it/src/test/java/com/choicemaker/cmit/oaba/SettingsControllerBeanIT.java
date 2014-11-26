package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_INTERVAL;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_BLOCKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_CHUNKSIZE;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_MATCHES;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MAX_OVERSIZED;
import static com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings.DEFAULT_MIN_FIELDS;
import static com.choicemaker.cmit.utils.EntityManagerUtils.MAX_MAX_SINGLE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultSettingsEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.MutableProbabilityModelStub;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class SettingsControllerBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

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

	/**
	 * Workaround for logger.entering(String,String) not showing up in JBOSS
	 * server log
	 */
	private static void logEntering(String method) {
		logger.info("Entering " + LOG_SOURCE + "." + method);
	}

	/**
	 * Workaround for logger.exiting(String,String) not showing up in JBOSS
	 * server log
	 */
	private static void logExiting(String method) {
		logger.info("Exiting " + LOG_SOURCE + "." + method);
	}

	@Resource
	private UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@EJB
	private SettingsController sm;

	private int initialAbaSettingsCount;
	private int initialDefaultAbaSettingsCount;
	private int initialOabaSettingsCount;
	private int initialDefaultOabaSettingsCount;
	private boolean setupOK;

	@Before
	public void setUp() throws Exception {
		final String METHOD = "setUp";
		logEntering(METHOD);
		setupOK = true;
		try {
			initialAbaSettingsCount = sm.findAllAbaSettings().size();
			initialDefaultAbaSettingsCount =
				sm.findAllDefaultAbaSettings().size();
			initialOabaSettingsCount = sm.findAllOabaSettings().size();
			initialDefaultOabaSettingsCount =
				sm.findAllDefaultOabaSettings().size();
		} catch (Exception x) {
			logger.severe(x.toString());
			setupOK = false;
		}
		logExiting(METHOD);
	}

	@After
	public void tearDown() throws Exception {
		final String METHOD = "tearDown";
		logEntering(METHOD);
		try {

			final int finalAbaSettingsCount = sm.findAllAbaSettings().size();
			String alert = "initialAbaSettingsCount != finalAbaSettingsCount";
			assertTrue(alert, initialAbaSettingsCount == finalAbaSettingsCount);

			final int finalDefaultAbaSettingsCount =
				sm.findAllDefaultAbaSettings().size();
			alert =
				"initialDefaultAbaSettingsCount != finalDefaultAbaSettingsCount";
			assertTrue(
					alert,
					initialDefaultAbaSettingsCount == finalDefaultAbaSettingsCount);

			final int finalOabaSettingsCount = sm.findAllOabaSettings().size();
			alert = "initialOabaSettingsCount != finalOabaSettingsCount";
			assertTrue(alert,
					initialOabaSettingsCount == finalOabaSettingsCount);

			final int finalDefaultOabaSettingsCount =
				sm.findAllDefaultOabaSettings().size();
			alert =
				"initialDefaultOabaSettingsCount != finalDefaultOabaSettingsCount";
			assertTrue(
					alert,
					initialDefaultOabaSettingsCount == finalDefaultOabaSettingsCount);

		} catch (Exception x) {
			logger.severe(x.toString());
		} catch (AssertionError x) {
			logger.severe(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	@InSequence(1)
	public void testEntityManager() {
		assertTrue(setupOK);
		assertTrue(em != null);
	}

	@Test
	@InSequence(1)
	public void testUserTransaction() {
		assertTrue(setupOK);
		assertTrue(utx != null);
	}

	@Test
	@InSequence(50)
	public void testPersistFindRemoveAba() {
		assertTrue(setupOK);
		final String METHOD = "testPersistFindRemove";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		// Create a configuration
		final AbaSettings aba0 = randomAbaSettings();
		assertTrue(aba0.getId() == AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!AbaSettingsEntity.isPersistent(aba0));

		// Save the configuration
		long id = AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID;
		AbaSettings aba1 = null;
		aba1 = sm.save(aba0);
		te.add(aba1);
		assertTrue(aba1 != null);
		id = aba1.getId();
		assertTrue(AbaSettingsEntity.isPersistent(aba1));
		assertTrue(id != AbaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		final long abaId1 = id;

		// Find the configuration
		AbaSettings aba2 = null;
		aba2 = sm.findAbaSettings(abaId1);
		assertTrue(aba2 != null);
		assertTrue(aba2.getId() == abaId1);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	@InSequence(50)
	public void testPersistFindRemoveOaba() {
		assertTrue(setupOK);
		final String METHOD = "testPersistFindRemove";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		// Create a configuration
		final OabaSettings oaba0 = randomOabaSettings();
		assertTrue(oaba0.getId() == OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!OabaSettingsEntity.isPersistent(oaba0));

		// Save the configuration
		long id = OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID;
		OabaSettings oaba1 = null;
		oaba1 = sm.save(oaba0);
		te.add(oaba1);
		assertTrue(oaba1 != null);
		id = oaba1.getId();
		assertTrue(OabaSettingsEntity.isPersistent(oaba1));
		assertTrue(id != OabaSettingsEntity.NONPERSISTENT_ABA_SETTINGS_ID);
		final long oabaId1 = id;

		// Find the configuration
		OabaSettings oaba2 = null;
		oaba2 = sm.findOabaSettings(oabaId1);
		assertTrue(oaba2 != null);
		assertTrue(oaba2.getId() == oabaId1);

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	public void testSetGetDefaultAbaConfiguration() {
		assertTrue(setupOK);
		final String METHOD = "testSetGetDefaultConfigurationString";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		// Verify that no default is returned for a non-existent model
		// with non-existent database and blocking configurations
		MutableProbabilityModelStub mpm = new MutableProbabilityModelStub();
		final String d0 = randomString();
		mpm.databaseConfigurationName = d0;
		final String b0 = randomString();
		mpm.blockingConfigurationName = b0;
		final AbaSettings aba0 = sm.findDefaultAbaSettings(mpm);
		assertTrue(aba0 == null);

		// Create random ABA settings, save them and set them as a default
		final AbaSettings aba1 = randomAbaSettings();
		te.add(aba1);
		final AbaSettings aba2 = sm.save(aba1);
		te.add(aba2);
		final DefaultSettingsEntity dsb2 =
			sm.setDefaultAbaConfiguration(mpm, d0, b0, aba2);
		te.add(dsb2);

		// Retrieve the default and check the settings
		final AbaSettings aba3 = sm.findDefaultAbaSettings(mpm);
		te.add(aba3);
		assertTrue(aba3 != null);
		assertTrue(aba3.getLimitPerBlockingSet() == aba1
				.getLimitPerBlockingSet());
		assertTrue(aba3.getLimitSingleBlockingSet() == aba1
				.getLimitSingleBlockingSet());
		assertTrue(aba3.getSingleTableBlockingSetGraceLimit() == aba1
				.getSingleTableBlockingSetGraceLimit());

		// Clean up the database
		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	public void testSetGetDefaultOabaConfiguration() {
		assertTrue(setupOK);
		final String METHOD = "testSetGetDefaultConfigurationString";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		// Verify that no default is returned for a non-existent model
		// with non-existent datoabase and blocking configurations
		MutableProbabilityModelStub mpm = new MutableProbabilityModelStub();
		final String d0 = randomString();
		mpm.databaseConfigurationName = d0;
		final String b0 = randomString();
		mpm.blockingConfigurationName = b0;
		final OabaSettings oaba0 = sm.findDefaultOabaSettings(mpm);
		assertTrue(oaba0 == null);

		// Create random OABA settings, save them and set them as a default
		final OabaSettings oaba1 = randomOabaSettings();
		te.add(oaba1);
		final OabaSettings oaba2 = sm.save(oaba1);
		te.add(oaba2);
		final DefaultSettingsEntity dsb2 =
			sm.setDefaultOabaConfiguration(mpm, d0, b0, oaba2);
		te.add(dsb2);

		// Retrieve the default and check the settings
		final OabaSettings oaba3 = sm.findDefaultOabaSettings(mpm);
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
		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

//	@Test
//	public void testMaxSingle() {
//		final String METHOD = "testMaxSingle";
//		TestEntities te = new TestEntities();
//
//		// Create parameters with a known value
//		OabaParametersEntity template =
//			prmController.createBatchParameters(METHOD, te);
//		final int v1 = random.nextInt();
//		OabaParametersEntity params =
//			new OabaParametersEntity(template.getModelConfigurationName(), v1,
//					template.getLowThreshold(), template.getHighThreshold(),
//					template.getStageRs(), template.getMasterRs(),
//					template.getTransitivity());
//		te.add(params);
//
//		// Save the params
//		final long id1 = prmController.save(params).getId();
//
//		// Get the params
//		params = null;
//		params = prmController.find(id1);
//
//		// Check the value
//		final int v2 = params.getMaxSingle();
//		assertTrue(v1 == v2);
//
//		try {
//			te.removePersistentObjects(em, utx);
//		} catch (Exception x) {
//			logger.severe(x.toString());
//			fail(x.toString());
//		}
//	}

}
