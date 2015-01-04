package com.choicemaker.cm.batch;

import java.io.Serializable;
import java.util.Date;

public interface BatchProcessingEvent extends Serializable {

	/** Default id value for non-persistent batch jobs */
	public static final long INVALID_ID = 0;

	/** Default event name */
	public static final String INVALID_EVENT_NAME = null;

	/** Default event sequence number */
	public static final int INVALID_EVENT_SEQNUM = -1;

	/** Default event information */
	public static final String DEFAULT_EVENT_INFO = null;

	/** Default event timestamp */
	public static final Date INVALID_TIMESTAMP = new Date(0L);

	/** Minimum estimate of the amount of processing completed (inclusive) */
	public static final float MINIMUM_FRACTION_COMPLETE = 0.0f;

	/** Default estimate of the amount of processing completed */
	public static final float DEFAULT_FRACTION_COMPLETE =
		MINIMUM_FRACTION_COMPLETE;

	/** Maximum estimate of the amount of processing completed (inclusive) */
	public static final float MAXIMUM_FRACTION_COMPLETE = 1.0f;

	/** Returns the persistence identifier for this entry */
	long getId();

	/** Returns the identifier of the batch job to which this entry applies */
	long getJobId();

	/** Returns the event type of this entry */
	String getEventType();

	/** Returns the event name for this entry */
	String getEventName();

	/** Returns the event sequence number for this entry */
	int getEventSequenceNumber();

	/** Returns optional, additional information about this event (may be null) */
	String getEventInfo();

	/** Returns the event timestamp for this entry */
	Date getEventTimestamp();

	/** Returns an estimate between 0.0 and 1.0 (inclusive) */
	float getFractionComplete();

}