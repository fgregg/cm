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
package org.eclipse.core.internal.indexing;

import java.io.*;
import java.util.*;

class LogWriter {

	protected FileOutputStream out;
	protected PageStore pageStore;

	/**
	 * Puts the modified pages to the log file.
	 */
	public static void putModifiedPages(PageStore pageStore, Map modifiedPages) throws PageStoreException {
		LogWriter writer = new LogWriter();
		writer.open(pageStore);
		writer.putModifiedPages(modifiedPages);
		writer.close();
	}

	/**
	 * Opens the log.
	 */
	protected void open(PageStore pageStore) throws PageStoreException {
		this.pageStore = pageStore;
		try {
			out = new FileOutputStream(Log.name(pageStore.getName()));
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogOpenFailure, e);
		}
	}

	/**
	 * Closes the log.
	 */
	protected void close() {
		try {
			out.close();
		} catch (IOException e) {
		}
		out = null;
	}

	/**
	 * Puts the modified pages into the log.
	 */
	protected void putModifiedPages(Map modifiedPages) throws PageStoreException {
		Buffer b4 = new Buffer(4);
		byte[] pageBuffer = new byte[Page.SIZE];
		int numberOfPages = modifiedPages.size();
		b4.put(0, 4, numberOfPages);
		try {
			write(b4.getByteArray());
			Iterator pageStream = modifiedPages.values().iterator();
			while (pageStream.hasNext()) {
				Page page = (Page) pageStream.next();
				int pageNumber = page.getPageNumber();
				b4.put(0, 4, pageNumber);
				write(b4.getByteArray());
				page.toBuffer(pageBuffer);
				write(pageBuffer);
			}
		} catch (IOException e) {
			throw new PageStoreException(PageStoreException.LogWriteFailure, e);
		}
	}
	
	public void write(byte[] buffer) throws IOException {
		out.write(buffer);
	}


}
