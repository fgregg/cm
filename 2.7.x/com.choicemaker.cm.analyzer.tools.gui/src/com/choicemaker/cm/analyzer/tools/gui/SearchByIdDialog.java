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
package com.choicemaker.cm.analyzer.tools.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.modelmaker.filter.CollectionMarkedRecordPairFilter;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * @author ajwinkel
 *
 */
public class SearchByIdDialog extends JDialog {
	
	private static SearchByIdDialog dialog;
	
	public synchronized static void showSearchByIdDialog(ModelMaker mm) {
		if (dialog == null) {
			dialog = new SearchByIdDialog(mm);
		}
		dialog.show();
	}
	
	protected ModelMaker modelMaker;
	
	private JTextField idField;

	private JRadioButton exactMatch;
	private JRadioButton partialMatch;
	private JRadioButton sourceOnly;
	private JRadioButton targetOnly;
	private JRadioButton both;
	
	public SearchByIdDialog(ModelMaker modelMaker) {
		super(modelMaker, "Search by ID", false);
		this.modelMaker = modelMaker;
		createContent();

		pack();
		setLocationRelativeTo(modelMaker);
	}

	private void createContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 0, 1, 0};
		layout.rowWeights = new double[] {1, 1, 1, 1, 1};
		getContentPane().setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);

		// new row
		
		c.gridy = 0;
		c.gridx = 0;
		getContentPane().add(new JLabel("Record ID: "), c);

		c.gridx = 2;
		c.gridwidth = 2;
		idField = new JTextField(20);
		getContentPane().add(idField, c);
		c.gridwidth = 1;
	
		// new row
		
		ButtonGroup bg = new ButtonGroup();
		
		exactMatch = new JRadioButton("Exact match");
		partialMatch = new JRadioButton("Partial match");
		bg.add(exactMatch);
		bg.add(partialMatch);
		exactMatch.setSelected(true);

		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 3;
		JPanel exactPanel = new JPanel();
		getContentPane().add(exactPanel, c);
		c.gridwidth = 1;
		
		exactPanel.add(exactMatch);
		exactPanel.add(partialMatch);
		
		// new row
		
		bg = new ButtonGroup();
		
		sourceOnly = new JRadioButton("Q only");
		targetOnly = new JRadioButton("M only");
		both = new JRadioButton("Either");
		bg.add(sourceOnly);
		bg.add(targetOnly);
		bg.add(both);
		both.setSelected(true);

		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 3;
		JPanel whichPanel = new JPanel();
		getContentPane().add(whichPanel, c);
		c.gridwidth = 1;
		
		whichPanel.add(sourceOnly);
		whichPanel.add(targetOnly);
		whichPanel.add(both);
		
		// new row
		
		c.gridy++;
		c.gridx = 2;
		c.anchor = GridBagConstraints.EAST;
		JButton dedup = new JButton(new SearchByIdAction());
		getContentPane().add(dedup, c);
		c.anchor = GridBagConstraints.CENTER;
		
		c.gridx = 3;
		JButton cancel = new JButton(new CancelAction());
		getContentPane().add(cancel, c);
	}
		
	private void searchById() {
		try {
			ImmutableProbabilityModel model = modelMaker.getProbabilityModel();
			Descriptor d = model.getAccessor().getDescriptor();

			String id = idField.getText().trim();
			if (id == null) {
				return;
			}
						
			List recordPairs = modelMaker.getSourceList();
			if (recordPairs == null) {
				return;
			}
			
			boolean isExactMatch = exactMatch.isSelected();

			int mask = 0;
			if (sourceOnly.isSelected()) {
				mask = 1;
			} else if (targetOnly.isSelected()) {
				mask = 2;
			} else {
				mask = 3;
			}

			List resultSet = new ArrayList();
			for (int i = 0; i < recordPairs.size(); i++) {
				MutableMarkedRecordPair mrp = (MutableMarkedRecordPair) recordPairs.get(i);
				if ((isMatch(id, mrp.getQueryRecord().getId(),isExactMatch) && (mask & 1) > 0) ||
					(isMatch(id, mrp.getMatchRecord().getId(),isExactMatch) && (mask & 2) > 0)) {
					resultSet.add(mrp);
				}
			}
			
			modelMaker.setFilter(new CollectionMarkedRecordPairFilter(modelMaker, resultSet));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private boolean isMatch(String id, Comparable recordId,boolean isExactMatch) {
		
		boolean retVal = false;		
		if (id != null && recordId != null) {
			String sid = id.trim();
			String sval = recordId.toString().trim();
			if (isExactMatch) {
				retVal = sid.equals(sval);
			} else {
				retVal = sid.indexOf(sval) >= 0 || sval.indexOf(sid) >= 0;
			}
		}
		return retVal;
	}
	
	private class SearchByIdAction extends AbstractAction implements DocumentListener {
		public SearchByIdAction() {
			super("Search");
			idField.getDocument().addDocumentListener(this);
			updateEnabled();
		}
		public void actionPerformed(ActionEvent e) {
			searchById();
		}
		public void updateEnabled() {
			boolean e = modelMaker.haveProbabilityModel() && modelMaker.isEvaluated() && idField.getText().trim().length() > 0;
			setEnabled(e);
		}
		public void changedUpdate(DocumentEvent e) { updateEnabled(); }
		public void insertUpdate(DocumentEvent e) { updateEnabled(); }
		public void removeUpdate(DocumentEvent e) { updateEnabled(); }
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Close");	
		}
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	
	
}
