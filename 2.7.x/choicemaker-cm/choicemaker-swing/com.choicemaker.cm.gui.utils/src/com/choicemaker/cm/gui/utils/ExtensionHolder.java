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

import org.eclipse.core.runtime.*;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class ExtensionHolder {
	private IExtension extension;
	private String name;
	
	public static ExtensionHolder[] getExtensionHolders(IExtensionPoint extensionPoint) {
		IExtension[] extensions = extensionPoint.getExtensions();
		ExtensionHolder[] res = new ExtensionHolder[extensions.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = new ExtensionHolder(extensions[i]);
		}
		return res;
	}
	
	public ExtensionHolder(IExtension extension, String name) {
		this.extension = extension;
		this.name = name;
	}
	
	public ExtensionHolder(IExtension extension) {
		this.extension = extension;
		this.name = extension.getConfigurationElements()[0].getAttribute("name");
	}
	
	public Object getInstance() throws CoreException {
		return extension.getConfigurationElements()[0].createExecutableExtension("class");
	}
	
	/**
	 * @return
	 */
	public IExtension getExtension() {
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
