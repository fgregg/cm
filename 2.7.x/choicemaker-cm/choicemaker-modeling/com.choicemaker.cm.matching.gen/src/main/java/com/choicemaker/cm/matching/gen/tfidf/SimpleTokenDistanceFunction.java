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
package com.choicemaker.cm.matching.gen.tfidf;

/**
 * Comment
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:04 $
 */
public class SimpleTokenDistanceFunction implements TokenDistanceFunction {

	public SimpleTokenDistanceFunction(StringDistanceFunction sd) {
		
	}

	public float distance(String s1, String s2) {
		return 0;
	}

	public float distance(TokenBag b1, TokenBag b2) {
		return 0;
	}

}
