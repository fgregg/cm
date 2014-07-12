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
package com.choicemaker.cm.io.flatfile.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.flatfile.base.FlatFileRecordSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.RecordSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
import com.choicemaker.util.FileUtilities;

/**
 * The MRPSGui associated the FlatFileRecordSource.
 * An objects of this class would be created by the 
 * FlatFileRecordSourceGuiFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build FlatFileRecordSources.
 * 
 * @author  Martin Buechi
 * @author  S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:11:07 $
 */
public class FlatFileRecordSourceGui extends RecordSourceGui implements Enable {
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(FlatFileRecordSourceGui.class);
	private static String RELATIVE = ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.source.file.relative");
	private static String ABSOLUTE = ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.source.file.absolute");
	private JLabel sourceNameLabel;
	private JLabel fileNameLabel;
	private JTextField fileName;
	private JButton browseButton;
	private JLabel fileRelative;
	private JComboBox fileRelativeBox;
	private JLabel multiFileLabel;
	private JCheckBox multiFile;
	private JLabel fixedLengthLabel;
	private JCheckBox fixedLength;
	private JLabel separatorLabel;
	private JComboBox separatorList;
	private JTextField separator;
	private JLabel taggedLabel;
	private JCheckBox tagged;

	private String saveFileN;
//	private String absFileN;
	private String extension;


	public FlatFileRecordSourceGui(ModelMaker parent, RecordSource s) {
		super(parent, ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.record.label"));
		init(s);
	}

	public void show() {
		setFields();
		setEnabledness();
		super.setVisible(true);
	}

	public void setFields() {
		sourceFileName.setText(source.getFileName());
		FlatFileRecordSource s = (FlatFileRecordSource) source;
		fileName.setText(s.getFileNamePrefix() + s.getFileNameSuffix());
		if (s.getRawFileNamePrefix() != null &&
			FileUtilities.isFileAbsolute(s.getRawFileNamePrefix())) {
			fileRelativeBox.setSelectedItem(ABSOLUTE);	
		} else {
			fileRelativeBox.setSelectedItem(RELATIVE);
		}
		multiFile.setSelected(s.isMultiFile());
		fixedLength.setSelected(s.isFixedLength());
		tagged.setSelected(s.isTagged());
		char sep = s.getSeparator();
		Separator other = null;
		for (int i = 0; i < Separator.SEPARATORS.length; ++i) {
			Separator ss = Separator.SEPARATORS[i];
			if (ss.isOther()) {
				other = ss;
			} else if (ss.getSeparator(separator.getText()) == sep) {
				separatorList.setSelectedItem(ss);
				other = null;
				break;
			}
		}
		if (other != null) {
			separatorList.setSelectedItem(other);
			separator.setText(String.valueOf(sep));
		}
	}

	private void computeFileNameAndExtension() {
		String fn = getAbsoluteFileName();
		int pos = fn.lastIndexOf('.');
		if (pos >= 0) {
//			absFileN = fn.substring(0, pos);
			extension = fn.substring(pos, fn.length());
		} else {
//			absFileN = fn;
			extension = "";
		}
		fn = getSaveFileName();
		pos = fn.lastIndexOf('.');
		if (pos >= 0) {
			saveFileN = fn.substring(0, pos);	
		} else {
			saveFileN = fn;	
		}
	}

	public void setEnabledness() {
		boolean ok =
			fileName.getText().length() > 0
				&& (sourceFileName.getText().length() > 0 || multiFile.isSelected())
				&& (fixedLength.isSelected() || ((Separator) separatorList.getSelectedItem()).isDefined(separator.getText()));
		okayButton.setEnabled(ok);
		separatorList.setEnabled(!fixedLength.isSelected());
		separator.setEnabled(!fixedLength.isSelected() && ((Separator) separatorList.getSelectedItem()).isOther());
	}

	public void buildSource() {
		FlatFileRecordSource ffSource = (FlatFileRecordSource) source;
		ffSource.setFileName(getSourceFileName());
		computeFileNameAndExtension();
		ffSource.setRawFileNamePrefix(saveFileN);
		ffSource.setFileNameSuffix(extension);
		ffSource.setMultiFile(multiFile.isSelected());
		ffSource.setFixedLength(fixedLength.isSelected());
		ffSource.setSeparator(separatorChar());
		ffSource.setTagged(tagged.isSelected());
	}

	private String getSaveFileName() {
		if (fileRelativeBox.getSelectedItem().equals(ABSOLUTE)) {
			return getAbsoluteFileName();		
		} else {
			File rel = new File(sourceFileName.getText().trim()).getAbsoluteFile().getParentFile();
			return FileUtilities.getRelativeFile(rel, getAbsoluteFileName()).toString();
		}
	}

	private String getAbsoluteFileName() {
		File rel = new File(sourceFileName.getText().trim()).getAbsoluteFile().getParentFile();
		return FileUtilities.getAbsoluteFile(rel, fileName.getText().trim()).toString();
	}

	/**
	 * Executed by the superclass constructor to build the panel.
	 */
	public void buildContent() {
		sourceNameLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.source.name"));
		sourceFileName = new JTextField(35);
		sourceFileBrowseButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("browse.elipsis"));

		fileNameLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.source.file"));
		fileName = new JTextField(10);
		browseButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("browse.elipsis"));

