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
package com.choicemaker.cm.matching.cfg.train;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.choicemaker.cm.matching.cfg.ParseTreeNode;
import com.choicemaker.cm.matching.cfg.ParsedData;
import com.choicemaker.cm.matching.cfg.Parser;

/**
 * .
 * 
 * @author Adam Winkel
 */
public class ParserAccuracyTest {

	public static int TOKENIZATION_MODE = -1;
	public static int PARSE_TREE_MODE = -2;
	public static int PARSED_DATA_MODE = -3;

	protected Parser parser;
	protected int mode;

	private int correctParse;
	private int incorrectParse;
	private int unparsable;
	private int noneGivenParsable;
	private int noneGivenUnparsable;

	protected String[] rawData;
	protected List[] tokenizations;
	protected ParseTreeNode[] parseTrees;
	protected ParsedData[] parsedData;

	public ParserAccuracyTest(Parser parser) {
		this(parser, PARSE_TREE_MODE);	
	}

	public ParserAccuracyTest(Parser parser, int mode) {
		this.parser = parser;
		this.mode = mode;
		
		correctParse = 0;
		incorrectParse = 0;
		unparsable = 0;
		noneGivenParsable = 0;
		noneGivenUnparsable = 0;
	}

	// TODO: tokenization and parsed data mode.
	public void processData(ParsedDataReader reader) throws IOException {
		while (reader.next()) {
			rawData = reader.getRawData();
			tokenizations = reader.getTokenizations();
			parseTrees = reader.getParseTrees();
			parsedData = reader.getAllParsedData();
			
			if (mode == PARSE_TREE_MODE) {
				processDatumParseTreeMode();
			} else if (mode == PARSED_DATA_MODE) {
				
			} else if (mode == TOKENIZATION_MODE) {
				
			} else {
				throw new IllegalStateException("Unknown mode!");	
			}
		}
	}

	private void processDatumParseTreeMode() {
		ParseTreeNode ptn = parser.getBestParseTree(rawData);
		if (parseTrees.length == 0) {
			if (ptn == null) {
				noneGivenUnparsable++;
				handleNoParseGivenUnparsable();
			} else {
				noneGivenParsable++;
				handleNoParseGivenParsable();
			}
		} else {
			if (ptn == null) {
				unparsable++;
				handleUnparsable();	
			} else if (ptn.equals(parseTrees[0])) {
				correctParse++;
				handleCorrectParse();	
			} else {
				incorrectParse++;
				handleIncorrectParse();	
			}
		}
	}

	public void printStats() {
		printStats(System.out);	
	}

	public void printStats(PrintStream ps) {
		int total = correctParse + incorrectParse + unparsable + noneGivenParsable + noneGivenUnparsable;		
		int parsesGiven = correctParse + incorrectParse + unparsable;
		int noParses = noneGivenParsable + noneGivenUnparsable;
		ps.println(total + " total data");
		ps.println("\t" + parsesGiven + " have parses");
		ps.println("Of those with parses given...");
		ps.println("\t" + correctParse + "(" + (correctParse * 100.0 / parsesGiven) + "%) parsed correctly");
		ps.println("\t" + incorrectParse + "(" + (incorrectParse * 100.0 / parsesGiven) + "%) parsed incorrectly");
		ps.println("\t" + unparsable + "(" + (unparsable * 100.0 / parsesGiven) + "%) unparsable");
		ps.println("Of those without parses given...");
		ps.println("\t" + noneGivenParsable + "(" + (noneGivenParsable * 100.0 / noParses) + "%) parsed");
		ps.println("\t" + noneGivenUnparsable + "(" + (noneGivenUnparsable * 100.0 / noParses) + "%) not parsed");		
	}

	public void handleCorrectParse() { }
	
	public void handleIncorrectParse() { 
		System.out.println("// Incorrect:");
		System.out.println(rawData[0]);
		//if (rawData[0].startsWith("348 A")) {
		//	System.out.println(rawData.length);
		//	System.out.println(rawData[1]);
		//}
		System.out.println( parser.getBestParseTree(rawData).prettyPrint() );
		System.out.println();
	}
	
	public void handleUnparsable() { 
		System.out.println("// Unparsable:");
		System.out.println(rawData[0]);
	}
	
	public void handleNoParseGivenParsable() { 
		//System.out.println(rawData[0]);
	}
	
	public void handleNoParseGivenUnparsable() { 
		//System.out.println(rawData[0]);
	}

}
