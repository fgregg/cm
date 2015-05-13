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

import static com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator.INVALID_INDEX;
import static com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.MutableRecordIdTranslatorLocal;

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
class MutableRecordIdTranslatorImpl implements MutableRecordIdTranslatorLocal {

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
			int expectedKeyValue = MINIMUM_VALID_INDEX - 1;
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
			.getLogger(MutableRecordIdTranslatorImpl.class.getName());

	/** A magic value indicating that the split() method has not been invoked */
	private static final int NOT_SPLIT = 0;

	/**
	 * An enumeration of states for a translator: mutable, immutable and
	 * inconsistent.
	 * <ul>
	 * <li><em><strong>MUTABLE</strong></em><br/>
	 * A translator is constructed in the mutable state. While it is mutable, it
	 * may be used to translate record ids to internal indices; this type of
	 * translation is called a <em>forward</em> translation.</li>
	 * <li><em><strong>IMMUTABLE</strong></em><br/>
	 * After all forward translations are completed, a translator may be used
	 * for <em>reverse lookups</em> of internal indices from record ids. Once
	 * the first reverse lookup is invoked, a translator switches to an
	 * immutable state. In the immutable state, it can only be used for reverse
	 * lookups, not for forward translations. A translator also switches to the
	 * immutable state when it is
	 * {@link #save(BatchJob, MutableRecordIdTranslator) saved} to a database</li>
	 * <li><em><strong>INCONSISTENT</strong></em><br/>
	 * If a forward translation is attempted after the initialization of reverse
	 * lookups, the translator state is marked as inconsistent and an
	 * IllegalStateException is thrown.</li>
	 * </ul>
	 */
	public static enum TRANSLATOR_STATE {
		MUTABLE, IMMUTABLE
	}

	/**
	 * A non-null record-id sink can be UNKNOWN, OPEN or CLOSED. (The UNKNOWN
	 * state is the state when a sink is obtained from a factory, since there's
	 * no way to check the state directly.) These states are a one-way rachet,
	 * from UNKNOWN to OPEN to CLOSED, with no other transition possible.
	 */
	protected static enum SINK_STATE {
		UNKNOWN, OPEN, CLOSED
	}

	private TRANSLATOR_STATE translatorState = TRANSLATOR_STATE.MUTABLE;

	private final BatchJob batchJob;

	private final IRecordIdSinkSourceFactory rFactory;

	/** Typically a source of staging records */
	private final IRecordIdSink sink1;

	/** Typically a source of master records */
	private final IRecordIdSink sink2;

	/** The state of sink1 */
	private SINK_STATE sink1State;

	/** The state of sink2 */
	private SINK_STATE sink2State;

	/** Number of ids written to sink1 */
	private int count1;

	/** Number of ids written to sink2 */
	private int count2;

	/** The type of record ids handled by this translator */
	private RECORD_ID_TYPE recordIdType;

	/** The next available internal index to which a record id may be mapped */
	private int currentIndex = MINIMUM_VALID_INDEX - 1;

	/**
	 * This is the internal index at which the indices for records from the
	 * second record source start. If this value is NOT_SPLIT, it means there is
	 * only 1 record source.
	 */
	private int splitIndex = NOT_SPLIT;

	private final Map<Comparable, Integer> seen = new HashMap<>();

	MutableRecordIdTranslatorImpl(BatchJob job,
			IRecordIdSinkSourceFactory factory, IRecordIdSink s1,
			IRecordIdSink s2) throws BlockingException {
		if (job == null || factory == null || s1 == null || s2 == null) {
			throw new IllegalArgumentException("null argument");
		}
		if (s1.exists()) {
			String msg = "translator cache already exists: " + s1;
			throw new IllegalArgumentException(msg);
		}
		if (s2.exists()) {
			String msg = "translator cache already exists: " + s2;
			throw new IllegalArgumentException(msg);
		}
		this.batchJob = job;
		this.rFactory = factory;
		this.sink1 = s1;
		log.info("Sink 1: " + sink1);
		this.sink1State = SINK_STATE.UNKNOWN;
		this.sink2 = s2;
		log.info("Sink 2: " + sink2);
		this.sink2State = SINK_STATE.UNKNOWN;
	}

	@Override
	public void cleanUp() throws BlockingException {
		if (getSink1().exists()) {
			getSink1().flush();
			getSink1().close();
			sink1State = SINK_STATE.CLOSED;
			getSink1().remove();
			sink1State = null;
		}
		if (getSink2().exists()) {
			getSink2().flush();
			getSink2().close();
			sink2State = SINK_STATE.CLOSED;
			getSink2().remove();
			sink2State = null;
		}
	}

