package com.choicemaker.cmit.utils.j2ee;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.e2.ejb.EjbPlatform;

public interface OabaTestParameters {

	Queue getBlockQueue();

	Queue getChunkQueue();

	Queue getDedupQueue();

	EjbPlatform getE2service();

	EntityManager getEm();

	JMSContext getJmsContext();

	OabaLinkageType getLinkageType();

	Logger getLogger();

	Queue getMatchDedupQueue();

	Queue getMatchSchedulerQueue();

	OabaJobController getOabaJobController();

	OabaParametersController getOabaParamsController();

	ProcessingController getOabaProcessingController();

	OabaService getOabaService();

	JMSConsumer getOabaStatusConsumer();

	Topic getOabaStatusTopic();

	OperationalPropertyController getOpPropController();

	BatchProcessingPhase getProcessingPhase();

	RecordIdController getRecordIdController();

	RecordSourceController getRecordSourceController();

	int getResultEventId();

	float getResultPercentComplete();

	Queue getResultQueue();

	ServerConfigurationController getServerController();

	OabaSettingsController getSettingsController();

	Queue getSingleMatchQueue();

	String getSourceName();

	Queue getStartQueue();

	WellKnownTestConfiguration getTestConfiguration();

	TestEntityCounts getTestEntityCounts();

	Queue getTransitivityQueue();

	UserTransaction getUtx();

}