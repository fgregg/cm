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
package com.choicemaker.cm.matching.intl;

import java.util.HashMap;
import java.util.Map;

import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.Parsers;
import com.choicemaker.cm.matching.cfg.map.MapParserUtils;
import com.choicemaker.cm.matching.en.us.ParsedAddress;
import com.choicemaker.cm.matching.gen.Maps;

/**
 * The international last-line parsers must be augmented before 
 * they are used.  This is in order to simplify the creation of 
 * state/province/region variables, token types, and the standardization
 * of the resulting parse trees.
 */
public class IntlLastLineParsers {

	private static Map parserSpecs = new HashMap();

	private static final String US = "us.lastLineParser";
	private static final String US_MAP = "us.standardStates";
	private static final String US_VAR = "V_STATE";
	
	private static final String CA = "ca.lastLineParser";
	private static final String CA_MAP = "ca.standardProvinces";
	private static final String CA_VAR = "V_PROVINCE";

	private static final String UK = "uk.lastLineParser";
	private static final String UK_MAP = "uk.standardRegions";
	private static final String UK_VAR = "V_UK_REGION";

	private static final String AU = "au.lastLineParser";
	private static final String AU_MAP = "au.standardStates";
	private static final String AU_VAR = "V_STATE";

	private static final String MX = "mx.lastLineParser";
	private static final String MX_MAP = "mx.standardStates";
	private static final String MX_VAR = "V_MX_STATE";

	private static final String BR = "br.lastLineParser";
	private static final String BR_MAP = "br.standardStates";
	private static final String BR_VAR = "V_BR_STATE";
	
	private static final String EC = "ec.lastLineParser";
	private static final String EC_MAP = "ec.standardProvinces";
	private static final String EC_VAR = "V_EC_PROVINCE";
	
	static {
		registerLastLineParser(US, US_MAP, US_VAR);
		registerLastLineParser(CA, CA_MAP, CA_VAR);
		registerLastLineParser(UK, UK_MAP, UK_VAR);
		registerLastLineParser(AU, AU_MAP, AU_VAR);
		registerLastLineParser(MX, MX_MAP, MX_VAR);
		registerLastLineParser(BR, BR_MAP, BR_VAR);
		registerLastLineParser(EC, EC_MAP, EC_VAR);
	}

	public static Parser getLastLineParserUS() {
		return getLastLineParser(US);
	}
	public static Parser getLastLineParserCA() {
		return getLastLineParser(CA);
	}
	public static Parser getLastLineParserUK() {
		return getLastLineParser(UK);
	}
	public static Parser getLastLineParserAU() {
		return getLastLineParser(AU);
	}
	public static Parser getLastLineParserMX() {
		return getLastLineParser(MX);
	}
	public static Parser getLastLineParserBR() {
		return getLastLineParser(BR);
	}
	public static Parser getLastLineParserEC() {
		return getLastLineParser(EC);
	}
	
	private static synchronized Parser getLastLineParser(String key) {
		return ((IntlParserSpec)parserSpecs.get(key)).getParser();
	}
	
	private static synchronized void registerLastLineParser(String parserName, String standardRegionMapName, String regionVariableName) {
		IntlParserSpec spec = new IntlParserSpec(parserName, standardRegionMapName, regionVariableName);
		parserSpecs.put(parserName, spec);
	}

	private static class IntlParserSpec {
		final String parserName;
		final String standardRegionMapName;
		final String regionVariableName;
		
		Parser parser;
		
		public IntlParserSpec(String parser, String regionMap, String regionVariable) {
			this.parserName = parser;
			this.standardRegionMapName = regionMap;
			this.regionVariableName = regionVariable;
		}
		
		public Parser getParser() {
			if (parser == null) {
				Parser p = Parsers.get(parserName);
				Map regionMap = Maps.getMap(standardRegionMapName);
				MapParserUtils.augmentParser(ParsedAddress.STATE, regionMap, regionVariableName, p);
				parser = p;
			}
			return parser;
		}
	}

	private IntlLastLineParsers() { }

}
