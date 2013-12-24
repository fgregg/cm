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
import java.util.Collections;
import java.util.List;

/**
 * Represents a rule in a context-free grammar.  
 * 
 * Rules are immutable.
 * 
 * This implementation requires that Tokens (terminals) appear BY THEMSELVES
 * on the RHS of a rule.  However, this limitation is easily accommodated by
 * the writer of the grammar.
 * 
 * Each rule of the form:
 *     LHS    --> rhsVar1 token1 token2 rhsVar2
 * 
 * is replaced by an equivalent set of rules:     
 * 	   LHS	    --> rhsVar1 tokenType1 tokenType2 rhsVar2     
 *     tokenType1   --> token1 
 *     tokenType2   --> token2
 * 
 * However, CFG's do not allow rules which contain Tokens.  These rules
 * are implicitly held by the TokenTypes, and are used in classes that
 * parse using CFG's.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class Rule {
	
	/** The left-hand side of this Rule */
	private Variable lhs;
	
	/** The right-hand side of this Rule */
	private List rhs; // = new ArrayList();
	private List unmodifiableRhs;
	
	/** The probability of this Rule */
	private double probability;
	
	/**
	 * Default constructor for subclasses.
	 */
	protected Rule() { }
	
	/**
	 * Creates a new Rule with the specified left-hand side, right-hand
	 * side, and probability.
	 * 
	 * @param lhs the left-hand side of this Rule
	 * @param rhs the right-hand side of this Rule
	 * @param probability this Rule's probability
	 * @throws IllegalArgumentException if the lhs is null, the rhs is null, or the
	 * probability is not in the range [0, 1]
	 */
	public Rule(Variable lhs, Symbol rhs, double probability) {
		
		if (lhs == null) {
			throw new IllegalArgumentException("lhs cannot be null.");
		} else if (rhs == null) {
			throw new IllegalArgumentException("rhs cannot be null.");
		}
		
		this.lhs = lhs;
		this.rhs = new ArrayList(1);
		this.rhs.add(rhs);
		this.unmodifiableRhs = Collections.unmodifiableList(this.rhs);
		
		setProbability(probability);
	}
	
	/**
	 * Creates a new Rule with the specified left-hand side and
	 * right-hand side.
	 * 
	 * @param lhs the left-hand side of this Rule
	 * @param rhs the right-hand side of this Rule
	 * @throws IllegalArgumentException if the lhs is null or the rhs is null
	 */	
	public Rule(Variable lhs, Symbol rhs) {
		this(lhs, rhs, 0);
	}
	
	/**
	 * Creates a new Rule with the specified left-hand side and 
	 * right-hand side.  This constructor retrieves the Rule probability from
	 * <code>tokenType</code>, and will throw an exception if the returned
	 * probability is out of range.
	 * 
	 * @param tokenType the left-hand side of this leaf/unit Rule
	 * @param token the right-hand side of this leaf/unit Rule
	 * @throws IllegalArgumentException if tokenType or token is null, or
	 * the probability retrieved from tokenType is out of range.
	 */
	public Rule(TokenType tokenType, Token token) {
		this(tokenType, token, tokenType.getTokenProbability(token));	
	}
	
	/**
	 * Creates a new Rule with the specified left-hand side, right-hand side,
	 * and probability.
	 * 
	 * This constructor enforces that the RHS complies with the restrictions
	 * on rules for this implementation of CFG's.  The RHS may not be empty
	 * and may not contain null elements or elements that are not Symbols.
	 * Furthermore, if the RHS's size is more than 1, the RHS must contain
	 * exclusively Variables (i.e. no Tokens).
	 * 
	 * @param lhs the left-hand side of this Rule
	 * @param rhs  the right-hand side of this Rule
	 * @param probability the probability for this Rule
	 * @throws IllegalArgumentException if lhs is null, the rhs is null, 
	 * the rhs is otherwise illegal (see above), or the probability is out of
	 * range.
	 */
	public Rule(Variable lhs, List rhs, double probability) {
		if (lhs == null) {
			throw new IllegalArgumentException("lhs cannot be null.");
		} else if (rhs == null) {
			throw new IllegalArgumentException("rhs cannot be null.");	
		}

		for (int i = 0; i < rhs.size(); i++) {
			if (rhs.get(i) == null) {
				throw new IllegalArgumentException(
					"RHS cannot contain null elements.");
			} else if (!(rhs.get(i) instanceof Symbol)) {
				throw new IllegalArgumentException(
					"Each element of the RHS must be an instance of Symbol");
			}
		}
	
		if (rhs.size() == 0) {
			throw new IllegalArgumentException(
				"Must have at least one symbol on the RHS of a rule");			
		} else if (rhs.size() > 1) {
			for (int i = 0; i < rhs.size(); i++) {
				Symbol s = (Symbol) rhs.get(i);	
				if (s instanceof Token) {
					throw new IllegalArgumentException(
						"Tokens must appear alone on the RHS of rules: " + s);	
				}
			}
		}

		this.lhs = lhs;
		this.rhs = new ArrayList(rhs);
		this.unmodifiableRhs = Collections.unmodifiableList(this.rhs);
		
		setProbability(probability);
	}

	/**
	 * Creates a new rule with the specified lhs and rhs.
	 * See the other constructors the restrictions on <code>rhs</code>.
	 * 
	 * @see #Rule(Variable, List, double)
	 */
	public Rule(Variable lhs, List rhs) {
		this(lhs, rhs, 0);	
	}

	/**
	 * Returns the left-hand side of this Rule.
	 */
	public Variable getLhs() {
		return lhs;
	}

	/**
	 * Returns the number of symbols on the right-hand side of this Rule.
	 */
	public int getRhsSize() {
		return rhs.size();
	}

	/**
	 * Returns the Symbol at the specified index on the right-hand side.
	 * Valid indices are 0 to <code>getRhsSize() - 1</code>.
	 */
	public Symbol getRhsSymbol(int index) {
		return (Symbol) rhs.get(index);	
	}
	
	/**
	 * Returns the right-hand side of this Rule as a list of Symbols.
	 */
	public List getRhs() {
		return unmodifiableRhs;
	}

	/**
	 * Returns the probability of this Rule.
	 */
	public double getProbability() {
		return probability;	
	}
	
	/**
	 * Sets the probability of this Rule.
	 */
	public void setProbability(double p) {
		if (p < 0 || p > 1) {
			throw new IllegalArgumentException(p + "");	
		}	
		this.probability = p;
	}
	
	/**
	 * Returns a String representation of this Rule, including the probability.
	 */
	public String toString() {
		StringBuffer sBuff = new StringBuffer();
		
		sBuff.append(lhs.toString());
		sBuff.append(" -->");
		
		for (int i = 0; i < getRhsSize(); i++) {
			sBuff.append(' ');
			sBuff.append(getRhsSymbol(i).toString());
		}
		
		sBuff.append(" [");
		sBuff.append(getProbability());
		sBuff.append(']');
				
		return sBuff.toString();
	}

	/**
	 * Compares the specified Object with this Rule.
	 * 
	 * To be considered equal, a Rule must have the same left-hand side and 
	 * right-hand side.  Rules with different probabilities, but are otherwise
	 * equal, are considered equal.
	 * 
	 * @param  obj  The Object in question
	 * @return true if, this Rule is equal to <code>obj</code>; false otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Rule)) {
			return false;
		}
		
		Rule rule = (Rule) obj;
		return lhs.equals(rule.lhs) && rhs.equals(rule.rhs);
	}
	
	/**
	 * Returns a hash code for this Rule.  Note that probability does not factor
	 * in to hash code.
	 */
	public int hashCode() {
		return lhs.hashCode() + rhs.hashCode();
	}


}
