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
package com.choicemaker.cm.modelmaker.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.modelmaker.filter.ListeningMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.panels.FilterCluePanel;

/**
 * @author S. Yoakum-Stover
 * @author Arturo Falck
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:46:11 $
 */
public class RecordPairFilterDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private ModelMaker parent;
	private ListeningMarkedRecordPairFilter filter;
	private JPanel panel;
	private FilterCluePanel content;
	private JButton cancel;
	private JButton reSet;
	private JButton set;

	public RecordPairFilterDialog(ModelMaker g) {
		super(g, ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.recordpairfilter.label"), false);
		parent = g;
		// Fail fast
		filter = parent.getFilter();
		if (filter == null) {
			throw new IllegalStateException("null filter");
		}
		buildPanel();
		addListeners();
		layoutPanel();
		setContentPane(panel);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocation();
		setVisible(false);
	}

	public void setVisible(boolean b) {
		if (b) {
// TODO:			updateDisplay();
			getRootPane().setDefaultButton(set);
		}
		super.setVisible(b);
	}

	public void reset() {
		content.reset();
	}

	private void set() {
		content.set();
	}

	public void applyFilter() {
		parent.filterMarkedRecordPairList();
	}

	public void buildPanel() {
		panel = new JPanel();
		content = new FilterCluePanel(parent, parent.getFilter());

		cancel = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));
		reSet = new JButton(ChoiceMakerCoreMessages.m.formatMessage("reset"));
		set = new JButton(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.recordpairfilter.apply"));
	}


	/**
	 * Adds the listeners to the GUI widgets.  These listeners handle adding, removing,
	 * and adjusting the filter element of the MarkedRecordPairFilter.
	 */
	private void addListeners() {
		//reSet
		reSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		//cancel
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		//set
		set.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				set();
				applyFilter();
				dispose();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.recordpairfilter");
	}

	private void layoutPanel() {
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		buttonPanel.add(reSet);
		buttonPanel.add(set);
		buttonPanel.add(cancel);

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(10, 10, 10, 10);
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(content, c);
		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		panel.add(buttonPanel, c);


	}

	private void setLocation() {
		setSize(550, 625);
		Dimension d1 = getSize();
		Dimension d2 = getToolkit().getScreenSize();
		int x = Math.max((d2.width - d1.width) / 2, 0);
		int y = Math.max((d2.height - d1.height) / 2, 0);
		super.setBounds(x, y, d1.width, d1.height);
	}
}