	@Override
	public void close() throws BlockingException {
		log.fine("close(): translatorState == " + translatorState);
		seen.clear();
		if (!isClosed()) {
			// Check first sink
			if (getSink1().exists() && sink1State == SINK_STATE.OPEN) {
				log.info("Writing ids to sink1: " + count1);
				getSink1().flush();
				log.info("Closing sink1: " + getSink1());
				getSink1().close();
				sink1State = SINK_STATE.CLOSED;

			} else {
				log.fine("Sink1 already closed: " + getSink1());
			}

			// Check second sink
			if (getSink2().exists() && sink2State == SINK_STATE.OPEN) {
				log.info("Writing ids to sink2: " + count2);
				getSink2().flush();
				log.info("Closing sink2: " + getSink2());
				getSink2().close();
				sink2State = SINK_STATE.CLOSED;

			} else {
				log.info("Sink2 does not exist: " + getSink2());
			}

			this.translatorState = TRANSLATOR_STATE.IMMUTABLE;
		} else {
			log.warning("translator is already closed");
		}

		String msg =
			"close(): " + isClosed() + ", translatorState == "
					+ translatorState;
		if (!isClosed()) {
			log.severe(msg);
		} else {
			log.info(msg);
		}
		assert isClosed();
	}

	@Override
	public void open() throws BlockingException {
		if (isClosed()) {
			throw new IllegalStateException("invalid translator state: "
					+ translatorState);
		}
		currentIndex = MINIMUM_VALID_INDEX - 1;
		log.info("Opening sink1: " + getSink1());
		getSink1().open();
		sink1State = SINK_STATE.OPEN;
		_setSplitIndex(NOT_SPLIT);
		seen.clear();
	}

	@Override
	public void split() throws BlockingException {
		if (getSplitIndex() == NOT_SPLIT) {
			_setSplitIndex(currentIndex + 1);
			seen.clear();
			log.info("Writing ids to sink1: " + count1);
			getSink1().flush();
			log.info("Closing sink1: " + getSink1());
			getSink1().close();
			sink1State = SINK_STATE.CLOSED;
			log.info("Opening sink2: " + getSink2());
			getSink2().open();
			sink2State = SINK_STATE.OPEN;
		} else {
			log.warning("Split method invoked on a previously split translator");
		}
	}

	@Override
	public int translate(Comparable o) throws BlockingException {
		if (isClosed()) {
			throw new IllegalStateException("translator is no longer mutable");
		}
		int retVal;
		if (o == null) {
			retVal = INVALID_INDEX;
			log.warning("translating null record id to an invalid internal index ("
					+ retVal + ")");

		} else {
			Integer i = seen.get(o);
			if (i != null) {
				retVal = i.intValue();

			} else {
				currentIndex++;
				retVal = currentIndex;
				seen.put(o, retVal);

				// figure out the id type for the sinks
				if (currentIndex == 0) {
					_setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
					getSink1().setRecordIDType(getRecordIdType());
					getSink2().setRecordIDType(getRecordIdType());
				}
				assert getRecordIdType() == RECORD_ID_TYPE.fromInstance(o);
				assert getRecordIdType() == getSink1().getRecordIdType();
				assert getRecordIdType() == getSink2().getRecordIdType();

				if (getSplitIndex() == NOT_SPLIT) {
					getSink1().writeRecordID(o);
					++count1;
				} else {
					getSink2().writeRecordID(o);
					++count2;
				}

			}
		}
		return retVal;
	}

	public boolean isClosed() {
		log.finer("isClosed(): translatorState: " + translatorState);
		boolean retVal;
		if (this.translatorState == TRANSLATOR_STATE.MUTABLE) {
			retVal = false;
		} else {
			assert this.translatorState == TRANSLATOR_STATE.IMMUTABLE;
			retVal = true;
		}
		log.finer("isClosed() == " + retVal);
		return retVal;
	}

	@Override
	public BatchJob getBatchJob() {
		return batchJob;
	}

	IRecordIdSinkSourceFactory getFactory() {
		return rFactory;
	}

	IRecordIdSink getSink1() {
		return sink1;
	}

	IRecordIdSink getSink2() {
		return sink2;
	}

	@Override
	public RECORD_ID_TYPE getRecordIdType() {
		return recordIdType;
	}

	protected void _setRecordIdType(RECORD_ID_TYPE dataType) {
		this.recordIdType = dataType;
	}

	@Override
	public int getSplitIndex() {
		return splitIndex;
	}

	@Override
	public boolean isSplit() {
		return splitIndex != NOT_SPLIT;
	}

	protected void _setSplitIndex(int splitIndex) {
		this.splitIndex = splitIndex;
	}

	@Override
	public String toString() {
		return "MutableRecordIdTranslatorImpl [recordIdType=" + recordIdType
				+ ", translatorState=" + translatorState + ", currentIndex="
				+ currentIndex + ", splitIndex=" + splitIndex + ", sink1="
				+ getSink1() + ", sink1State=" + sink1State + ", count1="
				+ count1 + ", sink2=" + getSink2() + ", sink2State="
				+ sink2State + ", count2=" + count2 + "]";
	}

	public boolean doTranslatorCachesExist() {
		return this.getSink1().exists() || this.getSink1().exists();
	}

}
