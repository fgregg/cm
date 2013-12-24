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
package com.choicemaker.cm.matching.cfg.cyk;

import java.util.List;

import com.choicemaker.cm.matching.cfg.*;
import com.choicemaker.cm.matching.cfg.cnf.*;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class CykParser extends AbstractParser {

	private CykParserChart cykParserChart;

	public CykParser() {
		// for subclasses...
	}

	public CykParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		super(t, g, s);
	}
	
	public CykParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s) {
		super(t, g, s);	
	}
	
	public CykParser(Tokenizer t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		super(t, g, s, c);
	}
	
	public CykParser(Tokenizer[] t, ContextFreeGrammar g, ParseTreeNodeStandardizer s, Class c) {
		super(t, g, s, c);	
	}

	public void setGrammar(ContextFreeGrammar g) {
		super.setGrammar(g);
		cykParserChart = new CykParserChart(new NearlyCnfGrammar(g));
	}

	protected ParseTreeNode getBestParseTreeFromParser(List tokens) {
		return cykParserChart.getBestParseTree(tokens);
	}

	protected ParseTreeNode[] getAllParseTreesFromParser(List tokens) {
		ParseTreeNode ptn = getBestParseTreeFromParser(tokens);
		if (ptn == null) {
			return new ParseTreeNode[0];	
		} else {
			return new ParseTreeNode[] {ptn};
		}
	}

}
