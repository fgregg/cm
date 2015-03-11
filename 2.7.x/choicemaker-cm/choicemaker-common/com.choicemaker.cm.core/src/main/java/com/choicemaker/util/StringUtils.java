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

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Utilities for dealing with <code>String</code>s.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:26:11 $
 */
public class StringUtils {
	/**
	 * Returns <code>true</code> iff <code>s</code> is not null and
	 * <code>s.length() > 0</code> and the string itself is not "NULL".
	 *
	 * @param s
	 *            The string to be tested.
	 * @return whether <code>s</code> is neither <code>null</code> nor
	 *         <code>""</code>.
	 */
	public static boolean nonEmptyString(String s) {
		return s != null && s.trim().length() > 0;
	}

	/**
	 * Returns the first character of a string or <code>null</code> if the
	 * latter is empty.
	 * 
	 * @param   s  The string from which the first character is to be returned.
	 * @return   The first character of <code>s</code> or <code>'\0'</code>
	 *            if the latter is empty.
	 */
	public static char getChar(String s) {
		if (s == null || s.length() == 0) {
			return '\0';
		} else {
			return s.charAt(0);
		}
	}

	/**
	 * Returns a boolean array whose length is the length of the
	 * input string and whose elements are true at the indices
	 * where the string contains digits.
	 * 
	 * @param s the input String
	 * @return a boolean array whose length is the length of the
	 * input string and whose elements are true at the indices
	 * where the string contains digits.
	 */
	public static boolean[] findNumbers(String s) {
		int len = s.length();
		boolean[] numbers = new boolean[len];
		for (int i = 0; i < len; ++i) {
			numbers[i] = Character.isDigit(s.charAt(i));
		}
		return numbers;
	}

	/**
	 * Returns the number of digits in <code>str</code>.
	 * 
	 * @param s the input String
	 * @return the number of digits found in <code>s</code>
	 */
	public static int countDigits(String s) {
		int len = s.length();
		int count = 0;
		for (int i = 0; i < len; ++i) {
			if (Character.isDigit(s.charAt(i))) {
				++count;
			}
		}
		return count;
	}

	/**
	 * Returns the number of digits in <code>s</code>, with a
	 * max of <code>limit</code>.  Once the <code>limit</code>-th 
	 * character is encountered, this method returns <code>limit</code>.
	 * 
	 * @param s the input String
	 * @param limit upper bound on the returned value
	 * @return the number of digits found in <code>s</code>
	 */
	public static int countDigits(String s, int limit) {
		if (s == null)
			return 0;
		int len = s.length();
		int count = 0;
		for (int i = 0;(count <= limit) && (i < len); ++i) {
			if (Character.isDigit(s.charAt(i))) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns the number of letters in <code>s</code>.
	 * 
	 * @param s the input String
	 * @return the number of letters found in <code>s</code>
	 */
	public static int countLetters(String s) {
		int len = s.length();
		int count = 0;
		for (int i = 0; i < len; ++i) {
			if (Character.isLetter(s.charAt(i))) {
				++count;
			}
		}
		return count;		
	}

	/**
	 * Returns true if one string begins or ends with the other.
	 * 
	 * @param s1 the first String
	 * @param s2 the second String
	 * @return true if one string begins or ends with the other.
	 */
	public static boolean overlap(String s1, String s2) {
		String sa, sb;
		if (s1.length() >= s2.length()) {
			sa = s1.trim();
			sb = s2.trim();
		} else {
			sa = s2.trim();
			sb = s1.trim();
		}
		return sa.startsWith(sb) || sa.endsWith(sb);
	}
	
	/**
	 * Returns the same input string but with the following replacements:
	 * <code>'.'</code> with <code>';'</code> and <code>','</code> with <code>spaces</code> 
	 * and simply remove <code>"'"</code>.
	 * Does not create leading spaces.
	 * Removes all trailing spaces.
	 * FIXME: move this mis-named method to AddressParser package com.choicemaker.cm.matching.en.us (the only place it is used)
	 * 
	 * @param s the input String
	 * @return a mangled version of the input
	 * @deprecated Will be removed in ChoiceMaker v3.0
	 */
	public static String removePunctuation(String s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			char ch;
			while (pos < len && (ch = s.charAt(pos)) != '.' && ch != ';' && ch != ',' && ch != '\'') {
				++pos;
			}
			if (pos == len) {
				// BUG: may return trailing spaces
				return s;
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					ch = s.charAt(pos);
					if (ch == '.' || ch == ';' || ch == ',') {
						if (out != 0) {
							res[out++] = ' ';
						}
					} else if (ch != '\'' && out != 0) {
						res[out++] = ch;
					}
					++pos;
				}
				while (out > 0 && res[out - 1] == ' ') {
					--out;
				}
				return new String(res, 0, out);
			}
		}
	}

