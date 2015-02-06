package com.choicemaker.cm.io.blocking.automated.offline.core;

public interface ProcessingStatus {

	/** This methods gets the most recent processing event */
	OabaEvent getCurrentProcessingStatus();

	/** This methods gets the id of the most recent processing event */
	int getCurrentProcessingStatusId2();

	/**
	 * This method gets the additional info associated with the most recent
	 * processing event.
	 */
	String getCurrentProcessingStatusInfo();

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentOabaEvent(OabaEvent event);

	/**
	 * This method sets the current processing event with null additional info.
	 */
	void setCurrentProcessingStatusId(int eventId);

	/**
	 * This method sets the current processing event with additional info.
	 */
	void setCurrentOabaEvent(OabaEvent event, String info);

	/**
	 * This method sets the current processing event with additional info.
	 */
	void setCurrentProcessingStatusInfo(int eventId, String info);

}