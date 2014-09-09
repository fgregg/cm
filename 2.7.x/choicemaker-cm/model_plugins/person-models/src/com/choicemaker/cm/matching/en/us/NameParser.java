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
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.choicemaker.cm.matching.en.Soundex;
import com.choicemaker.util.StringUtils;

/**
 * Utilities for dealing with compound names.
 * The name parser is used to break up names, e.g., first and middle name
 * reported in a single field, to filter out titles and suffixes, e.g., MR and
 * JR, and to remove invalid values, e.g., <code>N/A</code>. A name parser is instantiated
 * with the given first, middle, and last name. The broken up name parts can be
 * accessed via various accessor methods
 *
 * @deprecated use the generic CFG Parser interface
 * @see com.choicemaker.cm.matching.cfg.Parsers
 *
 * @author    S. Yoakum-Stover
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 22:17:24 $
 */
public class NameParser {

	public static Collection genericFirstNames = new HashSet();
	public static Collection childOfIndicators = new HashSet();
	public static Collection invalidLastNames = new HashSet();
	public static Collection nameTitles = new HashSet();
	public static Collection lastNamePrefixes = new HashSet();

	private String firstName = "";
	private String middleNames = "";
	private String lastName = "";
	private String titles = "";
	private String potentialMaidenName = "";
	private String mothersFirstName = "";

	public NameParser(String f, String m, String l) {
		String first =
			StringUtils.nonEmptyString(f)
				? StringUtils.removePunctuation(f)
				: "";
		String middle =
			StringUtils.nonEmptyString(m)
				? StringUtils.removePunctuation(m)
				: "";
		String last =
			StringUtils.nonEmptyString(l)
				? StringUtils.removePunctuation(l)
				: "";
		// BUGFIX rphall 2008-07-13
		// StringUtils.removePunctuation sometimes does not  remove trailing space
		first = first.trim();
		middle = middle.trim();
		last = last.trim();
		// END BUGFIX
		chunkUpNamesStrings(first, middle, last);
		if (firstName != null)
			firstName = firstName.intern();
		if (middleNames != null)
			middleNames = middleNames.intern();
		if (lastName != null)
			lastName = lastName.intern();
		if (titles != null)
			titles = titles.intern();
		if (potentialMaidenName != null)
			potentialMaidenName.intern();
	}

	/**
	 * @return the first name.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the middle name.
	 */
	public String getMiddleNames() {
		return middleNames;
	}

	/**
	 * @return the last name.
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return the titles.
	 */
	public String getTitles() {
		return titles;
	}

	/**
	 * @return the maiden name.
	 */
	public String getPotentialMaidenName() {
		return potentialMaidenName;
	}

	/**
	 * @return the mother's first name. This is specific to children's names.
	 */
	public String getMothersFirstName() {
		return mothersFirstName;
	}

	private final int NUM_NAMES = 4;
	private final int NUM_NAMES2 = 2 * NUM_NAMES;

	/**
	 * @return a measure for how many name components are swapped. Each exact
	 * swap increments the score by 2. Each name approximate swap (Soundex)
	 * increases the score by 1.
	 *
	 * A higher score indicates a higher similarity.
	 * E.g., the similarity score for "JIM R. SMITH" and "SMIT JIM" is 3.
	 */
	public int getSwapSimilarity(NameParser o) {
		String[] n = new String[NUM_NAMES2 + NUM_NAMES2];
		boolean[] f = new boolean[NUM_NAMES2];
		n[0] = firstName;
		n[1] = middleNames;
		n[2] = lastName;
		n[3] = potentialMaidenName;
		n[4] = o.firstName;
		n[5] = o.middleNames;
		n[6] = o.lastName;
		n[7] = o.potentialMaidenName;
		for (int i = 0; i < NUM_NAMES2; ++i) {
			f[i] = n[i] != null;
			if (f[i] && n[i].length() > 0) {
				n[i + NUM_NAMES2] = Soundex.soundex(n[i]);
			}
		}
		int score = 0;
		for (int i = 0; i < NUM_NAMES; ++i) {
			boolean sndx = false;
			for (int j = NUM_NAMES; j < NUM_NAMES2; ++j) {
				if (i + NUM_NAMES != j && f[i] && f[j]) {
					if (n[i] == n[j]) {
						if (sndx) {
							score += 1;
						} else {
							score += 2;
						}
						break;
					} else if (
						!sndx && n[i + NUM_NAMES2].equals(n[j + NUM_NAMES2])) {
						score += 1;
						sndx = true;
					}
				}
			}
		}
		return score;
	}

