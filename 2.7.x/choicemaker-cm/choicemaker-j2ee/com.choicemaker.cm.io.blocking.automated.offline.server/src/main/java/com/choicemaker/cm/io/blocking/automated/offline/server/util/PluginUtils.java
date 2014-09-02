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
package com.choicemaker.cm.io.blocking.automated.offline.server.util;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author pcheung
 *
 */
public class PluginUtils {
	private static final Logger log = Logger.getLogger(PluginUtils.class.getName());

	public static void debugPlugins () {
		IPluginRegistry registry = Platform.getPluginRegistry();
		IPluginDescriptor[] plugins = registry.getPluginDescriptors();
		for (int i = 0; i < plugins.length; i++) {
			if (plugins[i].isPluginActivated()) {
				log.fine("Plugin active: " + plugins[i].getUniqueIdentifier());
			} else {
				log.fine("Plugin NOT active: " + plugins[i].getUniqueIdentifier());				
			}
		}
	}

}
