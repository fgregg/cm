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
package com.choicemaker.cm.matching.cfg.standardizer;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.Token;
import com.choicemaker.cm.matching.cfg.TokenType;
import com.choicemaker.cm.matching.cfg.tokentype.OrdinalTokenType;

/**
 * @author ajwinkel
 *
 */
class NumberStandardizer extends DefaultStandardizer {
	
	protected boolean toOrd;

	public NumberStandardizer(String fieldName) {
		this(fieldName, false);
	}

	public NumberStandardizer(String fieldName, boolean toOrd) {
		super(fieldName);
		this.toOrd = toOrd;
	}

	public void standardize(ParseTreeNode node, ParsedData addr) {
		String num = extractNumber(node);
		if (toOrd) {
			num = OrdinalTokenType.numberToOrdinal(num);
		}
		addr.put(fieldName, num);
	}

	public static String extractNumber(ParseTreeNode node) {
		ParseTreeNode leftChild = node.getChild(0);
		TokenType tt = (TokenType)leftChild.getRule().getLhs();
		Token tok = (Token)leftChild.getRule().getRhsSymbol(0);
		return tt.getStandardToken(tok);
	}

}
