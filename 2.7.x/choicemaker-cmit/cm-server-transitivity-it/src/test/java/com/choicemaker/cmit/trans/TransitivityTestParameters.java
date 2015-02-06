package com.choicemaker.cmit.trans;

import javax.jms.JMSConsumer;

import com.choicemaker.cmit.utils.OabaTestParameters;

public interface TransitivityTestParameters extends OabaTestParameters {

	JMSConsumer getTransitivityStatusConsumer();

}
