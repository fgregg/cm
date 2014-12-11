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
package com.choicemaker.e2.utils;

import java.util.logging.Logger;

import com.choicemaker.e2.CMPluginDescriptor;
import com.choicemaker.e2.platform.CMPlatformUtils;

/**
 * @author pcheung
 *
 */
public class PluginUtils {

	private static final Logger log = Logger.getLogger(PluginUtils.class
			.getName());

	public static void debugPlugins() {
		CMPluginDescriptor[] plugins = CMPlatformUtils.getPluginDescriptors();
		for (int i = 0; i < plugins.length; i++) {
			if (plugins[i].isPluginActivated()) {
				log.fine("Plugin active: " + plugins[i].getUniqueIdentifier());
			} else {
				log.fine("Plugin NOT active: "
						+ plugins[i].getUniqueIdentifier());
			}
		}
	}

}
