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
package com.choicemaker.cm.analyzer.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Oversampling in a probability range.
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:33 $
 */
public class Limiter implements Serializable {
	private Random random = new Random();

	float from;
	float to;
	float probability;
	int max;
	int count;

	public Limiter(float from, float to, float probability, int max) {
		this.from = from;
		this.to = to;
		this.probability = probability;
		this.max = max;
	}

	public boolean accept(float p) {
		if (from <= p && p <= to) {
			if (count < max  && (probability == 1f || random.nextFloat() < probability)) {
				++count;
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(from * 100f);
		buf.append("-");
		buf.append(to * 100f);
		buf.append(":");
		buf.append(probability * 100f);
		buf.append(":");
		buf.append(max);
		return buf.toString();
	}

	/**
	 * 
	 * 0-1:5:100, 1:10:20:100
	 * 
	 * @param limiters
	 * @return String
	 */
	public static String getLimitersAsString(Limiter[] limiters) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < limiters.length; i++) {
			if (i != 0) {
				buf.append(", ");
			}
			buf.append(limiters[i].toString());
		}
		return buf.toString();
	}

	public static Limiter[] limitersFromString(String s) {
		StringTokenizer st = new StringTokenizer(s, "-:, ");
		ArrayList l = new ArrayList();
		try {
			while (st.countTokens() >= 4) {
				float from = Float.parseFloat(st.nextToken()) / 100;
				float to = Float.parseFloat(st.nextToken()) / 100;
				float probability = Float.parseFloat(st.nextToken()) / 100;
				int max = Integer.parseInt(st.nextToken());
				l.add(new Limiter(from, to, probability, max));
			}
		} catch (NumberFormatException ex) {
			throwException(s);
		}
		
		if (l.size() == 0 && s.getBytes().length > 0){
			throwException(s);
		}		
		
		return (Limiter[]) l.toArray(new Limiter[l.size()]);
	}

	protected static void throwException(String badArgument) throws IllegalArgumentException {
		throw new IllegalArgumentException("the argument must be of the form: 0-1:5:100, 1:10:20:100.  [" + badArgument + "]");
	}

	/**
	 * Returns the from.
	 * @return float
	 */
	public float getFrom() {
		return from;
	}

	/**
	 * Returns the max.
	 * @return int
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Returns the probability.
	 * @return float
	 */
	public float getProbability() {
		return probability;
	}

	/**
	 * Returns the to.
	 * @return float
	 */
	public float getTo() {
		return to;
	}

	/**
	 * Sets the from.
	 * @param from The from to set
	 */
	public void setFrom(float from) {
		this.from = from;
	}

	/**
	 * Sets the max.
	 * @param max The max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * Sets the probability.
	 * @param probability The probability to set
	 */
	public void setProbability(float probability) {
		this.probability = probability;
	}

	/**
	 * Sets the to.
	 * @param to The to to set
	 */
	public void setTo(float to) {
		this.to = to;
	}

}
