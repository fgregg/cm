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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.Rule;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.Variable;
import com.choicemaker.cm.matching.cfg.cnf.NearlyCnfGrammar;
import com.choicemaker.util.IntValuedHashMap;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class CykParserChart {

	protected NearlyCnfGrammar ncnfGrammar;

	/**
	 * numVariables is the length of the variables array.
	 * the terminals are indexed [0..numNonTokenTypes)
	 * the token types are indexed [numNonTokenTypes..numVariables)
	 */
	protected int numVariables;
	protected int numNonTokenTypes;
	protected Variable[] variables;
	protected IntValuedHashMap variableIndices;

	/**
	 * The ith entry is the index of the first rule with
	 * variable i as its left-hand side.
	 */
	protected int[] firstRule;
	
	/**
	 * The ith entry is one greater than the index of
	 * the last rule with variable i as its left-hand side.
	 */
	protected int[] lastRule;

	protected int numRules;
	protected int numNonTokenTypeUnitRules;
	protected Rule[] rules;
	protected IntValuedHashMap ruleIndices;
	
	protected int[] ruleLhs;
	protected int[] ruleRhs1;
	protected int[] ruleRhs2;
	protected float[] ruleProb;

	// Parsing structures

	protected List tokens;
	protected int numTokens;

	public float[][][] probChart;
	public int[][][] kChart;
	public int[][][] ruleChart;
	
	public CykParserChart(NearlyCnfGrammar g) {
		ncnfGrammar = g;
		initGrammarStructures();
	}

	public void printVariables() {
		for (int i = 0; i < numVariables; i++) {
			System.out.println(i + ": " + variables[i]);	
		}
	}

	public void printRules() {
		for (int i = 0; i < numRules; i++) {
			System.out.println(i + ": " + rules[i] + " [" + rules[i].getProbability() + "]");	
		}
	}

	public synchronized ParseTreeNode getBestParseTree(List tokens) {
		this.tokens = tokens;
		numTokens = tokens.size();
		initParseChart();
		fillInParseChart();
		
		return recoverParseTree();
	}

	private void initParseChart() {
		probChart = new float[numVariables][][];
		kChart = new int[numVariables][][];
		ruleChart = new int[numVariables][][];
		for (int v = 0; v < numVariables; v++) {
			probChart[v] = new float[numTokens][];
			kChart[v] = new int[numTokens][];
			ruleChart[v] = new int[numTokens][];
			for (int i = 0; i < numTokens; i++) {
				probChart[v][i] = new float[numTokens];
				kChart[v][i] = new int[numTokens];
				ruleChart[v][i] = new int[numTokens];

				Arrays.fill(probChart[v][i], -1.0f);
				Arrays.fill(kChart[v][i], -1);
				Arrays.fill(ruleChart[v][i], -1);
			}
		}
	}

	private void fillInParseChart() {
		fillInTokenTypeToTokenRules();
		fillInNonTerminalToTokenTypeRules();
		fillInNonTerminalToNonTerminalRules();
	}

	/**
	 * Fills in the pseudo-rules of TokenType to token.
	 * These rules are not explicitly in the grammar.
	 */
	private void fillInTokenTypeToTokenRules() {
		for (int v = numNonTokenTypes; v < numVariables; v++) {
			TokenType tt = (TokenType) variables[v];
			for (int i = 0; i < numTokens; i++) {
				Token t = (Token) tokens.get(i);
				if (tt.canHaveToken(t)) {
					probChart[v][i][i] = (float) tt.getTokenProbability(t);
					kChart[v][i][i] = i;
					ruleChart[v][i][i] = numRules; // redundant, but we do it anyway...
				}
			}
		}
	}
	
	/**
	 * Do the rules that are A --> B, where B is a token type.
	 */
	private void fillInNonTerminalToTokenTypeRules() {
		for (int r = numNonTokenTypeUnitRules; r < numRules; r++) {
			int lhs = ruleLhs[r];
			int rhs = ruleRhs1[r];
			for (int i = 0; i < numTokens; i++) {
				if (ruleChart[rhs][i][i] >= 0) {
					float p = probChart[rhs][i][i] * ruleProb[r];
					if (p > probChart[lhs][i][i]) {
						probChart[lhs][i][i] = probChart[rhs][i][i] * ruleProb[r];
						kChart[lhs][i][i] = i;
						ruleChart[lhs][i][i] = r;		
					}
				}
			}
		} 
	}
	
	private void fillInNonTerminalToNonTerminalRules() {
		int last, first;
		int lhs, rhs1, rhs2;
		float p;
		for (int j = 1; j < numTokens; j++) {
			for (int i = j-1; i >= 0; i--) {
				for (int k = i; k < j; k++) {
					for (int v = 0; v < numNonTokenTypes; v++) {
						last = lastRule[v];
						first = firstRule[v];
						for (int r = first; r < last; r++) {
							rhs1 = ruleRhs1[r];
							rhs2 = ruleRhs2[r];
							if (ruleChart[rhs1][i][k] >= 0 && ruleChart[rhs2][k+1][j] >= 0) {
								lhs = ruleLhs[r];
								p = ruleProb[r] * probChart[rhs1][i][k] * probChart[rhs2][k+1][j];
								if (ruleChart[lhs][i][j] < 0 || p > probChart[lhs][i][j]) {
									probChart[lhs][i][j] = p;
									kChart[lhs][i][j] = k;
									ruleChart[lhs][i][j] = r;
								}
							}
						}
					}
				}
			}
		}
	}

//	private ParseTreeNode recoverNcnfParseTree() {
//		return recoverNcnfParseTree(0, 0, numTokens - 1);
//	}

//	private ParseTreeNode recoverNcnfParseTree(int v, int i, int j) {
//		int r = ruleChart[v][i][j];
//		int k = kChart[v][i][j];
//
//		if (r < 0) {
//			return null;
//		} else if (r == numRules) {
//			Rule rule = new Rule((TokenType)variables[v], (Token) tokens.get(i));
//			return new ParseTreeNode(rule);
//		} else {
//			List kids = new ArrayList(2);
//			kids.add(recoverParseTree(ruleRhs1[r], i, k));
//			if (ruleRhs2[r] >= 0) {
//				kids.add(recoverParseTree(ruleRhs2[r], k+1, j));
//			}
//			return new ParseTreeNode(rules[r], kids);
//		}
//	}
	
	private ParseTreeNode recoverParseTree() {
		ParseTreeNode ptn = recoverParseTree(0, 0, numTokens - 1);
		if (ptn == null) {
			return null;	
		} else {
			return ptn.getChild(0);
		}
	}
		
	private ParseTreeNode recoverParseTree(int v, int i, int j) {
		int r = ruleChart[v][i][j];
		int k = kChart[v][i][j];

		if (r < 0) {
			return null;
		} else if (r == numRules) {
			Rule rule = new Rule((TokenType)variables[v], (Token) tokens.get(i));
			return new ParseTreeNode(rule);
		} else {
			if (rules[r] instanceof NearlyCnfGrammar.HeadCascadedRule) {
				return recoverCascadedRule(v, i, j);
			} else if (rules[r] instanceof NearlyCnfGrammar.SquashedRule) {
				return recoverSquashedRule(v, i, j);
			} else {
				List kids = new ArrayList(2);
				kids.add(recoverParseTree(ruleRhs1[r], i, k));
				if (ruleRhs2[r] >= 0) {
					kids.add(recoverParseTree(ruleRhs2[r], k+1, j));
				}
				return new ParseTreeNode(rules[r], kids);
			}
		}
	}
	
	private ParseTreeNode recoverCascadedRule(int v, int i, int j) {
		int r = ruleChart[v][i][j];
		int k = kChart[v][i][j];

		Rule base = ((NearlyCnfGrammar.HeadCascadedRule)rules[r]).getBaseRule();
		int rhsSize = base.getRhsSize();
		List kids = new ArrayList(rhsSize);
		
		int newR = r;
		int newI = i;
		int newK = k;
		int newV = v;
		for (int index = 0; index < rhsSize - 2; index++) {
			ParseTreeNode rhs1 = recoverParseTree(ruleRhs1[newR], newI, newK);
			kids.add(rhs1);
			
			newV = ruleRhs2[newR];
			newI = newK+1;

			newR = ruleChart[newV][newI][j];
			newK = kChart[newV][newI][j];
		}

		ParseTreeNode ptn;

		ptn = recoverParseTree(ruleRhs1[newR], newI, newK);
		kids.add(ptn);
		
		ptn = recoverParseTree(ruleRhs2[newR], newK+1, j);
		kids.add(ptn);

		if (base instanceof NearlyCnfGrammar.SquashedRule) {
			return unsquash((NearlyCnfGrammar.SquashedRule)base, kids);
		} else {
			return new ParseTreeNode(base, kids);
		}
	}
	
	private ParseTreeNode unsquash(NearlyCnfGrammar.SquashedRule r, List kids) {
		List squashed = r.getRules();
		int size = squashed.size();
		
		ParseTreeNode ret = new ParseTreeNode((Rule)squashed.get(--size), kids);
		while (--size >= 0) {
			kids = new ArrayList(1);
			kids.add(ret);
			ret = new ParseTreeNode((Rule)squashed.get(size), kids);
		}
		
		return ret;
	}
	
	private ParseTreeNode recoverSquashedRule(int v, int i, int j) {
		int r = ruleChart[v][i][j];
		int k = kChart[v][i][j];

		NearlyCnfGrammar.SquashedRule rule = (NearlyCnfGrammar.SquashedRule)rules[r];
		List squashed = rule.getRules();
		int squashedSize = squashed.size();

		// bottom level
		List kids = new ArrayList(2);
		kids.add(recoverParseTree(ruleRhs1[r], i, k));
		if (ruleRhs2[r] >= 0) {
			kids.add(recoverParseTree(ruleRhs2[r], k+1, j));
		}		
		ParseTreeNode ret = new ParseTreeNode((Rule)squashed.get(squashedSize - 1), kids);

		// successively higher levels
		for (int index = squashedSize - 2; index >= 0; index--) {
			kids = new ArrayList(1);
			kids.add(ret);
			ret = new ParseTreeNode((Rule)squashed.get(index), kids);
		}

		return ret;
	}
		
	private void initGrammarStructures() {
		
		// *************** Initialize the Variable Structures *****************
		
		List vList = ncnfGrammar.getVariables();
		final Variable sv = ncnfGrammar.getStartVariable();
		Comparator vc = new Comparator() {
			public int compare(Object obj1, Object obj2) {
				return obj1 == sv ? -1 : 
					obj2 == sv ? 1 : 
					obj1 instanceof TokenType ? 1 :
					obj2 instanceof TokenType ? -1 :
					0;
			}
		};
		Collections.sort(vList, vc);
		
		numVariables = vList.size();
		variables = new Variable[numVariables];
		variableIndices = new IntValuedHashMap();

		// these two filled in later		
		firstRule = new int[numVariables];
		lastRule = new int[numVariables];
		Arrays.fill(firstRule, -1);
		Arrays.fill(lastRule, -1);
				
		// assign indices to the variables by their order in the list.
		// note that the start variable is index 0, other non token-type variables
		// are next, followed by TokenTypes.
		numNonTokenTypes = 0;  // also fill this in as we loop.
		for (int i = 0; i < numVariables; i++) {
			Variable v = (Variable) vList.get(i);
			variables[i] = v;
			variableIndices.putInt(v, i);
			if (!(v instanceof TokenType)) {
				numNonTokenTypes++;	
			}
		}

		// *************** Intialize the Rule Structures **********************
		
		List rList = ncnfGrammar.getRules();
		Comparator rc = new Comparator() {
			public int compare(Object obj1, Object obj2) {
				Rule r1 = (Rule) obj1;
				Rule r2 = (Rule) obj2;
				
				if ((r1.getRhsSize() == 1) == (r2.getRhsSize() == 1)) {
					int idx1 = variableIndices.getInt(r1.getLhs());
					int idx2 = variableIndices.getInt(r2.getLhs());
					return idx1 < idx2 ? -1 : idx1 > idx2 ? 1 : 0;
				} else if (r1.getRhsSize() == 1) {
					return 1;
				} else {
					return -1;
				}
			}
		};
		Collections.sort(rList, rc);
		
		numRules = rList.size();
		rules = new Rule[numRules];
		ruleIndices = new IntValuedHashMap();
		
		ruleLhs = new int[numRules];
		ruleRhs1 = new int[numRules];
		ruleRhs2 = new int[numRules];
		ruleProb = new float[numRules];
		
		int lastLhs = -1;
		for (int i = 0; i < numRules; i++) {
			Rule r = (Rule) rList.get(i);
			rules[i] = r;
			ruleIndices.putInt(r, i);
			
			Variable lhs = r.getLhs();
			int lhsIndex = variableIndices.getInt(lhs);
			ruleLhs[i] = lhsIndex;
			ruleRhs1[i] = variableIndices.getInt(r.getRhsSymbol(0));
			if (r.getRhsSize() > 1) {
				ruleRhs2[i] = variableIndices.getInt(r.getRhsSymbol(1));
			} else {
				ruleRhs2[i] = -1;
			}			
			ruleProb[i] = (float) r.getProbability();
			
			if (r.getRhsSize() > 1) {
				if (lastLhs != ruleLhs[i]) {
					this.firstRule[lhsIndex] = i;
				}
				this.lastRule[lhsIndex] = i+1;
			
				lastLhs = lhsIndex;
				numNonTokenTypeUnitRules++;
			}
		}
		
	}
		
}
