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

import java.util.*;
import com.choicemaker.cm.core.util.*;

/**
 * @author ajwinkel
 *
 */
public class TfIdfCalculator {

	protected Tokenizer tokenizer;
	protected IntValuedHashMap counts = new IntValuedHashMap();
	protected int numLines = 0;

	public TfIdfCalculator() {
		this(new DefaultIdfTokenizer());
	}

	public TfIdfCalculator(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public synchronized void chunkAndAdd(String line) {
		addChunks( tokenizer.tokenize(line) );
	}
	
	public synchronized void addChunks(String[] words) {
		HashSet seen = new HashSet();
		for (int i = words.length - 1; i >= 0; i--) {
			if (!seen.contains(words[i])) {
				add(words[i]);
				seen.add(words[i]);
			}
		}
		numLines++;
	}
	
	public synchronized void addChunks(Set words) {
		Iterator itWords = words.iterator();
		while (itWords.hasNext()) {
			add((String)itWords.next());
		}
		numLines++;
	}
	
	protected synchronized void add(String word) {
		counts.increment(word);
	}

	public int getNumLines() {
		return numLines;
	}
	
	public Set getTokens() {
		return counts.keySet();
	}
	
	public List getSortedTokens() {
		return counts.sortedKeys();
	}
	
	public int getCount(String token) {
		return counts.getInt(token);
	}
	
	public double getIdf(String token) {
		double count = getCount(token);
		if (count == 0) {
			return -1;
		} else {
			return 1 + Math.log(numLines/count);
		}
	}
	
	public synchronized void printCounts() {
		List words = counts.sortedKeys();
		for (int i = 0; i < words.size(); i++) {
			System.out.println(words.get(i) + "\t\t" + counts.get(words.get(i)));
		}
	}

	public synchronized void printIdf() {
		DoubleValuedHashMap idf = computeIdf();
		List words = idf.sortedKeys();
		Collections.reverse(words);
		for (int i = 0; i < words.size(); i++) {
			Object w = words.get(i);
			System.out.println(w + "\t\t" + idf.get(w));
		}
	}
	
	public synchronized void printIdfMap() {
		DoubleValuedHashMap idf = computeIdf();
		List words = idf.sortedKeys();
		Collections.reverse(words);
		for (int i = 0; i < words.size(); i++) {
			Object w = words.get(i);
			System.out.println(w);
			System.out.println(idf.get(w));
		}
	}

	public synchronized DoubleValuedHashMap computeIdf() {
		return computeIdf(2);
	}

	public synchronized DoubleValuedHashMap computeIdf(int minOccurs) {
		DoubleValuedHashMap idfMap = new DoubleValuedHashMap();
		Iterator words = counts.keySet().iterator();
		while (words.hasNext()) {
			String w = (String) words.next();
			double count = counts.getInt(w);
			if (count >= minOccurs) {
				double idf = 1 + Math.log(numLines/count);
				idfMap.putDouble(w, idf);
			}
		}
		
		return idfMap;
	}

}
