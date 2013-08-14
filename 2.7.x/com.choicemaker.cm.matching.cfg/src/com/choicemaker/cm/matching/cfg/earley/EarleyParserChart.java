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

import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.Rule;
import com.choicemaker.cm.matching.cfg.Symbol;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.Variable;

/**
 * Implementation of the Earley CFG parsing algorithm on top of
 * a ParserChart.
 * 
 * A nice resource on the Earley algorithm is
 * Speech and Language Processing by Daniel Jurafsky and James H. Martin,
 * for example.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 * @see ParserChart
 * @see ParserState
 */
public class EarleyParserChart extends ParserChart {

	/**
	 * Creates a new EarleyParserChart from the specified List of Tokens
	 * and grammar.  Also performs the actual parsing.
	 */
	public EarleyParserChart(List tokens, ContextFreeGrammar grammar) {
		super(tokens, grammar);
		earleyParse();
	}
	
	/**
	 * Create this parse chart using the Earley algorithm.
	 */
	protected void earleyParse() {
	
		// enqueue a fake start to start with (in case there are many S rules).
		enqueue(fakeRule, 0, 0, 0);

		// iterate over the buckets, and then the states in each bucket.
		for (int bucket = 0; bucket <= tokens.size(); bucket++) {
			while (hasUnexploredStates(bucket)) {
				ParserState state = nextUnexploredState(bucket);
				if (!state.isComplete()) {
					if (! (state.getNextSymbol() instanceof TokenType)) {
						predictor(state);
					} else if (state.getEndIndex() < tokens.size()) {
						scanner(state);
					}
				} else {
					completer(state);
				}
			}
		}
		
	}
	
	/**
	 * Add all possible subtrees for the next symbol in <code>state</code>'s
	 * rule, to this chart.
	 */
	protected void predictor(ParserState state) {
		List rules = grammar.getRules((Variable)state.getNextSymbol());
		for (int i = 0; i < rules.size(); i++) {
			Rule rule = (Rule) rules.get(i);
			enqueue(rule, 0, state.getEndIndex(), state.getEndIndex());
		}
	}

	/**
	 * See if <code>state</code>'s next symbol (which is a TokenType by the
	 * above) can be paired with the next token in the input.  That is, can the
	 * next input token take on that TokenType.
	 */	   
	protected void scanner(ParserState state) {
		TokenType type = (TokenType) state.getNextSymbol();
		int end = state.getEndIndex();
		
		Token tok = (Token) tokens.get(end);
		if (type.canHaveToken(tok)) {
			Rule lexRule = new Rule(type, tok);
			enqueue(lexRule, 1, end, end+1);
		}
	}
	
	/**
	 * Move the dot ahead one position in states where <code>state</code> is 
	 * a subtree expansion of the next symbol (the symbol after the current
	 * dot position).
	 */
	protected void completer(ParserState state) {
		Symbol lhs = state.getRule().getLhs();
		int start = state.getStartIndex();
		int end = state.getEndIndex();

		List incomplete = getIncompleteStates(start);
		for (int i = 0; i < incomplete.size(); i++) {
			ParserState oldState = (ParserState) incomplete.get(i);
			
			if (oldState.getNextSymbol().equals(lhs)) {								
				ParserState newState = enqueue(oldState.getRule(), 
											   oldState.getDotPosition() + 1,
											   oldState.getStartIndex(),
											   end);
												   
				//newState.generateAndAddChildLists(oldState.getChildLists(), state);
				newState.addBackPointer(oldState, state);
			}
		}
	}
		
}
