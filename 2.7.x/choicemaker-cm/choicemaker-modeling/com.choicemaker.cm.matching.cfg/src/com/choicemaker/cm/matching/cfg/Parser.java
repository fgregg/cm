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

import java.util.List;

/**
 * .
 * 
 * @author Adam Winkel
 */
public interface Parser {
	public void setName(String name);
	public String getName();
	
	public void setSymbolFactory(SymbolFactory sf);
	public void setTokenizer(Tokenizer t);
	public void addTokenizer(Tokenizer t);
	public void setGrammar(ContextFreeGrammar g);
	public void setStandardizer(ParseTreeNodeStandardizer s);
	public void setParsedDataClass(Class cls);
	
	public SymbolFactory getSymbolFactory();
	public Tokenizer[] getTokenizers();
	public ContextFreeGrammar getGrammar();
	public ParseTreeNodeStandardizer getStandardizer();
	public Class getParsedDataClass();
	
	public ParsedData getBestParse(String s);
	public ParsedData getBestParse(String[] s);
	public ParsedData[] getAllParses(String s);
	public ParsedData[] getAllParses(String[] s);
	public ParseTreeNode getBestParseTree(String s);
	public ParseTreeNode getBestParseTree(String[] s);
	public ParseTreeNode[] getAllParseTrees(String s);
	public ParseTreeNode[] getAllParseTrees(String[] s);
	public List[] getAllTokenizations(String s);
	public List[] getAllTokenizations(String[] s);
}
