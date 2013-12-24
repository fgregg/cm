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

import com.choicemaker.cm.matching.cfg.TokenType;

/**
 * TokenToken type for which only accepts Strings of length 1, whose
 * first and only characters are letters.
 * 
 * This implementation does not override <code>getStandardToken(String)</code>,
 * as we assume letters passed to us are in upper case, which is a decent 
 * standard form.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class LetterTokenType extends TokenType {

	/**
	 * Constructs a new LetterTokenType with the specified display name.
	 */
	public LetterTokenType(String name) {
		super(name);	
	}

	/**
	 * Returns true iff this TokenType can have the specified token.
	 * As mentioned above, this happens only when token is a length 1 String for
	 * which <code>Character.isLetter(token.charAt(0))</code> is true.  This does
	 * not restrict us to uppercase or lowercase, or even exclusively Latin 
	 * characters.  The implications of this are unknown.
	 * 
	 * @see com.choicemaker.cm.matching.cfg.TokenType#canHaveToken(String)
	 */
	public boolean canHaveToken(String token) {
		return token != null && token.length() == 1 && Character.isLetter(token.charAt(0));
	}	

	/**
	 * Returns 1/26, as we assume that all tokens have been converted to uppercase
	 * and that all letters are equally likely.  These assumptions don't necessarily 
	 * hold, but it works well enough for our purposes (and at least the probabilities
	 * of the implied rules add up to 1, give that we only see uppercase, Latin
	 * characters).
	 */
	protected double getTokenProbability(String token) {
		return 1.0 / 26.0;
	}

}
