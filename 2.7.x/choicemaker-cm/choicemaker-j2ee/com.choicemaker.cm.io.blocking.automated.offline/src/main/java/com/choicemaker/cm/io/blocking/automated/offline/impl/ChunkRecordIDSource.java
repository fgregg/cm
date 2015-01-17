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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIdSource;

/**
 * @author pcheung
 *
 */
public class ChunkRecordIDSource extends BaseFileSource<Long> implements
		IChunkRecordIdSource {

	private long nextRecID;

	// this is true if the lastest value read in has been used.
	private boolean used = true;

	public ChunkRecordIDSource(String fileName) {
		super(fileName, EXTERNAL_DATA_FORMAT.BINARY);
	}

	@Deprecated
	public ChunkRecordIDSource(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public ChunkRecordIDSource(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	/**
	 * This returns the next id from the source.
	 * 
	 * @return long
	 * @throws OABABlockingException
	 * @throws EOFException
	 */
	private long readNext() throws EOFException, IOException {
		long ret = 0;

		if (type == EXTERNAL_DATA_FORMAT.STRING) {
			String str = br.readLine();
			if (str != null && !str.equals("")) {
				ret = Long.parseLong(str);
			} else {
				throw new EOFException();
			}
		} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {
			ret = dis.readLong();
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRowSource
	 * #hasNext()
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		if (this.used) {
			try {
				this.nextRecID = readNext();
				this.used = false;
			} catch (EOFException x) {
				this.nextRecID = 0;
				used = true;
			} catch (IOException ex) {
				throw new BlockingException(ex.toString());
			}
		}
		return !this.used;
	}

	@Override
	public Long next() {
		if (this.used) {
			try {
				this.nextRecID = readNext();
			} catch (IOException x) {
				throw new NoSuchElementException(x.toString());
			}
		}
		this.used = true;
		count++;

		return nextRecID;
	}

}
