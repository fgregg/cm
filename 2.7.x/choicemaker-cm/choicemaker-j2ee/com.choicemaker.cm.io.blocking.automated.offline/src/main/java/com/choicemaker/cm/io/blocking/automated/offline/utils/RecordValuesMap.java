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
package com.choicemaker.cm.io.blocking.automated.offline.utils;

import static java.util.logging.Level.FINE;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;
import com.choicemaker.util.IntArrayList;

/**
 * This object takes a IRecValSource and creates a ArrayList storing the record
 * ids and IntArrayList of value ids.
 *
 * The critical change to this code is that it uses IntArrayList instead of
 * Integer as map value and ArrayList value to handle stacking.
 *
 * This version uses the input record id to internal translator, therefore we
 * could use an array instead of hashMap;
 *
 * @author pcheung
 *
 */
public class RecordValuesMap /* implements Map<Integer, List<Integer>> */{

	public static interface ITranslatedRecordIdValueHashes {
		int getTranslatedRecordId();

		List<Integer> getValueHashes();
	}

	private static final Logger logger = Logger.getLogger(RecordValuesMap.class
			.getName());

	private static final int DEBUG_INTERVAL = 1000;

	private List<IntArrayList> recordList = null;

	public RecordValuesMap(IRecValSource rvSource) throws BlockingException {
		recordList = readColumnList(rvSource);
	}

	public IntArrayList getValues(int ind) {
		IntArrayList o = null;
		if (ind >= 0 && ind < recordList.size()) {
			o = recordList.get(ind);
		}
		return o;
	}

	public IntArrayList getValues(long ind) {
		return getValues((int) ind);
	}

	/**
	 * Returns a list of value lists, indexed by (translated) record id.
	 */
	public List<IntArrayList> getIndexedValues() {
		return new AbstractList<IntArrayList>() {

			@Override
			public IntArrayList get(int index) {
				return getValues(index);
			}

			@Override
			public int size() {
				return RecordValuesMap.this.size();
			}

		};
	}

	public int size() {
		int s = recordList.size();
		return s;
	}

	/**
	 * This method reads the ArrayList from a binary file. This assumes that the
	 * record ids coming in from the file is sorted in non-decreasing order.
	 *
	 * @param fileName
	 * @return ArrayList - array containing rec_id, val_id pairs.
	 */
	public static List<IntArrayList> readColumnList(IRecValSource rvSource)
			throws BlockingException {

		// List of (hashed) values indexed by (translated) id.
		List<IntArrayList> list = new ArrayList<>(1000);

		// Current translated id
		// FIXME code would be simpler if cid starts at -1
		int cid = 0;

		rvSource.open();
		while (rvSource.hasNext()) {
			// Loose loop invariant
			assert (list.isEmpty() && cid == 0)
					|| (list.size() == cid + 1 && list.get(cid) != null);

			// getNextRecID() and getNextValues() must be invoked together
			final int nextId = (int) rvSource.getNextRecID();
			IntArrayList values = rvSource.getNextValues();

			if (values == null) {
				// Nothing to do
				continue;
			}

			if (nextId < cid) {
				// Abort on illegal ordering
				String msg =
					"Illegal ordering for a record-value source:  (translated) "
							+ "record ids must be sorted in non-decreasing order. "
							+ "Current id: " + cid + ", Next id: " + nextId;
				logger.severe(msg);
				throw new IllegalStateException(msg);

			} else if (nextId == cid) {
				// Handle stacking on the primary key (virtual root)
				if (!list.isEmpty()) {
					assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
					IntArrayList previousValues =
						(IntArrayList) list.remove(cid);
					assert previousValues != null;
					values.addAll(previousValues);
					values = sortDedup(values);
					list.add(cid, values);
				} else {
					assert list.isEmpty();
					assert cid == 0;
					values = sortDedup(values);
					list.add(values);
					assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
				}
				// Invariant tightened
				assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
				assert list.get(cid) != null : contentMsg(list, cid);
				assert !list.isEmpty() : listEmptyMsg();
				assert nextId == cid : rowIsCurrentMsg(nextId, cid);
				debugRecordValues(cid, values);

			} else {
				assert nextId > cid;
				if (list.isEmpty()) {
					assert (cid == 0);
				} else {
					assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
				}

				if (nextId > cid) {
					// Pad missing values
					if (list.isEmpty()) {
						list.add(null);
						assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
					}
					for (int i = cid; i < nextId - 1; i++) {
						list.add(null);
						cid++;
						// Loose invariant partially honored
						assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
						// Loose invariant partially violated
						assert list.get(cid) == null;
					}
					assert nextId == cid + 1 : expectedId(nextId, cid + 1);
					// Loose invariant partially honored
					assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
				}
				values = sortDedup(values);
				list.add(values);
				cid++;

				// Invariant tightened
				assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
				assert list.get(cid) != null : contentMsg(list, cid);
				assert !list.isEmpty() : listEmptyMsg();
				assert nextId == cid : rowIsCurrentMsg(nextId, cid);
				debugRecordValues(cid, values);
			}

			// Loop invariant tightens after one loop
			assert list.size() == cid + 1 : sizeMsg(list, cid + 1);
			assert list.get(cid) != null : contentMsg(list, cid);
			assert !list.isEmpty() : listEmptyMsg();
			assert nextId == cid : rowIsCurrentMsg(nextId, cid);
		}
		rvSource.close();

		return list;
	}

	public static IntArrayList sortDedup(IntArrayList ial) {
		IntArrayList retVal = new IntArrayList();
		int max = Integer.MIN_VALUE;
		ial.sort();
		for (int j = 0; j < ial.size(); j++) {
			int i = ial.get(j);
			if (i > max || j == 0) {
				retVal.add(i);
				max = i;
			}
		}
		return retVal;
	}

	private static String expectedId(int row, int expected) {
		String msg =
			"Invariant violated: row (" + row + ") is not expected ("
					+ expected + ")";
		return msg;
	}

	private static String rowIsCurrentMsg(int row, int current) {
		String msg =
			"Invariant violated: row (" + row + ") is not current (" + current
					+ ")";
		return msg;
	}

	private static String listEmptyMsg() {
		String msg = "Invariant violated: list is empty";
		return msg;
	}

	private static String sizeMsg(List<IntArrayList> list, int size) {
		String msg =
			"Invariant violated: list.size() == " + list.size()
					+ ", expected size == " + size;
		return msg;
	}

	private static String contentMsg(List<IntArrayList> list, int idx) {
		String msg = "Invariant violated: list.get(" + idx + ")  is null";
		return msg;
	}

	private static void debugRecordValues(int current, IntArrayList values) {
		if (current % DEBUG_INTERVAL == 0 && logger.isLoggable(FINE)) {
			String msg =
				"Record id: " + current + ", values: " + values.toString();
			logger.fine(msg);
		}
	}

}
