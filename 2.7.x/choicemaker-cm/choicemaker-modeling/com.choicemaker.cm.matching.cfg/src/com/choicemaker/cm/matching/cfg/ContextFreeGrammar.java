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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.matching.cfg.tokentype.InlineTokenType;

/**
 * Represents a context-free grammar.
 * 
 * CFG's are immutable, i.e. Rules and StartSymbol cannot be changed 
 * after initialization.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class ContextFreeGrammar {

	/** the start Variable for this CFG */
	protected Variable startVariable;
	
	/** the Variables used by this CFG */
	protected HashSet variables = new HashSet();
	
	/** 
	 * A hash of collections from LHS symbol to a collection of rules 
	 * that have that symbol as the LHS.
	 */
	protected Map lhsBuckets = new HashMap();
	
	/**
	 * Default constructor for subclasses like CnfGrammar.
	 */
	protected ContextFreeGrammar() { }
		
	/**
	 * Create a new ContextFreeGrammar with the specified start variable
	 * and rules.
	 * 
	 * @param startVariable the start variable
	 * @param rules the rules in this 
	 */
	public ContextFreeGrammar(Variable startVariable, Collection rules) {
		this.startVariable = startVariable;
		addAllRules(rules);
	}
		
	/** 
	 * Add all the rules in <code>rules</code> to this CFG's set of rules.
	 * 
	 * @throws IllegalArgumentException if <code>rules</code> contains a
	 * &quot;leaf rule&quot; with a TokenType on the LHS and a Token on the RHS.
	 */
	public void addAllRules(Collection rules) {
		Iterator elements = rules.iterator();
		while (elements.hasNext()) {
			addRule((Rule) elements.next());
		}
	}
	
	/**
	 * Add <code>rule</code> to this CFG's set of rules.
	 * 
	 * @throws IllegalArgumentException if <code>rule</code> is a 
	 * &quot;leaf rule&quot;
	 */
	public void addRule(Rule rule) {
		
		// check the validity of the RHS.
		int rhsSize = rule.getRhsSize();
		for (int i = 0; i < rhsSize; i++) {
			if (rule.getRhsSymbol(i) instanceof Token) {
				throw new IllegalArgumentException(
					"Cannot add rules with Tokens on RHS to a CFG's set of rules.");	
			}
		}
		
		Variable lhs = rule.getLhs();
		CfgRuleBucket bucket = getLhsBucket(lhs);
		if (bucket == null) {
			bucket = new CfgRuleBucket(lhs);
			lhsBuckets.put(lhs, bucket);	
		}
		bucket.addRule(rule);

		variables.add(rule.getLhs());
		variables.addAll(rule.getRhs());
	}
	
	/**
	 * Removes the specified rule from this CFG
	 */
	public boolean removeRule(Rule rule) {
		CfgRuleBucket bucket = getLhsBucket(rule.getLhs());
		if (bucket == null) {
			return false;	
		} else {
			return bucket.removeRule(rule);
		}
	}
		
	/**
	 * Returns the start variable.
	 */
	public Variable getStartVariable() {
		return startVariable;
	}
	
	protected void setStartVariable(Variable v) {
		this.startVariable = v;
	}
		
	public List getVariables() {
		return new ArrayList(variables);
	}

	public List getInlineTokenTypes() {
		List vList = getVariables();
		List inline = new ArrayList();
		for (int i = 0; i < vList.size(); i++) {
			Variable v = (Variable) vList.get(i);
			if (v instanceof InlineTokenType) {
				inline.add(v);
			}
		}		
		return inline;
	}
		
	public int getNumRules() {
		int num = 0;
		Iterator it = lhsBuckets.values().iterator();
		while (it.hasNext()) {
			CfgRuleBucket bucket = (CfgRuleBucket) it.next();
			num += bucket.getNumRules();
		}	
		return num;
	}

	public boolean hasRule(Rule r) {
		return hasRule(r.getLhs(), r.getRhs());	
	}

	public boolean hasRule(Variable lhs, List rhs) {
		CfgRuleBucket bucket = getLhsBucket(lhs);
		if (bucket == null) {
			return false;
		}
		return bucket.hasRule(lhs, rhs);
	}

	public Rule getRule(Variable lhs, List rhs) {
		if (!hasRule(lhs, rhs)) {
			StringBuffer buff = new StringBuffer();
			buff.append(lhs.toString());
			buff.append(" -->");
			for (int i = 0; i < rhs.size(); i++) {
				buff.append(" " + rhs.get(i).toString());	
			}			
			throw new IllegalArgumentException("Specified rule does not exist: " + buff.toString());	
		}
		return getLhsBucket(lhs).getRule(lhs, rhs);
	}
	
	public List getRules(Variable lhs) {
		CfgRuleBucket bucket = getLhsBucket(lhs);
		if (bucket == null) {
			return new ArrayList();	
		}
		return bucket.getRules();
	}
	
	public List getRules() {
		ArrayList list = new ArrayList();
		Iterator it = lhsBuckets.values().iterator();
		while (it.hasNext()) {
			CfgRuleBucket bucket = (CfgRuleBucket)it.next();
			list.addAll(bucket.getRules());	
		}
		return list;	
	}

	protected CfgRuleBucket getLhsBucket(Variable lhs) {
		return (CfgRuleBucket) lhsBuckets.get(lhs);
	}

	/**
	 * Each bucket must have a sum probability of at least .999999, but
	 * less than 1.000001.
	 * 
	 * This relaxation of the requirement that probabilities sum to 1
	 * compensates for rounding error.
	 * 
	 * @return true iff this PCFG's probabilities are consistent.
	 */
	public boolean areRuleProbabilitiesConsistent() {
		boolean consistent = true;
		
		Iterator itVars = getVariables().iterator();
		while (itVars.hasNext()) {
			Variable v = (Variable)itVars.next();
			
			Iterator itRules = getRules(v).iterator();
			if (!itRules.hasNext()) {
				continue;	
			}			
			
			double sum = 0;
			while (itRules.hasNext()) {
				Rule r = (Rule) itRules.next();
				sum += r.getProbability();	
			}
			
			consistent &= (sum > .999999 && sum < 1.000001);
		}
		
		return consistent;
	}

	/**
	 * Returns true if this grammar has any dangling non-token type variables.
	 * 
	 * That is, if the grammar has at least one rule of the form 
	 *   A --&gt; B 
	 * where A is any variable and B is a non-token-type variable, but no rules
	 * of the form
	 *   B --&gt; C
	 * where C is any variable.
	 * 
	 * In general, this is indicative of programmer error, e.g. an undeclared token type
	 * in a grammar.
	 */
	public boolean hasDanglingNonTokenTypes() {
		
		Set lhsVariables = new HashSet();
		Set rhsNonTokenTypes = new HashSet();
		List rules = getRules();
		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			
			lhsVariables.add(r.getLhs());
			
			List rhs = r.getRhs();
			for (int j = 0; j < rhs.size(); j++) {
				Variable v = (Variable) rhs.get(j);
				if (!(v instanceof TokenType)) {
					rhsNonTokenTypes.add(v);	
				}
			}
		}
		
		return !lhsVariables.containsAll(rhsNonTokenTypes);
	}

	/**
	 * Returns true if this grammar has at least one useless rule.
	 * 
	 * That is, if this grammar has at least one rule of the form<br>
	 * &nbsp;&nbsp;A --&gt; B<br>
	 * where A is not the start variable and B is any variable, but no rules of the form<br>
	 * &nbsp;C --&gt; A
	 * 
	 * <p>
	 * Note that &quot;islands&quot of rules which are unreachable from the start variable
	 * are also useless.  However, the algorithms in hasUselessRules() and removeUselessRules() 
	 * do not find or remove such circularities.
	 * 
	 * <p>
	 * This can happen either as a result of programmer error, or as a result of 
	 * converting a grammar to Nearly Chomsky Normal Form.
	 */
	public boolean hasUselessRules() {
		return getUselessRules().size() != 0;
	}

	/**
	 * Removes all the useless rules in this grammar.  Note that this is done
	 * recursively, as removal of one useless rule may make other rules useless.
	 */
	public void removeUselessRules() {
		while (true) {
			List useless = getUselessRules();
			if (useless.size() == 0) {
				return;	
			}
			
			for (int i = 0; i < useless.size(); i++) {
				Rule r = (Rule) useless.get(i);
				removeRule(r);
			}
		}
	}

	/**
	 * Returns the first useless rule encountered in this grammar, or null if none
	 * exists.
	 */
	protected List getUselessRules() {
		List rules = getRules();		
		Set rhsVars = new HashSet();

		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			rhsVars.addAll(r.getRhs());
		}

		Variable sv = getStartVariable();

		List useless = new ArrayList();
		for (int i = 0; i < rules.size(); i++) {
			Rule r = (Rule) rules.get(i);
			Variable lhs = r.getLhs();			
			if (!lhs.equals(sv) && !rhsVars.contains(lhs)) {
				useless.add(r);
			}
		}
		
		return useless;
	}

	/**
	 * Creates and returns a String representing this CFG.
	 * The returned value should be suitable for parsing by 
	 * readFromFile().  The rules for the start symbol are
	 * the first rules in the string, and other rules are 
	 * grouped together by LHS.
	 */
	public String toString() {
		String s = "";

		List inlineTokenTypes = getInlineTokenTypes();
		for (int i = 0; i < inlineTokenTypes.size(); i++) {
			InlineTokenType tt = (InlineTokenType) inlineTokenTypes.get(i);
			s += tt.getDefinitionString() + "\n";	
		}
		
		Iterator rules = getRules(startVariable).iterator();
		while (rules.hasNext()) {
			s += rules.next() + "\n";	
		}
		
		Iterator keys = lhsBuckets.keySet().iterator();
		while (keys.hasNext()) {
			Variable lhs = (Variable) keys.next();
			if (lhs.equals(startVariable)) {
				continue;	
			}
			rules = getRules(lhs).iterator();
			while (rules.hasNext()) {
				s += rules.next() + "\n";	
			}
		}
		
		return s;
	}

	//
	// Inner class
	//
	
	/**
	 * A CfgRuleBucket contains all the rules which have the specified variable as the LHS.
	 */
	private static class CfgRuleBucket {
		
		Variable lhs;
		HashMap rules = new HashMap();
		
		List ruleList = new ArrayList(4);
		List unmodifiableRuleList = Collections.unmodifiableList(ruleList);
		
		public CfgRuleBucket(Variable lhs) {
			this.lhs = lhs;
		}
		
		public void addRule(Rule r) {
			List rhs = r.getRhs();
			if (rules.containsKey(rhs)) {
				throw new IllegalArgumentException("Attempt to add duplicate rule to CFG: " + r);	
			}
			rules.put(rhs, r);

			ruleList.add(r);
		}
		
		public boolean hasRule(Variable lhs, List rhs) {
			if (!this.lhs.equals(lhs)) {
				throw new IllegalStateException("Specified LHS different than bucket LHS!");	
			}			
			return rules.containsKey(rhs);
		}
		
		public Rule getRule(Variable lhs, List rhs) {
			if (!hasRule(lhs, rhs)) {
				throw new IllegalArgumentException("Rule does not exist!");
			}
			return (Rule)rules.get(rhs);	
		}
		
		public int getNumRules() {
			return rules.size();	
		}
		
		public List getRules() {
			return unmodifiableRuleList;
		}
		
		public boolean removeRule(Rule r) {
			if (!this.lhs.equals(r.getLhs())) {
				throw new IllegalStateException("Rule has LHS different than bucket LHS!");	
			}
			
			List rhs = r.getRhs();
			Object fromHash = rules.remove(rhs);
			
			ruleList.remove(r);
			
			return fromHash != null;
		}
	}

}
