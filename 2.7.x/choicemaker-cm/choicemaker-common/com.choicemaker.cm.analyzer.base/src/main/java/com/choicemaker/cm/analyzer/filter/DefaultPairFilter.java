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

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.base.MutableMarkedRecordPair;

/**
 * A stripped down Filter implementation that only limits the
 * probability range of marked record pairs the (ascending) range
 * <code>[fromPercentage,toPercentage]</code>, inclusive. Both
 * the <code>fromPercentage</code> and the <code>toPercentage</code>
 * must be between <code>0f</code> and <code>1f</code>, inclusive.
 * This implementation warns if <code>fromPercentage</code> is greater than
 * <code>toPercentage</code>, but it does not enforce this restriction.
 * If <code>fromPercentage</code> is greater than <code>toPercentage</code>,
 * then no record pair will pass this filter.
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:36:26 $
 */
public class DefaultPairFilter implements Filter {

	private static final long serialVersionUID = 1L;

	/** Default "from" percentage (inclusive) */
	public static final float DEFAULT_FROM_PERCENTAGE = 0.20f;

	/** Default "to" percentage (inclusive) */
	public static final float DEFAULT_TO_PERCENTAGE = 0.80f;

	private static Logger logger =
		Logger.getLogger(DefaultPairFilter.class.getName());

	private float fromPercentage = DEFAULT_FROM_PERCENTAGE;
	private float toPercentage = DEFAULT_TO_PERCENTAGE;

	public DefaultPairFilter() {
	}

	public DefaultPairFilter(float from, float to) {
		this();
		setFromPercentage(from);
		setToPercentage(to);
	}

	/**
	 * Get the value of fromPercentage.
	 * @return value of fromPercentage.
	 */
	public float getFromPercentage() {
		checkSanity();
		return fromPercentage;
	}

	/**
	 * Set the value of fromPercentage.
	 * @param v  a value between 0f and 1f, inclusive
	 */
	public void setFromPercentage(float v) {
		if (v < 0f || v > 1f) {
			throw new IllegalArgumentException(
				"from-percentage less than zero or greater than 1: " + v);
		}
		this.fromPercentage = v;
		checkSanity();
	}

	/**
	 * Get the value of toPercentage.
	 * @return value of toPercentage.
	 */
	public float getToPercentage() {
		checkSanity();
		return toPercentage;
	}

	/**
	 * Set the value of toPercentage.
	 * @param v  a value between 0f and 1f, inclusive
	 */
	public void setToPercentage(float v) {
		if (v < 0f || v > 1f) {
			throw new IllegalArgumentException(
				"to-percentage less than zero or greater than 1: " + v);
		}
		this.toPercentage = v;
		checkSanity();
	}

	public boolean satisfy(MutableMarkedRecordPair mrp) {
		checkSanity();
		return fromPercentage <= mrp.getProbability()
			&& mrp.getProbability() <= toPercentage;
	}

	/**
	 * Does nothing
	 */
	public void resetLimiters() {
	}

	/** Checks whether fromPercentage <= toPercentage */
	private void checkSanity() {
		if (this.fromPercentage > this.toPercentage) {
			logger.warn(
				"fromPercentage ("
					+ fromPercentage
					+ ") is greater that toPercentage("
					+ toPercentage
					+ ")");
		}
	}

}

