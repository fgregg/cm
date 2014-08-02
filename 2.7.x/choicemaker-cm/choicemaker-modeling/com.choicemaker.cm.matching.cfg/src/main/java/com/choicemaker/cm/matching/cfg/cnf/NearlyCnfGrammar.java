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
package com.choicemaker.cm.matching.cfg.cnf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

//import com.choicemaker.cm.core.util.*;
import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.Rule;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.Variable;

/**
 * Subclass of PCFG that ensures that the grammar is NEARLY in Chomsky Normal Form.
 * Instances of this class are created by converting a normal PCFG into
 * CNF with the static convertToCnf() method.
 *
 * The class is called *Nearly* CnfGrammar for a good reason.  This form has some
 * key similarities to true Chomsky Normal Form, including:
 * <ol>
 * <li>Easily parsable using a slightly modified version of the CYK (Cocke-Younger-Kasami)
 * algorithm (See Speech and Language Processing by Jurafsky and Martin.),
 * which is a bottom-up dynamic programming algorithm.
 * <li>Has no epsilon rules (however, our base CFG implementation doesn't allow
 * empty rules either).
 * <li>Has no rules with RHS's with more than two symbols
 * <li>Has no rules with mixed variables and symbols (due to the base implementation).
 * <li>Disallows the start variable from appearing on the RHS of rules.
 * </ol>
 *
 * Key differences between these *Nearly* CNF grammars and true CNF grammars:
 * <ol>
 * <li>As opposed to CNF grammars, NCNF grammars allow some unit rules.  An NCNF
 * grammar may have rules of the form A --> TT, where TT is a token type, whereas
 * true CNF grammars have no rules of the form A --> TT.  In a sense, the TokenTypes
 * are terminals.
 * <li>
 * </ol>
 * @author Adam Winkel
 */
public class NearlyCnfGrammar extends ContextFreeGrammar {

	protected ContextFreeGrammar source;

	protected String tempVariablePrefix;
	protected int tempVariableIndex = 0;

	/**
	 * Create a Nearly CNF Grammar from the specified source grammar.
	 */
	public NearlyCnfGrammar(ContextFreeGrammar source) {
		this.source = source;
		createTempVariablePrefix();
		convertToNearlyCnfForm();
	}

	/**
	 * Returns the source grammar
	 */
	public ContextFreeGrammar getSourceGrammar() {
		return source;
	}

	/**
	 * Returns the start variable for the source CFG.  This variable
	 * is the RHS of the new start rule.
	 */
	public Variable getOldStartVariable() {
		return source.getStartVariable();
	}

	public boolean isInNearlyCnfForm() {
		if (getRules(getStartVariable()).size() > 1) {
			return false;
		} else if (nextNonTokenTypeUnitRule() != null) {
			return false;
		} else if (nextLongRule() != null) {
			return false;
		} else {
			return true;
		}
	}

	public void ensureNearlyCnfForm() {
		if (!isInNearlyCnfForm()) {
			convertToNearlyCnfForm();
		}
	}

	/**
	 * The procedure that converts a generic grammar to CNF
	 */
	protected void convertToNearlyCnfForm() {
		initialAddRules();
		createStartVariableAndRule();
		fixUnitRules();
		fixLongRules();
	}

	private void initialAddRules() {
		addAllRules( source.getRules() );
	}

	private void createStartVariableAndRule() {
		Variable old = getOldStartVariable();
		startVariable = generateTempVariable();

		Rule newStartRule = new Rule(startVariable, old, 1.0);
		addRule(newStartRule);
	}

	//
	// Fixing Unit rules
	//

	private void fixUnitRules() {
		HashSet unitRulesPreviouslyRemoved = new HashSet();

		Rule r;
		while ((r = nextNonTokenTypeUnitRule()) != null) {
			Variable lhs = r.getLhs();
			Variable rhs = (Variable) r.getRhsSymbol(0);

			// check if this was a useless rule
			if (lhs.equals(rhs)) {
				continue;
			}

			if (!(rhs instanceof TokenType)) {
				removeRule(r);
				unitRulesPreviouslyRemoved.add(r);

				List rhsList = getRules(rhs);
				for (int i = 0; i < rhsList.size(); i++) {
					Rule base = (Rule) rhsList.get(i);
					SquashedRule newRule = new SquashedRule(r, base);
					if (!unitRulesPreviouslyRemoved.contains(newRule)) {
						addRule(newRule);
					} else {
						System.out.println("\t\tAlready removed rule: " + newRule);
					}
				}
			}
		}
	}

