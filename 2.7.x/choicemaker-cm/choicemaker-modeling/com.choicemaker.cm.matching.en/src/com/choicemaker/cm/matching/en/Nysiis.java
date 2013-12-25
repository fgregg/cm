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
 * Modified NYSIIS matching method holder class.
 * 
 * NYSIIS, the New York State Information Interchange System, (Newcombe 1988) 
 * is a phoneticization algorithm similar to Soundex. It also works especially 
 * well for English. NYSIIS accounts for more phonetic spelling errors than 
 * Soundex and at the same time maps fewer strings with different pronunciations 
 * to the same code.  Unlike Soundex, NYSIIS is slightly modified by most 
 * vendors. Hence, NYSIIS codes are not interchangeable.
 * 
 * @author    Martin Buechi (Martin.Buechi@choicemaker.com)
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:01 $
 */
public final class Nysiis {

	/**
	 * Hide constructor.
	 */
	private Nysiis() {
	}

	/**
	 * Determines whether a character is a vowel.
	 *
	 * @param   c The upper case character to be inspected.
	 * @return  <code>true</code> iff <code>c</code> is a vowel.
	 */
	private static boolean isVowel(char c) {
		return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U';
	}

	/**
	 * Modified NYSIIS name coding procedure.
	 *
	 * Based on the modified NYSIIS name coding procedure specification with the
	 * following additional modifications:
	 * <ol>
	 *    <li>All non-letter characters, including spaces and punctuation marks,
	 *        are removed at the very beginning.</li>
	 *    <li>No spaces are ever inserted into the input string or the NYSIIS code.
	 *        I.e., where the specification prescribes a replacement by "x ", we
	 *        simply replace by "x".</li>
	 *    <li>Suffixes "JR" and "SR" are removed before Step 2 rather than treating
	 *        them as errors in Step 3</li>
	 *    <li>Ambiguity between 6k and 6m is resolved by giving preference to 6k.
	 *        (Note that the resulting NYSIIS code is the same anyhow.)</li>
	 *    <li>In Step 6l, the current letter 'h' is only kept if <em>both</em> the
	 *        preceeding and the following letter are vowels. In all other cases,
	 *        the current letter is replaced by the preceeding one.</li>
	 * </ol>
	 *
	 * Code optimized for execution speed vs. legibility and code size.
	 *
	 * @param   s String to be encoded.
	 * @return  The NYSIIS code of s.
	 */
	public static String nysiis(String s) {
		if (s == null) {
			return "";
		}
		char[] is = s.toCharArray();
		// Input string can 'grow' in a certain case by a 'K'
		// This variable stores whether this has happened.
		boolean trailingK = false;

		// Remove non-letters.
		// Perform case conversion (incorrect for sharp s (German) and
		// two characters when running under Turkish locale)
		int last = -1;
		for (int i = 0; i < is.length; ++i) {
			if (Character.isLetter(is[i])) {
				is[++last] = Character.toUpperCase(is[i]);
			}
		}

		// Handle empty string (initially or after removing non-letters)
		if (last < 0) {
			return "";
		}

		char firstChar = is[0]; // Remember first character

		// Step 1
		switch (is[0]) {
			case 'M' :
				if (last > 1 && is[1] == 'A' && is[2] == 'C') {
					is[1] = 'C';
				}
				break;
			case 'K' :
				if (last > 0 && is[1] == 'N') {
					is[0] = 'N';
				} else {
					is[0] = 'C';
				}
				break;
			case 'P' :
				if (last > 0 && (is[1] == 'F' || is[1] == 'H')) {
					is[0] = 'F';
					is[1] = 'F';
				}
				break;
			case 'S' :
				if (last > 1 && is[1] == 'C' && is[2] == 'H') {
					is[1] = 'S';
					is[2] = 'S';
				}
				break;
			case 'W' :
				if (last > 0 && is[1] == 'R') {
					is[0] = 'R';
				}
				break;
			case 'R' :
				if (last > 0 && is[1] == 'H') {
					is[1] = 'R';
				}
				break;
			case 'D' :
				if (last > 0 && is[1] == 'G') {
					is[0] = 'G';
				}
				break;
			case 'E' :
			case 'I' :
			case 'O' :
			case 'U' : // Case 'A' is superfluous.
				// See comment above regarding spaces.
				is[0] = 'A';
				break;
		}

		// Remove JR or SR suffix
		if (is[last] == 'R' && last > 0 && (is[last - 1] == 'J' || is[last - 1] == 'S')) {
			last -= 2; // case where last becomes -1 is handled below in Step 2
		}

		// Step 2
		for (; last >= 0 && (is[last] == 'S' || is[last] == 'Z'); --last);
		if (last < 0) {
			return "";
		}

		// Step 3
		if (last > 0) { // All replacement strings are 2 or mor characters
			switch (is[last]) {
				case 'E' :
					if (is[last - 1] == 'E' || is[last - 1] == 'I' || is[last - 1] == 'Y') {
						is[--last] = 'Y';
					}
					break;
				case 'T' :
					if (is[last - 1] == 'D' || is[last - 1] == 'R') {
						is[--last] = 'D';
					} else if (is[last - 1] == 'N') {
						--last;
					}
					break;
				case 'D' :
					if (is[last - 1] == 'R') {
						is[--last] = 'D';
					} else if (is[last - 1] == 'N') {
						--last;
					}
					break;
				case 'X' :
					if (is[last - 1] == 'I' || is[last - 1] == 'E') {
						is[last - 1] = 'C';
						trailingK = true;
					}
					break;
			}
		}

		// Step 4
		char[] code = new char[last + 2];
		char cur = code[0] = is[0];

		// Step 5
		int clast = 0;

		// Step 6 and 7
		// We don't perform the changes in is in these steps.
		// Instead, we store the current character in cur.
		for (int p = 1; p <= last; ++p) {
			switch (is[p]) {
				// Case ' ' not needed because is does not contain non-letters. 
				case 'E' :
					if (p < last && is[p + 1] == 'V') {
						is[p + 1] = 'F';
					}
					cur = 'A';
					break;
				case 'A' :
				case 'I' :
				case 'O' :
				case 'U' :
					cur = 'A';
					break;
				case 'Y' :
					if (p == last) {
						cur = 'Y';
					} else {
						cur = 'A';
					}
					break;
				case 'Q' :
					cur = 'G';
					break;
				case 'Z' :
					cur = 'S';
					break;
				case 'M' :
					cur = 'N';
					break;
				case 'K' :
					if (p < last && is[p + 1] == 'N') {
						cur = 'N';
					} else {
						cur = 'C';
					}
					break;
				case 'S' :
					if (p + 1 < last && is[p + 1] == 'C' && is[p + 2] == 'H') {
						// Could increment p instead and adapt code below.
						is[p + 1] = 'S';
						if (p == last + 2) {
							is[p + 2] = 'A';
						} else {
							is[p + 2] = 'S';
						}
					} else if (p < last && is[p + 1] == 'H') {
						if (p == last + 1) {
							is[p + 1] = 'A';
						} else {
							is[p + 1] = 'S';
						}
					}
					cur = 'S';
					break;
				case 'P' :
					if (p < last && is[p + 1] == 'H') {
						is[p + 1] = 'F';
						cur = 'F';
					} else {
						cur = 'P';
					}
					break;
				case 'G' :
					if (p + 1 < last && is[p + 1] == 'H' && is[p + 2] == 'T') {
						is[p + 1] = 'T';
						cur = 'T';
					} else {
						cur = 'G';
					}
					break;
				case 'D' :
					if (p < last && is[p + 1] == 'G') {
						cur = 'G';
					} else {
						cur = 'D';
					}
					break;
				case 'W' :
					if (p < last && is[p + 1] == 'R') {
						cur = 'R';
					} else if (cur == 'A') {
						// 'A' currently last character of code. No duplicates inserted.
						continue;
					} else {
						cur = 'W';
					}
					break;
				case 'H' :
					if (cur == 'A' && p < last && isVowel(is[p + 1])) {
						cur = 'H';
					} else {
						// 'A' currently last character of code. No duplicates inserted.
						continue;
					}
					break;
				default :
					cur = is[p];
			}
			if (code[clast] != cur) {
				code[++clast] = cur;
			}
		}
		if (trailingK && code[clast] != 'K') {
			code[++clast] = 'K';
		}

		// Step 8
		if (code[clast] == 'S') {
			--clast;
		}

		// Step 9
		if (clast > 0 && code[clast] == 'Y' && code[clast - 1] == 'A') {
			code[--clast] = 'Y';
		}

		// Step 10
		if (clast >= 0 && code[clast] == 'A') {
			--clast;
		}

		// Step 11
		if (clast >= 0 && code[0] == 'A') {
			code[0] = firstChar;
		}
		return new String(code, 0, clast + 1);
	}
}
