/*
 * Copyright (c) 2001, 2009 ChoiceMaker Technologies, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     ChoiceMaker Technologies, Inc. - initial API and implementation
 */
package com.choicemaker.cm.urm.base;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A type of the match score
 * <ul>
 * <li>NO_NOTE  - match score note member will not assigned.</li>
 * <li>RULE_LIST_NOTE - match score note member will contain names of the fired clues or notes.
 * Only clues and notes marked in the model clue file by the <code>note</code>
 * modifier will be included into the note.</li>
 * </ul> 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 1:46:30 PM
 * @see
 */
public class ScoreType implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -848761881733981928L;

	private String value;

	private ScoreType(String value) {
		this.value = value;
	}

	public static final ScoreType NO_NOTE = new ScoreType("NO_NOTE");
	public static final ScoreType RULE_LIST_NOTE = new ScoreType("RULE_LIST_NOTE");
	
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
			throw new IllegalArgumentException(
				name + " is not a valid Decision3.");
		}
	}
	
	private static int 	nextOrdinal = 0;
	private final int 	ordinal = nextOrdinal++;
	private static final ScoreType[] VALUES = {NO_NOTE,RULE_LIST_NOTE};
	
	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	} 

}
