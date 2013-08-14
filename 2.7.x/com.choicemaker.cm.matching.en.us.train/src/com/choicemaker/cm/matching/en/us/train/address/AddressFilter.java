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
package com.choicemaker.cm.matching.en.us.train.address;

import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.train.ParsedDataFilter;
import com.choicemaker.cm.matching.cfg.train.ParsedDataReader;
import com.choicemaker.cm.matching.cfg.xmlconf.ContextFreeGrammarXmlConf;
import com.choicemaker.cm.matching.en.us.address.AddressParserUtils;
import com.choicemaker.cm.matching.en.us.address.AddressSymbolFactory;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class AddressFilter {

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Must have three args!");
			System.exit(1);	
		}
		
		AddressParserUtils.initRelevantSetsAndMaps();
		SymbolFactory factory = new AddressSymbolFactory();
		ContextFreeGrammar grammar = ContextFreeGrammarXmlConf.readFromFile(args[1], factory);
		ParsedDataReader rdr = new ParsedDataReader(args[2], factory, grammar);

		args[0] = args[0].intern();
		if (args[0] == "-parsed") {
			ParsedDataFilter.filterRawData(rdr, ParsedDataFilter.PARSED);
		} else if (args[0] == "-unparsed") {
			ParsedDataFilter.filterRawData(rdr, ParsedDataFilter.UNPARSED);
		} else if (args[0] == "-all") {
			ParsedDataFilter.filterRawData(rdr, ParsedDataFilter.ALL);
		} else {
			System.err.println("Unknown first argument: " + args[0]);	
		}
	}

}
