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
package com.choicemaker.cm.matching.en.us;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.choicemaker.util.StringUtils;

/**
 * The street parser is used to split a street address into street name, street 
 * number, and apartment number. A street parser is instantiated with the street 
 * address. The broken up name parts can be accessed via various accessor methods
 * 
 * @deprecated use the generic CFG Parser interface
 * @see com.choicemaker.cm.matching.cfg.Parsers
 * 
 * @author    S. Yoakum-Stover
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:16:00 $
 */
public class StreetParser {
	private static Logger logger = Logger.getLogger(StreetParser.class);
	public static Collection directions = new HashSet();
	public static Collection streetTypes = new HashSet();
	public static Collection ordinalExtensions = new HashSet();
	public static Map standardDirections = new HashMap();
	public static Map standardOrdinalTypes = new HashMap();
	public static Map commonStreetNameShortHands = new HashMap();
	public static Map standardStreetTypes = new HashMap();

	public static final int DIGITS = 0;
	public static final int DIGITS_PLUS = 1;
	public static final int DIRECTION = 2;
	public static final int WORD = 3;
	public static final int STREET_TYPE = 4;
	public static final int HYPHEN = 5;
	public static final int ORDINAL_EXTENSION = 6;

	private String[] tokens;
	private int[] tokenTypes;
	private String houseNumber = "";
	private String cleanStreet = "";
	private String apartment = "";

	public static StreetParser parse(String s) {
		return new StreetParser(s);
	}

	public StreetParser(String s) {
		if (StringUtils.nonEmptyString(s)) {
			parseStreetString(s);
			findNumberAndStreet();
			tokens = null;
			tokenTypes = null;
		}
	}

	/**
	 * @return the house number.
	 */
	public String getHouseNumber() {
		return houseNumber;
	}

	/**
	 * @return the street name.
	 */
	public String getStreetName() {
		return cleanStreet;
	}

	/**
	 * @return the apartment number.
	 */
	public String getApartment() {
		return apartment;
	}

	private void findNumberAndStreet() {
		int numToks = tokens.length;
		if (numToks == 0) {
			return;
		}
		if (numToks == 1 && (tokenTypes[0] != STREET_TYPE)) {
			cleanStreet = tokens[0];
			return;
		}

		boolean haveStreet = false;
		boolean haveStreetNum = false;
		StringBuffer st = new StringBuffer();
		StringBuffer apt = new StringBuffer();

		for (int i = 0; i < numToks; i++) {
			//leading number is house number except when followed by a street-type or ordinal extension.
			if (i == 0
				&& (tokenTypes[i] == DIGITS)
				&& (tokenTypes[i + 1] != STREET_TYPE)
				&& (tokenTypes[i + 1] != ORDINAL_EXTENSION)) {
				houseNumber = tokens[i];
			} else if (tokenTypes[i] == STREET_TYPE) {
				//this means we've reached the end of the street name.
				haveStreet = true;
				//check if next token is a direction, if so include it in the street.
				int q = i;
				while ((q + 1 < numToks) && (tokenTypes[q + 1] == DIRECTION)) {
					st.append(tokens[q + 1] + " ");
					q++;
				}
				i = q;
			} else if (st.length() > 0 && (tokens[i].equals("#") || tokens[i].startsWith("APT"))) {
				//this means we've reached the end of the street name.
				haveStreet = true;
			} else if (tokenTypes[i] == ORDINAL_EXTENSION) {
				//do nothing
			} else {
				if (!haveStreet
					&& !haveStreetNum
					&& (tokenTypes[i] == DIRECTION || tokenTypes[i] == DIGITS || tokens[i].length() > 1)) {
					st.append(tokens[i] + " ");
					if (tokenTypes[i] == DIGITS) {
						haveStreetNum = true;
					} else if (i + 1 < numToks && tokenTypes[i] != DIRECTION && tokenTypes[i + 1] == DIGITS) {
						haveStreet = true;
					}
				} else {
					apt.append(tokens[i]);
					if (houseNumber.length() == 0 && tokenTypes[i] == DIGITS) {
						houseNumber = tokens[i];
					}
				}
			}
		}
		cleanStreet = sbToTrimmedString(st);
		apartment = aptNormalize(apt.toString());
		//System.err.println("House #: " + houseNumber + " Street: " + cleanStreet + " Apt: " + apartment);
	}

	private String sbToTrimmedString(StringBuffer b) {
		int l = b.length();
		int start = 0;
		while (start < l && Character.isWhitespace(b.charAt(start))) {
			++start;
		}
		int end = b.length();
		while (end > start && Character.isWhitespace(b.charAt(end - 1))) {
			--end;
		}
		return b.substring(start, end);
	}

