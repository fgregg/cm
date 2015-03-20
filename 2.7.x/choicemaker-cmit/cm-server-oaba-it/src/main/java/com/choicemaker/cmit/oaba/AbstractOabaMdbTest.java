package com.choicemaker.cmit.oaba;

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
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cmit.oaba.util.OabaMdbTestProcedures;
import com.choicemaker.cmit.utils.BatchProcessingPhase;
import com.choicemaker.cmit.utils.JmsUtils;
import com.choicemaker.cmit.utils.OabaTestParameters;
import com.choicemaker.cmit.utils.TestEntityCounts;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.CMPluginRegistry;
import com.choicemaker.e2.ejb.EjbPlatform;

/**
 * A template for integration testing of OABA message-driven beans.
 * 
 * @author rphall
 *
 * @param <T>
 *            A Well-Known Test Configuration
 */
public abstract class AbstractOabaMdbTest<T extends WellKnownTestConfiguration>
		/* implements OabaTestParameters */ {

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

	private final BatchProcessingPhase oabaPhase;

	private JMSConsumer oabaStatusConsumer;

	// -- Read-only, injected instance data

	@EJB
	private EjbPlatform e2service;

	@Resource
	private UserTransaction utx;

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

	@EJB(beanName = "OabaProcessingControllerBean")
	private ProcessingController processingController;

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

	@Resource(lookup = "java:/choicemaker/urm/jms/statusTopic")
	private Topic oabaStatusTopic;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue transitivityQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Constructor

	public AbstractOabaMdbTest(String n, Logger g, int evtId, float pct,
			Class<T> configurationClass, BatchProcessingPhase oabaPhase) {
		if (n == null || n.isEmpty() || g == null || oabaPhase == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		if (!OabaMdbTestProcedures
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
			BatchJob batchJob);

	// -- Template methods

	public void checkCounts() throws AssertionError {
		TestEntityCounts te = getTestEntityCounts();
		te.checkCounts(getLogger(), getEm(), getUtx(), getOabaJobController(),
				getOabaParamsController(), getSettingsController(),
				getServerController(), getOabaProcessingController(),
				getOpPropController(), getRecordSourceController(),
				getRecordIdController());
	}

	@Before
	public final void setUp() throws Exception {
		final String METHOD = "setUp";
		getLogger().entering(getSourceName(), METHOD);

		getLogger().info("Creating an OABA status listener");
		this.oabaStatusConsumer =
			getJmsContext().createConsumer(getOabaStatusTopic());

		TestEntityCounts te =
			new TestEntityCounts(getLogger(), getOabaJobController(),
					getOabaParamsController(), getSettingsController(),
					getServerController(), getOabaProcessingController(),
					getOpPropController(), getRecordSourceController(),
					getRecordIdController());
		setTestEntityCounts(te);
		getLogger().exiting(getSourceName(), METHOD);
	}

	@After
	public final void tearDown() {
		String METHOD = "tearDown";
		getLogger().entering(getSourceName(), METHOD);

		getLogger().info("Closing the OABA status listener");
		try {
			JMSConsumer listener = this.getOabaStatusConsumer();
			listener.close();
		} catch (Exception x) {
			getLogger().warning(x.toString());
		}
	}

	@Test
	@InSequence(1)
	public final void checkPrequisites() {
		assertTrue(getOabaService() != null);
		assertTrue(getBlockQueue() != null);
		assertTrue(getChunkQueue() != null);
		assertTrue(getDedupQueue() != null);
		assertTrue(getEm() != null);
		assertTrue(getJmsContext() != null);
		assertTrue(getOabaJobController() != null);
		assertTrue(getLogger() != null);
		assertTrue(getMatchDedupQueue() != null);
		assertTrue(getMatchSchedulerQueue() != null);
		assertTrue(getOabaStatusTopic() != null);
		assertTrue(getSingleMatchQueue() != null);
		assertTrue(getSourceName() != null);
		assertTrue(getStartQueue() != null);
		assertTrue(getUtx() != null);

		assertTrue(getTransitivityQueue() != null);

		// These assertions are redundant because these controllers are
		// required during setUp()
		assertTrue(getOabaParamsController() != null);
		assertTrue(getOabaProcessingController() != null);
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

		JmsUtils.clearBatchProcessingNotifications(getSourceName(),
				getOabaStatusConsumer());
	}

	@Test
	@InSequence(3)
	public final void testLinkage() throws ServerConfigurationException {
		OabaMdbTestProcedures.testLinkageProcessing(this);
	}

	@Test
	@InSequence(4)
	public final void testDeduplication() throws ServerConfigurationException {
		OabaMdbTestProcedures.testDeduplicationProcessing(this);
	}

	// -- Modifiers

	protected void setTestEntityCounts(TestEntityCounts te) {
		this.te = te;
	}

	// -- Pseudo accessors

	public final T getTestConfiguration(OabaLinkageType type) {
		if (testConfiguration == null) {
			Class<T> c = getTestConfigurationClass();
			CMPluginRegistry r = getE2service().getPluginRegistry();
			testConfiguration =
				OabaMdbTestProcedures.createTestConfiguration(c, type, r);
		}
		assert testConfiguration != null;
		return testConfiguration;
	}

	public OabaTestParameters getTestParameters(OabaLinkageType linkage) {
		return new TestParametersDelegate(linkage);
	}

	// -- Accessors

	public final Class<T> getTestConfigurationClass() {
		return configurationClass;
	}

	protected final BatchProcessingPhase getProcessingPhase() {
		return oabaPhase;
	}

	protected final EjbPlatform getE2service() {
		return this.e2service;
	}

	protected final OabaService getOabaService() {
		return oabaService;
	}

	protected final Queue getBlockQueue() {
		return blockQueue;
	}

	protected final Queue getChunkQueue() {
		return chunkQueue;
	}

	protected final Queue getDedupQueue() {
		return dedupQueue;
	}

	protected final EntityManager getEm() {
		return em;
	}

	protected final JMSContext getJmsContext() {
		return jmsContext;
	}

	protected final OabaJobController getOabaJobController() {
		return oabaJobController;
	}

	public final Logger getLogger() {
		return logger;
	}

	protected final Queue getMatchDedupQueue() {
		return matchDedupQueue;
	}

	protected final Queue getMatchSchedulerQueue() {
		return matchSchedulerQueue;
	}

	protected final JMSConsumer getOabaStatusConsumer() {
		return oabaStatusConsumer;
	}

	protected final Topic getOabaStatusTopic() {
		return oabaStatusTopic;
	}

	protected final OperationalPropertyController getOpPropController() {
		return opPropController;
	}

	protected final RecordIdController getRecordIdController() {
		return ridController;
	}

	protected final OabaParametersController getOabaParamsController() {
		return oabaParamsController;
	}

	protected final ProcessingController getOabaProcessingController() {
		return processingController;
	}

	protected final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	protected final int getResultEventId() {
		return eventId;
	}

	protected final float getResultPercentComplete() {
		return percentComplete;
	}

	protected final ServerConfigurationController getServerController() {
		return serverController;
	}

	protected final OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	protected final Queue getSingleMatchQueue() {
		return singleMatchQueue;
	}

	public final String getSourceName() {
		return sourceName;
	}

	protected final Queue getStartQueue() {
		return startQueue;
	}

	protected TestEntityCounts getTestEntityCounts() {
		return te;
	}

	protected final Queue getTransitivityQueue() {
		return transitivityQueue;
	}

	protected final UserTransaction getUtx() {
		return utx;
	}

	protected class TestParametersDelegate implements OabaTestParameters {
		private final OabaLinkageType lt;
		TestParametersDelegate(OabaLinkageType linkageType) {
			assert linkageType != null;
			this.lt = linkageType;
		}
		@Override
		public final WellKnownTestConfiguration getTestConfiguration() {
			OabaLinkageType _lt = this.getLinkageType();
			return AbstractOabaMdbTest.this.getTestConfiguration(_lt);
		}
		@Override
		public OabaLinkageType getLinkageType() {
			return lt;
		}
		@Override
		public Queue getResultQueue() {
			return AbstractOabaMdbTest.this.getResultQueue();
		}
		@Override
		public String toString() {
			return AbstractOabaMdbTest.this.toString();
		}
		@Override
		public final BatchProcessingPhase getProcessingPhase() {
			return AbstractOabaMdbTest.this.getProcessingPhase();
		}
		@Override
		public final EjbPlatform getE2service() {
			return AbstractOabaMdbTest.this.getE2service();
		}
		@Override
		public final OabaService getOabaService() {
			return AbstractOabaMdbTest.this.getOabaService();
		}
		@Override
		public final Queue getBlockQueue() {
			return AbstractOabaMdbTest.this.getBlockQueue();
		}
		@Override
		public final Queue getChunkQueue() {
			return AbstractOabaMdbTest.this.getChunkQueue();
		}
		@Override
		public final Queue getDedupQueue() {
			return AbstractOabaMdbTest.this.getDedupQueue();
		}
		@Override
		public final EntityManager getEm() {
			return AbstractOabaMdbTest.this.getEm();
		}
		@Override
		public final JMSContext getJmsContext() {
			return AbstractOabaMdbTest.this.getJmsContext();
		}
		@Override
		public final OabaJobController getOabaJobController() {
			return AbstractOabaMdbTest.this.getOabaJobController();
		}
		@Override
		public final Logger getLogger() {
			return AbstractOabaMdbTest.this.getLogger();
		}
		@Override
		public final Queue getMatchDedupQueue() {
			return AbstractOabaMdbTest.this.getMatchDedupQueue();
		}
		@Override
		public final Queue getMatchSchedulerQueue() {
			return AbstractOabaMdbTest.this.getMatchSchedulerQueue();
		}
		@Override
		public final JMSConsumer getOabaStatusConsumer() {
			return AbstractOabaMdbTest.this.getOabaStatusConsumer();
		}
		@Override
		public final Topic getOabaStatusTopic() {
			return AbstractOabaMdbTest.this.getOabaStatusTopic();
		}
		@Override
		public final OperationalPropertyController getOpPropController() {
			return AbstractOabaMdbTest.this.getOpPropController();
		}
		@Override
		public final RecordIdController getRecordIdController() {
			return AbstractOabaMdbTest.this.getRecordIdController();
		}
		@Override
		public final OabaParametersController getOabaParamsController() {
			return AbstractOabaMdbTest.this.getOabaParamsController();
		}
		@Override
		public final ProcessingController getOabaProcessingController() {
			return AbstractOabaMdbTest.this.getOabaProcessingController();
		}
		@Override
		public final RecordSourceController getRecordSourceController() {
			return AbstractOabaMdbTest.this.getRecordSourceController();
		}
		@Override
		public final int getResultEventId() {
			return AbstractOabaMdbTest.this.getResultEventId();
		}
		@Override
		public final float getResultPercentComplete() {
			return AbstractOabaMdbTest.this.getResultPercentComplete();
		}
		@Override
		public final ServerConfigurationController getServerController() {
			return AbstractOabaMdbTest.this.getServerController();
		}
		@Override
		public final OabaSettingsController getSettingsController() {
			return AbstractOabaMdbTest.this.getSettingsController();
		}
		@Override
		public final Queue getSingleMatchQueue() {
			return AbstractOabaMdbTest.this.getSingleMatchQueue();
		}
		@Override
		public final String getSourceName() {
			return AbstractOabaMdbTest.this.getSourceName();
		}
		@Override
		public final Queue getStartQueue() {
			return AbstractOabaMdbTest.this.getStartQueue();
		}
		@Override
		public TestEntityCounts getTestEntityCounts() {
			return AbstractOabaMdbTest.this.getTestEntityCounts();
		}
		@Override
		public final Queue getTransitivityQueue() {
			return AbstractOabaMdbTest.this.getTransitivityQueue();
		}
		@Override
		public final UserTransaction getUtx() {
			return AbstractOabaMdbTest.this.getUtx();
		}
	}

}
