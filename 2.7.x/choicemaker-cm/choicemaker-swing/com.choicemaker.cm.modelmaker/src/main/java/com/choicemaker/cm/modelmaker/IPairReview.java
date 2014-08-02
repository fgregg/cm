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

import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;

/**
 * @author rphall
 *
 */
public interface IPairReview {
	//************************************************************************************************
	public void setMarkedRecordPair(int p);

	public int getMarkedRecordPair();

	/**
	 * This method is called by the ClueTableCellListener which is
	 * attached to the clue table in the TestingPanel.  It allows
	 * one to step through the MRPs associated with a given
	 * clue.
	 *
	 * @param clueID
	 * @param fireType
	 */
	public void updateRecordPairList(int clueID, int fireType);

	public void displayRecordPairFilterDialog();

	public void filterMarkedRecordPairList();

	public int[] getSelection();

	public ListeningMarkedRecordPairFilter getFilter();

	public void setFilter(ListeningMarkedRecordPairFilter filter);

}
