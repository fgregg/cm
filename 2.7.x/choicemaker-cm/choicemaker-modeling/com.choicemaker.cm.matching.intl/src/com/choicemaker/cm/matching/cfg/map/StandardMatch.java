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
package com.choicemaker.cm.matching.cfg.map;

import com.choicemaker.cm.matching.gen.LongestCommonSubsequence;
import com.choicemaker.util.StringUtils;


class StandardMatch {

	private String key;
	private String standard;
	
	private String[] split;

	public StandardMatch(String key, String standard) {
		this.key = key;
		this.standard = standard;
		
		this.split = StringUtils.split(key);
	}

	/**
	 * Although this interface is a bit troubling, pieces must
	 * be equivalent to StringUtils.split(s).
	 */
	public boolean isMatch(String s, String[] pieces) {
		if (standard.equals(s)) {
			return true;
		} else if (key.equals(s)) {
			return true;
		} else if (key.equals(StringUtils.removeNonDigits(s))) {
			return true;
		} else if (split.length == pieces.length) {
			for (int i = 0, n = split.length; i < n; i++) {
				if (!LongestCommonSubsequence.isLcsAbbrev(split[i], pieces[i], 1)) {
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getStandard() {
		return standard;
	}
	
	public String[] getWords() {
		return split;
	}
	
	public String getWord(int i) {
		return split[i];
	}
	
	public int getNumWords() {
		return split.length;	
	}
	
	public static boolean areEquivalent(StandardMatch m1, StandardMatch m2) {
		return m1.standard.equals(m2.standard);
	}
	
	public static StandardMatch resolveMatch(String s, String[] pieces, StandardMatch m1, StandardMatch m2) {
		if (StandardMatch.areEquivalent(m1, m2)) {
			return m1; // either one will do
		} else {
			boolean b1 = m1.key.startsWith(s);
			boolean b2 = m2.key.startsWith(s);
			if (b1 ^ b2) {
				// if one of them starts with the input String, return it.
				return b1 ? m1 : m2;
			} else {
				// otherwise, we can't tell the difference; return the first one
				System.err.println("\nAmbiguous value: " + s + " between '" + m1.key + "' and '" + m2.key + "'");
				return m1;
			}				
		}
	}
	
}
