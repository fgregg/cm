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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.choicemaker.cm.core.util.MessageUtil;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:29:52 $
 */
public class GenericProgressDialog extends JDialog implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	public static final String PN_DONE = "DONE";
	public static final String PN_ERROR = "ERROR";

	private Thread thread;

	private HashMap labels = new HashMap();
	
	private JButton cancel;
	private JButton ok;

	public GenericProgressDialog(Frame parent, String title, String[] propertyNames, Thread thread) {
		this(parent, title, propertyNames, null, thread);
	}

	public GenericProgressDialog(Frame parent, String title, String[] propertyNames, String[] initialValues, Thread thread) {
		super(parent, title);
		this.thread = thread;
		
		createContent(propertyNames, initialValues);
		createListeners();
		
		ok.setEnabled(thread.isAlive());
		
		pack();
		setLocationRelativeTo(parent);
	}
	
	public GenericProgressDialog(Dialog parent, String title, String[] propertyNames, Thread thread) {
		this(parent, title, propertyNames, null, thread);
	}
	
	public GenericProgressDialog(Dialog parent, String title, String[] propertyNames, String[] initialValues, Thread thread) {
		super(parent, title, true);
		this.thread = thread;
		
		createContent(propertyNames, initialValues);
		createListeners();
		
		ok.setEnabled(thread.isAlive());
		
		pack();
		setLocationRelativeTo(parent);		
	}
	
	private void createContent(String[] propertyNames, String[] values) {
		if (values == null) {
			values = new String[propertyNames.length];
			Arrays.fill(values, "");
		}

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);

		c.gridy = 0;
		
		for (int i = 0; i < propertyNames.length; i++, c.gridy++) {
			c.gridx = 0;
			c.anchor = GridBagConstraints.WEST;
			getContentPane().add(new JLabel(propertyNames[i]), c);

			c.gridx = 1;
			c.anchor = GridBagConstraints.EAST;
			JLabel label = new JLabel(values[i]);
			labels.put(propertyNames[i], label);
			getContentPane().add(label, c);
		}

		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		ok = new JButton(MessageUtil.m.formatMessage("ok"));
		ok.setEnabled(false);

		getContentPane().add(ok, c);
		c.gridx = 1;
		cancel = new JButton(MessageUtil.m.formatMessage("cancel"));
		getContentPane().add(cancel, c);
	}
		
	private void createListeners() {
		
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
		if (newValue == null) {
			newValue = "";
		}

		if (propertyName == PN_DONE) {
			boolean done = ((Boolean)newValue).booleanValue();
			ok.setEnabled(done);
			cancel.setEnabled(!done);
		} else if (propertyName == PN_ERROR) {
			dispose();
		} else if (labels.containsKey(propertyName)) {
			((JLabel)labels.get(propertyName)).setText(newValue.toString());
		}
	}

}
