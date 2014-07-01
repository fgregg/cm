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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.matching.gen.Maps;
import com.choicemaker.util.StringUtils;

/**
 * @author ajwinkel
 *
 */
public class MapStandardizer {

	private Set standardsSet;
	private Map standardsMap;
	private Map firstCharMap;

	public MapStandardizer(String mapName) {
		Map map = Maps.getMap(mapName);
		if (map == null) {
			throw new IllegalArgumentException("Unknown map: " + mapName);
		}
		init(map);
	}
	
	public MapStandardizer(Map map) {
		init(map);
	}

	private void init(Map input) {		
		// defensively copy the input Map
		input = new HashMap(input);
		
		Map fcMap = new HashMap();
		
		// group all the values in the input map by their first character
		// into a Set (so that we don't worry about sizes).
		for (Iterator it = input.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry e = (Map.Entry)it.next();
			String match = (String)e.getKey();
			String std = (String)e.getValue();
			Character c = CharacterCache.getCharacter(match.charAt(0));
			Set forC = (Set) fcMap.get(c);
			if (forC == null) {
				forC = new HashSet();
				fcMap.put(c, forC);
			}
			forC.add(new StandardMatch(match, std));
		}
		
		// collapse each Set into a String[] for processing speed.
		for (Iterator it = fcMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			Set forKey = (Set) entry.getValue();
			StandardMatch[] s = (StandardMatch[]) forKey.toArray(new StandardMatch[forKey.size()]);
			entry.setValue(s);
		}

		this.firstCharMap = fcMap;
		this.standardsMap = input;
		this.standardsSet = new HashSet(input.values());
	}


	public String getStandard(String s) {
		// check if the input is a standard
		if (standardsSet.contains(s)) {
			return s;
		}
		
		// check if the input is mapped directly to a standard
		if (standardsMap.containsKey(s)) {
			return (String)standardsMap.get(s);
		}
		
		// check the input for similarity to all values that start with the
		// same character.
		Character c = CharacterCache.getCharacter(s.charAt(0));
		StandardMatch[] forC = (StandardMatch[]) firstCharMap.get(c);
		if (forC == null) {
			//System.err.println("\nNo values start with the same letter as: " + s);
			return null;
		}

		// split the String
		String[] sPieces = StringUtils.split(s);

		StandardMatch bestMatch = null;
		for (int i = 0, n = forC.length; i < n; i++) {
			StandardMatch newMatch = forC[i];
			if (newMatch.isMatch(s, sPieces)) {
				if (bestMatch == null) {
					bestMatch = newMatch;
				} else {
					bestMatch = StandardMatch.resolveMatch(s, sPieces, bestMatch, newMatch);
				}
			}
		}
		
		return bestMatch != null ? bestMatch.getStandard() : null;
	}

}
