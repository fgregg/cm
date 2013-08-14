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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ProbabilityModel;
import com.choicemaker.cm.core.ml.none.None;
import com.choicemaker.cm.core.util.FileUtilities;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.util.OperationFailedException;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
/**
 * Description
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:44:54 $
 */
public class ModelBuilderDialog extends JDialog implements Enable {
	private static final String ABSOLUTE = 
		MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.cluefile.absolute");
	private static final String RELATIVE = 
		MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.cluefile.relative");
	
	private static Logger logger = Logger.getLogger(ModelBuilderDialog.class);
	private ModelMaker parent;
	private JPanel content;
	private JLabel modelFileName;
	private JTextField modelFileNameField;
	private JButton modelFileNameBrowseButton;
	private JLabel cluesFileName;
	private JTextField cluesFileNameField;
	private JButton cluesFileNameBrowseButton;
	private JLabel cluesRelativeLabel;
	private JComboBox cluesRelativeBox;
	private JLabel useAnt;
	private JCheckBox useAntCheckBox;
	private JLabel antCommand;
	private JTextField antCommandField;
	private JButton buildButton;
	private JButton cancelButton;
	private boolean isNewModel;
	private String oldName;

	public ModelBuilderDialog(ModelMaker g) {
		super(g, MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.label"), true);
		parent = g;
		isNewModel = true;
		buildContent();
		layoutContent();
		addContentListeners();
		setContentPane(content);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
	}

	public void modifyModel(IProbabilityModel pm) {
		isNewModel = false;
		oldName = pm.getFileName();
		modelFileNameField.setText(oldName);
		cluesFileNameField.setText(pm.getClueFileName());
		if (FileUtilities.isFileAbsolute(pm.getRawClueFileName())) {
			cluesRelativeBox.setSelectedItem(ABSOLUTE);	
		} else {
			cluesRelativeBox.setSelectedItem(RELATIVE);	
		}
		useAntCheckBox.setSelected(pm.isUseAnt());
		antCommandField.setText(pm.getAntCommand());
		setEnabledness();
	}

	public void newModel() {
		isNewModel = true;
		setEnabledness();
	}

	public void setEnabledness() {
		buildButton.setEnabled(modelFileNameField.getText().length() > 0 && cluesFileNameField.getText().length() > 0);
		antCommandField.setEnabled(useAntCheckBox.isSelected());
	}

	private boolean buildModel() {
		String modelFileName = modelFileNameField.getText().trim();
		if (!modelFileName.endsWith("." + Constants.MODEL_EXTENSION)) {
			modelFileName += "." + Constants.MODEL_EXTENSION;
		}
		if ((isNewModel || !modelFileName.equals(oldName)) && new File(modelFileName).exists()) {
			if (JOptionPane
				.showConfirmDialog(
					this,
					MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.replace", modelFileName),
					MessageUtil.m.formatMessage("confirm"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE)
				== JOptionPane.YES_OPTION) {
			} else {
				return false;
			}
		}
		
		String cluesFileName = cluesFileNameField.getText().trim();
		if (!cluesFileName.endsWith("." + Constants.CLUES_EXTENSION)) {
			cluesFileName += "." + Constants.CLUES_EXTENSION;	
		}

		// get an absolute file
		File cf = new File(cluesFileName);
		if (!cf.isAbsolute()) {
			File rel = new File(modelFileName).getAbsoluteFile().getParentFile();
			cluesFileName = FileUtilities.getAbsoluteFile(rel, cluesFileName).toString();
		}
		
		// adjust for the user's save preference.
		if (cluesRelativeBox.getSelectedItem().equals(RELATIVE)) {
			File rel = new File(modelFileName).getAbsoluteFile().getParentFile();
			cluesFileName = FileUtilities.getRelativeFile(rel, cluesFileName).toString();	
		}
		
		IProbabilityModel pm = new ProbabilityModel(modelFileName, cluesFileName);
		pm.setUseAnt(useAntCheckBox.isSelected());
		pm.setAntCommand(antCommandField.getText());
		pm.setMachineLearner(new None());
		boolean success = parent.buildProbabilityModel(pm);
		if (success) {
			try {
				parent.saveProbabilityModel(pm);
				parent.setProbabilityModel(pm);
				return true;
			} catch (OperationFailedException ex) {
				logger.error(new LoggingObject("CM-100501", pm.getName()), ex);
				return false;
			}
		} else {
			return false;
		}
	}

	private void addContentListeners() {
		//buildButton
		buildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buildModel()) {
					dispose();
				}
			}
		});

		//modelsFileNameBrowseButton
		modelFileNameBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = FileChooserFactory.selectModelFile(parent);
				if (file != null) {
					modelFileNameField.setText(file.getAbsolutePath());
				}
			}
		});

		//cluesFileNameBrowseButton
		cluesFileNameBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = FileChooserFactory.selectCluesFile(parent);
				if (file != null) {
					cluesFileNameField.setText(file.getAbsolutePath());
				}
			}
		});

		//cancelButton
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		EnablednessGuard dl = new EnablednessGuard(this);
		modelFileNameField.getDocument().addDocumentListener(dl);
		cluesFileNameField.getDocument().addDocumentListener(dl);

		useAntCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledness();
			}
		});

		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.modelbuilder");
	}

	private void buildContent() {
		content = new JPanel();
		modelFileName = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.name"));
		modelFileNameField = new JTextField(35);
		modelFileNameBrowseButton = new JButton(MessageUtil.m.formatMessage("browse.elipsis"));
		cluesFileName = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.cluefile"));
		cluesFileNameField = new JTextField(35);
		cluesFileNameBrowseButton = new JButton(MessageUtil.m.formatMessage("browse.elipsis"));
		cluesRelativeLabel = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.save.cluefile.as"));
		cluesRelativeBox = new JComboBox();
		cluesRelativeBox.addItem(RELATIVE);
		cluesRelativeBox.addItem(ABSOLUTE);
		useAnt = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.useant"));
		useAntCheckBox = new JCheckBox();
		useAntCheckBox.setSelected(false);
		antCommand = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.antcommand"));
		antCommandField = new JTextField(35);
		antCommandField.setText("-Dnomain=true compile");
		buildButton = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.model.builder.build"));
		cancelButton = new JButton(MessageUtil.m.formatMessage("cancel"));
	}

	private void layoutContent() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {0, 1, 0, 0};
		content.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 10);
		c.fill = GridBagConstraints.NONE;

		//Row 0 ........................................
		//modelName
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		content.add(modelFileName, c);
		//modelNameField
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(modelFileNameField, c);
		c.gridwidth = 1;

		//modelFileNameBrowseButton
		c.gridx = 3;
		c.weightx = 0;
		layout.setConstraints(modelFileNameBrowseButton, c);
		content.add(modelFileNameBrowseButton);

		//Row 1 ........................................
		//cluesFileName
		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(cluesFileName, c);
		//cluesFileNameField
		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(cluesFileNameField, c);
		content.add(cluesFileNameField);
		c.gridwidth = 1;

		//cluesFileNameBrowseButton
		c.gridx = 3;
		layout.setConstraints(cluesFileNameBrowseButton, c);
		content.add(cluesFileNameBrowseButton);

		//Row 2 ........................................
		c.gridy = 2;
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		content.add(cluesRelativeLabel, c);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		content.add(cluesRelativeBox, c);

		//Row 3 ........................................
		c.gridy = 3;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(useAnt, c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		content.add(useAntCheckBox, c);

		//Row 4 ........................................
		c.gridy = 4;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(antCommand, c);

		c.gridx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(antCommandField, c);
		c.gridwidth = 1;

		//Row 4 ........................................
		c.gridy = 5;
		c.gridx = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		layout.setConstraints(buildButton, c);
		content.add(buildButton);

		c.gridx = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(cancelButton, c);
		content.add(cancelButton);
	}
}
