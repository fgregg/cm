package com.choicemaker.cmit.oaba;

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.EVT_DONE_REC_VAL;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_REC_VAL;
import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.PersistableRecordSource;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.BlockingOABA;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.UpdateStatus;
import com.choicemaker.cmit.oaba.util.OabaDeploymentUtils;
import com.choicemaker.cmit.utils.EntityManagerUtils;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaTestUtils;
import com.choicemaker.cmit.utils.SimplePersonSqlServerTestConfiguration;
import com.choicemaker.e2.ejb.EjbPlatform;

@RunWith(Arquillian.class)
public class StartOabaIT {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	public static final String LOG_SOURCE = StartOabaIT.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(StartOabaIT.class
			.getName());

	/**
	 * Creates an EAR deployment in which the OABA server JAR is missing the
	 * BlockingOABA, SingleRecordMatch and UpdateStatus message beans. This
	 * allows other classes to attach to the block, singleRecordMatch and
	 * updateStatus queues for testing.
	 */
	@Deployment
	public static EnterpriseArchive createEarArchive() {
		Class<?>[] removedClasses =
			new Class<?>[] {
					BlockingOABA.class, SingleRecordMatch.class,
					UpdateStatus.class };
		return OabaDeploymentUtils.createEarArchive(removedClasses,
				TESTS_AS_EJB_MODULE);
	}

	@Resource(name = "DefaultManagedExecutorService")
	ManagedExecutorService executor;

	@Resource
	UserTransaction utx;

	@PersistenceContext(unitName = "oaba")
	EntityManager em;

	@EJB
	EjbPlatform e2service;

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	protected OabaService batchQuery;

	@EJB
	protected TestController controller;

	@EJB
	protected PersistableRecordSourceController rsController;

	@Resource(lookup = "choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Inject
	JMSContext jmsContext;

	private int initialOabaParamsCount;
	private int initialOabaJobCount;
	private int initialOabaProcessingCount;
	private boolean setupOK;

	@Before
	public void setUp() {
		final String METHOD = "setUp";
		logger.entering(LOG_SOURCE, METHOD);
		setupOK = true;
		try {
			initialOabaParamsCount = controller.findAllOabaParameters().size();
			initialOabaJobCount = controller.findAllOabaJobs().size();
			initialOabaProcessingCount =
				controller.findAllOabaProcessing().size();
		} catch (Exception x) {
			logger.severe(x.toString());
			setupOK = false;
		}
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@After
	public void tearDown() {
		final String METHOD = "tearDown";
		logger.entering(LOG_SOURCE, METHOD);
		try {

			int finalOabaParamsCount =
				controller.findAllOabaParameters().size();
			String alert = "initialOabaParamsCount != finalOabaParamsCount";
			assertTrue(alert, initialOabaParamsCount == finalOabaParamsCount);

			int finalOabaJobCount = controller.findAllOabaJobs().size();
			alert = "initialOabaJobCount != finalOabaJobCount";
			assertTrue(alert, initialOabaJobCount == finalOabaJobCount);

			int finalOabaProcessingCount =
				controller.findAllOabaProcessing().size();
			alert = "initialOabaProcessingCount != finalOabaProcessingCount";
			assertTrue(alert,
					initialOabaProcessingCount == finalOabaProcessingCount);

		} catch (Exception x) {
			logger.severe(x.toString());
		} catch (AssertionError x) {
			logger.severe(x.toString());
		}
		logger.exiting(LOG_SOURCE, METHOD);
	}

	@Test
	@InSequence(1)
	public void testPrequisites() {
		assertTrue(setupOK);
		assertTrue(em != null);
		assertTrue(executor != null);
		assertTrue(utx != null);
		assertTrue(e2service != null);
		assertTrue(batchQuery != null);
		assertTrue(controller != null);
		assertTrue(blockQueue != null);
		assertTrue(updateQueue != null);
		assertTrue(singleMatchQueue != null);
		assertTrue(jmsContext != null);
	}

	@Test
	@InSequence(4)
	public void clearBlockQueue() {
		assertTrue(setupOK);

		JmsUtils.clearStartDataFromQueue(LOG_SOURCE, jmsContext, blockQueue);
		JmsUtils.clearStartDataFromQueue(LOG_SOURCE, jmsContext,
				singleMatchQueue);

		JmsUtils.clearUpdateDataFromQueue(LOG_SOURCE, jmsContext, updateQueue);

	}

	@Test
	@InSequence(5)
	public void testStartLinkage() throws ServerConfigurationException {
		assertTrue(setupOK);
		String TEST = "testStartOABALinkage";
		logger.entering(LOG_SOURCE, TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging = rsController.save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);
		prs = c.getMasterRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource master = rsController.save(prs);
		assertTrue(master.getId() != PersistableRecordSource.NONPERSISTENT_ID);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging, master, c.getOabaTask());

		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(settingsController,
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(serverController);

		OabaTestUtils.testIntermediateOabaProcessing(LOG_SOURCE, TEST,
				externalID, bp, oabaSettings, serverConfiguration, batchQuery,
				jobController, paramsController, processingController,
				jmsContext, blockQueue, updateQueue, em, utx, EVT_DONE_REC_VAL,
				PCT_DONE_REC_VAL);

		logger.exiting(LOG_SOURCE, TEST);
	}

	@Test
	@InSequence(6)
	public void testStartDeduplication() throws ServerConfigurationException {
		assertTrue(setupOK);
		String TEST = "testStartOABAStage";
		logger.entering(LOG_SOURCE, TEST);

		final String externalID = EntityManagerUtils.createExternalId(TEST);
		final SimplePersonSqlServerTestConfiguration c =
			new SimplePersonSqlServerTestConfiguration();
		c.initialize(this.e2service.getPluginRegistry());

		PersistableRecordSource prs = c.getStagingRecordSource();
		assertTrue(prs != null);
		assertTrue(prs.getId() == PersistableRecordSource.NONPERSISTENT_ID);
		final PersistableRecordSource staging = rsController.save(prs);
		assertTrue(staging.getId() != PersistableRecordSource.NONPERSISTENT_ID);

		final OabaParameters bp =
			new OabaParametersEntity(c.getModelConfigurationName(), c
					.getThresholds().getDifferThreshold(), c.getThresholds()
					.getMatchThreshold(), staging);
		final OabaSettings oabaSettings =
			OabaUtils.getDefaultOabaSettings(settingsController,
					bp.getStageModel());
		final ServerConfiguration serverConfiguration =
			OabaUtils.getDefaultServerConfiguration(serverController);

		OabaTestUtils.testIntermediateOabaProcessing(LOG_SOURCE, TEST,
				externalID, bp, oabaSettings, serverConfiguration, batchQuery,
				jobController, paramsController, processingController,
				jmsContext, blockQueue, updateQueue, em, utx, EVT_DONE_REC_VAL,
				PCT_DONE_REC_VAL);

		logger.exiting(LOG_SOURCE, TEST);
	}

}
