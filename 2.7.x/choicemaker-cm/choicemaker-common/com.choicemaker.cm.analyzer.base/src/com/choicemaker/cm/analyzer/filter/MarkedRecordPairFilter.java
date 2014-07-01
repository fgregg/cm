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

import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IMarkedRecordPair;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.util.ArrayHelper;
import com.choicemaker.util.IntArrayList;

public class MarkedRecordPairFilter implements Filter, IMarkedRecordPairFilter {
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(MarkedRecordPairFilter.class);
	//private static IMarkedRecordPairFilter instance = null;
	private static final FilterCondition[] ZERO_CONDITION = new FilterCondition[0];
	private static final Limiter[] ZERO_SAMPLER = new Limiter[0];

	protected boolean[] humanDecision;
	protected boolean[] choiceMakerDecision;
	protected float fromPercentage;
	protected float toPercentage;
	protected FilterCondition[] conditions;
	protected Limiter[] limiters;
//	protected Random random = new Random();
	protected IntArrayList collection;

	public MarkedRecordPairFilter() {
		reset();
	}

	public void set(
		boolean[] humanDecision,
		boolean[] choiceMakerDecision,
		float fromPercentage,
		float toPercentage,
		FilterCondition[] conditions) {
		this.humanDecision = humanDecision;
		this.choiceMakerDecision = choiceMakerDecision;
		this.fromPercentage = fromPercentage;
		this.toPercentage = toPercentage;
		this.conditions = conditions;
	}

	/**
	 * Get the value of humanDecision.
	 * @return value of humanDecision.
	 */
	public boolean[] getHumanDecision() {
		return humanDecision;
	}

	/**
	 * Set the value of humanDecision.
	 * @param v  Value to assign to humanDecision.
	 */
	public void setHumanDecision(boolean[] v) {
		this.humanDecision = v;
	}

	/**
	 * Get the value of choiceMakerDecision.
	 * @return value of choiceMakerDecision.
	 */
	public boolean[] getChoiceMakerDecision() {
		return choiceMakerDecision;
	}

	/**
	 * Set the value of choiceMakerDecision.
	 * @param v  Value to assign to choiceMakerDecision.
	 */
	public void setChoiceMakerDecision(boolean[] v) {
		this.choiceMakerDecision = v;
	}

	/**
	 * Get the value of fromPercentage.
	 * @return value of fromPercentage.
	 */
	public float getFromPercentage() {
		return fromPercentage;
	}

	/**
	 * Set the value of fromPercentage.
	 * @param v  Value to assign to fromPercentage.
	 */
	public void setFromPercentage(float v) {
		this.fromPercentage = v;
	}

	/**
	 * Get the value of toPercentage.
	 * @return value of toPercentage.
	 */
	public float getToPercentage() {
		return toPercentage;
	}

	/**
	 * Set the value of toPercentage.
	 * @param v  Value to assign to toPercentage.
	 */
	public void setToPercentage(float v) {
		this.toPercentage = v;
	}

	/**
	 * Returns the conditions.
	 * @return FilterCondition[]
	 */
	public FilterCondition[] getConditions() {
		return conditions;
	}

	/**
	 * Sets the conditions.
	 * @param conditions The conditions to set
	 */
	public void setConditions(FilterCondition[] conditions) {
		this.conditions = conditions;
	}

	public void addCondition(FilterCondition c) {
		FilterCondition[] tmp = new FilterCondition[conditions.length + 1];
		System.arraycopy(conditions, 0, tmp, 0, conditions.length);
		tmp[conditions.length] = c;
		conditions = tmp;
	}

	/**
	 * Returns the limiters.
	 * @return Sampler[]
	 */
	public Limiter[] getLimiters() {
		return limiters;
	}

	/**
	 *
	 * 0-1:5:100, 1:10:20:100
	 *
	 * @param limiters
	 * @return String
	 */
	public String getLimitersAsString(){
		return Limiter.getLimitersAsString(limiters);
	}

	/**
	 * Sets the limiters.
	 *
	 * 0-1:5:100, 1:10:20:100
	 *
	 * @param limiters The limiters to set
	 */
	public void setLimiters(String samplers) {
		setLimiters(Limiter.limitersFromString(samplers));
	}

	/**
	 * Sets the limiters.
	 * @param limiters The limiters to set
	 */
	public void setLimiters(Limiter[] samplers) {
		this.limiters = samplers;
	}

	public void resetLimiters() {
		for (int i = 0; i < limiters.length; i++) {
			limiters[i].count = 0;
		}
	}

	public void reset() {
		humanDecision = ArrayHelper.getTrueArray(Decision.NUM_DECISIONS);
		choiceMakerDecision = ArrayHelper.getTrueArray(Decision.NUM_DECISIONS);
		fromPercentage = 0f;
		toPercentage = 1f;
		conditions = ZERO_CONDITION;
		limiters = ZERO_SAMPLER;
		collection = null;
	}

	public int[] filterSource(Collection src) {
		if (src == null) {
			return new int[0];
		}
		resetLimiters();
		IntArrayList result = new IntArrayList(100);
		Iterator iSrc = src.iterator();
		int index = 0;
		while (iSrc.hasNext()) {
			MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) iSrc.next();
			if (satisfy(mrp) && (collection == null || collection.contains(index))) {
				result.add(index);
			}
			++index;
		}
		return result.toArray();
	}

	private boolean clueConditions(ActiveClues clues) {
		for (int i = 0; i < conditions.length; i++) {
			if (!conditions[i].satisfy(clues)) {
				return false;
			}
		}
		return true;
	}

	public boolean satisfy(MutableMarkedRecordPair mrp) {
		return humanDecision[mrp.getMarkedDecision().toInt()]
			&& choiceMakerDecision[mrp.getCmDecision().toInt()]
			&& fromPercentage <= mrp.getProbability()
			&& mrp.getProbability() <= toPercentage
			&& clueConditions(mrp.getActiveClues())
			&& satisfySamplers(mrp);
	}

	private boolean satisfySamplers(IMarkedRecordPair mrp) {
		boolean result = true;
		float probability = mrp.getProbability();
		for (int i = 0; i < limiters.length && result; ++i) {
			result &= limiters[i].accept(probability);
		}
		return result;
	}
	/**
	 * @return
	 */
	public IntArrayList getCollection() {
		return collection;
	}

	/**
	 * @param list
	 */
	public void setCollection(IntArrayList list) {
		collection = list;
	}

}
