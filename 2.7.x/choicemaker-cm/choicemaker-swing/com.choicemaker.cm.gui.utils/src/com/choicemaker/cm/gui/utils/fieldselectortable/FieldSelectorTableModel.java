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
package com.choicemaker.cm.gui.utils.fieldselectortable;

import java.beans.*;

import javax.swing.JLabel;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.gui.utils.viewer.*;

/**
 * .  
 * 
 * @author  Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class FieldSelectorTableModel extends DefaultTableModel implements TableColumnModelListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(FieldSelectorTableModel.class);

	/**
	 * NOTE: this is the ColumnModel for the RecordTable... The <code> FieldSelectorTable </code>
	 * 		 is used to configute the <code>RecordTable</code>.  So the RecordTableColumnModel is
	 * 		 the actual <code>TableModel</code> for the FieldSelectorTable.
	 */
	private RecordTableColumnModel columnModel;

	static final public ColumnDefinition columns[] =
		{
			new ColumnDefinition("Field", 150, JLabel.CENTER),
			new ColumnDefinition("Alias", 150, JLabel.CENTER),
			new ColumnDefinition("Displayed", 450, JLabel.CENTER)
		};

	public static final int COL_FIELD = 0;
	public static final int COL_ALIAS = 1;
	public static final int COL_DISPLAYED = 2;

	private static final int COL_MAX = COL_DISPLAYED;

	public FieldSelectorTableModel(RecordTableColumnModel columnModel) {
		this.columnModel = columnModel;
		columnModel.addColumnModelListener(this);
		columnModel.addPropertyChangeListener(this);
	}

	public ColumnDefinition[] getColumnDefinitions() {
		return columns;
	}

	public String getTitle() {
		return "Field Selector";
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

	public String getColumnName(int col) {
		ColumnDefinition[] columnDefinitions = getColumnDefinitions();
		String str = columnDefinitions[col].getName();
		return str;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 2)
			return Boolean.class;
		else
			return String.class;
	}

	/**
	 * The Field name cannot be edited but its alias and visibility can.
	 */
	public boolean isCellEditable(int nRow, int nCol) {
		return nCol != 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		int returnValue = 0;
		if (columnModel != null){
			RecordTableColumn[] tableColumns = columnModel.getVisibleAndInvisibleColumns();
			returnValue = tableColumns.length;
		}
		
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		RecordTableColumn column = columnModel.getVisibleAndInvisibleColumns()[rowIndex];

		switch (columnIndex) {
			case COL_FIELD :
				return column.getFieldName();
			case COL_ALIAS :
				return column.getIdentifier();
			case COL_DISPLAYED :
				return Boolean.valueOf(column.isVisible());
		}
		throw new IllegalStateException("No such column: " + columnIndex);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		RecordTableColumn column = columnModel.getVisibleAndInvisibleColumns()[rowIndex];

		switch (columnIndex) {
			case COL_FIELD :
				throw new IllegalArgumentException("Fields cannot be edited. " + aValue);
			case COL_ALIAS :
				if (aValue instanceof String) {
					columnModel.setAlias(column, (String) aValue);
				} else {
					throw new IllegalArgumentException("Aliases are Strings. " + aValue);
				}
				break;
			case COL_DISPLAYED :
				if (aValue instanceof Boolean) {
					if (Boolean.TRUE == ((Boolean) aValue)){
						//BUG FIX #97: check the current condition.
						if (!((RecordTableColumn) column).isVisible()){
							columnModel.addColumn(column);
						}
					}
					else{
						//BUG FIX #97: check the current condition.
						if (((RecordTableColumn) column).isVisible()){
							columnModel.removeColumn(column);
						}
					}
				} else {
					throw new IllegalArgumentException("Visibility is Boolean. " + aValue);
				}
				break;
			default :
				throw new IllegalStateException("No such column: " + columnIndex);
		}
		this.fireTableDataChanged();
	}
	
	public boolean areAllColumnsVisible() {
		return columnModel.areAllColumnsVisible();
	}

	public void toggleAllColumnsVisible() {
		columnModel.toggleAllColumnsVisible();
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableColumnModelListener#columnAdded(javax.swing.event.TableColumnModelEvent)
	 */
	public void columnAdded(TableColumnModelEvent e) {
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableColumnModelListener#columnRemoved(javax.swing.event.TableColumnModelEvent)
	 */
	public void columnRemoved(TableColumnModelEvent e) {
		this.fireTableDataChanged();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableColumnModelListener#columnMoved(javax.swing.event.TableColumnModelEvent)
	 */
	public void columnMoved(TableColumnModelEvent e) {
		// DO NOTHING
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableColumnModelListener#columnMarginChanged(javax.swing.event.ChangeEvent)
	 */
	public void columnMarginChanged(ChangeEvent e) {
		// DO NOTHING
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void columnSelectionChanged(ListSelectionEvent e) {
		// DO NOTHING
	}
}
