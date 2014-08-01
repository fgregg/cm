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
 * This class provides static methods to generate names. A name is
 * represented with an interned string.
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public final class Names {

	/** an illegal name
	 */
	public static final String ERROR = "<error>".intern();

	private Names() {
	}

	/** create a new name from a string
	 */
	public static String fromString(String s) {
		return s.intern();
	}

	/** create a new name from a character array in "Source format"
	 */
	public static String fromArray(char[] cs, int offset, int length) {
		return new String(cs, offset, length).intern();
	}

	/** convert a string in "Source format" into "Internal format";
	 *  i.e. 16-bit characters
	 */
	public static char[] toInternal(String s) {
		char[] cs = new char[s.length()];
		s.getChars(0, cs.length, cs, 0);
		return toInternal(cs, 0, cs.length);
	}

	/** convert a character array in "Source format" into "Internal format";
	 *  i.e. 16-bit characters
	 */
	public static char[] toInternal(char[] cs, int offset, int length) {
		char[] dest = new char[length];
		int j = 0;
		int i = 0;
		int limit = offset + length;
		outer : while (i < limit)
			if ((cs[i] == '\\') && ((i + 5) < limit) && (cs[i + 1] == 'u')) {
				int num = 0;
				for (int k = 2; k < 6; k++) {
					int n = char2int(cs[i + k]);
					if (n < 0) {
						dest[j++] = cs[i++];
						continue outer;
					}
					num = num * 16 + n;
				}
				dest[j++] = (char) num;
				i += 6;
			} else
				dest[j++] = cs[i++];
		char[] res = new char[j];
		System.arraycopy(dest, 0, res, 0, j);
		return res;
	}

	/** convert a string into "Source format"; i.e. characters outside of
	 *  [0..127] are represented by unicode escapes \ u XXXX
	 */
	public static char[] toSource(String s) {
		char[] cs = new char[s.length()];
		s.getChars(0, cs.length, cs, 0);
		return toSource(cs, 0, cs.length);
	}

	/** convert a charcter array in "Internal format" into "Source format"; i.e.
	 *  characters outside of [0..127] are represented by unicode escapes \ u XXXX
	 */
	public static char[] toSource(char[] cs, int offset, int length) {
		char[] dest = new char[length * 6];
		int j = 0;
		int limit = offset + length;
		for (int i = offset; i < limit; i++) {
			char ch = cs[i];
			if ((' ' <= ch) && (ch <= 127))
				dest[j++] = ch;
			else {
				dest[j++] = '\\';
				dest[j++] = 'u';
				dest[j++] = int2char((ch >> 12) & 0xF);
				dest[j++] = int2char((ch >> 8) & 0xF);
				dest[j++] = int2char((ch >> 4) & 0xF);
				dest[j++] = int2char(ch & 0xF);
			}
		}
		char[] res = new char[j];
		System.arraycopy(dest, 0, res, 0, j);
		return res;
	}

	static char int2char(int x) {
		return (char) ((x <= 9) ? (x + '0') : (x - 10 + 'A'));
	}

	private static int char2int(char ch) {
		if (('0' <= ch) && (ch <= '9'))
			return ch - '0';
		else if (('A' <= ch) && (ch <= 'F'))
			return ch - 'A' + 10;
		else if (('a' <= ch) && (ch <= 'f'))
			return ch - 'a' + 10;
		else
			return -1;
	}
}
