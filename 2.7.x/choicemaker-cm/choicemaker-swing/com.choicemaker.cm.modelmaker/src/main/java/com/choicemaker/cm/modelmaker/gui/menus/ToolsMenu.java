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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;
import com.choicemaker.cm.modelmaker.gui.ModelMaker;
import com.choicemaker.cm.modelmaker.gui.dialogs.CollectionsDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.ExportProbabilitiesDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.GeoFunctionsDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.MatcherDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.ObjectMakerDialog;
import com.choicemaker.cm.modelmaker.gui.dialogs.StringComparator;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:16:21 $
 */
public class ToolsMenu extends JMenu {
	private static final long serialVersionUID = 1L;
	private ModelMaker modelMaker;

	public ToolsMenu(ModelMaker modelMaker) {
		super(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.tools"));
		this.modelMaker = modelMaker;
		buildMenu();
	}

	private class MatchAction extends AbstractAction implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		MatchAction() {
			super(ChoiceMakerCoreMessages.m.formatMessage("train.gui.modelmaker.menu.tools.match"));
			modelMaker.addPropertyChangeListener(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			new MatcherDialog(modelMaker).setVisible(true);
		}
		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			ImmutableProbabilityModel model = modelMaker.getProbabilityModel();
			setEnabled(model != null && model.canEvaluate());
		}

	}


	private class GeoTestAction extends AbstractAction implements PropertyChangeListener {
		private static final long serialVersionUID = 1L;
		GeoTestAction() {
			super("Geographical Functions...");
			modelMaker.addPropertyChangeListener(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			new GeoFunctionsDialog(modelMaker).setVisible(true);
		}
		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			ImmutableProbabilityModel model = modelMaker.getProbabilityModel();
			setEnabled(model != null);
		}

	}



	private class ExportProbabilitiesAction extends AbstractAction implements PropertyChangeListener {

		private static final long serialVersionUID = 1L;

		public ExportProbabilitiesAction() {
			super("Export Probabilities and Active Clues...");
			modelMaker.addPropertyChangeListener(this);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			new ExportProbabilitiesDialog(modelMaker).setVisible(true);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			setEnabled(modelMaker.haveSourceList());
		}

	}


	private void buildMenu() {
		AbstractAction matchAction = new MatchAction();
		// 2014-04-24 rphall: Commented out unused local variable.
		/* JMenuItem matchItem = */ add(matchAction);
		AbstractAction stringComparator = new AbstractAction("String Comparison Functions...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ev) {
				new StringComparator(modelMaker).setVisible(true);
			}
		};
		add(stringComparator);

		add( new GeoTestAction());

		AbstractAction collections = new AbstractAction("Collections Lookup...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				new CollectionsDialog(modelMaker).setVisible(true);
			}
		};
		add(collections);

		AbstractAction buildModelsJar = new AbstractAction("Build Holder Classes and DB Objects...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				new ObjectMakerDialog(modelMaker).setVisible(true);
			};
		};
		add(buildModelsJar);

		addSeparator();

		add(new ExportProbabilitiesAction());

		buildPluginToolMenuItems();
	}

	private void buildPluginToolMenuItems() {
		CMExtensionPoint pt = CMPlatformUtils.getExtensionPoint(ChoiceMakerExtensionPoint.CM_MODELMAKER_TOOLMENUITEM);
		CMExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			CMExtension extension = extensions[i];
			CMConfigurationElement[] els = extension.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				try {
					JMenuItem item = buildToolItem(els[j]);
					add(item);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private JMenuItem buildToolItem(CMConfigurationElement element) throws Exception {
		Action action = (Action) element.createExecutableExtension("class");
		if (action instanceof ToolAction) {
			((ToolAction)action).setModelMaker(modelMaker);
		}

		CMConfigurationElement[] kids = element.getChildren();
		if (kids.length > 0) {
			JMenu menu = new JMenu(action);
			for (int i = 0; i < kids.length; i++) {
				menu.add(buildToolItem(kids[i]));
			}
			return menu;
		} else {
			JMenuItem item = new JMenuItem(action);
			return item;
		}
	}

	public static abstract class ToolAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		protected ModelMaker modelMaker;

		public ToolAction() { }

		public ToolAction(String name) {
			super(name);
		}

		public ToolAction(String name, Icon icon) {
			super(name, icon);
		}

		public void setModelMaker(ModelMaker modelMaker) {
			this.modelMaker = modelMaker;
		}
	}

}
