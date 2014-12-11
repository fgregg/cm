package com.choicemaker.cmit.oaba.failed_experiments;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cmit.oaba.TestController;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;
//import com.choicemaker.cm.io.blocking.automated.offline.server.impl.SingleRecordMatch;

public abstract class AbstractOabaProcessing {

	public static final boolean TESTS_AS_EJB_MODULE = true;

	// -- Customizable methods

	protected abstract String getSourceName();

	protected abstract Logger getLogger();

	protected abstract WellKnownTestConfiguration createTestConfiguration(
			CMPluginRegistry registry);

	// -- Template methods

	@Before
	public void setUp() {
		final String METHOD = "setUp";
		getLogger().entering(getSourceName(), METHOD);
		setupOK = true;
		try {
			initialOabaParamsCount =
				getTestController().findAllOabaParameters().size();
			initialOabaJobCount = getTestController().findAllOabaJobs().size();
			initialOabaProcessingCount =
				getTestController().findAllOabaProcessing().size();
		} catch (Exception x) {
			getLogger().severe(x.toString());
			setupOK = false;
		}
		getLogger().exiting(getSourceName(), METHOD);
	}

	@After
	public void tearDown() {
		final String METHOD = "tearDown";
		getLogger().entering(getSourceName(), METHOD);
		try {

			int finalOabaParamsCount =
				getTestController().findAllOabaParameters().size();
			String alert = "initialOabaParamsCount != finalOabaParamsCount";
			assertTrue(alert, initialOabaParamsCount == finalOabaParamsCount);

			int finalOabaJobCount =
				getTestController().findAllOabaJobs().size();
			alert = "initialOabaJobCount != finalOabaJobCount";
			assertTrue(alert, initialOabaJobCount == finalOabaJobCount);

			int finalOabaProcessingCount =
				getTestController().findAllOabaProcessing().size();
			alert = "initialOabaProcessingCount != finalOabaProcessingCount";
			assertTrue(alert,
					initialOabaProcessingCount == finalOabaProcessingCount);

		} catch (Exception x) {
			getLogger().severe(x.toString());
		} catch (AssertionError x) {
			getLogger().severe(x.toString());
		}
		getLogger().exiting(getSourceName(), METHOD);
	}

	@Test
	@InSequence(1)
	public void testPrequisites() {
		assertTrue(setupOK);

		assertTrue(getEm() != null);
		assertTrue(getUtx() != null);
		assertTrue(getE2service() != null);
		assertTrue(getBatchQuery() != null);
		assertTrue(getTestController() != null);
		assertTrue(getChunkQueue() != null);
		assertTrue(getDedupQueue() != null);
		assertTrue(getMatchDedupQueue() != null);
		assertTrue(getMatchSchedulerQueue() != null);
		assertTrue(getUpdateQueue() != null);
		assertTrue(getJmsContext() != null);
	}

	@Test
	@InSequence(4)
	public void clearQueues() {
		assertTrue(setupOK);

		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getBlockQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getStartQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getSingleMatchQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getDedupQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getChunkQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getMatchDedupQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getMatchSchedulerQueue());

		JmsUtils.clearUpdateDataFromQueue(getSourceName(), getJmsContext(),
				getUpdateQueue());
	}

	@Test
	@InSequence(5)
	public abstract void testStartLinkage() throws Exception;

	@Test
	@InSequence(6)
	public abstract void testStartDeduplication() throws Exception;

	// -- Instance data

	protected int initialOabaParamsCount;

	protected int getInitialOabaParamsCount() {
		return initialOabaParamsCount;
	}

	protected int initialOabaJobCount;

	protected int getInitialOabaJobCount() {
		return initialOabaJobCount;
	}

	protected int initialOabaProcessingCount;

	protected int getInitialOabaProcessingCount() {
		return initialOabaProcessingCount;
	}

	private boolean setupOK;

	protected boolean isSetupOK() {
		return setupOK;
	}

	protected void setSetupOK(boolean setupOK) {
		this.setupOK = setupOK;
	}

	// -- Injected instance data

	@Resource
	public UserTransaction utx;

	protected UserTransaction getUtx() {
		return utx;
	}

	@PersistenceContext(unitName = "oaba")
	public EntityManager em;

	protected EntityManager getEm() {
		return em;
	}

	@EJB
	public EjbPlatform e2service;

	protected EjbPlatform getE2service() {
		return e2service;
	}

	@EJB
	public OabaJobControllerBean jobController;

	protected OabaJobControllerBean getJobController() {
		return jobController;
	}

	@EJB
	public OabaParametersControllerBean paramsController;

	protected OabaParametersControllerBean getParamsController() {
		return paramsController;
	}

	@EJB
	public SettingsController settingsController;

	protected SettingsController getSettingsController() {
		return settingsController;
	}

	@EJB
	public ServerConfigurationController serverController;

	protected ServerConfigurationController getServerController() {
		return serverController;
	}

	@EJB
	public OabaProcessingControllerBean processingController;

	protected OabaProcessingControllerBean getProcessingController() {
		return processingController;
	}

	@EJB
	public OabaService batchQuery;

	protected OabaService getBatchQuery() {
		return batchQuery;
	}

	@EJB
	public TestController testController;

	protected TestController getTestController() {
		return testController;
	}

	@EJB
	public PersistableRecordSourceController rsController;

	protected PersistableRecordSourceController getRecordSourceController() {
		return rsController;
	}

	@Resource(lookup = "choicemaker/urm/jms/getBlockQueue()")
	public Queue blockQueue;

	protected Queue getBlockQueue() {
		return blockQueue;
	}

	@Resource(lookup = "choicemaker/urm/jms/getChunkQueue()")
	public Queue chunkQueue;

	protected Queue getChunkQueue() {
		return chunkQueue;
	}

	@Resource(lookup = "choicemaker/urm/jms/getDedupQueue()")
	public Queue dedupQueue;

	protected Queue getDedupQueue() {
		return dedupQueue;
	}

	@Resource(lookup = "choicemaker/urm/jms/getMatchDedupQueue()")
	public Queue matchDedupQueue;

	protected Queue getMatchDedupQueue() {
		return matchDedupQueue;
	}

	@Resource(lookup = "choicemaker/urm/jms/getMatchSchedulerQueue()")
	public Queue matchSchedulerQueue;

	protected Queue getMatchSchedulerQueue() {
		return matchSchedulerQueue;
	}

	@Resource(lookup = "java:/choicemaker/urm/jms/getSingleMatchQueue()")
	public Queue singleMatchQueue;

	protected Queue getSingleMatchQueue() {
		return singleMatchQueue;
	}

	@Resource(lookup = "java:/choicemaker/urm/jms/getStartQueue()")
	public Queue startQueue;

	protected Queue getStartQueue() {
		return startQueue;
	}

	@Resource(lookup = "java:/choicemaker/urm/jms/getUpdateQueue()")
	public Queue updateQueue;

	protected Queue getUpdateQueue() {
		return updateQueue;
	}

	@Inject
	public JMSContext jmsContext;

	protected JMSContext getJmsContext() {
		return jmsContext;
	}

}
