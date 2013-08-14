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
 * The longest common substring algorithm (Friedman and Sideli 1992) provides 
 * two functions. First, it computes the length of the longest common substring 
 * between two strings. E.g., the longest common substring of "TEST" and "FEST" 
 * is "EST", which has a length of 3.  The second function is a similarity 
 * score. The function repeatedly removes the longest common substring from the 
 * two strings to be compared. The similarity score is defined by the sum of 
 * the lengths of the removed substrings divided by some measure. The latter 
 * can be the average, minimum, or maximum length of the original two strings.  
 * The longest common substring similarity score can be used, for example, when 
 * values are reversed. 
 * <br>
 * <b>E.g.</b>, the comparison of "JIM SMITH" with "SMITH JIM" 
 * returns a perfect score of 1. The function is equally well suited for most 
 * languages with a small alphabet.
 * 
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public class LongestCommonSubstring {

	/** AVERAGE parameter. */
	public static final int AVERAGE = -1;

	/** SHORTEST parameter. */
	public static final int SHORTEST = -2;

	/** LONGEST parameter. */
	public static final int LONGEST = -3;

	private static int[] longestCommonSubstringW(String a, String b) {
		int[] res = new int[3];
		if (a != null && b != null) {
			final int lenA = a.length();
			final int lenB = b.length();
			int[][] l = new int[lenA][lenB];
			for (int i = 0; i < lenA; ++i) {
				char cA = a.charAt(i);
				for (int j = 0; j < lenB; ++j) {
					if (cA == b.charAt(j)) {
						int cur = l[i][j] = i > 0 && j > 0 ? l[i - 1][j - 1] + 1 : 1;
						if (cur > res[0]) {
							res[0] = cur;
							res[1] = i - cur + 1;
							res[2] = j - cur + 1;
						}
					} else {
						l[i][j] = 0;
					}
				}
			}
		}
		return res;
	}

	/**
	 * Returns the length of the longest common substring.
	 * 
	 * @param a the first String
	 * @param b the second String
	 * @return the length of the longest common substring
	 */
	public static int longestCommonSubstring(String a, String b) {
		return longestCommonSubstringW(a, b)[0];
	}

	/**
	 * Computes the similarity between a and b. 
	 * 
	 * @param a the first String
	 * @param b the second String
	 * @param denominator defines what value is used as the denominator for 
	 * computing the similarity. It can be one of the following:
	 * <ul>
	 * 	<li> AVERAGE: (a.length() + b.length()) / 2f </li>
	 * 	<li> SHORTEST: min(a.length(), b.length()) </li>
	 * 	<li> LONGEST: max(a.length(), b.length()) </li>
	 * </ul>
	 * @param minLength determines the minimum length of a substring that is removed
	 * @param maxRepetition The maximum number of removal iterations
	 * @return the LCS similarity between a and b for the given parameters
	 */
	public static float similarity(String a, String b, int denominator, int minLength, int maxRepetition) {
		if (a == null || b == null)
			return 0;
		final int lenA = a.length();
		final int lenB = b.length();
		int sumSubstringLength = 0;
		if (lenA > 0 && lenB > 0 && maxRepetition > 0) {
			int repetition = 0;
			while (true) { // contains break
				int[] res = longestCommonSubstringW(a, b);
				++repetition;
				if (res[0] >= minLength) {
					sumSubstringLength += res[0];
					if (repetition < maxRepetition
						&& a.length() - res[0] >= minLength
						&& b.length() - res[0] >= minLength) {
						a = a.substring(0, res[1]) + a.substring(res[1] + res[0], a.length());
						b = b.substring(0, res[2]) + b.substring(res[2] + res[0], b.length());
					} else {
						break;
					}
				} else {
					break;
				}
			}
		}
		float den;
		switch (denominator) {
			case AVERAGE :
				den = (lenA + lenB) / 2f;
				break;
			case SHORTEST :
				den = Math.min(lenA, lenB);
				break;
			case LONGEST :
				den = Math.max(lenA, lenB);
				break;
			default :
				den = denominator;
		}
		return den == 0 ? 0 : sumSubstringLength / den;
	}

	/**
	 * Returns the SHORTEST similarity between a and b with minLength = 3 and maxRepetition = 3.
	 * 
	 * @param a the first String
	 * @param b the second String
	 * @return the SHORTEST similarity between a and b
	 */
	public static float similarity(String a, String b) {
		return similarity(a, b, SHORTEST, 3, 3);
	}
}
