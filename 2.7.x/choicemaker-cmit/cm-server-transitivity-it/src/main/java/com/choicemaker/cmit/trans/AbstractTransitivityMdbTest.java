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
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;
import com.choicemaker.cmit.trans.util.TransitivityMdbTestProcedures;
import com.choicemaker.cmit.utils.j2ee.BatchProcessingPhase;
import com.choicemaker.cmit.utils.j2ee.JmsUtils;
import com.choicemaker.cmit.utils.j2ee.TestEntityCounts;
import com.choicemaker.cmit.utils.j2ee.TransitivityTestParameters;
import com.choicemaker.cmit.utils.j2ee.WellKnownTestConfiguration;
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

	private final BatchProcessingPhase oabaPhase;

	private JMSConsumer oabaStatusConsumer;

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

	@EJB(beanName = "OabaProcessingControllerBean")
	private ProcessingController oabaProcessingController;

	@EJB(beanName = "TransitivityProcessingControllerBean")
	private ProcessingController transProcessingController;

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

	@Resource(lookup = "java:/choicemaker/urm/jms/transStatusTopic")
	private Topic transStatusTopic;

	@Resource(lookup = "java:/choicemaker/urm/jms/singleMatchQueue")
	private Queue singleMatchQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/startQueue")
	private Queue startQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue transitivityQueue;

	@Resource(lookup = "choicemaker/urm/jms/transMatchSchedulerQueue")
	private Queue transMatchSchedulerQueue;

	@Resource(lookup = "choicemaker/urm/jms/transMatchDedupQueue")
	private Queue transMatchDedupQueue;

	@Resource(lookup = "choicemaker/urm/jms/transSerializationQueue")
	private Queue transSerializationQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Constructor

	@Inject
	public AbstractTransitivityMdbTest(String n, Logger g, int evtId,
			float pct, Class<T> configurationClass,
			BatchProcessingPhase oabaPhase) {
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

	// @Override
	public abstract Queue getResultQueue();

	public abstract boolean isWorkingDirectoryCorrectAfterProcessing(
			BatchJob transJob);

	// -- Template methods

	public void checkCounts() throws AssertionError {
		TestEntityCounts te = getTestEntityCounts();
		te.checkCounts(getLogger(), getEm(), getUtx(), getOabaJobController(),
				getOabaParamsController(), getTransJobController(),
				getTransParamsController(), getSettingsController(),
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

		getLogger().info("Creating an Transitivity status listener");
		this.transStatusConsumer =
			getJmsContext().createConsumer(getTransitivityStatusTopic());

		this.te =
			new TestEntityCounts(getLogger(), getOabaJobController(),
					getOabaParamsController(), getTransJobController(),
					getTransParamsController(), getSettingsController(),
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

		getLogger().info("Closing the Transitivity status listener");
		try {
			JMSConsumer listener = this.getTransitivityStatusConsumer();
			listener.close();
		} catch (Exception x) {
			getLogger().warning(x.toString());
		}
	}

	@Test
	@InSequence(1)
	public final void checkPrequisites() {
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
		assertTrue(getOabaStatusTopic() != null);
		assertTrue(getSingleMatchQueue() != null);
		assertTrue(getSourceName() != null);
		assertTrue(getStartQueue() != null);
		assertTrue(getUtx() != null);

		assertTrue(getTransitivityQueue() != null);
		assertTrue(getTransitivityStatusTopic() != null);
		assertTrue(getTransMatchSchedulerQueue() != null);

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
		JmsUtils.clearStartDataFromQueue(getSourceName(), getJmsContext(),
				getTransMatchSchedulerQueue());

		JmsUtils.clearBatchProcessingNotifications(getSourceName(),
				this.getOabaStatusConsumer());
		JmsUtils.clearBatchProcessingNotifications(getSourceName(),
				getTransitivityStatusConsumer());
	}

	@Test
	@InSequence(3)
	public final void testLinkageTransitivity() throws Exception {
		OabaLinkageType task = OabaLinkageType.STAGING_TO_MASTER_LINKAGE;
		TransitivityMdbTestProcedures.testTransitivityProcessing(this, task);
	}

	@Test
	@InSequence(4)
	public final void testDeduplicationTransitivity() throws Exception {
		OabaLinkageType task = OabaLinkageType.STAGING_DEDUPLICATION;
		TransitivityMdbTestProcedures.testTransitivityProcessing(this, task);
	}

	// -- Modifiers

	protected void setTestEntityCounts(TestEntityCounts te) {
		this.te = te;
	}

	// -- Pseudo accessors

	public final TransitivityTestParameters getTestParameters(
			OabaLinkageType linkage) {
		return new TestParametersDelegate(this, linkage);
	}

	// @Override
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

	// -- Accessors

	protected final Class<T> getTestConfigurationClass() {
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

	public final TransitivityService getTransitivityService() {
		return transService;
	}

	public final TransitivityJobController getTransJobController() {
		return transJobController;
	}

	public final TransitivityParametersController getTransParamsController() {
		return transParamsController;
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

	protected final Logger getLogger() {
		return logger;
	}

	protected final Queue getMatchDedupQueue() {
		return matchDedupQueue;
	}

	protected final Queue getMatchSchedulerQueue() {
		return matchSchedulerQueue;
	}

	protected final JMSConsumer getTransitivityStatusConsumer() {
		return transStatusConsumer;
	}

	protected final JMSConsumer getOabaStatusConsumer() {
		return oabaStatusConsumer;
	}

	protected final Topic getOabaStatusTopic() {
		return oabaStatusTopic;
	}

	public OabaParametersController getOabaParamsController() {
		return oabaParamsController;
	}

	protected final OperationalPropertyController getOpPropController() {
		return opPropController;
	}

	protected final RecordIdController getRecordIdController() {
		return ridController;
	}

	protected final ProcessingController getOabaProcessingController() {
		return oabaProcessingController;
	}

	protected final ProcessingController getTransitivityProcessingController() {
		return transProcessingController;
	}

	protected final RecordSourceController getRecordSourceController() {
		return rsController;
	}

	public final int getResultEventId() {
		return eventId;
	}

	public final float getResultPercentComplete() {
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

	public TestEntityCounts getTestEntityCounts() {
		return te;
	}

	protected final Queue getTransitivityQueue() {
		return transitivityQueue;
	}

	protected final Topic getTransitivityStatusTopic() {
		return transStatusTopic;
	}

	protected final Queue getTransMatchSchedulerQueue() {
		return transMatchSchedulerQueue;
	}

	protected final Queue getTransMatchDedupQueue() {
		return transMatchDedupQueue;
	}

	protected final Queue getTransSerializationQueue() {
		return transSerializationQueue;
	}

	protected final UserTransaction getUtx() {
		return utx;
	}

	protected class TestParametersDelegate implements
			TransitivityTestParameters {
		private final AbstractTransitivityMdbTest<T> d;
		private final OabaLinkageType lt;

		TestParametersDelegate(AbstractTransitivityMdbTest<T> delegate,
				OabaLinkageType linkageType) {
			assert delegate != null;
			assert linkageType != null;
			this.d = delegate;
			this.lt = linkageType;
		}

		@Override
		public OabaLinkageType getLinkageType() {
			return lt;
		}

		@Override
		public Queue getResultQueue() {
			return d.getResultQueue();
		}

		@Override
		public String toString() {
			return d.toString();
		}

		@Override
		public final T getTestConfiguration() {
			OabaLinkageType _lt = this.getLinkageType();
			return d.getTestConfiguration(_lt);
		}

		@Override
		public final BatchProcessingPhase getProcessingPhase() {
			return d.getProcessingPhase();
		}

		@Override
		public final EjbPlatform getE2service() {
			return d.getE2service();
		}

		@Override
		public final OabaService getOabaService() {
			return d.getOabaService();
		}

		@Override
		public final Queue getBlockQueue() {
			return d.getBlockQueue();
		}

		@Override
		public final Queue getChunkQueue() {
			return d.getChunkQueue();
		}

		@Override
		public final Queue getDedupQueue() {
			return d.getDedupQueue();
		}

		@Override
		public final EntityManager getEm() {
			return d.getEm();
		}

		@Override
		public final JMSContext getJmsContext() {
			return d.getJmsContext();
		}

		@Override
		public final OabaJobController getOabaJobController() {
			return d.getOabaJobController();
		}

		@Override
		public final Logger getLogger() {
			return d.getLogger();
		}

		@Override
		public final Queue getMatchDedupQueue() {
			return d.getMatchDedupQueue();
		}

		@Override
		public final Queue getMatchSchedulerQueue() {
			return d.getMatchSchedulerQueue();
		}

		@Override
		public final JMSConsumer getTransitivityStatusConsumer() {
			return d.getTransitivityStatusConsumer();
		}

		@Override
		public final JMSConsumer getOabaStatusConsumer() {
			return d.getOabaStatusConsumer();
		}

		@Override
		public final Topic getOabaStatusTopic() {
			return d.getOabaStatusTopic();
		}

		@Override
		public OabaParametersController getOabaParamsController() {
			return d.getOabaParamsController();
		}

		@Override
		public final OperationalPropertyController getOpPropController() {
			return d.getOpPropController();
		}

		@Override
		public final RecordIdController getRecordIdController() {
			return d.getRecordIdController();
		}

		@Override
		public final ProcessingController getOabaProcessingController() {
			return d.getOabaProcessingController();
		}

		@Override
		public final ProcessingController getTransitivityProcessingController() {
			return d.getTransitivityProcessingController();
		}

		@Override
		public final RecordSourceController getRecordSourceController() {
			return d.getRecordSourceController();
		}

		@Override
		public final int getResultEventId() {
			return d.getResultEventId();
		}

		@Override
		public final float getResultPercentComplete() {
			return d.getResultPercentComplete();
		}

		@Override
		public final ServerConfigurationController getServerController() {
			return d.getServerController();
		}

		@Override
		public final OabaSettingsController getSettingsController() {
			return d.getSettingsController();
		}

		@Override
		public final Queue getSingleMatchQueue() {
			return d.getSingleMatchQueue();
		}

		@Override
		public final String getSourceName() {
			return d.getSourceName();
		}

		@Override
		public final Queue getStartQueue() {
			return d.getStartQueue();
		}

		@Override
		public TestEntityCounts getTestEntityCounts() {
			return d.getTestEntityCounts();
		}

		@Override
		public final Queue getTransitivityQueue() {
			return d.getTransitivityQueue();
		}

		@Override
		public final Queue getTransMatchSchedulerQueue() {
			return d.getTransMatchSchedulerQueue();
		}

		@Override
		public final Queue getTransMatchDedupQueue() {
			return d.getTransMatchDedupQueue();
		}

		@Override
		public final Queue getTransSerializationQueue() {
			return d.getTransSerializationQueue();
		}

		@Override
		public final UserTransaction getUtx() {
			return d.getUtx();
		}
	}

}
