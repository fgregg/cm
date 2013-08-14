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
package com.choicemaker.cm.matching.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * The SimpleTokenizer is a minimal implementation of the Tokenizer
 * interface.
 * 
 * SimpleTokenizer uses a three-stage process to tokenize a String or Strings.
 * <ul>
 * <li>
 * First, the String(s) is/are preprocessed to remove all illegal characters.
 * Legal characters include letters and numbers, as well as some punctuation.
 * Legal/illegal punctuation can be set by the user.  SimpleTokenizer removes
 * all characters with ASCII codes higher than 127, and inserts spaces in
 * place of "split punctuation", which is configurable by the user.
 * <li>
 * Second, the String(s) is/are broken up into sets of like characters (e.g.
 * letters, numbers, whitespace, etc.).  Whitespace is discarded automatically.
 * Each legal punctuation mark is split by itself.
 * <li>
 * Third, a set of Token objects is created from the broken up Strings created
 * by the previous step.  If tokenize() was called with a String array (because
 * the name, address, etc. was reported in multiple fields), SimpleTokenizer
 * can insert field separator tokens into the token List.  Then, the final List
 * of Tokens is returned.
 * </ul>
 *  
 * By default, it converts all letters to upper case, but can be
 * configured to not do so.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class SimpleTokenizer implements Tokenizer {
	
	protected byte[] types = new byte[MAX_CHAR];

	protected boolean convertToUpperCase;
		
	protected String legalPunc = "";
	protected String nonsplitPunc = "";
	
	/**
	 * Creates a new SimpleTokenizer, which converts letters to uppercase, and
	 * has the pound sign and hyphen as legal punctuation characters.
	 * All other punctuation is illegal, and all but the apostrophe causes a
	 * token split.
	 */
	public SimpleTokenizer() {
		setConvertToUpperCase(true);

		setLegalPunctuation("#-");
		setNonsplitPunctuation("\'");
	}

	/**
	 * Sets whether or not we convert letters to upper case.
	 */
	public void setConvertToUpperCase(boolean b) {
		convertToUpperCase = b;
	}

	/**
	 * Sets the legal punctuation for this SimpleTokenizer.
	 */
	public void setLegalPunctuation(String chars) {
		legalPunc = chars;	
		recalcTypesArray();
	}
	
	/**
	 * Illegal punctuation (that which is not included in any tokens) is 
	 * divided into two classes. Namely,
	 * 
	 * <ol>
	 * <li>
	 * punctuation which causes a split, as in J.R.R. Tolkien --> J R R Tolkien
	 * <li>
	 * punction which does not, as in Paddy  O'Mally --> Paddy OMally
	 * </ol>
	 * 
	 * Since few punctuation symbols generally fall into class 1, it's easier to 
	 * allow the user to specify the membership of these classes by exposing this method
	 * rather than setSplitPunctuation(String).
	 */
	public void setNonsplitPunctuation(String chars) {
		nonsplitPunc = chars;	
		recalcTypesArray();
	}
	
	//
	// Tokenization methods.
	//

	/**
	 * Returns a list of tokens for the specified String.
	 */
	public List tokenize(String s) {
		if (s == null) {
			return new ArrayList(0);
		}
		
		StringBuffer preprocessed = preprocessString(s);
		List tokenizedStrings = split(preprocessed);		
		List tokens = createTokens(tokenizedStrings);
		return tokens;
	}
	
	/**
	 * Tokenizes the specified array of Strings.
	 * Null array elements do not result in a Token, and no field-separator
	 * Token is used.
	 */
	public List tokenize(String[] s) {
		return tokenize(s, null, null);
	}
	
	/**
	 * Tokenizes the specified array of Strings.
	 * 
	 * If <code>separatorToken</code> is non null, it is inserted between the
	 * tokens from each element of <code>strings</code>.
	 * 
	 * If <code>nullToken</code> is non null, it acts as a placeholder for 
	 * null elements of <code>strings</code> in the final List of Tokens.
	 */
	public List tokenize(String[] strings, Token separatorToken, Token nullToken) {
		List finalList = new ArrayList();
				
		for (int i = 0; i < strings.length - 1; i++) {
			if (strings[i] != null) {
				finalList.addAll(tokenize(strings[i]));
			} else if (nullToken != null) {
				finalList.add(nullToken);
			}	

			if (separatorToken != null) {
				finalList.add(separatorToken);
			}
		}

		if (strings[strings.length - 1] != null) {
			finalList.addAll(tokenize(strings[strings.length - 1]));	
		} else if (nullToken != null) {
			finalList.add(nullToken);	
		}
		
		return finalList;
	}
	
	//
	// Other methods.
	//
	
	/**
	 * Returns a copy of <code>s</code> in which illegal characters are
	 * removed (if specified) and lower case letters are converted to upper
	 * case (if specified).  Also, carriage returns, newlines, etc. are
	 * converted to spaces and any leading and trailing whitespace is removed.
	 * 
	 * Note that characters whose ASCII codes are greater than 127 (and unicode
	 * characters greater than 127) are removed regardless of the value of
	 * removeIllegalChars.  However, we throw an exception only if
	 * errorOnIllegalChars is true.
	 */
	protected StringBuffer preprocessString(String s) {
		int len = s.length();
		StringBuffer buffer = new StringBuffer(len);
		
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c > 127) {
				// Nothing to do, just making sure "c" is in range...
			} else if (types[c] > 0) {
				if (convertToUpperCase && IS_LOWER[c]) {
					buffer.append((char) (c + LOWER_TO_UPPER));
				} else if (IS_WHITESPACE[c]) {
					buffer.append(' ');
				} else {
					buffer.append(c);
				}
			} else if (types[c] == ILLEGAL_SPLIT) {
				buffer.append(' ');
			}
			
		}
		
		return buffer;
	}
		
	/**
	 * Split the preprocessed string into characters of the same type.
	 */
	protected List split(StringBuffer s) {
		int len = s.length();	

		int lastType = 0;		
		StringBuffer curChars = new StringBuffer();
		List list = new ArrayList();
		
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			byte type = types[c];
			
			if ((type != lastType && lastType > 0) || lastType == LEGAL_PUNC) {
				list.add(curChars.toString());
				curChars.setLength(0);
			}
			
			curChars.append(c);
			lastType = type;
		}
		
		// add the last ones
		if (lastType > 0) {
			list.add(curChars.toString());
		}
		
		return list;
	}
	
	/**
	 * Build a list of Tokens from the split Strings.
	 */
	protected List createTokens(List strings) {
		int len = strings.size();
		
		List tokens = new ArrayList(len);
		for (int i = 0; i < len; i++) {
			String s = (String) strings.get(i);
			s = s.trim();
			
			if (s.length() > 0) {
				tokens.add(new Token(s));
			}
		}
		
		return tokens;
	}

	/**
	 * Each character has a type.  One of letter, digit, whitespace, legal punctuation,
	 * illegal split punctuation, illegal non-split punction.
	 */
	protected void recalcTypesArray() {
		// reset each character's legality.
		for (int i = 0; i < MAX_CHAR; i++) {
			if (IS_WHITESPACE[i]) {
				types[i] = WHITESPACE;
			} else if (IS_LETTER[i]) {
				types[i] = LETTER;
			} else if (IS_DIGIT[i]) {
				types[i] = DIGIT;
			} else {
				types[i] = ILLEGAL_SPLIT;
			}
		}
		
		// then possibly change our mind about the legal characters
		for (int i = 0; i < legalPunc.length(); i++) {
			if (types[legalPunc.charAt(i)] > 0) {
				continue;	
			}
			types[legalPunc.charAt(i)] = LEGAL_PUNC;
		}
		
		// then possibly change our mind about the split characters
		for (int i = 0; i < nonsplitPunc.length(); i++) {
			if (types[nonsplitPunc.charAt(i)] > 0) {
				continue;	
			}
			types[nonsplitPunc.charAt(i)] = ILLEGAL_NONSPLIT;
		}
	}

	//
	// Final variables.
	//
	
	private static final int MAX_CHAR = 128;
	
	private static final byte LETTER = 1;
	private static final byte DIGIT = 2;
	private static final byte WHITESPACE = 3;
	private static final byte LEGAL_PUNC = 4;
	
	private static final byte ILLEGAL_NONSPLIT = -2;
	private static final byte ILLEGAL_SPLIT = -1;
		
	private static boolean[] IS_LOWER = new boolean[MAX_CHAR];
	
	private static boolean[] IS_WHITESPACE = new boolean[MAX_CHAR];
	private static boolean[] IS_LETTER = new boolean[MAX_CHAR];
	private static boolean[] IS_DIGIT = new boolean[MAX_CHAR];

	private static final int LOWER_TO_UPPER = 'A' - 'a';

	//
	// initialize some 
	//
	static {
		for (int i = 0; i < MAX_CHAR; i++) {
			char c = (char) i;
			
			if (Character.isLetter(c)) {
				if (Character.isLowerCase(c)) {
					IS_LOWER[i] = true;
				}
				IS_LETTER[i] = true;
			} else if (Character.isDigit(c)) {
				IS_DIGIT[i] = true;
			} else if (Character.isWhitespace(c)) {
				IS_WHITESPACE[i] = true;
			}
		}
	
	}

}
