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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactories;
import com.choicemaker.cm.modelmaker.gui.renderers.CheckCellRenderer;
import com.choicemaker.cm.modelmaker.gui.utils.ClueDataComparator;
import com.choicemaker.cm.modelmaker.gui.utils.NullFloat;
import com.choicemaker.cm.modelmaker.gui.utils.NullInteger;

/**
 * The TableModel for the ClueTable in the AbstractModelReviewPanel.  
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:41:27 $
 */
public class ClueTableModel extends SortableTableModel {

	private static Logger logger = Logger.getLogger(ClueTableModel.class);

	private ClueDesc[] descriptions;
	private boolean[] enabled;
	private int numClues;
	private int[] totalFirings;
	private int[] correctFirings;
	private int[] incorrectFirings;
	private double[] percentFirings;
	private boolean haveCountsData;
	private String[] columnNames;
	private TableColumn[] columns;
	private boolean booleanDecision;
	private ClueTableModelPlugin plugin;
	private int numPluginColumns;

	private Vector rows;

	public static final ColumnDefinition[] PRE_COLUMNS =
		{
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.id"),
				100,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.cluename"),
				550,
				JLabel.LEFT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.enabled"),
				150,
				JLabel.CENTER),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.decision"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.type"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.common.report"),
				150,
				JLabel.RIGHT)};

	private static final ColumnDefinition[] BD_POST_COLUMNS =
		{
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.clue.fires"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.clue.hits"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.clue.misses"),
				150,
				JLabel.RIGHT),
			new ColumnDefinition(
				MessageUtil.m.formatMessage("train.gui.modelmaker.table.clue.hit.percentage"),
				150,
				JLabel.RIGHT)};

	private static final ColumnDefinition[] NBD_POST_COLUMNS = new ColumnDefinition[1];
	static {
		System.arraycopy(BD_POST_COLUMNS, 0, NBD_POST_COLUMNS, 0, NBD_POST_COLUMNS.length);
	};

	public static final int COL_ID = 0;
	public static final int COL_NAME = 1;
	public static final int COL_ENABLED = 2;
	public static final int COL_TOTAL_FIRES = 6;
	public static final int COL_CORRECT_FIRES = 7;
	public static final int COL_MISFIRES = 8;

	public ClueTableModel(IProbabilityModel model, Trainer trainer) {
		numClues = 0;
		haveCountsData = false;
		rows = new Vector();
		init(model);
		refresh(model, trainer);
	}

	private void init(IProbabilityModel model) {
		ClueSet clueSet = model.getClueSet();
		booleanDecision = clueSet != null && clueSet.getType() == ClueSetType.BOOLEAN && clueSet.hasDecision();
		List cols = new ArrayList();
		List colNames = new ArrayList();
		for (int i = 0; i < PRE_COLUMNS.length; i++) {
			if (i == COL_ENABLED) {
				cols.add(getCheckboxColumn(PRE_COLUMNS[i], i));
			} else {
				cols.add(getTextColumn(PRE_COLUMNS[i], i));
			}
			colNames.add(PRE_COLUMNS[i].getName());
		}
		
		MachineLearner ml = model.getMachineLearner();
		if (ml != null && !(ml instanceof com.choicemaker.cm.core.ml.none.None)) {
			plugin = MlGuiFactories.getGui(ml).getClueTableModelPlugin();
			plugin.setModel(model);
			plugin.setStartColumn(PRE_COLUMNS.length);
			numPluginColumns = plugin.getColumnCount();
		} else {
			numPluginColumns = 0;
		}
		
		for (int i = 0; i < numPluginColumns; i++) {
			cols.add(plugin.getColumn(i));
			colNames.add(plugin.getColumnName(i));
		}
		ColumnDefinition[] cds = booleanDecision ? BD_POST_COLUMNS : NBD_POST_COLUMNS;
		for (int i = 0; i < cds.length; i++) {
			cols.add(getTextColumn(cds[i], PRE_COLUMNS.length + numPluginColumns + i));
			colNames.add(cds[i].getName());
		}
		columns = (TableColumn[]) cols.toArray(new TableColumn[cols.size()]);
		setRawColumnNames((String[]) colNames.toArray(new String[colNames.size()]));
	}

	TableColumn[] getColumns() {
		return columns;
	}

	private TableColumn getTextColumn(ColumnDefinition cd, int columnNumber) {
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(cd.getAlignment());

		//TableCellEditors.................................
		TableCellEditor editor = new DefaultCellEditor(new JTextField());
		//TableColumn......................................
		return new TableColumn(columnNumber, cd.getWidth(), renderer, editor);
	}

	private TableColumn getCheckboxColumn(ColumnDefinition cd, int columnNumber) {
		TableCellRenderer renderer = new CheckCellRenderer();

		//TableCellEditors.................................
		TableCellEditor editor = new DefaultCellEditor(new JCheckBox());

		//TableColumn......................................
		return new TableColumn(columnNumber, cd.getWidth(), renderer, editor);
	}

	public void sort() {
		//logger.debug("Sorting called. sortCol = " + sortCol + " sort order = " + sortAsc);
		Collections.sort(rows, new ClueDataComparator(sortCol, sortAsc));
	}
	
	public void exportToFile(File f) throws IOException {
		// Fail fast
		if (f == null /* || !f.canWrite() */ ) {
			throw new IllegalArgumentException("null file or non-writeable file");
		}
		
		FileWriter w = new FileWriter(f);
		PrintWriter pw = new PrintWriter(w);
		exportColumnHeadings(pw);
		exportRows(pw);
		pw.flush();
	}
	
	public static final String COLUMN_SEPARATOR = "|";
	
	private void exportColumnHeadings(PrintWriter pw) {
		final int LAST_COLUMN_IDX = getColumnCount() - 1;
		for (int idxCol=0; idxCol<LAST_COLUMN_IDX; idxCol++) {
			String columnName = getRawColumnName(idxCol);
			columnName = columnName.trim();
			pw.print(columnName);
			pw.print(COLUMN_SEPARATOR);
		}
		String columnName = getRawColumnName(LAST_COLUMN_IDX);
		columnName = columnName.trim();
		pw.println(columnName);
	}
	
	private void exportRows(PrintWriter pw) {
		final int LAST_COLUMN_IDX = getColumnCount() - 1;
		for (int idxRow=0; idxRow<getRowCount(); idxRow++) {
			for (int idxCol=0; idxCol<LAST_COLUMN_IDX; idxCol++) {
				String value = this.getValueAt(idxRow,idxCol).toString();
				value = value.trim();
				pw.print(value);
				pw.print(COLUMN_SEPARATOR);
			}
			String value = this.getValueAt(idxRow,LAST_COLUMN_IDX).toString();
			value = value.trim();
			pw.println(value);
		}
	}
	
	private void refresh(ImmutableProbabilityModel pModel, Trainer trainer) {
		descriptions = pModel.getClueSet().getClueDesc();
		numClues = descriptions.length;
		enabled = pModel.getCluesToEvaluate();
		if (trainer == null) {
			haveCountsData = false;
		} else {
			totalFirings = trainer.getFirings();
			correctFirings = trainer.getCorrectFirings();
			incorrectFirings = trainer.getIncorrectFirings();
			percentFirings = trainer.getFiringPercentages();
			haveCountsData = true;
		}
		sortCol = 0;
		sortAsc = true;
		buildRows();
	}

