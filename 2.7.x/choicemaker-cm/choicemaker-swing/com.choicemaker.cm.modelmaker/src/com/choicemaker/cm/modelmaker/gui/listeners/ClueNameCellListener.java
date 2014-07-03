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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.NullInteger;

/**
 * This is the thing that listens for the mouse
 * clicks on a table cell.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
public class ClueNameCellListener extends MouseAdapter {

//	private static Logger logger = Logger.getLogger(ClueNameCellListener.class);
	private JTable table;
	private ModelMaker meTrainer;

	public ClueNameCellListener(ModelMaker met, JTable table) {
		meTrainer = met;
		this.table = table;
	}

	/**
	 * Invoked when the mouse has been clicked on a component.
	 */
	public void mousePressed(MouseEvent e) {
		Point origin = e.getPoint();
		int row = table.rowAtPoint(origin);
		int col = table.columnAtPoint(origin);
		int modelIndex = table.getColumnModel().getColumn(col).getModelIndex();
		if (modelIndex == 0 || modelIndex == 1) {
			int clueId = ((NullInteger) table.getValueAt(row, 0)).value();
			meTrainer.postClueText(clueId);
		}

	}

}
