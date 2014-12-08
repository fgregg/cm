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
 * The 2.5 implementation of {@link RecordType} as a pseudo enum.
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 2:09:30 PM
 */
public class RecordType0 implements Serializable {

	private static final long serialVersionUID = 271L;

	public static final RecordType0 NONE = new RecordType0("NONE");
	public static final RecordType0 HOLDER = new RecordType0("HOLDER");
	public static final RecordType0 REF = new RecordType0("REF");
	public static final RecordType0 GLOBAL_REF = new RecordType0("GLOBAL_REF");

	public static RecordType0 valueOf(String name) {
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
	private static final RecordType0[] VALUES = {
			NONE, HOLDER, REF, GLOBAL_REF };

	private final String name;

	private RecordType0(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

}
