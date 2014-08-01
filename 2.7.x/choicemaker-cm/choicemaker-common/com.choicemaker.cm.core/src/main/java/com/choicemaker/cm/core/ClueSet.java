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
package com.choicemaker.cm.core;

import com.choicemaker.cm.core.base.ActiveClues;

/**
 * Base interface for generated <code>ClueSet</code>s.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public interface ClueSet {
	/**
	 * Returns the type of the clueset
	 * 
	 * @return The type of the clueset.
	 */
	ClueSetType getType();
	
	/**
	 * Returns whether the clueset has decisions.
	 * 
	 * @return  Whether the clueset has decisions.
	 */
	boolean hasDecision();
	
	/**
	 * Returns the number of clues and rules in this <code>ClueSet</code>.
	 *
	 * @return  The number of clues and rules in this <code>ClueSet</code>.
	 */
	int size();

	/**
	 * Returns the number of clues predicting <code>Decision<code>
	 * <code>d</code> in this <code>ClueSet</code>.
	 *
	 * @return  The number of clues predicting <code>Decision</code>
	 *            <code>d</code> in this <code>ClueSet</code>.
	 */
	int size(Decision d);

	/**
	 * Returns the descriptions of the clues.
	 *
	 * @return  The descriptions of the clues.
	 */
	ClueDesc[] getClueDesc();

	/**
	 * Computes the clues and rules that fire when comparing two records.
	 *
	 * @param   q  One of the records.
	 * @param   m  The other record.
	 * @param   eval  The clues that should be evaluated. Used to filter out clues
	 *            that did not fire often enough on a particular training set or that
	 *            were manually turned off.
	 * @return  The active clues.
	 */
	ActiveClues getActiveClues(Record q, Record m, boolean[] eval);
}
