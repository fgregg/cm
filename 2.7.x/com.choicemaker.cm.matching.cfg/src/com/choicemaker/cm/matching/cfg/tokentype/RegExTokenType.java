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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.choicemaker.cm.matching.cfg.TokenType;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:00 $
 */
public class RegExTokenType extends TokenType {

	protected Pattern pattern;
	protected double defaultProbability;

	public RegExTokenType(String name) {
		super(name);
	}
	
	public RegExTokenType(String name, String regex) {
		this(name, Pattern.compile(regex));
	}

	public RegExTokenType(String name, String regex, int flags) {
		this(name, Pattern.compile(regex, flags));
	}

	public RegExTokenType(String name, Pattern regex) {
		super(name);
		setPattern(regex);
	}

	public void setPattern(String pattern) {
		setPattern(Pattern.compile(pattern));
	}
	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public void setDefaultProbability(double prob) {
		this.defaultProbability = prob;
	}
	
	public boolean canHaveToken(String token) {
		Matcher m = pattern.matcher(token);
		return m.matches();
	}
	
	public double getTokenProbability(String token) {
		return defaultProbability;
	}

}
