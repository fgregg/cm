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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.DefaultServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.MutableServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.DefaultServerConfigurationEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.TestEntityCounts;

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
	public void testPrequisites() {
		assertTrue(em != null);
		assertTrue(utx != null);
		assertTrue(serverController != null);
	}

	@Test
	@InSequence(10)
	public void testComputeAvailableProcessors() {
		int count =
			ServerConfigurationControllerBean.computeAvailableProcessors();
		assertTrue(count > -1);
	}

	@Test
	@InSequence(10)
	public void testComputeHostName() {
		String name = ServerConfigurationControllerBean.computeHostName();
		assertTrue(name != null && !name.trim().isEmpty());
	}

	@Test
	@InSequence(10)
	public void testComputeUniqueGenericName() {
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
		MutableServerConfiguration msc =
			serverController.computeGenericConfiguration();
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
		MutableServerConfiguration msc =
			serverController.computeGenericConfiguration();
		MutableServerConfiguration msc2 = serverController.clone(msc);

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
		final String METHOD = "testPersistFindRemove";
		logger.entering(LOG_SOURCE, METHOD);

		// Create a configuration
		final MutableServerConfiguration msc =
			serverController.computeGenericConfiguration();
		assertTrue(msc.getId() == ServerConfigurationEntity.NON_PERSISTENT_ID);
		assertTrue(!ServerConfigurationEntity.isPersistent(msc));

		// Save the configuration
		long id = ServerConfigurationEntity.NON_PERSISTENT_ID;
		try {
			ServerConfiguration sc = null;
			sc = serverController.save(msc);
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
		sc = serverController.findServerConfiguration(scID);
		assertTrue(sc != null);
		assertTrue(sc.getId() == scID);
		assertTrue(ServerConfigurationEntity.equalsIgnoreIdUuid(msc, sc));

		checkCounts();
	}

	@Test
	@InSequence(100)
	public void testFindAllServerConfigurations() {
		final String METHOD = "testFindAllServerConfigurations";
		logger.entering(LOG_SOURCE, METHOD);

		List<Long> scIds = new LinkedList<>();
		for (int i = 0; i < MAX_TEST_ITERATIONS; i++) {
			// Create and save a server configuration
			MutableServerConfiguration msc =
				serverController.computeGenericConfiguration();
			assertTrue(msc.getId() == 0);
			ServerConfiguration sc = null;
			try {
				sc = serverController.save(msc);
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
			serverController.findAllServerConfigurations();
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

		checkCounts();
	}

	@Test
	@InSequence(200)
	public void testSetGetDefaultConfigurationString() {
		final String METHOD = "testSetGetDefaultConfigurationString";
		logger.entering(LOG_SOURCE, METHOD);

		try {
			// Check that a default configuration is not returned for a (fake)
			// host when no defaults exist
			final String fakeHost1 = UUID.randomUUID().toString();
			DefaultServerConfiguration dsc2 =
				serverController.findDefaultServerConfiguration(fakeHost1);
			assertTrue(dsc2 == null);

			// Create a default configuration
			ServerConfiguration generic =
				serverController.computeGenericConfiguration(fakeHost1);
			final ServerConfiguration sc1 =
				serverController.setDefaultConfiguration(fakeHost1, generic);
			assertTrue(sc1 != null);
			assertTrue(ServerConfigurationEntity.isPersistent(sc1));
			te.add(sc1);
			dsc2 = serverController.findDefaultServerConfiguration(fakeHost1);
			assertTrue(dsc2 != null);
			te.add(dsc2);
			assertTrue(dsc2.getServerConfigurationId() == sc1.getId());

			// Verify that one persistent configuration now exists for the fake
			// host
			final boolean strictNoWildcards = true;
			List<ServerConfiguration> configs =
				serverController.findServerConfigurationsByHostName(fakeHost1,
						strictNoWildcards);
			assertTrue(configs.size() == 1);

			// Create and save a server configuration for another fake host
			final String fakeHost3 = UUID.randomUUID().toString();
			final MutableServerConfiguration msc3 =
				serverController.computeGenericConfiguration();
			msc3.setHostName(fakeHost3);
			assertTrue(msc3.getId() == 0);
			final ServerConfiguration sc3 = serverController.save(msc3);
			te.add(sc3);
			final long scId = sc3.getId();
			assertTrue(scId != 0);

			// Add another server configuration and verify that with two
			// persistent configurations, neither of which has been specified
			// as the default, that no default exists
			MutableServerConfiguration msc5 =
				serverController.computeGenericConfiguration();
			msc5.setHostName(fakeHost3);
			assertTrue(msc5.getId() == 0);
			final ServerConfiguration sc5 = serverController.save(msc5);
			te.add(sc5);
			configs =
				serverController.findServerConfigurationsByHostName(fakeHost3,
						strictNoWildcards);
			assertTrue(configs.size() == 2);
			final DefaultServerConfiguration dsc6 =
				serverController.findDefaultServerConfiguration(fakeHost3);
			assertTrue(dsc6 == null);

			// Set the first configuration as the default and retrieve it
			serverController.setDefaultConfiguration(fakeHost3, sc3);
			te.add(new DefaultServerConfigurationEntity(fakeHost3, sc3.getId()));
			final DefaultServerConfiguration dsc7 =
				serverController.findDefaultServerConfiguration(fakeHost3);
			assertTrue(dsc7 != null);
			assertTrue(dsc7.getServerConfigurationId() == sc3.getId());

		} catch (ServerConfigurationException e) {
			fail(e.toString());
		}

		checkCounts();
		logger.exiting(LOG_SOURCE, METHOD);
	}

}
