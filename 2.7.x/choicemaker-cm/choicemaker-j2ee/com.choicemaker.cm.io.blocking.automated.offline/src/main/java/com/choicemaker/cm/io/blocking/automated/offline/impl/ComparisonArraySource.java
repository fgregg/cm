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

import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_INTEGER;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_LONG;
import static com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE.TYPE_STRING;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.EXTERNAL_DATA_FORMAT;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * @author pcheung
 *
 */
// @SuppressWarnings({"rawtypes", "unchecked"})
public class ComparisonArraySource<T extends Comparable<T>> extends
		BaseFileSource<ComparisonArray<T>> implements IComparisonArraySource<T> {

	private ComparisonArray<T> nextGroup;

	@Deprecated
	public ComparisonArraySource(String fileName, int type) {
		super(fileName, EXTERNAL_DATA_FORMAT.fromSymbol(type));
	}

	public ComparisonArraySource(String fileName, EXTERNAL_DATA_FORMAT type) {
		super(fileName, type);
	}

	@Override
	public ComparisonArray<T> next() {
		if (this.nextGroup == null) {
			try {
				this.nextGroup = readNext();
			} catch (EOFException x) {
				throw new NoSuchElementException("EOFException: "
						+ x.getMessage());
			} catch (IOException x) {
				throw new NoSuchElementException("OABABlockingException: "
						+ x.getMessage());
			}
		}
		ComparisonArray<T> retVal = this.nextGroup;
		count++;
		this.nextGroup = null;

		return retVal;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<T> readArray(RECORD_ID_TYPE dataType) throws IOException {
		// read the number of records
		String str = br.readLine();
		int size = Integer.parseInt(str);

		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			str = br.readLine();

			if (dataType == TYPE_INTEGER) {
				Integer I = new Integer(str);
				list.add(I);
			} else if (dataType == TYPE_LONG) {
				Long L = new Long(str);
				list.add(L);
			} else if (dataType == TYPE_STRING) {
				list.add(str);
			}
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<T> readArrayBinary(RECORD_ID_TYPE dataType)
			throws IOException {
		// read the number of records
		int size = dis.readInt();

		@SuppressWarnings("rawtypes")
		ArrayList list = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			if (dataType == TYPE_INTEGER) {
				Integer I = new Integer(dis.readInt());
				list.add(I);
			} else if (dataType == TYPE_LONG) {
				Long L = new Long(dis.readLong());
				list.add(L);
			} else if (dataType == TYPE_STRING) {
				int s = dis.readInt();

				char[] data = new char[s];
				for (int j = 0; j < s; j++) {
					data[j] = dis.readChar();
				}
				String temp = new String(data);
				list.add(temp);
			}
		}

		return list;
	}

	private ComparisonArray<T> readNext() throws EOFException, IOException {
		ComparisonArray<T> ret = null;

		if (type == EXTERNAL_DATA_FORMAT.STRING) {
			String str;

			// read the data type for staging record
			str = br.readLine();

			if (str == null)
				throw new EOFException();

			int i = Integer.parseInt(str);
			RECORD_ID_TYPE stageType = RECORD_ID_TYPE.fromSymbol(i);

			// read the staging array
			ArrayList<T> stage = readArray(stageType);

			// read the master id type
			str = br.readLine();
			i = Integer.parseInt(str);
			RECORD_ID_TYPE masterType = RECORD_ID_TYPE.fromSymbol(i);

			// read the staging array
			ArrayList<T> master = readArray(masterType);

			ret = new ComparisonArray<T>(stage, master, stageType, masterType);

		} else if (type == EXTERNAL_DATA_FORMAT.BINARY) {

			// read the data type for staging record
			int i = dis.readInt();
			RECORD_ID_TYPE stageType = RECORD_ID_TYPE.fromSymbol(i);

			// read the staging array
			ArrayList<T> stage = readArrayBinary(stageType);

			// read the data type for master is.readInt();
			RECORD_ID_TYPE masterType = RECORD_ID_TYPE.fromSymbol(i);

			// read the master array
			ArrayList<T> master = readArrayBinary(masterType);

			ret = new ComparisonArray<T>(stage, master, stageType, masterType);

		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		if (this.nextGroup == null) {
			try {
				this.nextGroup = readNext();
			} catch (EOFException x) {
				this.nextGroup = null;
			} catch (IOException x) {
				throw new BlockingException(x.toString());
			}
		}
		return this.nextGroup != null;
	}

}
