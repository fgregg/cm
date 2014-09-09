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
package com.choicemaker.cm.matching.en.us.address;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.choicemaker.cm.matching.cfg.SimpleTokenizer;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.util.PrefixTree;
import com.choicemaker.util.SuffixTree;

/**
 * Tokenizer whose main purpose is to split up addresses that
 * have been &quot;smushed&quot; together.  That is, whitespace
 * has been inappropriately removed.  This is not a problem
 * if a house number and a alphabetic street name are run together,
 * as SimpleTokenizer breaks Strings on alpha/num boundaries.
 * However, if alphabetic street name and suffix are run together,
 * as in "FIFTHAVENUE", we need to split "AVENUE" off the end.
 * 
 * AddressTokenizer splits off pre-directions from the beginning,
 * and apartment types, post-directions, and suffixes from the
 * end of its arguments.
 * 
 * AddressTokenizer can also be configured to split up long digit 
 * strings.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class AddressTokenizer extends SimpleTokenizer {
		
	protected PrefixTree preDirections = new PrefixTree();
	protected SuffixTree streetSuffixes = new SuffixTree();
	protected SuffixTree postDirections = new SuffixTree();
	protected SuffixTree aptTypes = new SuffixTree();
	
	protected int splitDigitsMinLength = -1;
	protected int splitDigitsLeftLength = -1;
	
	/**
	 * Creates a new AddressTokenizer.
	 */
	public AddressTokenizer() { }

	public void setSplitPreDirections(Collection dirs)  {
		this.preDirections.addAll(dirs);	
	}
	
	public void setSplitSuffixes(Collection suffs) {
		this.streetSuffixes.addAll(suffs);
	}

	public void setSplitPostDirections(Collection dirs) {
		this.postDirections.addAll(dirs);	
	}

	public void setSplitAptTypes(Collection aptTypes) {
		this.aptTypes.addAll(aptTypes);	
	}

	/**
	 * Configures the splitting of digit Strings.
	 * 
	 * Any digit String whose length is at least <code>minLen</code>
	 * will be split into two Tokens, the first of which is the
	 * first <code>lhsLen</code> chars, and the second of which is
	 * the remaining <code>minLen - lhsLen</code> characters.
	 * 
	 * For example, if minLen = 4, and lhsLen = 2, 
	 * 
	 * 12345 --> 12 345
	 * 
	 * but 
	 * 
	 * 123 --> 123
	 */
	public void setSplitDigitStrings(int minLen, int lhsLen) {
		if (minLen < 2) {
			throw new IllegalArgumentException("Min length < 2: " + minLen);
		} else if (lhsLen < 1 || lhsLen >= minLen) {
			throw new IllegalArgumentException("Left length out of range ["
									 + 1 + "," + (minLen-1) + "]: " + lhsLen);
		}
		
		splitDigitsMinLength = minLen;
		splitDigitsLeftLength = lhsLen;
	}	

	//
	// Tokenization methods.
	//

	/**
	 * Overrides the SimpleTokenizer's implementation to break up &quot;smushed&quot;
	 * Strings.
	 */
	protected List createTokens(List strings) {
		int max = strings.size();
		
		List tokens = new ArrayList(max);
		for (int i = 0; i < max; i++) {
			
			String s = ((String)strings.get(i)).trim();
			if (s.length() == 0)
				continue;
				
			if (Character.isLetter(s.charAt(0))) {
					
				String preDir = preDirections.getLongestPrefix(s);
				if (preDir != null) {
					s = s.substring(preDir.length());	
				}
		
				String aptType = aptTypes.getLongestSuffix(s);
				if (aptType != null) {
					s = s.substring(0, s.length() - aptType.length());	
				}
		
				String postDir = postDirections.getLongestSuffix(s);
				if (postDir != null) {
					s = s.substring(0, s.length() - postDir.length());	
				}
		
				String suffix = streetSuffixes.getLongestSuffix(s);
				if (suffix != null) {
					s = s.substring(0, s.length() - suffix.length());
				}
				
				if (preDir != null)
					tokens.add(new Token(preDir));
	
				if (s.length() > 0)
					tokens.add(new Token(s));
			
				if (suffix != null)
					tokens.add(new Token(suffix));
				
				if (postDir != null)
					tokens.add(new Token(postDir));
				
				if (aptType != null)
					tokens.add(new Token(aptType));
				
			} else if (splitDigitsMinLength > 0 && Character.isDigit(s.charAt(0)) && 
					   s.length() >= splitDigitsMinLength) {
					   	
				tokens.add(new Token(s.substring(0, splitDigitsLeftLength)));
				tokens.add(new Token(s.substring(splitDigitsLeftLength)));
			
			} else {
				tokens.add(new Token(s));
			}
			
		}
		
		return tokens;
	}
	
}
