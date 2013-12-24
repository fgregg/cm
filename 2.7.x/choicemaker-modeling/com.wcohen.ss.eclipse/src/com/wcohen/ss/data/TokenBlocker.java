package com.wcohen.ss.data;

import com.wcohen.ss.api.*;
import com.wcohen.ss.tokens.*;
import java.util.*;

/**
 * Finds all pairs that share a not-too-common token.
 */

public class TokenBlocker extends Blocker 
{
	private static double defaultMaxFraction = 1.0;
	static {
		try {
			String s = System.getProperty("blockerMaxFraction");
			if (s!=null) defaultMaxFraction = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			;
		}
	}

	private static final Set STOPWORD_TOKEN_MARKER = new HashSet();

	private List pairList;
	protected Tokenizer tokenizer;
	private double maxFraction;
	private int numCorrectPairs;

	public TokenBlocker(Tokenizer tokenizer, double maxFraction) {
		this.tokenizer = tokenizer;
		this.maxFraction = maxFraction;
	}
	public TokenBlocker() {
		this(SimpleTokenizer.DEFAULT_TOKENIZER, defaultMaxFraction);
	}
	public double getMaxFraction() { return maxFraction; }
	public void setMaxFraction(double maxFraction) { this.maxFraction = maxFraction; }

	public void block(MatchData data) 
	{
		numCorrectPairs = countCorrectPairs(data);
		pairList = new ArrayList();
		if (!clusterMode && data.numSources()!=2) 
			throw new IllegalArgumentException("need exactly two sources out of clusterMode");
		if (clusterMode && data.numSources()!=1) 
			throw new IllegalArgumentException("need exactly one source in clusterMode");
		String smallSource = data.getSource(0);
		String bigSource = clusterMode ? data.getSource(0) : data.getSource(1);
		if (data.numInstances(smallSource)>data.numInstances(bigSource)) {
			String tmp = smallSource;
			smallSource = bigSource;
			bigSource = tmp;
		}
		// index the smaller source
		double maxSetSize = data.numInstances(smallSource)*maxFraction;
		Map index = new TreeMap();
		for (int i=0; i<data.numInstances(smallSource); i++) {
			Token[] tokens = tokenizer.tokenize( data.getInstance(smallSource,i).unwrap() );
			for (int j=0; j<tokens.length; j++) {
				Set containers = (Set) index.get(tokens[j]);
				if (containers==STOPWORD_TOKEN_MARKER) {
					/* do nothing */;
				} else if (containers==null) {
					containers = new TreeSet();
					index.put(tokens[j], containers);
				} 
				containers.add( new Integer(i) );
				// mark this if it's too full
				if (containers.size() > maxSetSize) {  
					index.put(tokens[j], STOPWORD_TOKEN_MARKER);						
				} 
			}
		}
		//System.out.println("data:\n"+data); showIndex(index);
		// find pairs
		Set pairedUpInstances = new TreeSet();
		for (int i=0; i<data.numInstances(bigSource); i++) {
			MatchData.Instance bigInst = data.getInstance(bigSource,i);
			pairedUpInstances.clear();
			Token[] tokens = tokenizer.tokenize( bigInst.unwrap() );			
			for (int j=0; j<tokens.length; j++) {			
				Set containers = (Set) index.get( tokens[j] );
				if (containers!=null && containers!=STOPWORD_TOKEN_MARKER) {
					for (Iterator _i=containers.iterator(); _i.hasNext(); ) {
						Integer smallIndexInteger = (Integer) _i.next();
						int smallIndex = smallIndexInteger.intValue();
						if (!pairedUpInstances.contains(smallIndexInteger) && 
                (smallSource!=bigSource || smallIndex>i))
						{
							MatchData.Instance smallInst = data.getInstance(smallSource, smallIndex);
							pairList.add( new Blocker.Pair( bigInst, smallInst, smallInst.sameId(bigInst) ));
							pairedUpInstances.add( smallIndexInteger );
						}
					}
				}
			}
		}
	}
	public int size() { return pairList.size();  }
	public Pair getPair(int i) { return (Pair)pairList.get(i); }
	public int numCorrectPairs() { return numCorrectPairs; }
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("[TokenBlocker:clusterMode=").append(clusterMode);
		buf.append(",maxFraction=").append(maxFraction);
		buf.append("]");
		
		return buf.toString();
	}
}
