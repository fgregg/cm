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
package com.choicemaker.cm.compiler.gen;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * The original version implemented with the Jakarta ORO library.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
class Perl5ValidConverter {
	/**
	 * 
	 * @param input
	 * @return String
	 */	
	public static String convertValids(String input) {
		Perl5Compiler perl5Compiler = new Perl5Compiler();
		Pattern validPattern = null;
		try {
			validPattern = perl5Compiler.compile("valid\\s*\\(([^\\(]+?)\\)");
		} catch (MalformedPatternException e) {
			e.printStackTrace();
		}
		Perl5Matcher matcher = new Perl5Matcher();
		PatternMatcherInput pmi = new PatternMatcherInput(input);
		
		if (! matcher.contains(pmi, validPattern)) {
			return input;
		}
		
		StringBuffer buffer = new StringBuffer();
		
		// last is the index of the character AFTER the last character of the last match.
		int last = 0;
		
		do {
			MatchResult m = matcher.getMatch();
			int start = m.beginOffset(0);
			int end = m.endOffset(0);
			
			// the portion before the current match.
			buffer.append(input.substring(last, start));
			
			// currentMatch is everything inside the valid's parenthesis, e.g.
			// valid( foo.bar ) --> 'foo.bar';
			String currentMatch = m.group(1);
			
			//System.out.println("\t" + currentMatch);
			
			String convertedValid = convertOneValid(currentMatch);
			buffer.append(convertedValid);
			
			last = end;
		} while (matcher.contains(pmi, validPattern));
		
		// the portion of the string after the last match
		if (last < input.length()) {
			buffer.append(input.substring(last));				
		}
		
		return buffer.toString();
	}

	/**
	 * Converts foo.bar to foo.__v_bar and foo to __v_foo
	 * Also removes 
	 * 
	 * @param validString
	 * @return String
	 */	
	private static String convertOneValid(String fieldString) {		
		fieldString = removeInternalWhitespace(fieldString.trim());

		int lastPeriod = fieldString.lastIndexOf('.');
		String converted =
			fieldString.substring(0, lastPeriod + 1) +
			"__v_" +
			fieldString.substring(lastPeriod + 1);
		
		return converted;
	}

	/**
	 * Removes spaces around dots, as Java allows things like 'foo . bar'
	 * 
	 * @param s
	 * @return String
	 */
	private static String removeInternalWhitespace(String s) {
		StringBuffer b = new StringBuffer();
		int size = s.length();
		for(int i = 0; i < size; ++i) {
			char c = s.charAt(i);
			if(!Character.isWhitespace(c)) {
				b.append(c);
			}
		}
		return b.toString();
	}

}
