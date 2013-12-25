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
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Thresholds;
import com.choicemaker.cm.mmdevtools.util.CrossValidator;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class CrossValidatorDialog extends JDialog {

	private ModelMaker modelMaker;

	private JRadioButton currentMrps, anotherMrps;
	private JTextField numPieces;
	private JButton runButton, closeButton;

	public CrossValidatorDialog(ModelMaker mm) {
		super(mm, "Cross Validation Tool", true);
		this.modelMaker = mm;
	
		createContent();
		createListeners();
		
		pack();
		setLocationRelativeTo(mm);
		
		updateEnabled();
	}

	private int getNumPieces() {
		try {
			return Integer.parseInt(numPieces.getText());
		} catch (NumberFormatException ex) {
			return -1;
		}
	}
	
	private List getSourceList() {
		return modelMaker.getSourceList();
	}

	private void updateEnabled() {
		runButton.setEnabled(getSourceList() != null && getNumPieces() > 0);
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 5, 2, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 4;
		getContentPane().add(new JLabel("Train/Test MRPS:"), c);
		c.gridwidth = 1;
		
		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		currentMrps = new JRadioButton("Current MRPS");
		currentMrps.setSelected(true);
		getContentPane().add(currentMrps, c);
		c.gridwidth = 1;
		
		//
		
		/*
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		anotherMrps = new JRadioButton("Another MRPS");
		getContentPane().add(anotherMrps, c);
		c.gridwidth = 1;
		*/
		
		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		getContentPane().add(new JLabel("Num Pieces"), c);
		c.gridwidth = 1;
		
		//
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		numPieces = new JTextField("10", 5);
		getContentPane().add(numPieces, c);
		c.gridwidth = 1;
		
		//
		
		c.gridy++;
		
		c.gridx = 2;
		runButton = new JButton("Run Cross Validation");
		getContentPane().add(runButton, c);
		
		c.gridx = 3;
		closeButton = new JButton("Close");
		getContentPane().add(closeButton, c);

	}

	private void createListeners() {
		
		DocumentListener dl = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { updateEnabled(); }
			public void insertUpdate(DocumentEvent e) { updateEnabled(); }
			public void removeUpdate(DocumentEvent e) { updateEnabled(); }			
		};
		numPieces.getDocument().addDocumentListener(dl);
		
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IProbabilityModel model = modelMaker.getProbabilityModel();
				Thresholds t = modelMaker.getThresholds();
				
				List mrpList = getSourceList();
				int numPieces = getNumPieces();
				CrossValidator cv = new CrossValidator(model, t.getDifferThreshold(), t.getMatchThreshold(), mrpList, numPieces);
				
				cv.run();
				cv.printResults();
			}
		});
		
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
	}

}
