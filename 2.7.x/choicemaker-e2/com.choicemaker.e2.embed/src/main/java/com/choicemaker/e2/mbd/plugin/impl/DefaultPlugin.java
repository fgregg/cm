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

/**
 * Dummy plugin runtime class implementation
 */
import com.choicemaker.e2.mbd.runtime.IPluginDescriptor;
import com.choicemaker.e2.mbd.runtime.Plugin;

public class DefaultPlugin extends Plugin {

public DefaultPlugin(IPluginDescriptor descriptor) {
	super(descriptor);
}
}
