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

import java.io.IOException;
import java.text.ParseException;

import com.choicemaker.cm.matching.cfg.ContextFreeGrammar;
import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.Tokenizer;
import com.choicemaker.cm.matching.cfg.cyk.CykParser;
import com.choicemaker.cm.matching.cfg.earley.EarleyParser;
import com.choicemaker.cm.matching.cfg.xmlconf.ContextFreeGrammarXmlConf;
import com.choicemaker.cm.matching.en.us.AddressParser;
import com.choicemaker.cm.matching.en.us.ParsedAddress;
import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.cm.matching.gen.Sets;

/**
 * @author ajwinkel
 *
 */
public class AddressParserUtils {

	private static final String DEFAULT_DATA_DIR = "../com.choicemaker.cm.matching.en.us.ny.nyc/etc/data";

	/**
	 * Initialize the relevant Sets and Maps for an AddressSymbolFactory, relative
	 * to &quot;plugin/locale_en_us/etc/data&quot;
	 */
	public static void initRelevantSetsAndMaps() throws IOException {
		initRelevantSetsAndMaps(DEFAULT_DATA_DIR);	
	}
	
	/**
	 * Initialize the relevant Sets and Maps for an AddressSymbolFactory, 
	 * relative to the specified directory.
	 */
	public static void initRelevantSetsAndMaps(String rel) throws IOException {
		Maps.addMap("wordCounts", Maps.readFileMap(rel + "/wordCounts.txt", "String", "int"));
		Maps.addMap("standardWords", Maps.readFileMap(rel + "/standardWords.txt", "String", "String"));
		
		Sets.addCollection("cardinals", Sets.readFileSet(rel + "/cardinals.txt"));
		Maps.addMap("cardinalCounts", Maps.readFileMap(rel + "/cardinalCounts.txt", "String", "int"));
		Maps.addMap("standardCardinals", Maps.readFileMap(rel + "/standardCardinals.txt", "String", "String"));

		Sets.addCollection("ordinals", Sets.readFileSet(rel + "/ordinals.txt"));
		Maps.addMap("ordinalCounts", Maps.readFileMap(rel + "/ordinalCounts.txt", "String", "int"));
		Maps.addMap("ordinalsToNumbers", Maps.readFileMap(rel + "/ordinalsToNumbers.txt", "String", "String"));

		Sets.addCollection("ordinalExtensions", Sets.readFileSet(rel + "/ordinalExtensions.txt"));
		Maps.addMap("ordinalExtensionCounts", Maps.readFileMap(rel + "/ordinalExtensionCounts.txt", "String", "int"));
		Maps.addMap("standardOrdinalExtensions",  Maps.readFileMap(rel + "/standardOrdinalExtensions.txt", "String", "String"));

		Sets.addCollection("directions", Sets.readFileSet(rel + "/directions.txt"));
		Maps.addMap("directionCounts", Maps.readFileMap(rel + "/directionCounts.txt", "String", "int"));
		Maps.addMap("standardDirections", Maps.readFileMap(rel + "/standardDirections.txt", "String", "String"));
	
		Sets.addCollection("directionsToSplit", Sets.readFileSet(rel + "/directionsToSplit.txt"));
	
		Sets.addCollection("streetSuffixes", Sets.readFileSet(rel + "/streetSuffixes.txt"));
		Maps.addMap("streetSuffixCounts", Maps.readFileMap(rel + "/streetSuffixCounts.txt", "String", "int"));
		Maps.addMap("standardStreetSuffixes", Maps.readFileMap(rel + "/standardStreetSuffixes.txt", "String", "String"));
	
		Sets.addCollection("aptTypes", Sets.readFileSet(rel + "/aptTypes.txt"));
		Maps.addMap("aptTypeCounts", Maps.readFileMap(rel + "/aptTypeCounts.txt", "String", "int"));
		Maps.addMap("standardAptTypes", Maps.readFileMap(rel + "/standardAptTypes.txt", "String", "String"));

		Sets.addCollection("cityWords_1_1", Sets.readFileSet(rel + "/cityWords_1_1.txt"));
		Sets.addCollection("cityWords_2_1", Sets.readFileSet(rel + "/cityWords_2_1.txt"));
		Sets.addCollection("cityWords_2_2", Sets.readFileSet(rel + "/cityWords_2_2.txt"));
		Sets.addCollection("cityWords_3_1", Sets.readFileSet(rel + "/cityWords_3_1.txt"));
		Sets.addCollection("cityWords_3_2", Sets.readFileSet(rel + "/cityWords_3_2.txt"));
		Sets.addCollection("cityWords_3_3", Sets.readFileSet(rel + "/cityWords_3_3.txt"));

		relevantSetsInitialized = true;
	}
	
