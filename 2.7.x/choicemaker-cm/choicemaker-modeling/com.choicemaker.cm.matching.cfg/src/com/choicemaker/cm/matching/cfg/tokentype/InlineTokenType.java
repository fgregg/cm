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
package com.choicemaker.cm.matching.cfg.tokentype;

import java.util.Iterator;

import com.choicemaker.cm.core.util.DoubleValuedHashMap;
import com.choicemaker.cm.matching.cfg.TokenType;


/**
 * Represents a TokenType defined within the grammar.
 */
public class InlineTokenType extends TokenType {

	protected DoubleValuedHashMap tokenMap;

	public InlineTokenType(String name, String[] tokens, double[] probabilities) {
		super(name);
		tokenMap = new DoubleValuedHashMap();
		for (int i = 0; i < tokens.length; i++) {
			if (probabilities[i] < 0 || probabilities[i] > 1) {
				throw new IllegalArgumentException("Probability must be between 0 and 1: " + probabilities[i]);
			}
			tokenMap.putDouble(tokens[i], probabilities[i]);
		}
	}

	public boolean canHaveToken(String token) {
		// 2014-04-24 rphall: Commented out unused local variable.
		// and unused 'if' statement
//		if (token.equals("#")) {
//			String s = "";
//		}
		return tokenMap.containsKey(token);
	}

	public double getTokenProbability(String token) {
		if (tokenMap.containsKey(token)) {
			return tokenMap.getDouble(token);
		} else {
			return 0;
		}
	}

	public String getDefinitionString() {
		StringBuffer s = new StringBuffer();

		s.append(name);
		s.append(' ');
		s.append(":=");

		int count = 0;
		Iterator it = tokenMap.keySet().iterator();
		while (it.hasNext()) {
			if (count++ > 0) {
				s.append(' ');
				s.append('|');
			}

			Object key = it.next();
			s.append(' ');
			s.append(key);
			s.append(' ');
			s.append('[');
			s.append(tokenMap.getDouble(key));
			s.append(']');
		}

		return s.toString();
	}

}
