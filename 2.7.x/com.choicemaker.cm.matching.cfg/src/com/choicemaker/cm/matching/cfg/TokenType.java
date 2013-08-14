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

/**
 * The TokenType class represents a TokenType variable in 
 * a context-free grammar.  TokenTypes may appear on the 
 * right-hand side of rules in a CFG, but may not appear on the
 * left-hand side of a CFG's rules.  Instead, a TokenType
 * is a compact, implicit representation of &quot;unit rules&quot;
 * of the form
 * 
 *   TokenTypeX --> TokenY
 * 
 * for each Token y that can take on this TokenType.  For example,
 * if STREET_SUFFIX and WORD are both TokenTypes, &quot;AVENUE&quot;
 * may be a Token that can take on either type, the first in 
 * 
 *  &quot;FIFTH AVENUE&quot;
 * 
 * and the second in 
 * 
 *  &quot;AVENUE OF THE AMERICAS&quot;
 * 
 * Thus, the two rules 
 * 
 *    STREET_SUFFIX --> &quot;AVENUE&quot;
 *    WORD --> &quot;AVENUE&quot;
 * 
 * are implicitly contained in the STREET_SUFFIX and WORD TokenTypes,
 * respectively.
 * 
 * Each TokenType contains information about the probabilities of
 * its implied rules.  As with any set of rules with the same
 * LHS variable, the probabilities of each TokenType's
 * implied rules should sum to one.  However, because some TokenTypes
 * are not closed sets (e.g. WORDs, for example), this can not be
 * accomplished exactly.  However, such TokenTypes can estimate 
 * probabilities based on observed counts of common Tokens (e.g.
 * many occurrences of "Main", and "Washington" WORD TokenTypes)
 * while making some allowance for rare and unseen Tokens.
 * See SetTokenType and WordTokenType for more how this can be
 * accomplished.
 * 
 * Each TokenType also has the responsibility of standardizing 
 * Tokens that can have take on that TokenType.  By default this
 * TokenType class does no standardization, and just returns the
 * String value of the Token passed to it.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 * @see Variable
 * @see Token
 * @see Rule
 * @see ContextFreeGrammar
 * @see com.choicemaker.cm.matching.cfg.tokentype.SetTokenType
 * @see com.choicemaker.cm.matching.cfg.tokentype.WordTokenType
 */
public abstract class TokenType extends Variable {

	/**
	 * Creates a new TokenType with the specified name.
	 * 
	 * @param name the name of this TokenType
	 */
	public TokenType(String name) {
		super(name);
	}
	
	/**
	 * Returns true iff the specified Token can take on this
	 * TokenType.
	 * 
	 * @param token the token in question
	 * @return true if <code>token</code> can take on this
	 * TokenType; false otherwise.
	 */
	public boolean canHaveToken(Token token) {
		return canHaveToken(token.toString());
	}
	
	/**
	 * Returns true iff the specified Token String can take
	 * on this TokenType
	 * 
	 * @param token the token in question
	 * @return true if <code>token</code> can take on this
	 * TokenType; false otherwise.
	 */
	public abstract boolean canHaveToken(String token);

	/**
	 * Returns the probability of the implied rule
	 * 
	 *    ThisTokenType --> token
	 * 
	 * This method checks that <code>canHaveToken(token)</code> returns
	 * true and then calls <code>getTokenProbability(token.toString())</code>.
	 * 
	 * Thus, subclass implementations of <code>getTokenProbability(String)</code>
	 * do not need to check that <code>canHaveToken(token)</code> is true.
	 * 
	 * @param token the token in question
	 * @return the probability of the implied rule <tt>ThisTokenType --> token</tt>
	 * @throws IllegalArgumentException if <code>token</code> cannot take on 
	 * this TokenType.
	 * @see #getTokenProbability(String)
	 */
	public double getTokenProbability(Token token) {
		String tokString = token.toString();
		
		if (!canHaveToken(tokString)) {
			throw new IllegalArgumentException(
				"TokenType " + toString() + " cannot have token \'" + tokString + "\'");
		}
		
		return getTokenProbability(tokString);
	}
	
	/**
	 * Returns the probability of the implied rule
	 * 
	 *    ThisTokenType --> token
	 * 
	 * <b>This method does not need to check that <code>canHaveToken(token)</code> is
	 * true</b> since this method has protected access rights. This should not be a
	 * problem unless superclasses override <code>getTokenProbability(Token)</code>.
	 * 
	 * @param token the token in question
	 * @return the probability of the implied rule <tt>ThisTokenType --> token</tt>
	 * @see #getTokenProbability(Token)
	 */
	protected abstract double getTokenProbability(String token);
	
	/**
	 * Returns the standard token String (of this TokenType) for <code>token</code>.
	 * 
	 * This method checks that <code>canHaveToken(token)</code> returns true
	 * and then calls <code>getStandardToken(token.toString())</code>.
	 * 
	 * Thus, subclass implementations of <code>getTokenProbability(String)</code>
	 * do not need to check that <code>canHaveToken(token)</code> is true.
	 * 
	 * @param token the token in question
	 * @return the standard token <code>String</code> for <code>token</code>
	 * @throws IllegalArgumentException if <code>token</code> cannot take on this 
	 * TokenType
	 * @see #getStandardToken(String)
	 */
	public String getStandardToken(Token token) {
		String tokString = token.toString();

		if (!canHaveToken(tokString)) {
			throw new IllegalArgumentException(
				"TokenType " + toString() + " cannot have token \'" + tokString + "\'");				
		}
		
		return getStandardToken(tokString);
	}

	/**
	 * Returns the standard token String (of this TokenType) for <code>token</code>.
	 * 
	 * <b>This method does not need to check that <code>canHaveToken(token)</code> is
	 * true</b> since this method has protected access rights. This should not be a
	 * problem unless superclasses override <code>getTokenProbability(Token)</code>.
	 * 
	 * @param token the token in question
	 * @return the standard token <code>String</code> for <code>token</code>
	 * @see #getStandardToken(Token)
	 */
	protected String getStandardToken(String token) {
		return token;
	}

}
