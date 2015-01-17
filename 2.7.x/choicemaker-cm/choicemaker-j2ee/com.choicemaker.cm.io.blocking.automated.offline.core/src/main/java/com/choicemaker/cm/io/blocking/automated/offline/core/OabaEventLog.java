package com.choicemaker.cm.io.blocking.automated.offline.core;

public interface OabaEventLog {

	/** This methods gets the most recent processing event */
	OabaEvent getCurrentOabaEvent();

	/** This methods gets the id of the most recent processing event */
	int getCurrentOabaEventId();

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentOabaEvent(OabaEvent event);

	/**
	 * This method sets the current processing event with additional info.
	 */
	void setCurrentOabaEvent(OabaEvent event, String info);

	/**
	 * This method gets the additional info associated with the most recent
	 * processing event.
	 */
	String getCurrentOabaEventInfo();

}