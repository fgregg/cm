package com.choicemaker.cmit.oaba;

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
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cmit.oaba.util.OabaTestController;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

public abstract class AbstractOabaProcessingTest<T extends WellKnownTestConfiguration>
		implements OabaProcessingTest<T> {
	
	// -- Read-write instance data

	private int initialOabaParamsCount;
	
	private int initialOabaJobCount;

	private int initialOabaProcessingCount;

	private boolean setupOK;
	
	/**
	 * A read-only member that is lazily initialized by
	 * {@link #getTestConfiguration()}
	 */
	private T testConfiguration;

	// -- Immutable, constructed instance data

	private final String sourceName;
	
	private final Logger logger;

	private final int eventId;

	private final int percentComplete;
	
	private final Class<T> configurationClass;
	
	private final OabaProcessingPhase oabaPhase;

	// -- Read-only, injected instance data

	@EJB
	private EjbPlatform e2service;

	@Resource
	private UserTransaction utx;

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private OabaService batchQuery;

	@EJB
	private OabaTestController oabaTestController;

	@EJB
	private PersistableRecordSourceController rsController;

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

	@Resource(lookup = "choicemaker/urm/jms/blockQueue")
	private Queue blockQueue;

	@Resource(lookup = "choicemaker/urm/jms/chunkQueue")
	private Queue chunkQueue;

	@Resource(lookup = "choicemaker/urm/jms/dedupQueue")
	private Queue dedupQueue;

	@Resource(lookup = "choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;
	
	@Resource(lookup = "choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Constructor
	
	@Inject
	public AbstractOabaProcessingTest(String n, Logger g, int evtId, int pct,
			Class<T> configurationClass,
			OabaProcessingPhase oabaPhase
			) {
		if (n == null || n.isEmpty() || g == null || oabaPhase == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		if (!OabaTestProcedures.isValidConfigurationClass(configurationClass)) {
			throw new IllegalArgumentException("invalid configuration class: " + configurationClass);
		}
		this.sourceName = n;
		this.logger = g;
		this.eventId = evtId;
		this.percentComplete = pct;
		this.configurationClass = configurationClass;
		this.oabaPhase = oabaPhase;
	}
	
	// -- Abstract methods

	@Override
	public abstract Queue getResultQueue();

	@Override
	public abstract boolean isWorkingDirectoryCorrectAfterLinkageProcessing();

	@Override
	public abstract boolean isWorkingDirectoryCorrectAfterDeduplicationProcessing() ;

	// -- Template methods

	@Before
	@Override
	public final void setUp() {
		final String METHOD = "setUp";
		getLogger().entering(getSourceName(), METHOD);
		setupOK = true;
		try {
			int initialValue;

			initialValue = getTestController().findAllOabaParameters().size();
			setInitialOabaParamsCount(initialValue);

			initialValue = getTestController().findAllOabaJobs().size();
			setInitialOabaJobCount(initialValue);

			initialValue = getTestController().findAllOabaProcessing().size();
			setInitialOabaProcessingCount(initialValue);

		} catch (Exception x) {
			getLogger().severe(x.toString());
			setupOK = false;
		}
		getLogger().exiting(getSourceName(), METHOD);
	}

	@After
	@Override
	public final void tearDown() {
		String METHOD = "tearDown";
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
	@Override
	public final void testPrequisites() {
		assertTrue(isSetupOK());

		assertTrue(getBatchQuery() != null);
		assertTrue(getBlockQueue() != null);
		assertTrue(getChunkQueue() != null);
		assertTrue(getDedupQueue() != null);
		assertTrue(getEm() != null);
		assertTrue(getJmsContext() != null);
		assertTrue(getJobController() != null);
		assertTrue(getLogger() != null);
		assertTrue(getMatchDedupQueue() != null);
		assertTrue(getMatchSchedulerQueue() != null);
		assertTrue(getParamsController() != null);
		assertTrue(getProcessingController() != null);
		assertTrue(getServerController() != null);
		assertTrue(getSettingsController() != null);
		assertTrue(getSingleMatchQueue() != null);
		assertTrue(getSourceName() != null);
		assertTrue(getStartQueue() != null);
		assertTrue(getTestController() != null);
		assertTrue(getUpdateQueue() != null);
		assertTrue(getUtx() != null);
	}

	@Test
	@InSequence(2)
	@Override
	public final void clearQueues() {
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
	@InSequence(3)
	@Override
	public final void testLinkage() throws ServerConfigurationException {
		OabaTestProcedures.testLinkageProcessing(this);
	}
	
	@Test
	@InSequence(4)
	@Override
	public final void testDeduplication() throws ServerConfigurationException {
		OabaTestProcedures.testDeduplicationProcessing(this);
	}
	
	// -- Modifiers
	
	@Override
	public final void setInitialOabaJobCount(int value) {
		this.initialOabaJobCount = value;
	}

	@Override
	public final void setInitialOabaParamsCount(int value) {
		this.initialOabaParamsCount = value;
	}

	@Override
	public final void setInitialOabaProcessingCount(int value) {
		this.initialOabaProcessingCount = value;
	}

	@Override
	public final void setSetupOK(boolean setupOK) {
		this.setupOK = setupOK;
	}

	// -- Accessors

	@Override
	public final int getInitialOabaJobCount() {
		return initialOabaJobCount;
	}

	@Override
	public final int getInitialOabaParamsCount() {
		return initialOabaParamsCount;
	}

	@Override
	public final int getInitialOabaProcessingCount() {
		return initialOabaProcessingCount;
	}

	@Override
	public final boolean isSetupOK() {
		return setupOK;
	}

	@Override
	public final Class<T> getTestConfigurationClass() {
		return configurationClass;
	}

	@Override
	public final T getTestConfiguration() {
		if (testConfiguration == null) {
			Class<T> c =
				getTestConfigurationClass();
			CMPluginRegistry r = getE2service().getPluginRegistry();
			testConfiguration =
				OabaTestProcedures.createTestConfiguration(c, r);
		}
		assert testConfiguration != null;
		return testConfiguration;
	}

	@Override
	public final OabaProcessingPhase getOabaProcessingPhase() {
		return oabaPhase;
	}

	@Override
	public final EjbPlatform getE2service() {
		return this.e2service;
	}

	@Override
	public final OabaService getBatchQuery() {
		return batchQuery;
	}

	@Override
	public final Queue getBlockQueue() {
		return blockQueue;
	}

	@Override
	public final Queue getChunkQueue() {
		return chunkQueue;
	}

	@Override
	public final Queue getDedupQueue() {
		return dedupQueue;
	}

	@Override
	public final EntityManager getEm() {
		return em;
	}

	@Override
	public final JMSContext getJmsContext() {
		return jmsContext;
	}

	@Override
	public final OabaJobControllerBean getJobController() {
		return jobController;
	}

	@Override
	public final Logger getLogger() {
		return logger;
	}

	@Override
	public final Queue getMatchDedupQueue() {
		return matchDedupQueue;
	}

	@Override
	public final Queue getMatchSchedulerQueue() {
		return matchSchedulerQueue;
	}

	@Override
	public final OabaParametersControllerBean getParamsController() {
		return paramsController;
	}

	@Override
	public final OabaProcessingControllerBean getProcessingController() {
		return processingController;
	}

	@Override
	public final PersistableRecordSourceController getRecordSourceController() {
		return rsController;
	}

	@Override
	public final int getResultEventId() {
		return eventId;
	}

	@Override
	public final int getResultPercentComplete() {
		return percentComplete;
	}

	@Override
	public final ServerConfigurationController getServerController() {
		return serverController;
	}

	@Override
	public final OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	@Override
	public final Queue getSingleMatchQueue() {
		return singleMatchQueue;
	}

	@Override
	public final String getSourceName() {
		return sourceName;
	}

	@Override
	public final Queue getStartQueue() {
		return startQueue;
	}

	@Override
	public final OabaTestController getTestController() {
		return oabaTestController;
	}

	@Override
	public final Queue getUpdateQueue() {
		return updateQueue;
	}

	@Override
	public final UserTransaction getUtx() {
		return utx;
	}

}