	/**
	 * Remove the apostrophes from the input String.
	 * 
	 * @param s the input String
	 * @return the input with apostrophes removed
	 * @deprecated Will be removed in ChoiceMaker v3.0
	 */
	public static String removeApostrophies(String s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			while (pos < len && s.charAt(pos) != '\'') {
				++pos;
			}
			if (pos == len) {
				return s;
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					char ch = s.charAt(pos);
					if (ch != '\'') {
						res[out++] = ch;
					}
					++pos;
				}
				return new String(res, 0, out);
			}
		}
	}

	/**
	 * Returns a copy of <code>s</code> with all non-digit characters
	 * removed.  
	 * 
	 * @param s the input String
	 * @return a copy with non-digits removed
	 */
	public static String removeNonDigits(String s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			while (pos < len && Character.isDigit(s.charAt(pos))) {
				++pos;
			}
			if (pos == len) {
				return s;
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					char ch = s.charAt(pos);
					if (Character.isDigit(ch)) {
						res[out++] = ch;
					}
					++pos;
				}
				return new String(res, 0, out);
			}
		}
	}


	/** 
	 * Returns a copy of <code>s</code> with all non-digit characters
	 * removed and only keep the last n characters if s.legnth is greater than n.
	 * 
	 * @param s - the input String
	 * @param n - the maximum number of characters to return.  If s.length is
	 * greater than n, then return the last n letters.
	 * @return
	 */
	public static String removeNonDigitsMaxLength (String s, int n) {
		String str = removeNonDigits(s);
		int i = str.length() - n;
		if (i > 0)
			return str.substring(i);
		else
			return str;
	}


	/**
	 * Returns a copy of s with all non-(digit or letter) characters
	 * removed. 
	 * 
	 * @param s the input String
	 * @return a copy of the input with all non-digit and -letter characters
	 * removed
	 * @deprecated  Will be removed in ChoiceMaker v3.0
	 * @see #removeNonDigitsLetters(java.lang.String)
	 */
	public static String removeNonDigitsLetters(StringBuffer s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			while (pos < len && Character.isLetterOrDigit(s.charAt(pos))) {
				++pos;
			}
			if (pos == len) {
				return s.toString();
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					char ch = s.charAt(pos);
					if (Character.isLetterOrDigit(ch)) {
						res[out++] = ch;
					}
					++pos;
				}
				return new String(res, 0, out);
			}
		}
	}

	/**
	 * Returns a copy of s with all non-(digit or letter) characters
	 * removed. If the input is <code>null</code>, returns <code>null</code>.
	 * 
	 * @param s the input string
	 * @return a version of s with non-digits and -letters removed
	 */
	public static String removeNonDigitsLetters(String s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			while (pos < len && Character.isLetterOrDigit(s.charAt(pos))) {
				++pos;
			}
			if (pos == len) {
				return s.toString();
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					char ch = s.charAt(pos);
					if (Character.isLetterOrDigit(ch)) {
						res[out++] = ch;
					}
					++pos;
				}
				return new String(res, 0, out);
			}
		}
	}

	/**
	 * Returns a copy of s with all non-letters
	 * removed. If the input is <code>null</code>, returns <code>null</code>.
	 * 
	 * @param s the input string
	 * @return a version of s with non-letters removed
	 */
	public static String removeNonLetters(String s) {
		if (s == null) {
			return null;
		} else {
			int len = s.length();
			int pos = 0;
			while (pos < len && Character.isLetter(s.charAt(pos))) {
				++pos;
			}
			if (pos == len) {
				return s.toString();
			} else {
				char[] res = new char[len];
				for (int i = 0; i < pos; ++i) {
					res[i] = s.charAt(i);
				}
				int out = pos;
				while (pos < len) {
					char ch = s.charAt(pos);
					if (Character.isLetter(ch)) {
						res[out++] = ch;
					}
					++pos;
				}

				return new String(res, 0, out);
			}
		}
	}

	/**
	 * Returns a copy of s with everything except letters and spaces
	 * removed. If the input is <code>null</code>, returns <code>null</code>.
	 * 
	 * @param in the input string
	 * @return a string with only letters and spaces
	 */
	public static String keepLettersAndSpaces(String in) {
		if (in == null) {
			return null;	
		}
		
		int len = in.length();
		StringBuffer out = new StringBuffer(len);
		boolean lastSpace = true;
		for (int i = 0; i < len; i++) {
			char c = in.charAt(i);
			if (Character.isLetter(c)) {
				out.append(c);
				lastSpace = false;
			} else if (!lastSpace) {
				out.append(' ');
				lastSpace = true;
			}
		}
		
		return out.toString().trim();
	}

	/**
	 * Returns true iff <code>s</code> is non-null and contains at least one
	 * character that is not a letter, digit, or underscore character.
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one non-word character
	 */
	public static boolean containsNonWordChars(String s) {
		if (s == null) {
			return false;	
		}
		
		int len = s.length();
		int pos = 0;
		while (pos < len) {
			char c = s.charAt(pos);
			if (!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
				return true;
			}
			pos++;
		} 
		return false;
	}
	
	/**
	 * Returns true iff <code>s</code> is non-null and contains non-digit characters.
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one character that is not a digit.
	 */
	public static boolean containsNonDigits(String s) {
		if (s == null) {
			return false;	
		}
		
		int len = s.length();
		int pos = 0;
		while (pos < len) {
			char c = s.charAt(pos);
			if (!Character.isDigit(c)) {
				return true;
			}
			pos++;
		} 
		return false;
	}

	/**
	 * Returns true iff <code>s</code> is non-null and contains non-letter characters.
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one character that is not a letter
	 */
	public static boolean containsNonLetters(String s) {
		if (s == null) {
			return false;	
		}
		
		int len = s.length();
		int pos = 0;
		while (pos < len) {
			char c = s.charAt(pos);
			if (!Character.isLetter(c)) {
				return true;
			}
			pos++;
		} 
		return false;
	}

	/**
	 * Returns true iff <code>s</code> is non-null and contains at least one letter.
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one letter
	 */
	public static boolean containsLetters(String s) {
		if (s == null) {
			return false;
		}
		
		for (int i = s.length() - 1; i >= 0; i--) {
			if (Character.isLetter(s.charAt(i))) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true iff <code>s</code> is non-null and contains at least one letter.
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one letter
	 */	
	public static boolean containsDigits(String s) {
		if (s == null) {
			return false;
		}
		
		for (int i = s.length() - 1; i >= 0; i--) {
			if (Character.isDigit(s.charAt(i))) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true iff <code>s</code> is non-null and contains at least one letter or digit
	 * 
	 * @param s the input String
	 * @return true iff <code>s</code> contains at least one letter or digit
	 */
	public static boolean containsDigitsOrLetters(String s) {
		if (s == null) {
			return false;
		}
		
		for (int i = s.length() - 1; i >= 0; i--) {
			char c = s.charAt(i);
			if (Character.isLetter(c) || Character.isDigit(c)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String trimLeadingZeros(String s) {
		if (s == null) {
			return null;
		} else {
			int index = 0;
			while (index < s.length() && s.charAt(index) == '0') {
				index++;
			}
			
			if (index > 0) {
				return s.substring(index);	
			} else {
				return s;
			}
		}
	}
	
	/**
	 * Returns a version of <code>s</code> with zeros appended to the
	 * left until it is at least <code>len</code> characters long.
	 * If <code>s</code> is already <code>len</code> characters long or longer,
	 * returns <code>s</code> in its entirety.
	 * 
	 * @param s the String to pad
	 * @param len the desired length
	 * @return a version of <code>s</code> padded to length <code>len</code> 
	 *   with zeros if necessary
	 */
	public static String padLeft(String s, int len) {
		return padLeft(s, len, '0');
	}

	/**
	 * Returns a version of <code>s</code> with zeros appended to the
	 * left until it is at least <code>len</code> characters long.
	 * If <code>s</code> is already <code>len</code> characters long or longer,
	 * returns <code>s</code> in its entirety.
	 * 
	 * For example,
	 * <p>
	 * <code>padLeft(&quot;8675309&quot, 10, '0')</code> 
	 * </p>
	 * returns <code>&quot;0008675309&quot;</code>.
	 * 
	 * @param s the String to pad
	 * @param len the desired length
	 * @param c the char to pad with
	 * @return a version of <code>s</code> padded to length <code>len</code> 
	 *   with <code>c</code> if necessary
	 */
	public static String padLeft(String s, int len, char c) {
		int cur = s.length();
		if (cur < len) {
			StringBuffer buff = new StringBuffer(len);
			while (cur++ < len) {
				buff.append(c);	
			}
			buff.append(s);
			return buff.toString();
		} else {
			return s;	
		}
	}

	/**
	 * Returns a version of <code>s</code> with zeros appended to the
	 * right until it is at least <code>len</code> characters long.
	 * If <code>s</code> is already <code>len</code> characters long or longer,
	 * returns <code>s</code> in its entirety.
	 * 
	 * @param s the String to pad
	 * @param len the desired length
	 * @return a version of <code>s</code> padded to length <code>len</code> 
	 *   with zeros if necessary
	 */
	public static String padRight(String s, int len) {
		return padRight(s, len, '0');
	}

	/**
	 * Returns a version of <code>s</code> with zeros appended to the
	 * right until it is at least <code>len</code> characters long.
	 * If <code>s</code> is already <code>len</code> characters long or longer,
	 * returns <code>s</code> in its entirety.
	 * 
	 * @param s the String to pad
	 * @param len the desired length
	 * @param c the char to pad with
	 * @return a version of <code>s</code> padded to length <code>len</code> 
	 *   with <code>c</code> if necessary
	 */
	public static String padRight(String s, int len, char c) {
		int cur = s.length();
		if (cur < len) {
			StringBuffer buff = new StringBuffer(len);
			buff.append(s);
			while (cur++ < len) {
				buff.append(c);	
			}
			return buff.toString();
		} else {
			return s;	
		}
	}

	/**
	 * Return the <code>long</code> value of input <code>String</code> <code>s</code>.
	 * If <code>s</code> is null or has no length, returns <code>-1</code>.  Otherwise,
	 * returns <code>Long.parseLong(s)</code>.
	 * 
	 * @param s the string for which to return the long value
	 * @return the <code>long</code> value of s
	 * @throws NumberFormatException if s cannot be converted to a long.
	 */
	public static long parseLong(String s) {
		if (s == null || s.length() == 0) {
			return -1;
		} else {
			return Long.parseLong(s);
		}
	}

	/**
	 * Return the <code>long</code> value of input <code>String</code> <code>s</code>
	 * after removing all non-digits.  If <code>s</code> is null or has no digits, 
	 * this method returns <code>-1</code>.
	 * 
	 * updated by PC on 2/12/07.  Handles the case where the string is longer than
	 * LONG.MAXVALUE.
	 * 
	 * @param s the string to convert to a <code>long</code>
	 * @return the <code>long</code> value of s
	 */
	public static long parseLongString(String s) {
		return parseLong(removeNonDigitsMaxLength(s,18));
	}

	/**
	 * Returns the number of position-wise character matches between 
	 * the two input Strings.  For example
	 * <p>
	 * <code>numMatchingCharacters(&quot;ABCD&quot;, &quot;AXXD&quot;)</code>
	 * </p>
	 * returns 2.
	 * 
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return the number of position-wise character matches between <code>s1</code> and <code>s2</code>
	 */
	public static int numMatchingCharacters(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return 0;
		}
		int len = Math.min(s1.length(), s2.length());
		int res = 0;
		for (int i = 0; i < len; ++i) {
			if (s1.charAt(i) == s2.charAt(i)) {
				++res;
			}
		}
		return res;
	}

	/**
	 * Splits <code>s</code> on whitespace using a <code>java.util.StringTokenizer</code>.
	 * 
	 * @param s String to split
	 * @return an array of <code>s</code>'s split pieces
	 */
	public static String[] split(String s) {
		return split(s, " ");	
	}

	/**
	 * Splits <code>s</code> on <code>delim</code> using a <code>java.util.StringTokenizer</code>.
	 * 
	 * @param s String to split
	 * @param delim delimiter on which to split
	 * @return an array of <code>s</code>'s split pieces
	 */
	public static String[] split(String s, char delim) {
		return split(s, String.valueOf(delim));
	}

	/**
	 * Splits <code>s</code> on <code>delim</code> using a <code>java.util.StringTokenizer</code>.
	 * 
	 * @param s String to split
	 * @param delim delimiter on which to split
	 * @return an array of <code>s</code>'s split pieces
	 */
	public static String[] split(String s, String delim) {
		if (s == null) {
			return new String[0];	
		} else {
			StringTokenizer t = new StringTokenizer(s, delim);
			String[] ret = new String[t.countTokens()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = t.nextToken();
			}
			return ret;
		}
	}

	public static String[] splitOnNonLetters(String s) {
		if (s == null) {
			return new String[0];
		} else {
			ArrayList out = new ArrayList();
			StringBuffer buff = new StringBuffer();
			int len = s.length();
			int idx = 0;
			while (true) {
				if (idx >= len) {
					if (buff.length() > 0) {
						out.add(buff.toString());
					}
					
					break;
				}
			
				char c = s.charAt(idx);
			
				if (!Character.isLetter(c)) {
					if (buff.length() > 0) {
						out.add(buff.toString());
					}
					buff.setLength(0);
				} else {
					buff.append(c);
				}
				
				idx++;
			}
		
			return (String[]) out.toArray(new String[out.size()]);	
		}
	}
	
	

	/**
	 * Joins <code>s1</code> and <code>s2</code> with a space.
	 * 
	 * @param s1 the first String
	 * @param s2 the second String
	 * @return <code>s1 + &quot; &quot; + s2</code>
	 */
	public static String join(String s1, String s2) {
		return join(new String[] {s1, s2});
	}

	/**
	 * Joins <code>s1</code>, <code>s2</code>, and <code>s3</code> with spaces.
	 * 
	 * @param s1 the first String
	 * @param s2 the second String
	 * @param s3 the third String
	 * @return <code>s1 + &quot; &quot; + s2 + &quot; &quot; + s3</code>
	 */	
	public static String join(String s1, String s2, String s3) {
		return join(new String[] {s1, s2, s3});		
	}

	/**
	 * Joins <code>s1</code>, <code>s2</code>, <code>s3</code>, and <code>s4</code> with spaces.
	 * 
	 * @param s1 the first String
	 * @param s2 the second String
	 * @param s3 the third String
	 * @param s4 the fourth String
	 * @return <code>s1 + &quot; &quot; + s2 + &quot; &quot; + s3 + &quot; &quot; + s4</code>
	 */	
	public static String join(String s1, String s2, String s3, String s4) {
		return join(new String[] {s1, s2, s3, s4});		
	}

	/**
	 * Joins the elements of <code>s</code> with spaces.  Null elements are skipped and
	 * do not cause space to be inserted into the returned String.
	 * 
	 * @param s an array of Strings to join
	 * @return the elements of <code>s</code> joined with spaces.
	 */
	public static String join(String[] s) {
		return join(s, " ");
	}

	/**
	 * Joins the elements of <code>s</code> with <code>delim</code>.  Null elements are
	 * skipped and do not cause the delimter to be inserted.
	 * 
	 * @param s an array of Strings to join
	 * @param delim the delimiter
	 * @return the elements of <code>s</code> joined by <code>delim</code>
	 */
	public static String join(String[] s, char delim) {
		return join(s, String.valueOf(delim));
	}

	/**
	 * Joins the elements of <code>s</code> with <code>delim</code>.  Null elements are
	 * skipped and do not cause the delimter to be inserted.
	 * 
	 * @param s an array of Strings to join
	 * @param delim the delimiter
	 * @return the elements of <code>s</code> joined by <code>delim</code>
	 */	
	public static String join(String[] s, String delim) {
		boolean started = false;
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < s.length; i++) {
			if (nonEmptyString(s[i])) {
				if (started) {
					buff.append(delim);
					buff.append(s[i]);
				} else {
					buff.append(s[i]);
					started = true;
				}
			}
		}
		return buff.toString();		
	}

}
