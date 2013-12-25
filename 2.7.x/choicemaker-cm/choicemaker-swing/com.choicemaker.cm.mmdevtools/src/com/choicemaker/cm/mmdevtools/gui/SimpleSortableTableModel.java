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
package com.choicemaker.cm.mmdevtools.gui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import com.choicemaker.cm.modelmaker.gui.tables.SortableTableModel;

/**
 * @author ajwinkel
 *
 */
public class SimpleSortableTableModel extends SortableTableModel {

	protected Object[][] data;
	protected Object[] headers;
	protected Class[] columnClasses;
	
	protected Integer[] rowMap;
	protected Comparator comp = new RowComparator();

	public SimpleSortableTableModel(Object[][] data, Object[] headers) {
		this.data = data;
		this.headers = headers;

		resetSorting();
		initColumnClasses();
	}
		
	public void resetSorting() {
		rowMap = new Integer[getRowCount()];
		for (int i = 0; i < rowMap.length; i++) {
			rowMap[i] = Integer.valueOf("" + i);
		}
		
		setSortedColumnIndex(-1);
	}

	public void sort() {
		int index = getSortedColumnIndex();
		if (index < 0) {
			return;
		}
		Arrays.sort(rowMap, comp);
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return headers.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public Object getValueAt(int row, int col) {
		int dataRow = rowMap[row].intValue();
		return data[dataRow][col];
	}

	public String getColumnName(int col) {
		if (getSortedColumnIndex() != col) {
			return headers[col].toString();
		} else {
			String name = headers[col].toString();
			if (isSortOrderAscending()) {
				return name + " >>";
			} else {
				return name + " <<";
			}
		}
	}

	public Class getColumnClass(int col) {
		return columnClasses[col];
	}

	private void initColumnClasses() {
		int numRows = getRowCount();
		int numColumns = getColumnCount();

		columnClasses = new Class[numColumns];		
		if (numRows * numColumns == 0) {
			for (int col = 0; col < numColumns; col++) {
				columnClasses[col] = Object.class; 
			}
			return;
		}
		
		for (int col = 0; col < numColumns; col++) {
			Class cls = null;
			for (int row = 0; row < numRows; row++) {
				Object val = getValueAt(row, col);
				if (val != null) {
					if (cls == null) { 
						cls = val.getClass();
					} else {
						cls = getGreatestCommonSuperclass(cls, val.getClass());
					}
				}
			}
			if (cls != null) {
				columnClasses[col] = cls;
			} else {
				columnClasses[col] = Object.class;
			}
		}
	}

	private Class getGreatestCommonSuperclass(Class c1, Class c2) {
		if (c1.equals(c2)) {
			return c1;
		}

		LinkedList h1 = new LinkedList();
		LinkedList h2 = new LinkedList();
		
		do {
			h1.add(0, c1);
			c1 = c1.getSuperclass();
		} while (c1 != null);

		do {
			h2.add(0, c2);
			c2 = c2.getSuperclass();
		} while (c2 != null);

		Class sup = Object.class;

		int max = Math.min(h1.size(), h2.size());
		for (int i = 0; i < max; i++) {
			if (!h1.get(i).equals(h2)) {
				break;
			} else {
				sup = (Class) h1.get(i);
			}
		}
		
		return sup;
	}

	private class RowComparator implements Comparator {
		public int compare(Object obj1, Object obj2) {
			int i1 = ((Integer)obj1).intValue();
			int i2 = ((Integer)obj2).intValue();
			
			Object v1 = data[i1][getSortedColumnIndex()];
			Object v2 = data[i2][getSortedColumnIndex()];
			
			int order = 0;
			
			if (v1 == null) {
				order = -1;
			} else if (v2 == null) {
				order = 1;
			} else if (!(v1 instanceof Comparable) || !(v2 instanceof Comparable)) {
				order = 0;
			} else {
				order = ((Comparable)v1).compareTo(v2);
			}
			
			return isSortOrderAscending() ? order : -order;
		}
	}

}
