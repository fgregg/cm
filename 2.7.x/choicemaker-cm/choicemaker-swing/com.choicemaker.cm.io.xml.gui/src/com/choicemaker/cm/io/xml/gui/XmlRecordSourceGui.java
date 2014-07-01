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
package com.choicemaker.cm.io.xml.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.xml.base.XmlRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.RecordSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;

/**
 * The MRPSGui associated the XmlRecordSource.
 * An objects of this class would be created by the 
 * XmlRecordSourceGuiFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build XmlRecordSources.
 * 
 * @author  S. Yoakum-Stover
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:16:12 $
 */
public class XmlRecordSourceGui extends RecordSourceGui implements Enable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(XmlRecordSourceGui.class);
	private static String RELATIVE = MessageUtil.m.formatMessage("io.common.gui.source.file.relative");
	private static String ABSOLUTE = MessageUtil.m.formatMessage("io.common.gui.source.file.absolute");
	private JLabel sourceFileNameLabel;
	private JLabel xmlFileNameLabel;
	private JTextField xmlFileName;
	private JButton browseButton;
	private JLabel xmlFileRelativeLabel;
	private JComboBox xmlFileRelativeBox;

	public XmlRecordSourceGui(ModelMaker parent, RecordSource s) {
		super(parent, MessageUtil.m.formatMessage("io.xml.gui.record.label"));
		init(s);
	}

	public void show() {
		setFields();
		setEnabledness();
		super.show();
	}

	public void setFields() {
		if (source != null) {
			XmlRecordSource s = (XmlRecordSource) source;
			sourceFileName.setText(s.getFileName());
			xmlFileName.setText(s.getXmlFileName());
			if (s.getRawXmlFileName() != null &&
				FileUtilities.isFileAbsolute(s.getRawXmlFileName())) {
				xmlFileRelativeBox.setSelectedItem(ABSOLUTE);	
			} else {
				xmlFileRelativeBox.setSelectedItem(RELATIVE);
			}
		}
	}

	public void setEnabledness() {
		boolean ok = xmlFileName.getText().length() > 0 && sourceFileName.getText().length() > 0;
		okayButton.setEnabled(ok);
	}

	public void buildSource() {
		XmlRecordSource xmlSource = (XmlRecordSource) source;
		xmlSource.setFileName(getSourceFileName());
		xmlSource.setRawXmlFileName(getSaveXmlFileName());
	}

	private String getSaveXmlFileName() {
		if (xmlFileRelativeBox.getSelectedItem().equals(ABSOLUTE)) {
			return getAbsoluteXmlFileName();		
		} else {
			File rel = new File(sourceFileName.getText().trim()).getAbsoluteFile().getParentFile();
			return FileUtilities.getRelativeFile(rel, getAbsoluteXmlFileName()).toString();
		}
	}

	private String getAbsoluteXmlFileName() {
		File rel = new File(sourceFileName.getText().trim()).getAbsoluteFile().getParentFile();
		return FileUtilities.getAbsoluteFile(rel, xmlFileName.getText().trim()).toString();
	}

	/**
	 * Executed by the superclass constructor to build the panel.
	 */
	public void buildContent() {
		sourceFileNameLabel = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.source.name"));
		sourceFileName = new JTextField(35);
		sourceFileBrowseButton = new JButton(MessageUtil.m.formatMessage("browse.elipsis"));

		xmlFileNameLabel = new JLabel(MessageUtil.m.formatMessage("io.xml.guilsource.file"));
		xmlFileName = new JTextField(10);
		browseButton = new JButton(MessageUtil.m.formatMessage("browse.elipsis"));

		xmlFileRelativeLabel = new JLabel(MessageUtil.m.formatMessage("io.common.gui.save.source.file.as"));
		xmlFileRelativeBox = new JComboBox();
		xmlFileRelativeBox.addItem(RELATIVE);
		xmlFileRelativeBox.addItem(ABSOLUTE);

		okayButton = new JButton(MessageUtil.m.formatMessage("ok"));
		cancelButton = new JButton(MessageUtil.m.formatMessage("cancel"));

		layoutContent();
	}

	public void addContentListeners() {
		super.addContentListeners();

		//sourceFileBrowseButton
		sourceFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File f = FileChooserFactory.selectRsFile(parent);
				if (f != null) {
					sourceFileName.setText(f.getAbsolutePath());
				}
			}
		});

		//browsebutton
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File f = FileChooserFactory.selectXmlFile(parent);
				if (f != null) {
					xmlFileName.setText(f.getAbsolutePath());
				}
			}
		});

		EnablednessGuard dl = new EnablednessGuard(this);
		sourceFileName.getDocument().addDocumentListener(dl);
		xmlFileName.getDocument().addDocumentListener(dl);
		
		JavaHelpUtils.enableHelpKey(this, "io.gui.xml.rs");
	}

	protected void generate() {
	}

	private void layoutContent() {
		//Layout content
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0};
		content.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//row 0........................................
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		content.add(sourceFileNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(sourceFileName, c);
		c.gridwidth = 1;
		c.gridx = 3;
		content.add(sourceFileBrowseButton, c);

		//row 1........................................
		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(xmlFileNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(xmlFileName, c);
		c.gridwidth = 1;
		c.gridx = 3;
		content.add(browseButton, c);

		c.gridy = 2;
		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(xmlFileRelativeLabel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		content.add(xmlFileRelativeBox, c);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 3;
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(okayButton, c);
		c.gridx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(cancelButton, c);
	}
}
