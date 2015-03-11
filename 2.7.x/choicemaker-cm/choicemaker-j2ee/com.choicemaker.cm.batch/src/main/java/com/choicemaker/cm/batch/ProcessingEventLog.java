package com.choicemaker.cm.batch;

import com.choicemaker.cm.args.ProcessingEvent;


public interface ProcessingEventLog {

	/** This methods gets the most recent processing event */
	ProcessingEvent getCurrentProcessingEvent();

	/** This methods gets the id of the most recent processing event */
	int getCurrentProcessingEventId();

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentProcessingEvent(ProcessingEvent event);

	/**
	 * This method sets the current processing event with additional info.
	 */
	void setCurrentProcessingEvent(ProcessingEvent event, String info);

	/**
	 * This method gets the additional info associated with the most recent
	 * processing event.
	 */
	String getCurrentProcessingEventInfo();

}