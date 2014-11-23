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
package com.choicemaker.cm.core.base;

/**
 *	Threshold values that can not be changed.
 * @author rphall 
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:00:57 $
 */
public class ImmutableThresholds implements Cloneable {

	public static final float PRECISION = 0.000001f;
	
	public static final double MAX_VALUE = 1.00f;

	public static final double MIN_VALUE = 0.0f;

	/**
	 * Checks that both thresholds are valid, and that the low threshold
	 * is not greater than the high threshold. If any of these constraints
	 * are violated, an IllegalArgumentException is thrown.
	 * @param lowThreshold
	 * @param highThreshold
	 * @throws IllegalArgumentException if:<ul>
	 * <li/> {@link #differThreshold differThreshold} is greater than {@link #MAX_VALUE MAX_VALUE}
	 * <li/> {@link #differThreshold differThreshold} is less than {@link #MIN_VALUE MIN_VALUE}
	 * <li/> {@link #matchThreshold matchThreshold} is greater than {@link #MAX_VALUE MAX_VALUE}
	 * <li/> {@link #matchThreshold matchThreshold} is less than {@link #MIN_VALUE MIN_VALUE}
	 * <li/> {@link #differThreshold differThreshold} is greater than {@link #matchThreshold matchThreshold}
	 * </ul>
	 */
	public static void validate(double lowThreshold, double highThreshold) {
		if (lowThreshold > MAX_VALUE || lowThreshold < MIN_VALUE
				|| highThreshold > MAX_VALUE || highThreshold < MIN_VALUE
				|| lowThreshold > highThreshold) {
			String msg =
				"The low threshold '" + lowThreshold
						+ "' or the high threshold '" + highThreshold
						+ "' violates the Threshold constraints";
			throw new IllegalArgumentException(msg);
		}
	}

	private double differThreshold;

	private double matchThreshold;

	/**
	 * Enforces {@link #invariant()}
	 * @param dt the differ threshold
	 * @param mt the match threshold
	 */
	public ImmutableThresholds(float dt, float mt) {
		this((double)dt, (double)mt);
	}
	
	protected ImmutableThresholds(double dt, double mt) {
		this.differThreshold = dt;
		this.matchThreshold = mt;
		// Fail fast
		invariant();
	}

	/**
	 * Enforces {@link #invariant()}
	 * @param that
	 */
	public ImmutableThresholds(ImmutableThresholds that) {
		this.differThreshold = that.differThreshold;
		this.matchThreshold = that.matchThreshold;
		// Fail fast
		invariant();
	}

	public ImmutableThresholds(Thresholds t) {
		this(t.getDifferThreshold(), t.getMatchThreshold());
	}

	/**
	 * Checks {@link #invariant()}
	 */
	public Object clone() {
		Object retVal;
		try {
			retVal = super.clone();
		} catch (CloneNotSupportedException x) {
			// Unexpected
			throw new Error(x.getMessage(), x);
		}
		// Fail fast
		((ImmutableThresholds)retVal).invariant();
		return retVal;
	}

	public boolean equals(Object o) {
		boolean retVal = false;
		if (o instanceof ImmutableThresholds) {
			ImmutableThresholds t = (ImmutableThresholds) o;
			boolean a =
				Math.abs(differThreshold - t.differThreshold)
					< ImmutableThresholds.PRECISION;
			boolean b =
				Math.abs(matchThreshold - t.matchThreshold)
					< ImmutableThresholds.PRECISION;
			retVal = (a && b);
		}
		return retVal;
	}

	/**
	 * Get the value of differThreshold.
	 * @return value of differThreshold.
	 */
	public float getDifferThreshold() {
		return (float) differThreshold;
	}

	/**
	 * Get the value of matchThreshold.
	 * @return value of matchThreshold.
	 */
	public float getMatchThreshold() {
		return (float) matchThreshold;
	}

	public Thresholds getMutableCopy() {
		return new Thresholds(this.differThreshold, this.matchThreshold);
	}

	public int hashCode() {
		int h1 = new Float(this.differThreshold).hashCode();
		int h2 = new Float(this.matchThreshold).hashCode();
		int retVal = h1 + h2;
		return retVal;
	}

	/**
	 * @throws IllegalStateException if:<ul>
	 * <li/> {@link #differThreshold differThreshold} is greater than {@link #MAX_VALUE MAX_VALUE}
	 * <li/> {@link #differThreshold differThreshold} is less than {@link #MIN_VALUE MIN_VALUE}
	 * <li/> {@link #matchThreshold matchThreshold} is greater than {@link #MAX_VALUE MAX_VALUE}
	 * <li/> {@link #matchThreshold matchThreshold} is less than {@link #MIN_VALUE MIN_VALUE}
	 * <li/> {@link #differThreshold differThreshold} is greater than {@link #matchThreshold matchThreshold}
	 * </ul>
	 */
	public void invariant() {
		if (differThreshold > MAX_VALUE || differThreshold < MIN_VALUE
				|| matchThreshold > MAX_VALUE || matchThreshold < MIN_VALUE
				|| differThreshold > matchThreshold) {
			String msg =
				"differThreshold '" + differThreshold + "' or matchThreshold '"
						+ matchThreshold
						+ "' violates the Thresholds invariant";
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Enforces {@link #invariant()}
	 * @param v
	 */
	protected void setDifferThreshold(double v) {
		differThreshold = v;
		// Fail fast
		invariant();
	}

	/**
	 * Enforces {@link #invariant()}
	 * @param v  Value to assign to matchThreshold.
	 */
	protected void setMatchThreshold(double v) {
		matchThreshold = v;
		// Fail fast
		invariant();
	}

}

