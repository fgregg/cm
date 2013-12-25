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

import java.text.DecimalFormat;

public class NullFloat implements Comparable {
	private final static NullFloat nullInstance = new NullFloat();
	private static DecimalFormat df = new DecimalFormat("##0.00");

	private final float val;
	private final boolean nul;
	private final String rPadding;

	public static NullFloat getNullInstance() {
		return nullInstance;
	}

	public NullFloat(float val) {
		this(val, "");
	}

	public NullFloat(float val, String rPadding) {
		this.val = val;
		this.nul = false;
		this.rPadding = rPadding;
	}

	private NullFloat() {
		this.val = 0;
		this.nul = true;
		this.rPadding = "";
	}

	public float value() {
		if (nul) {
			throw new IllegalStateException("Value of nul instance");
		} else {
			return val;
		}
	}

	public int compareTo(Object o) {
		NullFloat other = (NullFloat) o;
		if (nul) {
			if (other.nul) {
				return 0;
			} else {
				return -1;
			}
		} else if (other.nul) {
			return 1;
		} else {
			float thisVal = this.val;
			float anotherVal = other.val;
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}
	}

	public boolean equals(Object o) {
		NullFloat other = (NullFloat) o;
		return nul && other.nul || val == other.val;
	}

	public String toString() {
		return nul ? "" : df.format(val) + rPadding;
	}
}
