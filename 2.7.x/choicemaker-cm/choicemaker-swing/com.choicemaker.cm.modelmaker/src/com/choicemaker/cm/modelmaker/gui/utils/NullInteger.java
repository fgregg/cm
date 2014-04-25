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
package com.choicemaker.cm.modelmaker.gui.utils;

public class NullInteger implements Comparable {
	private final static NullInteger nullInstance = new NullInteger();

	private final int val;
	private final boolean nul;
	private final String rPadding;

	public static NullInteger getNullInstance() {
		return nullInstance;
	}

	public NullInteger(int val) {
		this(val, "");
	}

	public NullInteger(int val, String rPadding) {
		this.val = val;
		this.nul = false;
		this.rPadding = rPadding;
	}

	private NullInteger() {
		this.val = 0;
		this.nul = true;
		this.rPadding = "";
	}

	public int value() {
		if (nul) {
			throw new IllegalStateException("Value of nul instance");
		} else {
			return val;
		}
	}

	public int compareTo(Object o) {
		NullInteger other = (NullInteger) o;
		if (nul) {
			if (other.nul) {
				return 0;
			} else {
				return -1;
			}
		} else if (other.nul) {
			return 1;
		} else {
			int thisVal = this.val;
			int anotherVal = other.val;
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + val;
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NullInteger other = (NullInteger) obj;
		if (val != other.val)
			return false;
		return true;
	}
	
	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00(Object o) {
		NullInteger other = (NullInteger) o;
		return nul && other.nul || val == other.val;
	}

	public String toString() {
		return nul ? "" : String.valueOf(val) + rPadding;
	}
}
