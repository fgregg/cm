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
package com.choicemaker.cm.matching.cfg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.choicemaker.util.StringUtils;



/**
 * The ParsedDataHolder class is a base for classes like
 * ParsedAddress and ParsedName that serve as hashes of 
 * field names (e.g. STREET_NAME, STREET_SUFFIX, FIRST_NAME, etc.)
 * to the corresponding values 
 * (e.g. "BROADWAY", "AVE", "CHRISTOPHER", etc.).
 * 
 * In general, one or more instances of ParsedDataHolder are the 
 * end result or the name- or address-parsing process.
 * 
 * Classes extending this class may want to implement convenience
 * methods for retrieving or combining fields, but doing so is 
 * unnecessary.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 22:09:21 $
 */
public class ParsedData implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 1436115416922235337L;

	/**
	 * Map from field names to field values.
	 */
	protected Map data = new HashMap();
	
	/** 
	 * The probability of the the parse tree that was normalized
	 * to this ParsedDataHolder
	 */
	protected double probability;

	/**
	 * Creates a new ParsedDataHolder.
	 */
	public ParsedData() { }
	
	/**
	 * Returns true if <code>key</code> maps to a 
	 * non-null value String whose length is positive.
	 * Otherwise, returns false.
	 */
	public boolean has(String key) {
		return StringUtils.nonEmptyString(get(key));	
	}
	
	/**
	 * Returns the value for the specified key.
	 * 
	 * @param key the key in question
	 * @return the value at the specified key.
	 */
	public String get(String key) {
		return (String)data.get(key);	
	}

	/**
	 * Sets the value for the specified key.
	 */
	public void put(String key, String value) {
		data.put(key, value);
	}

	/**
	 * May overwrite existing values...
	 */
	public void putAll(ParsedData pd) {
		data.putAll(pd.data);
	}

	/**
	 * Appends <code>value</code> to the specified key's
	 * current value if it is non-null and has positive length.
	 */
	public void append(String key, String value) {
		if (has(key)) {
			put(key, get(key) + " " + value);	
		} else {
			put(key, value);
		}
	}

	/**
	 * Sets the probability assigned to this ParsedDataHolder.
	 * 
	 * @param p the probability of this ParsedDataHolder
	 * @see #getProbability()
	 * @see ParseTreeNode#getProbability()
	 */
	public void setProbability(double p) {
		probability = p;
	}
	
	/**
	 * Returns the probability assigned to this ParsedDataHolder.
	 * Roughly speaking, the returned value is the probability of
	 * the PCFG used to parse this data (address, name, etc.) 
	 * generates the parse tree that normalized to this 
	 * ParsedDataHolder, modulo a few caveats.  The sum of
	 * the probabilities of all parse trees produced by the 
	 * grammar should equal 1.
	 * 
	 * <p>
	 * The above assumes a couple things:
	 * <ul>
	 * <li>
	 * For each variable that is on the left-hand side of at
	 * least one rule in the grammar, the sum of those such rules
	 * equals 1.  Although it is explicitly assumed that this 
	 * assumption will be violated because of open-class TokenTypes
	 * such as words that appear in street names, we assume
	 * that this assumption will be <it>approximately</it> true.
	 * <li>
	 * This probability is the probability of the <it>highest-scoring</it>
	 * parse tree for the given tokenization (or tokenizations) of the
	 * String(s).  If two or more parse trees normalize to this 
	 * ParseDataHolder, we ignore all but the one with the highest
	 * probability.
	 * </ul>
	 * 
	 * @return the probability of this ParsedDataHolder
	 * @see Rule
	 * @see ContextFreeGrammar
	 * @see ParseTreeNode
	 */
	public double getProbability() {
		return probability;
	}
	
	public int size() {
		return data.size();	
	}
	
	public Set keySet() {
		return data.keySet();	
	}
	
	/**
	 * Returns true if <code>obj</code> is a ParsedDataHolder
	 * with the same key set as this ParsedDataHolder, and
	 * for each key, the values are either
	 * <ol>
	 * <li>both null or zero-length, or
	 * <li>v1.equals(v2) == true
	 * </ol>
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ParsedData))
			return false;
			
		ParsedData holder = (ParsedData) obj;
		
		if (data.size() != holder.data.size())
			return false;
			
		Iterator itKeys = data.keySet().iterator();
		while (itKeys.hasNext()) {
			String key = (String) itKeys.next();
			String v1 = get(key);	
			String v2 = holder.get(key);
			
			boolean vEqual = StringUtils.nonEmptyString(v1) ? 
							 v1.equals(v2) :
							 StringUtils.nonEmptyString(v2);
			if (!vEqual) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a hash code for this ParsedDataHolder.
	 */
	public int hashCode() {
		return data.hashCode();
	}
	
}
