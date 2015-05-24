package com.choicemaker.cm.args;

import java.io.Serializable;

public interface ProcessingEvent extends Serializable {
	String getEventName();
	int getEventId();
	float getPercentComplete();
}