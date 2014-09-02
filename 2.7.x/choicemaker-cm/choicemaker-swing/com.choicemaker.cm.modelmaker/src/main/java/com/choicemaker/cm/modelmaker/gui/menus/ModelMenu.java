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
package com.choicemaker.cm.modelmaker.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.util.logging.Logger;

import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.OperationFailedException;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.xmlconf.GeneratorXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.ExportClueTableDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.ModelBuilderDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.TrainDialog;
import com.choicemaker.util.FileUtilities;

/**
 * The menu from which a MarkedRecordPairModel is selected.  
 * 
 * @author  S. Yoakum-Stover
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:12:09 $
 */
public class ModelMenu extends LastUsedMenu {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ModelMenu.class.getName());

	private ModelMaker parent;

	private static final String MODEL_MENU = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model");

	public ModelMenu(ModelMaker g) {
		super(MODEL_MENU, "models", 10);
		this.setMnemonic(KeyEvent.VK_M);
		parent = g;
		buildMenu();
		addListeners();
	}

	private abstract class ModelAction extends AbstractAction implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		private boolean dependsModel;
		private boolean dependsSource;
		private boolean licensed;

		ModelAction(String name, Icon icon, boolean dependsModel, boolean dependsSource, boolean licensed) {
			super(name, icon);
			this.dependsModel = dependsModel;
			this.dependsSource = dependsSource;
			this.licensed = licensed;
			if (dependsModel || dependsSource) {
				parent.addPropertyChangeListener(this);
			}
			doSetEnabled();
		}

		protected boolean getEnabled() {
			return licensed
				&& (!dependsModel || parent.haveProbabilityModel())
				&& (!dependsSource || parent.haveMarkedRecordPairSource());
		}

		protected void doSetEnabled() {
			setEnabled(getEnabled());
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (dependsSource
				&& propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE
				|| dependsModel
				&& propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				doSetEnabled();
			}
		}
	}

	private class EvaluateAction extends ModelAction {
		private static final long serialVersionUID = 1L;

		EvaluateAction() {
			super(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.evaluate"),
				new ImageIcon(ModelMaker.class.getResource("images/evaluate.gif")),
				true,
				true,
				true);
			parent.getProbabilityModelEventMultiplexer().addPropertyChangeListener(this);
		}
		public void actionPerformed(ActionEvent e) {
			parent.evaluateClues();
		}

		protected boolean getEnabled() {
			return super.getEnabled() && parent.getProbabilityModel().canEvaluate();
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (ImmutableProbabilityModel.MACHINE_LEARNER == propertyName
				|| null == propertyName) {
				doSetEnabled();
			} else {
				super.propertyChange(e);
			}
		}
	};

	public void buildMenu() {
		// New
		Action newAction = new ModelAction(ChoiceMakerCoreMessages.m.formatMessage("new.elipsis"), null, false, false, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				ModelBuilderDialog builder = new ModelBuilderDialog(parent);
				builder.newModel();
				builder.setVisible(true);
			}
		};
		JMenuItem newItem = add(newAction);
		newItem.setIcon(null);
		newItem.setMnemonic('n');
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		// Open
		Action openAction = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("open.elipsis")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File file = FileChooserFactory.selectModelFile(parent);
				if (file != null) {
					open(file.getAbsolutePath());
				}
			}
		};
		JMenuItem openItem = add(openAction);
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		addSeparator();

		// Info
		ImageIcon infoIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/info.gif"));
		Action infoAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.info"),
				infoIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.postProbabilityModelInfo();
			}
		};
		JMenuItem infoItem = add(infoAction);
		infoItem.setIcon(null);
		infoItem.setMnemonic(KeyEvent.VK_I);
		infoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		//	JButton infoButton = parent.getToolBar().add(infoAction);
		//infoButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("info"));

		// Edit
		Action editAction = new ModelAction(ChoiceMakerCoreMessages.m.formatMessage("edit.elipsis"), null, true, false, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				ModelBuilderDialog builder = new ModelBuilderDialog(parent);
				IProbabilityModel pm = parent.getProbabilityModel();
				builder.modifyModel(pm);
				builder.setVisible(true);
			}
		};
		JMenuItem editItem = add(editAction);
		editItem.setIcon(null);
		editItem.setMnemonic('m');
		editItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

		addSeparator();

		// Rebuild
		ImageIcon buildIcon = new ImageIcon(ModelMaker.class.getResource("images/build.gif"));
		Action buildAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.rebuild"),
				buildIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.buildProbabilityModel(parent.getProbabilityModel());
			}
		};
		JMenuItem buildItem = add(buildAction);
		buildItem.setIcon(null);
		buildItem.setMnemonic(KeyEvent.VK_B);
		buildItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		JButton buildButton = parent.getToolBar().add(buildAction);
		buildButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.rebuild.tooltip"));

		// Reload
		Action reloadAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.reload"),
				null,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.reloadProbabilityModel();
			}
		};
		JMenuItem reloadItem = add(reloadAction);
		reloadItem.setIcon(null);
		reloadItem.setMnemonic(KeyEvent.VK_R);
		reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

		// deleteGenerated
		Action deleteGeneratedAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.deletegenerated"),
				null,
				false,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File f = new File(GeneratorXmlConf.getCodeRoot()).getAbsoluteFile();
				if(f.exists()) {
					FileUtilities.removeDir(f);
				}
			}
		};
		JMenuItem deleteGeneratedItem = add(deleteGeneratedAction);
		deleteGeneratedItem.setIcon(null);

		addSeparator();

		// Enable all clues
		ImageIcon enableCluesIcon = new ImageIcon(ModelMaker.class.getResource("images/enableClues.gif"));
		Action enableCluesAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.enable.clues"),
				enableCluesIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setAllCluesOrRules(ModelMaker.CLUES, true);
			}
		};
		JMenuItem enableCluesItem = add(enableCluesAction);
		enableCluesItem.setIcon(null);
		enableCluesItem.setMnemonic(KeyEvent.VK_E);
		enableCluesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		JButton enableCluesButton = parent.getToolBar().add(enableCluesAction);
		enableCluesButton.setToolTipText(
			ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.enable.clues.tooltip"));

		// Disable all clues
		ImageIcon disableCluesIcon = new ImageIcon(ModelMaker.class.getResource("images/disableClues.gif"));
		Action disableCluesAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.disable.clues"),
				disableCluesIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setAllCluesOrRules(ModelMaker.CLUES, false);
			}
		};
		JMenuItem disableCluesItem = add(disableCluesAction);
		disableCluesItem.setIcon(null);
		disableCluesItem.setMnemonic(KeyEvent.VK_D);
		disableCluesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		JButton disableCluesButton = parent.getToolBar().add(disableCluesAction);
		disableCluesButton.setToolTipText(
			ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.disable.clues.tooltip"));

		// Enable all rules
		ImageIcon enableRulesIcon = new ImageIcon(ModelMaker.class.getResource("images/enableRules.gif"));
		Action enableRulesAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.enable.rules"),
				enableRulesIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setAllCluesOrRules(ModelMaker.RULES, true);
			}
		};
		JMenuItem enableRulesItem = add(enableRulesAction);
		enableRulesItem.setIcon(null);
		enableRulesItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		JButton enableRulesButton = parent.getToolBar().add(enableRulesAction);
		enableRulesButton.setToolTipText(
			ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.enable.rules.tooltip"));

		// Disable all rules
		ImageIcon disableRulesIcon = new ImageIcon(ModelMaker.class.getResource("images/disableRules.gif"));
		Action disableRulesAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.disable.rules"),
				disableRulesIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setAllCluesOrRules(ModelMaker.RULES, false);
			}
		};
		JMenuItem disableRulesItem = add(disableRulesAction);
		disableRulesItem.setIcon(null);
		disableRulesItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		JButton disableRulesButton = parent.getToolBar().add(disableRulesAction);
		disableRulesButton.setToolTipText(
			ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.disable.rules.tooltip"));

		// Reset weights
//		ImageIcon resetWeightsIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/resetWeights.gif"));
//		Action resetWeightsAction =
//			new ModelAction(
//				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.resetweights"),
//				resetWeightsIcon,
//				true,
//				false,
//				true) {
//			public void actionPerformed(ActionEvent e) {
//				parent.resetWeights();
//			}
//		};
//		JMenuItem resetWeightsItem = add(resetWeightsAction);
//		resetWeightsItem.setIcon(null);
//		resetWeightsItem.setMnemonic(KeyEvent.VK_R);
//		resetWeightsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		// 	JButton resetWeightsButton = parent.getToolBar().add(resetWeightsAction);
		// 	resetWeightsButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.resetweights.tooltip"));

		// Save
		ImageIcon saveIcon = new ImageIcon(ModelMaker.class.getResource("images/save.gif"));
		Action saveAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.save"),
				saveIcon,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.saveActiveModel();
			}
		};
		JMenuItem saveItem = add(saveAction);
		saveItem.setIcon(null);
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		// JButton saveButton = parent.getToolBar().add(saveAction);
		// saveButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.save.tooltip"));

		// Export clue table
		Action exportClueTableAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.exportClueTable"),
				null,
				true,
				false,
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				ExportClueTableDialog builder = new ExportClueTableDialog(parent);
				// builder.newModel();
				builder.setVisible(true);
			}
		};
		JMenuItem exportClueTableItem = add(exportClueTableAction);
		exportClueTableItem.setIcon(null);
		// exportClueTableItem.setMnemonic('n');
		// exportClueTableItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		addSeparator();

		// Train
		ImageIcon trainIcon = new ImageIcon(ModelMaker.class.getResource("images/train.gif"));
		Action trainAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.train"),
				trainIcon,
				true,
				true,
				// 2009-07-06 rphall
				// Removed license validation for open-source release
				// LicenseManager.getBoolean("train", true)) {
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				new TrainDialog(parent, false).setVisible(true);
			}
		};
		JMenuItem trainItem = add(trainAction);
		trainItem.setIcon(null);
		trainItem.setMnemonic(KeyEvent.VK_T);
		trainItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		JButton trainButton = parent.getToolBar().add(trainAction);
		trainButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.train.tooltip"));


		// trainAndTest
		ImageIcon trainAndTestIcon = new ImageIcon(ModelMaker.class.getResource("images/trainAndTest.gif"));
		Action trainAndTestAction =
			new ModelAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.trainandtest"),
				trainAndTestIcon,
				true,
				true,
				// 2009-07-06 rphall
				// Removed license validation for open-source release
				// LicenseManager.getBoolean("train", true)) {
				true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				new TrainDialog(parent, true).setVisible(true);
			}
		};
		JMenuItem trainAndTestItem = add(trainAndTestAction);
		trainAndTestItem.setIcon(null);
		trainAndTestItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		JButton trainAndTestButton = parent.getToolBar().add(trainAndTestAction);
		trainAndTestButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.trainandtest.tooltip"));



		// Evaluate
		Action evaluateAction = new EvaluateAction();
		JMenuItem evaluateItem = add(evaluateAction);
		evaluateItem.setIcon(null);
		evaluateItem.setMnemonic(KeyEvent.VK_G);
		evaluateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		JButton evaluateButton = parent.getToolBar().add(evaluateAction);
		evaluateButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.model.evaluate.tooltip"));

		parent.getToolBar().addSeparator();

		addAutoItems();
	}

	private void addListeners() {
		parent.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ModelMakerEventNames.PROBABILITY_MODEL)) {
					IProbabilityModel model = parent.getProbabilityModel();
					if (model != null) {
						opened(model.getModelFilePath());
					}
				}
			}
		});
	}

	public void open(String fileName) {
		try {
			parent.setProbabilityModel(fileName, false);
		} catch (OperationFailedException ex) {
			logger.severe(new LoggingObject("CM-100502", fileName).toString() + ": " + ex);
			remove(fileName);
		}
	}
}
