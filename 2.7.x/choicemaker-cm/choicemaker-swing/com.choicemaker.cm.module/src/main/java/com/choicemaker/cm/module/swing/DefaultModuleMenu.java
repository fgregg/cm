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
package com.choicemaker.cm.module.swing;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.module.IModuleController;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * @author  rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:57 $
 */
public class DefaultModuleMenu extends JMenu {
	
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_MESSAGE_KEY = "train.gui.modelmaker.menu.module";

	private IModuleController module;
	private Object modelMakerObject;

	/**
	 * Lookd for the menu text under the default message key
	 * @param module
	 */
	public DefaultModuleMenu(IModuleController module, Object modelMakerObject) {
		this(module,modelMakerObject,DEFAULT_MESSAGE_KEY);
	}

	/**
	 * @param module
	 * @param messageKey the key into a resource bundle managed
	 * by the module for the menu text
	 */
	public DefaultModuleMenu(IModuleController module, Object modelMakerObject, String messageKey) {
		super();
		this.module = module;
		this.setModelMakerObject(modelMakerObject);
		if (this.module instanceof IModelMakerAware) {
			IModelMakerAware mma = (IModelMakerAware) module;
			mma.setModelMakerObject(modelMakerObject);
		}
		String text = this.module.getMessageSupport().formatMessage(messageKey);
		if (this.module instanceof IMenuAware) {
			IMenuAware ima = (IMenuAware) module;
			ima.setMenuObject(this);
		}
		this.setText(text);
		buildMenu();
	}

	private void buildMenu() {
		buildPluginClusterMenuItems();
	}

	private void buildPluginClusterMenuItems() {
		CMExtension[] extensions = CMPlatformUtils
				.getExtensions(ChoiceMakerExtensionPoint.CM_MODELMAKER_PLUGGABLEMENUITEM);
			for (int i = 0; i < extensions.length; i++) {
				CMExtension extension = extensions[i];
				CMConfigurationElement[] els = extension.getConfigurationElements();
				for (int j = 0; j < els.length; j++) {
					try {
						JMenuItem item = buildModuleItem(els[j]);
						add(item);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

	private JMenuItem buildModuleItem(CMConfigurationElement e) throws Exception {
		Action action = (Action) e.createExecutableExtension("class");
		if (action instanceof ModuleAction) {
			ModuleAction ca = (ModuleAction) action;
			ca.setModule(module);
		}

		JMenuItem retVal = null;
		CMConfigurationElement[] kids = e.getChildren();
		if (kids.length > 0) {
			String name = e.getName();
			if (name.equals("action")) {
				retVal = new JMenu(action);
				for (int i = 0; i < kids.length; i++) {
					retVal.add(buildModuleItem(kids[i]));
				}
			} else if (name.equals("radio")) {
				ButtonGroup group = new ButtonGroup();
				retVal = new JMenu(action);
				for (int i = 0; i < kids.length; i++) {
					JRadioButtonMenuItem radioItem =
						(JRadioButtonMenuItem) buildModuleItem(kids[i]);
					group.add(radioItem);
					retVal.add(radioItem);
				}
			} else {
				throw new Error("unexpected element name: '" + name + "'");
			}

		} else {
			String name = e.getName();
			if (name.equals("action")) {
				retVal = new JMenuItem(action);
			} else if (name.equals("toggle")) {
				retVal = new JCheckBoxMenuItem(action);
			} else if (name.equals("radioItem")) {
				retVal = new JRadioButtonMenuItem(action);
			} else {
				throw new Error("unexpected element name: '" + name + "'");
			}
		}

		if (retVal instanceof IModelMakerAware) {
			IModelMakerAware mma = (IModelMakerAware) retVal;
			mma.setModelMakerObject(this.getModelMakerObject());
		}
		if (action instanceof ModuleAction) {
			ModuleAction ca = (ModuleAction) action;
			ca.initializeButtonModel(retVal.getModel());
		}
		return retVal;
	}

	public void setModelMakerObject(Object modelMakerObject) {
		this.modelMakerObject = modelMakerObject;
	}

	public Object getModelMakerObject() {
		return modelMakerObject;
	}

}

