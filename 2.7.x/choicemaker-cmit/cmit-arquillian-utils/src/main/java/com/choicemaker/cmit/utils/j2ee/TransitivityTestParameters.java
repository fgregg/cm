package com.choicemaker.cmit.utils.j2ee;

import javax.jms.JMSConsumer;
import javax.jms.Queue;

import com.choicemaker.cm.batch.ProcessingController;

public interface TransitivityTestParameters extends OabaTestParameters {

	ProcessingController getTransitivityProcessingController();

	JMSConsumer getTransitivityStatusConsumer();

	Queue getTransMatchSchedulerQueue();

	Queue getTransMatchDedupQueue();

	Queue getTransSerializationQueue();

//	AnalysisResultFormat getAnalysisResultFormat();
//
//	String getGraphPropertyName();

}
