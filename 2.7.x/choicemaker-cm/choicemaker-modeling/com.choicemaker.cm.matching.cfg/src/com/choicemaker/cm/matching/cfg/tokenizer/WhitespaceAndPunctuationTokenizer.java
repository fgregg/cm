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
package com.choicemaker.cm.matching.cfg.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.Tokenizer;

/**
 * Throws away chars > 127, and all characters other than 
 * letters, numbers, and any user-specified legal punctuation.
 * 
 * Token boundaries are illegal characters and legal punctuation.
 * NOTE: this tokenizer does not split at letter/digit boundaries.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class WhitespaceAndPunctuationTokenizer implements Tokenizer {

	protected boolean convertToUpper = true;
	protected String legalPunctuation = "";
	protected boolean splitOnLegalPunctuation = true;

	private boolean dirty = true;
	
	protected boolean[] tokenDelim;
	protected char[] translationTable;
	
	public WhitespaceAndPunctuationTokenizer() {
		// nothing to do.
	}
			
	public void setLegalPunctuation(String punc) {
		if (punc == null) {
			punc = "";
		}
		
		if (!legalPunctuation.equals(punc)) {
			legalPunctuation = punc;
			dirty = true;
		}
	}

	public void setSplitOnLegalPunctuation(boolean b) {
		if (splitOnLegalPunctuation != b) {
			splitOnLegalPunctuation = b;
			dirty = true;
		}
	}
		
	public void setConvertToUpper(boolean b) {
		if (convertToUpper != b) {
			convertToUpper = b;
			dirty = true;
		}
	}
	
	public List tokenize(String s) {
		if (dirty) {
			recalcTranslationTable();
		}
		
		List tokens = new ArrayList(8);
		
		int len = s.length();
		
		char[] buff = new char[len];
		int buffLen = 0;
		for (int i = 0; i < len; i++) {
			int c = s.charAt(i);
			if (c > MAX_CHAR) {
				// clear the buffer.
				if (buffLen > 0) {
					tokens.add(new Token(String.valueOf(buff, 0, buffLen)));
					buffLen = 0;
				}
				
				continue;
			}
			
			char repl = translationTable[c];
			boolean delim = tokenDelim[c];
			
			if (delim) {
				// clear the buffer.
				if (buffLen > 0) {
					tokens.add(new Token(String.valueOf(buff, 0, buffLen)));
					buffLen = 0;
				}

				// check for legal punctuation
				if (repl != NULL) {
					tokens.add(new Token(String.valueOf((char)c)));
				}
			} else {
				buff[buffLen++] = repl;
			}
		}
		
		// clear the buffer.
		if (buffLen > 0) {
			tokens.add(new Token(String.valueOf(buff, 0, buffLen)));
		}
		
		return tokens;
	}
	
	/**
	 * TODO: implement tokenize(String[])
	 */
	public List tokenize(String[] s) {
		throw new UnsupportedOperationException();
	}
	
	private void recalcTranslationTable() {
		tokenDelim = new boolean[MAX_CHAR + 1];
		Arrays.fill(tokenDelim, true);

		translationTable = new char[128];
		Arrays.fill(translationTable, NULL);
		
		for (int i = 0; i < legalPunctuation.length(); i++) {
			char c = legalPunctuation.charAt(i);
			if (c == NULL) {
				throw new IllegalStateException("The null character can never be legal punctuation!");
			}
			tokenDelim[c] = splitOnLegalPunctuation;
			translationTable[c] = c;
		}
		
		for (int i = 0; i <= MAX_CHAR; i++) {
			char c = (char)i;
			if (Character.isDigit(c)) {
				tokenDelim[c] = false;
				translationTable[c] = c;
			} else if (Character.isLetter(c)) {
				tokenDelim[c] = false;
				translationTable[c] = convertToUpper ? Character.toUpperCase(c) : c;
			}
		}
	}

	private static final int MAX_CHAR = 127;
	private static final char NULL = '\0';

}
