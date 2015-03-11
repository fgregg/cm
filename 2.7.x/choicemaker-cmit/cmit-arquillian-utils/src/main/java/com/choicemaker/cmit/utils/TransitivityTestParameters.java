package com.choicemaker.cmit.utils;

import javax.jms.JMSConsumer;

import com.choicemaker.cm.batch.ProcessingController;

public interface TransitivityTestParameters extends OabaTestParameters {

	ProcessingController getTransitivityProcessingController();

	JMSConsumer getTransitivityStatusConsumer();

//	AnalysisResultFormat getAnalysisResultFormat();
//
//	String getGraphPropertyName();

}
