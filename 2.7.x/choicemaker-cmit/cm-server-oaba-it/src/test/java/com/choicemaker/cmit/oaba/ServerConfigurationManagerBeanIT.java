package com.choicemaker.cmit.oaba;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.MutableServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.TestEntities;

@RunWith(Arquillian.class)
public class ServerConfigurationManagerBeanIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public final int MAX_TEST_ITERATIONS = 10;

	/**
	 * Creates an EAR deployment.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses = null;
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	public static final String LOG_SOURCE =
		ServerConfigurationManagerBeanIT.class.getSimpleName();

	private static final Logger logger = Logger
			.getLogger(ServerConfigurationManagerBeanIT.class.getName());

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
	protected ServerConfigurationController scm;

	private int initialServerConfigCount;
	private int initialDefaultServerConfigCount;
	private boolean setupOK;

	@Before
	public void setUp() throws Exception {
		final String METHOD = "setUp";
		logEntering(METHOD);
		setupOK = true;
		try {
			initialServerConfigCount = scm.findAllServerConfigurations().size();
			initialDefaultServerConfigCount = scm.findAllDefaultServerConfigurations().size();
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

			int finalServerConfigCount =
				scm.findAllServerConfigurations().size();
			String alert = "initialServerConfigCount != finalServerConfigCount";
			assertTrue(alert,
					initialServerConfigCount == finalServerConfigCount);

			int finalDefaultServerConfigCount =
					scm.findAllDefaultServerConfigurations().size();
				alert = "initialDefaultServerConfigCount != finalDefaultServerConfigCount";
				assertTrue(alert,
						initialDefaultServerConfigCount == finalDefaultServerConfigCount);

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
	@InSequence(1)
	public void testServiceConfigurationManager() {
		assertTrue(setupOK);
		assertTrue(scm != null);
	}

	@Test
	@InSequence(10)
	public void testComputeAvailableProcessors() {
		assertTrue(setupOK);
		int count = ServerConfigurationControllerBean.computeAvailableProcessors();
		assertTrue(count > -1);
	}

	@Test
	@InSequence(10)
	public void testComputeHostName() {
		assertTrue(setupOK);
		String name = ServerConfigurationControllerBean.computeHostName();
		assertTrue(name != null && !name.trim().isEmpty());
	}

	@Test
	@InSequence(10)
	public void testComputeUniqueGenericName() {
		assertTrue(setupOK);
		Set<String> uniqueNames = new HashSet<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			String name =
				ServerConfigurationControllerBean.computeUniqueGenericName();
			uniqueNames.add(name);
		}
		assertTrue(uniqueNames.size() == MAX_TEST_ITERATIONS);
	}

	@Test
	@InSequence(20)
	public void testComputeGenericConfiguration() {
		assertTrue(setupOK);
		MutableServerConfiguration msc = scm.computeGenericConfiguration();
		assertTrue(msc.getId() == ServerConfigurationEntity.NON_PERSISTENT_ID);
		assertTrue(msc.getHostName().equals(
				ServerConfigurationControllerBean.computeHostName()));
		assertTrue(msc.getMaxChoiceMakerThreads() == ServerConfigurationControllerBean
				.computeAvailableProcessors());
		assertTrue(msc.getMaxOabaChunkFileCount() == ServerConfigurationControllerBean.DEFAULT_MAX_CHUNK_COUNT);
		assertTrue(msc.getMaxOabaChunkFileRecords() == ServerConfigurationControllerBean.DEFAULT_MAX_CHUNK_SIZE);
	}

	@Test
	@InSequence(20)
	public void testCloneServerConfiguration() {
		assertTrue(setupOK);
		MutableServerConfiguration msc = scm.computeGenericConfiguration();
		MutableServerConfiguration msc2 = scm.clone(msc);

		assertTrue(!msc.getName().equals(msc2.getName()));

		assertTrue(msc.getHostName().equals(msc2.getHostName()));
		assertTrue(msc.getMaxChoiceMakerThreads() == msc2
				.getMaxChoiceMakerThreads());
		assertTrue(msc.getMaxOabaChunkFileCount() == msc2
				.getMaxOabaChunkFileCount());
		assertTrue(msc.getMaxOabaChunkFileRecords() == msc2
				.getMaxOabaChunkFileRecords());
	}

	@Test
	@InSequence(50)
	public void testPersistFindRemove() {
		assertTrue(setupOK);
		final String METHOD = "testPersistFindRemove";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		// Create a configuration
		final MutableServerConfiguration msc =
			scm.computeGenericConfiguration();
		assertTrue(msc.getId() == ServerConfigurationEntity.NON_PERSISTENT_ID);
		assertTrue(!ServerConfigurationEntity.isPersistent(msc));

		// Save the configuration
		long id = ServerConfigurationEntity.NON_PERSISTENT_ID;
		try {
			ServerConfiguration sc = null;
			sc = scm.save(msc);
			te.add(sc);
			assertTrue(sc != null);
			id = sc.getId();
			assertTrue(ServerConfigurationEntity.isPersistent(sc));

			assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(msc, sc));
		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}
		assertTrue(id != ServerConfigurationEntity.NON_PERSISTENT_ID);
		final long scID = id;

		// Find the configuration
		ServerConfiguration sc = null;
		sc = scm.find(scID);
		assertTrue(sc != null);
		assertTrue(sc.getId() == scID);
		assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(msc, sc));

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

	@Test
	@InSequence(100)
	public void testFindAllServerConfigurations() {
		assertTrue(setupOK);
		final String METHOD = "testFindAllServerConfigurations";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		List<Long> scIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a server configuration
			MutableServerConfiguration msc = scm.computeGenericConfiguration();
			assertTrue(msc.getId() == 0);
			ServerConfiguration sc = null;
			try {
				sc = scm.save(msc);
			} catch (ServerConfigurationException e) {
				fail(e.toString());
			}
			te.add(sc);
			final long id = sc.getId();
			assertTrue(id != 0);
			scIds.add(id);
		}

		// Verify the number of server configurations has increased
		List<ServerConfiguration> serverConfigs =
			scm.findAllServerConfigurations();
		assertTrue(serverConfigs != null);

		// Find the server configurations
		boolean isFound = false;
		for (long scId : scIds) {
			for (ServerConfiguration sc : serverConfigs) {
				if (scId == sc.getId()) {
					isFound = true;
					break;
				}
			}
			assertTrue(isFound);
		}

		try {
			te.removePersistentObjects(em, utx);
		} catch (Exception x) {
			logger.severe(x.toString());
			fail(x.toString());
		}
		logExiting(METHOD);
	}

	// @Test
	// @InSequence(100)
	// public void testFindServerConfigurationsByHostNameString() {
	// assertTrue(setupOK);
	// fail("Not yet implemented");
	//
	// }
	//
	// @Test
	// @InSequence(100)
	// public void testFindServerConfigurationsByHostNameStringBoolean() {
	// assertTrue(setupOK);
	// fail("Not yet implemented");
	//
	// }
	//
	// @Test
	// @InSequence(200)
	// public void testSetDefaultConfiguration() {
	// assertTrue(setupOK);
	// fail("Not yet implemented");
	//
	// }
	//
	@Test
	@InSequence(200)
	public void testSetGetDefaultConfigurationString() {
		assertTrue(setupOK);
		final String METHOD = "testSetGetDefaultConfigurationString";
		logEntering(METHOD);
		final TestEntities te = new TestEntities();

		try {
			// Check that a default configuration is returned for a fake host,
			// even though no configurations exist
			final String fakeHost1 = UUID.randomUUID().toString();
			final boolean computeFallback = true;
			final ServerConfiguration sc1 =
				scm.getDefaultConfiguration(fakeHost1, computeFallback);
			assertTrue(sc1 != null);
			assertTrue(ServerConfigurationEntity.isPersistent(sc1));
			te.add(sc1);
			te.add(new DefaultServerConfigurationEntity(fakeHost1, sc1.getId()));

			// Verify that the no-param method works like the
			// getDefaultConfiguration(fakeHost, true) method
			final ServerConfiguration sc2 =
				scm.getDefaultConfiguration(fakeHost1);

			// Verify that the two defaults are the same persistent object
			assertTrue(sc1.getId() == sc2.getId());
			assertTrue(sc1.getUUID().equals(sc2.getUUID()));
			assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(sc1, sc2));

			// Verify that one persistent configuration now exists for the fake
			// host
			final boolean strictNoWildcards = true;
			List<ServerConfiguration> configs =
				scm.findServerConfigurationsByHostName(fakeHost1,
						strictNoWildcards);
			assertTrue(configs.size() == 1);

			// Create and save a server configuration for another fake host
			final String fakeHost3 = UUID.randomUUID().toString();
			final MutableServerConfiguration msc3 =
				scm.computeGenericConfiguration();
			msc3.setHostName(fakeHost3);
			assertTrue(msc3.getId() == 0);
			final ServerConfiguration sc3 = scm.save(msc3);
			te.add(sc3);
			final long scId = sc3.getId();
			assertTrue(scId != 0);

			// Verify that the lone, persistent configuration is now the default
			final boolean doNotComputeFallback = false;
			final ServerConfiguration sc4 =
				scm.getDefaultConfiguration(fakeHost3, doNotComputeFallback);
			assertTrue(sc4 != null);
			assertTrue(sc3.getId() == sc4.getId());
			assertTrue(sc3.getUUID().equals(sc4.getUUID()));
			assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(sc3, sc4));

			// Add another server configuration and verify that with two
			// persistent configurations, neither of which has been specified
			// as the default, that no default exists
			MutableServerConfiguration msc5 = scm.computeGenericConfiguration();
			msc5.setHostName(fakeHost3);
			assertTrue(msc5.getId() == 0);
			final ServerConfiguration sc5 = scm.save(msc5);
			te.add(sc5);
			configs =
				scm.findServerConfigurationsByHostName(fakeHost3,
						strictNoWildcards);
			assertTrue(configs.size() == 2);
			final ServerConfiguration sc6 =
				scm.getDefaultConfiguration(fakeHost3, doNotComputeFallback);
			assertTrue(sc6 == null);

			// Set the first configuration as the default and retrieve it
			scm.setDefaultConfiguration(fakeHost3, sc3);
			te.add(new DefaultServerConfigurationEntity(fakeHost3, sc3.getId()));
			final ServerConfiguration sc7 =
				scm.getDefaultConfiguration(fakeHost3, computeFallback);
			assertTrue(sc7 != null);
			assertTrue(sc3.getId() == sc7.getId());
			assertTrue(sc3.getUUID().equals(sc7.getUUID()));
			assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(sc3, sc7));

		} catch (ServerConfigurationException e) {
			fail(e.toString());
		} finally {
			try {
				te.removePersistentObjects(em, utx);
			} catch (Exception x) {
				logger.severe(x.toString());
				fail(x.toString());
			}
		}

		logExiting(METHOD);
	}

	// @Test
	// @InSequence(200)
	// public void testGetDefaultConfigurationStringBoolean() {
	// assertTrue(setupOK);
	// fail("Not yet implemented");
	//
	// }

}
