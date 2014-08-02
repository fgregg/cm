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
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class CascadedParser implements Parser {
	
	public static final String PARSER_NAME = "ParseName";

	protected String name;
	protected List parsers;
	protected int size;

	public CascadedParser() {
		parsers = new ArrayList();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addParser(Parser parser) {
		parsers.add(parser);
		size = parsers.size();
	}

	public int size() {
		return size;
	}

	public ParsedData getBestParse(String s) {
		for (int i = 0; i < size; i++) {
			Parser parser = (Parser) parsers.get(i);
			ParsedData pd = parser.getBestParse(s);
			if (pd != null) {
				pd.put(PARSER_NAME, parser.getName());
				return pd;
			}
		}
		
		return null;
	}

	public ParsedData getBestParse(String[] s) {
		for (int i = 0; i < size; i++) {
			Parser parser = (Parser) parsers.get(i);
			ParsedData pd = parser.getBestParse(s);
			if (pd != null) {
				pd.put(PARSER_NAME, parser.getName());
				return pd;
			}
		}
		
		return null;
	}

	//
	// Method stubs because of Parser's specification.
	//

	public void setSymbolFactory(SymbolFactory sf) {
		throw new UnsupportedOperationException();
	}
	
	public void setTokenizer(Tokenizer t) {
		throw new UnsupportedOperationException();
	}

	public void addTokenizer(Tokenizer t) {
		throw new UnsupportedOperationException();
	}

	public void setGrammar(ContextFreeGrammar g) {
		throw new UnsupportedOperationException();
	}

	public void setStandardizer(ParseTreeNodeStandardizer s) {
		throw new UnsupportedOperationException();
	}

	public void setParsedDataClass(Class cls) {
		throw new UnsupportedOperationException();
	}

	public SymbolFactory getSymbolFactory() {
		throw new UnsupportedOperationException();
	}

	public Tokenizer[] getTokenizers() {
		throw new UnsupportedOperationException();
	}

	public ContextFreeGrammar getGrammar() {
		throw new UnsupportedOperationException();
	}

	public ParseTreeNodeStandardizer getStandardizer() {
		throw new UnsupportedOperationException();
	}

	public Class getParsedDataClass() {
		throw new UnsupportedOperationException();
	}
	
	public ParsedData[] getAllParses(String s) {
		throw new UnsupportedOperationException();
	}

	public ParsedData[] getAllParses(String[] s) {
		throw new UnsupportedOperationException();
	}

	public ParseTreeNode getBestParseTree(String s) {
		throw new UnsupportedOperationException();
	}

	public ParseTreeNode getBestParseTree(String[] s) {
		throw new UnsupportedOperationException();
	}

	public ParseTreeNode[] getAllParseTrees(String s) {
		throw new UnsupportedOperationException();
	}

	public ParseTreeNode[] getAllParseTrees(String[] s) {
		throw new UnsupportedOperationException();
	}

	public List[] getAllTokenizations(String s) {
		throw new UnsupportedOperationException();
	}

	public List[] getAllTokenizations(String[] s) {
		throw new UnsupportedOperationException();
	}	

}
