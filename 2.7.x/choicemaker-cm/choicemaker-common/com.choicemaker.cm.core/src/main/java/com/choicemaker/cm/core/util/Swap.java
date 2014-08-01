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
package com.choicemaker.cm.core.util;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class Swap {
	private static final int NONE = 0;
	private static final int DIFFERENT = 1;
	private static final int SAME = 2;

	public static boolean swapsame(Object[][] q, Object[][] m, int numPerConjunct, int minNumMoved) {
		int[] matches = new int[m.length];
		int diffMatches = 0;
		int sameMatches = 0;
		cnt : for (int i = 0; i < q.length; i++) {
			if (diffMatches >= minNumMoved && diffMatches + sameMatches >= numPerConjunct) {
				return true;
			}
			boolean sm = false;
			Object[] qi = q[i];
			for (int j = 0; j < qi.length; j++) {
				Object qij = qi[j];
				for (int k = 0; k < m.length; k++) {
					if (matches[k] == NONE || matches[k] == SAME) {
						Object[] mk = m[k];
						for (int l = 0; l < mk.length; l++) {
							Object mkl = mk[l];
							if (qij == null ? mkl == null : qij.equals(mkl)) {
								if (i == k) {
									sm = true;
								} else {
									++diffMatches;
									if (matches[k] == SAME) {
										--sameMatches;
									}
									matches[k] = DIFFERENT;
									continue cnt;
								}
							}
						}
					}
				}
			}
			if (sm) {
				++sameMatches;
				matches[i] = SAME;
			}
		}
		return diffMatches >= minNumMoved && diffMatches + sameMatches >= numPerConjunct;
	}
}
