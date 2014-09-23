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
package com.choicemaker.cm.gui.utils;

import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.CMExtensionPoint;
import com.choicemaker.e2.E2Exception;

/**
 * Comment
 *
 * @author Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class ExtensionHolder {
	private CMExtension extension;
	private String name;

	/**
	 * Returns all the extensions implemented against a specified extension
	 * point
	 */
	public static ExtensionHolder[] getExtensionsOfExtensionPoint(
			CMExtensionPoint extensionPoint) {
		CMExtension[] extensions = extensionPoint.getExtensions();
		ExtensionHolder[] res = new ExtensionHolder[extensions.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = new ExtensionHolder(extensions[i]);
		}
		return res;
	}

//	/**
//	 * Returns all the extensions implemented by a specified plugin against any
//	 * extension point
//	 */
//	public static ExtensionHolder[] getExtensionsOfPlugin(String pluginId) {
//		CMExtension[] extensions =
//			CMPlatformUtils.getPluginExtensions(pluginId);
//		ExtensionHolder[] res = new ExtensionHolder[extensions.length];
//		for (int i = 0; i < res.length; i++) {
//			res[i] = new ExtensionHolder(extensions[i]);
//		}
//		return res;
//	}

	public ExtensionHolder(CMExtension extension, String name) {
		this.extension = extension;
		this.name = name;
	}

	public ExtensionHolder(CMExtension extension) {
		this.extension = extension;
		this.name =
			extension.getConfigurationElements()[0].getAttribute("name");
	}

	public Object getInstance() throws E2Exception {
		return extension.getConfigurationElements()[0]
				.createExecutableExtension("class");
	}

	/**
	 * @return
	 */
	public CMExtension getExtension() {
		return extension;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
}
