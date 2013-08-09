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

import java.util.Arrays;

/**
 * Implementation of the Longest Common Subsequence algorithm as described
 * in "Introduction to Algorithms" by Cormen, Leiserson, and Rivest.
 * 
 * The LCS of two strings x and y is the number of characters in order that
 * x and y have in common.  e.g. the LCS of "synchronize" and "chrome" is
 * 5:
 * <pre>   
 * 		syn chro niz e
 *  	    chro  m  e
 * </pre>
 * 
 * @author    Adam Winkel
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public final class LongestCommonSubsequence {

	/**
	 * Returns whether one of the two strings of length at least 2 is a subsequence of the other and they start with the same
	 * characters. More specifically, returns true iff:
	 * <ol>
	 * 	<li>the minimum of the lengths of <code>w1</code> and <code>w2</code> is at least 2, and</li>
	 * 	<li>the longest common subsequence length of <code>w1</code> and <code>w2</code> is equal to the 
	 * 		minimum of the lengths of <code>w1</code> and <code>w2</code>, and</li>
	 * 	<li><code>w1</code> and <code>w2</code> begin with the same character.</li>
	 * </ol>
	 * 
	 * @param   w1  One of the strings.
	 * @param   w2  The other string.
	 * @return  Whether one string is a subsequence of the other.
	 */
	public static boolean isLcsAbbrev(String w1, String w2) {
		return isLcsAbbrev(w1, w2, 2);
	}

	/**
	 * Returns whether one of the two strings of length at least <code>minLength</code> is a subsequence of the other and they start with the same
	 * characters. More specifically, returns true iff:
	 * <ol>
	 * 	<li>the minimum of the lengths of <code>w1</code> and <code>w2</code> is at least <code>minLength</code>, and</li>
	 * 	<li>the longest common subsequence length of <code>w1</code> and <code>w2</code> is equal to the 
	 * 		minimum of the lengths of <code>w1</code> and <code>w2</code>, and</li>
	 * 	<li><code>w1</code> and <code>w2</code> begin with the same character.</li>
	 * </ol>
	 * 
	 * @param   w1  the first String
	 * @param   w2  the second String
	 * @param   minLength w1 and w2 must be at least <code>minLength</code> for this method to return true
	 * @return  Whether one string is a subsequence of the other
	 */
	public static boolean isLcsAbbrev(String w1, String w2, int minLength) {
		return w1.length() >= minLength
			&& w2.length() >= minLength
			&& w1.charAt(0) == w2.charAt(0)
			&& Math.min(w1.length(), w2.length()) == lcsLength(w1, w2);
	}

	/**
	 * Returns whether one of the two strings of length at least <code>minLength</code> is a subsequence of the other.
	 * More specifically, returns true iff:
	 * <ol>
	 * 	<li>the minimum of the lengths of <code>w1</code> and <code>w2</code> is at least <code>minLength</code>, and</li>
	 * 	<li>the longest common subsequence length of <code>w1</code> and <code>w2</code> is equal to the 
	 * 		minimum of the lengths of <code>w1</code> and <code>w2</code>, and</li>
	 * </ol>
	 * 
	 * <b>Note:</b> this method does not care if w1 and w2 begin with the same character or not.
	 * 
	 * @param   w1  the first String
	 * @param   w2  the second String
	 * @param   minLength w1 and w2 must be at least <code>minLength</code> for this method to return true
	 * @return  Whether one string is a subsequence of the other
	 */
	public static boolean isLcsAbbrevAnyStart(String w1, String w2, int minLength) {
		return w1.length() >= minLength
			&& w2.length() >= minLength
			&& Math.min(w1.length(), w2.length()) == lcsLength(w1, w2);
	}	

	/**
	 * Returns the length of the longest common subsequence between <code>w1</code>
	 * and <code>w2</code>.
	 * 
	 * @param   w1  One of the strings.
	 * @param   w2  The other string.
	 * @return  The length of the longest common subsequence.
	 */
	public static int lcsLength(String w1, String w2) {
		int[][] c = lcsChart(w1, w2);
		return c[w1.length()][w2.length()];
	}

	/**
	 * Creates and returns an m by n chart where m = x.length(), 
	 * n = y.length(), and the ith, jth entry is the length of the 
	 * longest common subsequence between indices 0 and i in String x
	 * and 0 and j in String y.
	 * 
	 * Thus, the LCS length is c[x.length()][y.length()].
	 * 
	 * Note that the actual characters of the longest common subsequence
	 * can be recovered from x, y, and the returned chart.
	 * 
	 * @param   x  One of the strings.
	 * @param   y  The other string.
	 * @return  the LCS chart
	 */
	public static int[][] lcsChart(String x, String y) {
		int m = x.length();
		int n = y.length();

		int[][] c = new int[m + 1][];
		for (int i = 0; i <= m; i++) {
			c[i] = new int[n + 1];
			c[i][0] = 0;
		}
		Arrays.fill(c[0], 0);

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				int cx = x.charAt(i - 1);
				int cy = y.charAt(j - 1);
				if (cx == cy) {
					c[i][j] = c[i - 1][j - 1] + 1;
				} else {
					c[i][j] = Math.max(c[i - 1][j], c[i][j - 1]);
				}
			}
		}

		return c;
	}

	private LongestCommonSubsequence() { }
}
