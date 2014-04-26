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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairBinder;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.LinkMap;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.filter.ModelMakerCollectionMRPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class MrpsDecisionComparatorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static void showDialog(ModelMaker modelMaker) {
		new MrpsDecisionComparatorDialog(modelMaker).show();
	}

	private ModelMaker modelMaker;

	private JTextField mrps1, mrps2;
	private JRadioButton pairByPair, byIds;
	private ConfusionMatrix confusionMatrix;

	private List pairs;
	private List[][] matrix;

	public MrpsDecisionComparatorDialog(ModelMaker modelMaker) {
		super(modelMaker, "MRPS Decision Comparator", false);
		this.modelMaker = modelMaker;

		createContent();
		createListeners();

		pack();
		setLocationRelativeTo(modelMaker);
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0, 0};
		layout.rowWeights = new double[] {0, 0, 0, 0, 1};
		getContentPane().setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.BOTH;

		//

		c.gridy = 0;
		c.gridx = 0;
		getContentPane().add(new JLabel("MRPS 1:"), c);

		c.gridx++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		mrps1 = new JTextField(40);
		getContentPane().add(mrps1, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 4;
		getContentPane().add(new JButton(new MrpsBrowseAction(mrps1)), c);

		//

		c.gridy++;
		c.gridx = 0;
		getContentPane().add(new JLabel("MRPS 2:"), c);

		c.gridx++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		mrps2 = new JTextField(40);
		getContentPane().add(mrps2, c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;

		c.gridx = 4;
		getContentPane().add(new JButton(new MrpsBrowseAction(mrps2)), c);

		//

		c.gridy++;
		c.gridx = 2;
		pairByPair = new JRadioButton("Pair by Pair");
		pairByPair.setSelected(true);
		getContentPane().add(pairByPair, c);

		c.gridx = 3;
		byIds = new JRadioButton("By Ids");
		getContentPane().add(byIds, c);

		ButtonGroup bg = new ButtonGroup();
		bg.add(pairByPair);
		bg.add(byIds);

		//

		c.gridy++;
		c.gridx = 2;
		getContentPane().add(new JButton(new CancelAction()), c);

		c.gridx++;
		getContentPane().add(new JButton(new CompareAction()), c);

		//

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		confusionMatrix = new ConfusionMatrix();
		getContentPane().add(confusionMatrix, c);
	}

	private void createListeners() {
		confusionMatrix.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int row = confusionMatrix.rowAtPoint(e.getPoint());
				int col = confusionMatrix.columnAtPoint(e.getPoint());
				if (row > 0 && col > 0) {
					modelMaker.setFilter(new ModelMakerCollectionMRPairFilter(modelMaker, matrix[row-1][col-1]));
				}
			}
		});
	}

	private void computeMatrix() {
		modelMaker.setMultiSource(1, null);

		IProbabilityModel model = modelMaker.getProbabilityModel();
		if (model == null) {
			return;
		}

		String mrpsName1 = mrps1.getText().trim();
		String mrpsName2 = mrps2.getText().trim();

		MarkedRecordPairSource mrps1 = null;
		try {
			mrps1 = MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(mrpsName1);
			mrps1.setModel(model);
		} catch (Exception ex) {
			ErrorDialog.showErrorDialog(modelMaker, "Unable to open MRPS: " + mrpsName1, ex);
		}

		MarkedRecordPairSource mrps2 = null;
		try {
			mrps2 = MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(mrpsName2);
			mrps2.setModel(model);
		} catch (Exception ex) {
			ErrorDialog.showErrorDialog(modelMaker, "Unable to open MRPS: " + mrpsName2, ex);
		}

		if (mrps1 == null || mrps2 == null) {
			return;
		}

		pairs = new ArrayList();

		matrix = new ArrayList[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				matrix[i][j] = new ArrayList();
			}
		}

		// actually fill in pairs and matrix.
		if (pairByPair.isSelected()) {
			computeMatrixPairByPair(mrps1, mrps2);
		} else {
			computeMatrixByIds(mrps1, mrps2);
		}

		int[][] data = new int[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				data[i][j] = matrix[i][j].size();
			}
		}

		confusionMatrix.setData(data);
		modelMaker.setMultiSource(1, MarkedRecordPairBinder.getMarkedRecordPairSource(pairs));
		modelMaker.setMultiIncludeHolds(1, true);
		modelMaker.evaluateClues();
	}

	private void computeMatrixPairByPair(MarkedRecordPairSource mrps1, MarkedRecordPairSource mrps2) {
		try {
			mrps1.open();
			mrps2.open();

			while (true) {
				if (!mrps1.hasNext() && !mrps2.hasNext()) {
					break;
				} else if (!mrps1.hasNext() || !mrps2.hasNext()) {
					ErrorDialog.showErrorDialog(modelMaker, "Warning: MRPS's are of different size!  Will not continue!");
				}

				MutableMarkedRecordPair mrp1 = mrps1.getNextMarkedRecordPair();
				ImmutableMarkedRecordPair mrp2 = mrps2.getNextMarkedRecordPair();

				int i = mrp1.getMarkedDecision().toInt();
				int j = mrp2.getMarkedDecision().toInt();

				matrix[i][j].add(mrp1);
				pairs.add(mrp1);
			}
		} catch (Exception ex) {
			ErrorDialog.showErrorDialog(modelMaker, "Problems reading from MRPS", ex);
		}
	}

	private void computeMatrixByIds(MarkedRecordPairSource mrps1, MarkedRecordPairSource mrps2) {
		LinkMap map1 = null;
		LinkMap map2 = null;
		try {
			map1 = new LinkMap(mrps1);
			map2 = new LinkMap(mrps2);
			List links = map1.getLinks();
			for (int index = 0; index < links.size(); index++) {
				MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) links.get(index);

				int i = mrp.getMarkedDecision().toInt();
				int j = 3; // default not in other.

				if (map2.hasLink(mrp.getQueryRecord(), mrp.getMatchRecord())) {
					j = ((ImmutableMarkedRecordPair)map2.getLinks(mrp.getQueryRecord().getId().toString(), mrp.getMatchRecord().getId().toString()).get(0)).getMarkedDecision().toInt();
				}

				matrix[i][j].add(mrp);
				pairs.add(mrp);
			}

			links = map2.getLinks();
			for (int index = 0; index < links.size(); index++) {
				MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) links.get(index);

				int i = 3; // default not in other
				int j = mrp.getMarkedDecision().toInt();

				if (!map1.hasLink(mrp.getQueryRecord(), mrp.getMatchRecord())) {
					matrix[i][j].add(mrp);
					pairs.add(mrp);
				}
			}
		} catch (Exception ex) {
			ErrorDialog.showErrorDialog(modelMaker, "Problems reading from MRPS", ex);
		}
	}

	static class ConfusionMatrix extends JTable {
		private static final long serialVersionUID = 1L;
		private static final Object[] rowHeaders = {"MRPS1 Differ", "MRPS1 Match", "MRPS1 Hold", "Not in MRPS1"};
		private static final Object[] columnHeaders = {"MRPS2 Differ", "MRPS2 Match", "MRPS2 Hold", "Not in MRPS2"};
		private TableCellRenderer headerRenderer;
		public ConfusionMatrix() {
			setData(new int[4][4]);
			headerRenderer = getTableHeader().getDefaultRenderer();
			setRowSelectionAllowed(false);
			setColumnSelectionAllowed(false);
		}
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (row * column == 0) {
				return headerRenderer;
			} else {
				return super.getCellRenderer(row, column);
			}
		}
		public void setData(int[][] data) {
			Object[][] realData = new Object[5][5];
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					realData[i+1][j+1] = new Integer(data[i][j]);
				}
			}

			for (int j = 0; j < 4; j++) {
				realData[0][j+1] = columnHeaders[j];
			}

			for (int i = 0; i < 4; i++) {
				realData[i+1][0] = rowHeaders[i];
			}

			setModel(new DefaultTableModel(realData, new Object[5]) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int col) {
					return false;
				}
			});
		}
	}

	class CompareAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public CompareAction() {
			super("Compare");
		}
		public void actionPerformed(ActionEvent e) {
			computeMatrix();
		}
	}

	class CancelAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public CancelAction() {
			super("Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

	class MrpsBrowseAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private JTextField tf;
		public MrpsBrowseAction(JTextField tf) {
			super("Browse");
			this.tf = tf;
		}
		public void actionPerformed(ActionEvent e) {
			File f = FileChooserFactory.selectMrpsFile(modelMaker);
			if (f != null) {
				tf.setText(f.getAbsolutePath());
			}
		}
	}

}
