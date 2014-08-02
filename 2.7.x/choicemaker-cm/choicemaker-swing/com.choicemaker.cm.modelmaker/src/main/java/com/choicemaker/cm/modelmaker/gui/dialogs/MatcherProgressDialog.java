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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.matcher.Matcher;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:08 $
 */
class MatcherProgressDialog extends JDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private Matcher matcher;
	private JLabel numRecordsFromSmall;
	private JLabel numRecordsFromLarge;
	private JLabel numPairs;
	private JButton cancel;
	private JButton ok;
	private Thread thread;

	MatcherProgressDialog(JFrame frame, Matcher matcher, Thread thread) {
		super(frame, "Matching");
		this.matcher = matcher;
		this.thread = thread;
		buildDialog();
		addListeners();
		matcher.addPropertyChangeListener(this);
		ok.setEnabled(matcher.isDone());
		pack();
		setLocationRelativeTo(frame);
	}
	/**
	 * Method addListeners.
	 */
	private void addListeners() {
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancel.setEnabled(false);
				thread.interrupt();
				try {
					thread.join();
				} catch (InterruptedException e) {
					// do nothing
				}
				dispose();
			}
		});
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		if (propertyName == Matcher.NUM_RECORDS_FROM_SMALL) {
			numRecordsFromSmall.setText(newValue.toString());
		} else if (propertyName == Matcher.NUM_RECORDS_FROM_LARGE) {
			numRecordsFromLarge.setText(newValue.toString());
		} else if (propertyName == Matcher.NUM_PAIRS) {
			numPairs.setText(newValue.toString());
		} else if (propertyName == Matcher.DONE) {
			ok.setEnabled(((Boolean)newValue).booleanValue());
		}
	}

	void setValues() {
		numRecordsFromSmall.setText(String.valueOf(matcher.getNumRecordsFromSmall()));
		numRecordsFromLarge.setText(String.valueOf(matcher.getNumRecordsFromLarge()));
		numPairs.setText(String.valueOf(matcher.getNumPairs()));
		ok.setEnabled(matcher.isDone());
	}

	private void buildDialog() {
		JPanel content = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWeights = new double[] { 1 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		content.add(new JLabel("Number of records read from small source"), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		numRecordsFromSmall = new JLabel("0");
		content.add(numRecordsFromSmall, c);

		c.gridy = 1;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		content.add(new JLabel("Number of records read from large source"), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		numRecordsFromLarge = new JLabel("0");
		content.add(numRecordsFromLarge, c);

		c.gridy = 2;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		content.add(new JLabel("Number of pairs produced"), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		numPairs = new JLabel("0");
		content.add(numPairs, c);

		c.gridy = 3;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		ok = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
		ok.setEnabled(false);
		content.add(ok, c);
		c.gridx = 1;
		cancel = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));
		content.add(cancel, c);

		setContentPane(content);
	}
}
