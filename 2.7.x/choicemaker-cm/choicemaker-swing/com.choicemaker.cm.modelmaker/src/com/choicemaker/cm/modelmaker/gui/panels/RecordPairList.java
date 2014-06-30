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
package com.choicemaker.cm.modelmaker.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.choicemaker.cm.core.base.RepositoryChangeEvent;
import com.choicemaker.cm.core.base.RepositoryChangeListener;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;

public class RecordPairList
	extends JPanel
	implements RepositoryChangeListener, PropertyChangeListener, EvaluationListener {
	private static final long serialVersionUID = 1L;
	private ModelMaker parent;
	private DefaultListModel recordPairListModel;
	private JList recordPairList;

	public RecordPairList(ModelMaker parent) {
		this.parent = parent;
		build();
		addListeners();
		parent.addPropertyChangeListener(this);
		parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		parent.addMarkedRecordPairDataChangeListener(this);
		parent.addEvaluationListener(this);
	}

	private void build() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.panel.recordpairlist")), c);
		recordPairListModel = new DefaultListModel();
		recordPairList = new JList(recordPairListModel);
		JScrollPane p = new JScrollPane(recordPairList);
		p.setMinimumSize(new Dimension(50, 50));
		p.setPreferredSize(new Dimension(50, 400));
		c.gridy = 2;
		c.gridheight = 3;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		add(p, c);
	}

	private void addListeners() {
		recordPairList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String sv = (String) recordPairList.getSelectedValue();
				if (sv != null) {
					int selection = Integer.parseInt(sv);
					parent.setMarkedRecordPair(selection);
					parent.showHumanReviewPanel();
				}
			}
		});
	}

	public void updateRecordPairList(int[] items) {
		recordPairListModel.clear();
		if (items == null) {
			return;
		}
		for (int i = 0; i < items.length; i++) {
			recordPairListModel.addElement(String.valueOf(items[i]));
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object source = evt.getSource();
		if (source == parent) {
			if (propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE || propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				clearRecordPairList();
			}
		} else if (source == parent.getProbabilityModel()) {
			if (propertyName == null) {
				clearRecordPairList();
			}
		}
	}

	public void evaluated(EvaluationEvent evt) {
		clearRecordPairList();
	}

	public void setChanged(RepositoryChangeEvent evt) {
		clearRecordPairList();
	}

	public void recordDataChanged(RepositoryChangeEvent evt) {
	}

	public void markupDataChanged(RepositoryChangeEvent evt) {
	}

	private void clearRecordPairList() {
		recordPairListModel.clear();
	}

	public void reviewNextMarkedRecordPair() {
		int selectionIndex = recordPairList.getSelectedIndex();
		if (selectionIndex == -1) {
			int first = recordPairList.getFirstVisibleIndex();
			if (first != -1) {
				recordPairList.setSelectedIndex(first);
			}
		} else if (selectionIndex >= recordPairListModel.size() - 1) {
			return;
		}
		recordPairList.setSelectedIndex(selectionIndex + 1);
	}

	public void reviewPreviousMarkedRecordPair() {
		int selectionIndex = recordPairList.getSelectedIndex();
		if (selectionIndex == -1) {
			selectionIndex = recordPairList.getLastVisibleIndex();
			recordPairList.setSelectedIndex(selectionIndex);
		} else if (selectionIndex == 0) {
			return;
		}
		recordPairList.setSelectedIndex(selectionIndex - 1);
	}

	public int modelSize() {
		return recordPairListModel.size();
	}

	public int getFirstIndex() {
		return Integer.parseInt((String) recordPairListModel.firstElement());
	}

	public int getLastIndex() {
		return Integer.parseInt((String) recordPairListModel.lastElement());
	}
}