		fileRelative = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.save.source.file.as"));
		fileRelativeBox = new JComboBox();
		fileRelativeBox.addItem(RELATIVE);
		fileRelativeBox.addItem(ABSOLUTE);

		multiFileLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.multifile"));
		multiFile = new JCheckBox();
		fixedLengthLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.fixedwidth"));
		fixedLength = new JCheckBox();
		separatorLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.delimiter"));
		separator = new JTextField(3);
		separator.setPreferredSize(separator.getPreferredSize());
		separatorList = new JComboBox(Separator.SEPARATORS);
		taggedLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.tagged"));
		tagged = new JCheckBox();

		okayButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
		cancelButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));

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
				File file = FileChooserFactory.selectFlatFile(parent);
				if (file != null) {
					fileName.setText(file.getAbsolutePath());
				}
			}
		});

		ItemListener il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setEnabledness();
			}
		};
		multiFile.addItemListener(il);
		fixedLength.addItemListener(il);
		tagged.addItemListener(il);

		EnablednessGuard dl = new EnablednessGuard(this);
		sourceFileName.getDocument().addDocumentListener(dl);
		fileName.getDocument().addDocumentListener(dl);
		separator.getDocument().addDocumentListener(dl);
		separatorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "io.gui.flatfile.rs");
	}

	private char separatorChar() {
		return ((Separator) separatorList.getSelectedItem()).getSeparator(separator.getText());
	}

	private void layoutContent() {
		//Layout content
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWeights = new double[] { 0, 0, 1, 0, 0 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//row 0........................................
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(sourceNameLabel, c);
		content.add(sourceNameLabel);
		c.gridx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(sourceFileName, c);
		content.add(sourceFileName);
		c.gridwidth = 1;
		c.gridx = 4;
		c.weightx = 0;
		layout.setConstraints(sourceFileBrowseButton, c);
		content.add(sourceFileBrowseButton);

		//row 1........................................
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(fileNameLabel, c);
		content.add(fileNameLabel);
		c.gridx = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(fileName, c);
		content.add(fileName);
		c.gridwidth = 1;
		c.gridx = 4;
		c.weightx = 0;
		layout.setConstraints(browseButton, c);
		content.add(browseButton);

		c.gridy = 2;
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(fileRelative, c);
		c.gridx = 3;
		c.anchor = GridBagConstraints.WEST;
		content.add(fileRelativeBox, c);
		c.fill = GridBagConstraints.HORIZONTAL;

		//row 2........................................
		c.gridy = 3;
		c.gridx = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(multiFileLabel, c);
		content.add(multiFileLabel);
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(multiFile, c);
		content.add(multiFile);

		//row 4........................................
		c.gridwidth = 1;
		c.gridy = 4;
		c.gridx = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(fixedLengthLabel, c);
		content.add(fixedLengthLabel);
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(fixedLength, c);
		content.add(fixedLength);

		//row 5........................................
		c.gridwidth = 1;
		c.gridy = 6;
		c.gridx = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(separatorLabel, c);
		content.add(separatorLabel);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		content.add(separatorList, c);
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		layout.setConstraints(separator, c);
		content.add(separator);
		c.fill = GridBagConstraints.HORIZONTAL;

		//row 6........................................
		c.gridy = 7;
		c.gridx = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(taggedLabel, c);
		content.add(taggedLabel);
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(tagged, c);
		content.add(tagged);

		c.gridy = 8;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 3;
		content.add(okayButton, c);
		c.gridx = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(cancelButton, c);
	}
}
