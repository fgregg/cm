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
 * A type of the match score
 * <ul>
 * <li>NO_NOTE - match score note member will not assigned.</li>
 * <li>RULE_LIST_NOTE match score note member will contain names of the fired
 * clues or notes. Only clues and notes marked in the model clue file by the
 * <code>note</code> modifier will be included into the note.</li>
 * </ul>
 * <p>
 *
 * @author emoussikaev
 * @version Revision: 2.5 Date: Nov 1, 2005 1:46:30 PM
 */
public class ScoreType implements java.io.Serializable {

	private static final long serialVersionUID = 271L;

	private String value;

	private ScoreType(String value) {
		this.value = value;
	}

	public static final ScoreType NO_NOTE = new ScoreType("NO_NOTE");
	public static final ScoreType RULE_LIST_NOTE = new ScoreType(
			"RULE_LIST_NOTE");

	public String toString() {
		return value;
	}

	public static ScoreType valueOf(String name) {
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
	private static final ScoreType[] VALUES = {
			NO_NOTE, RULE_LIST_NOTE };

	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	}

}
