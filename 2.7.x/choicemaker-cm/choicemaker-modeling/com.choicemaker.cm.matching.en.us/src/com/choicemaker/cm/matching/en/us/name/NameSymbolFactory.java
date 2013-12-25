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
package com.choicemaker.cm.matching.en.us.name;

import com.choicemaker.cm.matching.cfg.SimpleSymbolFactory;
import com.choicemaker.cm.matching.cfg.tokentype.LetterTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.SetTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.StringTokenType;
import com.choicemaker.cm.matching.cfg.tokentype.WordTokenType;
import com.choicemaker.cm.matching.gen.Maps;

/**
 * The NameSymbolFactory just creates and adds relevant
 * Variable and Token types to itself.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:02 $
 */
public class NameSymbolFactory extends SimpleSymbolFactory {

	/**
	 * Create a new NameSymbolFactory.
	 */
	public NameSymbolFactory() {

		//
		// Variables
		//
		
		/*
		 * 
		this.addVariable(new Variable("NAME"));

		this.addVariable(new Variable("NAME1"));
		this.addVariable(new Variable("NAME2"));
		this.addVariable(new Variable("NAME3"));
		this.addVariable(new Variable("NAME5"));

		this.addVariable(new Variable("FAMN"));

		this.addVariable(new Variable("FN"));
		this.addVariable(new Variable("MN"));
		
		this.addVariable(new Variable("GN1"));
		this.addVariable(new Variable("GN2"));
		
		this.addVariable(new Variable("LN"));
		
		this.addVariable(new Variable("HYPHLN"));
		this.addVariable(new Variable("COLLN"));
		this.addVariable(new Variable("ONELN"));
		this.addVariable(new Variable("TWOLN"));
		
		this.addVariable(new Variable("PFX"));
		this.addVariable(new Variable("SFX"));
		
		// quoted word
		this.addVariable(new Variable("WDFNQUOT"));
		
		// parenthesized words
		this.addVariable(new Variable("WDPFXPAREN"));
		this.addVariable(new Variable("WDFNPAREN"));
		this.addVariable(new Variable("WDLNPAREN"));
		*/

		//		
		// TokenTypes
		//

		SetTokenType set;
		WordTokenType word;

		this.addVariable(new LetterTokenType("LTR"));
				
		word = new WordTokenType("WDFN");
		word.setMembers(Maps.getMap("firstNameCounts").keySet());
		word.setCounts(Maps.getMap("firstNameCounts"), 1000);
		word.setMinLength(2);
		this.addVariable(word);
		
		word = new WordTokenType("WDLN");
		word.setMembers(Maps.getMap("lastNameCounts").keySet());
		word.setCounts(Maps.getMap("lastNameCounts"), 1500);
		word.setMinLength(2);
		this.addVariable(word);
	
		// collapsable name fragment
		set = new SetTokenType("FRAG", "collapsableNameFragments");
		set.setDefaultProbability(.1);
		this.addVariable(set);

		// legal name prefixes
		set = new SetTokenType("WDPFX", "namePrefixes");
		set.setDefaultProbability(.1);
		this.addVariable(set);
	
		// legal name suffixes
		set = new SetTokenType("WDSFX", "nameSuffixes");
		set.setDefaultProbability(.1);
		this.addVariable(set);

		// Punctuation
		this.addVariable(new StringTokenType("HYPH", "-"));
		this.addVariable(new StringTokenType("COMMA", ","));		
		this.addVariable(new StringTokenType("QUOT", "\""));
		this.addVariable(new StringTokenType("LPAREN", "["));
		this.addVariable(new StringTokenType("RPAREN", "]"));

		// Field Separators
		this.addVariable(new StringTokenType("SEP1", "#"));
		this.addVariable(new StringTokenType("SEP2", "##"));
		this.addVariable(new StringTokenType("SEP3", "###"));
	}
	
}
