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
package com.choicemaker.cm.gui.utils.viewer.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

/**
 * This class holds on to a column from a RecordTable so that it can be removed and
 * inserted according to the users preference.
 * 
 * @author S. Yoakum-Stover
 */
public class ColumnKeeper implements ActionListener {
	private static Logger logger = Logger.getLogger(ColumnKeeper.class);
	private JTable table;
	private TableColumn col;
	private int colTableModelIndex;

	public ColumnKeeper(JTable table, TableColumn col, int index) {
		this.table = table;
		this.col = col;
		this.colTableModelIndex = index;
	}

	public void actionPerformed(ActionEvent e) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
		if (item.isSelected()) {
			table.addColumn(col);
		} else {
			table.removeColumn(col);
		}
		//        table.tableChanged(new TableModelEvent(table.getModel()));
		//        table.repaint();
	}

}
