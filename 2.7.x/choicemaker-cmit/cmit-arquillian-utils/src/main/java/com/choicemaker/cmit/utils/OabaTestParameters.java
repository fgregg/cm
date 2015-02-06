package com.choicemaker.cmit.utils;

import java.util.logging.Logger;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import com.choicemaker.cm.args.OabaLinkageType;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaService;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.e2.ejb.EjbPlatform;

public interface OabaTestParameters {

	Queue getResultQueue();

	WellKnownTestConfiguration getTestConfiguration(OabaLinkageType type);

	OabaProcessingPhase getOabaProcessingPhase();

	EjbPlatform getE2service();

	OabaService getOabaService();

	Queue getBlockQueue();

	Queue getChunkQueue();

	Queue getDedupQueue();

	EntityManager getEm();

	JMSContext getJmsContext();

	OabaJobController getJobController();

	Logger getLogger();

	Queue getMatchDedupQueue();

	Queue getMatchSchedulerQueue();

	JMSConsumer getOabaStatusConsumer();

	Topic getOabaStatusTopic();

	OperationalPropertyController getOpPropController();

	RecordIdController getRecordIdController();

	OabaParametersController getParamsController();

	OabaProcessingController getProcessingController();

	RecordSourceController getRecordSourceController();

	int getResultEventId();

	float getResultPercentComplete();

	ServerConfigurationController getServerController();

	OabaSettingsController getSettingsController();

	Queue getSingleMatchQueue();

	String getSourceName();

	Queue getStartQueue();

	TestEntityCounts getTestEntityCounts();

	Queue getTransitivityQueue();

	UserTransaction getUtx();

}