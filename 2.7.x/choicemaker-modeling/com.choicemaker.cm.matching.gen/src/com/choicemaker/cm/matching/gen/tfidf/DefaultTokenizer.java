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
package com.choicemaker.cm.matching.gen.tfidf;

import java.util.StringTokenizer;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public class DefaultTokenizer implements Tokenizer {

	private static Tokenizer instance;
	
	public static Tokenizer instance() {
		if (instance == null) {
			instance = new DefaultTokenizer();
		}
		
		return instance;
	}

	public String[] tokenize(String s) {
		return defaultTokenize(s);
	}
	
	/**
	 * Tokenizes the input string by spaces, and interns the resulting tokens.
	 */
	public static String[] defaultTokenize(String s) {
		StringTokenizer toks = new StringTokenizer(s);
		String[] tokens = new String[toks.countTokens()];
		
		int i = 0;
		while (toks.hasMoreTokens()) {
			tokens[i++] = toks.nextToken().intern();
		}
		
		return tokens;
	}
		
	private DefaultTokenizer() { }

}
