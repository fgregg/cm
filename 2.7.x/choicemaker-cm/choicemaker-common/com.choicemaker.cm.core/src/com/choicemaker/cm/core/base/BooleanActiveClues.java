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

import com.choicemaker.cm.core.util.IntArrayList;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public class BooleanActiveClues extends ActiveClues {
	// Use IntArrayList rather than ArrayList to avoid creation of Integer objects.
	private IntArrayList activeClues;

	public BooleanActiveClues() {
		this(30);
	}
	
	/**
	 * Constructs a <code>ActiveClues</code> instance with the specified
	 * initial capacity for the list of active clues.
	 * 
	 * @param   initialCapacity  The initial capacity of each list.
	 */
	public BooleanActiveClues(int initialCapacity) {
		activeClues = new IntArrayList(initialCapacity);
	}


	public int size() {
		return activeClues.size();
	}
	
	/**
	 * Adds the index of an active clue to the respective list.
	 *
	 * @param  clueNumber  The index of the clue to be added.
	 * @param  modifier  The modifier, e.g., note or report.
	 * @see    ClueDesc
	 */
	public void add(int clueNumber, int modifier) {
		activeClues.add(clueNumber);
		if (modifier == ClueDesc.NOTE) {
			notes.add(clueNumber);
		} else if (modifier == ClueDesc.REPORT) {
			notes.add(clueNumber);
			report = true;
		}
	}

	/**
	 * Returns the clue index at the specified index in this list.
	 *
	 * @param   index  The index.
	 * @return  The clue number.
	 */
	public int get(int index) {
		return activeClues.get(index);
	}
	
	/**
	 * Returns the indices of all active clues and rules.
	 * <p>
	 * The parameter <code>model</code> is needed to efficiently support
	 * clues that are used for decision making and as rules.
	 *
	 * @return   The indices of all active clues and rules.
	 */
	public int[] getCluesAndRules() {
		IntArrayList res = new IntArrayList();
		res.addAll(activeClues);
		res.addAll(rules);
		return res.toArray();
	}

	/**
	 * Returns <code>true</code> if the list of active clues
	 * contains the specified clue.
	 *
	 * @param   clueNumber  The clue number.
	 * @return  <code>true</code> if the list list of active clues
	 *            contains the specified clue.
	 */
	public boolean containsClue(int clueNumber) {
		return activeClues.contains(clueNumber);
	}

	/**
	 * Returns <code>true</code> if the list of active clues or rules contains
	 * the specified index.
	 *
	 * @param   number  The clue/rule number.
	 * @return  <code>true</code> if the list of active clues or rules contains
	 *          the specified index.
	 */
	public boolean containsClueOrRule(int number) {
		return activeClues.contains(number) || rules.contains(number);
	}
}
