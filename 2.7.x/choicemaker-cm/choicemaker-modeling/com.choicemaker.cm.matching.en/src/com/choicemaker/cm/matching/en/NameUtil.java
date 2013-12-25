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
 * Utilties useful in general English-language name matching.
 * @author rphall
 */
public class NameUtil {

	/* Avoids importing com.choicemaker.cm.util.StringUtils */
	private static boolean nonEmptyString(String s) {
		boolean retVal = s != null && s.length() > 0;
		return retVal;
	}

	/**
	 * Declares a match if one String is of length 1 and the other String begins with it,
	 * or if the two Strings are equals.  For example
	 * MatchingInitialOrName("A", "Annie") -> true
	 * MatchingInitialOrName("A", "A") -> true
	 * MatchingInitialOrName("Ann", "Ann") -> true
	 * MatchingInitialOrName("Anette", "Annie") -> false
	 * MatchingInitialOrName("A", "B") -> false
	 * @author S. Yoakum-Stover
	 * @version$Revision: 1.2 $ $Date: 2010/03/27 22:18:58 $
	 */
	public static boolean matchingInitialOrName(String s1, String s2) {
		boolean isMatch = false;
		if (nonEmptyString(s1) && nonEmptyString(s2)) {
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
		}
		return isMatch;
	}

}


