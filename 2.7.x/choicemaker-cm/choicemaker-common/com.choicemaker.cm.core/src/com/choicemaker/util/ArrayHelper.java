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
package com.choicemaker.util;

import java.util.Arrays;

public class ArrayHelper {

	public static boolean contains(Object[] a, Object o) {
		for (int i = 0; i < a.length; ++i) {
			if (o.equals(a[i])) {
				return true;
			}
		}
		return false;
	}

	public static float[] getOneArray(int size) {
		float[] res = new float[size];
		Arrays.fill(res, 1.0f);
		return res;
	}

	/**
	 * Returns a boolean array with all elements set to <code>true</code>.
	 *
	 * This method is useful for creating a <code>cluesToEvaluate</code>
	 * value that can be used to evaluate all clues.
	 *
	 * @return  A boolean array of length <code>size</code> with all
	 *            elements set to <code>true</code>.
	 */
	public static boolean[] getTrueArray(int size) {
		boolean[] res = new boolean[size];
		Arrays.fill(res, true);
		return res;
	}

}
