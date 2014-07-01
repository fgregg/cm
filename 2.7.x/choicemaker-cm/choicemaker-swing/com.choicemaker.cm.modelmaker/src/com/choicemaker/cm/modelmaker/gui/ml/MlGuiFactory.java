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
package com.choicemaker.cm.modelmaker.gui.ml;

import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.DynamicDispatchHandler;
import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;
import com.choicemaker.cm.modelmaker.gui.tables.ActiveClueTableModelPlugin;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTableModelPlugin;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:52:27 $
 */
public abstract class MlGuiFactory implements DynamicDispatchHandler {
	private static ClueTableModelPlugin clueTableModelPlugin;
	private static ActiveClueTableModelPlugin activeClueTableModelPlugin;

	/**
	 * Returns the TrainDialogPlugin to show as part of AbstractApplication's Train dialog.
	 * This is used to specify parameters specific to this machine learning technique.
	 *
	 * @return   The TrainDialogPlugin to show as part of AbstractApplication's Train dialog.
	 */
	public abstract TrainDialogPlugin getTrainDialogPlugin(MachineLearner learner);

	public synchronized ClueTableModelPlugin getClueTableModelPlugin() {
		if(clueTableModelPlugin == null) {
			clueTableModelPlugin = new ClueTableModelPlugin() {
				private static final long serialVersionUID = 1L;
				public TableColumn getColumn(int column) {
					throw new UnsupportedOperationException();
				}
				public int getRowCount() {
					throw new UnsupportedOperationException();
				}
				public int getColumnCount() {
					return 0;
				}
				public Object getValueAt(int rowIndex, int columnIndex) {
					throw new UnsupportedOperationException();
				}
			};
		}
		return clueTableModelPlugin;
	}
	
	public synchronized ActiveClueTableModelPlugin getActiveClueTableModelPlugin() {
		if(activeClueTableModelPlugin == null) {
			activeClueTableModelPlugin = new ActiveClueTableModelPlugin() {
				private static final long serialVersionUID = 1L;
				public TableColumn getColumn(int column) {
					throw new UnsupportedOperationException();
				}
				public int getRowCount() {
					throw new UnsupportedOperationException();
				}
				public int getColumnCount() {
					return 0;
				}
				public Object getValueAt(int rowIndex, int columnIndex) {
					throw new UnsupportedOperationException();
				}				
			};
		}
		return activeClueTableModelPlugin;
	}
	
	public abstract MachineLearner getMlInstance();
}
