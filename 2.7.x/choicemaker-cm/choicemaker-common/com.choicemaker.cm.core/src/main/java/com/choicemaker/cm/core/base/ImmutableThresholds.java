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

	public static final float MAX_VALUE = 1.01f;

	public static final float MIN_VALUE = 0.0f;

	public static final float PRECISION = 0.000001f;

	private float differThreshold;

	private float matchThreshold;

	/**
	 * Enforces {@link #invariant()}
	 * @param dt the differ threshold
	 * @param mt the match threshold
	 */
	public ImmutableThresholds(float dt, float mt) {
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
		return differThreshold;
	}

	/**
	 * Get the value of matchThreshold.
	 * @return value of matchThreshold.
	 */
	public float getMatchThreshold() {
		return matchThreshold;
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
		String msg =
			"differThreshold '"
				+ differThreshold
				+ "' or matchThreshold '"
				+ matchThreshold
				+ "' violates the Thresholds invariant";
		if (this.getDifferThreshold() > MAX_VALUE) {
			throw new IllegalStateException(msg);
		} else if (this.differThreshold < MIN_VALUE) {
			throw new IllegalStateException(msg);
		} else if (this.matchThreshold > MAX_VALUE) {
			throw new IllegalStateException(msg);
		} else if (this.matchThreshold < MIN_VALUE) {
			throw new IllegalStateException(msg);
		} else if (this.differThreshold > this.matchThreshold) {
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Enforces {@link #invariant()}
	 * @param v
	 */
	protected void setDifferThreshold(float v) {
		differThreshold = v;
		// Fail fast
		invariant();
	}

	/**
	 * Enforces {@link #invariant()}
	 * @param v  Value to assign to matchThreshold.
	 */
	protected void setMatchThreshold(float v) {
		matchThreshold = v;
		// Fail fast
		invariant();
	}

}

