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
 * Modified soundex matching method.
 * 
 * Soundex is a standard phoneticization algorithm. It is a very simple 
 * algorithm that works especially well for English. Many databases have a 
 * built-in Soundex function that can be used for creating derived fields for 
 * blocking.
 *
 * @author    Martin Buechi (Martin.Buechi@choicemaker.com)
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:01 $
 */
public final class Soundex {

	/**
	 * Hide constructor.
	 */
	private Soundex() {
	}

	/**
	 * Gives the soundex digit corresponding to the input letter.
	 *
	 * @param   letter The letter 'A'..'Z' in upper case to be transformed.
	 * @return  The soundex digit.
	 */
	private static int charToSoundex(char letter) {
		switch (letter) {
			case 'B' :
			case 'F' :
			case 'P' :
			case 'V' :
				return 1;
			case 'C' :
			case 'G' :
			case 'J' :
			case 'K' :
			case 'Q' :
			case 'S' :
			case 'X' :
			case 'Z' :
				return 2;
			case 'D' :
			case 'T' :
				return 3;
			case 'L' :
				return 4;
			case 'M' :
			case 'N' :
				return 5;
			case 'R' :
				return 6;
			default :
				return -1;
		}
	}

	/**
	 * Modified soundex coding procedure.
	 *
	 * Based on the soundex specification with the following modifications:
	 * <ol>
	 *     <li>The returned string is alwasy numberOfSoundexDigits + 1
	 *         characters long. (If the input string s is null or "", the
	 *         code is "999...".</li>
	 * </ol>
	 *
	 * @param   s the input string.
	 * @return  the Soundex of s to 3 Digits accuracy.
	 */
	public static String soundex(String s) {
		return soundex(s, 3);
	}

	/**
	 * Modified soundex coding procedure.
	 *
	 * Based on the soundex specification with the following modifications:
	 * <ol>
	 *     <li>The returned string is alwasy numberOfSoundexDigits + 1
	 *         characters long. (If the input string s is null or "", the
	 *         code is "999...".</li>
	 * </ol>
	 *
	 * @param   s the input string.
	 * @param   numberOfSoundexDigits Number of digits. Must be greater or equal 
	 * to 0.
	 * @return  the Soundex of s to numDigits accuracy.
	 */
	public static String soundex(String s, int numberOfSoundexDigits) {
		char[] res = new char[numberOfSoundexDigits + 1];
		if (s == null || s.length() == 0) {
			for (int i = 0; i <= numberOfSoundexDigits; ++i) {
				res[i] = '9';
			}
		} else {
			res[0] = Character.toUpperCase(s.charAt(0));
			if (numberOfSoundexDigits > 0) {
				int resLast = 0;
				int lastDigit = -1;
				int len = s.length();
				for (int i = 1; i < len; ++i) { // contains break
					char c = s.charAt(i);
					// Case conversion. Only need to handle 'a'..'z'.
					if ('a' <= c) {
						c -= ('a' - 'A');
					}
					if ('A' <= c && c <= 'Z') {
						int curDigit = charToSoundex(c);
						if (curDigit != -1 && curDigit != lastDigit) {
							res[++resLast] = (char) (curDigit + '0');
							if (resLast == numberOfSoundexDigits) {
								break;
							}
							lastDigit = curDigit;
						}
					}
				}
				while (resLast < numberOfSoundexDigits) {
					res[++resLast] = '0';
				}
			}
		}
		return new String(res);
	}
}
