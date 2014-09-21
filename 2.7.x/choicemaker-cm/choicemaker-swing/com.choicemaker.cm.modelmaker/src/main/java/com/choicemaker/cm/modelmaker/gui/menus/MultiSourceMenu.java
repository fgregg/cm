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
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.MarkedRecordPairSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.core.util.LoggingObject;
import com.choicemaker.cm.core.xmlconf.ExtensionPointMapper;
import com.choicemaker.cm.core.xmlconf.MarkedRecordPairSourceXmlConf;
import com.choicemaker.cm.gui.utils.dialogs.FileChooserFactory;
import com.choicemaker.cm.modelmaker.ModelMakerEventNames;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.abstraction.PreferenceKeys;
import com.choicemaker.cm.modelmaker.gui.dialogs.SourceTypeSelectorDialog;
import com.choicemaker.cm.modelmaker.gui.sources.SourceGuiFactory;

/**
 * The menu from which a MarkedRecordPairSource is selected.
 *
 * @author S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:12:51 $
 */
public class MultiSourceMenu extends LastUsedMenu {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SourceMenu.class.getName());

	private ModelMaker parent;
	private int num;
	private int modifierMask;

	public MultiSourceMenu(ModelMaker g, String name, int num, int modifierMask) {
		super(name, PreferenceKeys.SOURCES_MENU + name, 10);
		parent = g;
		this.num = num;
		this.modifierMask = modifierMask;
		buildMenu();

	}

	private abstract class SourceAction extends AbstractAction implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		private boolean dependsModel;
		private boolean dependsSource;
		private boolean licensed;

		SourceAction(String name, Icon icon, boolean dependsModel, boolean dependsSource, boolean licensed) {
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
			// AJW 2004-04-26: use the source num to avoid enabling an action that would throw an error.
			return licensed && (!dependsModel || parent.haveProbabilityModel()) && (!dependsSource || parent.haveMarkedRecordPairSource(num));
			//return licensed && (!dependsModel || parent.haveProbabilityModel()) && (!dependsSource || parent.haveMarkedRecordPairSource());
		}

		private void doSetEnabled() {
			setEnabled(getEnabled());
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			// AJW 2004-04-26: used parentheses here.
			if ((dependsSource && propertyName == ModelMakerEventNames.MARKED_RECORD_PAIR_SOURCE) ||
				(dependsModel && propertyName == ModelMakerEventNames.PROBABILITY_MODEL)) {
				doSetEnabled();
			}
		}
	}

	public void buildMenu() {
		// New
		ImageIcon newIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/new.gif"));
		Action newAction = new SourceAction(ChoiceMakerCoreMessages.m.formatMessage("new.elipsis"), newIcon, false, false, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				MarkedRecordPairSource source = (MarkedRecordPairSource) new SourceTypeSelectorDialog(parent, false).define();
				if (source != null) {
					try {
						MarkedRecordPairSourceXmlConf.add(source);
						opened(source.getFileName());
					} catch (XmlConfException ex) {
						logger.severe(new LoggingObject("CM-100601", source.getFileName()).toString() + ": " + ex);
					}
					parent.setMultiSource(num, source);
				}
			}
		};
		JMenuItem newItem = add(newAction);
		newItem.setIcon(null);
		newItem.setMnemonic('n');
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK + modifierMask));

		// Open
		ImageIcon openIcon = null; //open ImageIcon(AbstractApplication.class.getResource("images/open.gif"));
		Action openAction = new SourceAction(ChoiceMakerCoreMessages.m.formatMessage("open.elipsis"), openIcon, false, false, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				File file = FileChooserFactory.selectMrpsFile(parent);
				if (file != null) {
					open(file.getAbsolutePath());
				}
			}
		};
		JMenuItem openItem = add(openAction);
		openItem.setIcon(null);
		openItem.setMnemonic('o');
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK + +modifierMask));

		add(new AbstractAction("Close") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				parent.setMultiSource(num, null);
			}
		});

		// Holds
		ImageIcon holdIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/hold.gif"));
		Action holdAction = new AbstractAction(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.includeholds"), holdIcon) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
				parent.setMultiIncludeHolds(num, item.isSelected());
			}
		};
		final JCheckBoxMenuItem holdItem = new JCheckBoxMenuItem(holdAction);
		add(holdItem);
		holdItem.setIcon(null);
		holdItem.setMnemonic('h');
		holdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK + modifierMask));
		parent.addPropertyChangeListener(ModelMakerEventNames.MULTI_INCLUDE_HOLDS, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				boolean mih = parent.getMultiIncludeHolds(num);
				if (holdItem.isSelected() != mih) {
					holdItem.setSelected(mih);
				}
			}
		});

		addSeparator();

		// Edit
		ImageIcon editIcon = null; //new ImageIcon(AbstractApplication.class.getResource("images/edit.gif"));
		Action editAction = new SourceAction(ChoiceMakerCoreMessages.m.formatMessage("edit.elipsis"), editIcon, false, true, true) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				MarkedRecordPairSource source = parent.getMultiSource(num);
				SourceGuiFactory factory = null;
				try {
					factory = (SourceGuiFactory) ExtensionPointMapper.getInstance(ChoiceMakerExtensionPoint.CM_MODELMAKER_MRPSREADERGUI, source.getClass());
				} catch (XmlConfException e1) {
					e1.printStackTrace();
				}
				if (factory != null) {
					source = (MarkedRecordPairSource) factory.createGui(parent, source).define();
					if (source != null) {
						try {
							MarkedRecordPairSourceXmlConf.add(source);
						} catch (XmlConfException ex) {
							logger.severe(new LoggingObject("CM-100402", source.getFileName()).toString() + ": " + ex);
						}
						parent.setMultiSource(num, source);
					} else {
						logger.severe("null source");
					}
				} else {
					logger.severe("null factory");
				}
			}
		};
		JMenuItem editItem = add(editAction);
		editItem.setIcon(null);
		editItem.setMnemonic('m');
		editItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK + modifierMask));
		// 	JButton editButton = parent.getToolBar().add(editAction);
		// 	editButton.setToolTipText(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.source.edit.tooltip"));

		addAutoItems();
	}

	public void open(String fileName) {
		try {
			MarkedRecordPairSource s = MarkedRecordPairSourceXmlConf.getMarkedRecordPairSource(fileName);
			parent.setMultiSource(num, s);
			opened(fileName);
		} catch (XmlConfException ex) {
			logger.severe(new LoggingObject("CM-100601", fileName).toString() + ": " + ex);
			remove(fileName);
		}
	}
}
