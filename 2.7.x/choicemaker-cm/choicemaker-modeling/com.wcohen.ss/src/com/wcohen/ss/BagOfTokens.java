package com.wcohen.ss;

import java.util.*;
import com.wcohen.ss.api.*;

/**
 * A string, with an associated bag of tokens.  Each token has an
 * associated weight.
 * 
 */

public class BagOfTokens extends BasicStringWrapper
{
	private static final long serialVersionUID = 1L;
	private Map weightMap = new TreeMap();
	private double totalWeight = 0;
	private Token[] tokens;
	
	public BagOfTokens(String s,Token[] tokens) 
	{
		super(s);
		this.tokens = tokens;
		for (int i=0; i<tokens.length; i++) {
			weightMap.put(tokens[i], new Double(getWeight(tokens[i])+1) );
		}
		totalWeight = tokens.length;
	}

	
	public Set getDistinctTokens() {
		return weightMap.keySet();
	}
	
	/** Iterates over all tokens in the bag. */
	public Iterator tokenIterator() {
		return weightMap.keySet().iterator();
	}
	
	/** Test if this token appears at least once. */
    public boolean contains(Token tok) {
		return weightMap.get(tok)!=null;
	}
	
	/** Weight associated with a token: by default, the number of times
	 * the token appears in the bag. */
    public double getWeight(Token tok) {
		Double f = (Double) weightMap.get(tok);
		return f==null ? 0 : f.doubleValue();
	}
	
	/** Change the weight of a token in the bag */
    public void setWeight(Token tok, double d) {
		Double oldWeight = (Double) weightMap.get(tok);
		totalWeight += oldWeight==null ? d : (d - oldWeight.doubleValue());
		weightMap.put(tok,new Double(d));
	}
	
	/** Number of distinct tokens in the bag. */
	public int size() {
		return weightMap.keySet().size();
	}

	/** Total weight of all tokens in bag */
	public double getTotalWeight() {
		return totalWeight;
	}

	/** Return array of tokens */
	public Token[] getTokens() {
		return tokens;
	}
}
