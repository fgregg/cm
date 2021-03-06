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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.JTextField;

import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.viewer.InternalFrameModel;

/**
 * @author Arturo Falck
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class InternalFrameDialog extends JDialog {
	private static final long serialVersionUID = 1L;


	private InternalFrameModel internalFrameModel;

	private JPanel panel;
	private JButton set;

	private JTextField frameAlias;

	public InternalFrameDialog(JFrame frame, InternalFrameModel internalFrameModel) {
		super(frame, "Frame Title", true);
		this.internalFrameModel = internalFrameModel;
		buildPanel();
		addListeners();
		layoutPanel();
		setContentPane(panel);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	public void buildPanel() {
		panel = new JPanel();
		
		frameAlias = new JTextField(20);
		frameAlias.setText(internalFrameModel.getAlias());
		
		frameAlias.setMinimumSize(frameAlias.getPreferredSize());
				
		set = new JButton("Close");
	}


	/**
	 * Adds the listeners to the GUI widgets.  These listeners handle adding, removing,
	 * and adjusting the filter element of the MarkedRecordPairFilter.
	 */
	private void addListeners() {
		
		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.internalframe");

		frameAlias.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InternalFrameDialog.this.internalFrameModel.setAlias(frameAlias.getText());
			}
		});
		
		frameAlias.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				InternalFrameDialog.this.internalFrameModel.setAlias(frameAlias.getText());
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
		JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 10, 10));
		buttonPanel.add(set);
		
		JPanel titleSelector = new JPanel(new GridLayout(2,1,10,10));
		titleSelector.add(new JLabel("Alias:"));
		titleSelector.add(frameAlias);

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 10, 10, 10);
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		panel.add(titleSelector, c);
//		c.weightx = 1;
//		c.weighty = 1;
//		c.fill = GridBagConstraints.BOTH;
//		c.gridy = 1;
//		panel.add(content, c);
		c.gridy = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		panel.add(buttonPanel, c);
		

	}

}
