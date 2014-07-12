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
package com.choicemaker.cm.ml.me.gui;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.choicemaker.cm.core.MachineLearner;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.ml.me.base.MaximumEntropy;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactory;
import com.choicemaker.cm.modelmaker.gui.tables.ActiveClueTableModelPlugin;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTableModelPlugin;
import com.choicemaker.cm.modelmaker.gui.utils.NullFloat;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:06 $
 */
public class MeGuiFactory extends MlGuiFactory {
	private ClueTableModelPlugin clueTableModelPlugin;
	private ActiveClueTableModelPlugin activeClueTableModelPlugin;

	public ClueTableModelPlugin getClueTableModelPlugin() {
		if (clueTableModelPlugin == null) {
			clueTableModelPlugin = new ClueTableModelPlugin() {
				private static final long serialVersionUID = 1L;
				TableColumn weightColumn;
				public TableColumn getColumn(int column) {
					if (weightColumn == null) {
						DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
						renderer.setHorizontalAlignment(JLabel.RIGHT);
						TableCellEditor editor = new DefaultCellEditor(new JTextField());
						weightColumn = new TableColumn(startColumn, 150, renderer, editor);
					}
					return weightColumn;
				}
				public String getColumnName(int column) {
					return ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.weight");
				}
				public boolean isCellEditable(int row, int column) {
					return !model.getClueSet().getClueDesc()[row].rule;
				}
				public Object getValueAt(int row, int column) {
					float[] weights = ((MaximumEntropy) model.getMachineLearner()).getWeights();
					return weights == null
						|| model.getClueSet().getClueDesc()[row].rule
							? NullFloat.getNullInstance()
							: new NullFloat(weights[row]);
				}
				public void setValueAt(Object value, int row, int col) {
					try {
						float we = Float.parseFloat(value.toString());
						((MaximumEntropy) model.getMachineLearner()).getWeights()[row] = we;
					} catch (NumberFormatException ex) {
						// ignore
					}
				}
				public int getColumnCount() {
					return 1;
				}
				public int getRowCount() {
					throw new UnsupportedOperationException();
				}
			};
		}
		return clueTableModelPlugin;
	}

	public ActiveClueTableModelPlugin getActiveClueTableModelPlugin() {
		if (activeClueTableModelPlugin == null) {
			activeClueTableModelPlugin = new ActiveClueTableModelPlugin() {
				private static final long serialVersionUID = 1L;
				TableColumn weightColumn;
				public TableColumn getColumn(int column) {
					if (weightColumn == null) {
						DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
						renderer.setHorizontalAlignment(JLabel.RIGHT);
						TableCellEditor editor = new DefaultCellEditor(new JTextField());
						weightColumn = new TableColumn(startColumn, 150, renderer, editor);
					}
					return weightColumn;
				}
				public String getColumnName(int column) {
					return ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.table.common.weight");
				}
				public boolean isCellEditable(int row, int column) {
					return false;
				}
				public Object getValueAt(int row, int column) {
					if (model.getClueSet().getClueDesc()[row].rule) {
						return NullFloat.getNullInstance();
					} else {
						return new NullFloat(((MaximumEntropy) model.getMachineLearner()).getWeights()[row]);
					}
				}
				public void setValueAt(Object value, int row, int col) {
					throw new UnsupportedOperationException();
				}
				public int getColumnCount() {
					return 1;
				}
				public int getRowCount() {
					throw new UnsupportedOperationException();
				}
			};
		}
		return activeClueTableModelPlugin;
	}
	/**
	 * @see com.choicemaker.cm.ml.gui.MlGuiFactory#getTrainDialogPlugin(com.choicemaker.cm.core.MachineLearner)
	 */
	public TrainDialogPlugin getTrainDialogPlugin(MachineLearner learner) {
		return new MeTrainDialogPlugin((MaximumEntropy) learner);
	}
	/**
	 * @see com.choicemaker.cm.core.base.DynamicDispatchHandler#getHandler()
	 */
	public Object getHandler() {
		return this;
	}
	/**
	 * @see com.choicemaker.cm.core.base.DynamicDispatchHandler#getHandledType()
	 */
	public Class getHandledType() {
		return MaximumEntropy.class;
	}

	public String toString() {
		return ChoiceMakerCoreMessages.m.formatMessage("ml.me.label");
	}
	/**
	 * @see com.choicemaker.cm.ml.gui.MlGuiFactory#getMlInstance()
	 */
	public MachineLearner getMlInstance() {
		return new MaximumEntropy();
	}
}
