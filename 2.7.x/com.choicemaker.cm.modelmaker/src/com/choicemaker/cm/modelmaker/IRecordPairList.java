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
package com.choicemaker.cm.modelmaker;

/**
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/28 17:14:24 $
 */
public interface IRecordPairList {

	/** Same convention for no selection as JList (e.g. return <code>-1</code>) */
	public final int NO_SELECTION = -1;

	/** Sets the list of pairs displayed by this list */
	public abstract void updateRecordPairList(int[] items);

	/**
	 * Selects the next pair in this list, or {@link #getLastIndex()} if
	 * the selectikon is already at the last pair.
	 */
	public abstract void reviewNextMarkedRecordPair();

	/**
	 * Selects the previous pair in this list, or {@link #getFirstIndex()} if
	 * the selectikon is already at the first pair.
	 */
	public abstract void reviewPreviousMarkedRecordPair();

	/** Returns the number of pairs in this list */
	public abstract int modelSize();

	/** Returns a (non-negative) value greater than {@link #NO_SELECTION} */
	public abstract int getFirstIndex();

	/** Returns a (non-negative) value at least as large as {@link #getFirstIndexI()} */
	public abstract int getLastIndex();

	/**
	 * Returns a value in the range {@link #getFirstIndex()} to
	 * {@link #getLastIndex()} (inclusive) if some record is selected,
	 * otherwise returns {@link com.choicemaker.cm.modelmaker.gui.IRecordPairList#NO_SELECTION
	 */
	public abstract int getSelectedIndex();

	// public abstract void setSelectedIndex(int index);

}

