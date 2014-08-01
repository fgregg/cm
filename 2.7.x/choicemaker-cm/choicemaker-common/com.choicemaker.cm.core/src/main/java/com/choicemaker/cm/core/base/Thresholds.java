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
 * Mutable match and differ thresholds.
 * @author Martin Buechi
 * @author rphall
 * @version $Revision: 1.2 $ $Date: 2010/03/27 21:35:15 $
 */
public class Thresholds extends ImmutableThresholds implements Cloneable {

	public Thresholds(float differThreshold, float matchThreshold) {
		super(MIN_VALUE,MAX_VALUE);
		this.setDifferThreshold(differThreshold);
		this.setMatchThreshold(matchThreshold);
	}
	
	public Thresholds(ImmutableThresholds imt) {
		super(imt);
	}

	public Object clone() {
		Object retVal = super.clone();
		return retVal;
	}

	/**
	 * Sets the value of differThreshold in a forgivng manner.
	 * <em>This method is different than the method for {@link IThresholds#setDifferThreshold(float)}:</em><ul>
	 * <li/> If the specified value is less than
	 * {@link IThresholds#MIN_VALUE MIN_VALUE}
	 * then the specified threshold is set to <code>MIN_VALUE</code>.
	 * <li/> If the specified value is greater than
	 * {@link IThresholds#MAX_VALUE MAX_VALUE}
	 * then the specified threshold is set to <code>MAX_VALUE</code>.
	 * <li/> If the specified value is greater than {@link IThresholds#getMatchThreshold() getMatchThreshold()}
	 * then the match threshold is {@link IThresholds#setMatchThreshold(float) reset}
	 * to the specified value.
	 * <li/> The differ threshold is {@link IThresholds#setDifferThreshold(float) set} to the specified value.
	 * </ul>
	 * @param v  Value to assign to differThreshold.
	 */
	public void setDifferThreshold(float v) {
		super.invariant();
		if (v <ImmutableThresholds.MIN_VALUE) {
			v =ImmutableThresholds.MIN_VALUE;
		} else if (v > ImmutableThresholds.MAX_VALUE) {
			v = ImmutableThresholds.MAX_VALUE;
		}
		if ( getMatchThreshold() < v ) {
			super.setMatchThreshold(v);
		}
		super.setDifferThreshold(v);
		super.invariant();
	}

	/**
	 * Sets the value of matchThreshold.
	 * <em>This method is different than the method for {@link IThresholds#setMatchThreshold(float)}:</em><ul>
	 * <li/> If the specified value is greater than
	 * {@link IThresholds#MAX_VALUE MAX_VALUE}
	 * then the specified threshold is set to <code>MAX_VALUE</code>.
	 * <li/> If the specified value is less than
	 * {@link IThresholds#MIN_VALUE MIN_VALUE}
	 * then the specified threshold is set to <code>MIN_VALUE</code>
	 * <li/> If the specified value is less than {@link IThresholds#getDifferThreshold() getDifferThreshold()}
	 * then the differ threshold is {@link IThresholds#setDifferThreshold(float) reset}
	 * to the specified value.
	 * <li/> The match threshold is {@link IThresholds#setMatchThreshold(float) set} to the specified value.
	 * </ul>
	 * @param v  Value to assign to matchThreshold.
	 */
	public void setMatchThreshold(float v) {
		super.invariant();
		if (v <ImmutableThresholds.MIN_VALUE) {
			v =ImmutableThresholds.MIN_VALUE;
		} else if (v > ImmutableThresholds.MAX_VALUE) {
			v = ImmutableThresholds.MAX_VALUE;
		}
		if ( getDifferThreshold() > v ) {
			this.setDifferThreshold(v);
		}
		super.setMatchThreshold(v);
		super.invariant();
	}

}

