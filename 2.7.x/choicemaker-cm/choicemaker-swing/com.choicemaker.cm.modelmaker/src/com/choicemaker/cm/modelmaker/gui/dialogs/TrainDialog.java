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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ml.MachineLearner;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.gui.utils.JavaHelpUtils;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.hooks.TrainDialogPlugin;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactories;
import com.choicemaker.cm.modelmaker.gui.ml.MlGuiFactory;
import com.choicemaker.cm.modelmaker.gui.utils.Enable;
import com.choicemaker.cm.modelmaker.gui.utils.EnablednessGuard;
/**
 * Description
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 12:50:36 $
 */
public class TrainDialog extends JDialog implements Enable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(TrainDialog.class);
	private static int BASE_HEIGHT = 220;
	private static int MIN_WIDTH = 330;
	private ModelMaker parent;
	private JPanel content;
	private JButton trainButton;
	private JButton cancelButton;
	private JLabel firingThreshold;
	private JTextField firingThresholdField;
	private JLabel recompileLabel;
	private JCheckBox recompile;
	private JLabel enableCluesLabel;
	private JCheckBox enableClues;
	private JLabel enableRulesLabel;
	private JCheckBox enableRules;
	private JPanel trainDialogPluginContainer;
	private TrainDialogPlugin trainDialogPlugin;
	private JComboBox mlc;
	private MachineLearner ml;
	private boolean setting;
	private boolean andTest;

	public TrainDialog(ModelMaker parent, boolean andTest) {
		super(parent, MessageUtil.m.formatMessage(andTest ? "train.gui.modelmaker.dialog.trainandtest.label" : "train.gui.modelmaker.dialog.train.label"), true);
		this.parent = parent;
		this.andTest = andTest;
		buildContent();
		layoutContent();
		addContentListeners();
		setContentPane(content);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		Point pt = getLocation();
		pt.y = Math.max(10, pt.x - 200);
		setLocation(pt);
	}

	public void show() {
		IProbabilityModel model = parent.getProbabilityModel();
		firingThresholdField.setText(String.valueOf(model.getFiringThreshold()));
		enableClues.setSelected(model.isEnableAllCluesBeforeTraining());
		enableRules.setSelected(model.isEnableAllRulesBeforeTraining());
		ml = model.getMachineLearner();
		if (ml.canUse(parent.getProbabilityModel().getClueSet())) {
			setMachineLearner();
		} else {
			mlc.setSelectedIndex(0);
		}
		setEnabledness();
		super.show();
	}

	private void setMachineLearner() {
		setting = true;
		trainDialogPluginContainer.removeAll();
		trainDialogPlugin = MlGuiFactories.getGui(ml).getTrainDialogPlugin(ml);
		trainDialogPlugin.setTrainDialog(this);
		//trainDialogPlugin.init(parent.getProbabilityModel());
		trainDialogPluginContainer.add(trainDialogPlugin, BorderLayout.CENTER);
		pack();
		mlc.setSelectedItem(MlGuiFactories.getGui(ml));
		setting = false;
		setEnabledness();
	}

	public void setEnabledness() {
		int ni = 0;
		try {
			ni = Integer.parseInt(firingThresholdField.getText());
		} catch (NumberFormatException ex) {
			// ignore
		}
		trainButton.setEnabled(trainDialogPlugin != null && trainDialogPlugin.isParametersValid() && ni >= 2);
	}

	private void addContentListeners() {
		//trainButton
		trainButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IProbabilityModel model = parent.getProbabilityModel();
					MachineLearner cml = model.getMachineLearner();
					if (ml != cml) {
						model.setMachineLearner(ml);
					}
					if (trainDialogPlugin.isParametersValid()) {
						trainDialogPlugin.set();
						boolean success =
							parent.train(
								recompile.isSelected(),
								enableClues.isSelected(),
								enableRules.isSelected(),
								Integer.parseInt(firingThresholdField.getText()),
								andTest);
						if (success) {
							dispose();
						}
					}
				} catch (NumberFormatException ex) {
					// ignore
				}
			}
		});

		//cancelButton
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		mlc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!setting) {
					ml = ((MlGuiFactory) mlc.getSelectedItem()).getMlInstance();
					setMachineLearner();
				}
			}
		});

		EnablednessGuard dl = new EnablednessGuard(this);
		firingThresholdField.getDocument().addDocumentListener(dl);
		
		JavaHelpUtils.enableHelpKey(this, "train.gui.dialog.train");
	}

	public void dispose() {
		ml = null;
		trainDialogPluginContainer.removeAll();
		super.dispose();
	}

	private void buildContent() {
		content = new JPanel();
		firingThreshold = new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.firingthreshold"));
		firingThresholdField = new JTextField(5);
		recompileLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.recompile.if.necessary"));
		recompile = new JCheckBox();
		recompile.setSelected(true);
		enableCluesLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.enable.all.clues"));
		enableClues = new JCheckBox();
		enableRulesLabel =
			new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.enable.all.rules"));
		enableRules = new JCheckBox();
		trainButton = new JButton(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.train"));
		cancelButton = new JButton(MessageUtil.m.formatMessage("cancel"));
		trainDialogPluginContainer = new JPanel(new BorderLayout());
		mlc = new JComboBox(new Vector(MlGuiFactories.getAllGuis()));
	}

	private void layoutContent() {
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 5, 5);

		//Row 0 ........................................
		//firingThreshold
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		content.add(firingThreshold, c);
		//firingThresholdField
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(firingThresholdField, c);

		//Row 1 ........................................
		//recompile
		c.gridy = 1;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(recompileLabel, c);
		//trainingIterationsField
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(recompile, c);

		//Row 2 ........................................
		//enable clues
		c.gridy = 2;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(enableCluesLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(enableClues, c);

		//Row 2 ........................................
		//enable clues
		c.gridy = 3;
		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		content.add(enableRulesLabel, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		content.add(enableRules, c);

		//Row 2 ........................................
		c.gridy = 4;
		c.gridx = 0;
		content.add(new JLabel(MessageUtil.m.formatMessage("train.gui.modelmaker.dialog.train.ml")), c);
		c.gridx = 1;
		content.add(mlc, c);

		//Row 4 ........................................
		//plugin
		c.gridy = 5;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		content.add(trainDialogPluginContainer, c);
		c.gridwidth = 1;

		JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 10));
		buttons.add(trainButton);
		buttons.add(cancelButton);
		c.gridy = 6;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		content.add(buttons, c);
	}
}
