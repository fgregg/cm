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
 * The 2.5 implementation of {@link AnalysisResultFormat} as a pseudo enum.
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 12:00:14 PM
 */
public class AnalysisResultFormat0 implements java.io.Serializable {

	private static final long serialVersionUID = 271L;

	public static final AnalysisResultFormat0 XML = new AnalysisResultFormat0(
			"XML");
	public static final AnalysisResultFormat0 SORT_BY_HOLD_GROUP =
		new AnalysisResultFormat0("SORT_BY_HOLD_GROUP"); // pre-order (prefix
														// traversal) a node is
														// visited before
														// children
	public static final AnalysisResultFormat0 SORT_BY_RECORD_ID =
		new AnalysisResultFormat0("SORT_BY_RECORD_ID");// post-order (postfix
														// traversal) a node is
														// visited after
														// children

	public static AnalysisResultFormat0 valueOf(String name) {
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
	private static final AnalysisResultFormat0[] VALUES = {
			XML, SORT_BY_HOLD_GROUP, SORT_BY_RECORD_ID };

	private String value;

	/**
	 * Constructs a <code>ClrFormatType</code>
	 * <p>
	 * 
	 * @param value
	 */
	public AnalysisResultFormat0(String value) {
		super();
		this.value = value;
	}

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
		if (obj != null && obj instanceof AnalysisResultFormat0) {
			AnalysisResultFormat0 arf1 = this;
			AnalysisResultFormat0 arf2 = (AnalysisResultFormat0) obj;
			retVal = arf1.value.equals(arf2.value);
		}
		return retVal;
	}

}
