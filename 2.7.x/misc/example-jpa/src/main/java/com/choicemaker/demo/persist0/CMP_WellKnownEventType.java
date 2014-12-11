package com.choicemaker.demo.persist0;

/**
 * An non-exclusive enumeration of pre-defined types of audit events.
 * Applications may extend this enumeration with application-specific audit
 * types.
 * 
 * @author rphall
 *
 */
public enum CMP_WellKnownEventType {
	STATUS;

	// -- String representation

	/** Returns an upper-case name of the event type */
	public String toString() {
		String retVal = this.name();
		// fromString(String) depends on upper case
		assert retVal.equals(retVal.trim().toUpperCase());
		return retVal;
	}

	/** Returns an upper-case name of the event type */
	public static final String toString(CMP_WellKnownEventType status) {
		String retVal = status == null ? null : status.toString();
		return retVal;
	}

	/** Converts an upper-case name to an event type, if possible */
	public static final CMP_WellKnownEventType fromString(String s) {
		CMP_WellKnownEventType retVal = null;
		if (s != null) {
			retVal = CMP_WellKnownEventType.valueOf(s.trim().toUpperCase());
		}
		return retVal;
	}

}