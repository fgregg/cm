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

package com.choicemaker.e2.mbd.core.internal.plugins;

import java.net.URL;

import com.choicemaker.e2.mbd.core.runtime.model.PluginFragmentModel;

public class FragmentDescriptor extends PluginFragmentModel {

	// constants
//	static final String FRAGMENT_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLFragmentConnection.FRAGMENT + "/"; //$NON-NLS-1$ //$NON-NLS-2$

public String toString() {
	return getId() + PluginDescriptor.VERSION_SEPARATOR + getVersion();
}
public URL getInstallURL() {
//	try {
		return null; // PlatformURLFactory.createURL(FRAGMENT_URL + toString() + "/"); //$NON-NLS-1$
//	} catch (MalformedURLException e) {
//		throw new IllegalStateException(); // unchecked
//	}
}
}
