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
package com.choicemaker.cm.gui.utils.viewer;

import javax.swing.table.AbstractTableModel;

import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.base.RecordData;

/**
 * The TableModel for the RecordTable.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class RecordTableModel extends AbstractTableModel implements RepositoryChangeListener {
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(RecordTableModel.class);

	private Descriptor descriptor;
	private boolean topTable;
	private RecordData recordData;
	private Record record;
	private Record comparisonRecord;
	private boolean[] editable;
	private boolean contenEditable;

	public RecordTableModel(boolean contenEditable, Descriptor descriptor, boolean topTable) {
		this.contenEditable = contenEditable;
		this.descriptor = descriptor;
		this.topTable = topTable;
		editable = descriptor.getEditable(DerivedSource.ALL);
	}

	public void setRecordData(RecordData recordData) {
		if (isRecordPairSet()) {
			this.recordData.removeRepositoryChangeListener(this);
		}
		this.recordData = recordData;
		if (!isRecordPairSet()) {
			record = null;
			comparisonRecord = null;
		} else {
			recordData.addRepositoryChangeListener(this);
			if (topTable) {
				this.record = recordData.getFirstRecord();
				this.comparisonRecord = recordData.getSecondRecord();
			} else {
				this.record = recordData.getSecondRecord();
				this.comparisonRecord = recordData.getFirstRecord();
			}
		}
		fireTableDataChanged();
	}

	public boolean isRecordPairSet() {
		return recordData != null;
	}

	public boolean isStackable() {
		return descriptor.isStackable();
	}

	public int getRowCount() {
		return (record == null) ? 0 : descriptor.getRowCount(record);
	}

	public int getColumnCount() {
		return descriptor.getColumnCount();
	}

	public String getColumnName(int iCol) {
		throw new UnsupportedOperationException("Shouldn't be called.");
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		boolean isValid = descriptor.getValidity(record, rowIndex, columnIndex);
		boolean isUnique = true;

		String value = descriptor.getValueAsString(record, rowIndex, columnIndex);
		if (comparisonRecord != null) {
			if (value != null) {
				int numRows = descriptor.getRowCount(comparisonRecord);
				for (int i = 0; i < numRows; i++) {
					String otherValue = descriptor.getValueAsString(comparisonRecord, i, columnIndex);
					if (value.equals(otherValue)) {
						isUnique = false;
						break;
					}
				}
			}
		}
		return new TypedValue(value, isValid, isUnique, !editable[columnIndex]);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return contenEditable && editable[columnIndex];
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		descriptor.setValue(record, rowIndex, columnIndex, (String) aValue);
		this.fireTableCellUpdated(rowIndex, columnIndex);
		recordData.fireRecordDataChanged();
	}

	public void addRow(int iPos, boolean above) {
		descriptor.addRow(iPos, above, record);
		recordData.fireRecordDataChanged();
	}

	public void deleteRow(int iPos) {
		descriptor.deleteRow(record, iPos);
		recordData.fireRecordDataChanged();
	}
	/**
	 * @see com.choicemaker.cm.train.gui.listeners.RepositoryChangeListener#setChanged(com.choicemaker.cm.train.gui.listeners.RepositoryChangeEvent)
	 */
	public void setChanged(RepositoryChangeEvent evt) {
	}
	/**
	 * @see com.choicemaker.cm.train.gui.listeners.RepositoryChangeListener#recordDataChanged(com.choicemaker.cm.train.gui.listeners.RepositoryChangeEvent)
	 */
	public void recordDataChanged(RepositoryChangeEvent evt) {
		fireTableDataChanged();
	}
	/**
	 * @see com.choicemaker.cm.train.gui.listeners.RepositoryChangeListener#markupDataChanged(com.choicemaker.cm.train.gui.listeners.RepositoryChangeEvent)
	 */
	public void markupDataChanged(RepositoryChangeEvent evt) {
	}
}
