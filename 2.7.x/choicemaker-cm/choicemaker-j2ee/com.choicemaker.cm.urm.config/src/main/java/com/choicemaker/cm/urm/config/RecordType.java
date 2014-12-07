/*
 * RecordType.java       Revision: 2.5  Date: Sep 9, 2005 3:53:13 PM 
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
import java.io.Serializable;

/**
 * A type of the record object
 * <ul>
 * <li>NONE no data</li>
 * <li>HOLDER record holder object</li>
 * <li>REF record reference object</li>
 * <li>GLOBAL_REF record global reference object</li>
 * </ul>
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 2:09:30 PM
 */
public class RecordType implements Serializable {

	private static final long serialVersionUID = 271L;

	private String value;

	public RecordType(String value) {
		this.value = value;
	}

	public static final RecordType NONE = new RecordType("NONE");
	public static final RecordType HOLDER = new RecordType("HOLDER");
	public static final RecordType REF = new RecordType("REF");
	public static final RecordType GLOBAL_REF = new RecordType("GLOBAL_REF");

	public String toString() {
		return value;
	}

	public static RecordType valueOf(String name) {
		name = name.intern();
		if (NONE.toString().intern() == name) {
			return NONE;
		} else if (HOLDER.toString().intern() == name) {
			return HOLDER;
		} else if (REF.toString().intern() == name) {
			return REF;
		} else if (GLOBAL_REF.toString().intern() == name) {
			return GLOBAL_REF;
		} else {
			throw new IllegalArgumentException(name
					+ " is not a valid RecordType.");
		}
	}

	private static int nextOrdinal = 0;
	private final int ordinal = nextOrdinal++;
	private static final RecordType[] VALUES = {
			NONE, HOLDER, REF, GLOBAL_REF };

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

}
