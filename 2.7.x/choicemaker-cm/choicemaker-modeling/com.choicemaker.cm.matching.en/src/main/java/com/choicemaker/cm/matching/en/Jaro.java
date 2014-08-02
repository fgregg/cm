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
package com.choicemaker.cm.matching.en;

/**
 * The Winkler and Jaro algorithm (Porter and Winkler 1980) is a string
 * comparator developed by two researchers at the US Census Bureau. It computes
 * a similarity score between two strings in the range 0 to 1. It works
 * especially well for English. Differences at the beginning are weighed more
 * strongly than differences towards the end of the strings.
 *
 * @author    Peining Tao
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:01 $
 */
public class Jaro {
	private static boolean numb(char c) {
		return (c >= '0') && (c <= '9');
	}

	private static boolean notNum(char c) {
		return (c < '0') || (c > '9');
	}

	private static boolean inRange(int i) {
		return (i > 0) && (i < 91);
	}

	private static final int SPMAX = 36;
	private static char sp[][] = { { 'A', 'E' }, {
			'A', 'I' }, {
			'A', 'O' }, {
			'A', 'U' }, {
			'B', 'V' }, /* 00 -> 04 */ {
			'E', 'I' }, {
			'E', 'O' }, {
			'E', 'U' }, {
			'I', 'O' }, {
			'I', 'U' }, /* 05 -> 09 */ {
			'O', 'U' }, {
			'I', 'Y' }, {
			'E', 'Y' }, {
			'C', 'G' }, {
			'E', 'F' }, /* 10 -> 14 */ {
			'W', 'U' }, {
			'W', 'V' }, {
			'X', 'K' }, {
			'S', 'Z' }, {
			'X', 'S' }, /* 15 -> 19 */ {
			'Q', 'C' }, {
			'U', 'V' }, {
			'M', 'N' }, {
			'L', 'I' }, {
			'Q', 'O' }, /* 20 -> 24 */ {
			'P', 'R' }, {
			'I', 'J' }, {
			'2', 'Z' }, {
			'5', 'S' }, {
			'8', 'B' }, /* 25 -> 29 */ {
			'1', 'I' }, {
			'1', 'L' }, {
			'0', 'O' }, {
			'0', 'Q' }, {
			'C', 'K' }, /* 30 -> 34 */ {
			'G', 'J' }
	}; /* 35       */

	static int[][] adjwt = new int[91][91];
	static {
		for (int i = 0; i < 91; i++)
			for (int j = 0; j < 91; j++)
				adjwt[i][j] = 0;

		/* Set default adjusted weights                                               */
		for (int i = 0; i <= SPMAX - 1; i++) {
			adjwt[sp[i][0]][sp[i][1]] = 3;
			adjwt[sp[i][1]][sp[i][0]] = 3;
		}

		/* Re-adjust weight of certain sound alikes - added in Version 3              */

		adjwt[73][89] = 4;
		adjwt[89][73] = 4;
		adjwt[67][75] = 4;
		adjwt[75][67] = 4;
	}

	/**
	 * Computes the similarity of two strings according to the Jaro algorithm.
	 * This version includes group matching introduced in 97 by
	 * Susan M. Odell and Richard T. Jufer.
	 *
	 * @param a The first string.
	 * @param b The second string.
	 * @return the Jaro similarity between a and b
	 * @throws NullPointerException if one or both strings are <code>null</code>.
	 */
	public static float jaro(String a, String b) {
		return jaro(a, b, true, true, false);
	}

	/**
	 * Computes the similarity of two strings according to the Jaro algorithm.
	 * This version uses classic symbol to symbol matching.
	 * Another method of this class "jaro" uses more recent group matching approach.
	 *
	 * @param a The first string.
	 * @param b The second string.
	 * @return the Jaro similarity between a and b
	 * @throws NullPointerException if one or both strings are <code>null</code>.
	 */
	public static float jaroClassic(String a, String b) {
		return jaroClassic(a, b, true, true, false);
	}

