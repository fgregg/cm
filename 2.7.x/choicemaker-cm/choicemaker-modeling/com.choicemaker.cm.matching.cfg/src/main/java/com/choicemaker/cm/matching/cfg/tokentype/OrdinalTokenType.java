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

import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Sets;
import com.choicemaker.util.StringUtils;

/**
 * 
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class OrdinalTokenType extends SetTokenType {

	public OrdinalTokenType(String name, String setName, String countsName, 
							   double defaultProb, String standardsName) {
		super(name, setName, countsName, defaultProb, standardsName);
	}

	/**
	 * 
	 */
	protected String getStandardToken(String tok) {
		String num = ordinalToNumber(tok);
		return numberToOrdinal(num);
	}
	
	//
	// Utilities.
	//
	
	public static String ordinalToNumber(String ord) {
		if (!Sets.includes("ordinals", ord)) {
			throw new IllegalArgumentException("Cannot convert the ordinal '" + ord + "' to a number.");
		} else {
			Object obj = Maps.lookup("ordinalsToNumbers", ord);
			if (obj != null) {
				return obj.toString().trim();
			} else {
				return "";
			}
		}
	}
	
//	public String ordinalToNumber(String ord) {
//		if (!super.canHaveToken(ord)) {
//			throw new IllegalArgumentException("Cannot convert the ordinal '" + ord + "' to a number.");
//		}
//		
//		return super.getStandardToken(ord);
//	}
	
	public static String numberToOrdinal(String num) {
		if (StringUtils.containsNonDigits(num)) {
			throw new NumberFormatException(num);
		}
		
		int len = num.length();
		if (len == 0) {
			return num;	
		}
		
		char c0 = len > 0 ? num.charAt(len - 1) : '0';
		char c1 = len > 1 ? num.charAt(len - 2) : '0';
				
		if (len > 1 && c1 == '1') {
			return num + "TH";
		}
		
		switch(c0) {
			case '1':
				return num + "ST";
			case '2':
				return num + "ND";
			case '3':
				return num + "RD";
			default:
				return num + "TH";
		}
		
	}
	
}