	private void parseStreetString(String s) {
		String str = StringUtils.removePunctuation(s);
		// BUGFIX rphall 2008-07-13
		// StringUtils.removePunctuation sometimes does not  remove trailing space
		str = str == null ? null : str.trim();
		// END BUGFIX

		//tokenize the string, unSmush if necessary
		StringTokenizer sTok = new StringTokenizer(str);
		int numToks = sTok.countTokens();
		if (numToks == 1) {
			str = unSmush(str);
			sTok = new StringTokenizer(str);
		}

		numToks = sTok.countTokens();
		//System.err.println("Num toks = " + numToks);

		tokens = new String[numToks];
		for (int i = 0; i < numToks; i++) {
			tokens[i] = sTok.nextToken();
		}
		tokenTypes = identifyTokens(tokens);
		//System.err.println("After identification.");
		//for(int k=0; k<numToks; k++) System.err.println(tokens[k] + " " + tokenTypes[k]);

		standardize();
		numToks = tokens.length;
		//System.err.println("After standardization.");
		//for(int k=0; k<numToks; k++) System.err.print(tokens[k] + " (" + tokenTypes[k] + ") ");
		//System.err.println();
	}

	private void standardize() {
		int numToks = tokens.length;

		for (int i = 0; i < numToks; i++) {
			boolean hasPrevious = i != 0;
			boolean hasNext = i != numToks - 1;
			boolean hasNextNext = i != numToks - 2;

			switch (tokenTypes[i]) {
				case DIGITS :
					//do nothing
					break;
				case DIGITS_PLUS :
					//check for ##-## pattern
					int index = tokens[i].indexOf("-");
					if (index > 0) {
						String s = tokens[i].substring(0, index) + tokens[i].substring(index + 1);
						tokens[i] = s;
						tokenTypes[i] = DIGITS;
					}
					//check for ##xxx
					String uS = unSmush(tokens[i]);
					StringTokenizer additionalToks = new StringTokenizer(uS);
					int count = additionalToks.countTokens();
					if (count > 1) {
						String[] subToks = new String[count];
						//System.err.print("Subtoks are: ");
						for (int m = 0; m < count; m++) {
							subToks[m] = additionalToks.nextToken();
							//System.err.print(subToks[m] + " " );
						}
						//System.err.println();
						int[] subTokTypes = identifyTokens(subToks);

						String[] expandedTokens = new String[numToks + count - 1];
						int[] expandedTokenTypes = new int[numToks + count - 1];
						int k;
						for (k = 0; k < i; k++) {
							expandedTokens[k] = tokens[k];
							expandedTokenTypes[k] = tokenTypes[k];
							//System.err.println("A tok[ " + k + " ] = " + expandedTokens[k] + " type: " + expandedTokenTypes[k]);
						}
						for (int n = 0; n < count; n++) {
							expandedTokens[k + n] = subToks[n];
							expandedTokenTypes[k + n] = subTokTypes[n];
							//System.err.println("B tok[ " + (k+n) + " ] = " + expandedTokens[k+n] + " type: " + expandedTokenTypes[k+n]);
						}
						for (int j = i + 1; j < numToks; j++) {
							expandedTokens[j + count - 1] = tokens[j];
							expandedTokenTypes[j + count - 1] = tokenTypes[j];
							//System.err.println("C tok[ " + (j+count-1) + " ] = " + expandedTokens[j+count-1] + " type: " + expandedTokenTypes[j+count-1]);
						}
						tokens = expandedTokens;
						tokenTypes = expandedTokenTypes;
						numToks = expandedTokens.length;
						//for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
						//System.err.println();
						//System.err.println("Subtok done.");
					}
					break;
				case HYPHEN :
					//check for ## - ## pattern
					if (hasPrevious
						&& hasNextNext
						&& tokenTypes[i - 1] == DIGITS
						&& i + 1 < tokenTypes.length
						&& tokenTypes[i + 1] == DIGITS) {
						//                         System.err.println("***Hyphen pattern.");
						String[] newTokens = new String[numToks - 2];
						int[] newTokenTypes = new int[numToks - 2];
						int k;
						for (k = 0; k < i - 1; k++) {
							newTokens[k] = tokens[k];
							newTokenTypes[k] = tokenTypes[k];
						}
						newTokens[k] = tokens[i - 1] + tokens[i + 1];
						newTokenTypes[k] = DIGITS;
						for (int j = k + 1; j < numToks - 2; j++) {
							newTokens[j] = tokens[j + 2];
							newTokenTypes[j] = tokenTypes[j + 2];
						}
						tokens = newTokens;
						tokenTypes = newTokenTypes;
						numToks = tokens.length;
						//                         for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
						//                         System.err.println();
					}
					break;
				case DIRECTION :
					break;
				case ORDINAL_EXTENSION :
					//do nothing
					break;
				case WORD :
					//check for "THST" or "THDR"
					if (tokens[i].equals("THST") || tokens[i].equals("THDR")) {
						String[] newTokens = new String[numToks + 1];
						int[] newTokenTypes = new int[numToks + 1];
						int k;
						for (k = 0; k < i; k++) {
							newTokens[k] = tokens[k];
							newTokenTypes[k] = tokenTypes[k];
						}
						newTokens[k] = "TH";
						newTokenTypes[k] = ORDINAL_EXTENSION;
						newTokens[k + 1] = (tokens[i].equals("THST")) ? "ST" : "DR";
						newTokenTypes[k + 1] = STREET_TYPE;
						for (int j = k + 2; j < numToks + 1; j++) {
							newTokens[j] = tokens[j - 1];
							newTokenTypes[j] = tokenTypes[j - 1];
						}
						tokens = newTokens;
						tokenTypes = newTokenTypes;
						numToks = tokens.length;
						//                          for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
						//                          System.err.println();
					}
					//check for "RDST"
					else if (tokens[i].equals("RDST")) {
						String[] newTokens = new String[numToks + 1];
						int[] newTokenTypes = new int[numToks + 1];
						int k;
						for (k = 0; k < i; k++) {
							newTokens[k] = tokens[k];
							newTokenTypes[k] = tokenTypes[k];
						}
						newTokens[k] = "RD";
						newTokenTypes[k] = ORDINAL_EXTENSION;
						newTokens[k + 1] = "ST";
						newTokenTypes[k + 1] = STREET_TYPE;
						for (int j = k + 2; j < numToks + 1; j++) {
							newTokens[j] = tokens[j - 1];
							newTokenTypes[j] = tokenTypes[j - 1];
						}
						tokens = newTokens;
						tokenTypes = newTokenTypes;
						numToks = tokens.length;
						//                          for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
						//                          System.err.println();
					}
					//check for "NDST"
					else if (tokens[i].equals("NDST")) {
						String[] newTokens = new String[numToks + 1];
						int[] newTokenTypes = new int[numToks + 1];
						int k;
						for (k = 0; k < i; k++) {
							newTokens[k] = tokens[k];
							newTokenTypes[k] = tokenTypes[k];
						}
						newTokens[k] = "ND";
						newTokenTypes[k] = ORDINAL_EXTENSION;
						newTokens[k + 1] = "ST";
						newTokenTypes[k + 1] = STREET_TYPE;
						for (int j = k + 2; j < numToks + 1; j++) {
							newTokens[j] = tokens[j - 1];
							newTokenTypes[j] = tokenTypes[j - 1];
						}
						tokens = newTokens;
						tokenTypes = newTokenTypes;
						numToks = tokens.length;
						//                          for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
						//                          System.err.println();
					} else {
						String stdValue = (String) standardOrdinalTypes.get(tokens[i]);
						if (stdValue != null) {
							tokens[i] = stdValue;
							tokenTypes[i] = DIGITS;
						}
						stdValue = (String) commonStreetNameShortHands.get(tokens[i]);
						if (stdValue != null) {
							tokens[i] = stdValue;
						}
					}
					break;
				case STREET_TYPE :
					String stdValue = (String) standardStreetTypes.get(tokens[i]);
					if (StringUtils.nonEmptyString(stdValue)) {
						if (stdValue.equals("ST")
							&& i < numToks / 2
							&& hasNext
							&& (tokenTypes[i + 1] != DIGITS)
							&& (tokenTypes[i + 1] != DIGITS_PLUS)
							&& (!tokens[i + 1].equals("APT"))) {
							tokens[i] = "SAINT";
							tokenTypes[i] = WORD;
							//                          for(int q=0; q<numToks; q++) System.err.print(tokens[q] + " (" + tokenTypes[q] + ") ");
							//                          System.err.println();
						} else {
							tokens[i] = stdValue;
						}
					}
					break;
			}
		}
	}

