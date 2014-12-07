/*
 * GroupMatchType.java       Revision: 2.5  Date: Sep 9, 2005 2:53:25 PM 
 *
 * Copyright (c) 2001 ChoiceMaker Technologies, Inc.
 * 48 Wall Street, 11th Floor, New York, NY 10005
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ChoiceMaker Technologies Inc. ("Confidential Information").
 */
package com.choicemaker.cm.urm.config;

import java.io.ObjectStreamException;

/**
 * A type that defines how the result of the batch analysis will be organized
 * for output:
 * <ul>
 * <li>XML - as xml file,
 * <li>H3L as a list of triplets &lt; i,j,k> where i is a connected (by hold or
 * match) record set ID, j is a linked record set ID, k is a record ID.
 * <li>R3L as a list of triplets &lt; j,j,k> where i is a record ID, j is a
 * linked record set ID, k is a connected record set ID.
 * </ul>
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 12:00:14 PM
 */
public class AnalysisResultFormat implements java.io.Serializable {

	private static final long serialVersionUID = 271L;

	private String value;

	/**
	 * Constructs a <code>ClrFormatType</code>
	 * <p>
	 * 
	 * @param value
	 */
	public AnalysisResultFormat(String value) {
		super();
		this.value = value;
	}

	public static final AnalysisResultFormat XML = new AnalysisResultFormat(
			"XML");
	public static final AnalysisResultFormat SORT_BY_HOLD_GROUP =
		new AnalysisResultFormat("SORT_BY_HOLD_GROUP"); // pre-order (prefix
														// traversal) a node is
														// visited before
														// children
	public static final AnalysisResultFormat SORT_BY_RECORD_ID =
		new AnalysisResultFormat("SORT_BY_RECORD_ID");// post-order (postfix
														// traversal) a node is
														// visited after
														// children

	public String toString() {
		return value;
	}

	public String getFileExtension() {
		if (this.equals(XML)) {
			return "XML";
		} else if (this.equals(SORT_BY_HOLD_GROUP)) {
			return "H3L";
		} else if (this.equals(SORT_BY_RECORD_ID)) {
			return "R3L";
		} else {
			return "unknown";
		}
	}

	public String getDisplayName() {
		if (this.equals(XML)) {
			return "XML";
		} else if (this.equals(SORT_BY_HOLD_GROUP)) {
			return "SORT_BY_HOLD_GROUP";
		} else if (this.equals(SORT_BY_RECORD_ID)) {
			return "SORT_BY_RECORD_ID";
		} else {
			return "unknown";
		}
	}

	public static AnalysisResultFormat valueOf(String name) {
		name = name.intern();
		if (XML.value.equals(name)) {
			return XML;
		} else if (SORT_BY_HOLD_GROUP.value.equals(name)) {
			return SORT_BY_HOLD_GROUP;
		} else if (SORT_BY_RECORD_ID.value.equals(name)) {
			return SORT_BY_RECORD_ID;
		} else {
			throw new IllegalArgumentException(name
					+ " is not a valid AnalysisResultFormat.");
		}
	}

	private static int nextOrdinal = 0;
	private final int ordinal = nextOrdinal++;
	private static final AnalysisResultFormat[] VALUES = {
			XML, SORT_BY_HOLD_GROUP, SORT_BY_RECORD_ID };

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean retVal = false;
		if (obj != null && obj instanceof AnalysisResultFormat) {
			AnalysisResultFormat arf1 = this;
			AnalysisResultFormat arf2 = (AnalysisResultFormat) obj;
			retVal = arf1.value.equals(arf2.value);
		}
		return retVal;
	}

}
