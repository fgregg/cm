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

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.IntArrayList;

/**
 * Encapsulates a list of active clues for each <code>Decision</code> and
 * active rules.
 *
 * Clues and rules are assigned consecutive numbers, starting at 0, in the order
 * of definition in the clue file. They are numbered together, e.g., if the clue
 * file contains a clue A, followed by a rule B and a clue C, A has number 0,
 * B number 1, and C number 2. 
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:59:11 $
 * @see       Decision
 */
public abstract class ActiveClues {
	/** The list of the indexes of all active rules. */
	protected IntArrayList rules;
	protected IntArrayList notes;
	protected boolean report;

	/**
	 * Constructs a <code>ActiveClues</code> instance with the default capacity
	 * for the list of active clues.
	 */
	public ActiveClues() {
		rules = new IntArrayList();
		notes = new IntArrayList();
	}

	/**
	 * Gets the number of active clues.
	 *
	 * @return  The number of active clues.
	 */
	public abstract int size();

	/**
	 * Returns <code>true</code> if the list of rules contains the specified rule.
	 *
	 * @param   ruleNumber  The rule number.
	 * @return  <code>true</code> if the list list of rules
	 *            contains the specified rule.
	 */
	public boolean containsRule(int ruleNumber) {
		return rules.contains(ruleNumber);
	}

	/**
	 * Adds the index of a rule to the list.
	 *
	 * @param  ruleNumber  The index of the rule to be added.
	 * @param  modifier  The modifier, e.g., note or report.
	 * @see    ClueDesc
	 */
	public void addRule(int ruleNumber, int modifier) {
		rules.add(ruleNumber);
		if (modifier == ClueDesc.NOTE) {
			notes.add(ruleNumber);
		} else if (modifier == ClueDesc.REPORT) {
			notes.add(ruleNumber);
			report = true;
		}
	}

	/**
	 * Returns the indices of all active rules.
	 *
	 * @return   The indices of all active rules.
	 */
	public int[] getRules() {
		return rules.toArray();
	}

	/**
	 * Returns the indices of all active clues and rules with a note or report modifier.
	 *
	 * @return   The indices of all active clues and rules with a note or report modifier.
	 */
	public int[] getNotes() {
		return notes.toArray();
	}

	/**
	 * Returns the number of active rules.
	 *
	 * @return   The number of active rules.
	 */
	public int sizeRules() {
		return rules.size();
	}

	/**
	 * Returns the rule at the specified index in the active rules list.
	 *
	 * @return   The rule at the specified index in the active rules list.
	 */
	public int getRule(int idx) {
		return rules.get(idx);
	}

	/**
	 * Returns the names of all rules and clues with modifier note or report.
	 *
	 * @param   model  The <code>ProbabilityModel</code> used to produce this
	 *            instance.
	 * @return  The names of all active rules and clues with modifier note or report.
	 */
	public String[] getNotes(ImmutableProbabilityModel model) {
		ClueDesc[] cd = model.getAccessor().getClueSet().getClueDesc();
		int size = notes.size();
		String[] res = new String[size];
		for (int i = 0; i < size; ++i) {
			res[i] = cd[notes.get(i)].getName();
		}
		return res;
	}

	/**
	 * Returns true if there is an active clue or rule with report modifier.
	 *
	 * @return   Returns true if there is an active clue or rule with report modifier.
	 */
	public boolean isReport() {
		return report;
	}
}
