package com.choicemaker.cmit.oaba;

import static com.choicemaker.cmit.oaba.util.OabaConstants.CURRENT_MAVEN_COORDINATES;
import static com.choicemaker.cmit.oaba.util.OabaConstants.PERSISTENCE_CONFIGURATION;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_HAS_BEANS;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_MODULE_NAME;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_POM_FILE;
import static com.choicemaker.cmit.utils.DeploymentUtils.DEFAULT_TEST_CLASSES_PATH;
import static com.choicemaker.cmit.utils.DeploymentUtils.createEAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.createJAR;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolveDependencies;
import static com.choicemaker.cmit.utils.DeploymentUtils.resolvePom;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.io.blocking.automated.AbaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsManager;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbaSettingsBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaSettingsBean;
import com.choicemaker.cmit.utils.MutableProbabilityModelStub;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class SettingsManagerBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public static final String REGEX_EJB_DEPENDENCIES =
		"com.choicemaker.cm.io.blocking.automated.offline.server.*.jar"
				+ "|com.choicemaker.e2.ejb.*.jar";

	protected static Random random = new Random(new Date().getTime());

	protected AbaSettings randomAbaSettings() {
		AbaSettings retVal =
			new AbaSettingsBean(
					random.nextInt(AbaSettings.DEFAULT_LIMIT_PER_BLOCKING_SET),
					random.nextInt(AbaSettings.DEFAULT_LIMIT_SINGLE_BLOCKING_SET),
					random.nextInt(AbaSettings.DEFAULT_SINGLE_TABLE_GRACE_LIMIT));
		return retVal;
	}

	protected OabaSettings randomOabaSettings() {
		OabaSettings retVal =
			new OabaSettingsBean(randomAbaSettings(),
					random.nextInt(OabaSettings.DEFAULT_MAX_BLOCKSIZE),
					random.nextInt(OabaSettings.DEFAULT_MAX_CHUNKSIZE),
					random.nextInt(OabaSettings.DEFAULT_MAX_OVERSIZED),
					random.nextInt(OabaSettings.DEFAULT_MIN_FIELDS),
					random.nextInt(OabaSettings.DEFAULT_INTERVAL));
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
		PomEquippedResolveStage pom = resolvePom(DEFAULT_POM_FILE);

		File[] libs = resolveDependencies(pom);

		// Filter the OABA server and E2Plaform JARs from the dependencies;
		// they will be added as modules.
		final Pattern p = Pattern.compile(REGEX_EJB_DEPENDENCIES);
		Set<File> ejbJARs = new LinkedHashSet<>();
		List<File> filteredLibs = new LinkedList<>();
		for (File lib : libs) {
			String name = lib.getName();
			Matcher m = p.matcher(name);
			if (m.matches()) {
				boolean isAdded = ejbJARs.add(lib);
				if (!isAdded) {
					String path = lib.getAbsolutePath();
					throw new RuntimeException("failed to add (duplicate?): "
							+ path);
				}
			} else {
				filteredLibs.add(lib);
			}
		}
		File[] libs2 = filteredLibs.toArray(new File[filteredLibs.size()]);

		JavaArchive tests =
			createJAR(pom, CURRENT_MAVEN_COORDINATES, DEFAULT_MODULE_NAME,
					DEFAULT_TEST_CLASSES_PATH, PERSISTENCE_CONFIGURATION,
					DEFAULT_HAS_BEANS);
		EnterpriseArchive retVal = createEAR(tests, libs2, TESTS_AS_EJB_MODULE);

		// Filter the targeted paths from the EJB JARs
		for (File ejb : ejbJARs) {
			JavaArchive module =
				ShrinkWrap.createFromZipFile(JavaArchive.class, ejb);
			retVal.addAsModule(module);
		}

		return retVal;
	}

	public static final String LOG_SOURCE = SettingsManagerBeanIT.class
			.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(SettingsManagerBeanIT.class.getName());

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
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	protected SettingsManager scm;

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
			initialAbaSettingsCount = scm.findAllAbaSettings().size();
			initialDefaultAbaSettingsCount =
				scm.findAllDefaultAbaSettings().size();
			initialOabaSettingsCount = scm.findAllOabaSettings().size();
			initialDefaultOabaSettingsCount =
				scm.findAllDefaultOabaSettings().size();
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

			final int finalAbaSettingsCount = scm.findAllAbaSettings().size();
			String alert = "initialAbaSettingsCount != finalAbaSettingsCount";
			assertTrue(alert, initialAbaSettingsCount == finalAbaSettingsCount);

			final int finalDefaultAbaSettingsCount =
				scm.findAllDefaultAbaSettings().size();
			alert =
				"initialDefaultAbaSettingsCount != finalDefaultAbaSettingsCount";
			assertTrue(
					alert,
					initialDefaultAbaSettingsCount == finalDefaultAbaSettingsCount);

			final int finalOabaSettingsCount = scm.findAllOabaSettings().size();
			alert = "initialOabaSettingsCount != finalOabaSettingsCount";
			assertTrue(alert,
					initialOabaSettingsCount == finalOabaSettingsCount);

			final int finalDefaultOabaSettingsCount =
				scm.findAllDefaultOabaSettings().size();
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
		assertTrue(aba0.getId() == AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!AbaSettingsBean.isPersistent(aba0));

		// Save the configuration
		long id = AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;
		AbaSettings aba1 = null;
		aba1 = scm.save(aba0);
		te.add(aba1);
		assertTrue(aba1 != null);
		id = aba1.getId();
		assertTrue(AbaSettingsBean.isPersistent(aba1));
		assertTrue(id != AbaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID);
		final long abaId1 = id;

		// Find the configuration
		AbaSettings aba2 = null;
		aba2 = scm.findAbaSettings(abaId1);
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
		assertTrue(oaba0.getId() == OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID);
		assertTrue(!OabaSettingsBean.isPersistent(oaba0));

		// Save the configuration
		long id = OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID;
		OabaSettings oaba1 = null;
		oaba1 = scm.save(oaba0);
		te.add(oaba1);
		assertTrue(oaba1 != null);
		id = oaba1.getId();
		assertTrue(OabaSettingsBean.isPersistent(oaba1));
		assertTrue(id != OabaSettingsBean.NONPERSISTENT_ABA_SETTINGS_ID);
		final long oabaId1 = id;

		// Find the configuration
		OabaSettings oaba2 = null;
		oaba2 = scm.findOabaSettings(oabaId1);
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
		final AbaSettings aba0 = scm.findDefaultAbaSettings(mpm, d0, b0);
		assertTrue(aba0 == null);
		
		// Create random ABA settings, save them and set them as a default
		final AbaSettings aba1 = randomAbaSettings();
		te.add(aba1);
		final AbaSettings aba2 = scm.save(aba1);
		te.add(aba2);
		scm.setDefaultAbaConfiguration(mpm, d0, b0, aba2);
		
		// Retrieve the default and check the settings
		final AbaSettings aba3 = scm.findDefaultAbaSettings(mpm, d0, b0);
		te.add(aba3);
		assertTrue(aba3 != null);
		assertTrue(aba3.getLimitPerBlockingSet() == aba1.getLimitPerBlockingSet());
		assertTrue(aba3.getLimitSingleBlockingSet() == aba1.getLimitSingleBlockingSet());
		assertTrue(aba3.getSingleTableBlockingSetGraceLimit() == aba1.getSingleTableBlockingSetGraceLimit());

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
		final OabaSettings oaba0 = scm.findDefaultOabaSettings(mpm, d0, b0);
		assertTrue(oaba0 == null);
		
		// Create random OABA settings, save them and set them as a default
		final OabaSettings oaba1 = randomOabaSettings();
		te.add(oaba1);
		final OabaSettings oaba2 = scm.save(oaba1);
		te.add(oaba2);
		scm.setDefaultOabaConfiguration(mpm, d0, b0, oaba2);
		
		// Retrieve the default and check the settings
		final OabaSettings oaba3 = scm.findDefaultOabaSettings(mpm, d0, b0);
		te.add(oaba3);
		assertTrue(oaba3 != null);
		assertTrue(oaba3.getLimitPerBlockingSet() == oaba1.getLimitPerBlockingSet());
		assertTrue(oaba3.getLimitSingleBlockingSet() == oaba1.getLimitSingleBlockingSet());
		assertTrue(oaba3.getSingleTableBlockingSetGraceLimit() == oaba1.getSingleTableBlockingSetGraceLimit());
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
	
}
