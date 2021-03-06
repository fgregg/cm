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

import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.HistoryStore;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import java.io.InputStream;

public class FileState extends PlatformObject implements IFileState {
	protected long lastModified;
	protected UniversalUniqueIdentifier uuid;
	protected HistoryStore store;
	protected IPath fullPath;
public FileState(HistoryStore store, IPath fullPath, long lastModified, UniversalUniqueIdentifier uuid) {
	this.store = store;
	this.lastModified = lastModified;
	this.uuid = uuid;
	this.fullPath = fullPath;
}
/**
 * @see IFileState#exists
 */
public boolean exists() {
	return store.exists(this);
}
/**
 * @see IFileState#getContents
 */
public InputStream getContents() throws CoreException {
	return store.getContents(this);
}
/**
 * @see IFileState
 */
public IPath getFullPath() {
	return fullPath;
}
/**
 * @see IFileState#getModificationTime
 */
public long getModificationTime() {
	return lastModified;
}
/**
 * @see IFileState
 */
public String getName() {
	return fullPath.lastSegment();
}
public UniversalUniqueIdentifier getUUID() {
	return uuid;
}
/**
 * @see IFileState
 */
public boolean isReadOnly() {
	return true;
}
/**
 * Returns a string representation of this object. Used for debug only.
 */
public String toString() {
	StringBuffer s = new StringBuffer();
	s.append("uuid: ").append(uuid.toString()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	s.append("lastModified: ").append(lastModified).append("\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
	return s.toString();
}
}
