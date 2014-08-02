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
package com.choicemaker.cm.gui.utils.viewer.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.fieldselectortable.FieldSelectorTable;
import com.choicemaker.cm.gui.utils.viewer.RecordPairFrameModel;

/**
 * @author Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class FieldSelectorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(FieldSelectorDialog.class);

	private RecordPairFrameModel recordPairFrameModel;

	private JScrollPane content;

	private JButton set;

	private JTextField frameAlias;

	public FieldSelectorDialog(JFrame frame, RecordPairFrameModel recordPairFrameModel) {
		super(frame, "Field Selector", true);
		this.recordPairFrameModel = recordPairFrameModel;
		buildPanel();
		addListeners();
		layoutPanel();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public void buildPanel() {		
		frameAlias = new JTextField(30);
		frameAlias.setText(recordPairFrameModel.getAlias());
		
		frameAlias.setMinimumSize(frameAlias.getPreferredSize());
		
		content = new JScrollPane();
		FieldSelectorTable table = new FieldSelectorTable();
		
		table.setModel(recordPairFrameModel.getRecordTableColumnModel());
		content.getViewport().add(table);
		content.setPreferredSize(new Dimension(300, 300));
		content.setMinimumSize(new Dimension(300, 100));
		
		set = new JButton("Close");
	}


	/**
	 * Adds the listeners to the GUI widgets.  These listeners handle adding, removing,
	 * and adjusting the filter element of the MarkedRecordPairFilter.
	 */
	private void addListeners() {
		
		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.fieldselector");
		
		frameAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FieldSelectorDialog.this.recordPairFrameModel.setAlias(frameAlias.getText());
			}
		});
		
		frameAlias.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				FieldSelectorDialog.this.recordPairFrameModel.setAlias(frameAlias.getText());
			}
		});
		
		set.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				set();
				dispose();
			}
		});
	}

	private void layoutPanel() {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		layout.columnWeights = new double[] { 0, 0, 1, 0 };
		layout.rowWeights = new double[] {0, 0, 1, 0};
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);
		
		c.gridy = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Title:"), c);
		c.gridx = 1;	
		panel.add(new JLabel(recordPairFrameModel.getDescriptor().getName()), c);
		
		c.gridy = 1;
		c.gridx = 0;
		panel.add(new JLabel("Alias:"), c);
		c.gridx = 1;
		c.gridwidth = 2;
		panel.add(frameAlias, c);
		c.gridwidth = 1;
		
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		panel.add(content, c);
		
		c.gridy = 3;
		c.gridx = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		panel.add(set, c);
				
		setContentPane(panel);
	}
}
