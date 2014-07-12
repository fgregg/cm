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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.BooleanActiveClues;
import com.choicemaker.cm.core.base.IntActiveClues;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactories;
import com.choicemaker.cm.modelmaker.gui.utils.ClueDataComparator;
import com.choicemaker.cm.modelmaker.gui.utils.NullInteger;

/**
 * Table model for the ActiveClueTable.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:20:03 $
 */
public class ActiveClueTableModel extends SortableTableModel {
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(ActiveClueTableModel.class);

	private IProbabilityModel pModel;
	private ActiveClues activeClues;
	private MutableMarkedRecordPair record;
	private Vector rows;
	private boolean booleanType;
	private ColumnDefinition[] preColumns;
	private String[] columnNames;
	private TableColumn[] columns;
	private ActiveClueTableModelPlugin plugin;
	private int numPluginColumns;
		
	static final private ColumnDefinition[] NB_PRE_COLUMNS =
		{
			new ColumnDefinition(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.id"), 80, JLabel.RIGHT),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.cluename"),
				580,
				JLabel.LEFT),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.decision"),
				150,
				JLabel.CENTER),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.type"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.report"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.active.value"),
				150,
				JLabel.RIGHT)
			};
	static final private ColumnDefinition[] BOOLEAN_PRE_COLUMNS = new ColumnDefinition[NB_PRE_COLUMNS.length -1];
	static {
		System.arraycopy(NB_PRE_COLUMNS, 0, BOOLEAN_PRE_COLUMNS, 0, BOOLEAN_PRE_COLUMNS.length);
	}

	public static final int COL_ID = 0;
	public static final int COL_NAME = 1;
	public static final int COL_WEIGHT = 2;
	public static final int COL_DECISION = 3;
	public static final int COL_TYPE = 4;
	public static final int COL_REPORT = 5;

	ActiveClueTableModel(IProbabilityModel pModel) {
		this.pModel = pModel;
		init(pModel);
	}

	private void init(IProbabilityModel model) {
		ClueSet clueSet = model.getClueSet();
		booleanType = clueSet != null && clueSet.getType() == ClueSetType.BOOLEAN;
		List cols = new ArrayList();
		List colNames = new ArrayList();
		preColumns = booleanType ? BOOLEAN_PRE_COLUMNS : NB_PRE_COLUMNS;
		for (int i = 0; i < preColumns.length; i++) {
			cols.add(getTextColumn(preColumns[i], i));
			colNames.add(preColumns[i].getName());
		}
		
		MachineLearner ml = model.getMachineLearner();
		if (ml != null && !(ml instanceof com.choicemaker.cm.core.base.DoNothingMachineLearning)) {
			plugin = MlGuiFactories.getGui(ml).getActiveClueTableModelPlugin();
			plugin.setModel(model);
			plugin.setStartColumn(preColumns.length);
			numPluginColumns = plugin.getColumnCount();
		} else {
			numPluginColumns = 0;
		}
		
		for (int i = 0; i < numPluginColumns; i++) {
			cols.add(plugin.getColumn(i));
			colNames.add(plugin.getColumnName(i));
		}
		columns = (TableColumn[]) cols.toArray(new TableColumn[cols.size()]);
		columnNames = (String[]) colNames.toArray(new String[colNames.size()]);
	}
	
	private TableColumn getTextColumn(ColumnDefinition cd, int columnNumber) {
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(cd.getAlignment());

		//TableCellEditors.................................
		TableCellEditor editor = new DefaultCellEditor(new JTextField());
		//TableColumn......................................
		return new TableColumn(columnNumber, cd.getWidth(), renderer, editor);
	}
	
	TableColumn[] getColumns() {
		return columns;
	}
		
	public void sort() {
		Collections.sort(rows, new ClueDataComparator(sortCol, sortAsc));
	}

	public void setMarkedRecordPair(MutableMarkedRecordPair record) {
		if (plugin != null) {
			plugin.setMarkedRecordPair(record);
		}
		ClueDesc[] descriptions = pModel.getClueSet().getClueDesc();
		sortCol = 0;
		sortAsc = true;
		this.record = record;
		rows = new Vector(10);
		activeClues = record.getActiveClues();
		if (activeClues instanceof BooleanActiveClues) {
			int[] firings;
			firings = ((BooleanActiveClues) activeClues).getCluesAndRules();
			for (int i = 0; i < firings.length; ++i) {
				int clueNum = firings[i];
				ClueDesc desc = descriptions[clueNum];
				Object[] c = getCommonRowData(desc);
				addPluginData(c, clueNum);
				rows.add(new ClTableRow(desc, c));
			}
		} else if(activeClues instanceof IntActiveClues) {
			IntActiveClues iac = (IntActiveClues)activeClues;
			int[] values = iac.values;
			boolean[] cluesToEvaluate = pModel.getCluesToEvaluate();
			for(int i = 0; i < values.length; ++i) {
				ClueDesc desc = descriptions[i];
				if(cluesToEvaluate[i] && !desc.rule) {
					Object[] c = getCommonRowData(desc);
					c[5] = new NullInteger(values[i]);
					addPluginData(c, i);					
					rows.add(new ClTableRow(desc, c));
				}
			}
			addRuleRows(descriptions, activeClues, NullInteger.getNullInstance());
		}
		sort();
	}
	
	private Object[] getCommonRowData(ClueDesc desc) {
		Object[] c = new Object[getColumnCount()];
		c[0] = new NullInteger(desc.getNumber(), "  ");
		c[1] = desc.getName();
		c[2] = desc.getDecision().toString();
		c[3] = ClTableRow.getType(desc);
		c[4] = ClTableRow.getModifier(desc);
		return c;
	}		
	
	private void addPluginData(Object[] c, int row) {
		for (int j = 0; j < numPluginColumns; j++) {
			c[preColumns.length + j] = plugin.getValueAt(row, j);
		}
	}
	
	private void addRuleRows(ClueDesc[] descriptions, ActiveClues activeClues, Object trueValue) {
		int[] activeRules = activeClues.getRules();
		for (int i = 0; i < activeRules.length; i++) {
			int clueNum = activeRules[i];
			ClueDesc desc = descriptions[clueNum];
			Object[] c = getCommonRowData(desc);
			c[5] = trueValue;
			addPluginData(c, clueNum);
			rows.add(new ClTableRow(desc, c));
		}			
	}

	public void reset() {
		if (plugin != null) {
			plugin.setMarkedRecordPair(null);
		}
		record = null;
	}

	public String getTitle() {
		return ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.active.label");
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
		return (record == null) ? 0 : rows.size();
	}

	public String getColumnName(int col) {
		String str = columnNames[col];
		if (col == sortCol) {
			str += sortAsc ? " >>" : " <<";
		}
		return str;
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
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (record == null || rowIndex < 0 || rowIndex >= getRowCount()) {
			return "";
		} else {
			return ((ClTableRow) rows.elementAt(rowIndex)).getColumn(columnIndex);
		}
	}
}
