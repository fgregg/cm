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

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.ClueNameCellListener;
import com.choicemaker.cm.modelmaker.gui.listeners.ClueTableCellListener;
import com.choicemaker.cm.modelmaker.gui.listeners.TableColumnListener;

/**
 * The clueTable in the AbstractModelReviewPanel.  
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:37:08 $
 */
public class ClueTable extends JTable {
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(ClueTable.class);
	private ClueTableModel myModel;
	private ModelMaker modelMaker;
//	private TableColumn[] columns;

	public ClueTable(ModelMaker modelMaker) {
		super();
		this.modelMaker = modelMaker;
		init();
	}

	private void init() {
		myModel = new ClueTableModel(modelMaker.getProbabilityModel(), modelMaker.getTrainer());
		setAutoCreateColumnsFromModel(false);
		setModel(myModel);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableColumn[] columns = myModel.getColumns();
		for (int i = 0; i < columns.length; i++) {
			addColumn(columns[i]);
		}

		JTableHeader header = getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.addMouseListener(new TableColumnListener(this, myModel));

		addMouseListener(new ClueTableCellListener(modelMaker, this, myModel.getNumPluginColumns()));
		addMouseListener(new ClueNameCellListener(modelMaker, this));

		//setPreferredScrollableViewportSize(new Dimension(500, 800));
		//setPreferredSize(new Dimension(500, 800));
	}

//	private void refresh() {
//		//refresh the column headings
//		TableColumnModel colModel = getColumnModel();
//		for (int i = 0; i < myModel.getColumnCount(); i++) {
//			TableColumn tCol = colModel.getColumn(i);
//			tCol.setHeaderValue(myModel.getColumnName(tCol.getModelIndex()));
//		}
//		getTableHeader().repaint();
//
//		//repaint the table
//		tableChanged(new TableModelEvent(myModel));
//		repaint();
//	}	

}
