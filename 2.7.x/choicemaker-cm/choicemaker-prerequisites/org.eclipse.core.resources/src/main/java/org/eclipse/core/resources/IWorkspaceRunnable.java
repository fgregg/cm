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
package org.eclipse.core.resources;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

/**
 * A runnable which executes as a batch operation within the workspace.
 * The <code>IWorkspaceRunnable</code> interface should be implemented
 * by any class whose instances are intended to be run by
 * <code>IWorkspace.run</code>.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IWorkspace#run
 */
public interface IWorkspaceRunnable {
/**
 * Runs the operation reporting progress to and accepting
 * cancellation requests from the given progress monitor.
 * <p>
 * Implementors of this method should check the progress monitor
 * for cancellation when it is safe and appropriate to do so.  The cancellation
 * request should be propagated to the caller by throwing 
 * <code>OperationCanceledException</code>.
 * </p>
 * 
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this operation fails.
 */
public void run(IProgressMonitor monitor) throws CoreException;
}
