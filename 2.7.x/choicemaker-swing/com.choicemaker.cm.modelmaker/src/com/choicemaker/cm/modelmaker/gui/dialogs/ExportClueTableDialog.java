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

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.MrpsExport;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.tables.ClueTableModel;

/**
 * @author rphall
 */
public class ExportClueTableDialog extends JDialog {

	private static File lastFile;

	private ClueTableModel clueTableModel;
	private ModelMaker parent;

	private JTextField fileField;
	private JButton browseButton;
	private JButton okButton, cancelButton;

	public ExportClueTableDialog(ModelMaker parent) {
		super(parent, "Export Clue Table", true);
		this.parent = parent;
		setClueTableModel(parent.getTrainingControlPanel().getClueTableModel());

		createContent();
		createListeners();

		pack();
		setResizable(false);
		setLocationRelativeTo(parent);

		updateEnabledness();
	}

	private void setClueTableModel(ClueTableModel clueTableModel) {
		this.clueTableModel = clueTableModel;
	}

	private ClueTableModel getClueTableModel() {
		return clueTableModel;
	}

/*
	private void exportProbabilities(
		List pairs,
		File file,
		boolean ids,
		boolean prob,
		boolean dec,
		int acPolicy,
		String delim)
		throws IOException {

		ImmutableProbabilityModel model = parent.getProbabilityModel();
		FileWriter fw = new FileWriter(file);
		MrpsExport.exportProbabilities(
			model,
			pairs,
			fw,
			ids,
			prob,
			dec,
			acPolicy,
			delim);
		return;
	}
*/

	private void updateEnabledness() {
		File f = getFile();
		boolean b =
			f != null
				&& (f.isFile()
					|| (!f.exists() && f.getParentFile().isDirectory()));
		okButton.setEnabled(b);
	}

	private void maybeRememberFile() {
		File f = getFile();
		if (f == null) {
			return;
		} else if (f.exists() || f.getParentFile().isDirectory()) {
			lastFile = f;
		}
	}

	private File getFile() {
		String text = fileField.getText().trim();
		if (text.length() > 0) {
			return new File(text).getAbsoluteFile();
		} else {
			return null;
		}
	}

	private void mySetCursor(int cursorType) {
		Cursor c = Cursor.getPredefinedCursor(cursorType);
		this.setCursor(c);
		parent.setCursor(c);
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0, 1, 1, 0 };
		getContentPane().setLayout(layout);

		Insets smallInsets = new Insets(2, 3, 2, 3);
		Insets bigInsets = new Insets(2, 20, 2, 3);
		Insets reallyBigInsets = new Insets(2, 37, 2, 3);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = smallInsets;
		c.fill = GridBagConstraints.HORIZONTAL;

		//

		c.gridy = 0;

		c.gridx = 0;
		getContentPane().add(new JLabel("Destination: "), c);

		c.gridx = 1;
		c.gridwidth = 2;
		fileField = new JTextField(30);
		if (lastFile != null) {
			fileField.setText(lastFile.getAbsolutePath());
		}
		getContentPane().add(fileField, c);
		c.gridwidth = 1;

		c.gridx = 3;
		browseButton = new JButton("Browse");
		getContentPane().add(browseButton, c);

		// horizontal spacer.

		c.gridx = 0;
		c.gridy = 1;
		getContentPane().add(Box.createVerticalStrut(10), c);

		// 

		c.gridy++;

		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		okButton = new JButton("OK");
		getContentPane().add(okButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;

		c.gridx = 3;
		cancelButton = new JButton("Cancel");
		getContentPane().add(cancelButton, c);

	}

	private void createListeners() {

		fileField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateEnabledness();
			}
			public void insertUpdate(DocumentEvent e) {
				updateEnabledness();
			}
			public void removeUpdate(DocumentEvent e) {
				updateEnabledness();
			}
		});

		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				File f = getFile();
				if (f != null) {
					fc.setCurrentDirectory(f);
				}

				if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					fileField.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		ItemListener updateListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateEnabledness();
			}
		};

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List pairs = parent.getSourceList();
				File file = getFile();
				try {
					mySetCursor(Cursor.WAIT_CURSOR);
					getClueTableModel().exportToFile(file);
					maybeRememberFile();
					dispose();
					mySetCursor(Cursor.DEFAULT_CURSOR);
				} catch (Exception ex) {
					mySetCursor(Cursor.DEFAULT_CURSOR);
					Logger.getLogger(ExportClueTableDialog.class).error(
						"Unable to export clue table",
						ex);
				}
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maybeRememberFile();
				dispose();
			}
		});

	}

}