//	public void refreshStatistics(Trainer trainer) {
//		if (trainer == null) {
//			isEmpty = false;
//			haveCountsData = false;
//			sortCol = 0;
//			sortAsc = true;
//		} else {
//			totalFirings = trainer.getFirings();
//			correctFirings = trainer.getCorrectFirings();
//			incorrectFirings = trainer.getIncorrectFirings();
//			percentFirings = trainer.getFiringPercentages();
//			haveCountsData = true;
//			sortCol = 0;
//			sortAsc = true;
//		}
//		buildRows();
//	}

	public void buildRows() {
		rows.clear();
		int numColumns = getColumnCount();
		for (int i = 0; i < numClues; i++) {
			Object[] c = new Object[numColumns];
			ClueDesc desc = descriptions[i];
			c[0] = new NullInteger(desc.getNumber(), "  ");
			c[1] = desc.getName();
			c[2] = new Boolean(enabled[i]);
			c[3] = desc.getDecision().toString();
			c[4] = ClTableRow.getType(desc);
			c[5] = ClTableRow.getModifier(desc);
			for (int j = 0; j < numPluginColumns; j++) {
				c[PRE_COLUMNS.length + j] = plugin.getValueAt(i, j);
			}
			c[6 + numPluginColumns] =
				haveCountsData
					&& (booleanDecision || desc.rule)
						? new NullInteger(totalFirings[i], "  ")
						: NullInteger.getNullInstance();
			if (booleanDecision) {
				boolean n = !desc.rule && haveCountsData;
				c[7 + numPluginColumns] = n ? new NullInteger(correctFirings[i], "  ") : NullInteger.getNullInstance();
				c[8 + numPluginColumns] =
					n ? new NullInteger(incorrectFirings[i], "  ") : NullInteger.getNullInstance();
				c[9 + numPluginColumns] =
					n ? new NullFloat((float) (100 * percentFirings[i]), "  ") : NullFloat.getNullInstance();
			}
			rows.add(new ClTableRow(desc, c));
		}
	}

	public String getTitle() {
		return MessageUtil.m.formatMessage("train.gui.modelmaker.table.clue.label");
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

	private void setRawColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	private String[] getRawColumnNames() {
		return columnNames;
	}

	public String getRawColumnName(int col) {
		String str = getRawColumnNames()[col];
		return str;
	}

	public String getColumnName(int col) {
		String str = getRawColumnNames()[col];
		if (col == sortCol) {
			str += sortAsc ? " >>" : " <<";
		}
		return str;
	}

	public boolean isCellEditable(int nRow, int nCol) {
		if (nCol == COL_ENABLED) {
			return true;
		} else if (nCol >= PRE_COLUMNS.length && nCol < PRE_COLUMNS.length + numPluginColumns) {
			ClTableRow aRow = (ClTableRow) rows.elementAt(nRow);
			int clueId = ((NullInteger) aRow.getColumn(COL_ID)).value();
			return plugin.isCellEditable(clueId, nCol - PRE_COLUMNS.length);
		} else {
			return false;
		}
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
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			return "";
		} else {
			return ((ClTableRow) rows.elementAt(rowIndex)).getColumn(columnIndex);
		}
	}

	/**
	 * This clever little method in addition to updating the 
	 * values displayed in the table also updates the underlying
	 * arrays that are the source of this data.
	 * 
	 * @param value
	 * @param nRow
	 * @param nCol
	 */
	public void setValueAt(Object value, int nRow, int nCol) {
		ClTableRow aRow = (ClTableRow) rows.elementAt(nRow);
		int clueId = ((NullInteger) aRow.getColumn(COL_ID)).value();
		if (nCol == COL_ENABLED) {
			Boolean b = (Boolean) value;
			aRow.set(COL_ENABLED, b);
			enabled[clueId] = b.booleanValue();
		} else if (nCol >= PRE_COLUMNS.length && nCol < PRE_COLUMNS.length + numPluginColumns) {
			plugin.setValueAt(value, clueId, nCol - PRE_COLUMNS.length);
			aRow.set(nCol, plugin.getValueAt(clueId, nCol - PRE_COLUMNS.length));
		}
	}

	public int getNumPluginColumns() {
		return numPluginColumns;
	}
}
