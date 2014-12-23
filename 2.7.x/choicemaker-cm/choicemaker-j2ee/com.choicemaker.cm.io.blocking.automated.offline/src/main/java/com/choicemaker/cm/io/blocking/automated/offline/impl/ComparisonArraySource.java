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
import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;

/**
 * @author pcheung
 *
 */
//@SuppressWarnings({"rawtypes", "unchecked"})
public class ComparisonArraySource<T extends Comparable<T>> extends
		BaseFileSource<ComparisonArray<T>> implements IComparisonArraySource<T> {

	private ComparisonArray<T> nextGroup;

	public ComparisonArraySource (String fileName, int type) {
		init (fileName, type);
	}

	@Override
	public ComparisonArray<T> next() {
		return getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonGroupSource#getNext()
	 */
	public ComparisonArray<T> getNext() {
		if (this.nextGroup == null) {
			try {
				this.nextGroup = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"OABABlockingException: " + x.getMessage());
			}
		}
		ComparisonArray<T> retVal = this.nextGroup;
		count ++;
		this.nextGroup = null;

		return retVal;
	}
	
	
	@SuppressWarnings("unchecked")
	private ArrayList<T> readArray (int dataType) throws IOException {
		//read the number of records
		String str = br.readLine();
		int size = Integer.parseInt(str);
		
		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList<>(size);
		for (int i=0; i<size; i++) {
			str = br.readLine();
			
			if (dataType == Constants.TYPE_INTEGER) {
				Integer I = new Integer (str);
				list.add(I);
			} else if (dataType == Constants.TYPE_LONG) {
				Long L = new Long (str);
				list.add(L);
			} else if (dataType == Constants.TYPE_STRING) {
				list.add(str);
			}
		}
		
		return list;
	}


	@SuppressWarnings("unchecked")
	private ArrayList<T> readArrayBinary (int dataType) throws IOException {
		//read the number of records
		int size = dis.readInt();
		
		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList (size);
		for (int i=0; i<size; i++) {
			if (dataType == Constants.TYPE_INTEGER) {
				Integer I = new Integer ( dis.readInt() );
				list.add(I);
			} else if (dataType == Constants.TYPE_LONG) {
				Long L = new Long ( dis.readLong() );
				list.add(L);
			} else if (dataType == Constants.TYPE_STRING) {
				int s = dis.readInt();
				
				char[] data = new char[s];
				for (int j=0; j< s; j++) {
					data[j] = dis.readChar();
				}
				String temp = new String (data); 
				list.add(temp);
			}
		}
		
		return list;
	}
	
	
	private ComparisonArray<T> readNext () throws EOFException, IOException {
		ComparisonArray<T> ret = null;
		
		if (type == Constants.STRING) {
			String str;
				
			//read the data type for staging record
			str = br.readLine();
				
			if (str == null) throw new EOFException ();
				
			int stageType = Integer.parseInt(str);
				
			//read the staging array
			ArrayList<T> stage = (ArrayList<T>) readArray (stageType);
				
			//read the master id type
			str = br.readLine();
			int masterType = Integer.parseInt(str);
				
			//read the staging array
			ArrayList<T> master = (ArrayList<T>) readArray (masterType);
				
			ret = new ComparisonArray<T>(stage, master, stageType, masterType);								

		} else if (type == Constants.BINARY) {

			//read the data type for staging record
			int stageType = dis.readInt();
				
			//read the staging array
			ArrayList<T> stage = readArrayBinary (stageType);

			//read the data type for master record
			int masterType = dis.readInt();
				
			//read the master array
			ArrayList<T> master = readArrayBinary (masterType);

			ret = new ComparisonArray<T>(stage, master, stageType, masterType);				

		}
		return ret;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		if (this.nextGroup == null) {
			try {
				this.nextGroup = readNext();
			} catch (EOFException x) {
				this.nextGroup = null;
			} catch (IOException x) {
				throw new BlockingException (x.toString());
			}
		}
		return this.nextGroup != null;
	}

}