	private int[] identifyTokens(String[] toks) {
		int numToks = toks.length;
		int[] tokenTypes = new int[numToks];
		Object stdValue;
		for (int i = 0; i < numToks; i++) {
			String tok = toks[i];
			int numDigits = StringUtils.countDigits(tok);

			//a number
			if (numDigits > 0) {
				if (numDigits == tok.length()) {
					tokenTypes[i] = DIGITS;
				} else {
					tokenTypes[i] = DIGITS_PLUS;
				}
			}
			//a direction
			else if ((stdValue = standardDirections.get(tok)) != null) {
				tokenTypes[i] = DIRECTION;
				toks[i] = (String) stdValue;
			}
			//a street type (eg. Road, ave, street)
			else if (streetTypes.contains(tok)) {
				tokenTypes[i] = STREET_TYPE;
			}
			//a hypen
			else if (tok.equals("-")) {
				tokenTypes[i] = HYPHEN;
			}
			//ordinal extension
			else if (ordinalExtensions.contains(tok)) {
				tokenTypes[i] = ORDINAL_EXTENSION;
			}
			//otherwise its just a word
			else {
				tokenTypes[i] = WORD;
			}
		}
		return tokenTypes;
	}

	/**
	 * Breaks up a string on numbers and street types by inserting
	 * spaces.  For example:
	 * "303VernonAve12B" becomes "303 Vernon Ave 12 B".
	 * 
	 * @param str
	 * @return 
	 */
	private String unSmush(String str) {
		int[] space = new int[str.length() + 1];
		//         Collection streetTypes = Colls.getCollection("streetTypes");
		//         Iterator it = streetTypes.iterator();
		//         while (it.hasNext()) {
		//             String type = (String)it.next();
		//             int index = str.indexOf(type);
		//             if (index >=0) {
		//                 space[index] = 1;
		//                 space[index + type.length()] = 1;
		//             }
		//         }

		boolean[] numbers = StringUtils.findNumbers(str);
		boolean previous = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] != previous) {
				space[i] = 1;
				previous = !previous;
			}
		}
		StringBuffer result = new StringBuffer("");
		int iStart = 0;
		for (int i = 0; i < space.length; i++) {
			if (space[i] == 1) {
				result.append(separateStreetType(str.substring(iStart, i)) + " ");
				iStart = i;
			}
		}
		result.append(separateStreetType(str.substring(iStart)));
		//        System.out.println("unSmush result is " + result.toString());

		StringTokenizer sTok = new StringTokenizer(result.toString(), "#");
		result = new StringBuffer();
		while (sTok.hasMoreTokens()) {
			result.append(sTok.nextToken() + " ");
		}
		return result.toString();
	}

	private String separateStreetType(String s) {
		int len = s.length();
		if (len > 6 && s.endsWith("STREET")) {
			s = s.substring(0, len - 6) + " STREET";
		} else if (len > 9 && s.endsWith("BOULEVARD")) {
			s = s.substring(0, len - 9) + " BOULEVARD";
		} else if (len > 4 && s.endsWith("BLVD")) {
			s = s.substring(0, len - 4) + " BLVD";
		} else if (len > 4 && s.endsWith("ROAD")) {
			s = s.substring(0, len - 4) + " ROAD";
		} else if (len > 6 && s.endsWith("AVENUE")) {
			s = s.substring(0, len - 6) + " AVENUE";
		} else if (len > 3 && s.endsWith("AVE")) {
			s = s.substring(0, len - 3) + " AVE";
		}
		return s;
	}

	public static String aptNormalize(String apt) {
		String r = StringUtils.removeNonDigitsLetters(apt).intern();
		if (r == "BAS" || r == "BSMT" || r == "BMT" || r == "BST") {
			return "BSMT";
		}
		if (r == "PVTH" || r == "PH" || r == "PVT") {
			return "PH";
		}
		if (r.startsWith("APT")) {
			r = r.substring(3, r.length());
		} else if (r.startsWith("NO")) {
			r = r.substring(2, r.length());
		} else if (r.endsWith("FL")) {
			r = r.substring(0, r.length() - 2);
		} else if (r.endsWith("FLR")) {
			r = r.substring(0, r.length() - 3);
		} else if (r.endsWith("FLOOR")) {
			r = r.substring(0, r.length() - 5);
		}
		if (r.endsWith("ST") || r.endsWith("ND") || r.endsWith("RD") || r.endsWith("TH")) {
			r = r.substring(0, r.length() - 2);
		}
		final int len = r.length();
		if (len > 1) {
			if (r.charAt(0) == '0') {
				r = numbersBeforeLetters(r);
			} else {
				int i = 0;
				while (i < len && Character.isDigit(r.charAt(i))) {
					++i;
				}
				while (i < len && Character.isLetter(r.charAt(i))) {
					++i;
				}
				if (i != len) {
					r = numbersBeforeLetters(r);
				}
			}
		}
		return r;
	}

	private static String numbersBeforeLetters(String a) {
		final int len = a.length();
		StringBuffer b = new StringBuffer(len);
		boolean leading = true;
		for (int i = 0; i < len; ++i) {
			char c = a.charAt(i);
			if (Character.isDigit(c)) {
				if (!leading || c != '0') {
					b.append(c);
					leading = false;
				}
			}
		}
		for (int i = 0; i < len; ++i) {
			char c = a.charAt(i);
			if (Character.isLetter(c)) {
				b.append(c);
			}
		}
		return b.toString();
	}
}
