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
package com.choicemaker.cm.modelmaker.gui.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.choicemaker.cm.modelmaker.gui.tables.SortableTableModel;

/**
 * This is the thing that listens for the mouse
 * clicks on the column headings in order to trigger the
 * sorting of rows.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:09 $
 */
public class TableColumnListener extends MouseAdapter {

	private JTable table;
	private SortableTableModel model;

	public TableColumnListener(JTable table, SortableTableModel model) {
		this.table = table;
		this.model = model;
	}

	/**
	 * Invoked when the mouse has been clicked on a component.
	 */
	public void mouseClicked(MouseEvent e) {
		TableColumnModel colModel = table.getColumnModel();
		int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
		int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

		if (model.isColumnSortable(modelIndex)){
			if ((modelIndex < 0) /*|| (modelIndex > 3) What was I doing this for?*/
				) {
				return;
			}
	
			if (model.getSortedColumnIndex() == modelIndex) {
				model.reverseSortOrder();
			} else {
				model.setSortedColumnIndex(modelIndex);
			}
	
			for (int i = 0; i < model.getColumnCount(); i++) {
				TableColumn tCol = colModel.getColumn(i);
				tCol.setHeaderValue(model.getColumnName(tCol.getModelIndex()));
			}
	
			table.getTableHeader().repaint();
	
			model.sort();
			table.tableChanged(new TableModelEvent(model));
			table.repaint();
		}
	}

}
