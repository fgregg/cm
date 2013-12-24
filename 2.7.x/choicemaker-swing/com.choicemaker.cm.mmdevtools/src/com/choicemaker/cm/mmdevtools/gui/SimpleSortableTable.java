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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.choicemaker.cm.modelmaker.gui.tables.SortableTableModel;

/**
 * @author ajwinkel
 *
 */
public class SimpleSortableTable extends JTable {

	private MouseListener headerListener = new HeaderListener();

	public SimpleSortableTable() {
		super(0, 2);
		setTableHeader(new JTableHeader(getColumnModel()));
	}

	public SimpleSortableTable(Object[][] data, Object[] headers) {
		super(new SimpleSortableTableModel(data, headers));	
		setTableHeader(new JTableHeader(getColumnModel()));
	}

	public SimpleSortableTable(TableModel model) {
		super(model);
		setTableHeader(new JTableHeader(getColumnModel()));
	}

	public void setTableHeader(JTableHeader header) {
		JTableHeader old = getTableHeader();
		if (old != null) {
			old.removeMouseListener(headerListener);
		}
		
		super.setTableHeader(header);
		
		if (header != null) {
			header.setUpdateTableInRealTime(true);
			header.addMouseListener(headerListener);
		}
	}

	private void refreshColumnHeaders() {
		TableModel model = getModel();
		if (model == null) {
			return;
		}

		TableColumnModel colModel = getColumnModel();
		
		for (int i = 0; i < model.getColumnCount(); i++) {
			TableColumn tCol = colModel.getColumn(i);
			tCol.setHeaderValue(model.getColumnName(tCol.getModelIndex()));
		}
	
		getTableHeader().repaint();
	}

	private class HeaderListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!(getModel() instanceof SortableTableModel)) {
				return;
			}

			TableColumnModel colModel = getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
			if (modelIndex < 0) {
				return;
			}
			
			SortableTableModel model = (SortableTableModel) getModel();
			if (model.isColumnSortable(modelIndex)) {	
				if (model.getSortedColumnIndex() == modelIndex) {
					model.reverseSortOrder();
				} else {
					model.setSortedColumnIndex(modelIndex);
				}
				
				model.sort();

				refreshColumnHeaders();
			}
		}

	}

}
