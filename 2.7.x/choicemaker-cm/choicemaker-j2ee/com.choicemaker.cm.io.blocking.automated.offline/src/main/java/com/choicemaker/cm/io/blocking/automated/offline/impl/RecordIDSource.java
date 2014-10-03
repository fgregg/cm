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
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RecordIDSource extends BaseFileSource implements IRecordIDSource {

	protected int dataType;
	protected Comparable nextID;
	private boolean isFirst = true;

	public RecordIDSource (String fileName, int type) {
		init (fileName, type);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource#getNextID()
	 */
	public Comparable getNextID() throws BlockingException {
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
		Comparable retVal = this.nextID;
		count ++;
		
		this.nextID = null;

		return retVal;
	}
	
	
	private Comparable readNext () throws EOFException, IOException {
		Comparable ret = null;
		
		if (type == Constants.STRING) {
			String str;
				
			if (isFirst) {
				str = br.readLine();
				if (str != null && !str.equals("")) 
					dataType = Integer.parseInt(str);
				isFirst = false;
			}
		
			str = br.readLine();
				
			if (str != null && !str.equals("")) {
				if (dataType == Constants.TYPE_INTEGER) {
					ret = new Integer (str);
				} else if (dataType == Constants.TYPE_LONG) {
					ret = new Long (str);
				} else if (dataType == Constants.TYPE_STRING) {
					ret = str;
				}
			} else {
				throw new EOFException ();
			}
				
		} else if (type == Constants.BINARY) {
			if (isFirst) {
				dataType = dis.readInt();
				isFirst = false;
			}
				
			if (dataType == Constants.TYPE_INTEGER) {
				int i = dis.readInt();
				ret = new Integer (i);
			} else if (dataType == Constants.TYPE_LONG) {
				long l = dis.readLong();
				ret = new Long (l);
			} else if (dataType == Constants.TYPE_STRING) {
				int size = dis.readInt();
				char[] data = new char[size];
				for (int i=0; i< size; i++) {
					data[i] = dis.readChar();
				}
				ret = new String (data);
			}
		}
		return ret;
	}
	
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSource#getType()
	 */
	public int getRecordIDType() throws BlockingException {
		return dataType;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		if (this.nextID == null) {
			try {
				this.nextID = readNext();
			} catch (EOFException x) {
				this.nextID = null;
			} catch (IOException x) {
				throw new BlockingException (x.toString());
			}
		}
		return this.nextID != null;
	}



}
