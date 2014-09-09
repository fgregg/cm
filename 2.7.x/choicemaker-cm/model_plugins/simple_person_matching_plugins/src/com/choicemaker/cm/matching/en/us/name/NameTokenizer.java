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
package com.choicemaker.cm.matching.en.us.name;

import java.util.List;

import com.choicemaker.cm.matching.cfg.SimpleTokenizer;
import com.choicemaker.cm.matching.cfg.Token;

/**
 * NameTokenizer extends SimpleTokenizer adds two simple things
 * to the SimpleTokenizer functionality.
 * 
 * <ul>
 * <li>
 * Parentheses are converted to square brackets to avoid 
 * later problems, as parens are "rule delimeters" in
 * printed ParseTreeNodes.
 * <li>
 * Legal punctuation is then left and right square brackets,
 * quotation marks, and hyphens.  All these marks appear in 
 * names and have useful semantics.
 * <li>
 * Apostrophes are non-split punctuation.
 * </ul>
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class NameTokenizer extends SimpleTokenizer {
	
	/**
	 * Create a new NameTokenizer, which by default converts names
	 * to upper case; has brackets, quotation marks, and hyphens 
	 * as legal punctuation; and has apostrophes as non-split illegal
	 * punctuation.
	 */
	public NameTokenizer() {
		super();

		// parens are legal (for nicnames and maiden names), quotes
		// are legal (for nicknames), and hyphens are legal for general
		// hyphenated names.
		setLegalPunctuation("[]\"-");
		setNonsplitPunctuation("\'");
	}
	
	/**
	 * Overrides SimpleTokenizer's implementation, to substitute
	 * square brackets for parenthesis.
	 */
	protected StringBuffer preprocessString(String s) {
		s = s.replace('(', '[');
		s = s.replace(')', ']');
		return super.preprocessString(s); 
	}

	/**
	 * Overrides the parent class's implementation.
	 */
	//public List tokenize(String s) {
	//	return tokenize(new String[] {s});
	//}

	/**
	 * Overrides SimpleTokenizer's implementation, to insert 
	 * unique separators at the beginning and end of the list of tokens,
	 * depending on how many arguments we've gotten.
	 */
	public List tokenize(String[] s) {
		s = filterNonNull(s);
		if (s.length < 1 || s.length > 3) {
			throw new IllegalArgumentException(
				"We are not equipped to tokenize names with " + s.length + " fields.");
		}
		
		List tokens = super.tokenize(s, SEPARATORS[1], NULL_TOKEN);
		tokens.add(0, SEPARATORS[s.length]);
		tokens.add(SEPARATORS[s.length]);
		return tokens;
	}

	private String[] filterNonNull(String[] s) {
		int num = 0;
		for (int i = 0; i < s.length; i++) {
			if (s[i] != null && s[i].trim().length() > 0) {
				num++;
			}			
		}
		
		if (num == s.length) {
			return s;
		} else {
			String[] nonNull = new String[num];
			int index = 0;
			for (int i = 0; i < s.length; i++) {
				if (s[i] != null) {
					String si = s[i].trim();
					if (si.length() > 0) {
						nonNull[index++] = si;
					}
				}
			}
			return nonNull;
		}
	}
		
	private static final Token NULL_TOKEN = new Token("_");

	private static final Token[] SEPARATORS = {
		null,
		new Token("#"),
		new Token("##"),
		new Token("###"),
	};
}
