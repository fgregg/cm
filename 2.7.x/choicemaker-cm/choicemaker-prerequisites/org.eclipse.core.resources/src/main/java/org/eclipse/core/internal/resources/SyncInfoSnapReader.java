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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
public class SyncInfoSnapReader {
	protected Workspace workspace;
	protected Synchronizer synchronizer;
public SyncInfoSnapReader(Workspace workspace, Synchronizer synchronizer) {
	super();
	this.workspace = workspace;
	this.synchronizer = synchronizer;
}
/**
 * Returns the appropriate reader for the given version.
 */
protected SyncInfoSnapReader getReader(int formatVersion) throws IOException {
	switch (formatVersion) {
		case 3 :
			return new SyncInfoSnapReader_3(workspace, synchronizer);
		default :
			throw new IOException(Policy.bind("resources.format")); //$NON-NLS-1$
	}
}
public void readPartners(DataInputStream input) throws CoreException {
	try {
		int size = input.readInt();
		Set registry = new HashSet(size);
		for (int i = 0; i < size; i++) {
			String qualifier = input.readUTF();
			String local = input.readUTF();
			registry.add(new QualifiedName(qualifier, local));
		}
		synchronizer.setRegistry(registry);
	} catch (IOException e) {
		String message = Policy.bind("resources.readSync", e.toString()); //$NON-NLS-1$
		throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, message));
	}
}
public void readSyncInfo(DataInputStream input) throws IOException {
	// dispatch to the appropriate reader depending
	// on the version of the file
	int formatVersion = readVersionNumber(input);
	SyncInfoSnapReader reader = getReader(formatVersion);
	reader.readSyncInfo(input);
}
protected static int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
}
