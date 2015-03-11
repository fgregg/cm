package com.choicemaker.cm.args;

public class BatchProcessingEvent implements ProcessingEvent, BatchProcessing {

	// -- Well-known instances

	public static final BatchProcessingEvent INIT = new BatchProcessingEvent(
			NAME_INIT, EVT_INIT, PCT_INIT);

	public static final BatchProcessingEvent DONE = new BatchProcessingEvent(
			NAME_DONE, EVT_DONE, PCT_DONE);
	
	// -- Instance data

	private final String name;
	private final int eventId;
	private final float percentComplete;

	// -- Constructor

	public BatchProcessingEvent(String name, int id, float estimate) {
		if (name == null || !name.equals(name.trim()) || name.isEmpty()) {
			throw new IllegalArgumentException("invalid name: '" + name + "'");
		}
		if (!name.equals(name.toUpperCase())) {
			throw new IllegalArgumentException("name must be upper case: '"
					+ name + "'");
		}
		if (Float.isNaN(estimate) || estimate < MINIMUM_FRACTION_COMPLETE
				|| estimate > MAXIMUM_FRACTION_COMPLETE) {
			throw new IllegalArgumentException("invalid estimate: " + estimate);
		}
		this.name = name;
		this.eventId = id;
		this.percentComplete = estimate;
	}

	// -- Accessors

	@Override
	public final String getEventName() {
		return name;
	}

	@Override
	public final int getEventId() {
		return eventId;
	}

	@Override
	public final float getPercentComplete() {
		return percentComplete;
	}

	// -- Identity

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + eventId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(percentComplete);
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BatchProcessingEvent)) {
			return false;
		}
		BatchProcessingEvent other = (BatchProcessingEvent) obj;
		if (eventId != other.eventId) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (Float.floatToIntBits(percentComplete) != Float
				.floatToIntBits(other.percentComplete)) {
			return false;
		}
		return true;
	}

	@Override
	public final String toString() {
		return "BatchProcessingEvent [name=" + name + ", eventId=" + eventId
				+ ", percentComplete=" + percentComplete + "]";
	}

}
