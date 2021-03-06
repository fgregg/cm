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
package org.eclipse.core.internal.resources;

import java.util.Map;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.team.IMoveDeleteHook;

/**
 * Provides special internal access to the workspace resource implementation.
 * This class is to be used for testing purposes only.
 * 
 * @since 2.0
 */
public class TestingSupport {

	/* 
	 * Class cannot be instantiated.
	 */
	private TestingSupport() {
	}

	/**
	 * Installs the given move-delete hook implementation in the given 
	 * workspace. This overrides the normal workspace behavior. Subsequent
	 * calls to <code>IResource.delete</code> and <code>move</code> will call
	 * the given hook instead of the one contributed to the extension point.
	 * Use <code>null</code> to restore the default workspace behavior.
	 * 
	 * @param workspace the workspace
	 * @param hook the hook implementation, or <code>null</code> to restore
	 *    the default workspace behavior
	 */
	public static void installMoveDeleteHook(IWorkspace workspace, IMoveDeleteHook hook) {
		Workspace ws = (Workspace)workspace;
		if (hook != null) {
			ws.moveDeleteHook = hook;
		} else {
			ws.moveDeleteHook = null;
			ws.initializeMoveDeleteHook();
		}
	}
	
	/**
	 * Returns a copy of the session properties for the given resource. If the resource
	 * is not accessible or any problems occur accessing it, then <code>null</code> is
	 * returned.
	 * 
	 * @param resource the resource to get the properties from
	 * @return the resource's session properties or <code>null</code>
	 * @since 2.1
	 */
	public static Map getSessionProperties(IResource resource) {
		ResourceInfo info = ((Resource) resource).getResourceInfo(true, false);
		if (info == null)
			return null;
		return info.sessionProperties == null ? null : (Map) info.sessionProperties.clone();
	}
}
