/*
 * ScoreNoteType.java       Revision: 2.5  Date: Sep 9, 2005 3:25:10 PM 
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
 * The 2.5 implementation of {@link ScoreType} as a pseudo enum.
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 1:46:30 PM
 */
public class ScoreType0 implements java.io.Serializable {

	private static final long serialVersionUID = 271L;

	public static final ScoreType0 NO_NOTE = new ScoreType0("NO_NOTE");
	public static final ScoreType0 RULE_LIST_NOTE = new ScoreType0(
			"RULE_LIST_NOTE");

	public static ScoreType0 valueOf(String name) {
		name = name.intern();
		if (NO_NOTE.toString().intern() == name) {
			return NO_NOTE;
		} else if (RULE_LIST_NOTE.toString().intern() == name) {
			return RULE_LIST_NOTE;
		} else {
			throw new IllegalArgumentException(name
					+ " is not a valid Record Score.");
		}
	}

	private static int nextOrdinal = 0;
	private final int ordinal = nextOrdinal++;
	private static final ScoreType0[] VALUES = {
			NO_NOTE, RULE_LIST_NOTE };

	private String value;

	private ScoreType0(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

}
