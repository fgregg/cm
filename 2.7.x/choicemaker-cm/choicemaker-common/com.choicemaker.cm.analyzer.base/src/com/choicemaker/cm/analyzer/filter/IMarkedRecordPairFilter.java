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
import java.util.Collection;

import com.choicemaker.util.IntArrayList;

/**
 * @author rphall
 */
public interface IMarkedRecordPairFilter extends Serializable {
	public abstract void set(
		boolean[] humanDecision,
		boolean[] choiceMakerDecision,
		float fromPercentage,
		float toPercentage,
		FilterCondition[] conditions);
	/**
	 * Get the value of humanDecision.
	 * @return value of humanDecision.
	 */
	public abstract boolean[] getHumanDecision();
	/**
	 * Set the value of humanDecision.
	 * @param v  Value to assign to humanDecision.
	 */
	public abstract void setHumanDecision(boolean[] v);
	/**
	 * Get the value of choiceMakerDecision.
	 * @return value of choiceMakerDecision.
	 */
	public abstract boolean[] getChoiceMakerDecision();
	/**
	 * Set the value of choiceMakerDecision.
	 * @param v  Value to assign to choiceMakerDecision.
	 */
	public abstract void setChoiceMakerDecision(boolean[] v);
	/**
	 * Get the value of fromPercentage.
	 * @return value of fromPercentage.
	 */
	public abstract float getFromPercentage();
	/**
	 * Set the value of fromPercentage.
	 * @param v  Value to assign to fromPercentage.
	 */
	public abstract void setFromPercentage(float v);
	/**
	 * Get the value of toPercentage.
	 * @return value of toPercentage.
	 */
	public abstract float getToPercentage();
	/**
	 * Set the value of toPercentage.
	 * @param v  Value to assign to toPercentage.
	 */
	public abstract void setToPercentage(float v);
	/**
	 * Returns the conditions.
	 * @return FilterCondition[]
	 */
	public abstract FilterCondition[] getConditions();
	/**
	 * Sets the conditions.
	 * @param conditions The conditions to set
	 */
	public abstract void setConditions(FilterCondition[] conditions);
	public abstract void addCondition(FilterCondition c);
	/**
	 * Returns the limiters.
	 * @return Sampler[]
	 */
	public abstract Limiter[] getLimiters();
	/**
	 * 
	 * 0-1:5:100, 1:10:20:100
	 * 
	 * @param limiters
	 * @return String
	 */
	public abstract String getLimitersAsString();
	/**
	 * Sets the limiters.
	 * 
	 * 0-1:5:100, 1:10:20:100
	 * 
	 * @param limiters The limiters to set
	 */
	public abstract void setLimiters(String samplers);
	/**
	 * Sets the limiters.
	 * @param limiters The limiters to set
	 */
	public abstract void setLimiters(Limiter[] samplers);
	public abstract void reset();
	public abstract int[] filterSource(Collection src);
	/**
	 * @return
	 */
	public abstract IntArrayList getCollection();
	/**
	 * @param list
	 */
	public abstract void setCollection(IntArrayList list);
}
