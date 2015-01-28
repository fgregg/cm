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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.io.Serializable;

import com.choicemaker.util.IntArrayList;
import com.choicemaker.util.LongArrayList;

/**
 * This object represents a Blocking Set. It stores the columns id and values,
 * and a list of row ids in the set.
 *
 * @author pcheung
 *
 */
public class BlockSet implements Serializable, IIDSet {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 4684611552672342011L;

	IntArrayList columns; // LinkedList containing BlockValue objects.
	LongArrayList ids; // This stores the Record ids belonging to this blocking
						// set.

	private String toString;

	public BlockSet() {
		columns = new IntArrayList(3);
		ids = new LongArrayList(2);
	}

	public BlockSet(int column) {
		columns = new IntArrayList();
		columns.add(column);
		ids = new LongArrayList(2);
	}

	public void addColumn(int column) {
		columns.add(column);
		toString = null;
	}

	public void addColumns(IntArrayList cols) {
		columns.addAll(cols);
		toString = null;
	}

	public IntArrayList getColumns() {
		return columns;
	}

	public void addRecordID(int i) {
		ids.add(i);
		toString = null;
	}

	public void addRecordID(long l) {
		ids.add(l);
		toString = null;
	}

	public void setRecordIDs(LongArrayList list) {
		ids = list;
		toString = null;
	}

	@Override
	public LongArrayList getRecordIDs() {
		return ids;
	}

	public boolean equals(BlockSet bs) {
		if (this.columns.equals(bs.columns) && this.ids.equals(bs.ids))
			return true;
		else
			return false;
	}

	private static final int MAX_RECORD_IDS_PRINTED = 3;

	public String toString() {
		if (toString != null) {
			return toString;
		} else {
			String s = "BlockSet [recordIds: ";
			final int LIMIT = Math.min(MAX_RECORD_IDS_PRINTED, ids.size());
			if (LIMIT <= 0) {
				s += "<none>]";
			} else {
				for (int i = 0; i < LIMIT - 1; i++) {
					s += ids.get(i) + " ";
				}
				s += ids.get(LIMIT - 1) + " ...]";
			}
			toString = s;
		}
		assert toString != null;
		return toString;
	}

}
