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
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object handles reading MatchRecord objects from a file.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class MatchRecord2Source<T extends Comparable<T>> extends
		BaseFileSource<MatchRecord2<T>> implements IMatchRecord2Source<T> {

	private MatchRecord2 next = null;

	/**
	 * This constructor takes in the filename of the source and an indicator
	 * telling it if the data is stored as strings or binary.
	 * 
	 * @param fileName
	 * @param type
	 */
	@Deprecated
	public MatchRecord2Source(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public MatchRecord2Source(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	/**
	 * This gets the next available MatchRecord2 from the source.
	 */
	private MatchRecord2<T> readNext() throws EOFException, IOException {

		MatchRecord2<T> retVal = null;
		if (type == EXTERNAL_DATA_FORMAT.STRING) {
			retVal = readMatchRecord(br);

		} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {
			Comparable c1 = readIDBinary();
			Comparable c2 = readIDBinary();
			float f = dis.readFloat();
			char cDecision = dis.readChar();
			Decision d = Decision.valueOf(cDecision);
			char cRole = dis.readChar();
			RECORD_SOURCE_ROLE role = RECORD_SOURCE_ROLE.fromSymbol(cRole);
			String notes = readInfo();
			retVal = new MatchRecord2(c1, c2, role, f, d, notes);
		}

		return retVal;
	}

	/**
	 * This checks o see if there is any more elements in the source. Always
	 * call hasNext before calling getNext ().
	 * 
	 * @return boolean - true if there are more elements in the source.
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		if (this.next == null) {
			try {
				this.next = readNext();
			} catch (EOFException x) {
				this.next = null;
			} catch (IOException x) {
				throw new BlockingException();
			}
		}
		return this.next != null;
	}

	/**
	 * This method takes in a BufferedReader and reads the next MatchRecord2
	 * object.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static MatchRecord2 readMatchRecord(BufferedReader reader)
			throws IOException {

		String str = reader.readLine();
		MatchRecord2 retVal = null;
		if ((str != null) && (str.length() > 0)) {
			StringTokenizer st = new StringTokenizer(str);

			// First record identifier
			int i = Integer.parseInt(st.nextToken());
			RECORD_ID_TYPE dataType = RECORD_ID_TYPE.fromSymbol(i);
			Comparable c1 = readIDString(dataType, st.nextToken());

			// Second record identifier
			i = Integer.parseInt(st.nextToken());
			assert dataType == RECORD_ID_TYPE.fromSymbol(i);
			Comparable c2 = readIDString(dataType, st.nextToken());

			// Probability, decision and delimited notes
			float f = Float.parseFloat(st.nextToken());
			char c = st.nextToken().charAt(0);
			Decision d = Decision.valueOf(c);

			c = st.nextToken().charAt(0);
			RECORD_SOURCE_ROLE role = RECORD_SOURCE_ROLE.fromSymbol(c);

			String notes = null;
			if (st.hasMoreTokens()) {
				notes = st.nextToken();
			}

			retVal = new MatchRecord2(c1, c2, role, f, d, notes);
		}
		return retVal;
	}

	private static Comparable readIDString(RECORD_ID_TYPE dataType, String data)
			throws EOFException, IOException {
		Comparable c = null;

		if (dataType == RECORD_ID_TYPE.TYPE_INTEGER) {
			c = new Integer(data);
		} else if (dataType == RECORD_ID_TYPE.TYPE_LONG) {
			c = new Long(data);
		} else if (dataType == RECORD_ID_TYPE.TYPE_STRING) {
			c = data;
		}

		return c;
	}

	private Comparable readIDBinary() throws EOFException, IOException {
		Comparable c = null;

		int s = dis.readInt();
		RECORD_ID_TYPE dataType = RECORD_ID_TYPE.fromSymbol(s);

		if (dataType == RECORD_ID_TYPE.TYPE_INTEGER) {
			c = new Integer(dis.readInt());
		} else if (dataType == RECORD_ID_TYPE.TYPE_LONG) {
			c = new Long(dis.readLong());
		} else if (dataType == RECORD_ID_TYPE.TYPE_STRING) {
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

	@Override
	public MatchRecord2<T> next() {
		if (this.next == null) {
			try {
				this.next = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException("EOFException: "
						+ x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException("OABABlockingException: "
						+ x.getMessage());
			}
		}
		MatchRecord2 retVal = this.next;
		count++;

		this.next = null;

		return retVal;
	} // getNext()

	@Override
	public int getCount() {
		return count;
	}

}
