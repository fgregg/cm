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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.RecordRecordData;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.viewer.CompositePane;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.module.IUserMessages;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:46:36 $
 */
public class RecordSourceViewerDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RecordSourceViewerDialog.class.getName());
	
	private final IUserMessages userMessages;
	private RecordSource recordSource;
	private JPanel content;
	private JButton closeButton;
	private JButton nextButton;
	private CompositePane compositePane;
	private boolean open;
	private ImmutableProbabilityModel probabilityModel;
	private CompositePaneModel compositePaneModel;

	public RecordSourceViewerDialog(Frame parent, RecordSource recordSource,
			ImmutableProbabilityModel probabilityModel,
			CompositePaneModel compositePaneModel,
			IUserMessages userMessages) {
		super(parent, "Record source preview");
		if (recordSource == null || probabilityModel == null
				|| compositePaneModel == null || userMessages == null) {
			throw new IllegalArgumentException("null argument");
		}
		setModal(true);
		this.userMessages = userMessages;
		this.recordSource = recordSource;
		this.probabilityModel = probabilityModel;
		this.compositePaneModel = compositePaneModel;
		buildContent();
		addContentListeners();
		setContentPane(content);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		Dimension packPreferred = getPreferredSize();
		Dimension screenSize = getToolkit().getScreenSize();
		setSize(Math.min(packPreferred.width, screenSize.width - 30), Math.min(packPreferred.height, screenSize.height - 30));
		setLocationRelativeTo(parent);
		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.recordsourceviewer");
		showNext();
	}
	/**
	 * Method addContentListeners.
	 */
	private void addContentListeners() {
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showNext();
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	public void dispose() {
		super.dispose();
		try {
			recordSource.close();
		} catch(Exception ex) {
		}
	}

	private void showNext() {
		try {
			if (!open) {
				recordSource.setModel(probabilityModel);
				recordSource.open();
				open = true;
			}
			if (recordSource.hasNext()) {
				compositePane.setRecordData(new RecordRecordData(recordSource.getNext()));
				nextButton.setEnabled(recordSource.hasNext());
			}
		} catch (IOException ex) {
			LoggingObject lo = new LoggingObject("CM-100601", recordSource.getName());
			String msg = lo.getFormattedMessage() + ": " + ex;
			userMessages.postMessage(msg);
			logger.severe(msg);
		}
	}

	/**
	 * Method buildContent.
	 */
	private void buildContent() {
		content = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.rowWeights = new double[] { 1, 0 };
		layout.columnWeights = new double[] { 1, 0, 0 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);
		compositePane = new CompositePane(false, false);
		compositePane.setCompositePaneModel(compositePaneModel);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		content.add(new JScrollPane(compositePane), c);
		c.gridwidth = 1;

		c.gridy = 1;
		c.gridx = 1;
		nextButton = new JButton("Next");
		content.add(nextButton, c);

		c.gridx = 2;
		closeButton = new JButton("Close");
		content.add(closeButton, c);
	}
}
