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

import java.util.ArrayList;
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
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class RecordValue2 {

	private static final Logger logger = Logger.getLogger(RecordValue2.class
			.getName());

	private static final int DEBUG_INTERVAL = 1000;

	private ArrayList recordList = null;

	public RecordValue2(IRecValSource rvSource) throws BlockingException {
		recordList = readColumnList(rvSource);
	}

	public Object get(int ind) {
		Object o = null;
		if (ind >= 0 && ind < recordList.size())
			o = recordList.get(ind);
		return o;
	}

	public Object get(long ind) {
		return get((int) ind);
	}

	public ArrayList getList() {
		return recordList;
	}

	public int size() {
		int s = 0;
		s = recordList.size();
		return s;
	}

	/**
	 * This method reads the ArrayList from a binary file. This assumes that the
	 * record ids coming in from the file is sorted in non-decreasing order.
	 *
	 * @param fileName
	 * @return ArrayList - array containing rec_id, val_id pairs.
	 */
	public static ArrayList readColumnList(IRecValSource rvSource)
			throws BlockingException {

		ArrayList list = new ArrayList(1000);
		int current = 0;

		rvSource.open();
		while (rvSource.hasNext()) {
			// Invariant
			assert list.size() == 0
					|| (list.size() == current + 1 && list.get(current) != null);

			// getNextRecID() and getNextValues() must be invoked together
			int row = (int) rvSource.getNextRecID();
			IntArrayList values = rvSource.getNextValues();

			if (values == null) {
				// Nothing to do
				continue;
			}

			if (row < current) {
				// Abort on illegal ordering
				String msg =
					"Illegal ordering for a record-value source:  (translated) "
							+ "record ids must be sorted in non-decreasing order. "
							+ "Current id: " + current + ", Next id: " + row;
				logger.severe(msg);
				throw new IllegalStateException(msg);

			} else if (row == current) {
				// Handle stacking on the primary key (virtual root)
				if (list.size() > 0) {
					assert list.size() == current + 1;
					IntArrayList previousValues =
						(IntArrayList) list.get(current);
					assert previousValues != null;
					values.addAll(previousValues);
					list.add(current, values);
				} else {
					assert list.size() == 0;
					list.add(values);
				}
				assert row == current;
				assert list.size() == current + 1;
				assert list.get(current) != null;
				debugRecordValues(current, values);

			} else {
				assert row > current;

				if (row > current + 1) {
					// Pad missing values
					for (int i = current; i < row - 1; i++) {
						current++;
						list.add(null);
					}
				}
				assert row == current + 1;

				current++;
				list.add(values);
				assert row == current;
				assert list.size() == current + 1;
				assert list.get(current) != null;
				debugRecordValues(current, values);
			}
			
			// Invariant tightens as soon as execution completes one loop
			assert row == current;
			assert list.size() == current + 1 && list.get(current) != null;
		}
		rvSource.close();

		return list;
	}

	private static void debugRecordValues(int current, IntArrayList values) {
		if (current % DEBUG_INTERVAL == 0 && logger.isLoggable(FINE)) {
			String msg =
				"Record id: " + current + ", values: " + values.toString();
			logger.fine(msg);
		}
	}

}
