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
package com.choicemaker.cm.matching.cfg.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.matching.cfg.ParseTreeNodeStandardizer;
import com.choicemaker.cm.matching.cfg.Parser;
import com.choicemaker.cm.matching.cfg.Rule;
import com.choicemaker.cm.matching.cfg.SymbolFactory;
import com.choicemaker.cm.matching.cfg.Variable;
import com.choicemaker.cm.matching.cfg.standardizer.RecursiveStandardizer;
import com.choicemaker.cm.matching.cfg.tokentype.SetTokenType;

/**
 * @author ajwinkel
 *
 */
public class MapParserUtils {

	private static final double DELTA = .00001;

	public static void augmentParser(String targetFieldName, Map standardsMap, String rootVariableName, Parser parser) {
		SymbolFactory factory = parser.getSymbolFactory();
		Variable rootVariable = factory.getVariable(rootVariableName);
		if (rootVariable == null) {
			throw new IllegalArgumentException("Symbol factory has no variable named " + rootVariableName);
		}

		ParseTreeNodeStandardizer standardizer = parser.getStandardizer();
		if (!(standardizer instanceof RecursiveStandardizer)) {
			throw new IllegalArgumentException("Standardizer is not an instanceof RecursiveStandardizer");
		}

		List additionalRules = createMapVariable(standardsMap, rootVariable, factory);
		parser.getGrammar().addAllRules(additionalRules);
		
		ParseTreeNodeStandardizer std = createNodeStandardizer(targetFieldName, standardsMap);
		((RecursiveStandardizer)standardizer).putStandardizer(rootVariable, std);
	}

	public static MapNodeStandardizer createNodeStandardizer(String fieldName, Map standardsMap) {
		return new MapNodeStandardizer(fieldName, standardsMap);
	}

	/**
	 * Creates a set of varibles and rules that will allow for the inclusion of the
	 * information in <code>standardsMap</code> to be used in a CFG.
	 * 
	 * This method will create one or more variables, which will be added to the given
	 * SymbolFactory.  It will also create a list of rules connecting the given Variable
	 * to the intermediate Variables.  These rules are returned from the method and 
	 * are suitable for adding to a CFG.  The rules will be such that if multiple word
	 * keys exist in the standardsMap, keys with more words will be favored.  That is,
	 * &quot;Podunk West Virginia&quot; should parse to city = Podunk, state = West Virginia,
	 * as opposed to &quot;West&quot; becoming part of the city.
	 * 
	 * rootVariable should already be in the given SymbolFactory.
	 * 
	 * Don't do anything stupid with standardsMap (in another Thread) after it's passed to 
	 * this method.
	 * 
	 * 
	 */
	public static List createMapVariable(Map standardsMap, Variable rootVariable, SymbolFactory factory) {
		StandardMatch[] matches = createStandardMatches(standardsMap);
		
		List rules = new ArrayList();
		
		int maxVariableWords = maxWordCount(matches);

		String vNameBase = rootVariable.toString() + "__";
		float baseProbability = 0.999f / maxVariableWords;
		for (int i = 1; i <= maxVariableWords; i++) {
			Variable v = createNWordVariable(matches, i, vNameBase, factory, rules);
			if (v != null) {
				Rule r = new Rule(rootVariable, v, baseProbability + i * DELTA);
				rules.add(r);
			}
		}
		
		return rules;
	}
	
	private static Variable createNWordVariable(StandardMatch[] matches, int numWords, String vNameBase, SymbolFactory factory, List rules) {
		// the ith set holds the words in the ith position [0, (numWords - 1)]
		Set[] ss = new Set[numWords];
		for (int i = 0; i < ss.length; i++) {
			ss[i] = new HashSet();
		}
		
		for (int i = 0, n = matches.length; i < n; i++) {
			StandardMatch mi = matches[i];
			if (mi.getNumWords() == numWords) {
				for (int j = 0; j < numWords; j++) {
					ss[j].add(mi.getWord(j));
				}
			}
		}
		
		// if there are no StandardMatch objects with numWords words.
		// This may happen if there are 'gaps' in the number of words represented in the map.
		if (ss[0].size() == 0) {
			return null;
		}

		List rhs = new ArrayList(numWords);		
		for (int i = 0; i < numWords; i++) {
			// e.g. TOPNAME__TOKEN_2_3 (the 2nd word of 3-word decompositions of TOPNAMEs)
			String viName = vNameBase + "_TT_" + (i+1) + '_' + numWords;
			SetTokenType tt = new SetTokenType(viName, ss[i]);
			tt.setDefaultProbability(1.0);
			factory.addVariable(tt);
			rhs.add(tt);
		}

		// create the head variable
		// e.g. TOPNAME__3 (3-word decompositions of TOPNAMEs)
		Variable v = new Variable(vNameBase + numWords);
		factory.addVariable(v);

		Rule r = new Rule(v, rhs, 1.0);
		rules.add(r);

		return v;
	}
	
	private static StandardMatch[] createStandardMatches(Map standardsMap) {
		StandardMatch[] matches = new StandardMatch[standardsMap.size()];
		int num = 0;
		for (Iterator it = standardsMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry e = (Map.Entry)it.next();
			matches[num++] = new StandardMatch((String)e.getKey(), (String)e.getValue());
		}
		return matches;
	}
	
	private static int maxWordCount(StandardMatch[] matches) {
		int max = 0;
		for (int i = 0, n = matches.length; i < n; i++) {
			int ssi = matches[i].getNumWords();
			if (ssi > max) {
				max = ssi;
			}
		}
		
		return max;
	}

	private MapParserUtils() { }

}
