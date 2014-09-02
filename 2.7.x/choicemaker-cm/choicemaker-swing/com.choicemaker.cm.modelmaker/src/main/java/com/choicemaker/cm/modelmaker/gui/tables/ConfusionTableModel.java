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

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.utils.ValueError;

/**
 * The TableModel for the ConfusionTable. 
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class ConfusionTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;


	private int[][] statMatrix;
	private boolean isEmpty;

	static final public ColumnDefinition columns[] =
		{
			new ColumnDefinition(" ", 200, JLabel.LEFT),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.cm.differ"),
				200,
				JLabel.CENTER),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.cm.match"),
				200,
				JLabel.CENTER),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.cm.hold"),
				200,
				JLabel.CENTER),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.total"),
				200,
				JLabel.CENTER),
			};

	public static final int COL_ROW_LABEL = 0;
	public static final int COL_DIFFER = 1;
	public static final int COL_MATCH = 2;
	public static final int COL_HOLD = 3;
	public static final int COL_TOTAL = 4;

	public ConfusionTableModel() {
		isEmpty = true;
	}

	public ColumnDefinition[] getColumnDefinitions() {
		return columns;
	}

	public void refresh(int[][] data) {
		statMatrix = data;
		isEmpty = false;
	}

	public void reset() {
		isEmpty = true;
	}

	public String getTitle() {
		return ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.label");
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
		return 5;
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
		return 4;
	}

	public String getColumnName(int col) {
		return columns[col].getName();
	}

	public boolean isCellEditable(int nRow, int nCol) {
		return false;
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
	public Object getValueAt(int iRow, int iCol) {
		Object thing = null;

		if (iCol == COL_ROW_LABEL) {
			switch (iRow) {
				case 0 :
					thing = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.human.differ");
					break;
				case 1 :
					thing = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.human.match");
					break;
				case 2 :
					thing = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.human.hold");
					break;
				case 3 :
					thing = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.confusion.total");
					break;
			}
		} else if (isEmpty || (iRow < 0) || (iRow >= getRowCount())) {
			return "";
		} else {
			thing = String.valueOf(statMatrix[iRow][iCol - 1]);
			if (iRow < 3 && iCol < 4 && iRow != iCol - 1) {
				thing = new ValueError((String) thing);
			}
		}
		return thing;
	}
}
