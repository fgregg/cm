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
package com.choicemaker.cm.modelmaker.gui.utils;

import java.text.DecimalFormat;
import java.util.List;

import com.choicemaker.cm.core.Decision;
import com.jrefinery.data.AbstractDataset;
import com.jrefinery.data.CategoryDataset;
import com.jrefinery.data.DefaultKeyedValues2D;

/**
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:10 $
 */
public class HistoCategoryDataset extends AbstractDataset implements CategoryDataset {
	/** A storage structure for the data. */
	private DefaultKeyedValues2D data;

	/** The series names. */
	protected String[] seriesNames;

	/** The categories. */
	protected String[] categories;

	private boolean includeHolds = true;

	private boolean hd;

	private static final Integer NULL_INT = new Integer(0);
	private static final DecimalFormat DF = new DecimalFormat("##0");

	public HistoCategoryDataset(String[] seriesNames, int numCategories) {
		this.seriesNames = seriesNames;
		setNumCategories(numCategories);
		this.data = new DefaultKeyedValues2D();
	}

	public void setNumCategories(int numCategories) {
		String[] cats = new String[numCategories];
		float step = 100f / numCategories;
		float base = 0f;
		for (int i = 0; i < numCategories; ++i) {
			cats[i] = DF.format(base) + " - " + DF.format(base += step) + " %";
		}
		this.categories = cats;
	}

	public void setIncludeHolds(boolean v) {
		includeHolds = v;
	}
	
	public boolean isIncludeHolds() {
		return includeHolds;
	}

	public void setData(Number[][] dd) {
		this.data = new DefaultKeyedValues2D();
		if (dd == null) {
			hd = false;
		} else {
			for (int i = 0; i < dd.length; i++) {
				Decision decision = Decision.valueOf(decisionMap(i));
				if (includeHolds || decision != Decision.HOLD) {
					Number[] d = dd[i];
					Comparable r = seriesNames[i];
					for (int j = 0; j < d.length; j++) {
						Comparable c = categories[j];
						setValue(d[j], r, c);
					}
				}
			}
			hd = true;
		}
		this.fireDatasetChanged();
	}

	private int decisionMap(int d) {
		if (d == 0) {
			return 0;
		} else if (d == 1) {
			return 2;
		} else {
			return 1;
		}
	}

	public boolean hasData() {
		return hd;
	}

	/**
	 * Returns the number of rows in the table.
	 *
	 * @return the row count.
	 */
	public int getRowCount() {
		return this.data.getRowCount();
	}

	/**
	 * Returns the number of columns in the table.
	 *
	 * @return the column count.
	 */
	public int getColumnCount() {
		return this.data.getColumnCount();
	}

	/**
	 * Returns a value from the table.
	 *
	 * @param row  the row index (zero-based).
	 * @param column  the column index (zero-based).
	 *
	 * @return the value (possibly null).
	 */
	public Number getValue(int row, int column) {
		return this.data.getValue(row, column);
	}

	/**
	 * Returns a row key.
	 *
	 * @param row  the row index (zero-based).
	 *
	 * @return the row key.
	 */
	public Comparable getRowKey(int row) {
		return this.data.getRowKey(row);
	}

	/**
	 * Returns the row index for a given key.
	 *
	 * @param key  the row key.
	 *
	 * @return the row index.
	 */
	public int getRowIndex(Comparable key) {
		return this.data.getRowIndex(key);
	}

	/**
	 * Returns the row keys.
	 *
	 * @return the keys.
	 */
	public List getRowKeys() {
		return this.data.getRowKeys();
	}

	/**
	 * Returns a column key.
	 *
	 * @param column  the column index (zero-based).
	 *
	 * @return the column key.
	 */
	public Comparable getColumnKey(int column) {
		return this.data.getColumnKey(column);
	}

	/**
	 * Returns the column index for a given key.
	 *
	 * @param key  the column key.
	 *
	 * @return the column index.
	 */
	public int getColumnIndex(Comparable key) {
		return this.data.getColumnIndex(key);
	}

	/**
	 * Returns the column keys.
	 *
	 * @return the keys.
	 */
	public List getColumnKeys() {
		return this.data.getColumnKeys();
	}

	/**
	 * Returns the value for a pair of keys.
	 * <P>
	 * This method should return <code>null</code> if either of the keys is not found.
	 *
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 *
	 * @return the value.
	 */
	public Number getValue(Comparable rowKey, Comparable columnKey) {
		return this.data.getValue(rowKey, columnKey);
	}

	/**
	 * Adds a value to the table.  Performs the same function as setValue(...).
	 * 
	 * @param value  the value.
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 */
	public void addValue(Number value, Comparable rowKey, Comparable columnKey) {
		this.data.addValue(value, rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Adds a value to the table.
	 * 
	 * @param value  the value.
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 */
	public void addValue(double value, Comparable rowKey, Comparable columnKey) {
		this.addValue(new Double(value), rowKey, columnKey);
	}

	/**
	 * Adds or updates a value in the table.
	 * 
	 * @param value  the value.
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 */
	public void setValue(Number value, Comparable rowKey, Comparable columnKey) {
		this.data.setValue(value, rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Adds or updates a value in the table.
	 * 
	 * @param value  the value.
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 */
	public void setValue(double value, Comparable rowKey, Comparable columnKey) {
		this.setValue(new Double(value), rowKey, columnKey);
	}

	/**
	 * Removes a value from the dataset.
	 * 
	 * @param rowKey  the row key.
	 * @param columnKey  the column key.
	 */
	public void removeValue(Comparable rowKey, Comparable columnKey) {
		this.data.removeValue(rowKey, columnKey);
		fireDatasetChanged();
	}

	/**
	 * Removes a row from the dataset.
	 * 
	 * @param rowIndex  the row index.
	 */
	public void removeRow(int rowIndex) {
		this.data.removeRow(rowIndex);
		fireDatasetChanged();
	}

	/**
	 * Removes a row from the dataset.
	 * 
	 * @param rowKey  the row key.
	 */
	public void removeRow(Comparable rowKey) {
		this.data.removeRow(rowKey);
		fireDatasetChanged();
	}

	/**
	 * Removes a column from the dataset.
	 * 
	 * @param columnIndex  the column index.
	 */
	public void removeColumn(int columnIndex) {
		this.data.removeColumn(columnIndex);
		fireDatasetChanged();
	}

	/**
	 * Removes a column from the dataset.
	 * 
	 * @param columnKey  the column key.
	 */
	public void removeColumn(Comparable columnKey) {
		this.data.removeColumn(columnKey);
		fireDatasetChanged();
	}
}
