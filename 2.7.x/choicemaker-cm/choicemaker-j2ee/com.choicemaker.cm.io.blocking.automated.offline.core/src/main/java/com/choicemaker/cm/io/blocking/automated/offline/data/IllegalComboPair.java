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
package com.choicemaker.cm.io.blocking.automated.offline.data;

/**
 * This object represents a pair of columns that should not be blocked together
 * by BABA.
 * 
 * @author pcheung
 *
 */
public class IllegalComboPair implements Comparable<IllegalComboPair> {

	private int col1;
	private int col2;

	public IllegalComboPair(int col1, int col2) {
		this.col1 = col1;
		this.col2 = col2;
	}

	public int getCol1() {
		return col1;
	}

	public int getCol2() {
		return col2;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IllegalComboPair) {
			IllegalComboPair p = (IllegalComboPair) o;
			if (this.col1 == p.col1 && this.col2 == p.col2)
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(IllegalComboPair p) {
		if (this.col1 < p.col1)
			return -1;
		else if (this.col1 > p.col1)
			return 1;
		else {
			if (this.col2 < p.col2)
				return -1;
			else if (this.col2 > p.col2)
				return 1;
			else
				return 0;
		}
	}

	@Override
	public int hashCode() {
		return col1 ^ col2;
	}

}
