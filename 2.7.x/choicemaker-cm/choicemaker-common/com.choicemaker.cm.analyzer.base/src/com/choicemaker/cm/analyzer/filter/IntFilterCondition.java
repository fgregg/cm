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

import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.IntActiveClues;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.3 $ $Date: 2010/03/29 14:36:26 $
 */
public class IntFilterCondition implements FilterCondition {

	private static final long serialVersionUID = 1L;
	public static final int NULL_PARAM = Integer.MIN_VALUE;
	private static final int NULL_CLUE_NUM = Integer.MIN_VALUE;

	public static final int MIN = 0;
	public static final int NULL_CONDITION = MIN;
	public static final int EQUALS = 1;
	public static final int NOT_EQUALS = 2;
	public static final int LESS_THAN = 3;
	public static final int LESS_THAN_EQUAL = 4;
	public static final int GREATER_THAN = 5;
	public static final int GREATER_THAN_EQUAL = 6;
	public static final int BETWEEN = 7;
	public static final int OUTSIDE = 8;
	public static final int MAX = OUTSIDE;

	private static final String CONDITION_STRING[] =
	{
//		"-",
//		"EQUALS",
//		"LESS THAN",
//		"LESS THAN or EQUAL",
//		"GREATER THAN",
//		"GREATER THAN or EQUAL",
//		"BETWEEN"
		"-",
		"=",
		"!=",
		"<",
		"<=",
		">",
		">=",
		"[a...b]",
		")a...b("
	};

	private int clueNum;
	private int condition;
	private int a = NULL_PARAM;
	private int b = NULL_PARAM;

	public IntFilterCondition(){
		this(NULL_CONDITION);
	}

	public IntFilterCondition(int condition){
		this(NULL_CLUE_NUM, condition, NULL_PARAM);
	}

	public IntFilterCondition(int clueNum, int condition, int value){
		this(clueNum, condition, value, value);
	}

	public IntFilterCondition(int clueNum, int condition, int a, int b){
		verifyArguments(condition);

		this.clueNum = clueNum;
		this.condition = condition;
		this.a = a;
		this.b = b;
	}

	protected void verifyArguments(int condition) throws IllegalArgumentException {
		if(condition < MIN || condition > MAX){
			throw new IllegalArgumentException("The condition must be one of the Constants exposed by IntFilterCondition");
		}
	}

	/**
	 * @see com.choicemaker.cm.train.filter.FilterCondition#satisfy(com.choicemaker.cm.core.base.ActiveClues)
	 */
	public boolean satisfy(ActiveClues clues) {
		IntActiveClues intActiveClues = (IntActiveClues)clues;
		boolean returnValue;

		switch (condition) {
			case EQUALS :
				returnValue = intActiveClues.values[clueNum] == a;
				break;

			case NOT_EQUALS :
				returnValue = intActiveClues.values[clueNum] != a;
				break;

			case LESS_THAN :
				returnValue = intActiveClues.values[clueNum] < b;
				break;

			case LESS_THAN_EQUAL :
				returnValue = intActiveClues.values[clueNum] <= b;
				break;

			case GREATER_THAN :
				returnValue = intActiveClues.values[clueNum] > a;
				break;

			case GREATER_THAN_EQUAL :
				returnValue = intActiveClues.values[clueNum] >= a;
				break;

			case BETWEEN :
				returnValue =  a <= intActiveClues.values[clueNum] && intActiveClues.values[clueNum] <= b;
				break;

			case OUTSIDE :
				returnValue = intActiveClues.values[clueNum] < a && b < intActiveClues.values[clueNum];
				break;

			default :
				returnValue = false;
				break;
		}

		return returnValue;
	}

	/**
	 * Returns the a.
	 * @return int
	 */
	public int getA() {
		return a;
	}

	/**
	 * Returns the b.
	 * @return int
	 */
	public int getB() {
		return b;
	}

	/**
	 * Returns the clueNum.
	 * @return int
	 */
	public int getClueNum() {
		return clueNum;
	}

	/**
	 * Returns the condition.
	 * @return int
	 */
	public int getCondition() {
		return condition;
	}

	public String getConditionString(){
		return CONDITION_STRING[getCondition()];
	}

	public String toString(){
		String returnValue = getConditionString();

		//TODO: consider making toString() more sophisticated.
		// For example, if it is a prototype return the conditionString,
		// otherwise return a nicely formatted compilation

		return returnValue;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a;
		result = prime * result + b;
		result = prime * result + clueNum;
		result = prime * result + condition;
		return result;
	}

	/**
	 * @return true if we represent a NULL_CONDITION and are being compared against a null, or a NULL_FILTER_CONDITION;
	 * true if we are compared agains another IntFilterCondition that represents the same condition as us;
	 * false otherwise.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ( obj == null || obj == FilterCondition.NULL_FILTER_CONDITION){
			return getCondition() == NULL_CONDITION;
		}
		if (getClass() != obj.getClass())
			return false;
		IntFilterCondition other = (IntFilterCondition) obj;
		if (getA() != other.getA())
			return false;
		if (getB() != other.getB())
			return false;
		if (getClueNum() != other.getClueNum())
			return false;
		if (getCondition() != other.getCondition())
			return false;
		return true;
	}

	/**
	 * Old implmemtation of {@link #equals(Object)}. Use this method only for
	 * testing.
	 */
	public boolean equals_00(Object other){
		if ( other == null || other == FilterCondition.NULL_FILTER_CONDITION){
			return getCondition() == NULL_CONDITION;
		}
		else{
			if (other instanceof IntFilterCondition){
				IntFilterCondition otherfilterCondition = (IntFilterCondition)other;
				return otherfilterCondition.getA() == getA()
					&& otherfilterCondition.getB() == getB()
					&& otherfilterCondition.getClueNum() == getClueNum()
					&& otherfilterCondition.getCondition() == getCondition();
			}
			else{
				return false;
			}
		}
	}
	/**
	 * @see com.choicemaker.cm.train.filter.FilterCondition#createFilterCondition(int)
	 */
	public FilterCondition createFilterCondition(int clueNum) {
		return new IntFilterCondition(clueNum, condition, a, b);
	}

}
