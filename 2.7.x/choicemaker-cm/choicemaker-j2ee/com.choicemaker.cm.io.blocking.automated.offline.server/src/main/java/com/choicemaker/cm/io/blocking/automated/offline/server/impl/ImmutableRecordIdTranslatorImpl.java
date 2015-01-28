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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.impl.BatchJobEntity;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ImmutableRecordIdTranslatorLocal;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdTranslation;

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
class ImmutableRecordIdTranslatorImpl implements
		ImmutableRecordIdTranslatorLocal {

	private static final Logger log = Logger
			.getLogger(ImmutableRecordIdTranslatorImpl.class.getName());

	/** An initialization helper class */
	protected static class INITIALIZATION_RETURN_VALUE {
		public final RECORD_ID_TYPE recordIdType;
		public final int splitIndex;

		public INITIALIZATION_RETURN_VALUE(RECORD_ID_TYPE rit, int idx) {
			this.recordIdType = rit;
			this.splitIndex = idx;
		}
	}

	/**
	 * For testing purposes only. Returns a list of map values in the order of
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
					String msg =
						"Record index '" + keyValue
								+ "' does not match the expected value '"
								+ expectedKeyValue + "'";
					log.severe(msg);
					throw new IllegalStateException(msg);
				}
				Object value = map.get(key);
				retVal.add(value);
			}
		}
		return retVal;
	}

	/**
	 * If <code>expectedRecordIdType</code> is null, it will be determined from
	 * the specified translations.
	 */
	static <T extends Comparable<T>> ImmutableRecordIdTranslatorImpl createTranslator(
			final BatchJob job, final RECORD_ID_TYPE expectedRecordIdType,
			List<AbstractRecordIdTranslationEntity<T>> translations)
			throws BlockingException {

		if (job == null || !BatchJobEntity.isPersistent(job)) {
			String msg = "invalid job: " + job;
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (translations.isEmpty()) {
			String msg = "No record-id translations for job " + job.getId();
			log.severe(msg);
			throw new BlockingException(msg);
		}

		final Map<T, Integer> ids1_To_Indices = new HashMap<>();
		final Map<T, Integer> ids2_To_Indices = new HashMap<>();
		final SortedMap<Integer, T> indices_To_Ids1 = new TreeMap<>();
		final SortedMap<Integer, T> indices_To_Ids2 = new TreeMap<>();

		RECORD_ID_TYPE recordIdType = expectedRecordIdType;
		if (recordIdType != null) {
			log.info("record-id type: " + recordIdType);
		}
		int count1 = 0;
		int count2 = 0;
		int splitIndex = ImmutableRecordIdTranslatorImpl.NOT_SPLIT;
		for (RecordIdTranslation<T> rt : translations) {

			// Check or set the record-id type
			RECORD_ID_TYPE rid = rt.getRecordType();
			if (recordIdType == null) {
				assert rid != null;
				recordIdType = rid;
				log.info("record-id type: " + recordIdType);
			} else {
				if (recordIdType != rid) {
					String msg =
						"inconsistent record-id types: " + recordIdType
								+ " and " + rid + " (count1 == " + count1
								+ ", count2 == " + count2
								+ ", translation id == " + rt.getId() + ")";
					log.severe(msg);
					throw new BlockingException(msg);
				}
			}

			// Get the translated index and the recordId
			int index = rt.getTranslatedId();
			T recordId = rt.getRecordId();
			if (count1 == 0 && count2 == 0) {
				// Check the generic type one time
				Class<?> c = recordId.getClass();
				String csn = c.getSimpleName();
				log.info("record-id class: " + csn);
				if (recordIdType != RECORD_ID_TYPE.fromClass(c)) {
					assert recordId != null;
					String msg = "Inconsistent record id class: " + csn;
					log.severe(msg);
					throw new BlockingException(msg);
				}
			}

			// Figure out where to put the recordId and index
			RECORD_SOURCE_ROLE rsr = rt.getRecordSource();
			boolean isFirstRecord = rsr.isFirstSource();
			boolean isSplitIndex = rsr.isSplitIndex();
			if (isFirstRecord) {
				++count1;
				ids1_To_Indices.put(recordId, index);
				indices_To_Ids1.put(index, recordId);
			} else if (!isSplitIndex) {
				++count2;
				ids2_To_Indices.put(recordId, index);
				indices_To_Ids2.put(index, recordId);
			} else {
				final int si = rt.getTranslatedId();
				if (si != RecordIdTranslation.INVALID_TRANSLATED_ID
						&& splitIndex == RecordIdTranslation.INVALID_TRANSLATED_ID) {
					splitIndex = rt.getTranslatedId();
				} else if (si != RecordIdTranslation.INVALID_TRANSLATED_ID) {
					String msg =
						"Multiple entries for split index: " + splitIndex
								+ ", " + si;
					log.severe(msg);
					throw new BlockingException(msg);
				} else {
					String msg = "Invalid split index: " + si;
					log.severe(msg);
					throw new BlockingException(msg);
				}
			}
		}
		log.info("source1 translations: " + count1);
		log.info("source2 translations: " + count2);
		log.info("split index: " + splitIndex);
		if (count1 == 0 && count2 == 0) {
			String msg =
				"UNEXPECTED (should be caught by preconditions): "
						+ "no record-id translations for job " + job.getId();
			log.severe(msg);
			throw new Error(msg);
		}
		assert recordIdType != null;
		assert expectedRecordIdType == null
				|| expectedRecordIdType == recordIdType;

		ImmutableRecordIdTranslatorImpl irit =
			new ImmutableRecordIdTranslatorImpl(job, recordIdType,
					ids1_To_Indices, ids2_To_Indices, indices_To_Ids1,
					indices_To_Ids2, splitIndex);
		return irit;
	}

	// Trick to check assertion status
	protected boolean onAssert = false;
	{
		// Initialized on construction
		assert onAssert = true;
	}

	private final BatchJob batchJob;

	/** The type of record ids handled by this translator */
	private final RECORD_ID_TYPE recordIdType;

	/**
	 * This is the internal index at which the indices for records from the
	 * second record source start. If this value is NOT_SPLIT, it means there is
	 * only 1 record source.
	 */
	private final int splitIndex;

	/** Maps staging ids to indices. */
	// IMPLEMENTATION NOTE:
	// Record ids should be unique across staging and master record
	// sources, so just one map should be sufficient for both sources.
	// Splitting the map in two covers a corner case where this contract
	// is broken; i.e. the case where a staging record and a master record
	// actually have the same id; the staging and master sources are distinct;
	// and therefore the records may contain different data.
	final Map ids1_To_Indices = new HashMap();

	/** Maps master ids to indices. */
	// See the implementation note for ids1_To_Indices
	final Map ids2_To_Indices = new HashMap();

	/** A map of indices for staging records to the ids of those records */
	private final SortedMap indices_To_Ids1 = new TreeMap();

	/** A map of indices for staging records to the ids of those records */
	private final SortedMap indices_To_Ids2 = new TreeMap();

	// public ImmutableRecordIdTranslatorImpl(BatchJob job)
	// throws BlockingException {
	// if (job == null) {
	// throw new IllegalArgumentException("null batch job");
	// }
	// this.batchJob = job;
	// RecordIdSinkSourceFactory rFactory = getTransIDFactory(job);
	// final IRecordIdSink sink1 = rFactory.getNextSink();
	// source1 = rFactory.getSource(sink1);
	// log.info("Source 1: " + source1);
	// final IRecordIdSink sink2 = rFactory.getNextSink();
	// source2 = rFactory.getSource(sink2);
	// log.info("Source 2: " + source2);
	// initialize();
	// }

	public ImmutableRecordIdTranslatorImpl(final BatchJob job,
			final RECORD_ID_TYPE recordIdType,
			final Map<?, Integer> ids1_To_Indices,
			final Map<?, Integer> ids2_To_Indices,
			final SortedMap<Integer, ?> indices_To_Ids1,
			final SortedMap<Integer, ?> indices_To_Ids2, int splitIndex)
			throws BlockingException {
		if (job == null) {
			String msg = "null batch job";
			throw new IllegalArgumentException(msg);
		}
		if (recordIdType == null) {
			String msg = "null record-id type";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (ids1_To_Indices == null || ids2_To_Indices == null
				|| indices_To_Ids1 == null || indices_To_Ids2 == null) {
			String msg = "null map";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.batchJob = job;
		this.recordIdType = recordIdType;
		this.splitIndex = splitIndex;
		this.ids1_To_Indices.putAll(ids1_To_Indices);
		this.indices_To_Ids1.putAll(indices_To_Ids1);
		this.ids2_To_Indices.putAll(ids2_To_Indices);
		this.indices_To_Ids2.putAll(indices_To_Ids2);
	}

	public ImmutableRecordIdTranslatorImpl(BatchJob job, IRecordIdSource s1,
			IRecordIdSource s2) throws BlockingException {
		if (job == null) {
			String msg = "null batch job";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (s1 == null || s2 == null) {
			String msg = "null argument";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.batchJob = job;
		INITIALIZATION_RETURN_VALUE irv = initializeFromSources(s1, s2);
		this.splitIndex = irv.splitIndex;
		this.recordIdType = irv.recordIdType;
	}

	@Override
	public BatchJob getBatchJob() {
		return batchJob;
	}

	@Override
	public RECORD_ID_TYPE getRecordIdType() {
		if (recordIdType == null) {
			if (ids1_To_Indices.isEmpty() && this.indices_To_Ids1.isEmpty()
					&& this.ids2_To_Indices.isEmpty()
					&& this.indices_To_Ids2.isEmpty()) {
				log.warning("Record-id translator has no data");
			} else {
				String msg = "null record-id type";
				log.severe(msg);
				throw new IllegalStateException(msg);
			}

		}
		return recordIdType;
	}

	@Override
	public int getSplitIndex() {
		return splitIndex;
	}

	@Override
	public boolean isSplit() {
		return splitIndex != NOT_SPLIT;
	}

	public boolean isEmpty() {
		boolean retVal = ids1_To_Indices.isEmpty() && ids2_To_Indices.isEmpty();
		if (retVal) {
			assert indices_To_Ids1.isEmpty() && indices_To_Ids2.isEmpty();
		} else {
			assert !indices_To_Ids1.isEmpty() || !indices_To_Ids2.isEmpty();
		}
		return retVal;
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

	/**
	 * Called during construction
	 * 
	 * @return the split index
	 */
	protected INITIALIZATION_RETURN_VALUE initializeFromSources(
			IRecordIdSource source1, IRecordIdSource source2)
			throws BlockingException {
		assert source1 != null;
		assert source2 != null;

		assert this.ids1_To_Indices != null;
		assert this.indices_To_Ids1 != null;
		assert this.ids2_To_Indices != null;
		assert this.indices_To_Ids2 != null;

		// The split index that will be computed and returned
		int retIDX = NOT_SPLIT;
		RECORD_ID_TYPE retRIT = null;

		// Read the first source
		int count = -1;
		if (source1.exists()) {
			source1.open();
			while (source1.hasNext()) {
				++count;
				Integer index = new Integer(count);
				Comparable id = (Comparable) source1.next();
				retRIT = setRecordIdType(onAssert, retRIT, id);
				Object previous = this.indices_To_Ids1.put(index, id);
				if (previous != null) {
					// Unexpected algorithm error
					String msg = "duplicate staging index '" + index + "'";
					log.severe(msg);
					throw new Error(msg);
				}
				previous = this.ids1_To_Indices.put(id, index);
				if (previous != null) {
					String msg =
						"duplicate staging record id value '" + id + "'";
					log.severe(msg);
					throw new BlockingException(msg);
				}
			}
			source1.close();
			source1.delete();
		}
		log.info("Number of ids from first source: " + (count + 1));

		// Counts the records in source2, does NOT index source2.
		// NOTE the split index is set by count, not count2
		int count2 = 0; 

		// Read the second source if there is one
		if (source2.exists()) {

			// Set the split index
			retIDX = count + 1;

			count = -1;
			source2.open();
			while (source2.hasNext()) {
				++count2;
				++count;
				Integer index = new Integer(count);
				Comparable id = (Comparable) source2.next();
				Object previous = this.indices_To_Ids2.put(index, id);
				if (previous != null) {
					// Unexpected algorithm error
					String msg = "duplicate master index '" + index + "'";
					log.severe(msg);
					throw new Error(msg);
				}
				previous = this.ids2_To_Indices.put(id, index);
				if (previous != null) {
					String msg =
						"duplicate master record id value '" + id + "'";
					log.severe(msg);
					throw new BlockingException(msg);
				}
			}
			source2.close();
			source2.delete();
		}
		log.info("Number of ids from second source: " + (count2));
		log.info("Split index: " + retIDX);
		log.info("Record-id type: " + retRIT);
		INITIALIZATION_RETURN_VALUE retVal =
			new INITIALIZATION_RETURN_VALUE(retRIT, retIDX);
		return retVal;
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
			String msg = "invalid internal index '" + internalID + "'";
			log.severe(msg);
			throw new IllegalStateException(msg);
		}
		Comparable retVal;
		if (getSplitIndex() == NOT_SPLIT) {
			Integer key = new Integer(internalID);
			retVal = (Comparable) this.indices_To_Ids1.get(key);
			if (retVal == null) {
				log.warning("translating internal, staging index (no split) '"
						+ internalID + "' to a null record id");
			}
		} else {
			if (internalID < getSplitIndex()) {
				Integer key = new Integer(internalID);
				retVal = (Comparable) this.indices_To_Ids1.get(key);
				if (retVal == null) {
					log.warning("translating internal, staging index (before split) '"
							+ internalID + "' to a null record id");
				}
			} else {
				Integer key = new Integer(internalID - getSplitIndex());
				retVal = (Comparable) this.indices_To_Ids2.get(key);
				if (retVal == null) {
					log.warning("translating internal, master index (after split) '"
							+ internalID + "' to a null record id");
				}
			}
		}
		return retVal;
	}

	protected static RECORD_ID_TYPE setRecordIdType(boolean onAssert,
			RECORD_ID_TYPE recordIdType, Comparable id) {
		if (id == null) {
			String msg = "null id";
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
		if (recordIdType == null) {
			recordIdType = RECORD_ID_TYPE.fromInstance(id);
			assert recordIdType != null;

		} else if (onAssert) {
			Class<?> c = id.getClass();
			RECORD_ID_TYPE rit2 = RECORD_ID_TYPE.fromClass(c);
			if (recordIdType != rit2) {
				String msg =
					"Inconsistent record id types: " + recordIdType + " and "
							+ c.getSimpleName();
				log.severe(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		return recordIdType;
	}

	void assertEqual(String failureMsg, Object i1, Object i2) {
		if ((i1 == null && i2 != null) || !i1.equals(i2)) {
			String msg = failureMsg + ": " + i1 + ", " + i2;
			log.severe(msg);
			throw new AssertionError(msg);
		}
	}

	private static void assertEqual(String failureMsg, int i1, int i2) {
		if (i1 != i2) {
			String msg = failureMsg + ": " + i1 + ", " + i2;
			log.severe(msg);
			throw new AssertionError(msg);
		}
	}

	private static void assertContains(String failureMsg, Collection<?> s,
			Object o) {
		if (!s.contains(o)) {
			String msg = failureMsg + ": " + o;
			log.severe(msg);
			throw new AssertionError(msg);
		}
	}

	private static void assertContains(String failureMsg, Collection<?> trans,
			Collection<?> impl) {
		if (!trans.containsAll(impl)) {
			int count1 = trans.size();
			int count2 = impl.size();
			int diff = Math.abs(count1 - count2);
			String msg;
			if (count1 > count2) {
				msg = failureMsg + ": more translations are persisted: " + diff;
				log.severe(msg);
				throw new AssertionError(msg);
			} else if (count2 > count1) {
				msg =
					failureMsg + ": fewer translations are persisted: " + diff;
				log.severe(msg);
				throw new AssertionError(msg);
			} else {
				msg = failureMsg + ": different translations are persisted";
				log.severe(msg);
				throw new AssertionError(msg);
			}
		}
	}

	<T extends Comparable<T>> void assertPersistent(
			List<AbstractRecordIdTranslationEntity<T>> translations) {

		// Trick to check whether assertions are enabled
		// *assigns* true to assertOn if assertions are on.
		boolean assertOn = false;
		assert assertOn = true;
		if (assertOn) {
			// Check that each translation is mapped
			Set<T> rids = new HashSet<>();
			Set<Integer> indices = new HashSet<>();
			for (AbstractRecordIdTranslationEntity<T> translation : translations) {
				RECORD_ID_TYPE rit = translation.getRecordType();
				assertEqual("inconsistent record-id type: ", rit,
						this.recordIdType);

				T rid = translation.getRecordId();
				rids.add(rid);
				int index = translation.getTranslatedId();
				indices.add(index);

				RECORD_SOURCE_ROLE rsr = translation.getRecordSource();
				if (rsr.isSplitIndex()) {
					assertEqual("inconsistent split index", index,
							this.getSplitIndex());

				} else if (rsr.isFirstSource()) {
					Set idxKeys = this.indices_To_Ids1.keySet();
					Collection idxValues = this.ids1_To_Indices.values();
					Set ridKeys = this.ids1_To_Indices.keySet();
					Collection ridValues = this.indices_To_Ids1.values();

					assertContains("extra translation index", idxKeys, index);
					assertContains("extra translation index", idxValues, index);
					assertContains("extra translation record id", ridKeys, rid);
					assertContains("extra translation record id", ridValues,
							rid);

				} else if (!rsr.isFirstSource()) {
					Set idxKeys = this.indices_To_Ids1.keySet();
					Collection idxValues = this.ids1_To_Indices.values();
					Set ridKeys = this.ids1_To_Indices.keySet();
					Collection ridValues = this.indices_To_Ids1.values();

					assertContains("extra translation index", idxKeys, index);
					assertContains("extra translation index", idxValues, index);
					assertContains("extra translation record id", ridKeys, rid);
					assertContains("extra translation record id", ridValues,
							rid);
				}
			}

			// Check that each key and value of each map is persisted
			assertContains("non-persistent indices 1", indices,
					indices_To_Ids1.keySet());
			assertContains("non-persistent indices 2", indices,
					indices_To_Ids2.keySet());
			assertContains("non-persistent record ids 1", rids,
					indices_To_Ids1.values());
			assertContains("non-persistent record ids 2", rids,
					indices_To_Ids2.values());

			assertContains("non-persistent record ids 1", rids,
					indices_To_Ids1.values());
			assertContains("non-persistent record ids 2", rids,
					indices_To_Ids2.values());
			assertContains("non-persistent indices 1", indices,
					indices_To_Ids1.keySet());
			assertContains("non-persistent indices 2", indices,
					indices_To_Ids2.keySet());
		}
	}

	@Override
	public String toString() {
		return "ImmutableRecordIdTranslatorImpl [recordIdType=" + recordIdType
				+ ", splitIndex=" + getSplitIndex() + ", source1 size="
				+ ids1_To_Indices.size() + ", source2 size="
				+ ids2_To_Indices.size() + "]";
	}

}
