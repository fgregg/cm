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
 * Utility class to transliterate characters in the ANSI character set
 * (codes 128 through 255, inclusive) to their equivalents in the 26-letter 
 * Roman alphabet.
 * 
 * <p>
 * Most characters in the range 192 through 255 map nicely to a single character,
 * some map two two characters, for example, the 'ae' ligature.  However, the
 * multiplication symbol (looks like an 'x') is mapped to '*', and the division symbol 
 * (hyphen with dots above and below) is mapped to '/'. 
 * 
 * <p>
 * The range 128 through 191 has only a few alphabetic characters, which are mapped
 * to their roman equivalents.  Also, the code 160 (non-breaking space) maps to 
 * ASCII 32, a regular space.  The remaining characters in the range are mapped to
 * spaces.
 * 
 * <p>
 * Characters with code > 255 are also mapped to space.
 * 
 * @author Adam Winkel
 */
public final class Latin1 {

	/*
	public static IntValuedHashMap unknownCharMap = new IntValuedHashMap();

	public static void printUnknownCharMap() {
		synchronized(unknownCharMap) {
			for (Iterator it = new TreeSet(unknownCharMap.keySet()).iterator(); it.hasNext(); ) {
				Object key = it.next();
				System.out.println(key + " (" + ((char)((Integer)key).intValue()) + "):\t" + unknownCharMap.get(key));
			}
		}
	}
	*/

	/**
	 * Maps alphabetic characters in the range [128, 255] to
	 * their equivalent ASCII character or characters.
	 * 
	 * By default, replaces unknown (or ignored) characters with
	 * spaces.
	 * 
	 * @param s the input String
	 * @return a transliterated version of <code>s</code>
	 */
	public static String toAscii(String s) {
		return toAscii(s, true);
	}
	
	/**
	 * Maps alphabetic (and selected non-alphabetic) characters in the range 
	 * [128, 255] to their equivalence ASCII character or characters.
	 * 
	 * @param s the input String
	 * @param replaceUnknownCharsWithSpace if true, the return value will have spaces 
	 * where unknown characters used to be.  Otherwise, unknown characters are
	 * simply removed
	 * @return a transliterated version of <code>s</code>
	 */
	public static String toAscii(String s, boolean replaceUnknownCharsWithSpace) {
		int len = s.length();
		StringBuffer buff = new StringBuffer(len);
		
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			
			if (c < 128) {
				buff.append(c);
			} else if (c > 255) {
				//System.err.println("Character > 255 processed (" + (int)c + "): " + c + "\n\t" + s);
				
				if (replaceUnknownCharsWithSpace) {
					buff.append(' ');
				}
				
				//synchronized(unknownCharMap) {
				//	unknownCharMap.increment(new Integer((int)c));
				//}
				
			// NOTE: 128 thru 129 not converted (unknown currency symbol, unused)
			} else if (c == 130) {
				// baseline single quote
				buff.append('\'');
			// NOTE: 131 not converted (florin symbol)
			} else if (c == 132) {
				// baseline double quote
				buff.append('"');
			// NOTE: 133 thru 137 not converted (single and double daggers, circumflex (carrot), and per mil)
			} else if (c == 138) {
				buff.append('S');
			// NOTE: 139 not converted (left single guillemet)
			} else if (c == 140) {
				buff.append("Oe");
			// NOTE: 141 not converted (unused)
			} else if (c == 142) {
				buff.append('Z');
			// NOTE: 143 thru 144 not converted (both unused)
			} else if (c >= 145 && c <= 146) {
				// open and close single quotes
				buff.append('\'');
			} else if (c >= 147 && c <= 148) {
				// open and close double quotes
				buff.append('"');
			// NOTE: 149 not converted (large bullet)
			} else if (c >= 150 && c <= 151) {
				// en dash, em dash, respectively
				buff.append('-');
			// NOTE: 152 thru 153 not converted (tilde, unregistered trademark (TM))
			} else if (c == 154) {
				buff.append('s');
			// NOTE: 155 not converted (right single guillemet)
			} else if (c == 156) {
				buff.append("oe");
			// NOTE: 157 not converted (unused)
			} else if (c == 158) {
				buff.append('z');
			} else if (c == 159) {
				buff.append('Y');
			} else if (c == 160) {
				buff.append(' ');
			// NOTE: 161 thru 191 not converted (some are converted below)
			} else if (c >= 192 && c <= 197) {
				buff.append('A');
			} else if (c == 198) {
				buff.append("Ae");
			} else if (c == 199) {
				buff.append('C');
			} else if (c >= 200 && c <= 203) {
				buff.append('E');
			} else if (c >= 204 && c <= 207) {
				buff.append('I');
			} else if (c == 208) {
				buff.append("Th");
			} else if (c == 209) {
				buff.append('N');
			} else if (c >= 210 && c <= 214) {
				buff.append('O');
			} else if (c == 215) {
				// multiplication symbol
				buff.append('*');
			} else if (c == 216) {
				buff.append('O');
			} else if (c >= 217 && c <= 220) {
				buff.append('U');
			} else if (c == 221) {
				buff.append('Y');
			} else if (c == 222) {
				buff.append('D');
			} else if (c == 223) {
				buff.append("ss");
			} else if (c >= 224 && c <= 229) {
				buff.append('a');
			} else if (c == 230) {
				buff.append("ae");
			} else if (c == 231) {
				buff.append('c');
			} else if (c >= 232 && c <= 235) {
				buff.append('e');
			} else if (c >= 236 && c <= 239) {
				buff.append('i');
			} else if (c == 240) {
				buff.append("th");
			} else if (c == 241) {
				buff.append('n');
			} else if (c >= 242 && c <= 246) {
				buff.append('o');
			} else if (c == 247) {
				// division symbol
				buff.append('/');
			} else if (c == 248) {
				buff.append('o');
			} else if (c >= 249 && c <= 252) {
				buff.append('u');
			} else if (c == 253) {
				buff.append('y');
			} else if (c == 254) {
				buff.append('d');
			} else if (c == 255) {
				buff.append('y');
			
			// NOTE: the following are questionable transliterations for some characters between 161 and 191
			} else if (c == 170) {
				// feminine ordinal (looks like a superscript 'a')
				buff.append('a');
			} else if (c == 173) {
				// soft hyphen
				// do not append anything (soft hyphen indicates a legal place to break a word between multi lines)
			} else if (c == 176) {
				// degree symbol
				buff.append('o');
			} else if (c == 178) {
				// superscript 2
				buff.append('2');
			} else if (c == 179) {
				// superscript 3
				buff.append('3');
			} else if (c == 184) {
				// cedilla
				// do not append anything!
			} else if (c == 185) {
				// superscript 1
				buff.append('1');
			} else if (c == 186) {
				// masculine ordinal (looks like a superscript 'o')
				buff.append('o');
			
			} else {
				//System.err.println("Unknown character > 127 and < 192 (" + (int) c + "): " + c + "\n\t" + s);

				if (replaceUnknownCharsWithSpace) {
					buff.append(' ');
				}

				//synchronized(unknownCharMap) {
				//	unknownCharMap.increment(new Integer((int)c));
				//}

			}
		}
		
		return buff.toString();
	}

	private Latin1() { }

}
