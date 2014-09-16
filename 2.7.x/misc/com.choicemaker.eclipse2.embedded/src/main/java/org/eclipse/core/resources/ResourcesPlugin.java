/**
 * @(#)$RCSfile: ResourcesPlugin.java,v $  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 * 
 * Copyright (c) 2003 ChoiceMaker Technologies, Inc. 
 * 71 W 23rd St, Ste 515, New York, NY 10010 
 * All rights reserved.
 * 
 * This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

package org.eclipse.core.resources;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/27 03:58:28 $
 */
public class ResourcesPlugin extends Plugin {
	public ResourcesPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
	}
	public static IWorkspace getWorkspace() {
		throw new UnsupportedOperationException("Workspace not supported in single-jar runtime.");
	}
}
