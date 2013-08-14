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
package com.choicemaker.cm.matching.cfg.train;

import java.io.*;
import java.util.*;

import com.choicemaker.cm.core.util.IntValuedHashMap;

import com.choicemaker.cm.matching.cfg.*;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:00 $
 */
public class GrammarTrainer {
	
	private ContextFreeGrammar grammar;
	
	private IntValuedHashMap variableCounts = new IntValuedHashMap();
	private IntValuedHashMap ruleCounts = new IntValuedHashMap();
	
	private Map tokenCounts = new HashMap();
	
	public GrammarTrainer(ContextFreeGrammar grammar) {
		this.grammar = grammar;
	}
	
	public GrammarTrainer(ContextFreeGrammar grammar, ParsedDataReader reader) throws IOException {
		this.grammar = grammar;
		readParseTrees(reader);
	}
	
	/**
	 * Gets only the first parse tree (if one exists) for each parsedDatum element.
	 */
	public void readParseTrees(ParsedDataReader reader) throws IOException {
		while (reader.next()) {
			ParseTreeNode ptn = reader.getParseTree();
			if (ptn != null) {
				addParseTree(ptn);
			}
		}
	}
	
	public void addAllParseTrees(Collection parseTrees) {
		Iterator it = parseTrees.iterator();
		while (it.hasNext()) {
			addParseTree((ParseTreeNode)it.next());
		}
	}
	
	public void addAllParseTrees(ParseTreeNode[] parseTrees) {
		for (int i = 0; i < parseTrees.length; i++) {
			addParseTree(parseTrees[i]);	
		}	
	}
	
	public void addParseTree(ParseTreeNode tree) {
		Rule r = tree.getRule();
		
		if (r.getLhs() instanceof TokenType) {
			Variable tokenType = r.getLhs();
			Symbol token = r.getRhsSymbol(0);
			
			IntValuedHashMap typeMap = (IntValuedHashMap) tokenCounts.get(tokenType);
			if (typeMap == null) {
				typeMap = new IntValuedHashMap();
				tokenCounts.put(tokenType, typeMap);
			}
			
			typeMap.increment(token);
		}
		
		incrementVariableCount(r.getLhs());
		incrementRuleCount(r);
		
		Iterator itKids = tree.getChildren().iterator();
		while (itKids.hasNext()) {
			addParseTree((ParseTreeNode)itKids.next());	
		}
	}
	
	protected void incrementVariableCount(Variable v) {
		variableCounts.putInt(v, variableCounts.getInt(v)+1);
	}
	
	protected void incrementRuleCount(Rule r) {
		ruleCounts.putInt(r, ruleCounts.getInt(r)+1);
	}
			
	public Collection getTokenTypes() {
		return tokenCounts.keySet();	
	}
	
	public IntValuedHashMap getTokenCounts(TokenType type) {
		return (IntValuedHashMap)tokenCounts.get(type);	
	}
	
	public ContextFreeGrammar getTrainedGrammar() {
		ContextFreeGrammar newGrammar = new ContextFreeGrammar(grammar.getStartVariable(), grammar.getRules());
		
		Iterator itVariables = newGrammar.getVariables().iterator();
		while (itVariables.hasNext()) {
			Variable v = (Variable)itVariables.next();
			double varCount = variableCounts.getInt(v);
			
			Iterator itRules = newGrammar.getRules(v).iterator();
			while (itRules.hasNext()) {
				Rule r = (Rule)itRules.next();
				int ruleCount = ruleCounts.getInt(r);
				
				if (varCount == 0) {
					r.setProbability(0);
				} else {
					r.setProbability(ruleCount/varCount);
				}
			}
		}
		
		return newGrammar;
	}

	/**
	 * Write the trained grammar to STDOUT.
	 */
	public void writeTrainedGrammar() throws IOException {
		writeTrainedGrammar(System.out);	
	}

	public void writeTrainedGrammar(String fileName) throws IOException {
		writeTrainedGrammar(new File(fileName));
	}
	
	public void writeTrainedGrammar(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		writeTrainedGrammar(fos);
		fos.close();		
	}

	public void writeTrainedGrammar(OutputStream os) throws IOException {
		writeTrainedGrammar(new PrintStream(os));
	}
	
	public void writeTrainedGrammar(PrintStream ps) throws IOException {
		ContextFreeGrammar trained = getTrainedGrammar();
		ps.println(trained);
	}
	
	/**
	 * Write the specified token type map to STDOUT.
	 */
	public void writeTokenTypeCounts(TokenType tt) throws IOException {
		writeTokenTypeCountsHeader(tt, System.out);
		writeTokenTypeCounts(tt, System.out);
	}
	
	private void writeTokenTypeCountsHeader(TokenType tt, PrintStream ps) throws IOException {
		ps.println("TokenType: " + tt);
		ps.println("---------------");
	}

	public void writeTokenTypeCounts(TokenType tt, String fileName) throws IOException {
		writeTokenTypeCounts(tt, new File(fileName));
	}
	
	public void writeTokenTypeCounts(TokenType tt, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		writeTokenTypeCounts(tt, fos);
		fos.close();	
	}
	
	public void writeTokenTypeCounts(TokenType tt, OutputStream os) throws IOException {
		writeTokenTypeCounts(tt, new PrintStream(os));
	}
	
	public void writeTokenTypeCounts(TokenType tt, PrintStream ps) throws IOException {
		IntValuedHashMap tokenCounts = getTokenCounts(tt);
		Iterator itTokens = tokenCounts.sortedKeys().iterator();
		while (itTokens.hasNext()) {
			Token tok = (Token) itTokens.next();
			ps.println(tok.toString());
			ps.println(tokenCounts.getInt(tok));
		}
	}
	
	/**
	 * Write the trained grammar and the all token type counts maps to STDOUT.
	 */
	public void writeAll(PrintStream ps) throws IOException {
		writeTrainedGrammar(ps);
		ps.println();
		
		Iterator itTokenTypes = getTokenTypes().iterator();
		while (itTokenTypes.hasNext()) {
			TokenType tt = (TokenType) itTokenTypes.next();
			writeTokenTypeCountsHeader(tt, ps);
			writeTokenTypeCounts(tt, ps);
			ps.println();
		}		
	}
	
	public void writeAll() throws IOException {
		writeAll(System.out);	
	}
	
}