	/**
	 * Computes the Jaro-Winkler similarity between ying and yang.
	 * This version includes group matching introduced in 97 by
	 * Susan M. Odell and Richard T. Jufer.
	 *
	 * @param ying The first string.
	 * @param yang The second string.
	 * @param higherScoreForLongStrings Increase the probability of a match when the number of matched
	 *              characters is large.  This option allows for a little more
	 *              tolerance when the strings are large.  It is not an appropriate
	 *              test when comparing fixed length fields such as phone and
	 *              social security numbers.
	 * @param convertToUpper All lower case characters are converted to upper case prior
	 *              to the comparison.  Disabling this clue means that the lower
	 *              case string "code" will not be recognized as the same as the
	 *              upper case string "CODE".  Also, the adjustment for similar
	 *              characters section only applies to uppercase characters.
	 * @param checkForNumbers Check for numbers in data; if number is present in either the
	 *              ying or the yang string and the strings do not match, then 0.0
	 *              is returned, else the calculated weight is returned.
	 * @return the Jaro similarity between a and b
	 * @throws NullPointerException if one or both strings are <code>null</code>.
	 */
	public static float jaro(
		String ying,
		String yang,
		boolean higherScoreForLongStrings,
		boolean convertToUpper,
		boolean checkForNumbers) {
		int y_length = Math.min(ying.length(), yang.length());

		float weight, Num_sim;
		int minv, search_range, ying_length, N_trans, Num_com, yang_length;
		long G_trans;
		int N_simi;
		int done, nchek, match;
		boolean num_pres = false; /* added in version 3 */
		int i, j, nc, m, n;

		char[] ying_hold1 = trimAndConvert(ying, y_length, convertToUpper);
		char[] yang_hold1 = trimAndConvert(yang, y_length, convertToUpper);

		ying_length = ying_hold1.length;
		yang_length = yang_hold1.length;
		/* If either string is blank - return - added in Version 2                    */
		if (ying_length == 0 || yang_length == 0) {
			return 0.0f;
		}

		/* Identify the strings to be compared by stripping off all leading and
		   trailing spaces.                                                           */
		if (ying_length > yang_length) {
			search_range = ying_length;
			minv = yang_length;

		} else {
			search_range = yang_length;
			minv = ying_length;
		}

		/* If either string is blank - return                                         */
		/* if (!minv) return(0.0);                   removed in version 2             */

		/* Blank out the flags                                                        */
		byte[] ying_flag1 = new byte[search_range];
		byte[] yang_flag1 = new byte[search_range];

		search_range = (search_range / 2) - 1;
		if (search_range < 0)
			search_range = 0; /* added in version 2               */

		/* If match numbers option is deactivated set a number present flag -
		   Added Ver 3 ...  */
		if (checkForNumbers) {
			num_pres = chk_for_number(ying_hold1, yang_hold1);
		}

		/* looking through substrings of decreasing size,
		   Count and flag the matched pairs.                                          */

		N_trans = 0;
		G_trans = 0;
		Num_com = 0;

		done = 0;
		nchek = minv;
		/* start with substrings of len "minv" and continue down to substrings of
		   len 1                                                                      */
		for (nc = nchek; nc >= 1; nc--) {
			for (i = 0; i <= ying_length - nc; i++) {
				for (j = 0; j <= yang_length - nc; j++) {
					/* assume match
					   - if ((ying char NE yang char) or either is prev flagged cancel match     */
					match = 1;
					for (m = 0; m <= nc - 1; m++) {
						if ((ying_hold1[i + m] != yang_hold1[j + m])
							|| (ying_flag1[i + m] == 1)
							|| (yang_flag1[j + m] == 1)) {
							match = 0;
							break;
						}
					}
					/* flag the match                                                             */
					if (match != 0) {
						for (n = 0; n <= nc - 1; n++) {
							ying_flag1[i + n] = 1;
							yang_flag1[j + n] = 1;
						}
						/* incr # in common by this grp len and count the group transpositions or
						   single transpositions                                                      */
						Num_com += nc;
						if (i != j) {
							if (nc > 1) {
								G_trans++;

							} else {
								if (Math.abs(i - j) <= search_range) {
									N_trans++;

								} else {
									ying_flag1[i] = 0;
									yang_flag1[j] = 0;
									Num_com--;
								}
							}
						}
						/* group matched, incr indices and decr group len for next group              */
						j = j + nc - 1;
						i = i + nc - 1;
						if (nc > minv - Num_com)
							nc = minv - Num_com + 1;
						if (Num_com == minv) {
							done = 1;
							break;
						}
					}
				}
				if (done != 0)
					break;
			}
			if (done != 0)
				break;
		}

		/* if all chars matched and no transpositions return 1.0                      */
		if ((Num_com == ying_length) && (Num_com == yang_length) && (G_trans == 0) && (N_trans == 0))
			return (1.0f);
		else {

			/*  if number matching deactivated and # present return 0.0                   */
			if (checkForNumbers && num_pres) {
				return (0.0f);
			}
		}

		/* if no com char, then return with 0.0                                       */
		if (Num_com == 0)
			return (0.0f);

		/* adjust for similarities in nonmatched characters                           */
		N_simi = 0;
		if (minv > Num_com) {
			for (i = 0; i < ying_length; i++) {
				if (ying_flag1[i] == 0 && inRange(ying_hold1[i])) {
					for (j = 0; j < yang_length; j++) {
						if (yang_flag1[j] == 0 && inRange(yang_hold1[j])) {
							if (adjwt[ying_hold1[i]][yang_hold1[j]] > 0) {
								N_simi += adjwt[ying_hold1[i]][yang_hold1[j]];
								yang_flag1[j] = 2;
								break;
							}
						}
					}
				}
			}
		}
		Num_sim = (N_simi) / 10.0f + Num_com;

		/* Main weight computation.                                                   */
		weight =
			Num_sim / (ying_length)
				+ Num_sim / (yang_length)
				+ ((float) (2 * Num_com - N_trans - 2 * G_trans)) / ((float) (2 * Num_com));

		weight = weight / 3.0f;

		/* Continue to boost the weight if the strings are similar                    */
		if (weight > 0.7) {

			/* Adjust for having up to the first 4 characters in common                 */
			j = (minv >= 4) ? 4 : minv;
			for (i = 0;((i < j) && (ying_hold1[i] == yang_hold1[i]) && (notNum(ying_hold1[i]))); i++);
			if (i != 0)
				weight += i * 0.1 * (1.0 - weight);

			/* Optionally adjust for long strings.                                      */
			/* After agreeing beginning chars, at least two more must agree and
			   the agreeing characters must be > .5 of remaining characters.          */
			if (higherScoreForLongStrings && (minv > 4) && (Num_com > i + 1) && (2 * Num_com >= minv + i))
				if (notNum(ying_hold1[0]))
					weight += (float) (1.0 - weight)
						* ((float) (Num_com - i - 1) / ((float) (ying_length + yang_length - i * 2 + 2)));
		}

		return (weight);

	} /* end of strcmp95 */

