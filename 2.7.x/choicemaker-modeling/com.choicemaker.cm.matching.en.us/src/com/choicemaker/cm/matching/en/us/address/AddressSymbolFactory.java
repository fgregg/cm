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
package com.choicemaker.cm.matching.en.us.address;

import com.choicemaker.cm.matching.cfg.SimpleSymbolFactory;
import com.choicemaker.cm.matching.cfg.tokentype.AnyTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.LetterTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.NumberTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.OrdinalTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.SetTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.WordTokenType;

/**
 * Subclass of SimpleSymbolFactory that creates and adds
 * relevant Variables and TokenTypes to itself automatically.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class AddressSymbolFactory extends SimpleSymbolFactory {
	
	public AddressSymbolFactory() {
		
		// token types
		addVariable(new AnyTokenType("ANY"));
		addVariable(new NumberTokenType("NUM"));
		addVariable(new WordTokenType("WD", "wordCounts", .001, "standardWords"));
		addVariable(new WordTokenType("WD2", 2));
		addVariable(new LetterTokenType("LTR"));
		addVariable(new SetTokenType("CARD", "cardinals", "cardinalCounts", .01, "standardCardinals"));
		addVariable(new OrdinalTokenType("ORD", "ordinals", "ordinalCounts", 0.0, "ordinalsToNumbers"));
		addVariable(new SetTokenType("ORDX", "ordinalExtensions", "ordinalExtensionCounts", 0.0, "standardOrdinalExtensions"));
		addVariable(new SetTokenType("DIR", "directions", "directionCounts", 1.0/100.0, "standardDirections"));
		addVariable(new SetTokenType("SS", "streetSuffixes", "streetSuffixCounts", 0.01, "standardStreetSuffixes"));
		addVariable(new SetTokenType("AT", "aptTypes", "aptTypeCounts", 0.01, "standardAptTypes"));

		addVariable(new SetTokenType("TT_CITY_1_1", "cityWords_1_1"));
		addVariable(new SetTokenType("TT_CITY_2_1", "cityWords_2_1"));
		addVariable(new SetTokenType("TT_CITY_2_2", "cityWords_2_2"));
		addVariable(new SetTokenType("TT_CITY_3_1", "cityWords_3_1"));
		addVariable(new SetTokenType("TT_CITY_3_2", "cityWords_3_2"));
		addVariable(new SetTokenType("TT_CITY_3_3", "cityWords_3_3"));

		// NOTE: the following non-terminals are used in the grammar:
		//   SADDR (start variable)
		//
		//   HN
		//
		//   FSN
		//   SN
		//
		//   FAPT
		//   APT
		//
		//   MULTIANY
		//

		// NOTE: the following inline token types should be defined in the grammar:
		//   HYPH := -
		//   PND  := #

	}
	
}