	public void chunkUpNamesStrings(String first, String middle, String last) {

		//chunk up the first name
		String[] firstNames = chunkUpNameString(first, true);
		//place the first name chunks
		firstName = firstNames[0]; //first
		middleNames = firstNames[1]; //middles
		String tmpTitles = firstNames[2];
		//logger.debug("f: " + firstName + " m: " + middleNames + " l: " + lastName + " t: " + titles);

		//place the middle names
		if (StringUtils.nonEmptyString(middle)) {
			String[] tmpMiddles = chunkUpNameString(middle, false);
			if (StringUtils.nonEmptyString(tmpMiddles[0])) {
				middleNames =
					concatWithSeparator(middleNames, tmpMiddles[0], " ");
			}
			if (StringUtils.nonEmptyString(tmpMiddles[1])) {
				middleNames =
					concatWithSeparator(middleNames, tmpMiddles[1], " ");
			}
			tmpTitles = concatWithSeparator(tmpTitles, tmpMiddles[2], " ");
		}

		//chunk up the last name
		String[] lastNames = chunkUpLastNameString(last);

		//place the last name chunks
		// 2008-10-20 rphall
		// Parsing of compound last names is improved if the
		// lastNames[1] tokens are NOT combined with middle names
		// e.g. WONG DE JESUS, VAN DER ZEE
		//middleNames = concatWithSeparator(middleNames, lastNames[1], " "); //middles
		// END CHANGE
		lastName = lastNames[0]; //last
		potentialMaidenName = lastNames[3]; //potential maiden
		tmpTitles = concatWithSeparator(tmpTitles, lastNames[2], " "); //titles
		titles = dedupTokens(tmpTitles); //titles
	}

