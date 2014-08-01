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
package com.choicemaker.cm.core.xmlconf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import com.choicemaker.cm.core.XmlConfException;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:27:24 $
 */
public class ExtensionPointMapper {
	public static Object getInstance(String extensionPoint, String extensionId) throws XmlConfException {
		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint(extensionPoint);
		IExtension ext = pt.getExtension(extensionId);
		if (ext == null) {
			IExtension[] extensions = pt.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elems = extensions[i].getConfigurationElements();
				if (elems.length > 0 && extensionId.equals(elems[0].getAttribute("backwardCompatibilityId"))) {
					ext = extensions[i];
					break;
				}
			}
			if (ext == null) {
				throw new XmlConfException("Unknown extension: " + extensionId);
			}
		}
		try {
			return ext.getConfigurationElements()[0].createExecutableExtension("class");
		} catch (CoreException ex) {
			throw new XmlConfException("Configuration error", ex);
		}
	}

	public static Object getInstance(String extensionPoint, Class handledClass)
		throws XmlConfException {
		try {
			String handledClassName = handledClass.getName();
			IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint(extensionPoint);
			IExtension[] extensions = pt.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elems = extension.getConfigurationElements();
				if (elems.length > 0 && handledClassName.equals(elems[0].getAttribute("handledClass"))) {
					return elems[0].createExecutableExtension("class");
				}
			}
		} catch (Exception ex) {
			throw new XmlConfException("Configuration error.", ex);
		}
		throw new XmlConfException("No configurator found.");
	}

	public static Object[] getAllInstances(Plugin plugin, String extensionPoint) {
		List l = new ArrayList();
		IExtensionPoint pt = plugin.getDescriptor().getExtensionPoint(extensionPoint);
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] elems = extension.getConfigurationElements();
			if (elems.length > 0) {
				try {
					l.add(elems[0].createExecutableExtension("class"));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return l.toArray();
	}
}
