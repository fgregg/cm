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
package com.choicemaker.cm.matching.gen;

/**
 * Computes the modified edit distance between 2 strings.
 * As compared to regular edit distance, "swaps" or "twiddles" of two 
 * letters count as a single edit rather than two.
 * 
 * @author Adam Winkel
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:24:25 $
 * @see EditDistance
 */

public class EditDistance2 {
	
	public static final int DISTANCE_LIMIT = Integer.MAX_VALUE - 1;

	/**
	 * Computes the modified edit distance between two strings.
	 *
	 * If the result would be greater than <code>maxDistance</code>, then
	 * <code>maxDistance + 1</code> is returned. The rationale for
	 * the the <code>maxDistance</code> parameter is to improve performance
	 * by stopping the computation if the result would be bigger than
	 * interested.
	 *
	 * @param  s           The first string.
	 * @param  t           The second string.
	 * @param  maxDistance The maximum distance. Must be <code>&gt; 0</code> and
	 *           <code>&le; DISTANCE_LIMIT</code>.
	 * @return the minimum edit distance between <code>s</code> and <code>t</code>
	 * @throws IllegalArgumentException if the maxDistance is not positive or
	 * is greater than {@link #DISTANCE_LIMIT}
	 */
	public static int editDistance2(String s, String t, int maxDistance) {
		
		if (maxDistance < 1 || maxDistance > DISTANCE_LIMIT) {
			throw new IllegalArgumentException("invalid maximum distance: "
					+ maxDistance);
		}

		final int MAX_RETURN = maxDistance + 1;
		if (s == null || t == null) {
			return MAX_RETURN;
		}

		int m = s.length();
		int n = t.length();
		if (m == 0) {
			return Math.min(MAX_RETURN, n);
		}
		if (n == 0) {
			return Math.min(MAX_RETURN, m);
		}

		// BUG rphall 2008-12-18
		// The maxDistance parameter is not used to stop the computation
		int[][] d = new int[m + 1][n + 1];
		for (int i = 0; i <= m; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= n; j++) {
			d[0][j] = j;
		}

		int cost;
		for (int i = 1; i <= m; i++) {
			char s_i = s.charAt(i - 1);
			for (int j = 1; j <= n; j++) {
				if (s_i == t.charAt(j - 1)) {
					cost = 0;
				} else {
					cost = 1;	
				}
								
				d[i][j] = Math.min(d[i-1][j] + 1, Math.min(d[i][j-1] + 1, d[i-1][j-1] + cost));

				if (i > 1 && j > 1 && s.charAt(i - 2) == t.charAt(j - 1) && s.charAt(i - 1) == t.charAt(j - 2)) {
					d[i][j] = Math.min(d[i][j], d[i-2][j-2] + 1);	
				}
			}
		}
		// END BUG

		return Math.min(MAX_RETURN, d[m][n]);
	}

	/**
	 * Computes the modified edit distance between two strings.
	 *
	 * This is equivalent to <code>editDistance(s, t, DISTANCE_LIMIT)</code>.
	 *
	 * @param  s           The first string.
	 * @param  t           The second string.
	 * @return the minimum edit distance between <code>a</code> and <code>b</code> 
	 */
	public static int editDistance2(String s, String t) {
		return editDistance2(s, t, DISTANCE_LIMIT);
	}
}
