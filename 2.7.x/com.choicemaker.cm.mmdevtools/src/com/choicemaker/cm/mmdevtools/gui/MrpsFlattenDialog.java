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
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.RecordSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.ErrorDialog;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author Adam Winkel
 */
public class MrpsFlattenDialog extends JDialog {

	public static void showMrpsFlattenDialog(ModelMaker modelMaker) {
		new MrpsFlattenDialog(modelMaker).show();
	}

	private ModelMaker modelMaker;
	
	private FileSelector rsSelector;
	private JButton flatten, cancel;

	public MrpsFlattenDialog(ModelMaker modelMaker) {
		super(modelMaker, "MRPS Flattener", true);
		this.modelMaker = modelMaker;
		
		createContent();
		createListeners();
		
		pack();
		setLocationRelativeTo(modelMaker);
	}
	
	private boolean doFlattening() {
		List sourceList = modelMaker.getSourceList();

		RecordSource rs = null;
		try {
			rs = RecordSourceXmlConf.getRecordSource(rsSelector.getFile().getPath());
		} catch (Exception ex) {
			ErrorDialog.showErrorDialog(this, "Error opening RecordSource: " + ex.getMessage(), ex);
			return false;
		}
		
		rs.setModel(modelMaker.getProbabilityModel());
		RecordSink sink = (RecordSink) rs.getSink();
		try {
			Set ids = new HashSet();
			sink.open();
			for (int i = 0; i < sourceList.size(); i++) {
				MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) sourceList.get(i);
				if (ids.add(mrp.getQueryRecord().getId())) {
					sink.put(mrp.getQueryRecord());
				}
				if (ids.add(mrp.getMatchRecord().getId())) {
					sink.put(mrp.getMatchRecord());
				}
			}
			sink.close();		
		} catch (IOException ex) {
			ErrorDialog.showErrorDialog(this, "Error transfering records to sink: " + ex.getMessage(), ex);
			return false;
		}
		
		return true;
	}
	
	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0};
		layout.rowWeights = new double[] {0, 0, 1};
		getContentPane().setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		//
		
		rsSelector = new RsSelector("Browse");
				
		//
		
		c.gridy = 0;
		
		c.gridx = 0;
		getContentPane().add(rsSelector.getLabel(), c);

		c.gridx = 1;
		c.gridwidth = 2;
		getContentPane().add(rsSelector.getTextField(), c);
		c.gridwidth = 1;
		
		c.gridx = 3;
		getContentPane().add(rsSelector.getBrowseButton(), c);
		
		//
		
		c.gridy++;
		
		c.gridx = 2;
		flatten = new JButton("Flatten");
		flatten.setEnabled(false);
		getContentPane().add(flatten, c);
		
		c.gridx = 3;
		cancel = new JButton("Cancel");
		getContentPane().add(cancel, c);
		
	}

	private void createListeners() {
		
		rsSelector.addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				flatten.setEnabled(rsSelector.hasFile());
			}
			public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
			public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
		});
		
		flatten.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (doFlattening()) {
					dispose();
				}
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
	}
		
	class RsSelector extends FileSelector {
		public RsSelector(String label) {
			super(label);
		}
		protected File selectFile() {
			return FileChooserFactory.selectRsFile(modelMaker);
		}
	}

}
