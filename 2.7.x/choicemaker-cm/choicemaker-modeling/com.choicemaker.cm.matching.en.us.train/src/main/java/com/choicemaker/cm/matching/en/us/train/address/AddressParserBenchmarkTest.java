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

import java.io.IOException;
import java.text.ParseException;

import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.train.FlatFileRawDataReader;
import com.choicemaker.cm.matching.cfg.train.ParserBenchmarkTest;
import com.choicemaker.cm.matching.cfg.train.RawDataReader;
import com.choicemaker.cm.matching.en.us.address.AddressParserUtils;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class AddressParserBenchmarkTest extends ParserBenchmarkTest {

	public AddressParserBenchmarkTest(String[] args) throws IOException, ParseException {
		super();
		
		if (args.length != 3) {
			System.err.println("Must have exactly three args");
			System.exit(1);
		}
		
		String type = args[0];
		String grammarFile = args[1];
		String rawDataFile = args[2];
		
		AddressParserUtils.initRelevantSetsAndMaps();
		
		Parser parser = AddressParserUtils.createDefaultAddressParser(grammarFile);
		RawDataReader reader = new FlatFileRawDataReader(rawDataFile);
		
		setType(type);
		setParser(parser);
		setRawDataReader(reader);
	}

	public static void main(String[] args) throws Exception {
		AddressParserBenchmarkTest test = new AddressParserBenchmarkTest(args);

		test.runTest();
		test.printResults();
	}

}
