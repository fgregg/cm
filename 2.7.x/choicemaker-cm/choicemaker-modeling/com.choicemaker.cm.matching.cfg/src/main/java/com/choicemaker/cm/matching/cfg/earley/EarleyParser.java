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
package com.choicemaker.cm.matching.cfg.earley;

import java.util.List;

import com.choicemaker.cm.matching.cfg.AbstractParser;
import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.Tokenizer;

/**
 * 
 * 
 * @author Adam Winkel
 */
public class EarleyParser extends AbstractParser {

	public EarleyParser() {
		super();
	}

	public EarleyParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		super(t, g, s);
	}
	
	public EarleyParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		super(t, g, s);	
	}
	
	public EarleyParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		super(t, g, s, c);
	}
	
	public EarleyParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		super(t, g, s, c);
	}

	protected ParseTreeNode getBestParseTreeFromParser(List tokens) {
		EarleyParserChart chart = new EarleyParserChart(tokens, grammar);
		if (chart.isParsed()) {
			return chart.getBestParseTree();
		} else {
			return null;	
		}
	}

	protected ParseTreeNode[] getAllParseTreesFromParser(List tokens) {
		EarleyParserChart chart = new EarleyParserChart(tokens, grammar);

		ParseTreeNode[] ret = new ParseTreeNode[chart.getNumParseTrees()];			
		for (int i = 0; i < chart.getNumParseTrees(); i++) {
			ret[i] = chart.getParseTree(i);
		}

		return ret;
	}

}
