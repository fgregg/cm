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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSink;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSinkFactory;
import com.choicemaker.cm.io.xml.base.XmlMarkedRecordPairSource;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.MarkedRecordPairSourceGui;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
import com.choicemaker.util.FileUtilities;

/**
 * The MRPSGui associated the XmlMarkedRecordPairSource.
 * An objects of this class would be created by the
 * XmlMarkedRecordPairSourceGuiFactory.  It is used
 * by the AbstractApplication so that users can easily configure
 * and build XmlMarkedRecordPairSources.
 *
 * @author  S. Yoakum-Stover
 * @author  Martin Buechi
 * @version $Revision: 1.3 $ $Date: 2010/03/28 09:15:52 $
 */
public class XmlMarkedRecordPairSourceGui extends MarkedRecordPairSourceGui implements Enable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(XmlMarkedRecordPairSourceGui.class);
	private static String GENERATE_MODE_LABEL = "Generate new source file";
	private static String CREATE_MODE_LABEL = "Use existing source file";
	private static String RELATIVE = MessageUtil.m.formatMessage("io.common.gui.source.file.relative");
	private static String ABSOLUTE = MessageUtil.m.formatMessage("io.common.gui.source.file.absolute");
	private static Dimension CREATE_DIMENSION = new Dimension(500, 140);
	private static Dimension GENERATE_DIMENSION = new Dimension(500, 400);
	private JComponent[] generateComponents;
	private JButton modeButton;
	private JLabel sourceFileNameLabel;
	private JLabel xmlFileNameLabel;
	private JTextField xmlFileName;
	private JButton browseButton;
	private JLabel xmlFileRelativeLabel;
	private JComboBox xmlFileRelativeBox;
	private JLabel sourcesListLabel;
	private JList sourcesList;
	private JScrollPane sourcesListScrollPane;
	private JButton addButton;
	private JButton removeButton;
	private JLabel distributeOverLabel;
	private JTextField distributeOver;
	private JLabel maxPairsPerFileLabel;
	private JTextField maxPairsPerFile;

	private String fileN;
	private String extension;
	private boolean save;

	public XmlMarkedRecordPairSourceGui(ModelMaker parent, MarkedRecordPairSource s, boolean save) {
		super(parent, MessageUtil.m.formatMessage("io.xml.gui.label"));
		this.save = save;
		init(s);
	}

	public void show() {
		setFields();
		setEnabledness();
		super.setVisible(true);
	}

	public void setFields() {
		if(!save) {
			distributeOver.setText("1");
			maxPairsPerFile.setText("0");
		}
		if (source != null) {
			XmlMarkedRecordPairSource s = (XmlMarkedRecordPairSource) source;
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

	private void computeFileNameAndExtension() {
		String fn = getSaveXmlFileName();
		int pos = fn.lastIndexOf('.');
		if (pos >= 0) {
			fileN = fn.substring(0, pos);
			extension = fn.substring(pos, fn.length());
		} else {
			fileN = fn;
			extension = "";
		}
	}

	public void setEnabledness() {
		boolean ok = xmlFileName.getText().length() > 0 && sourceFileName.getText().length() > 0;
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
		if(!save) {
			removeButton.setEnabled(!sourcesList.isSelectionEmpty());
		}
	}

	public void buildSource() {
		XmlMarkedRecordPairSource xmlSource = (XmlMarkedRecordPairSource) source;
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

		if (!save) {
			modeButton = new JButton(GENERATE_MODE_LABEL);
			generateComponents = new JComponent[8];
			sourcesListLabel = new JLabel(MessageUtil.m.formatMessage("io.common.gui.source.sources"));
			generateComponents[0] = sourcesListLabel;
			sourcesList = new JList(new DefaultListModel());
			sourcesListScrollPane = new JScrollPane();
			sourcesListScrollPane.getViewport().add(sourcesList);
			sourcesListScrollPane.setPreferredSize(new Dimension(50, 100));
			generateComponents[1] = sourcesListScrollPane;
			addButton = new JButton("Add...");
			generateComponents[2] = addButton;
			removeButton = new JButton("Remove");
			generateComponents[3] = removeButton;
			distributeOverLabel = new JLabel(MessageUtil.m.formatMessage("io.common.gui.distribute.roundrobin"));
			generateComponents[4] = distributeOverLabel;
			distributeOver = new JTextField(5);
			generateComponents[5] = distributeOver;
			maxPairsPerFileLabel = new JLabel(MessageUtil.m.formatMessage("io.common.gui.distribute.maxpairsperfile"));
			generateComponents[6] = maxPairsPerFileLabel;
			maxPairsPerFile = new JTextField(5);
			generateComponents[7] = maxPairsPerFile;
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
				File f = FileChooserFactory.selectXmlFile(parent);
				if (f != null) {
					xmlFileName.setText(f.getAbsolutePath());
				}
			}
		});

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
					//validate();
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
		xmlFileName.getDocument().addDocumentListener(dl);

		JavaHelpUtils.enableHelpKey(this, "io.gui.xml.mrps");
	}

	protected void generate() {
		Object[] sources = ((DefaultListModel) sourcesList.getModel()).toArray();
		String[] sourceNames = new String[sources.length];
		System.arraycopy(sources, 0, sourceNames, 0, sources.length);
		try {
			int d = Integer.parseInt(distributeOver.getText());
			int s = Integer.parseInt(maxPairsPerFile.getText());
			if (s == 0) {
				s = Integer.MAX_VALUE;
			}
			if (d == 1 && s == Integer.MAX_VALUE) {
				XmlMarkedRecordPairSink sink =
					new XmlMarkedRecordPairSink(
						getSourceFileName(),
						getSaveXmlFileName(),
						parent.getProbabilityModel());
				MarkedRecordPairBinder.store(sourceNames, parent.getProbabilityModel(), sink);
				buildSource();
			} else {
				computeFileNameAndExtension();
				String fileNameBase = getSourceFileName();
				fileNameBase = fileNameBase.substring(0, fileNameBase.length() - Constants.MRPS_EXTENSION.length() - 1);
				XmlMarkedRecordPairSinkFactory sinkFactory =
					new XmlMarkedRecordPairSinkFactory(fileNameBase, fileN, extension, parent.getProbabilityModel());
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

	private void layoutContent() {
		//Layout content
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		layout.columnWeights = new double[] {0, 1, 0};
		layout.rowWeights = new double[] {0, 0, 0, 0, 0, 0, 1, 0, 0};
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//row 0........................................
		c.gridy = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		if(!save) {
			sourceFileNameLabel.setPreferredSize(maxPairsPerFileLabel.getPreferredSize());
		}
		content.add(sourceFileNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(sourceFileName, c);
		c.gridwidth = 1;

		c.gridx = 3;
		layout.setConstraints(sourceFileBrowseButton, c);
		content.add(sourceFileBrowseButton);

		//row 1........................................
		c.gridy = 1;
		c.gridx = 0;
		content.add(xmlFileNameLabel, c);
		c.gridx = 1;
		c.gridwidth = 2;
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
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		if (!save) {
			content.add(modeButton, c);
		}
		c.gridx = 2;
		c.anchor = GridBagConstraints.EAST;
		content.add(okayButton, c);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(cancelButton, c);

		if (!save) {
			c.gridy = 4;
			c.gridx = 0;
			content.add(sourcesListLabel, c);

			//row 1--------------------------
			c.gridy = 5;
			c.fill = GridBagConstraints.BOTH;
			c.gridwidth = 3;
			c.gridheight = 3;
			content.add(sourcesListScrollPane, c);
			c.gridwidth = 1;
			c.gridheight = 1;

			c.gridx = 3;
			c.fill = GridBagConstraints.HORIZONTAL;
			content.add(addButton, c);

			c.gridy = 6;
			content.add(removeButton, c);


			//row 3--------------------------
			c.gridy = 8;
			c.gridx = 0;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			content.add(distributeOverLabel, c);
			c.gridx = 1;
			content.add(distributeOver, c);

			//row 4--------------------------
			c.gridy = 9;
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
