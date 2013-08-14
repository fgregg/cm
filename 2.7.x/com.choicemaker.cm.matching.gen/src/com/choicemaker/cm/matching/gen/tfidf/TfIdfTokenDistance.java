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

import java.util.Iterator;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:05 $
 */
public class TfIdfTokenDistance implements TokenDistanceFunction {

	protected Tokenizer tokenizer;
	protected WeightingFunction wf;

	public TfIdfTokenDistance() {
		this(DefaultTokenizer.instance());
	}
	
	public TfIdfTokenDistance(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public TfIdfTokenDistance(WeightingFunction wf) {
		this(DefaultTokenizer.instance(), wf);
	}

	public TfIdfTokenDistance(Tokenizer tokenizer, WeightingFunction wf) {
		this.tokenizer = tokenizer;
		this.wf = wf;
	}

	public float distance(String s1, String s2) {
		if (wf != null) {
			return distance(new TokenBag(tokenizer.tokenize(s1), wf), 
							new TokenBag(tokenizer.tokenize(s2), wf));
		} else {
			return distance(new TokenBag(tokenizer.tokenize(s1)), 
							new TokenBag(tokenizer.tokenize(s2)));
		}
	}

	/**
	 * Expects two normalized token bags.
	 */
	public float distance(TokenBag b1, TokenBag b2) {
		if (b1.size() > b2.size()) {
			TokenBag temp = b1;
			b1 = b2;
			b2 = temp;
		}
		
		float sim = 0f;
		
		Iterator it = b1.tokenIterator();
		while (it.hasNext()) {
			String t = (String)it.next();
			float w2 = b2.weight(t);
			if (w2 > 0) {
				sim += b1.weight(t) * w2;
			}
		}
		
		return sim;
	}

}
