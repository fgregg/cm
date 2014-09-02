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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.MarkedRecordPairBinder;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.flatfile.base.FlatFileMarkedRecordPairSink;
import com.choicemaker.cm.io.flatfile.base.FlatFileMarkedRecordPairSinkFactory;
import com.choicemaker.cm.io.flatfile.base.FlatFileMarkedRecordPairSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.MarkedRecordPairSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
import com.choicemaker.util.FileUtilities;

/**
 * The MRPSGui associated the FlatFileMarkedRecordPairSource.
 * An objects of this class would be created by the
 * FlatFileMarkedRecordPairSourceGuiFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build FlatFileMarkedRecordPairSources.
 *
 * @author  Martin Buechi
 * @author  S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/28 09:11:07 $
 */
public class FlatFileMarkedRecordPairSourceGui extends MarkedRecordPairSourceGui implements Enable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(FlatFileMarkedRecordPairSourceGui.class.getName());
	private JLabel sourceNameLabel;
	private JLabel fileNameLabel;
	private JTextField fileName;
	private JButton browseButton;
	private JLabel fileRelative;
	private JComboBox fileRelativeBox;
	private JLabel multiFileLabel;
	private JCheckBox multiFile;
	private JLabel singleLineLabel;
	private JCheckBox singleLine;
	private JLabel fixedLengthLabel;
	private JCheckBox fixedLength;
	private JLabel separatorLabel;
	private JComboBox separatorList;
	private JTextField separator;
	private JLabel taggedLabel;
	private JCheckBox tagged;
	private static String RELATIVE = ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.source.file.relative");
	private static String ABSOLUTE = ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.source.file.absolute");
	private static String GENERATE_MODE_LABEL = "Generate new source file";
	private static String CREATE_MODE_LABEL = "Use existing source file";
	private static Dimension CREATE_DIMENSION = new Dimension(500, 300);
	private static Dimension GENERATE_DIMENSION = new Dimension(500, 500);
	private JComponent[] generateComponents;
	private JButton modeButton;

	private JLabel sourcesListLabel;
	private JList sourcesList;
	private JScrollPane sourcesListScrollPane;
	private JButton addButton;
	private JButton removeButton;
	private JLabel removeCharLabel;
	private JCheckBox removeChar;
	private JLabel distributeOverLabel;
	private JTextField distributeOver;
	private JLabel maxPairsPerFileLabel;
	private JTextField maxPairsPerFile;

	private String saveFileN;
	private String absFileN;
	private String extension;
	private boolean save;

	public FlatFileMarkedRecordPairSourceGui(ModelMaker parent, MarkedRecordPairSource s, boolean save) {
		super(parent, ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.label"));
		this.save = save;
		init(s);
	}

	public void show() {
		setFields();
		setEnabledness();
		Point pt = getLocation();
		pt.y = Math.max(0, pt.y - 100);
		setLocation(pt);
		super.setVisible(true);
	}

	public void setFields() {
		if (!save) {
			distributeOver.setText("1");
			maxPairsPerFile.setText("0");
		}
		sourceFileName.setText(source.getFileName());
		FlatFileMarkedRecordPairSource s = (FlatFileMarkedRecordPairSource) source;
		fileName.setText(s.getFileNamePrefix() + s.getFileNameSuffix());
		if (s.getRawFileNamePrefix() != null &&
			FileUtilities.isFileAbsolute(s.getRawFileNamePrefix())) {
			fileRelativeBox.setSelectedItem(ABSOLUTE);
		} else {
			fileRelativeBox.setSelectedItem(RELATIVE);
		}
		multiFile.setSelected(s.isMultiFile());
		singleLine.setSelected(s.isSingleLine());
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
		String fn = getSaveFileName();
		int pos = fn.lastIndexOf('.');
		if (pos >= 0) {
			absFileN = fn.substring(0, pos);
			extension = fn.substring(pos, fn.length());
		} else {
			absFileN = fn;
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
				&& (fixedLength.isSelected()
					|| ((Separator) separatorList.getSelectedItem()).isDefined(separator.getText()));
		if (mode == CREATE) {
			okayButton.setEnabled(ok);
		} else {
			boolean generate = ok && sourcesList.getModel().getSize() > 0 && parent.haveProbabilityModel();
			try {
				int d = Integer.parseInt(distributeOver.getText());
				int s = Integer.parseInt(maxPairsPerFile.getText());
				generate &= d > 0 && s >= 0;
			} catch (NumberFormatException ex) {
				generate = false;
			}
			okayButton.setEnabled(generate);
		}
		separatorList.setEnabled(!fixedLength.isSelected());
		separator.setEnabled(!fixedLength.isSelected() && ((Separator) separatorList.getSelectedItem()).isOther());
		if (!save) {
			removeChar.setEnabled(!fixedLength.isSelected());
		}
		multiFile.setEnabled(!singleLine.isSelected());
		singleLine.setEnabled(!multiFile.isSelected() && !tagged.isSelected());
		tagged.setEnabled(!singleLine.isSelected());
		if (!save) {
			removeButton.setEnabled(!sourcesList.isSelectionEmpty());
		}
	}

	public void buildSource() {
		FlatFileMarkedRecordPairSource ffSource = (FlatFileMarkedRecordPairSource) source;
		ffSource.setFileName(getSourceFileName());
		computeFileNameAndExtension();
		ffSource.setRawFileNamePrefix(saveFileN);
		ffSource.setFileNameSuffix(extension);
		ffSource.setMultiFile(multiFile.isSelected());
		ffSource.setSingleLine(singleLine.isSelected());
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
		singleLineLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.singleline"));
		singleLine = new JCheckBox();
		fixedLengthLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.fixedwidth"));
		fixedLength = new JCheckBox();
		separatorLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.delimiter"));
		separator = new JTextField(3);
		separatorList = new JComboBox(Separator.SEPARATORS);
		taggedLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.tagged"));
		tagged = new JCheckBox();

		okayButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("ok"));
		cancelButton = new JButton(ChoiceMakerCoreMessages.m.formatMessage("cancel"));

		if (!save) {
			modeButton = new JButton(GENERATE_MODE_LABEL);
			generateComponents = new JComponent[10];
			sourcesListLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.source.sources"));
			generateComponents[0] = sourcesListLabel;
			sourcesList = new JList(new DefaultListModel());
			sourcesListScrollPane = new JScrollPane();
			sourcesListScrollPane.getViewport().add(sourcesList);
			removeCharLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.flatfile.gui.remove.delimiter"));
			generateComponents[1] = removeCharLabel;
			removeChar = new JCheckBox();
			removeChar.setSelected(true);
			generateComponents[2] = removeChar;
			generateComponents[3] = sourcesListScrollPane;
			addButton = new JButton("Add...");
			generateComponents[4] = addButton;
			removeButton = new JButton("Remove");
			generateComponents[5] = removeButton;
			distributeOverLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.distribute.roundrobin"));
			generateComponents[6] = distributeOverLabel;
			distributeOver = new JTextField(5);
			generateComponents[7] = distributeOver;
			maxPairsPerFileLabel = new JLabel(ChoiceMakerCoreMessages.m.formatMessage("io.common.gui.distribute.maxpairsperfile"));
			generateComponents[8] = maxPairsPerFileLabel;
			maxPairsPerFile = new JTextField(5);
			generateComponents[9] = maxPairsPerFile;
		}
		layoutContent();
	}

	public void addContentListeners() {
		super.addContentListeners();
		EnablednessGuard dl = new EnablednessGuard(this);

		//sourceFileBrowseButton
		sourceFileBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File f = FileChooserFactory.selectMrpsFile(parent);
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
		singleLine.addItemListener(il);
		fixedLength.addItemListener(il);
		tagged.addItemListener(il);

		if (!save) {
			// removeButton
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					DefaultListModel m = (DefaultListModel) sourcesList.getModel();
					int[] si = sourcesList.getSelectedIndices();
					for (int i = si.length - 1; i >= 0; --i) {
						m.removeElementAt(si[i]);
					}
					setEnabledness();
				}
			});

			//addButton
			addButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					File[] fs = FileChooserFactory.selectMrpsFiles(parent);
					DefaultListModel m = (DefaultListModel) sourcesList.getModel();
					for (int i = 0; i < fs.length; ++i) {
						m.addElement(fs[i].getAbsolutePath());
					}
					setEnabledness();
				}
			});

			modeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					if (mode == CREATE) {
						mode = GENERATE;
						modeButton.setText(CREATE_MODE_LABEL);
						setSize(GENERATE_DIMENSION);
					} else {
						mode = CREATE;
						modeButton.setText(GENERATE_MODE_LABEL);
						setSize(CREATE_DIMENSION);
					}
					setGenerateComponentsVisibility();
					pack();
					setEnabledness();
				}
			});

			sourcesList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					setEnabledness();
				}
			});
			distributeOver.getDocument().addDocumentListener(dl);
			maxPairsPerFile.getDocument().addDocumentListener(dl);
		}

		sourceFileName.getDocument().addDocumentListener(dl);
		fileName.getDocument().addDocumentListener(dl);
		separator.getDocument().addDocumentListener(dl);
		separatorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "io.gui.flatfile.mrps");
	}

	protected void generate() {
		Object[] sources = ((DefaultListModel) sourcesList.getModel()).toArray();
		String[] sourceNames = new String[sources.length];
		System.arraycopy(sources, 0, sourceNames, 0, sources.length);
		try {
			computeFileNameAndExtension();
			int d = Integer.parseInt(distributeOver.getText());
			int s = Integer.parseInt(maxPairsPerFile.getText());
			if (s == 0) {
				s = Integer.MAX_VALUE;
			}
			if (d == 1 && s == Integer.MAX_VALUE) {
				FlatFileMarkedRecordPairSink sink =
					new FlatFileMarkedRecordPairSink(
						getSourceFileName(),
						absFileN,
						extension,
						multiFile.isSelected(),
						singleLine.isSelected(),
						fixedLength.isSelected(),
						separatorChar(),
						tagged.isSelected(),
						removeChar.isSelected(),
						parent.getProbabilityModel());
				MarkedRecordPairBinder.store(sourceNames, parent.getProbabilityModel(), sink);
				buildSource();
			} else {
				String fileNameBase = getSourceFileName();
				fileNameBase = fileNameBase.substring(0, fileNameBase.length() - Constants.MRPS_EXTENSION.length() - 1);
				FlatFileMarkedRecordPairSinkFactory sinkFactory =
					new FlatFileMarkedRecordPairSinkFactory(
						fileNameBase,
						absFileN,
						extension,
						multiFile.isSelected(),
						singleLine.isSelected(),
						fixedLength.isSelected(),
						separatorChar(),
						tagged.isSelected(),
						removeChar.isSelected(),
						parent.getProbabilityModel());
				MarkedRecordPairBinder.store(sourceNames, parent.getProbabilityModel(), sinkFactory, d, s);
				Source[] srcs = sinkFactory.getSources();
				source = srcs[0];
				for (int i = 1; i < srcs.length; ++i) {
					try {
						MarkedRecordPairSourceXmlConf.add((MarkedRecordPairSource) srcs[i]);
					} catch (XmlConfException ex) {
						logger.error(new LoggingObject("CM-020001", srcs[i]), ex);
					}
				}
			}
		} catch (XmlConfException ex) {
			logger.error(new LoggingObject("CM-020001"), ex);
			return;
		} catch (IOException ex) {
			logger.error(new LoggingObject("CM-020001"), ex);
			return;
		}
		dispose();
	}

	private char separatorChar() {
		return ((Separator) separatorList.getSelectedItem()).getSeparator(separator.getText());
	}

	private void layoutContent() {
		//Layout content
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWeights = new double[] { 0, 0, 1, 0, 0 };
		layout.rowWeights = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 };
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//row 0........................................
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		if(!save) {
			sourceNameLabel.setPreferredSize(removeCharLabel.getPreferredSize());
		}
		content.add(sourceNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 3;
		content.add(sourceFileName, c);
		c.gridwidth = 1;
		c.gridx = 4;
		content.add(sourceFileBrowseButton, c);

		//row 1........................................
		c.gridy = 1;
		c.gridx = 0;
		content.add(fileNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 3;
		content.add(fileName, c);
		c.gridwidth = 1;
		c.gridx = 4;
		content.add(browseButton, c);

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
		content.add(multiFileLabel, c);
		c.gridx = 1;
		content.add(multiFile, c);

		//row 3........................................
		c.gridy = 4;
		c.gridx = 0;
		content.add(singleLineLabel, c);
		c.gridx = 1;
		content.add(singleLine, c);

		//row 4........................................
		c.gridy = 5;
		c.gridx = 0;
		content.add(fixedLengthLabel, c);
		c.gridx = 1;
		content.add(fixedLength, c);

		//row 5........................................
		c.gridy = 6;
		c.gridx = 0;
		content.add(separatorLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		content.add(separatorList, c);
		c.gridx = 2;
		content.add(separator, c);

		//row 6........................................
		c.gridy = 7;
		c.gridx = 0;
		content.add(taggedLabel, c);
		c.gridx = 1;
		content.add(tagged, c);

		c.gridy = 8;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		if (!save) {
			content.add(modeButton, c);
		}
		c.gridx = 3;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(okayButton, c);
		c.gridx = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		content.add(cancelButton, c);

		if (!save) {
			//row 8--------------------------
			c.gridy = 9;
			c.gridx = 0;
			content.add(sourcesListLabel, c);

			//row 9--------------------------
			c.gridy = 10;
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 3;
			c.gridheight = 3;
			content.add(sourcesListScrollPane, c);
			c.gridwidth = 1;
			c.gridheight = 1;
			c.fill = GridBagConstraints.HORIZONTAL;

			c.gridx = 3;
			content.add(addButton, c);

			//row 2
			c.gridy = 11;
			content.add(removeButton, c);

			//row 10--------------------------
			c.gridy = 13;
			c.gridx = 0;
			content.add(removeCharLabel, c);
			c.gridx = 1;
			content.add(removeChar, c);

			//row 11--------------------------
			c.gridy = 14;
			c.gridx = 0;
			content.add(distributeOverLabel, c);
			c.gridx = 1;
			c.fill = GridBagConstraints.NONE;
			content.add(distributeOver, c);

			//row 12--------------------------
			c.gridy = 15;
			c.gridx = 0;
			content.add(maxPairsPerFileLabel, c);
			c.gridx = 1;
			content.add(maxPairsPerFile, c);

			setGenerateComponentsVisibility();
		}
	}

	private void setGenerateComponentsVisibility() {
		boolean visible = mode == GENERATE;
		for (int i = 0; i < generateComponents.length; ++i) {
			generateComponents[i].setVisible(visible);
		}
	}
}