	private Rule nextNonTokenTypeUnitRule() {
		List rules = getRules();
		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			if (r.getRhsSize() == 1) {
				if (!(r.getRhsSymbol(0) instanceof TokenType)) {
					return r;
				}
			}
		}

		return null;
	}

	//
	// Fixing Long Rules:  creates a series of cascaded rules to convert, e.g.
	//  A --> B C D E
	// into
	//  A --> B TEMP1
	//  TEMP1 --> C TEMP2
	//  TEMP2 --> D E
	//

	private void fixLongRules() {
		Rule r;
		while ((r = nextLongRule()) != null) {
			removeRule(r);

			int max = r.getRhsSize() - 2;

			Variable lhs = r.getLhs();
			Variable rhs1 = (Variable) r.getRhsSymbol(0);
			Variable rhs2 = generateTempVariable();
			addRule(createHeadCascadedRule(r, rhs2));
			lhs = rhs2;

			for (int i = 1; i < max; i++) {
				rhs1 = (Variable) r.getRhsSymbol(i);
				rhs2 = generateTempVariable();
				addRule(createCascadedRule(lhs, rhs1, rhs2));
				lhs = rhs2;
			}

			rhs1 = (Variable) r.getRhsSymbol(max);
			rhs2 = (Variable) r.getRhsSymbol(max+1);
			addRule(createCascadedRule(lhs, rhs1, rhs2));
		}
	}

	private Rule nextLongRule() {
		List rules = getRules();
		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			if (r.getRhsSize() > 2) {
				return r;
			}
		}

		return null;
	}

	//
	// Temp Variables.  These are introduced when creating a new start variable, and when
	//
	//

	private void createTempVariablePrefix() {
		List vars = source.getVariables();
		String[] names = new String[vars.size()];
		int maxLength = 0;
		for (int i = 0; i < names.length; i++) {
			names[i] = ((Variable)vars.get(i)).toString();
			if (names[i].length() > maxLength) {
				maxLength = names[i].length();
			}
		}

		// 2014-04-24 rphall: Commented out unused local variable.
//		PrefixTree pft = new PrefixTree(names);
		String tempPrefix = "TEMP" + '_';
		while (tempPrefix.length() < maxLength) {
			tempPrefix += '_';
		}

		tempVariablePrefix = tempPrefix;
	}

	private Variable generateTempVariable() {
		return new Variable(tempVariablePrefix + tempVariableIndex++);
	}

	//
	// Inner Rule classes
	//

	public static class SquashedRule extends Rule {
		private List rules;
		public SquashedRule(Rule top, Rule child) {
			super(top.getLhs(), child.getRhs(), top.getProbability() * child.getProbability());

			rules = new ArrayList();

			if (top instanceof SquashedRule) {
				rules.addAll(((SquashedRule)top).getRules());
			} else {
				rules.add(top);
			}

			if (child instanceof SquashedRule) {
				rules.addAll(((SquashedRule)child).getRules());
			} else {
				rules.add(child);
			}
		}
		public List getRules() {
			return rules;
		}
		public int getNumRules() {
			return rules.size();
		}
	}

	public static class HeadCascadedRule extends Rule {
		private Rule base;
		HeadCascadedRule(Variable lhs, List rhs, Rule base) {
			super(lhs, rhs, base.getProbability());
			this.base = base;
		}
		public Rule getBaseRule() {
			return base;
		}
	}

	private static Rule createHeadCascadedRule(Rule base, Variable rhs2) {
		List rhs = new ArrayList(2);
		rhs.add(base.getRhsSymbol(0));
		rhs.add(rhs2);

		return new HeadCascadedRule(base.getLhs(), rhs, base);
	}

	private static Rule createCascadedRule(Variable lhs, Variable rhs1, Variable rhs2) {
		List rhs = new ArrayList(2);
		rhs.add(rhs1);
		rhs.add(rhs2);

		return new Rule(lhs, rhs, 1.0f);
	}

}
