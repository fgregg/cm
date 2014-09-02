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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;

import com.choicemaker.cm.matching.cfg.util.CfgUtils;

/**
 * .
 * 
 * @author Adam Winkel
 */
public abstract class AbstractParser implements Parser {

	protected String name;

	protected SymbolFactory symbolFactory;
	protected Tokenizer[] tokenizers;
	protected ContextFreeGrammar grammar;
	protected ParseTreeNodeStandardizer standardizer;
	
	protected Class parsedDataClass;
	
	protected AbstractParser() {
		tokenizers = new Tokenizer[0];
		parsedDataClass = ParsedData.class;
	}
	
	public AbstractParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		this(t, g, s, null);	
	}
	
	public AbstractParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		this(t, g, s, null);	
	}
	
	public AbstractParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		this(new Tokenizer[] {t}, g, s, c);
	}

	public AbstractParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		setTokenizers(t);
		setGrammar(g);
		setStandardizer(s);

		setParsedDataClass(c != null ? c : ParsedData.class);		
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setSymbolFactory(SymbolFactory sf) {
		this.symbolFactory = sf;
	}
	
	public SymbolFactory getSymbolFactory() {
		return symbolFactory;
	}

	public void addTokenizer(Tokenizer t) {
		Tokenizer[] cur = this.tokenizers;
		tokenizers = new Tokenizer[cur.length + 1];
		System.arraycopy(cur, 0, tokenizers, 0, cur.length);
		tokenizers[cur.length] = t;
	}

	public void setTokenizer(Tokenizer t) {
		this.tokenizers = new Tokenizer[] {t};
	}
	
	public void setTokenizers(Tokenizer[] t) {
		this.tokenizers = new Tokenizer[t.length];
		System.arraycopy(t, 0, tokenizers, 0, t.length);
	}
	
	public Tokenizer[] getTokenizers() {
		Tokenizer[] t = new Tokenizer[tokenizers.length];
		System.arraycopy(tokenizers, 0, t, 0, t.length);
		return t;
	}
	
	public void setGrammar(ContextFreeGrammar g) {
		this.grammar = g;
	}
	
	public ContextFreeGrammar getGrammar() {
		return grammar;
	}
	
	public void setStandardizer(ParseTreeNodeStandardizer s) {
		this.standardizer = s;
	}
	
	public ParseTreeNodeStandardizer getStandardizer() {
		return standardizer;
	}
	
	public void setParsedDataClass(Class cls) {
		this.parsedDataClass = cls;
	}
	
	public Class getParsedDataClass() {
		return parsedDataClass;
	}

	public ParsedData getBestParse(String s) {
		ParseTreeNode best = getBestParseTree(s);
		if (best != null) {
			ParsedData d = getParsedDataInstance();
			standardizer.standardize(best, d);
			return d;	
		} else {
			return null;
		}
	}
	
	public ParsedData getBestParse(String[] s) {
		ParseTreeNode best = getBestParseTree(s);
		if (best != null) {
			ParsedData d = getParsedDataInstance();
			standardizer.standardize(best, d);
			return d;
		} else {
			return null;	
		}
	}
	
	public ParsedData[] getAllParses(String s) {
		HashSet parsedDataHash = new HashSet();

		ParseTreeNode[] parseTrees = getAllParseTrees(s);
		for (int i = 0; i < parseTrees.length; i++) {
			ParsedData d = getParsedDataInstance();
			standardizer.standardize(parseTrees[i], d);
			d.setProbability(parseTrees[i].getProbability());
			parsedDataHash.add(d);
		}
		
		ParsedData[] ret = new ParsedData[parsedDataHash.size()];
		Iterator itParsedData = parsedDataHash.iterator();
		int i = 0;
		while (itParsedData.hasNext()) {
			ret[i++] = (ParsedData) itParsedData.next();	
		}
		CfgUtils.sortParsedDataHolders(ret);
		return ret;
	}		
		
	public ParsedData[] getAllParses(String[] s) {
		HashSet parsedDataHash = new HashSet();

		ParseTreeNode[] parseTrees = getAllParseTrees(s);
		for (int i = 0; i < parseTrees.length; i++) {
			ParsedData d = getParsedDataInstance();
			standardizer.standardize(parseTrees[i], d);
			d.setProbability(parseTrees[i].getProbability());
			parsedDataHash.add(d);
		}
		
		ParsedData[] ret = new ParsedData[parsedDataHash.size()];
		Iterator itParsedData = parsedDataHash.iterator();
		int i = 0;
		while (itParsedData.hasNext()) {
			ret[i++] = (ParsedData) itParsedData.next();	
		}
		CfgUtils.sortParsedDataHolders(ret);
		return ret;
	}
	
	
	protected ParsedData getParsedDataInstance() {
		try {
			return (ParsedData)parsedDataClass.newInstance();
		} catch (IllegalAccessException ex) {
			Logger.getLogger(getClass().getName()).severe(ex.toString());
			return null;
		} catch (InstantiationException ex) {
			Logger.getLogger(getClass().getName()).severe(ex.toString());
			return null;
		}
	}
	
	//
	// *********************** Public tokenizatoin methods *************
	//
	
	public ParseTreeNode getBestParseTree(String s) {
		List[] tokens = getAllTokenizations(s);

		ParseTreeNode best = null;
		double bestProb = -1.0;
		for (int t = 0; t < tokens.length; t++) {
			ParseTreeNode tree = getBestParseTreeFromParser(tokens[t]);
			if (tree != null) {
				double treeProb = tree.getProbability();
				if (treeProb > bestProb) {
					best = tree;
					bestProb = treeProb;
				}
			}
		}
		return best;
	}
	
	public ParseTreeNode getBestParseTree(String[] s) {
		List[] tokens = getAllTokenizations(s);

		ParseTreeNode best = null;
		double bestProb = -1.0;
		for (int t = 0; t < tokens.length; t++) {
			ParseTreeNode tree = getBestParseTreeFromParser(tokens[t]);
			if (tree != null) {
				double treeProb = tree.getProbability();
				if (treeProb > bestProb) {
					best = tree;
					bestProb = treeProb;	
				}
			}
		}
		return best;
	}
	
	public ParseTreeNode[] getAllParseTrees(String s) {
		HashSet parseTreeHash = new HashSet();
		
		List[] tokenizations = getAllTokenizations(s);	
		for (int i = 0; i < tokenizations.length; i++) {
			ParseTreeNode[] treeArray = getAllParseTreesFromParser(tokenizations[i]);
			for (int j = 0; j < treeArray.length; j++) {
				parseTreeHash.add(treeArray[j]);
			}
		}

		ParseTreeNode[] ret = new ParseTreeNode[parseTreeHash.size()];
		Iterator itTrees = parseTreeHash.iterator();
		int i = 0;
		while (itTrees.hasNext()) {
			ret[i++] = (ParseTreeNode)itTrees.next();
		}
		CfgUtils.sortParseTrees(ret);
		
		return ret;
	}
	
	public ParseTreeNode[] getAllParseTrees(String[] s) {
		HashSet parseTreeHash = new HashSet();
		
		List[] tokenizations = getAllTokenizations(s);	
		for (int i = 0; i < tokenizations.length; i++) {
			ParseTreeNode[] treeArray = getAllParseTreesFromParser(tokenizations[i]);
			for (int j = 0; j < treeArray.length; j++) {
				parseTreeHash.add(treeArray[j]);
			}
		}

		ParseTreeNode[] ret = new ParseTreeNode[parseTreeHash.size()];
		Iterator itTrees = parseTreeHash.iterator();
		int i = 0;
		while (itTrees.hasNext()) {
			ret[i++] = (ParseTreeNode)itTrees.next();
		}
		CfgUtils.sortParseTrees(ret);
		
		return ret;
	}
	
	//
	// **************************** Getting Tokenizations *********************
	//
	
	public List getTokenization(String s) {
		return tokenizers[0].tokenize(s);	
	}
	
	/**
	 * Returns the tokenization produced by the first (and perhaps only)
	 * Tokenizer passed to the Constructor.
	 */
	public List getTokenization(String[] s) {
		return tokenizers[0].tokenize(s);
	}

	public List[] getAllTokenizations(String s) {
		HashSet tokenizations = new HashSet();
		for (int i = 0; i < tokenizers.length; i++) {
			List tokens = tokenizers[i].tokenize(s);
			if (tokens.size() > 0) {
				tokenizations.add(tokens);
			}
		}
		
		List[] ret = new List[tokenizations.size()];
		Iterator itTokenizations = tokenizations.iterator();
		int i = 0;
		while (itTokenizations.hasNext()) {
			ret[i++] = (List) itTokenizations.next();
		}
		return ret;
	}
	
	/**
	 * Returns all <b>unique</b> tokenizations produced by all the 
	 * the tokenizers, that is, an array of Lists of Strings.
	 */
	public List[] getAllTokenizations(String[] s) {
		HashSet tokenizations = new HashSet();
		for (int i = 0; i < tokenizers.length; i++) {
			List tokens = tokenizers[i].tokenize(s);
			if (tokens.size() > 0) {
				tokenizations.add(tokens);	
			}
		}

		List[] ret = new List[tokenizations.size()];
		Iterator itTokenizations = tokenizations.iterator();
		int i = 0;
		while (itTokenizations.hasNext()) {
			ret[i++] = (List) itTokenizations.next();
		}
		return ret;
	}
	
	//
	// *********************** Abstract Methods ****************************
	//
	
	protected abstract ParseTreeNode getBestParseTreeFromParser(List tokens);
	protected abstract ParseTreeNode[] getAllParseTreesFromParser(List tokens);
	
}
