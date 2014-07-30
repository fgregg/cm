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
package com.choicemaker.cm.core.sort;

import java.util.Comparator;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.DescriptorCollection;

/**
 * Comment
 *
 * @author   Martin Buechi
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:06 $
 */
public class RecordComparator implements Comparator {
	private Descriptor descriptor;
	private int col;
	private boolean ascending;
	private Condition condition;

	public RecordComparator(Descriptor rootDescriptor, SortCondition sortCondition) {
		DescriptorCollection c = new DescriptorCollection(rootDescriptor);
		descriptor = c.getDescriptor(sortCondition.getNode());
		col = descriptor.getColumnIndexByName(sortCondition.getField());
		this.ascending = sortCondition.isAscending();
	}

	public RecordComparator(Descriptor rootDescriptor, SortCondition sortCondition, Condition condition) {
		this(rootDescriptor, sortCondition);
		this.condition = condition;
	}

	public int compare(Object o1, Object o2) {
		int res;
		Comparable v1 = getComparisonValue((Record) o1);
		Comparable v2 = getComparisonValue((Record) o2);
		if (v1 == null) {
			res = v2 == null ? 0 : -1;
		} else if (v2 == null) {
			res = 1;
		} else {
			res = v1.compareTo(v2);
		}
		if (!ascending) {
			res = -res;
		}
		return res;
	}

	private Comparable getComparisonValue(Record r) {
		Comparable value = null;
		int numRows = descriptor.getRowCount(r);
		for (int i = 0; i < numRows; ++i) {
			Object o = descriptor.getValue(r, i, col);
			Comparable v = null;
			if (o != null) {
				if (o instanceof Comparable) {
					v = (Comparable) o;
				} else {
					v = o.toString();
				}
			}
			if (condition == null || condition.accept(r, i, o)) {
				if (value == null || (v != null && (v.compareTo(value) < 0 ^ !ascending))) {
					value = v;
				}
			}
		}
		return value;
	}
}
