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
package com.choicemaker.cm.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;

/**
 * @author ajwinkel
 *
 */
public final class NamedResources {

	public static final String EXTENSION_POINT = ChoiceMakerExtensionPoint.CM_CORE_NAMEDRESOURCE;

	public static InputStream getNamedResource(String resourceName) throws IOException {
		IExtensionPoint pt = Platform.getPluginRegistry().getExtensionPoint(EXTENSION_POINT);
		IExtension[] extensions = pt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension ext = extensions[i];
			IConfigurationElement[] els = ext.getConfigurationElements();
			for (int j = 0; j < els.length; j++) {
				IConfigurationElement el = els[j];
				String name = el.getAttribute("name");
				if (name.equals(resourceName)) {
					String file = el.getAttribute("file");
					URL url = new URL(ext.getDeclaringPluginDescriptor().getInstallURL(), file);
					return url.openStream();
				}
			}
		}
		
		throw new IOException("Can't find named resource: " + resourceName);
	}

	private NamedResources() { }

}
