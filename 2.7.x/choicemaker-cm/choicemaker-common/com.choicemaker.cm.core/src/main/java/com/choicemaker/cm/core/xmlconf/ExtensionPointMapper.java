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

import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.CMPlugin;
import com.choicemaker.e2.E2Exception;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:27:24 $
 */
public class ExtensionPointMapper {
	public static Object getInstance(String extensionPoint, String extensionId) throws E2Exception {
		CMExtension ext = CMPlatformUtils.getExtension(extensionPoint,extensionId);
		if (ext == null) {
			CMExtension[] extensions = CMPlatformUtils.getExtensions(extensionPoint);
			for (int i = 0; i < extensions.length; i++) {
				CMConfigurationElement[] elems = extensions[i].getConfigurationElements();
				if (elems.length > 0 && extensionId.equals(elems[0].getAttribute("backwardCompatibilityId"))) {
					ext = extensions[i];
					break;
				}
			}
			if (ext == null) {
				throw new E2Exception("Unknown extension: " + extensionId);
			}
		}
		try {
			return ext.getConfigurationElements()[0].createExecutableExtension("class");
		} catch (E2Exception ex) {
			throw new E2Exception("Configuration error", ex);
		}
	}

	public static Object getInstance(String extensionPoint, Class handledClass)
		throws E2Exception {
		try {
			String handledClassName = handledClass.getName();
			CMExtension[] extensions = CMPlatformUtils.getExtensions(extensionPoint);

			for (int i = 0; i < extensions.length; i++) {
				CMExtension extension = extensions[i];
				CMConfigurationElement[] elems = extension.getConfigurationElements();
				if (elems.length > 0 && handledClassName.equals(elems[0].getAttribute("handledClass"))) {
					return elems[0].createExecutableExtension("class");
				}
			}
		} catch (Exception ex) {
			throw new E2Exception("Configuration error.", ex);
		}
		throw new E2Exception("No configurator found.");
	}

	public static Object[] getAllInstances(CMPlugin plugin, String extensionPoint) {
		List l = new ArrayList();
		CMExtensionPoint pt = plugin.getDescriptor().getExtensionPoint(extensionPoint);
		CMExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			CMExtension extension = extensions[i];
			CMConfigurationElement[] elems = extension.getConfigurationElements();
			if (elems.length > 0) {
				try {
					l.add(elems[0].createExecutableExtension("class"));
				} catch (E2Exception e) {
					e.printStackTrace();
				}
			}
		}
		return l.toArray();
	}
}
