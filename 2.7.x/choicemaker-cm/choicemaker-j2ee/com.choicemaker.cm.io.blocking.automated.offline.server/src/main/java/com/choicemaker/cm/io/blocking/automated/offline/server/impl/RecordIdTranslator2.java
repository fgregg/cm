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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.ArrayList;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * A cached collection of translated record identifiers. (Persistent
 * translations are maintained by
 * {@link com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaRecordIdController
 * OabaRecordIdController}).
 * <p>
 * This version can fail because it assumes that the same number of
 * record ids comes back from the database each time and in exactly the same
 * order. However, the OABA currently pulls records from the database three times
 * (during OABA blocking and chunking, and then during transitivity analysis).
 * The database can change between these pulls, and in some installations does,
 * which changes the number and ordering of the record ids that are retrieved.
 * </p>
 * @author pcheung
 * @deprecated see RecordIdTranslator3
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
class RecordIdTranslator2 implements IRecordIdTranslator2 {

	private final IRecordIdSinkSourceFactory rFactory;

	// These two files store the input record ids. The first record id
	// corresponds to internal id 0, etc.
	private final IRecordIdSink sink1;
	private final IRecordIdSink sink2;

	// This is an indicator of the class type of record id.
	private RECORD_ID_TYPE recordIdType;

	/**
	 * This contains the range of the record ID in sink1. range1[0] is min and
	 * range1[1] is max.
	 */
	private Comparable[] range1 = new Comparable[2];

	/**
	 * This contains the range of the record ID in sink2. range2[0] is min and
	 * range2[1] is max.
	 */
	private Comparable[] range2 = new Comparable[2];

	/**
	 * This contains the mapping from input record id I to internal record id J.
	 * mapping[J] = I. J starts from 0.
	 */
	private int currentIndex = -1;

	/**
	 * This is the point at which the second record source record ids start. if
	 * this is 0, it means there is only 1 record source.
	 */
	private int splitIndex = 0;

	// indicates whether initReverseTranslation has happened.
	private boolean initialized = false;

	// These two lists are use during reverse translation.
	private ArrayList list1;
	private ArrayList list2;

	RecordIdTranslator2(IRecordIdSinkSourceFactory rFactory)
			throws BlockingException {
		if (rFactory == null) {
			throw new IllegalArgumentException("null factory");
		}
		this.rFactory = rFactory;

		this.sink1 = rFactory.getNextSink();
		if (this.sink1 == null) {
			throw new IllegalStateException("null sink (1)");
		}

		this.sink2 = rFactory.getNextSink();
		if (this.sink2 == null) {
			throw new IllegalStateException("null sink (2)");
		}

		this.range1[0] = null;
		this.range1[1] = null;

		this.range2[0] = null;
		this.range2[1] = null;
	}

	@Override 
	public void cleanUp() throws BlockingException {
		list1 = null;
		list2 = null;

		sink1.remove();
		if (splitIndex > 0)
			sink2.remove();
	}

	@Override 
	public void close() throws BlockingException {
		if (splitIndex == 0)
			sink1.close();
		else
			sink2.close();
	}

	/**
	 * This returns an ArrayList of record IDs from the first source. Usually,
	 * the staging source.
	 */
	@Override 
	public ArrayList getList1() {
		return list1;
	}

	/**
	 * This returns an ArrayList of record IDs from the second source. Usually,
	 * the master source.
	 */
	@Override 
	public ArrayList getList2() {
		return list2;
	}

	// @Override
	public Comparable[] getRange1() {
		return range1;
	}

	// @Override
	public Comparable[] getRange2() {
		return range2;
	}

	@Override 
	public RECORD_ID_TYPE getRecordIdType() {
		return recordIdType;
	}

	@Override 
	public int getSplitIndex() {
		return splitIndex;
	}

	@Override 
	public void initReverseTranslation() throws BlockingException {
		if (!initialized) {
			if (splitIndex == 0)
				list1 = new ArrayList(currentIndex);
			else
				list1 = new ArrayList(splitIndex);

			IRecordIdSource source1 = rFactory.getSource(sink1);
			source1.open();

			while (source1.hasNext()) {
				list1.add(source1.next());
			}

			source1.close();

			// Read the second source if there is one
			if (splitIndex > 0) {
				list2 = new ArrayList(currentIndex - splitIndex + 1);
				IRecordIdSource source2 = rFactory.getSource(sink2);
				source2.open();

				while (source2.hasNext()) {
					list2.add(source2.next());
				}

				source2.close();
			}

			initialized = true;
		}
	}

	@Override 
	public void open() throws BlockingException {
		currentIndex = -1;
		sink1.open();
		splitIndex = 0;
	}

	@Override 
	public void recover() throws BlockingException {
		IRecordIdSource source = rFactory.getSource(sink1);
		currentIndex = -1;
		splitIndex = 0;
		if (source.exists()) {
			source.open();
			while (source.hasNext()) {
				currentIndex++;

				Comparable o = (Comparable) source.next();
				setMinMax(o, range1);
			}
			source.close();
			sink1.append();
		}

		source = rFactory.getSource(sink2);
		if (source.exists()) {
			sink1.close();
			splitIndex = currentIndex + 1;
			source.open();
			while (source.hasNext()) {
				currentIndex++;

				Comparable o = (Comparable) source.next();
				setMinMax(o, range2);
			}
			source.close();
			sink2.append();
		}
	}

	@Override 
	public Comparable reverseLookup(int internalID) {
		Comparable o = null;

		if (splitIndex == 0)
			o = (Comparable) list1.get(internalID);
		else {
			if (internalID < splitIndex)
				o = (Comparable) list1.get(internalID);
			else {
				o = (Comparable) list2.get(internalID - splitIndex);
			}
		}
		return o;
	}

	/**
	 * This method compares o to the range. If o is smaller than range[0], then
	 * replace range[0] with o. If o is larger than range[1], then replace
	 * range[1] with o.
	 * 
	 * @param o
	 * @param range
	 */
	private void setMinMax(Comparable o, Comparable[] range) {
		if (range[0] == null)
			range[0] = o;
		else {
			if (o.compareTo(range[0]) < 0)
				range[0] = o;
		}

		if (range[1] == null)
			range[1] = o;
		else {
			if (o.compareTo(range[1]) > 0)
				range[1] = o;
		}
	}

	protected void setRecordIdType(RECORD_ID_TYPE rit) {
		this.recordIdType = rit;
	}

	@Override 
	public void split() throws BlockingException {
		splitIndex = currentIndex + 1;
		sink1.close();
		sink2.open();
	}

	@Override 
	public String toString() {
		return "RecordIdTranslator2 [recordIdType=" + getRecordIdType()
				+ ", currentIndex=" + currentIndex
				+ ", splitIndex=" + splitIndex + "]";
	}

	@Override 
	public int translate(Comparable o) throws BlockingException {
		currentIndex++;

		// figure out the id type for the first file
		if (currentIndex == 0) {
			setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
			sink1.setRecordIDType(getRecordIdType());
		}

		// figure out the id type for the second file
		if (currentIndex == splitIndex) {
			if (getRecordIdType() == null) {
				setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
			} else {
				assert getRecordIdType() == RECORD_ID_TYPE.fromInstance(o);
			}
			assert getRecordIdType() != null;
			sink2.setRecordIDType(getRecordIdType());
		}

		if (splitIndex == 0) {
			sink1.writeRecordID(o);
			setMinMax(o, range1);

		} else {
			sink2.writeRecordID(o);
			setMinMax(o, range2);
		}

		return currentIndex;
	}

}
