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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public class SoftTfIdfTokenDistance extends TfIdfTokenDistance {

	protected StringDistanceFunction df;
	protected float simCutoff;

	public SoftTfIdfTokenDistance(WeightingFunction wf, StringDistanceFunction df, float simCutoff) {
		this(DefaultTokenizer.instance(), wf, df, simCutoff);
	}

	public SoftTfIdfTokenDistance(Tokenizer t, WeightingFunction wf, StringDistanceFunction df, float simCutoff) {
		super(t, wf);
		
		this.df = df;
		this.simCutoff = simCutoff;
	}

	public float distance(TokenBag b1, TokenBag b2) {
		if (b1.size() > b2.size()) {
			TokenBag temp = b1;
			b1 = b2;
			b2 = temp;
		}

		List toks2 = null;
		int toks2Len = -1;

		float sim = 0f;
		Iterator it = b1.tokenIterator();
		while (it.hasNext()) {
			String token = (String) it.next();
			if (b2.hasToken(token)) {
				sim += b1.weight(token) * b2.weight(token);
			} else {
				if (toks2 == null) {
					toks2 = new ArrayList(b2.tokenSet());
					toks2Len = toks2.size();
				}
				
				String token2 = null;
				float tokenSim = 0f;
				for (int i = 0; i < toks2Len; i++) {
					String temp = (String) toks2.get(i);
					float tempSim = df.distance(token, temp);
					if (tempSim > tokenSim) {
						token2 = temp;
						tokenSim = tempSim;
					}
				}
				
				if (tokenSim >= simCutoff) {
					sim += b1.weight(token) * b2.weight(token2) * tokenSim;
				}
			}
		}
		
		return sim;
	}

}
