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

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.modelmaker.gui.renderers.ColoredTableCellRenderer;

/**
 * The active clues for a particular MarkedRecordPair.  Displayed on the DefaultPairReviewPanel.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:35:52 $
 */
public class CluePerformanceTable extends JTable {
	private static final long serialVersionUID = 1L;
	private static final Color GREEN = new Color(195, 237, 196);
	private CluePerformanceTableModel myModel;

	public CluePerformanceTable() {
		super();
		init();
	}

	private void init() {
		myModel = new CluePerformanceTableModel();
		setAutoCreateColumnsFromModel(false);
		setModel(myModel);

		//setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ColumnDefinition[] colDefinitions = myModel.getColumnDefinitions();
		for (int k = 0; k < colDefinitions.length; k++) {
			//TableCellRenderers...............................
			Color[] rowColors = { Color.white, Color.white, GREEN, GREEN, GREEN };
			DefaultTableCellRenderer renderer = ColoredTableCellRenderer.getColoredRowsInstance(rowColors);
			renderer.setHorizontalAlignment(colDefinitions[k].getAlignment());

			//TableCellEditors.................................
			TableCellEditor editor = new DefaultCellEditor(new JTextField());

			//TableColumn......................................
			TableColumn column = new TableColumn(k, colDefinitions[k].getWidth(), renderer, editor);
			addColumn(column);
		}

		JTableHeader header = getTableHeader();
		header.setUpdateTableInRealTime(true);
		//header.addMouseListener(new TableColumnListener(this, myModel));

		//setPreferredScrollableViewportSize(new Dimension(500, 200));
	}
	public void reset() {
		myModel.wipeOutTable();
		refresh();
	}

	public void refresh(IProbabilityModel pm, Trainer t) {
		myModel.refresh(pm, t);
		refresh();
	}

	private void refresh() {
		//repaint the table
		tableChanged(new TableModelEvent(myModel));
		repaint();
	}

}
