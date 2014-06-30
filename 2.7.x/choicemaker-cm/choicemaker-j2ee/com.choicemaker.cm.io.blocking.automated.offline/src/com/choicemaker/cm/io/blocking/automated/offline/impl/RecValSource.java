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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;

/**
 * @author pcheung
 *
 */
public class RecValSource extends BaseFileSource implements IRecValSource {
	
	private long nextRecID;
	private IntArrayList nextValues;

	//this is true if the lastest value read in has been used.
	private boolean usedID = true;

	public RecValSource (String fileName) {
		init (fileName, Constants.BINARY);
	}
	

	public RecValSource (String fileName, int type) {
		init (fileName, type);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource#hasNext()
	 */
	public boolean hasNext() throws BlockingException{
		if (this.usedID) {
			try {
				this.nextRecID = readNext();
				this.usedID = false;
			} catch (EOFException x) {
				this.nextRecID = 0;
				usedID = true;
			} catch (IOException ex) {
				throw new BlockingException (ex.toString());
			}
		}
		return !this.usedID;
	}


	private long readNext () throws EOFException, IOException {
		long ret = 0;
		if (type == Constants.BINARY) {
			ret = dis.readLong();
			
			int size = dis.readInt();
			int [] data = new int [size];

			for (int i=0; i<size ; i++) {
				data[i] = dis.readInt();
			}
			
			nextValues = new IntArrayList (size, data);

		} else if (type == Constants.STRING) {
			String str = br.readLine();
			//if there is a blank line, return false				
			if (str == null || str.equals("")) throw new EOFException ();

			int ind1 = 0;
			int ind2 = getNextLocation (str, ' ', ind1);
			ret = Long.parseLong(str.substring(ind1, ind2));
				
			ind1 = ind2 + 1;
			ind2 = getNextLocation (str, ' ', ind1);
			nextValues = new IntArrayList (1);
			while (ind2 != -1) {
				nextValues.add( Integer.parseInt(str.substring(ind1, ind2)) );
				ind1 = ind2 + 1;
				ind2 = getNextLocation (str, ' ', ind1);
			}
		}
		count ++;
		return ret;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource#getNextRecID()
	 */
	public long getNextRecID() {
		if (this.usedID) {
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
		this.usedID = true;

		return nextRecID;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource#getNextValues()
	 */
	public IntArrayList getNextValues() {
		if (this.nextValues == null) {
			try {
				nextRecID = readNext();
				usedID = false;
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"OABABlockingException: " + x.getMessage());
			}
		}
		IntArrayList retVal = this.nextValues;
		this.nextValues = null;

		return retVal;
	}

}
