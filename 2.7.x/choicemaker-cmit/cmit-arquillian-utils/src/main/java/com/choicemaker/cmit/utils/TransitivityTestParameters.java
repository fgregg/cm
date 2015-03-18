package com.choicemaker.cmit.utils;

import javax.jms.JMSConsumer;
import javax.jms.Queue;

import com.choicemaker.cm.batch.ProcessingController;

public interface TransitivityTestParameters extends OabaTestParameters {

	ProcessingController getTransitivityProcessingController();

	JMSConsumer getTransitivityStatusConsumer();

	Queue getTransMatchSchedulerQueue();

	Queue getTransMatchDedupQueue();

//	AnalysisResultFormat getAnalysisResultFormat();
//
//	String getGraphPropertyName();

}
