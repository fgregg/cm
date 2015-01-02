package com.choicemaker.cm.transitivity.core;

import com.choicemaker.cm.transitivity.core.TransitivityProcessing.TransitivityEvent;

public interface TransitivityProcessingLog {

	/** This methods gets the most recent processing event */
	TransitivityEvent getCurrentTransitivityEvent();

	/** This methods gets the id of the most recent processing event */
	//	@Deprecated
	int getCurrentTransitivityEventId();

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentTransitivityProcessingEvent(TransitivityEvent event);

	/**
	 * This method sets the current processing event with additional info.
	 */
	void setCurrentTransitivityProcessingEvent(TransitivityEvent stat, String info);

	/**
	 * This method gets the additional info associated with the most recent
	 * processing event.
	 */
	String getCurrentTransitivityEventInfo();

}