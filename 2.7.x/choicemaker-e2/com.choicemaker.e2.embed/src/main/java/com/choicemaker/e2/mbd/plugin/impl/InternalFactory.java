/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.choicemaker.e2.mbd.plugin.impl;

import com.choicemaker.e2.mbd.runtime.MultiStatus;
import com.choicemaker.e2.mbd.runtime.model.ConfigurationElementModel;
import com.choicemaker.e2.mbd.runtime.model.ConfigurationPropertyModel;
import com.choicemaker.e2.mbd.runtime.model.ExtensionModel;
import com.choicemaker.e2.mbd.runtime.model.ExtensionPointModel;
import com.choicemaker.e2.mbd.runtime.model.Factory;
import com.choicemaker.e2.mbd.runtime.model.LibraryModel;
import com.choicemaker.e2.mbd.runtime.model.PluginDescriptorModel;
import com.choicemaker.e2.mbd.runtime.model.PluginFragmentModel;
import com.choicemaker.e2.mbd.runtime.model.PluginPrerequisiteModel;
import com.choicemaker.e2.mbd.runtime.model.PluginRegistryModel;

public class InternalFactory extends Factory {
public InternalFactory(MultiStatus status) {
	super(status);
}
public ConfigurationElementModel createConfigurationElement() {
	return new ConfigurationElement();
}
public ConfigurationPropertyModel createConfigurationProperty() {
	return new ConfigurationProperty();
}
public ExtensionModel createExtension() {
	return new Extension();
}
public ExtensionPointModel createExtensionPoint() {
	return new ExtensionPoint();
}



public LibraryModel createLibrary() {
	return new Library();
}
public PluginDescriptorModel createPluginDescriptor() {
	return new PluginDescriptor();
}

public PluginFragmentModel createPluginFragment() {
	return new FragmentDescriptor();
}

public PluginPrerequisiteModel createPluginPrerequisite() {
	return new PluginPrerequisite();
}
public PluginRegistryModel createPluginRegistry() {
	return new PluginRegistry();
}
}