	private static char[] trimAndConvert(String s, int maxLen, boolean convertToUpper) {
		int len = s.length();
		int firstNonWhite = 0;
		while (firstNonWhite < len && Character.isSpaceChar(s.charAt(firstNonWhite))) {
			++firstNonWhite;
		}
		if (firstNonWhite == len) {
			return new char[0];
		}
		int lastNonWhite = len - 1;
		while (lastNonWhite >= 0 && Character.isSpaceChar(s.charAt(lastNonWhite))) {
			--lastNonWhite;
		}
		int resLen = Math.min(lastNonWhite - firstNonWhite + 1, maxLen);
		char[] res = new char[resLen];
		for (int i = 0; i < resLen; ++i) {
			if (convertToUpper) {
				res[i] = Character.toUpperCase(s.charAt(i + firstNonWhite));
			} else {
				res[i] = s.charAt(i + firstNonWhite);
			}
		}
		return res;
	}

	private static boolean chk_for_number(char[] a, char[] b) {
		int len = Math.min(a.length, b.length);
		for (int i = 0; i <= len - 1; i++) {
			if (numb(a[i]) || numb(b[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Computes the Jaro-Winkler similarity between ying and yang.
	 * This version uses classic symbol to symbol matching.
	 * Another method of this class "jaro" uses more recent group matching approach.
	 *
	 * @param ying The first string.
	 * @param yang The second string.
	 * @param higherScoreForLongStrings Increase the probability of a match when the number of matched
	 *              characters is large.  This option allows for a little more
	 *              tolerance when the strings are large.  It is not an appropriate
	 *              test when comparing fixed length fields such as phone and
	 *              social security numbers.
	 * @param convertToUpper All lower case characters are converted to upper case prior
	 *              to the comparison.  Disabling this clue means that the lower
	 *              case string "code" will not be recognized as the same as the
	 *              upper case string "CODE".  Also, the adjustment for similar
	 *              characters section only applies to uppercase characters.
	 * @param checkForNumbers Check for numbers in data; if number is present in either the
	 *              ying or the yang string and the strings do not match, then 0.0
	 *              is returned, else the calculated weight is returned.
	 * @return the Jaro similarity between a and b
	 * @throws NullPointerException if one or both strings are <code>null</code>.
	 */
	public static float jaroClassic(
		String ying,
		String yang,
		boolean higherScoreForLongStrings,
		boolean convertToUpper,
		boolean checkForNumbers) {
		int y_length = Math.min(ying.length(), yang.length());

		float weight, Num_sim;
		int minv, search_range, ying_length, N_trans, Num_com, yang_length;
		int lowlim,hilim;

		int N_simi;
		//int done, nchek, match;
		boolean num_pres = false; /* added in version 3 */
		// 2014-04-24 rphall: Commented out unused local variables.
		int		yl1 /*, yi_st */;
		int i, j, k /*, nc, m, n */;

		char[] ying_hold1 = trimAndConvert(ying, y_length, convertToUpper);
		char[] yang_hold1 = trimAndConvert(yang, y_length, convertToUpper);

		ying_length = ying_hold1.length;
		yang_length = yang_hold1.length;
		/* If either string is blank - return - added in Version 2                    */
		if (ying_length == 0 || yang_length == 0) {
			return 0.0f;
		}

		/* Identify the strings to be compared by stripping off all leading and
		   trailing spaces.                                                           */
		if (ying_length > yang_length) {
			search_range = ying_length;
			minv = yang_length;

		} else {
			search_range = yang_length;
			minv = ying_length;
		}

		/* If either string is blank - return                                         */
		/* if (!minv) return(0.0);                   removed in version 2             */

		/* Blank out the flags                                                        */
		byte[] ying_flag1 = new byte[search_range];
		byte[] yang_flag1 = new byte[search_range];

		search_range = (search_range / 2) - 1;
		if (search_range < 0)
			search_range = 0; /* added in version 2               */

		/* If match numbers option is deactivated set a number present flag -
		   Added Ver 3 ...  */
		if (checkForNumbers) {
			num_pres = chk_for_number(ying_hold1, yang_hold1);
		}

		/* looking through substrings of decreasing size,
		   Count and flag the matched pairs.                                          */

		N_trans = 0;
		Num_com = 0;

		yl1 = yang_length - 1;
		for (i = 0;i < ying_length;i++) {
		  lowlim = (i >= search_range) ? i - search_range : 0;
		  hilim = ((i + search_range) <= yl1) ? (i + search_range) : yl1;
		  for (j = lowlim;j <= hilim;j++)  {
			if ((yang_flag1[j] != '1') && (yang_hold1[j] == ying_hold1[i])) {
				yang_flag1[j] = '1';
				ying_flag1[i] = '1';
				Num_com++;
				break;
		} } }

		/* If no characters in common - return                                        */
		if (Num_com == 0) return (0.0f);

		/* Count the number of transpositions                                         */
		k = N_trans = 0;
		for (i = 0;i < ying_length;i++) {
		  if (ying_flag1[i] == '1') {
			for (j = k;j < yang_length;j++) {
				if (yang_flag1[j] == '1') {
				 k = j + 1;
				 break;
			} }
			if (ying_hold1[i] != yang_hold1[j]) N_trans++;
		} }
		N_trans = N_trans / 2;

		/* if all chars matched and no transpositions return 1.0                      */
		if ((Num_com == ying_length) &&
			(Num_com == yang_length) &&
			  (N_trans == 0))
		  return(1.0f);
		else {
			/*  if number matching deactivated and # present return 0.0                   */
				if (checkForNumbers && num_pres) {
					return (0.0f);
				}
		}

		/* adjust for similarities in nonmatched characters                           */
		N_simi = 0;
		if (minv > Num_com) {
			for (i = 0; i < ying_length; i++) {
				if (ying_flag1[i] == 0 && inRange(ying_hold1[i])) {
					for (j = 0; j < yang_length; j++) {
						if (yang_flag1[j] == 0 && inRange(yang_hold1[j])) {
							if (adjwt[ying_hold1[i]][yang_hold1[j]] > 0) {
								N_simi += adjwt[ying_hold1[i]][yang_hold1[j]];
								yang_flag1[j] = 2;
								break;
							}
						}
					}
				}
			}
		}
		Num_sim = (N_simi) / 10.0f + Num_com;

		/* Main weight computation.                                                   */
		weight =
			Num_sim / (ying_length)
				+ Num_sim / (yang_length)
			    + ((float) (Num_com - N_trans)) / ((float) Num_com);

		weight = weight / 3.0f;

		/* Continue to boost the weight if the strings are similar                    */
		if (weight > 0.7) {

			/* Adjust for having up to the first 4 characters in common                 */
			j = (minv >= 4) ? 4 : minv;
			for (i = 0;((i < j) && (ying_hold1[i] == yang_hold1[i]) && (notNum(ying_hold1[i]))); i++);
			if (i != 0)
				weight += i * 0.1 * (1.0 - weight);

			/* Optionally adjust for long strings.                                      */
			/* After agreeing beginning chars, at least two more must agree and
			   the agreeing characters must be > .5 of remaining characters.          */
			if (higherScoreForLongStrings && (minv > 4) && (Num_com > i + 1) && (2 * Num_com >= minv + i))
				if (notNum(ying_hold1[0]))
					weight += (float) (1.0 - weight)
						* ((float) (Num_com - i - 1) / ((float) (ying_length + yang_length - i * 2 + 2)));
		}

		return (weight);

	} /* end of strcmp95 */

}
