package com.choicemaker.cmit.oaba;

import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cmit.oaba.util.OabaTestController;
import com.choicemaker.cmit.utils.OabaProcessingPhase;
import com.choicemaker.cmit.utils.WellKnownTestConfiguration;
import com.choicemaker.e2.ejb.EjbPlatform;

public interface OabaProcessingTest<T extends WellKnownTestConfiguration> {

	// -- JUnit tests and helpers

	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * &#064;Before
	 * public void setUp() {
	 * }
	 * </pre>
	 */
	void setUp();

	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * &#064;After
	 * public void tearDown() {
	 * }
	 * </pre>
	 */
	void tearDown();

	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * 	&#064;Test
	 * 	&#064;InSequence(1)
	 * }
	 * </pre>
	 */
	void testPrequisites();

	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * 	&#064;Test
	 * 	&#064;InSequence(2)
	 * }
	 * </pre>
	 */
	void clearQueues();
	
	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * 	&#064;Test
	 * 	&#064;InSequence(3)
	 * }
	 * </pre>
	 */
	void testStartLinkage() throws ServerConfigurationException;

	/**
	 * Call back from {@link
	 * com.choicemaker.cmit.oaba.delegated_design.OabaTestProcedures#
	 * testLinkageProcessing(OabaProcessingTest0<T>, OabaProcessingPhase)}
	 */
	boolean isWorkingDirectoryCorrectAfterLinkageProcessing();

	/**
	 * Implementations should annotate this method as
	 * 
	 * <pre>
	 * 	&#064;Test
	 * 	&#064;InSequence(3)
	 * }
	 * </pre>
	 */
	void testStartDeduplication() throws ServerConfigurationException;

	/**
	 * Call back from {@link
	 * com.choicemaker.cmit.oaba.delegated_design.OabaTestProcedures#
	 * testDeduplicationProcessing(OabaProcessingTest0<T>, OabaProcessingPhase)}
	 */
	boolean isWorkingDirectoryCorrectAfterDeduplicationProcessing();
	
	// -- Read-write instance data

	int getInitialOabaParamsCount();

	void setInitialOabaParamsCount(int value);

	int getInitialOabaJobCount();

	void setInitialOabaJobCount(int value);

	int getInitialOabaProcessingCount();

	void setInitialOabaProcessingCount(int value);

	boolean isSetupOK();

	void setSetupOK(boolean setupOK);

	// -- Read-only instance data

	String getSourceName();

	Logger getLogger();

	int getResultEventId();

	int getResultPercentComplete();

	Class<T> getTestConfigurationClass();

	T getTestConfiguration();
	
	OabaProcessingPhase getOabaProcessingPhase();

	Queue getResultQueue();

	EjbPlatform getE2service();

	UserTransaction getUtx();

	EntityManager getEm();

	OabaJobControllerBean getJobController();

	OabaParametersControllerBean getParamsController();

	SettingsController getSettingsController();

	ServerConfigurationController getServerController();

	OabaProcessingControllerBean getProcessingController();

	OabaService getBatchQuery();

	OabaTestController getTestController();

	PersistableRecordSourceController getRecordSourceController();

	Queue getBlockQueue();

	Queue getChunkQueue();

	Queue getDedupQueue();

	Queue getMatchDedupQueue();

	Queue getMatchSchedulerQueue();

	Queue getSingleMatchQueue();

	Queue getStartQueue();

	Queue getUpdateQueue();

	JMSContext getJmsContext();

}
