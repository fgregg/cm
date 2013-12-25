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

import javax.swing.text.Document;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.module.IModuleController;
import com.choicemaker.cm.module.IModuleController.IUserInterface;

/**
 * Panel that loads a configuration of {@link IModule} from the
 * registry, or uses an instance of {@link #DefaultModule} if no
 * module configuration exists in the registry.
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/27 19:27:57 $
 */
public class DefaultManagedPanel extends AbstractTabbedPanel implements IModelMakerAware {

	private final IModuleController module;
	private Object modelMakerObject;
	
	public String getTabName() {
		return DefaultManagedPanel.class.getName();
	}

	public DefaultManagedPanel(Document d, Object mmo) {
		super();
		// Fail fast
		if (d == null || mmo == null) {
			throw new IllegalArgumentException("null argument");
		}
		this.modelMakerObject = mmo;
		this.module = createModule(d);
		IUserInterface ui = this.module.getUserInterface();
		if (ui instanceof IPanelControl) {
			IPanelControl ipc = (IPanelControl) ui;
			ipc.setManagedPanel(this);
		}
	}

	/**
	 * Create an instance of an {@link #IModule} configuration from
	 * the PluginRegistry. The configuration must be unique within the registry.
	 * If no configuration exists within the register, an instance of
	 * {@link #DefaultModule} is returned
	 * @return a non-null instance of a IModule configuration.
	 */
	public IModuleController createModule(Document d) {

		// Create a default builder
		IModuleController retVal = new DefaultModuleController(d);

		// Replace the default with a configured instance, if one exists
		IExtensionPoint pt =
			Platform.getPluginRegistry().getExtensionPoint(
				"com.choicemaker.cm.modelmaker.pluggableController");
		if (pt != null) {
		IExtension[] extensions = pt.getExtensions();
		if (extensions.length > 1) {
			throw new Error(
				"too many extensions of IModule: "
					+ extensions.length);
		} else if (extensions.length == 1) {
			IExtension extension = extensions[0];
			IConfigurationElement[] els = extension.getConfigurationElements();
			if (els.length != 1) {
				throw new Error(
					"too many configurations of the default module: "
						+ extensions.length);
			}
			IConfigurationElement element = els[0];
			try {
				retVal =
					(IModuleController) element.createExecutableExtension(
						"class");
				if (retVal instanceof IModelMakerAware) {
					IModelMakerAware mma = (IModelMakerAware) retVal;
					mma.setModelMakerObject(this.getModelMakerObject());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				String msg =
					"Error creating default configuration: "
						+ ex.getMessage();
				throw new Error(msg, ex);
			}
		}
		}

		return retVal;
	}

	public IModuleController getModule() {
		return module;
	}

	public void setModelMakerObject(Object modelMakerObject) {
		this.modelMakerObject = modelMakerObject;
	}

	public Object getModelMakerObject() {
		return modelMakerObject;
	}

}