	/**
	 * Initialize the relevant Sets and Maps as resource streams relative to the 
	 * given class and &quot;plugin/locale_en_us/etc/data&quot;.
	 */
	public static void initRelevantSetsAndMaps(Class cls) throws IOException {
		initRelevantSetsAndMaps(cls, DEFAULT_DATA_DIR);	
	}

	/**
	 * Initialize the relevant Sets and Maps as resource streams relative to the 
	 * given class and directory.
	 */	
	public static void initRelevantSetsAndMaps(Class cls, String rel) throws IOException {		
		Maps.addMap("wordCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/wordCounts.txt"), "String", "int"));
		Maps.addMap("standardWords", Maps.readFileMap(cls.getResourceAsStream(rel + "/standardWords.txt"), "String", "String"));
		
		Sets.addCollection("cardinals", Sets.readFileSet(cls.getResourceAsStream(rel + "/cardinals.txt")));
		Maps.addMap("cardinalCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/cardinalCounts.txt"), "String", "int"));
		Maps.addMap("standardCardinals", Maps.readFileMap(cls.getResourceAsStream(rel + "/standardCardinals.txt"), "String", "String"));

		Sets.addCollection("ordinals", Sets.readFileSet(cls.getResourceAsStream(rel + "/ordinals.txt")));
		Maps.addMap("ordinalCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/ordinalCounts.txt"), "String", "int"));
		Maps.addMap("ordinalsToNumbers", Maps.readFileMap(cls.getResourceAsStream(rel + "/ordinalsToNumbers.txt"), "String", "String"));

		Sets.addCollection("ordinalExtensions", Sets.readFileSet(cls.getResourceAsStream(rel + "/ordinalExtensions.txt")));
		Maps.addMap("ordinalExtensionCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/ordinalExtensionCounts.txt"), "String", "int"));
		Maps.addMap("standardOrdinalExtensions",  Maps.readFileMap(cls.getResourceAsStream(rel + "/standardOrdinalExtensions.txt"), "String", "String"));

		Sets.addCollection("directions", Sets.readFileSet(cls.getResourceAsStream(rel + "/directions.txt")));
		Maps.addMap("directionCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/directionCounts.txt"), "String", "int"));
		Maps.addMap("standardDirections", Maps.readFileMap(cls.getResourceAsStream(rel + "/standardDirections.txt"), "String", "String"));

		Sets.addCollection("directionsToSplit", Sets.readFileSet(cls.getResourceAsStream(rel + "/directionsToSplit.txt")));
	
		Sets.addCollection("streetSuffixes", Sets.readFileSet(cls.getResourceAsStream(rel + "/streetSuffixes.txt")));
		Maps.addMap("streetSuffixCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/streetSuffixCounts.txt"), "String", "int"));
		Maps.addMap("standardStreetSuffixes", Maps.readFileMap(cls.getResourceAsStream(rel + "/standardStreetSuffixes.txt"), "String", "String"));
	
		Sets.addCollection("aptTypes", Sets.readFileSet(cls.getResourceAsStream(rel + "/aptTypes.txt")));
		Maps.addMap("aptTypeCounts", Maps.readFileMap(cls.getResourceAsStream(rel + "/aptTypeCounts.txt"), "String", "int"));
		Maps.addMap("standardAptTypes", Maps.readFileMap(cls.getResourceAsStream(rel + "/standardAptTypes.txt"), "String", "String"));

		Sets.addCollection("cityWords_1_1", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_1_1.txt")));
		Sets.addCollection("cityWords_2_1", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_2_1.txt")));
		Sets.addCollection("cityWords_2_2", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_2_2.txt")));
		Sets.addCollection("cityWords_3_1", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_3_1.txt")));
		Sets.addCollection("cityWords_3_2", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_3_2.txt")));
		Sets.addCollection("cityWords_3_3", Sets.readFileSet(cls.getResourceAsStream(rel + "/cityWords_3_3.txt")));
	
		relevantSetsInitialized = true;
	}
	
	private static void ensureRelevantSetsAndMaps() {
		if (!relevantSetsInitialized) {
			try {
				initRelevantSetsAndMaps();
			} catch (Exception ex) {
				ex.printStackTrace();	
			}
		}		
	}

	public static Tokenizer getBareBonesTokenizer() {
		if (bareBones == null) {
			bareBones = new AddressTokenizer();				
		}
		return bareBones;
	}

	public static Tokenizer getSplitDigitsOnlyTokenizer() {
		if (splitDigitsOnly == null) {
			ensureRelevantSetsAndMaps();	

			splitDigitsOnly = new AddressTokenizer();
			splitDigitsOnly.setSplitDigitStrings(4, 2);
		}

		return splitDigitsOnly;
	}
	
	public static Tokenizer getSplitWordsOnlyTokenizer() {
		if (splitWordsOnly == null) {
			ensureRelevantSetsAndMaps();
			
			splitWordsOnly = new AddressTokenizer();
			splitWordsOnly.setSplitPreDirections(Sets.getCollection("directionsToSplit"));
			splitWordsOnly.setSplitSuffixes(Sets.getCollection("streetSuffixes"));
			splitWordsOnly.setSplitPostDirections(Sets.getCollection("directionsToSplit"));
			splitWordsOnly.setSplitAptTypes(Sets.getCollection("aptTypes"));
		}
		
		return splitWordsOnly;
	}

	public static Tokenizer getFullTokenizer() {
		if (fullTokenizer == null) {
			ensureRelevantSetsAndMaps();
			
			fullTokenizer = new AddressTokenizer();
			fullTokenizer.setSplitPreDirections(Sets.getCollection("directionsToSplit"));
			fullTokenizer.setSplitPostDirections(Sets.getCollection("directionsToSplit"));
			fullTokenizer.setSplitSuffixes(Sets.getCollection("streetSuffixes"));
			fullTokenizer.setSplitAptTypes(Sets.getCollection("aptTypes"));
			fullTokenizer.setSplitDigitStrings(4, 2);			
		}
		
		return fullTokenizer;
	}

	public static Tokenizer[] getAllTokenizers() {
		return new Tokenizer[] {
			getBareBonesTokenizer(),
			getSplitDigitsOnlyTokenizer(),
			getSplitWordsOnlyTokenizer(),
			getFullTokenizer()	
		};	
	}

	public static AddressParser createDefaultAddressParser(String grammarFile) throws IOException, ParseException {
		SymbolFactory f = new AddressSymbolFactory();
		ContextFreeGrammar g = ContextFreeGrammarXmlConf.readFromFile(grammarFile, f);
		ParseTreeNodeStandardizer s = new AddressStandardizer(f);
		
		return new AddressParser(getAllTokenizers(), g, s);
	}

	public static CykParser createCykAddressParser(String grammarFile) throws IOException, ParseException {
		SymbolFactory f = new AddressSymbolFactory();
		ContextFreeGrammar g = ContextFreeGrammarXmlConf.readFromFile(grammarFile, f);
		ParseTreeNodeStandardizer s = new AddressStandardizer(f);
		
		return new CykParser(getAllTokenizers(), g, s, ParsedAddress.class);
	}
	
	public static EarleyParser createEarleyAddressParser(String grammarFile) throws IOException, ParseException {
		SymbolFactory f = new AddressSymbolFactory();
		ContextFreeGrammar g = ContextFreeGrammarXmlConf.readFromFile(grammarFile, f);
		ParseTreeNodeStandardizer s = new AddressStandardizer(f);
		
		return new EarleyParser(getAllTokenizers(), g, s, ParsedAddress.class);
	}

	private static boolean relevantSetsInitialized = false;

	private static AddressTokenizer bareBones;
	private static AddressTokenizer splitDigitsOnly;
	private static AddressTokenizer splitWordsOnly;
	private static AddressTokenizer fullTokenizer;

}
