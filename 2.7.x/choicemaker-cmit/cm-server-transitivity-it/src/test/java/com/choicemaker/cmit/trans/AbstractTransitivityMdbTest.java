package com.choicemaker.cmit.trans;

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
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cmit.trans.util.TransitivityMdbTestProcedures;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

/**
 * A template for integration testing of Transitivity message-driven beans.
 * 
 * @author rphall
 *
 * @param <T>
 *            A Well-Known Test Configuration
 */
public abstract class AbstractTransitivityMdbTest<T extends WellKnownTestConfiguration>
		implements TransitivityTestParameters {

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
	private TransitivityService transService;

	@EJB
	private TransitivityJobController transJobController;

	@EJB
	private TransitivityParametersController transParamsController;

	@EJB
	private OabaService oabaService;

	@EJB
	private OabaJobController oabaJobController;

	@EJB
	private OabaParametersController oabaParamsController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OabaProcessingController processingController;

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

	@Override
	public abstract Queue getResultQueue();

	public abstract boolean isWorkingDirectoryCorrectAfterProcessing(
			OabaLinkageType linkage, OabaJob batchJob, OabaParameters bp,
			OabaSettings oabaSettings, ServerConfiguration serverConfiguration);

	// -- Template methods

	public void checkCounts() throws AssertionError {
		TestEntityCounts te = getTestEntityCounts();
		te.checkCounts(getLogger(), getEm(), getUtx(), getOabaJobController(),
				getOabaParamsController(), getTransJobController(),
				getTransParamsController(), getSettingsController(),
				getServerController(), getProcessingController(),
				getOpPropController(), getRecordSourceController(),
				getRecordIdController());
	}

	@Before
	public final void setUp() throws Exception {
		final String METHOD = "setUp";
		getLogger().entering(getSourceName(), METHOD);

		getLogger().info("Creating an Transitivity status listener");
		this.transStatusConsumer =
			getJmsContext().createConsumer(getTransitivityStatusTopic());

		te = null;
		new TestEntityCounts(getLogger(), getOabaJobController(),
				getOabaParamsController(), getTransJobController(),
				getTransParamsController(), getSettingsController(),
				getServerController(), getProcessingController(),
				getOpPropController(), getRecordSourceController(),
				getRecordIdController());
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
		assertTrue(getOabaJobController() != null);
		assertTrue(getLogger() != null);
		assertTrue(getMatchDedupQueue() != null);
		assertTrue(getMatchSchedulerQueue() != null);
		assertTrue(getTransitivityStatusTopic() != null);
		assertTrue(getSingleMatchQueue() != null);
		assertTrue(getSourceName() != null);
		assertTrue(getStartQueue() != null);
		assertTrue(getUtx() != null);

		assertTrue(getTransitivityQueue() != null);

		// These assertions are redundant because these controllers are
		// required during setUp()
		assertTrue(getOabaParamsController() != null);
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
//		String TEST = "testLinkageTransitivity";
//		final String externalId = EntityManagerUtils.createExternalId(TEST);
//		OabaJob oabaJob =
//			OabaTestUtils.startOabaJob(OabaLinkageType.STAGING_DEDUPLICATION,
//					TEST, this, externalId);
//		TransitivityMdbTestProcedures.testTransitivityProcessing(this, oabaJob);
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

	@Override
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

	@Override
	public final OabaProcessingPhase getOabaProcessingPhase() {
		return oabaPhase;
	}

	@Override
	public final EjbPlatform getE2service() {
		return this.e2service;
	}

	@Override
	public final OabaService getOabaService() {
		return oabaService;
	}

	public final TransitivityService getTransitivityService() {
		return transService;
	}

	public final TransitivityJobController getTransJobController() {
		return transJobController;
	}

	public final TransitivityParametersController getTransParamsController() {
		return transParamsController;
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
	public final OabaJobController getOabaJobController() {
		return oabaJobController;
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
	public final JMSConsumer getTransitivityStatusConsumer() {
		return transStatusConsumer;
	}

	@Override
	public JMSConsumer getOabaStatusConsumer() {
		// FIXME TODO Auto-generated method stub
		return null;
	}

	@Override
	public Topic getOabaStatusTopic() {
		// FIXME TODO Auto-generated method stub
		return null;
	}

	@Override
	public OabaParametersController getParamsController() {
		// FIXME TODO Auto-generated method stub
		return null;
	}

	@Override
	public final OperationalPropertyController getOpPropController() {
		return opPropController;
	}

	@Override
	public final RecordIdController getRecordIdController() {
		return ridController;
	}

	public final OabaParametersController getOabaParamsController() {
		return oabaParamsController;
	}

	@Override
	public final OabaProcessingController getProcessingController() {
		return processingController;
	}

	@Override
	public final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	@Override
	public final int getResultEventId() {
		return eventId;
	}

	@Override
	public final float getResultPercentComplete() {
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
	public TestEntityCounts getTestEntityCounts() {
		return te;
	}

	@Override
	public final Queue getTransitivityQueue() {
		return transitivityQueue;
	}

	public final Topic getTransitivityStatusTopic() {
		return transStatusTopic;
	}

	@Override
	public final UserTransaction getUtx() {
		return utx;
	}

}
