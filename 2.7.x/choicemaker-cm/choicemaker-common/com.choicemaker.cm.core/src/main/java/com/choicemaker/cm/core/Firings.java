package com.choicemaker.cm.core;

import com.choicemaker.cm.core.base.RecordData;

/**
 * The clues (and rules) that are triggered by a pair of records within the
 * context of some probability model. This interface makes sense only within the
 * context of a specific model evaluating a particular record pair.
 */
public interface Firings {

	/** The model to which these active clues and rules refer */
	ImmutableProbabilityModel getProbabilityModel();

	/** The pair of records to which these active clues and rules refer */
	RecordData getRecordPair();

	/** The number of active clues */
	int clueCount();

	/**
	 * Adds the index of an active clue to the active list.
	 *
	 * @param  clueNumber  The index of the clue to be added.
	 * @param  modifier  The modifier, e.g., note or report.
	 * @see    ClueDesc
	 */
	void addClue(int clueNumber, int modifier);

	/**
	 * Returns <code>true</code> if the active list
	 * contains the specified clue.
	 *
	 * @param   clueNumber  The clue number.
	 * @return  <code>true</code> if the active list
	 *            contains the specified clue.
	 */
	boolean containsClue(int clueNumber);

	/**
	 * Returns the clue index at the specified index in this list.
	 *
	 * @param   index  The index.
	 * @return  The clue number.
	 */
	int getClue(int index);

	/**
	 * Returns the indices of all active clues and rules.
	 *
	 * @return   The indices of all active clues and rules.
	 */
	int[] getCluesAndRules();

	/**
	 * Returns <code>true</code> if the active list of contains
	 * the specified index.
	 *
	 * @param   number  The clue/rule number.
	 * @return  <code>true</code> if the active list contains
	 *          the specified index.
	 */
	boolean containsClueOrRule(int number);

	/**
	 * Gets the number of active rules.
	 *
	 * @return  The number of active rules.
	 */
	int ruleCount();

	/**
	 * Returns <code>true</code> if the active list contains the specified rule.
	 *
	 * @param   ruleNumber  The rule number.
	 * @return  <code>true</code> if the list list of rules
	 *            contains the specified rule.
	 */
	boolean containsRule(int ruleNumber);

	/**
	 * Adds the index of a rule to the list.
	 *
	 * @param  ruleNumber  The index of the rule to be added.
	 * @param  modifier  The modifier, e.g., note or report.
	 * @see    ClueDesc
	 */
	void addRule(int ruleNumber, int modifier);

	/**
	 * Returns the indices of all active rules.
	 *
	 * @return   The indices of all active rules.
	 */
	int[] getRules();

	/**
	 * Returns the indices of all active clues and rules with a note or report modifier.
	 *
	 * @return   The indices of all active rules and rules with a note or report modifier.
	 */
	int[] getNotes();

	/**
	 * Returns the rule at the specified index in the active rules list.
	 *
	 * @return   The rule at the specified index in the active rules list.
	 */
	int getRule(int idx);

	/**
	 * Returns true if there is an active clue or rule with report modifier.
	 *
	 * @return   Returns true if there is an active clue or rule with report modifier.
	 */
	boolean isReport();

}
