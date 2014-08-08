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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.Constants;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.RecordIdentifierType;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object handles reading MatchRecord objects from a file.
 * 
 * @author pcheung
 *
 */
public class MatchRecord2Source
	extends BaseFileSource
	implements IMatchRecord2Source {

	private MatchRecord2 next = null;

	/** This constructor takes in the filename of the source and an indicator telling it
	 * if the data is stored as strings or binary. 
	 * 
	 * @param fileName
	 * @param type
	 */
	public MatchRecord2Source(String fileName, int type) {
		init(fileName, type);
	}

	/** This gets the next available MatchRecord2 from the source.
	 * 
	 * @return MatchRecord2
	 * @throws OABABlockingException
	 */
	private MatchRecord2 readNext() throws EOFException, IOException {
		MatchRecord2 retVal = null;

		if (type == Constants.STRING) {
			retVal = readMatchRecord(br);

		} else if (type == Constants.BINARY) {

			Comparable c1 = readIDBinary();
			Comparable c2 = readIDBinary();

			float f = dis.readFloat();
			char type = dis.readChar();
			char source = dis.readChar();

			String info = readInfo();

			retVal = new MatchRecord2(c1, c2, source, f, type, info);
		}


		return retVal;
	}

	/** This checks o see if there is any more elements in the source. 
	 * Always call hasNext before calling getNext ().
	 * 
	 * @return boolean - true if there are more elements in the source.
	 */
	public boolean hasNext() throws BlockingException {
		if (this.next == null) {
			try {
				this.next = readNext();
			} catch (EOFException x) {
				this.next = null;
			} catch (IOException x) {
				throw new BlockingException ();
			}
		}
		return this.next != null;
	}

	/** This method takes in a BufferedReader and reads the next MatchRecord2 object.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static MatchRecord2 readMatchRecord(BufferedReader reader)
		throws IOException {
		MatchRecord2 mr = null;

		String str = reader.readLine();
		
		if ((str != null) && (str.length() > 0)) {
			StringTokenizer st = new StringTokenizer(str);

			int dataType = Integer.parseInt(st.nextToken());
			Comparable c1 = readIDString(dataType, st.nextToken());

			dataType = Integer.parseInt(st.nextToken());
			Comparable c2 = readIDString(dataType, st.nextToken());

			float f = Float.parseFloat(st.nextToken());

			char tt = st.nextToken().charAt(0);

			char source = st.nextToken().charAt(0);

			String info = null;
			if (st.hasMoreTokens()) {
				info = st.nextToken();
			}

			mr = new MatchRecord2(c1, c2, source, f, tt, info);
		}
		return mr;
	}

	private static Comparable readIDString(int dataType, String data)
		throws EOFException, IOException {
		Comparable c = null;

		if (dataType == RecordIdentifierType.TYPE_INTEGER.typeId) {
			c = new Integer(data);
		} else if (dataType == RecordIdentifierType.TYPE_LONG.typeId) {
			c = new Long(data);
		} else if (dataType == RecordIdentifierType.TYPE_STRING.typeId) {
			c = data;
		}

		return c;
	}

	private Comparable readIDBinary() throws EOFException, IOException {
		Comparable c = null;

		int dataType = dis.readInt();

		if (dataType == RecordIdentifierType.TYPE_INTEGER.typeId) {
			c = new Integer(dis.readInt());
		} else if (dataType == RecordIdentifierType.TYPE_LONG.typeId) {
			c = new Long(dis.readLong());
		} else if (dataType == RecordIdentifierType.TYPE_STRING.typeId) {
			int size = dis.readInt();
			char[] data = new char[size];
			for (int i = 0; i < size; i++) {
				data[i] = dis.readChar();
			}
			c = new String(data);
		}

		return c;
	}

	private String readInfo() throws EOFException, IOException {
		String c = null;

		int size = dis.readInt();

		if (size == 0) {
			return null;
		} else {
			char[] data = new char[size];
			for (int i = 0; i < size; i++) {
				data[i] = dis.readChar();
			}
			c = new String(data);
		}

		return c;
	}

	/** This method gets the next MatchRecord2 from the source.  You should call
	 * hasNext () to check if there is any more elements before calling getNext ().
	 * 
	 */
	public MatchRecord2 getNext() {
		if (this.next == null) {
			try {
				this.next = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException(
					"EOFException: " + x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException(
					"OABABlockingException: " + x.getMessage());
			}
		}
		MatchRecord2 retVal = this.next;
		count ++;
		
		this.next = null;

		return retVal;
	} // getNext()

	public int getCount() {
		return count;
	}

}
