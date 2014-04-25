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

/**
 * Typesafe enum for pseud-decisions on record pairs. Pseudo-decisions
 * are used for rules only.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:35:15 $
 */
public class ExtDecision extends Decision {

	private static final long serialVersionUID = 1L;

	/** The number of decisions. */
	public final static int NUM_DECISIONS = 7;

	private static Decision[] vals = new Decision[NUM_DECISIONS];

	/** The nodiffer pseudo-decision. */
	public static final ExtDecision NODIFFER = new ExtDecision("nodiffer", "C", 3);

	/** The nomatch pseudo-decision. */
	public static final ExtDecision NOMATCH = new ExtDecision("nomatch", "L", 4);

	/** The nohold pseudo-decision. */
	public static final ExtDecision NOHOLD = new ExtDecision("nohold", "G", 5);

	/** The none pseudo-decision. */
	public static final ExtDecision NONE = new ExtDecision("none", "N", 6);

	private ExtDecision(String name, String singleChar, int no) {
		super(name, singleChar, no);
    if (0<= no && no < NUM_DECISIONS) {
		  vals[no] = this;
    }
	}

	/**
	 * Returns the corresponding <code>Decision</code>.
	 * 
	 * @return  The corresponding <code>Decision</code>.
	 * @throws  IllegalArgumentException if <code>name</code> is not a valid decision.
	 */
	public static Decision valueOf(String name) {
		name = name.intern();
		if ("nodiffer" == name) {
			return NODIFFER;
		} else if ("nohold" == name) {
			return NOHOLD;
		} else if ("nomatch" == name) {
			return NOMATCH;
		} else if ("none" == name) {
			return NONE;
		} else {
			return Decision.valueOf(name);
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
		if (name == 'c') {
			return NODIFFER;
		} else if (name == 'g') {
			return NOHOLD;
		} else if (name == 'l') {
			return NOMATCH;
		} else if (name == 'n') {
			return NONE;
		} else {
			return Decision.valueOf(name);
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
		return no < Decision.NUM_DECISIONS ? Decision.valueOf(no) : vals[no];
	}

	private Object readResolve() throws ObjectStreamException {
		return valueOf(toInt());
	}

	/** 
	 * Compares this object with the specified object for order, where
	 * quite arbitrarily<ul>
	 * <li/> NONE &lt; NOHOLD &lt; NOMATCH &lt; NODIFFER</ul>
	 * and necessarily<ul>
	 * <li/>NODIFFER &lt; MATCH &lt; HOLD &lt; DIFFER.</ul>
	 *
	 * @return   A negative integer, zero, or a positive integer as this object is less than,
	 *           equal to, or greater than the specified object.
	 */
	public int compareTo(Object o) {
		ExtDecision e = (ExtDecision) o;
		int retVal;
		if (e instanceof Decision) {
			retVal = -1;
		} else if (this == e) {
				retVal = 0;
		} else {
			int thisOrdinal = NUM_DECISIONS - this.toInt();
			int thatOrdinal = NUM_DECISIONS - e.toInt();
			if (thisOrdinal < thatOrdinal) {
				retVal = 1;
			} else {
				retVal = -1;
			}
		}
		return retVal;
	}

}

