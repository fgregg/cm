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
import java.util.Set;

import com.choicemaker.cm.core.util.FloatValuedHashMap;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:05 $
 */
public class TokenBag {

	protected FloatValuedHashMap weights = new FloatValuedHashMap();
	protected boolean weighted;
	protected boolean normalized;
	
	public TokenBag(String[] tokens) {
		this(tokens, null, false);
	}
	
	public TokenBag(String[] tokens, WeightingFunction wfxn) {
		this(tokens, wfxn, true);
	}
	
	public TokenBag(String[] tokens, WeightingFunction wfxn, boolean normalize) {
		for (int i = tokens.length - 1; i >= 0; i--) {
			weights.increment(tokens[i]);
		}

		if (wfxn != null) {
			float norm = 0f;
			Iterator it = weights.keySet().iterator();
			while (it.hasNext()) {
				String tok = (String)it.next();
				float w = (float) Math.log(weights.getFloat(tok) + 1) * wfxn.weight(tok);
				weights.putFloat(tok, w);
				norm += w * w;
			}
			
			weighted = true;
			
			if (normalize && norm > 0) {
				norm = 1f / (float) Math.sqrt(norm);
				
				it = weights.keySet().iterator();
				while (it.hasNext()) {
					weights.multiply(it.next(), norm);
				}
				
				normalized = true;
			}
		}

	}
		
	public int size() {
		return weights.size();
	}

	public boolean hasToken(String token) {
		return weights.containsKey(token);
	}
	
	public float weight(String token) {
		return weights.getFloat(token);
	}

	public Set tokenSet() {
		return weights.keySet();
	}
	
	public Iterator tokenIterator() {
		return weights.keySet().iterator();
	}

}
