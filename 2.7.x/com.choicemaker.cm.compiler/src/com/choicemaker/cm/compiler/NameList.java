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
package com.choicemaker.cm.compiler;

/**
 * Class for incrementally assembling name arrays
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public final class NameList {

	/** the name list
	 */
	private String[] strings = new String[4];

	/** the length of the list
	 */
	private int len = 0;

	/** append a name to the list
	 */
	public void append(String tree) {
		if (len == strings.length) {
			String[] ts = new String[len * 2];
			System.arraycopy(strings, 0, ts, 0, len);
			strings = ts;
		}
		strings[len++] = tree;
	}

	/** append a string array to the list
	 */
	public void append(String[] ts) {
		for (int j = 0; j < ts.length; j++)
			append(ts[j]);
	}

	/** remove the last n entries
	 */
	public String remove(int n) {
		return strings[len -= n];
	}

	/** return the current length
	 */
	public int length() {
		return len;
	}

	/** access i-th entry
	 */
	public String get(int i) {
		return strings[i];
	}

	/** convert tree list to array
	 */
	public String[] toArray() {
		String[] ts = new String[len];
		System.arraycopy(strings, 0, ts, 0, len);
		return ts;
	}
}
