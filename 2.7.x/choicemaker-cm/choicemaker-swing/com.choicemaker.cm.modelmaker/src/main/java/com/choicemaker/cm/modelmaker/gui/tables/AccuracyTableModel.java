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

import java.text.DecimalFormat;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;

public class AccuracyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private boolean isEmpty;
	private float[][] data;
	private float[] firstColumn;
	private TableModelEvent tme;
	private String[] columnNames;
	private static final String ACCURACY =
		ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.accuracy");
	private static final String HUMAN_REVIEW =
		ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.humanreview");
	private static final String DIFFER_THRESHOLD =
		ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.differthreshold");
	private static final String MATCH_THRESHOLD =
		ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.panel.holdvsacc.table.matchthreshold");
	private static final DecimalFormat DF2 = new DecimalFormat("##0.00");

	public AccuracyTableModel(boolean firstColAcc, float[] firstColumn) {
		this.firstColumn = firstColumn;
		isEmpty = true;
		if (firstColAcc) {
			columnNames = new String[] { ACCURACY, HUMAN_REVIEW, DIFFER_THRESHOLD, MATCH_THRESHOLD };
		} else {
			columnNames = new String[] { HUMAN_REVIEW, ACCURACY, DIFFER_THRESHOLD, MATCH_THRESHOLD };
		}
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void refresh(float[][] data) {
		this.data = data;
		isEmpty = false;
		fireChange();
	}

	public void reset() {
		isEmpty = true;
		fireChange();
	}

	private void fireChange() {
		if (tme == null) {
			tme = new TableModelEvent(this);
		}
		fireTableChanged(tme);
	}

	public String getTitle() {
		return ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.label");
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return firstColumn.length;
	}

	public boolean isCellEditable(int nRow, int nCol) {
		return false;
	}

	public Object getValueAt(int iRow, int iCol) {
		if (isEmpty || iCol == 0) {
			if (iCol == 0) {
				return DF2.format(firstColumn[iRow] * 100) + " %";
			} else {
				return "";
			}
		} else {
			float val = data[iRow][iCol];
			if (Float.isNaN(val)) {
				return "---";
			} else {
				return DF2.format(val * 100) + " %";
			}
		}
	}
}
