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
package com.choicemaker.cm.modelmaker.gui.tables.filtercluetable;

import java.util.Collections;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.filter.FilterCondition;
import com.choicemaker.cm.modelmaker.gui.tables.SortableTableModel;
import com.choicemaker.cm.modelmaker.gui.utils.ClueDataComparator;

/**
 * The TableModel for the ClueTable in the AbstractModelReviewPanel.  
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:53:36 $
 */
public class FilterClueTableModel extends SortableTableModel {

	private static Logger logger = Logger.getLogger(FilterClueTableModel.class);

	private ClueDesc[] descriptions;
	private boolean isEmpty;
	private int numClues;
	private ClueSet clueSet;

	private Vector rows;

	static final public ColumnDefinition columns[] =
		{
			new ColumnDefinition(MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.id"), 100, JLabel.RIGHT),
			new ColumnDefinition(MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.cluename"), 550, JLabel.LEFT),
			new ColumnDefinition(MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.decision"), 150, JLabel.RIGHT),
			new ColumnDefinition(MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.type"), 150, JLabel.RIGHT),
			new ColumnDefinition(MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.report"), 150, JLabel.RIGHT),
			new ColumnDefinition("Condition", 150, JLabel.CENTER),
			new ColumnDefinition("Parameters", 450, JLabel.CENTER)};

	public static final int COL_ID = 0;
	public static final int COL_NAME = 1;
	public static final int COL_DECISION = 2;
	public static final int COL_TYPE = 3;
	public static final int COL_REPORT = 4;

	private static final int COL_FILTER_CONDITION = 5;
	/**
	 * NOTE: The two columns: COL_CONDITION and COL_PARAMETERS refer to the same Item in the FilterClueTableRow
	 */
	public static final int COL_CONDITION = COL_FILTER_CONDITION;
	/**
	 * NOTE: The two columns: COL_CONDITION and COL_PARAMETERS refer to the same Item in the FilterClueTableRow
	 */
	public static final int COL_PARAMETERS = COL_FILTER_CONDITION + 1;

	private static final int COL_MAX = COL_PARAMETERS;

	public FilterClueTableModel() {
		isEmpty = true;
		numClues = 0;
		rows = new Vector(10);
		buildRows();
	}

	public ColumnDefinition[] getColumnDefinitions() {
		return columns;
	}
	/**
	 * The COL_PARAMETERS is not sortable.
	 * The COL_CONDITION is not sortable.
	 */
	public boolean isColumnSortable(int column) {
		return (column != COL_PARAMETERS && column != COL_CONDITION);
	}

	public void sort() {
		//logger.debug("Sorting called. sortCol = " + sortCol + " sort order = " + sortAsc);
		Collections.sort(rows, new ClueDataComparator(sortCol, sortAsc));
	}

	public void refresh(ImmutableProbabilityModel pModel) {
		if (pModel == null) {
			wipeOutTable();
			return;
		}
		clueSet = pModel.getClueSet();
		descriptions = clueSet.getClueDesc();
		numClues = descriptions.length;
		isEmpty = false;
		sortCol = 0;
		sortAsc = true;
		buildRows();
	}

	public void wipeOutTable() {
		isEmpty = true;
		numClues = 0;
		descriptions = null;
		sortCol = 0;
		sortAsc = true;
		buildRows();
	}

	public void buildRows() {
		rows.clear();
		//logger.debug("Building rows.");
		for (int i = 0; i < numClues; i++) {
			FilterClueTableRow arow = new FilterClueTableRow(clueSet, descriptions[i]);
			rows.add(arow);
		}
	}

	public String getTitle() {
		return MessageUtil.m.formatMessage("train.gui.modelmaker.table.filter.label");
	}

	/**
	 * Returns the number of columns in the model. A
	 * <code>JTable</code> uses this method to determine how many columns it
	 * should create and display by default.
	 *
	 * @return the number of columns in the model
	 * @see #getRowCount
	 */
	public int getColumnCount() {
		return columns.length;
	}
	/**
	 * Returns the number of rows in the model. A
	 * <code>JTable</code> uses this method to determine how many rows it
	 * should display.  This method should be quick, as it
	 * is called frequently during rendering.
	 *
	 * @return the number of rows in the model
	 * @see #getColumnCount
	 */
	public int getRowCount() {
		return numClues;
	}

	public String getColumnName(int col) {
		String str = columns[col].getName();
		if (col == sortCol) {
			str += sortAsc ? " >>" : " <<";
		}
		return str;
	}

	public boolean isCellEditable(int nRow, int nCol) {
		return refersToFilterCondition(nCol) && !isNullConditionParameters(nRow, nCol);
	}

	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>. This method behaves correctly even if 
	 * we don't have any data.
	 * 
	 * @param rowIndex the row whose value is to be queried
	 * @param columnIndex
	 *                 the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (isEmpty || (rowIndex < 0) || (rowIndex >= getRowCount())) {
			return "";
		} else {
			return getValueAt(((FilterClueTableRow) rows.elementAt(rowIndex)), columnIndex);
		}
	}

	/**
	 * translates between the public perception of the Model and the model as is stored in the FilterCluesTableRow.
	 */
	protected Object getValueAt(FilterClueTableRow row, int columnIndex) {
		if (refersToFilterCondition(columnIndex)) {
			FilterCondition data = row.getFilterCondition();
			switch (columnIndex) {
				case COL_CONDITION :
					// falls through.
				case COL_PARAMETERS :
					return data;

				default :
					throw new IllegalArgumentException("columnIndex {" + columnIndex + "} > number of columns {" + COL_MAX + "}");
			}
		} else {
			return row.getColumn(columnIndex);
		}
	}

	/**
	 * translates between the public perception of the Model and the model as is stored in the FilterCluesTableRow.
	 * NOTE: only columns that refer to FilterConditions are editable.
	 */
	public void setValueAt(Object value, int nRow, int nCol) {
		if (refersToFilterCondition(nCol)) {
			setFilterConditionAt((FilterCondition) value, nRow);
		}
		fireTableChanged(new TableModelEvent(this, nRow, nRow, nCol));
	}

	protected void setFilterConditionAt(FilterCondition value, int nRow) {
		((FilterClueTableRow) rows.elementAt(nRow)).setFilterCondition(value);
	}

	protected boolean refersToFilterCondition(int columnIndex) {
		return columnIndex == COL_CONDITION || columnIndex == COL_PARAMETERS;
	}

	protected boolean isNullConditionParameters(int rowIndex, int columnIndex) {
		return columnIndex == COL_PARAMETERS && FilterCondition.NULL_FILTER_CONDITION.equals(getValueAt(rowIndex, columnIndex));
	}

	//TODO: rename method to addFilterConditions or something like it.
	public void select(FilterCondition[] filterCondition) {
		deselectAll();
		for (int i = 0; i < filterCondition.length; i++) {
			if (filterCondition[i] != null) {
				setFilterConditionAt(filterCondition[i], filterCondition[i].getClueNum());
			}
		}
	}

	/**
	 * Creates a sparse array of FilterConditions (does not include null conditions).
	 */
	public FilterCondition[] getFilterConditions() {
		Vector filterConditions = new Vector();
		for (int i = 0; i < rows.size(); i++) {
			FilterCondition condition = ((FilterClueTableRow) rows.elementAt(i)).getFilterCondition();
			if (condition != null && !FilterCondition.NULL_FILTER_CONDITION.equals(condition)) {
				filterConditions.addElement(condition);
			}
		}
		FilterCondition[] returnValue = new FilterCondition[filterConditions.size()];
		filterConditions.copyInto(returnValue);

		return returnValue;
	}

	public void deselectAll() {
		buildRows();
	}
}
