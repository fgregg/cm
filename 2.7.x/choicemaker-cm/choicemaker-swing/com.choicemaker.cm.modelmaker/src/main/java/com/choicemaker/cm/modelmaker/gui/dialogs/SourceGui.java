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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.util.logging.Logger;

import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;

/**
 * Superclass of the different types of MRPSGuis associated
 * with the different types of SourceRecordPairSources.
 * Objects of this class are created by the corresponding
 * type of SourceGuiFactory.  They are used
 * by the Application so that users can easily configure
 * and build SourceSources.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:47:04 $
 */
public abstract class SourceGui extends JDialog {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SourceGui.class.getName());
	protected static final int CREATE = 0;
	protected static final int GENERATE = 1;

	protected int mode = CREATE;

	protected ModelMaker parent;
	protected JPanel content;
	protected Source source;
	protected JButton okayButton;
	protected JButton cancelButton;
	protected boolean isNewSource;
	protected JTextField sourceFileName;
	protected JButton sourceFileBrowseButton;
	protected String oldName;
//	private int type;

	public Source define() {
		setVisible(true);
		return source;
	}

	/**
	 * Builds a JDialog.
	 */
	protected SourceGui(ModelMaker parent, String title) {
		super(parent, title, true);
		this.parent = parent;
		isNewSource = true;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				source = null;
			}
		});
	}

	protected void init(Source s) {
		source = s;
		content = new JPanel();
		setContentPane(content);
		buildContent();
		addContentListeners();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
	}

	protected void create() {
		try {
			String newName = getSourceFileName();
			if ((isNewSource || !newName.equals(oldName)) && new File(newName).exists()) {
				if (JOptionPane
					.showConfirmDialog(
						null,
						ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.replace", newName),
						ChoiceMakerCoreMessages.m.formatMessage("confirm"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE)
					== JOptionPane.YES_OPTION) {
				} else {
					return;
				}
			}
			buildSource();
			dispose();
		} catch (Exception ex) {
			logger.severe(new LoggingObject("CM-100701", getSourceFileName()).toString() + ": " + ex);
		}
	}

	protected void generate() {
	}


	protected abstract String getFileTypeDescription();

	protected abstract String getExtension();

	/**
	 * The build button triggers the building of the model.
	 */
	protected void addContentListeners() {
		//okayButton
		okayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (mode == CREATE) {
					create();
				} else {
					generate();
				}
			}
		});

		//cancelButton
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent ev) {
				source = null;
				dispose();
			}
		});
	}

	public void modifySource(String oldName) {
		this.oldName = oldName;
		isNewSource = false;
	}

	/**
	 * Abstract method that must be implemented by subclasses
	 * to build the JPanel for display by the parent.  This
	 * method is executed by the constructor.
	 */
	public abstract void buildContent();

	/**
	 * Abstract method that must be implemented by subclasses
	 * to build the appropriate kind of Source.
	 */
	public abstract void buildSource();

	protected String getSourceFileName() {
		String n = sourceFileName.getText();
		String extension = getExtension();
		if (!n.endsWith("." + extension)) {
			n += "." + extension;
		}
		return n;
	}

}
