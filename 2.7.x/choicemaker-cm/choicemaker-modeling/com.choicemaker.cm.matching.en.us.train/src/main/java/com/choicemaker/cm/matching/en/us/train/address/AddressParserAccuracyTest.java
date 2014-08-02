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
import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.train.ParsedDataReader;
import com.choicemaker.cm.matching.cfg.train.ParserAccuracyTest;
import com.choicemaker.cm.matching.cfg.xmlconf.ContextFreeGrammarXmlConf;
import com.choicemaker.cm.matching.en.us.address.AddressParserUtils;
import com.choicemaker.cm.matching.en.us.address.AddressSymbolFactory;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class AddressParserAccuracyTest extends ParserAccuracyTest {

	public AddressParserAccuracyTest(Parser p) {
		super(p);	
	}

	public static void main(String[] args) throws Exception {
		AddressParserUtils.initRelevantSetsAndMaps();		
		SymbolFactory factory = new AddressSymbolFactory();
		ContextFreeGrammar grammar = ContextFreeGrammarXmlConf.readFromFile(args[0], factory);

		Parser parser = AddressParserUtils.createDefaultAddressParser(args[0]);

		AddressParserAccuracyTest test = new AddressParserAccuracyTest(parser);

		for (int i = 1; i < args.length; i++) {
			System.out.println("// ******* Processing: " + args[i] + " *******");
			ParsedDataReader rdr = new ParsedDataReader(args[i], factory, grammar);
			test.processData(rdr);
		}
		
		test.printStats();
	}

}
