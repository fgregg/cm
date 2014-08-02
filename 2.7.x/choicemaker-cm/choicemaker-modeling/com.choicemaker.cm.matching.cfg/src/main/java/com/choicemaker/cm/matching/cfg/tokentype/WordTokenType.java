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
package com.choicemaker.cm.matching.cfg.tokentype;


/**
 * Word tokens are non-null Strings with a length of at least one.
 * The first character must be a letter, however no explicit check
 * is done on the remainder of the String.  Depending on how one 
 * wants to define a word, this may need to overridden.
 * 
 * An instance of WordTokenType can have a minimum length greater 
 * than one.
 *
 * WordTokenType extends SetTokenType, and uses SetTokenType's 
 * machinery to compute probabilities from counts Maps, and returning
 * the standard token forms, <b>however</b> WordTokenType overrides
 * <code>canHaveToken(String)</code> and does not use a membership set.
 * Instead, words are defined as in the above paragraphs. 
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:00 $
 * @see SetTokenType
 */
public class WordTokenType extends SetTokenType {

	/** The minimum String length to take on this TokenType */
	protected int minLength = 1;

	protected int maxLength = -1;

	public WordTokenType(String name, int length) {
		super(name);
		setMinLength(length);
		setMaxLength(length);
		setDefaultProbability(0.01);
	}

	/**
	 * Creates a new WordTokenType with the specified name.
	 */
	public WordTokenType(String name) {
		super(name);
	}

	/**
	 * Creates a new WordTokenType with the specified name, counts name, 
	 * and default probability.
	 */
	public WordTokenType(String name, String countsName, double defaultProb) {
		this(name);
		setCounts(countsName);
		setDefaultProbability(defaultProb);
	}

	/**
	 * Creates a new WordTokenType with the specified name, counts name,
	 * default probability, and standards name.
	 */
	public WordTokenType(String name, String countsName, 
						 double defaultProb, String standardsName) {
		this(name, countsName, defaultProb);
		setStandards(standardsName);
	}
	
	/**
	 * Sets the minimun length for this WordTokenType.
	 */
	public void setMinLength(int minLength) {
		if (minLength < 1) {
			throw new IllegalArgumentException("Min length cannot be less than 1: " + minLength);	
		}
		
		this.minLength = minLength;
	}
	
	/**
	 * Returns the minimum length for this WordTokenType.
	 */
	public int getMinLength() {
		return minLength;	
	}
	
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	/**
	 * Override SetTokenType's implementation. Any String whose first character is
	 * a letter, and whose length is greater than the minimum for this WordTokenType,
	 * can take on this TokenType.
	 */
	public boolean canHaveToken(String token) {
		return token != null && 
			token.length() >= minLength && 
			(maxLength < 0 || token.length() <= maxLength) &&
			Character.isLetter(token.charAt(0));
	}
	
}
