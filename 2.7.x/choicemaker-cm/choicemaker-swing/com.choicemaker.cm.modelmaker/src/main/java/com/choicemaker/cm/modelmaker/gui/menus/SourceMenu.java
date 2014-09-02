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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.RepositoryChangeEvent;
import com.choicemaker.cm.core.RepositoryChangeListener;
import com.choicemaker.cm.core.Source;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.MarkedRecordPairBinder;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceTypeSelectorDialog;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationEvent;
import com.choicemaker.cm.modelmaker.gui.listeners.EvaluationListener;

/**
 * The menu from which a MarkedRecordPairSource is selected.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.3 $ $Date: 2010/03/29 13:14:25 $
 */
public class SourceMenu extends JMenu {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SourceMenu.class.getName());

	private ModelMaker parent;
	private static final String SOURCE_MENU = ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source");
	private MultiSourceMenu[] multiSourceMenus = new MultiSourceMenu[2];

	public SourceMenu(ModelMaker g) {
		super(SOURCE_MENU);
		parent = g;
		this.setMnemonic(KeyEvent.VK_S);
		buildMenu();

		// Set "Include Holds" to true for test sources
		parent.setMultiIncludeHolds(1,true);
	}

	private class SaveAsAction extends AbstractAction implements EvaluationListener {
		private static final long serialVersionUID = 1L;
//		private boolean selection;

		SaveAsAction(String name, Icon icon, boolean selection) {
			super(name, icon);
			parent.addEvaluationListener(this);
			setEnabled(false);
		}

		private void setEnabled() {
			setEnabled(parent.haveSourceList());
		}

		public void evaluated(EvaluationEvent evt) {
			setEnabled();
		}

		public void actionPerformed(ActionEvent e) {
			Source source = new SourceTypeSelectorDialog(parent, true).define();
			if (source != null) {
				MarkedRecordPairSink sink = (MarkedRecordPairSink) source.getSink();
				sink.setModel(parent.getProbabilityModel());
				try {
					MarkedRecordPairBinder.store(parent.getSourceList(), parent.getSelection(), sink);
					MarkedRecordPairSourceXmlConf.add((MarkedRecordPairSource) source);
				} catch (IOException ex) {
					logger.error(new LoggingObject("CM-100602", sink.getName()), ex);
				} catch (XmlConfException ex) {
					logger.error(new LoggingObject("CM-100602", sink.getName()), ex);
				}
			}
		}
	}

	private class SaveAction extends AbstractAction implements RepositoryChangeListener, PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		private boolean saveableSource;

		SaveAction(String name, Icon icon) {
			super(name, icon);
			parent.addMarkedRecordPairDataChangeListener(this);
			parent.getRepository().addRepositoryChangeListener(this);
			parent.addPropertyChangeListener(this);
			setEnabled(false);
		}

		private void setEnabled() {
			setEnabled(saveableSource && parent.haveSourceList());
		}

		public void setChanged(RepositoryChangeEvent evt) {
			setEnabled();
		}

		public void recordDataChanged(RepositoryChangeEvent evt) {
			setEnabled();
		}

		public void markupDataChanged(RepositoryChangeEvent evt) {
			setEnabled();
		}

		public void propertyChange(PropertyChangeEvent e) {
			MarkedRecordPairSource src = parent.getMarkedRecordPairSource();
			saveableSource = src != null && src.hasSink();
			setEnabled();
		}

		public void actionPerformed(ActionEvent e) {
			boolean d = true;
			if (!parent.isIncludeHolds()) {
				d =
					JOptionPane.showConfirmDialog(
						parent,
						"Records originally marked hold will be lost. Save anyhow?",
						"Warning",
						JOptionPane.YES_NO_OPTION)
						== JOptionPane.YES_OPTION;
			}
			if (d) {
				parent.saveMarkedRecordPairSource();
				setEnabled(false);
			}
		}
	}

	private class SelectCheckedAction extends AbstractAction implements EvaluationListener {
		private static final long serialVersionUID = 1L;

		public SelectCheckedAction() {
			super("Select checked");
			parent.addEvaluationListener(this);
			setEnabled(false);
		}

		private void setEnabled() {
			setEnabled(parent.haveSourceList());
		}

		public void evaluated(EvaluationEvent e) {
			setEnabled();
		}

		public void actionPerformed(ActionEvent e) {
			parent.getFilter().reset();
			parent.getFilter().setCollection(parent.getChecked());
			parent.filterMarkedRecordPairList();
		}
	}

	private class SaveCheckedAction extends AbstractAction implements EvaluationListener {
		private static final long serialVersionUID = 1L;

		public SaveCheckedAction() {
			super("Save checked as...");
			parent.addEvaluationListener(this);
			setEnabled(false);
		}

		private void setEnabled() {
			setEnabled(parent.haveSourceList());
		}

		public void evaluated(EvaluationEvent evt) {
			setEnabled();
		}

		public void actionPerformed(ActionEvent e) {
			Source source = new SourceTypeSelectorDialog(parent, true).define();
			if (source != null) {
				MarkedRecordPairSink sink = (MarkedRecordPairSink) source.getSink();
				sink.setModel(parent.getProbabilityModel());
				try {
					parent.sortChecked();
					MarkedRecordPairBinder.store(parent.getSourceList(), parent.getCheckedIndices(), sink);
					MarkedRecordPairSourceXmlConf.add((MarkedRecordPairSource) source);
				} catch (IOException ex) {
					logger.error(new LoggingObject("CM-100602", sink.getName()), ex);
				} catch (XmlConfException ex) {
					logger.error(new LoggingObject("CM-100602", sink.getName()), ex);
				}
			}
		}
	}

	private class CheckAction extends AbstractAction implements EvaluationListener {
		private static final long serialVersionUID = 1L;
		private boolean check;
		public CheckAction(boolean check) {
			super(check ? "Check all" : "Uncheck all");
			this.check = check;
			parent.addEvaluationListener(this);
			setEnabled(false);
		}

		private void setEnabled() {
			setEnabled(parent.haveSourceList());
		}

		public void evaluated(EvaluationEvent evt) {
			setEnabled();
		}

		public void actionPerformed(ActionEvent e) {
			if(check) {
				parent.checkAll();
			} else {
				parent.uncheckAll();
			}
		}
	}

	public void buildMenu() {
		JMenu training = multiSourceMenus[0] = new MultiSourceMenu(parent, "Training", 0, 0);
		add(training);

		JMenu test = multiSourceMenus[1] = new MultiSourceMenu(parent, "Test", 1, ActionEvent.SHIFT_MASK);
		add(test);

		// 2014-04-24 rphall: Commented out unused local variable.
//		ImageIcon swapSourcesIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/swapSources.gif"));
		JMenuItem swapSourcesItem = add(new AbstractAction("Swap sources") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.swapSources();
				opened(0);
				opened(1);
			}

			private void opened(int i) {
				MarkedRecordPairSource m = parent.getMultiSource(i);
				if (m != null) {
					multiSourceMenus[i].opened(m.getFileName());
				}
			}
		});
		swapSourcesItem.setIcon(null);
		swapSourcesItem.setMnemonic(KeyEvent.VK_X);
		swapSourcesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));

		final JCheckBoxMenuItem keepBothInMemory = new JCheckBoxMenuItem("Keep both sources in memory  ");
		keepBothInMemory.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				parent.setKeepAllSourcesInMemory(keepBothInMemory.isSelected());
			}
		});
		add(keepBothInMemory);
		keepBothInMemory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));

		addSeparator();

		// Save
		ImageIcon saveIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/save.gif"));
		Action saveAction =
			new SaveAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.save"), saveIcon);
		JMenuItem saveItem = add(saveAction);
		saveItem.setIcon(null);
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		// 	JButton saveButton = parent.getToolBar().add(saveAction);
		// 	saveButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.save.tooltip"));

		// Save selection
		ImageIcon saveSelectionIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/saveSelection.gif"));
		Action saveSelectionAction =
			new SaveAsAction(
				ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.save.selection"),
				saveSelectionIcon,
				true);
		JMenuItem saveSelectionItem = add(saveSelectionAction);
		saveSelectionItem.setIcon(null);
		saveSelectionItem.setMnemonic(KeyEvent.VK_S);
		saveSelectionItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
		// 	JButton saveSelectionButton = parent.getToolBar().add(saveSelectionAction);
		// 	saveSelectionButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.saveSelection.tooltip"));

		addSeparator();

		add(new CheckAction(false));
		add(new CheckAction(true));

		add(new SelectCheckedAction());

		add(new SaveCheckedAction());

	}
}
