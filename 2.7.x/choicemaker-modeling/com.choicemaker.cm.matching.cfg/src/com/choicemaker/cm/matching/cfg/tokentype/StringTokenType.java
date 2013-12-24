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
 * TokenType that accepts only one token: the value passed to the constructor.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:00 $
 */
public class StringTokenType extends TokenType {

	/** The only token that can take on this TokenType */
	private String value;
	
	/**
	 * Creates a new StringTokenType with the specified name and token String.
	 * 
	 * @param tokenName the name of this StringTokenType
	 * @param value the only token that can take on this TokenType
	 */
	public StringTokenType(String tokenName, String value) {
		super(tokenName);
		
		this.value = value;
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null.");	
		}
	}	
	
	/**
	 * Returns <code>value.equals(token)</code>.
	 */
	public boolean canHaveToken(String token) {
		return value.equals(token);
	}

	/**
	 * Return 1, as there is only one Token that can take on this
	 * TokenType.
	 */
	protected double getTokenProbability(String token) {
		return 1.0;
	}

}
