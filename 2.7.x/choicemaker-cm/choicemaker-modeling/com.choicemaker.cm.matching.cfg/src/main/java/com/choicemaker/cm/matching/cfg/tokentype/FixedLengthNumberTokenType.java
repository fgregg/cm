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
 * @author Adam Winkel
 */
public class FixedLengthNumberTokenType extends NumberTokenType {

	protected int length;
	protected double probability;

	public FixedLengthNumberTokenType(String name) {
		this(name, 1);
	}

	public FixedLengthNumberTokenType(String name, int length) {
		super(name);
		setLength(length);
	}
	
	public void setLength(int len) {
		this.length = len;
		this.probability = 1.0 / Math.pow(10, length);
	}
	
	public boolean canHaveToken(String token) {
		return super.canHaveToken(token) && token.length() == length;
	}

	protected double getTokenProbability(String token) {
		return probability;
	}

}
