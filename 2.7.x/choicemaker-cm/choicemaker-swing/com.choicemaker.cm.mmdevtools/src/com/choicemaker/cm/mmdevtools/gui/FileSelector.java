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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

public abstract class FileSelector {
	protected JLabel label;
	protected JTextField textField;
	protected JButton button;
	public FileSelector(String title) {
		this(title, 30, "Browse");
	}
	public FileSelector(String title, int fieldWidth, String browseText) {
		label = new JLabel(title);
		textField = new JTextField(fieldWidth);
		button = new JButton(browseText);
		
		createBrowseListener();
	}
	public boolean hasFile() {
		return textField.getText().trim().length() > 0;
	}
	public File getFile() {
		String text = textField.getText().trim();
		if (text.length() > 0) {
			return new File(text);
		} else {
			return null;
		}
	}
	public void setFile(File f) {
		textField.setText(f.getAbsolutePath());
	}
	public JLabel getLabel() {
		return label;
	}
	public JTextField getTextField() {
		return textField;
	}
	public JButton getBrowseButton() {
		return button;
	}
	public void addDocumentListener(DocumentListener dl) {
		textField.getDocument().addDocumentListener(dl);
	}
	private void createBrowseListener() {
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File f = selectFile();
				if (f != null) {
					setFile(f);
				} 
			}
		});
	}
	protected abstract File selectFile();
	
}
