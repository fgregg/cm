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
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRecordIDSource;

/**
 * @author pcheung
 *
 */
public class ChunkRecordIDSource extends BaseFileSource implements IChunkRecordIDSource {

	private long nextRecID;
	
	//this is true if the lastest value read in has been used.
	private boolean used = true;


	public ChunkRecordIDSource (String fileName) {
		init (fileName, Constants.BINARY);
	}
	

	public ChunkRecordIDSource (String fileName, int type) {
		init (fileName, type);
	}
	

	/**
	 * This returns the next id from the source.
	 * 
	 * @return long
	 * @throws OABABlockingException
	 * @throws EOFException
	 */
	private long readNext () throws EOFException, IOException {
		long ret = 0;
	
		if (type == Constants.STRING) {
			String str = br.readLine();
			if (str != null && !str.equals("")) {
				ret = Long.parseLong(str);
			} else {
				throw new EOFException ();
			}
		} else if (type == Constants.BINARY) {
			ret = dis.readLong() ;
		}


		return ret;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRowSource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		if (this.used) {
			try {
				this.nextRecID = readNext();
				this.used = false;
			} catch (EOFException x) {
				this.nextRecID = 0;
				used = true;
			} catch (IOException ex) {
				throw new BlockingException (ex.toString());
			}
		}
		return !this.used;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IChunkRowSource#getNext()
	 */
	public long getNext() throws BlockingException {
		if (this.used) {
			try {
				this.nextRecID = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"IOFException: " + x.getMessage());
			}
		}
		this.used = true;
		count ++;

		return nextRecID;
	}


}
