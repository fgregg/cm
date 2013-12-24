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
package com.choicemaker.cm.core;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Typesafe enum for decisions on record pairs.
 * Decisions are also known as futures.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:33:06 $
 */

public class Decision implements Serializable, Comparable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -2193844678908298448L;

	/** The number of different decisions. */
	public final static int NUM_DECISIONS = 3;

	private static Decision[] vals = new Decision[NUM_DECISIONS];

	/** The decision differ. */
	public static final Decision DIFFER = new Decision("differ", "D", 0);

	/** The decision match. */
	public static final Decision MATCH = new Decision("match", "M", 1);

	/** The decision hold. */
	public static final Decision HOLD = new Decision("hold", "H", 2);

	private final transient String name;
	private final int no;
	private final transient String singleChar;

	protected Decision(String name, String singleChar, int no) {
		this.name = name;
		this.singleChar = singleChar;
		this.no = no;
		if (0 <= no && no < NUM_DECISIONS)
			vals[no] = this;
	}

	public String toString() {
		return name;
	}

	/**
	 * Returns the single character representation of the
	 * <code>Decision</code>. Returns "D", "M", or "H".
	 *
	 * @return  the single character representation of the
	 * <code>Decision</code>.
	 */
	public String toSingleCharString() {
		return singleChar;
	}

	/**
	 * Returns the corresponding <code>Decision</code>.
	 * 
	 * @return  The corresponding <code>Decision</code>.
	 * @throws  IllegalArgumentException if <code>name</code> is not a valid decision.
	 */
	public static Decision valueOf(String name) {
		name = name.intern();
		if ("differ" == name) {
			return DIFFER;
		} else if ("hold" == name) {
			return HOLD;
		} else if ("match" == name) {
			return MATCH;
		} else {
			throw new IllegalArgumentException(name + " is not a valid Decision.");
		}
	}

	/**
	 * Returns the corresponding <code>Decision</code>.
	 * 
	 * @return  The corresponding <code>Decision</code>.
	 * @throws  IllegalArgumentException if <code>name</code> is not a valid decision.
	 */
	public static Decision valueOf(char name) {
		name = Character.toLowerCase(name);
		if (name == 'd') {
			return DIFFER;
		} else if (name == 'h') {
			return HOLD;
		} else if (name == 'm') {
			return MATCH;
		} else {
			throw new IllegalArgumentException(name + " is not a valid Decision.");
		}
	}

	/**
	 * Returns the corresponding <code>Decision</code>.
	 * 
	 * @return  The corresponding <code>Decision</code>.
	 * @throws  IndexOutOfBoundsException if <code>no</code> is out of the range
	 *            <code>(no < 0 || no >= NUM_DECISIONS)</code>.
	 */
	public static Decision valueOf(int no) {
		return vals[no];
	}

	/**
	 * Returns the <code>int</code> value corresponding to this decision.
	 *
	 * @return The <code>int</code> value corresponding to this decision.
	 */
	public int toInt() {
		return no;
	}

	/** 
	 * Compares this object with the specified object for order, where MATCH &lt; HOLD &lt; DIFFER.
	 *
	 * @return   A negative integer, zero, or a positive integer as this object is less than,
	 *           equal to, or greater than the specified object.
	 */
	public int compareTo(Object o) {
		Decision d = (Decision) o;
		if (this == d) {
			return 0;
		} else if (this == DIFFER || (this == HOLD && d == MATCH)) {
			return 1;
		} else {
			return -1;
		}
	}

	private Object readResolve() throws ObjectStreamException {
		return valueOf(no);
	}
	
	public boolean equals(Object o) {
		Decision d = (Decision) o;
		boolean retVal = this.no == d.no;
		return retVal;
	}
	
	public int hashCode() {
		return this.no;
	}

}


