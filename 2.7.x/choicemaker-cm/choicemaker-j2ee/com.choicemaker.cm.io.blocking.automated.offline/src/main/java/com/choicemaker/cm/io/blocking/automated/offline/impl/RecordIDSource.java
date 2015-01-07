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
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({"unchecked"})
public class RecordIDSource<T extends Comparable<T>> extends BaseFileSource<T>
		implements IRecordIDSource<T> {

	protected RECORD_ID_TYPE dataType;
	protected T nextID;
	private boolean isFirst = true;

	@Deprecated
	public RecordIDSource(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public RecordIDSource(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	public T next() {
		if (this.nextID == null) {
			try {
				this.nextID = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"BlockingException: " + x.getMessage());
			}
		}
		T retVal = this.nextID;
		count ++;
		
		this.nextID = null;

		return retVal;
	}
	
	
	private T readNext () throws EOFException, IOException {
		T ret = null;
		
		if (type == EXTERNAL_DATA_FORMAT.STRING) {
			String str;
				
			if (isFirst) {
				str = br.readLine();
				if (str != null && !str.equals("")) 
					dataType = RECORD_ID_TYPE.fromSymbol(Integer.parseInt(str));
				isFirst = false;
			}
		
			str = br.readLine();
				
			if (str != null && !str.equals("")) {
				if (dataType == RECORD_ID_TYPE.TYPE_INTEGER) {
					ret = (T) new Integer (str);
				} else if (dataType == RECORD_ID_TYPE.TYPE_LONG) {
					ret = (T) new Long (str);
				} else if (dataType == RECORD_ID_TYPE.TYPE_STRING) {
					ret = (T) str;
				}
			} else {
				throw new EOFException ();
			}
				
		} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {
			if (isFirst) {
				dataType = RECORD_ID_TYPE.fromSymbol(dis.readInt());
				isFirst = false;
			}
				
			if (dataType == RECORD_ID_TYPE.TYPE_INTEGER) {
				int i = dis.readInt();
				ret = (T) new Integer (i);
			} else if (dataType == RECORD_ID_TYPE.TYPE_LONG) {
				long l = dis.readLong();
				ret = (T) new Long (l);
			} else if (dataType == RECORD_ID_TYPE.TYPE_STRING) {
				int size = dis.readInt();
				char[] data = new char[size];
				for (int i=0; i< size; i++) {
					data[i] = dis.readChar();
				}
				ret = (T) new String (data);
			}
		}
		return ret;
	}

	public RECORD_ID_TYPE getRecordIDType() throws BlockingException {
		return dataType;
	}

	public boolean hasNext() throws BlockingException {
		if (this.nextID == null) {
			try {
				this.nextID = readNext();
			} catch (EOFException x) {
				this.nextID = null;
			} catch (IOException x) {
				throw new BlockingException(x.toString());
			}
		}
		return this.nextID != null;
	}

}
