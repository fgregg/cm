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
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.gui.utils.viewer.CompositePaneModel;
import com.choicemaker.cm.gui.utils.viewer.xmlconf.RecordPairViewerXmlConf;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.abstraction.PreferenceKeys;
import com.choicemaker.cm.modelmaker.gui.panels.HumanReviewPanel;

/**
 *
 * @author  S. Yoakum-Stover
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:10:23 $
 */
public class LayoutMenu extends LastUsedMenu {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LayoutMenu.class.getName());

	private HumanReviewPanel parent;

	public LayoutMenu(HumanReviewPanel g) {
		super(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.layout"), PreferenceKeys.LAYOUT_MENU, 10);
		parent = g;
		this.setMnemonic(KeyEvent.VK_L);
		buildMenu();
	}

	private abstract class LayoutAction extends AbstractAction implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		private boolean dependsModel;

		LayoutAction(String name, Icon icon, boolean dependsModel) {
			super(name, icon);
			this.dependsModel = dependsModel;
			if (dependsModel) {
				parent.getModelMaker().addPropertyChangeListener(this);
			}
			doSetEnabled();
		}

		protected boolean getEnabled() {
			return (!dependsModel || parent.getModelMaker().haveProbabilityModel());
		}

		private void doSetEnabled() {
			setEnabled(getEnabled());
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (dependsModel && propertyName == ModelMakerEventNames.PROBABILITY_MODEL) {
				doSetEnabled();
			}
		}
	}

	public void buildMenu() {
		Action newAction = new LayoutAction(ChoiceMakerCoreMessages.m.formatMessage("new"), null, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setDefaultLayout();
			}
		};
		add(newAction);

		Action openAction = new LayoutAction(ChoiceMakerCoreMessages.m.formatMessage("open.elipsis"), null, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File file = FileChooserFactory.selectLayoutFile(parent);
				if (file != null) {
					open(file.getAbsolutePath());
				}
			}
		};
		add(openAction);

		Action saveAction = new LayoutAction(ChoiceMakerCoreMessages.m.formatMessage("save"), null, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				CompositePaneModel layout = parent.getCurrentLayout();
				if (layout.getFileName() == null) {
					saveAs();
				} else {
					try {
						RecordPairViewerXmlConf.saveLayout(layout);
					} catch (Exception ex) {
						logger.severe(new LoggingObject("CM-100301", layout.getFileName()).getFormattedMessage() + ": " + ex);
					}
				}
			}
		};
		add(saveAction);

		Action saveAsAction = new LayoutAction(ChoiceMakerCoreMessages.m.formatMessage("saveas.elipsis"), null, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		};
		add(saveAsAction);

		addAutoItems();
	}

	private void saveAs() {
		File file = FileChooserFactory.selectLayoutFile(parent, JFileChooser.SAVE_DIALOG);
		if (file != null) {
			CompositePaneModel layout = null;
			try {
				layout = parent.getCurrentLayout();
				String fileName = file.getAbsolutePath();
				if (!fileName.endsWith(".layout")) {
					fileName += ".layout";
				}
				if (!fileName.equals(layout.getFileName()) && new File(fileName).exists()) {
					if (JOptionPane
						.showConfirmDialog(
							this,
							ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.dialog.savelayout.replace", fileName),
							ChoiceMakerCoreMessages.m.formatMessage("confirm"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE)
						!= JOptionPane.YES_OPTION) {
						return;
					}
				}
				layout.setFileName(fileName);
				RecordPairViewerXmlConf.saveLayout(layout);
				opened(layout.getFileName());
			} catch (Exception ex) {
				String fileName = layout == null ? "null" : layout.getFileName();
				logger.severe(new LoggingObject("CM-100301", fileName).getFormattedMessage() + ": " + ex);
			}
		}
	}

	public void open(String fileName) {
		try {
			CompositePaneModel layout = RecordPairViewerXmlConf.readLayout(fileName, parent.getModelMaker().getProbabilityModel().getAccessor().getDescriptor());
			layout.setEnableEditing(true);
			parent.setCurrentLayout(layout);
			opened(fileName);
		} catch (Exception ex) {
			logger.severe(new LoggingObject("CM-100302", fileName).getFormattedMessage() + ": " + ex);
			remove(fileName);
		}
	}
}
