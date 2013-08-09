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
package com.choicemaker.cm.matching.wfst;

import java.util.List;

/**
 * A parser that produces one or more plausible parses of an input String. The
 * alternative parses may be assigned a weight that indicates their relative
 * probability of being correct.
 * @author rphall
 */
public interface AmbiguousParser {
	
	/**
	 * A reserved key which, if present in a parse, holds an String value representing
	 * a float value (i.e. Float.valueOf(..) will correctly parse the String value) that
	 * indicates the relative probability of the parse being the best choice among
	 * a List of other weighted parses.
	 * @see #parse(String)
	 */
	String PARSE_WEIGHT = "PARSE_WEIGHT";

	/**
	 * Indicates whether this parser returns weighted parses.
	 * @return
	 * @see #parse(Sting)
	 * @see #PARSE_WEIGHT
	 */	
	boolean isWeighted();
	
	/**
	 * Transforms a string into a list of possible parses. Each parse is represented
	 * by a Map.  The keys of a map are nonterminal symbols in a
	 * grammar; the value of a key is the yield of (the subtree rooted in)
	 * that  nonterminal symbol
	 * @param s the (non-null) String to parse.
	 * @return a non-null (but possibly empty) list of non-null (but possibly empty)
	 * map instances.
	 */
	List parse(String s);

} // AmbiguousParser


