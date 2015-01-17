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
import java.util.Iterator;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.choicemaker.cm.batch.BatchJob;
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
public class MutableRecordIdTranslatorImpl implements MutableRecordIdTranslator {

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

	/**
	 * A delegate that implements methods declared by the
	 * ImmutableRecordIdTranslator interface. This instance is null in the
	 * MUTABLE state.
	 */
	private ImmutableRecordIdTranslator immutableTranslator;

	private final IRecordIdSinkSourceFactory rFactory;

	/** Typically a source of staging records */
	private IRecordIdSink sink1;

	/** Typically a source of master records */
	private IRecordIdSink sink2;

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
	private int currentIndex =
		ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;

	/**
	 * This is the internal index at which the indices for records from the
	 * second record source start. If this value is NOT_SPLIT, it means there is
	 * only 1 record source.
	 */
	private int splitIndex = NOT_SPLIT;

	public MutableRecordIdTranslatorImpl(IRecordIdSinkSourceFactory rFactory)
			throws BlockingException {
		this.rFactory = rFactory;
		sink1 = rFactory.getNextSink();
		sink1State = SINK_STATE.UNKNOWN;
		log.info("Sink 1: " + sink1);
		sink2 = rFactory.getNextSink();
		sink2State = SINK_STATE.UNKNOWN;
		log.info("Sink 2: " + sink2);
	}

	@Override
	public void cleanUp() throws BlockingException {
		if (sink1.exists()) {
			sink1.flush();
			sink1.close();
			sink1State = SINK_STATE.CLOSED;
			sink1.remove();
			sink1 = null;
			sink1State = null;
		}
		if (sink2.exists()) {
			sink2.flush();
			sink2.close();
			sink2State = SINK_STATE.CLOSED;
			sink2.remove();
			sink2 = null;
			sink2State = null;
		}
	}

	@Override
	public void close() throws BlockingException {
		log.info("close(): translatorState == " + translatorState);
		if (!isClosed()) {
			if (sink1.exists() && sink1State == SINK_STATE.OPEN) {
				log.info("Writing ids to sink1: " + count1);
				sink1.flush();
				log.info("Closing sink1");
				sink1.close();
				sink1State = SINK_STATE.CLOSED;
			} else {
				log.warning("Sink1 does not exist or is not open: " + sink1);
			}
			if (sink2.exists() && sink2State == SINK_STATE.OPEN) {
				log.info("Writing ids to sink2: " + count2);
				sink2.flush();
				log.info("Closing sink2");
				sink2.close();
				sink2State = SINK_STATE.CLOSED;
			} else {
				log.warning("Sink2 does not exist: " + sink2);
			}
			this.translatorState = TRANSLATOR_STATE.IMMUTABLE;
		} else {
			log.warning("translator is already closed");
		}
		log.info("close(): translatorState == " + translatorState);
		assert isClosed();
	}

	@Override
	public void open() throws BlockingException {
		if (translatorState != TRANSLATOR_STATE.MUTABLE) {
			throw new IllegalStateException("invalid translator state: "
					+ translatorState);
		}
		currentIndex = ImmutableRecordIdTranslator.MINIMUM_VALID_INDEX - 1;
		log.info("Opening sink1: " + sink1);
		sink1.open();
		sink1State = SINK_STATE.OPEN;
		_setSplitIndex(NOT_SPLIT);
	}

	@Override
	public void split() throws BlockingException {
		if (_getSplitIndex() == NOT_SPLIT) {
			_setSplitIndex(currentIndex + 1);
			log.info("Flushing sink1: " + sink1);
			sink1.flush();
			log.info("Closing sink1: " + sink1);
			sink1.close();
			sink1State = SINK_STATE.CLOSED;
			log.info("Opening sink2: " + sink2);
			sink2.open();
			sink2State = SINK_STATE.OPEN;
		} else {
			log.warning("Split method invoked on a previously split translator");
		}
	}

	@Override
	public int translate(Comparable o) throws BlockingException {
		if (translatorState != TRANSLATOR_STATE.MUTABLE) {
			throw new IllegalStateException("translator is no longer mutable");
		}
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
				_setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
				sink1.setRecordIDType(_getRecordIdType());
			}

			// figure out the id type for the second file
			if (_getSplitIndex() != NOT_SPLIT
					&& currentIndex == _getSplitIndex()) {
				if (_getRecordIdType() == null) {
					_setRecordIdType(RECORD_ID_TYPE.fromInstance(o));
				} else {
					assert _getRecordIdType() == RECORD_ID_TYPE.fromInstance(o);
				}
				assert _getRecordIdType() != null;
				sink2.setRecordIDType(_getRecordIdType());
			}

			if (_getSplitIndex() == NOT_SPLIT) {
				sink1.writeRecordID(o);
				++count1;
			} else {
				sink2.writeRecordID(o);
				++count2;
			}
		}
		return retVal;
	}

	@Override
	public ImmutableRecordIdTranslator toImmutableTranslator()
			throws BlockingException {
		log.info("toImmutableTranslator(): thread: "
				+ Thread.currentThread().getName());
		if (this.immutableTranslator != null) {
			String msg =
				"Already converted to an immutable translator: "
						+ immutableTranslator;
			log.fine(msg);
		} else {
			close();
			final IRecordIdSource s1 = rFactory.getSource(sink1);
			final IRecordIdSource s2 = rFactory.getSource(sink2);
			immutableTranslator = new ImmutableRecordIdTranslatorImpl(s1, s2);
		}
		assert isClosed();
		assert immutableTranslator != null;
		return immutableTranslator;
	}

	public boolean isClosed() {
		log.info("isClosed(): translatorState: " + translatorState);
		boolean retVal;
		if (this.translatorState == TRANSLATOR_STATE.MUTABLE) {
			retVal = false;
		} else {
			assert this.translatorState == TRANSLATOR_STATE.IMMUTABLE;
			retVal = true;
		}
		log.info("isClosed() == " + retVal);
		return retVal;
	}

	protected RECORD_ID_TYPE _getRecordIdType() {
		return recordIdType;
	}

	protected void _setRecordIdType(RECORD_ID_TYPE dataType) {
		this.recordIdType = dataType;
	}

	protected int _getSplitIndex() {
		return splitIndex;
	}

	protected void _setSplitIndex(int splitIndex) {
		this.splitIndex = splitIndex;
	}

	@Override
	public String toString() {
		return "MutableRecordIdTranslatorImpl [recordIdType=" + recordIdType
				+ ", translatorState=" + translatorState + ", currentIndex="
				+ currentIndex + ", splitIndex=" + splitIndex + ", sink1="
				+ sink1 + ", sink1State=" + sink1State + ", count1=" + count1
				+ ", sink2=" + sink2 + ", sink2State=" + sink2State
				+ ", count2=" + count2 + "]";
	}

}
