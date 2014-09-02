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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.gui.utils.viewer.HeaderRenderer;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.panels.TestingControlPanel;
import com.choicemaker.cm.modelmaker.gui.renderers.ColorTableErrorCellRenderer;

/**
 * The TableModel for the confusion matrix in the DefaultTestingControlPanel.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:42:04 $
 */
public class ConfusionTable extends JTable {

	private static final long serialVersionUID = 1L;
	private TestingControlPanel parent;
	private ConfusionTableModel myModel;

	public ConfusionTable(TestingControlPanel parent, ConfusionTableModel tableModel) {
		super();
		this.parent = parent;
		myModel = tableModel;
		init();
	}

	private void init() {

		setAutoCreateColumnsFromModel(false);
		setModel(myModel);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellSelectionEnabled(true);

		ColumnDefinition[] colDefinitions = myModel.getColumnDefinitions();
		for (int k = 0; k < colDefinitions.length; k++) {
			//TableCellRenderers...............................
			DefaultTableCellRenderer renderer;
			if (k == 0) {
				renderer = new HeaderRenderer(false);
			} else {
				renderer = new ColorTableErrorCellRenderer();
				renderer.setHorizontalAlignment(colDefinitions[k].getAlignment());
			}

			//TableCellEditors.................................
			TableCellEditor editor = new DefaultCellEditor(new JTextField());

			//TableColumn......................................
			TableColumn column = new TableColumn(k, colDefinitions[k].getWidth(), renderer, editor);
			addColumn(column);
		}

		getTableHeader().setUpdateTableInRealTime(false);

		addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				Point origin = e.getPoint();
				int row = rowAtPoint(origin);
				int col = columnAtPoint(origin);
				if ((row < 0) || (col < 1) || (row > 3) || (col > 4)) {
					return;
				}
				ListeningMarkedRecordPairFilter filter = parent.getModelMaker().getFilter();
				filter.reset();
				if (row < 3) {
					boolean[] b = new boolean[Decision.NUM_DECISIONS];
					b[row] = true;
					filter.setHumanDecision(b);
				}
				if (col < 4) {
					boolean[] b = new boolean[Decision.NUM_DECISIONS];
					b[col - 1] = true;
					filter.setChoiceMakerDecision(b);
				}
				parent.getModelMaker().filterMarkedRecordPairList();
			}
		});

	}

	public void reset() {
		myModel.reset();
		tableChanged(new TableModelEvent(myModel));
		repaint();
	}

	public void refresh() {
		myModel.refresh(parent.getModelMaker().getStatistics().getConfusionMatrix());
		tableChanged(new TableModelEvent(myModel));
		repaint();
	}

}