	private static String dedupTokens(String tokens) {
		String retVal = "";
		if (StringUtils.nonEmptyString(tokens)) {
			StringTokenizer st = new StringTokenizer(tokens);
			int count = st.countTokens();
			// Do nothing if count == 0
			if (count == 1) {
				retVal = tokens.trim();
			} else {
				Set seen = new HashSet();
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					boolean isNew = seen.add(token);
					if (isNew) {
						retVal = concatWithSeparator(retVal, token, " ");
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Chunk up a first name into three parts - a first name,
	 * one or more middle names, and one or more titles.  We
	 * parse only on spaces including  newlines and such.
	 * We don't do anything special with hyphens, they remain.
	 *
	 * @param str
	 * @param isAFirstName true if this is a first name string,
	 * false for last name.
	 */
	public String[] chunkUpNameString(String str, boolean isAFirstName) {
		String[] theNames = { "", "", "" }; //first, middles, titles
		if (!StringUtils.nonEmptyString(str)) {
			return theNames;
		}

		if (isAFirstName) {
			if (genericFirstNames.contains(str)) {
				return theNames;
			}
			int lastSpace = str.lastIndexOf(' ');
			if (lastSpace > 0) {
				String s = str.substring(0, lastSpace).trim();
				if (childOfIndicators.contains(s)) {
					String tmp = str.substring(lastSpace + 1, str.length());
					StringTokenizer st = new StringTokenizer(tmp);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						boolean isGenericFirstName =
							genericFirstNames.contains(token);
						if (!isGenericFirstName) {
							mothersFirstName
								+= concatWithSeparator(
									mothersFirstName,
									token,
									" ");
						}
					}
					return theNames;
				}
			}
		}

		StringTokenizer sTok = new StringTokenizer(str);
		int numToks = sTok.countTokens();
		if (numToks == 1) {
			str = str.trim();
			if (nameTitles.contains(str)) {
				theNames[2] = str;
			} else if (isAFirstName || !invalidLastNames.contains(str)) {
				theNames[0] = str;
			}
			return theNames;
		}

		for (int iTok = 0; iTok < numToks; iTok++) {
			String nameTok = sTok.nextToken();
			if (nameTitles.contains(nameTok)) {
				theNames[2] = concatWithSeparator(theNames[2], nameTok, " ");
			}
			//if we don't already have a "first name" this is it.
			else if (!StringUtils.nonEmptyString(theNames[0])) {
				theNames[0] = nameTok;
			}
			//otherwise add it to the list of middle names.
			else {
				theNames[1] = concatWithSeparator(theNames[1], nameTok, " ");
			}
		}
		return theNames;
	}

	public String[] chunkUpLastNameString(String s) {
		String[] theNames = { "", "", "", "" };
		//last, unused, titles, possible maiden
		if (!StringUtils.nonEmptyString(s)) {
			return theNames;
		}

		s = fixMc(s);
		String flipped = flipToks(s);
		String[] chunks = chunkUpNameString(flipped, false);
		theNames[2] = chunks[2]; //titles

		// 2008-10-20 rphall
		// Compound names like VAN DER ZEE or WONG DE JESUS
		// need special handling. Iterate through the middle tokens,
		// placing last-name prefixes like DE, DER or VAN back with
		// the last name. Any thing left over in a compound name
		// is a maiden name. Note that compound names have
		// precedence over hyphenated names; e.g.
		//
		// WONG DE SMITH-JONES => maiden: WONG, ln: DE SMITH-JONES
		// SMITH-JONES LEE-TAYLOR => maiden: SMITH-JONES, ln: LEE-TAYLOR
		// DE JESUS VAN DER ZEE => maiden: DE JESUS, ln: VAN DER ZEE
		//

		// If a name is compound, calculate maiden without regard for hyphenation
		String tmpLast = chunks[0];
		String tmpMaiden = "";
		StringTokenizer st = new StringTokenizer(chunks[1]);
		boolean isCompound = st.hasMoreTokens();

		boolean isLastNameComponent = true; // last vs maiden
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			boolean isLastNamePrefix = lastNamePrefixes.contains(token);
			if (isLastNameComponent && isLastNamePrefix) {
				tmpLast = concatWithSeparator(tmpLast, token, " ");
			} else {
				isLastNameComponent = false;
				tmpMaiden = concatWithSeparator(tmpMaiden, token, " ");
			}
		}

		// The tmp variables hold tokens in reverse order
		theNames[0] = flipToks(tmpLast); //last name
		theNames[3] = flipToks(tmpMaiden);
		//potential maiden names flipped back into the correct order

		// If not compound, look for a hyphen in the last name.
		// If found separate names
		if (!isCompound) {
			int index =
				Math.max(chunks[0].indexOf('-'), chunks[0].indexOf('/'));
			if (index > 0) {
				theNames[0] = chunks[0].substring(index + 1);
				theNames[3] = chunks[0].substring(0, index);
				//the first part becomes a potential maiden name
			}
		}
		// END CHANGES 2008-10-20 rphall

		return theNames;
	}

	public static String fixMc(String s) {
		int len = s.length();
		int i = 0;
		if (len >= 4) {
			if (s.charAt(0) == 'M') {
				char c = s.charAt(1);
				if (c == 'C' && s.charAt(2) == ' ') {
					i = 3;
					while (i < len && s.charAt(i) == ' ') {
						++i;
					}
					s = "MC" + s.substring(i, len);
					len = s.length();
				} else if (
					c == 'A' && s.charAt(2) == 'C' && s.charAt(3) == ' ') {
					i = 4;
					while (i < len && s.charAt(i) == ' ') {
						++i;
					}
					s = "MAC" + s.substring(i, len);
					len = s.length();
				}
			}
			while (i < len) {
				char c = s.charAt(i);
				if (c == '-' || c == ' ' || c == '/') {
					return s.substring(0, i + 1)
						+ fixMc(s.substring(i + 1, len));
				}
				++i;
			}
		}
		return s;
	}

	/**
	 * Flips the order of the tokens in the string.
	 *
	 * @param s
	 */
	public static String flipToks(String s) {
		StringTokenizer sTok = new StringTokenizer(s);
		int numToks = sTok.countTokens();
		if (numToks <= 1) {
			return s;
		}
		String[] tokArray = new String[numToks];
		for (int i = 0; i < numToks; i++) {
			tokArray[numToks - 1 - i] = sTok.nextToken();
		}
		String result = "";
		for (int j = 0; j < numToks; j++) {
			result = concatWithSeparator(result, tokArray[j], " ");
		}
		return result;
	}

	/**
	 * Declares a match if one String is of length 1 and the other String begins with it,
	 * or if the two Strings are equals.  For example
	 * MatchingInitialOrName("A", "Annie") -> true
	 * MatchingInitialOrName("A", "A") -> true
	 * MatchingInitialOrName("Ann", "Ann") -> true
	 * MatchingInitialOrName("Anette", "Annie") -> false
	 * MatchingInitialOrName("A", "B") -> false
	 */
	public static boolean matchingInitialOrName(String s1, String s2) {
		boolean isMatch = false;
		if (!StringUtils.nonEmptyString(s1)
			|| !StringUtils.nonEmptyString(s2)) {
			return false;
		}
		int len1 = s1.length();
		int len2 = s2.length();

		//Check for matching initial
		if (len1 == 1) {
			isMatch = s2.startsWith(s1);
		} else if (len2 == 1) {
			isMatch = s1.startsWith(s2);
		} else { // Otherwise check for complete match
			isMatch = s2.equals(s1);
		}
		return isMatch;
	}

	public static String concatWithSeparator(
		String s1,
		String s2,
		String sep) {
		boolean b1 = StringUtils.nonEmptyString(s1);
		boolean b2 = StringUtils.nonEmptyString(s2);
		if (b1) {
			return b2 ? (s1 + sep + s2) : s1;
		} else {
			return b2 ? s2 : null;
		}
	}
}
