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
package com.choicemaker.cm.core.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This is a wrapper object which contains a array of long that can grow when necessary.  It is better than using
 * ArrayList of Long Object.
 * 
 * @author pcheung
 *
 */
public class LongArrayList implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 6480979214963160995L;

	/** Storage for elements. */
	private long[] data;

	/** Number of data elements actually used. */
	private int size;

	public LongArrayList(int sizeInit, long[] dataInit) {
		size = sizeInit;
		data = dataInit;
	}

	//--------------------------------------------------------------

	/** Creates a new empty List, initial size is 16. */
	public LongArrayList() {
		this(16);
	}

	public LongArrayList(int initialSize) {
		data = new long[initialSize];
		size = 0;
	}

	/**
	 * Copy constructor.
	 * @param  orig  List that supplies the initial elements for
	 *               the new List.
	 */
	public LongArrayList(LongArrayList orig) {
		data = new long[orig.data.length];
		size = orig.size;
		System.arraycopy(orig.data, 0, data, 0, size);
	}

	//-----------------------------------------------------------------

	public boolean equals(Object obj) {
		LongArrayList operand;
		int i;

		if (obj instanceof LongArrayList) {
			operand = (LongArrayList) obj;
			if (size == operand.size) {
				for (i = 0; i < size; i++) {
					if (data[i] != operand.data[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return size;
	}

	/**
	 * Gets an element from the List.
	 * @param   idx  index of the element asked for
	 * @return  selected element
	 */
	public long get(int idx) {
		return data[idx];
	}

	/**
	 * Replaces an element in the List.
	 * @param  ele  new element
	 * @param  idx  index of the element to be replaced
	 */
	public void set(int idx, long ele) {
		data[idx] = ele;
	}

	public void ensureCapacity(int min) {
		long[] tmp;
		int old;
		int capacity;

		old = data.length;
		if (min > old) {
			tmp = data;
			capacity = (old * 5) / 3 + 1;
			if (capacity < min) {
				capacity = min;
			}
			data = new long[capacity];
			System.arraycopy(tmp, 0, data, 0, size);
		}
	}

	/**
	 * Adds an element to the List. All following elements
	 * are moved up by one index.
	 * @param  idx  where to insert the new element
	 * @param  ele  new element
	 */
	public void add(int idx, long ele) {
		ensureCapacity(size + 1);
		System.arraycopy(data, idx, data, idx + 1, size - idx);
		data[idx] = ele;
		size++;
	}

	/**
	 * Adds an element to the end of the List.
	 * @param  ele  new element
	 */
	public void add(long ele) {
		ensureCapacity(size + 1);
		data[size++] = ele;
	}

	public void addAll(LongArrayList op) {
		ensureCapacity(size + op.size);
		System.arraycopy(op.data, 0, data, size, op.size);
		size += op.size;
	}

	/**
	 * Removes an element from the List. All following elements
	 * are moved down by one index.
	 * @param  idx  index of the element to remove
	 */
	public void remove(int idx) {
		size--;
		System.arraycopy(data, idx + 1, data, idx, size - idx);
	}

	/**
	 * Removes all elements.
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Searches an element.
	 * @param   ele  element to look for
	 * @return  index of the first element found; -1 if nothing was found
	 */
	public int indexOf(long ele) {
		int i;

		for (i = 0; i < size; i++) {
			if (data[i] == ele) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(long ele) {
		return indexOf(ele) != -1;
	}

	/**
	 * Returns the number of elements in the List.
	 * @return number of elements
	 */
	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Creates an array with all elements of the List.
	 * @return  the array requested
	 */
	public long[] toArray() {
		return toArray(new long[size]);
	}

	public long[] toArray(long[] result) {
		if (result.length < size) {
			result = new long[size];
		}
		System.arraycopy(data, 0, result, 0, size);
		return result;
	}

	public void sort() {
		Arrays.sort(data, 0, size);
	}

	//-----------------------------------------------------------------

	/**
	 * Returns a string representation.
	 * @return string representation
	 */
	public String toString() {
		StringBuffer buf;
		int i, max;

		max = size();
		buf = new StringBuffer();
		for (i = 0; i < max; i++) {
			buf.append(" " + get(i));
		}
		return buf.toString();
	}

}
