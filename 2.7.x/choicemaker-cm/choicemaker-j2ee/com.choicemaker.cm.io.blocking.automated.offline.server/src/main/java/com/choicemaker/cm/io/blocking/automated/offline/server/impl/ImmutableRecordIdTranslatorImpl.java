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

import static com.choicemaker.cm.io.blocking.automated.offline.server.impl.RecordIdControllerBean.BASENAME_RECORDID_TRANSLATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
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
public class ImmutableRecordIdTranslatorImpl implements
		ImmutableRecordIdTranslator {

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

	/**
	 * This gets the factory that is used to get translator id sink and source.
	 */
	private static RecordIdSinkSourceFactory getTransIDFactory(BatchJob job) {
		String wd = OabaFileUtils.getWorkingDir(job);
		return new RecordIdSinkSourceFactory(wd, BASENAME_RECORDID_TRANSLATOR,
				OabaFileUtils.BINARY_SUFFIX);
	}

	private static final Logger log = Logger
			.getLogger(ImmutableRecordIdTranslatorImpl.class.getName());

	/** A magic value indicating that the split() method has not been invoked */
	private static final int NOT_SPLIT = 0;

	private final IRecordIdSource source1;
	private final IRecordIdSource source2;

	/** The type of record ids handled by this translator */
	private RECORD_ID_TYPE recordIdType;

	/**
	 * This is the internal index at which the indices for records from the
	 * second record source start. If this value is NOT_SPLIT, it means there is
	 * only 1 record source.
	 */
	private int splitIndex = NOT_SPLIT;

	/** Maps staging ids to indices. */
	// IMPLEMENTATION NOTE:
	// Record ids should be unique across staging and master record
	// sources, so just one map should be sufficient for both sources.
	// Splitting the map in two covers a corner case where this contract
	// is broken; i.e. the case where a staging record and a master record
	// actually have the same id; the staging and master sources are distinct;
	// and therefore the records may contain different data.
	private final Map ids1_To_Indices = new HashMap();

	/** Maps master ids to indices. */
	// See the implementation note for ids1_To_Indices
	private final Map ids2_To_Indices = new HashMap();

	/** A map of indices for staging records to the ids of those records */
	private final SortedMap indices_To_Ids1 = new TreeMap();

	/** A map of indices for staging records to the ids of those records */
	private final SortedMap indices_To_Ids2 = new TreeMap();

	public ImmutableRecordIdTranslatorImpl(BatchJob job)
			throws BlockingException {
		if (job == null) {
			throw new IllegalArgumentException("null batch job");
		}
		RecordIdSinkSourceFactory rFactory = getTransIDFactory(job);
		final IRecordIdSink sink1 = rFactory.getNextSink();
		source1 = rFactory.getSource(sink1);
		log.info("Source 1: " + source1);
		final IRecordIdSink sink2 = rFactory.getNextSink();
		source2 = rFactory.getSource(sink2);
		log.info("Source 2: " + source2);
		initialize();
	}

	public ImmutableRecordIdTranslatorImpl(IRecordIdSource s1,
			IRecordIdSource s2) throws BlockingException {
		if (s1 == null || s2 == null) {
			throw new IllegalArgumentException("null argument");
		}
		source1 = s1;
		log.info("Source 1: " + source1);
		source2 = s2;
		log.info("Source 2: " + source2);
		initialize();
	}

	@Override
	public void cleanUp() throws BlockingException {
		if (source1.exists()) {
			source1.delete();
		}
		if (source2.exists()) {
			source2.delete();
		}
	}

	@Override
	public RECORD_ID_TYPE getRecordIdType() {
		if (recordIdType == null) {
			if (ids1_To_Indices.isEmpty() && this.indices_To_Ids1.isEmpty()
					&& this.ids2_To_Indices.isEmpty() && this.indices_To_Ids2
						.isEmpty()) {
				log.warning("Record-id translator has no data");
			} else {
				throw new IllegalStateException("null record-id type");
			}

		}
		return recordIdType;
	}

	@Override
	public int getSplitIndex() {
		return splitIndex;
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

	/** Called during construction */
	protected void initialize() throws BlockingException {
		assert this.ids1_To_Indices != null;
		assert this.indices_To_Ids1 != null;
		assert source1 != null;

		assert this.ids2_To_Indices != null;
		assert this.indices_To_Ids2 != null;
		assert source2 != null;

		splitIndex = NOT_SPLIT;
		int count = -1;
		if (source1.exists()) {
			source1.open();
			while (source1.hasNext()) {
				++count;
				Integer index = new Integer(count);
				Comparable id = (Comparable) source1.next();
				setRecordIdType(id);
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
			source1.close();
		}
		log.info("Number of ids from first source: " + count);

		// Read the second source if there is one
		if (source2.exists()) {
			splitIndex = count + 1;
			count = -1;
			source2.open();
			while (source2.hasNext()) {
				++count;
				Integer index = new Integer(count);
				Comparable id = (Comparable) source2.next();
				Object previous = this.indices_To_Ids2.put(index, id);
				if (previous != null) {
					// Unexpected algorithm error
					throw new Error("duplicate master index '" + index + "'");
				}
				previous = this.ids2_To_Indices.put(id, index);
				if (previous != null) {
					throw new BlockingException(
							"duplicate master record id value '" + id + "'");
				}
			}
			source2.close();
		}
		log.info("Number of ids from second source: " + count);

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
	public Comparable reverseLookup(int internalID) {
		if (internalID < 0) {
			throw new IllegalStateException("invalid internal index '"
					+ internalID + "'");
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

	protected void setRecordIdType(Comparable id) {
		assert id != null;
		if (recordIdType == null) {
			this.recordIdType = RECORD_ID_TYPE.fromInstance(id);
			assert this.recordIdType != null;
		} else {
			assert this.recordIdType == RECORD_ID_TYPE.fromInstance(id);
		}
	}

	@Override
	public String toString() {
		return "ImmutableRecordIdTranslatorImpl [recordIdType=" + recordIdType
				+ ", splitIndex=" + splitIndex + ", source1=" + source1
				+ ", source2=" + source2 + "]";
	}

}
