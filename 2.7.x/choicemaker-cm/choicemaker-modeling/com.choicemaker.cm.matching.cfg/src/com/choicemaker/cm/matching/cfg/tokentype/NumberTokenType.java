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

import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.matching.cfg.TokenType;

/**
 * Token type made up exclusively of Strings of digits.  The
 * name of the class may be slightly misleading, as decimal 
 * numbers are not included in this TokenType, nor are negative
 * numbers.
 *
 * There is no need to standardize numbers, as they are already 
 * in a standard form.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 * @see com.choicemaker.cm.matching.cfg.TokenType
 */
public class NumberTokenType extends TokenType {

	/**
	 * Creates a new NumberTokenType with the specified name.
	 */
	public NumberTokenType(String name) {
		super(name);
	}

	/**
	 * Returns true if <code>token</code> can take on this TokenType, 
	 * as per the general contract of <code>canHaveToken(String)</code>.
	 * 
	 * Number tokens are positive length Strings that contain only digits.
	 */
	public boolean canHaveToken(String token) {
		return token != null && token.length() > 0 && !StringUtils.containsNonDigits(token);
	}
	
	/**
	 * The sum of the probabilities for all n-digit numbers is
	 * 1 / 2^n, and we distribute that weight evenly over the bucket, so
	 * the probability of an n-digit number is 1 / (10 * 2^n), which when
	 * summed over all numbers, gives 1.
	 * 
	 * However, the lowest probability we give any digit string is 
	 * 1 / (10 * 2^5), regardless of how long it is.
	 */
	protected double getTokenProbability(String token) {
		int len = Math.min(token.length(), 5);
		return P[len];	
	}

	private static double[] P = {
		0,
		.1 * .5,
		.1 * .5 * .5,
		.1 * .5 * .5 * .5,
		.1 * .5 * .5 * .5 * .5,
		.1 * .5 * .5 * .5 * .5 * .5
	};

}
