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
package com.choicemaker.cm.modelmaker.gui.tables;

import javax.swing.table.AbstractTableModel;

/**
 * Interface for a table whose columns are sortable.
 * 
 * @author S. Yoakum-Stover
 */
public abstract class SortableTableModel extends AbstractTableModel {

	protected int sortCol = 0;
	protected boolean sortAsc = true;

	public abstract void sort();

	/**
	 * By default all columns are sortable.
	 */
	public boolean isColumnSortable(int column){
		return true;
	}

	public int getSortedColumnIndex() {
		return sortCol;
	}

	public void setSortedColumnIndex(int i) {
		sortCol = i;
	}

	public boolean isSortOrderAscending() {
		return sortAsc;
	}

	public void reverseSortOrder() {
		sortAsc = !sortAsc;
	}

}
