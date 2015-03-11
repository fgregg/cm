package com.choicemaker.cm.args;

public interface ProcessingEvent {
	String getEventName();
	int getEventId();
	float getPercentComplete();
}