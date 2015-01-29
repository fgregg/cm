package com.choicemaker.cmit;

import static org.junit.Assert.assertTrue;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

public abstract class AbstractTransitivityMdbTest<T extends WellKnownTestConfiguration> {

	// -- Read-write instance data

	/**
	 * A read-only member that is lazily initialized by
	 * {@link #getTestConfiguration()}
	 */
	private T testConfiguration;

	/** Initialized during {@link #setUp()} */
	private TestEntityCounts te;

	// -- Immutable, constructed instance data

	private final String sourceName;

	private final Logger logger;

	private final int eventId;

	private final float percentComplete;

	private final Class<T> configurationClass;

	private final OabaProcessingPhase oabaPhase;

	private JMSConsumer transStatusConsumer;

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
	private OabaProcessingController processingController;

	@EJB
	private OabaService oabaService;

	@EJB
	private TransitivityService transitivityService;

	@EJB
	private TransitivityTestController oabaTestController;

	@EJB
	private OperationalPropertyController opPropController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private RecordSourceController rsController;

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

	@Resource(lookup = "java:/choicemaker/urm/jms/transStatusTopic")
	private Topic transStatusTopic;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue transitivityQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Constructor

	@Inject
	public AbstractTransitivityMdbTest(String n, Logger g, int evtId,
			float pct, Class<T> configurationClass,
			OabaProcessingPhase oabaPhase) {
		if (n == null || n.isEmpty() || g == null || oabaPhase == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		if (!TransitivityMdbTestProcedures
				.isValidConfigurationClass(configurationClass)) {
			throw new IllegalArgumentException("invalid configuration class: "
					+ configurationClass);
		}
		this.sourceName = n;
		this.logger = g;
		this.eventId = evtId;
		this.percentComplete = pct;
		this.configurationClass = configurationClass;
		this.oabaPhase = oabaPhase;
	}

	// -- Abstract methods

	public abstract Queue getResultQueue();

	public abstract boolean isWorkingDirectoryCorrectAfterProcessing(
			OabaLinkageType linkage, OabaJob batchJob, OabaParameters bp,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration);

	// -- Template methods

	public void checkCounts() throws AssertionError {
		// FIXME STUBBED
//		TestEntityCounts te = getTestEntityCounts();
//		te.checkCounts(getLogger(), getEm(), getUtx(), getJobController(),
//				getParamsController(), getSettingsController(),
//				getServerController(), getProcessingController(),
//				getOpPropController(), getRecordSourceController(),
//				getRecordIdController());
		// FIXME STUBBED
	}

	@Before
	public final void setUp() throws Exception {
		final String METHOD = "setUp";
		getLogger().entering(getSourceName(), METHOD);

		getLogger().info("Creating an OABA status listener");
		this.transStatusConsumer =
			getJmsContext().createConsumer(getTransitivityStatusTopic());

		// FIXME STUBBED
		te = null;
//			new TestEntityCounts(getLogger(), getJobController(),
//					getParamsController(), getSettingsController(),
//					getServerController(), getProcessingController(),
//					getOpPropController(), getRecordSourceController(),
//					getRecordIdController());
		// END FIXME
		setTestEntityCounts(te);
		getLogger().exiting(getSourceName(), METHOD);
	}

	@After
	public final void tearDown() {
		String METHOD = "tearDown";
		getLogger().entering(getSourceName(), METHOD);

		getLogger().info("Closing the Transitivity status listener");
		this.getTransitivityStatusConsumer().close();
	}

	@Test
	@InSequence(1)
	public final void testPrequisites() {
		assertTrue(getTransitivityService() != null);
		assertTrue(getBlockQueue() != null);
		assertTrue(getChunkQueue() != null);
		assertTrue(getDedupQueue() != null);
		assertTrue(getEm() != null);
		assertTrue(getJmsContext() != null);
		assertTrue(getJobController() != null);
		assertTrue(getLogger() != null);
		assertTrue(getMatchDedupQueue() != null);
		assertTrue(getMatchSchedulerQueue() != null);
		assertTrue(getTransitivityStatusTopic() != null);
		assertTrue(getSingleMatchQueue() != null);
		assertTrue(getSourceName() != null);
		assertTrue(getStartQueue() != null);
		assertTrue(getTestController() != null);
		assertTrue(getUtx() != null);

		assertTrue(getTransitivityQueue() != null);

		// These assertions are redundant because these controllers are
		// required during setUp()
		assertTrue(getParamsController() != null);
		assertTrue(getProcessingController() != null);
		assertTrue(getServerController() != null);
		assertTrue(getSettingsController() != null);
	}

	@Test
	@InSequence(2)
	public final void clearDestinations() {

		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getStartQueue());
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getBlockQueue());
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

		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getTransitivityQueue());

		JmsUtils.clearOabaNotifications(getSourceName(),
				getTransitivityStatusConsumer());
	}

	@Test
	@InSequence(3)
	public final void testLinkageTransitivity()
			throws ServerConfigurationException {
		// FIXME
		// TransitivityMdbTestProcedures.testLinkageTransitivity(this);
	}

	@Test
	@InSequence(4)
	public final void testDeduplicationTransitivity()
			throws ServerConfigurationException {
		// FIXME
		// TransitivityMdbTestProcedures.testDeduplicationTransitivity(this);
	}

	// -- Modifiers

	protected void setTestEntityCounts(TestEntityCounts te) {
		this.te = te;
	}

	// -- Accessors

	public final Class<T> getTestConfigurationClass() {
		return configurationClass;
	}

	public final T getTestConfiguration(OabaLinkageType linkage) {
		if (testConfiguration == null) {
			Class<T> c = getTestConfigurationClass();
			CMPluginRegistry r = getE2service().getPluginRegistry();
			testConfiguration =
				TransitivityMdbTestProcedures.createTestConfiguration(c,
						linkage, r);
		}
		assert testConfiguration != null;
		return testConfiguration;
	}

	public final OabaProcessingPhase getOabaProcessingPhase() {
		return oabaPhase;
	}

	public final EjbPlatform getE2service() {
		return this.e2service;
	}

	public final OabaService getOabaService() {
		return oabaService;
	}

	public final TransitivityService getTransitivityService() {
		return transitivityService;
	}

	public final Queue getBlockQueue() {
		return blockQueue;
	}

	public final Queue getChunkQueue() {
		return chunkQueue;
	}

	public final Queue getDedupQueue() {
		return dedupQueue;
	}

	public final EntityManager getEm() {
		return em;
	}

	public final JMSContext getJmsContext() {
		return jmsContext;
	}

	public final OabaJobControllerBean getJobController() {
		return jobController;
	}

	public final Logger getLogger() {
		return logger;
	}

	public final Queue getMatchDedupQueue() {
		return matchDedupQueue;
	}

	public final Queue getMatchSchedulerQueue() {
		return matchSchedulerQueue;
	}

	public final JMSConsumer getTransitivityStatusConsumer() {
		return transStatusConsumer;
	}

	public final OperationalPropertyController getOpPropController() {
		return opPropController;
	}

	public final RecordIdController getRecordIdController() {
		return ridController;
	}

	public final OabaParametersControllerBean getParamsController() {
		return paramsController;
	}

	public final OabaProcessingController getProcessingController() {
		return processingController;
	}

	public final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	public final int getResultEventId() {
		return eventId;
	}

	public final float getResultPercentComplete() {
		return percentComplete;
	}

	public final ServerConfigurationController getServerController() {
		return serverController;
	}

	public final OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	public final Queue getSingleMatchQueue() {
		return singleMatchQueue;
	}

	public final String getSourceName() {
		return sourceName;
	}

	public final Queue getStartQueue() {
		return startQueue;
	}

	public final TransitivityTestController getTestController() {
		return oabaTestController;
	}

	public TestEntityCounts getTestEntityCounts() {
		return te;
	}

	public final Queue getTransitivityQueue() {
		return transitivityQueue;
	}

	public final Topic getTransitivityStatusTopic() {
		return transStatusTopic;
	}

	public final UserTransaction getUtx() {
		return utx;
	}

}
