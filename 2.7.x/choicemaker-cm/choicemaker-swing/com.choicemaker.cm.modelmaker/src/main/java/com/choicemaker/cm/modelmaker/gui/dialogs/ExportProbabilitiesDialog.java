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
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.MrpsExport;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel (initial version)
 * @author rphall (AC_CLUE_NAMES,AC_GROUPS)
 *
 */
public class ExportProbabilitiesDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ExportProbabilitiesDialog.class.getName());

	private static final int AC_NONE = MrpsExport.AC_NONE;
	private static final int AC_BIT_VECTOR = MrpsExport.AC_BIT_VECTOR;
	private static final int AC_CLUE_INDICES = MrpsExport.AC_CLUE_INDICES;
	private static final int AC_CLUE_NAMES = MrpsExport.AC_CLUE_NAMES;
	private static final int AC_GROUPS = MrpsExport.AC_GROUPS;

	private static File lastFile;

	private ModelMaker parent;

	private JTextField fileField;
	private JButton browseButton;
	private JCheckBox idsBox, probabilityBox, decisionBox, markingBox, detailsBox;
	private JRadioButton acNone, acBitVector, acClueIndices, acClueNames, acClueOnlyGroups;
	private JButton okButton, cancelButton;

	public ExportProbabilitiesDialog(ModelMaker parent) {
		super(parent, "Export Probabilities and Active Clues", true);
		this.parent = parent;
		
		createContent();
		createListeners();
		
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
		
		updateEnabledness();
	}
		
	private void exportProbabilities(
		List pairs, 
		File file, 
		boolean ids, 
		boolean prob, 
		boolean dec,
		boolean mrk,
		boolean details,
		int acPolicy, 
		String delim) throws 
			IOException {
		
		ImmutableProbabilityModel model = parent.getProbabilityModel();
		FileWriter fw = new FileWriter(file);
		MrpsExport.exportProbabilities(model,pairs,fw,ids,prob,dec,mrk,details,acPolicy,delim);
		return;
	}
	
	private void updateEnabledness() {
		File f = getFile();
		boolean b = f != null && (f.isFile() || (!f.exists() && f.getParentFile().isDirectory())) &&
			(includeIds() || includeProbability() || includeDecision() || getIncludeActiveCluesPolicy() != AC_NONE);
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
	
	private boolean includeIds() {
		return idsBox.isSelected();
	}
	
	private boolean includeProbability() {
		return probabilityBox.isSelected();
	}
	
	private boolean includeDecision() {
		return decisionBox.isSelected();
	}
	
	private boolean includeMarking() {
		return markingBox.isSelected();
	}
	
	private boolean includeProbabilityDetails() {
		return detailsBox.isSelected();
	}
	
	private int getIncludeActiveCluesPolicy() {
		int retVal;
		if (acNone.isSelected()) {
			retVal = AC_NONE;
		} else if (acBitVector.isSelected()) {
			retVal = AC_BIT_VECTOR;
		} else if (acClueIndices.isSelected()) {
			retVal = AC_CLUE_INDICES;
		} else if (acClueNames.isSelected()) {
			retVal = AC_CLUE_NAMES;
		} else if (acClueOnlyGroups.isSelected()) {
			retVal = AC_GROUPS;
		} else {
			// Error
			logger.severe("Design error: unexpected ActiveCluesPolicy. No clues exported.");
			retVal = AC_NONE;
		}
		return retVal;
	}
	
	private void mySetCursor(int cursorType) {
		Cursor c = Cursor.getPredefinedCursor(cursorType);
		this.setCursor(c);
		parent.setCursor(c);
	}
	
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 1, 0};
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
		
		c.gridy = 1;
		getContentPane().add(Box.createVerticalStrut(10), c);
	
		// left side...
		
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		c.insets = bigInsets;
		getContentPane().add(new JLabel("Primary Output:"), c);
		
		c.insets = reallyBigInsets;
		
		c.gridy++;
		idsBox = new JCheckBox("Q Id, M Id");
		idsBox.setSelected(true);
		getContentPane().add(idsBox, c);

		c.gridy++;
		probabilityBox =  new JCheckBox("CM Probability");
		probabilityBox.setSelected(true);
		getContentPane().add(probabilityBox, c);

		c.gridy++;
		decisionBox =  new JCheckBox("CM Decision");
		decisionBox.setSelected(false);
		getContentPane().add(decisionBox, c);

		c.gridy++;
		markingBox =  new JCheckBox("Human Marking");
		markingBox.setSelected(false);
		getContentPane().add(markingBox, c);

		c.gridy++;
		detailsBox =  new JCheckBox("Probability Details");
		detailsBox.setSelected(false);
		getContentPane().add(detailsBox, c);

		c.gridwidth = 1;
		c.insets = smallInsets;
		
		// right side...
		
		c.gridy = 2;
		
		c.gridx = 2;
		c.gridwidth = 2;
		getContentPane().add(new JLabel("Clues and Rules:"), c);

		// three rows here...
		
		c.insets = bigInsets;
				
		c.gridy++;
		acNone = new JRadioButton("DoNothingMachineLearning");
		acNone.setSelected(true);
		getContentPane().add(acNone, c);

		c.gridy++;
		acBitVector = new JRadioButton("Bit Vector");
		getContentPane().add(acBitVector, c);

		c.gridy++;
		acClueIndices = new JRadioButton("Active Clue/Rule Indices");
		getContentPane().add(acClueIndices, c);
		
		c.gridy++;
		acClueNames = new JRadioButton("Active Clue/Rule Names");
		getContentPane().add(acClueNames, c);

		c.gridy++;
		acClueOnlyGroups = new JRadioButton("Active Clues, No Rules, MEGAM/YASMET Format");
		getContentPane().add(acClueOnlyGroups, c);

		c.gridwidth = 1;
		c.insets = smallInsets;
		
		// horizontal spacer.
		
		c.gridy = 8;
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
			public void changedUpdate(DocumentEvent e) { updateEnabledness(); }
			public void insertUpdate(DocumentEvent e) { updateEnabledness(); }
			public void removeUpdate(DocumentEvent e) { updateEnabledness(); }
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
		
		idsBox.addItemListener(updateListener);
		probabilityBox.addItemListener(updateListener);
		decisionBox.addItemListener(updateListener);
		markingBox.addItemListener(updateListener);
		detailsBox.addItemListener(updateListener);

		acNone.addItemListener(updateListener);
		acBitVector.addItemListener(updateListener);
		acClueIndices.addItemListener(updateListener);
		acClueNames.addItemListener(updateListener);
		acClueOnlyGroups.addItemListener(updateListener);

		ButtonGroup bg = new ButtonGroup();
		bg.add(acNone);
		bg.add(acBitVector);
		bg.add(acClueIndices);
		bg.add(acClueNames);
		bg.add(acClueOnlyGroups);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List pairs = parent.getSourceList();
				File file = getFile();
				try {
					mySetCursor(Cursor.WAIT_CURSOR);
					exportProbabilities(
						pairs, 
						file, 
						includeIds(), 
						includeProbability(), 
						includeDecision(),
						includeMarking(),
						includeProbabilityDetails(),
						getIncludeActiveCluesPolicy(), ", ");
					maybeRememberFile();
					dispose();
					mySetCursor(Cursor.DEFAULT_CURSOR);
				} catch (IOException ex) {
					mySetCursor(Cursor.DEFAULT_CURSOR);
					logger.severe("Unable to export probabilities: " + ex);
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
