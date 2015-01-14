/*
 * Copyright (c) 2011 Rick Hall and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rick Hall - initial API and implementation
 */
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;

/**
 * A cached collection of translated record identifiers. (Persistent
 * translations are maintained by
 * {@link com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaRecordIdController
 * OabaRecordIdController}).
 * 
 * @author rphall
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class RecordIdTranslator3 implements MutableRecordIdTranslator {

	/**
	 * For testing purposes only. Returns a list of map value in the order of
	 * their keys. Checks that the keys are Integers in the range from 0 to some
	 * positive limit with no intervening gaps.
	 * 
	 * @return a non-null List of values enumerated in key-value order. The size
	 *         of the list is the limiting key value described above.
	 * @exception IllegalStateException
	 */
	public static ArrayList sortedMapToList(SortedMap map) {
		ArrayList retVal = null;
		if (map != null) {
			retVal = new ArrayList();
			int expectedKeyValue =
				ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;
			for (Iterator i = map.keySet().iterator(); i.hasNext();) {
				++expectedKeyValue;
				Integer key = (Integer) i.next();
				int keyValue = key.intValue();
				if (keyValue != expectedKeyValue) {
					throw new IllegalStateException("Record index '" + keyValue
							+ "' does not match the expected value '"
							+ expectedKeyValue + "'");
				}
				Object value = map.get(key);
				retVal.add(value);
			}
		}
		return retVal;
	}

	private static final Logger log = Logger
			.getLogger(RecordIdTranslator3.class.getName());

	/** A magic value indicating that the split() method has not been invoked */
	private static final int NOT_SPLIT = 0;

	private IRecordIdSinkSourceFactory rFactory;

	// These two files store the input record id2. The first id correspond to
	// internal id 0, etc.
	private IRecordIdSink sink1;
	private IRecordIdSink sink2;

	// This is an indicator of the class type of record id.
	private RECORD_ID_TYPE recordIdType;

	/**
	 * This contains the mapping from input record id I to internal record id J.
	 * mapping[J] = I. J starts from 0.
	 */
	private int currentIndex =
		ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;

	/**
	 * This is the point at which the second record source record ids start. If
	 * this is NOT_SPLIT, it means there is only 1 record source.
	 */
	private int splitIndex = NOT_SPLIT;

	// indicates whether initReverseTranslation has happened.
	private boolean initialized = false;

	/** Maps staging ids to indices. */
	// IMPLEMENTATION NOTE:
	// Record ids should be unique across staging and master record
	// sources, so just one map should be sufficient for both sources.
	// Splitting the map in two covers a corner case where this contract
	// is broken; i.e. the case where a staging record and a master record
	// actually have the same id; the staging and master sources are distinct;
	// and therefore the records may contain different data.
	private Map ids1_To_Indices;

	/** Maps master ids to indices. */
	// See the implemetation note for ids1_To_Indices
	private Map ids2_To_Indices;

	/**
	 * Maps staging indices to record ids. Staging indices are unique only
	 * within the staging source; similar values may be used as master indices.
	 */
	private SortedMap indices_To_Ids1;

	/**
	 * Maps master indices to record ids. Master indices are unique only within
	 * the master source; similar values may be used as staging indices.
	 */
	private SortedMap indices_To_Ids2;

	public RecordIdTranslator3(IRecordIdSinkSourceFactory rFactory)
			throws BlockingException {
		this.rFactory = rFactory;
		sink1 = rFactory.getNextSink();
		sink2 = rFactory.getNextSink();
	}

	@Override
	public void cleanUp() throws BlockingException {
		this.ids1_To_Indices = null;
		this.ids2_To_Indices = null;
		this.indices_To_Ids1 = null;
		this.indices_To_Ids2 = null;

		sink1.remove();
		if (splitIndex > NOT_SPLIT)
			sink2.remove();
	}

	@Override
	public void close() throws BlockingException {
		if (splitIndex == NOT_SPLIT)
			sink1.close();
		else
			sink2.close();
	}

	@Override
	public ArrayList getList1() {
		// API BUG 2011-01-29 rphall
		// This is a miserable method signature, but it is required for
		// compatibility with the existing OABA classes. It implicitly assumes
		// that record indices run from 0 to some (positive) limit, with no
		// intervening gaps. That's an OK implementation assumption, but it
		// shouldn't be exposed in the API. Instead, there should be a method
		// to write a translator to a (revised) IChunkRecordIdSink, which is the
		// only reason this method is needed.
		ArrayList retVal = sortedMapToList(this.indices_To_Ids1);
		return retVal;
	}

	@Override
	public ArrayList getList2() {
		// See the implementation note for getList1().
		ArrayList retVal = sortedMapToList(this.indices_To_Ids2);
		return retVal;
	}

	public Comparable[] getRange1() {
		throw new RuntimeException("deprecated -- not implemented");
	}

	public Comparable[] getRange2() {
		throw new RuntimeException("deprecated -- not implemented");
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
			this.ids1_To_Indices = new HashMap();
			this.ids2_To_Indices = new HashMap();
			this.indices_To_Ids1 = new TreeMap();

			int count = -1;
			IRecordIdSource source1 = rFactory.getSource(sink1);
			source1.open();
			while (source1.hasNext()) {
				++count;
				Integer index = new Integer(count);
				Comparable id = (Comparable) source1.next();
				Object previous = this.indices_To_Ids1.put(index, id);
				if (previous != null) {
					// Unexpected algorithm error
					throw new Error("duplicate staging index '" + index + "'");
				}
				previous = this.ids1_To_Indices.put(id, index);
				if (previous != null) {
					throw new BlockingException(
							"duplicate staging record id value '" + id + "'");
				}
			}
			currentIndex = count;
			source1.close();

			// Read the second source if there is one
			IRecordIdSource source2 = rFactory.getSource(sink2);
			if (source2.exists()) {
				splitIndex = currentIndex + 1;
				this.indices_To_Ids2 = new TreeMap();
				count = -1;
				source2.open();
				while (source2.hasNext()) {
					++count;
					Integer index = new Integer(count);
					Comparable id = (Comparable) source2.next();
					Object previous = this.indices_To_Ids2.put(index, id);
					if (previous != null) {
						// Unexpected algorithm error
						throw new Error("duplicate master index '" + index
								+ "'");
					}
					previous = this.ids2_To_Indices.put(id, index);
					if (previous != null) {
						throw new BlockingException(
								"duplicate master record id value '" + id + "'");
					}
				}
				source2.close();
				currentIndex = splitIndex + count;
			}

			initialized = true;
		}
	}

	@Override
	public int lookupMasterIndex(Comparable recordID) {
		int retVal = INVALID_INDEX;
		if (recordID != null) {
			Integer i = (Integer) this.ids2_To_Indices.get(recordID);
			if (i != null) {
				retVal = i.intValue();
			}
		}
		return retVal;
	}

	@Override
	public int lookupStagingIndex(Comparable recordID) {
		int retVal = INVALID_INDEX;
		if (recordID != null) {
			Integer i = (Integer) this.ids1_To_Indices.get(recordID);
			if (i != null) {
				retVal = i.intValue();
			}
		}
		return retVal;
	}

	@Override
	public void open() throws BlockingException {
		currentIndex = ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;
		sink1.open();
		splitIndex = NOT_SPLIT;
	}

	@Override
	public void recover() throws BlockingException {
		IRecordIdSource source = rFactory.getSource(sink1);
		currentIndex = ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;
		splitIndex = NOT_SPLIT;
		if (source.exists()) {
			source.open();
			while (source.hasNext()) {
				source.next();
				currentIndex++;
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
				source.next();
				currentIndex++;
			}
			source.close();
			sink2.append();
		}
	}

	@Override
	public Comparable reverseLookup(int internalID) {
		if (internalID < 0) {
			log.warning("invalid internal index '" + internalID + "'");
		}
		Comparable retVal;
		if (splitIndex == NOT_SPLIT) {
			Integer key = new Integer(internalID);
			retVal = (Comparable) this.indices_To_Ids1.get(key);
		} else {
			if (internalID < splitIndex) {
				Integer key = new Integer(internalID);
				retVal = (Comparable) this.indices_To_Ids1.get(key);
			} else {
				Integer key = new Integer(internalID - splitIndex);
				retVal = (Comparable) this.indices_To_Ids2.get(key);
			}
		}
		if (retVal == null) {
			log.warning("translating internal index '" + internalID
					+ "' to a null record id");
		}
		return retVal;
	}

	protected void setRecordIdType(RECORD_ID_TYPE dataType) {
		this.recordIdType = dataType;
	}

	@Override
	public void split() throws BlockingException {
		if (splitIndex == NOT_SPLIT) {
			splitIndex = currentIndex + 1;
			sink1.close();
			sink2.open();
		} else {
			log.warning("Split method invoked on a previously split translator");
		}
	}

	@Override
	public int translate(Comparable o) throws BlockingException {
		int retVal;
		if (o == null) {
			retVal = ImmutableRecordIdTranslator.INVALID_INDEX;
			log.warning("translating null record id to an invalid internal index ("
					+ retVal + ")");
		} else {
			currentIndex++;
			retVal = currentIndex;

			// figure out the id type for the first file
			if (currentIndex == 0) {
				setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
				sink1.setRecordIDType(getRecordIdType());
			}

			// figure out the id type for the second file
			if (splitIndex != NOT_SPLIT && currentIndex == splitIndex) {
				if (getRecordIdType() == null) {
					setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
				} else {
					assert getRecordIdType() == RECORD_ID_TYPE.fromInstance(o);
				}
				assert getRecordIdType() != null;
				sink2.setRecordIDType(getRecordIdType());
			}

			if (splitIndex == NOT_SPLIT) {
				sink1.writeRecordID(o);
			} else {
				sink2.writeRecordID(o);
			}
		}
		return retVal;
	}

}
