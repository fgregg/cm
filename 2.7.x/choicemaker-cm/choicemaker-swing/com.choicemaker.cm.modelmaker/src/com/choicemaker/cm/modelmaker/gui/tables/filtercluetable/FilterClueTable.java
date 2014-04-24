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

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.choicemaker.cm.analyzer.filter.FilterCondition;
import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.ClueNameCellListener;
import com.choicemaker.cm.modelmaker.gui.listeners.TableColumnListener;

/**
 * The clueTable in the AbstractModelReviewPanel.
 *
 * @author S. Yoakum-Stover
 * @author Arturo Falck
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:53:36 $
 */
public class FilterClueTable extends JTable{

	private static Logger logger = Logger.getLogger(FilterClueTable.class);
	private FilterClueTableModel myModel;
	private ModelMaker meTrainer;

	private static final int NONE = Integer.MIN_VALUE;
	private int colConditionChanged = NONE;

	public FilterClueTable(ModelMaker met) {
		super();
		meTrainer = met;
		init();
	}

	private void init() {
		myModel = new FilterClueTableModel();
		myModel.addTableModelListener(this);
		setAutoCreateColumnsFromModel(false);
		setModel(myModel);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ColumnDefinition[] colDefinitions = myModel.getColumnDefinitions();
		for (int k = 0; k < colDefinitions.length; k++) {
			//TableCellRenderers...............................
			TableCellRenderer renderer;
			if (k == FilterClueTableModel.COL_PARAMETERS) {
				renderer = createParameterRenderer();
			} else {
				DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
					public Component getTableCellRendererComponent(
						JTable table,
						Object value,
						boolean selected,
						boolean focused,
						int row,
						int column) {
						setEnabled(table == null || table.isEnabled()); // see question above
						// 			    if(((FieldSelectorTableModel)table.getModel()).isReport(row)) {
						// 				setForeground(Color.blue);
						// 			    } else {
						// 				setForeground(Color.black);
						// 			    }
						super.getTableCellRendererComponent(table, value, selected, focused, row, column);
						return this;
					}
				};
				textRenderer.setHorizontalAlignment(colDefinitions[k].getAlignment());
				renderer = textRenderer;
			}

			//TableCellEditors.................................
			TableCellEditor editor;
			if (k == FilterClueTableModel.COL_CONDITION) {
				editor = createConditionEditor();
			} else if (k == FilterClueTableModel.COL_PARAMETERS) {
				editor = createParameterEditor();
			} else {
				editor = new DefaultCellEditor(new JTextField());
			}

			//TableColumn......................................
			TableColumn column = new TableColumn(k, colDefinitions[k].getWidth(), renderer, editor);
			addColumn(column);
		}

		JTableHeader header = getTableHeader();
		header.setUpdateTableInRealTime(true);

		header.addMouseListener(new TableColumnListener(this, myModel));
		addMouseListener(new ClueNameCellListener(meTrainer, this));

	}

	protected TableCellEditor createConditionEditor(){
		return new FilterConditionCellEditor();
	}

	protected TableCellEditor createParameterEditor(){
		return new IntFilterParamsCellEditor();
	}

	protected TableCellRenderer createParameterRenderer(){
		return new IntFilterParamsCellRenderer();
	}

	/**
	 * Asks the myModel to rebuild its rows, then refresh itself.
	 */
	public void setClues(IProbabilityModel pm) {
		myModel.refresh(pm);
		refresh();
	}

	private void refresh() {
		//refresh the column headings
		TableColumnModel colModel = getColumnModel();
		for (int i = 0; i < myModel.getColumnCount(); i++) {
			TableColumn tCol = colModel.getColumn(i);
			tCol.setHeaderValue(myModel.getColumnName(tCol.getModelIndex()));
		}
		getTableHeader().repaint();

		//repaint the table
		tableChanged(new TableModelEvent(myModel));
		repaint();
	}

	public void deselectAll() {
		myModel.deselectAll();
		refresh();
	}

	public void select(FilterCondition[] filterCondition) {
		myModel.select(filterCondition);
	}

	public FilterCondition[] getFilterConditions(){
		return myModel.getFilterConditions();
	}

	/**
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 *
	 * Overridden to provide automatic editing of the COL_PARAMETERS functionality when a COL_CONDITION is edited.
	 */
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);

		if (e.getColumn() == FilterClueTableModel.COL_CONDITION
			&& !(FilterCondition.NULL_FILTER_CONDITION.equals(getValueAt(e.getFirstRow(), e.getColumn())))) {
				colConditionChanged = e.getFirstRow();
		}
	}

	/**
	 * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
	 */
	public void editingStopped(ChangeEvent e) {
		super.editingStopped(e);

		if (colConditionChanged != NONE){
			editCellAt(colConditionChanged, getColumnModel().getColumnIndex(getModel().getColumnName(FilterClueTableModel.COL_PARAMETERS)));
			colConditionChanged = NONE;
		}
		repaint();
	}

}
