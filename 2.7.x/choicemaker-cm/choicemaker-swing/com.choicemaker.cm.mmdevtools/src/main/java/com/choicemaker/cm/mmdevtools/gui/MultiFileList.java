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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;

/**
 * @author ajwinkel
 *
 */
public class MultiFileList extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final int RS = -1;
	public static final int MRPS = -2;

	protected int fileType;

	protected JList list;
	protected JButton addButton;
	protected JButton removeButton;

	public MultiFileList(int fileType) {
		this.fileType = fileType;
		if (fileType != RS && fileType != MRPS) {
			throw new IllegalArgumentException("File type invalid");
		}
		
		buildContent();
		addListeners();
	}

	/**
	 * Hook that subclasses can use to perform custom add operations...
	 */
	protected void performAdd() {
		File[] files = null;
		if (fileType == RS) {
			files = FileChooserFactory.selectRsFiles(getParent());
		} else if (fileType == MRPS) {
			files = FileChooserFactory.selectMrpsFiles(getParent());
		} else {
			throw new IllegalStateException("Unknown fileType");
		}
		
		if (files.length > 0) {
			addFiles(files);
		}
	}

	protected void addListener(ListDataListener listener) {
		((DefaultListModel)list.getModel()).addListDataListener(listener);
	}

	protected void removeListener(ListDataListener listener) {
		((DefaultListModel)list.getModel()).removeListDataListener(listener);
	}

	protected void addFiles(File[] files) {
		DefaultListModel model = (DefaultListModel) list.getModel();
		int size = model.size();
		for (int i = 0; i < files.length; i++) {
			model.add(size + i, files[i]);
		}
	}
	
	public File[] getFiles() {
		DefaultListModel model = (DefaultListModel) list.getModel();
		File[] files = new File[model.size()];
		model.copyInto(files);
		return files;
	}

	public int getNumFiles() {
		DefaultListModel model = (DefaultListModel) list.getModel();
		return model.size();
	}

	protected void buildContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {1, 0};
		layout.rowWeights = new double[] {0, 0, 1};
		setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);

		//
		
		c.gridy = 0;
		c.gridx = 0;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		list = new JList(new DefaultListModel());
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(list);
		add(listScroller, c);
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;		

		c.gridx = 1;
		addButton = new JButton("Add...");
		add(addButton, c);

		//
		
		c.gridy = 1;		
		c.gridx = 1;			
		removeButton = new JButton("Remove");
		removeButton.setEnabled(false);
		add(removeButton, c);
	}
	
	protected void addListeners() {
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					removeButton.setEnabled(list.getSelectedIndex() >= 0);
				}
			}
		});

		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel model = (DefaultListModel) list.getModel();
				model.remove(list.getSelectedIndex());
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performAdd();
			}
		});
	}
	
}
