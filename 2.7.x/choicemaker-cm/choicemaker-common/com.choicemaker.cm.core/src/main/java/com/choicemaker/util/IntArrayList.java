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
package com.choicemaker.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * List of int elements. Similar to java.lang.List or
 * java.util.ArrayList, but it stores primitive int values.
 * Generic collections for primitive types should remove the
 * need for IntArrayList. I implemented only those methods
 * that I actually need.
 *
 * @author    Martin Buechi
 * @author    rphall (added toList() method)
 */

public class IntArrayList implements Serializable, Cloneable {

	static final long serialVersionUID = 271L;

	/** Storage for elements. */
	private int[] data;

	/** Number of data elements actually used. */
	private int size;

	//--------------------------------------------------------------

	/** Creates a new empty List, initial size is 16. */
	public IntArrayList() {
		this(16);
	}

	public IntArrayList(int initialSize) {
		data = new int[initialSize];
		size = 0;
	}

	public IntArrayList(int[] dataInit) {
		this(dataInit.length,dataInit);
	}

	public IntArrayList(int sizeInit, int[] dataInit) {
		size = sizeInit;
		data = dataInit;
	}


	/**
	 * Copy constructor.
	 * @param  orig  List that supplies the initial elements for
	 *               the new List.
	 */
	public IntArrayList(IntArrayList orig) {
		data = new int[orig.data.length];
		size = orig.size;
		System.arraycopy(orig.data, 0, data, 0, size);
	}

	//-----------------------------------------------------------------

	/**
	 * Returns a copy of this IntArrayList.  
	 */
	public Object clone() {
		try {
			IntArrayList copy = (IntArrayList)super.clone();
			copy.data = this.toArray();
			
			return copy;
		} catch (CloneNotSupportedException ex) {
			// never happens; this class is Cloneable.
			return null;
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof IntArrayList) {
			IntArrayList operand = (IntArrayList) obj;
			if (size == operand.size) {
				for (int i = 0; i < size; i++) {
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
		int hash = 1;
		for (int i = 0; i < size; i++) {
			hash = (43 * hash) + get(i);
		}
		return hash;
	}

	/**
	 * Gets an element from the List.
	 * @param   idx  index of the element asked for
	 * @return  selected element
	 * @throws IndexOutOfBoundsException if <code>idx</code> is out of range
	 */
	public int get(int idx) {
		if (idx < 0 || idx >= size) {
			throw new IndexOutOfBoundsException(String.valueOf(idx));
		}
		return data[idx];
	}

	/**
	 * Replaces an element in the List.
	 * @param  ele  new element
	 * @param  idx  index of the element to be replaced
	 * @throws IndexOutOfBoundsException if <code>idx</code> is out of range
	 */
	public void set(int idx, int ele) {
		if (idx < 0 || idx >= size) {
			throw new IndexOutOfBoundsException(String.valueOf(idx));
		}
		data[idx] = ele;
	}

	/**
	 * Enlarges this IntArrayList, if needed, in order to accomodate a <code>min</code> total elements,
	 * including existing elements.
	 */
	public void ensureCapacity(int min) {
		int[] tmp;
		int old;
		int capacity;

		old = data.length;
		if (min > old) {
			tmp = data;
			capacity = (old * 5) / 3 + 1;
			if (capacity < min) {
				capacity = min;
			}
			data = new int[capacity];
			System.arraycopy(tmp, 0, data, 0, size);
		}
	}

	/**
	 * Adds an element to the List. All following elements
	 * are moved up by one index.
	 * @param  idx  where to insert the new element
	 * @param  ele  new element
	 * @throws IndexOutOfBoundsException if <code>idx</code> is out of range
	 */
	public void add(int idx, int ele) {
		if (idx < 0 || idx > size) {
			throw new IndexOutOfBoundsException(String.valueOf(idx));
		}
		ensureCapacity(size + 1);
		System.arraycopy(data, idx, data, idx + 1, size - idx);
		data[idx] = ele;
		size++;
	}

	/**
	 * Adds an element to the end of the List.
	 * @param  ele  new element
	 */
	public void add(int ele) {
		ensureCapacity(size + 1);
		data[size++] = ele;
	}

	public void addAll(IntArrayList op) {
		ensureCapacity(size + op.size);
		System.arraycopy(op.data, 0, data, size, op.size);
		size += op.size;
	}

	/**
	 * Removes an element from the List. All following elements
	 * are moved down by one index.
	 * @param  idx  index of the element to remove
	 * @throws IndexOutOfBoundsException if <code>idx</code> is out of range
	 */
	public void remove(int idx) {
		if (idx < 0 || idx >= size) {
			throw new IndexOutOfBoundsException(String.valueOf(idx));
		}
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
	public int indexOf(int ele) {
		int i;

		for (i = 0; i < size; i++) {
			if (data[i] == ele) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(int ele) {
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
	public int[] toArray() {
		return toArray(new int[size]);
	}

	public int[] toArray(int[] result) {
		if (result.length < size) {
			result = new int[size];
		}
		System.arraycopy(data, 0, result, 0, size);
		return result;
	}

	/**
	 * Sorts the elements of this IntArrayList in place using 
	 * <code>java.util.Arrays.sort(int[])</code>.
	 */
	public void sort() {
		Arrays.sort(data, 0, size);
	}

	//-----------------------------------------------------------------

	/**
	 * Returns a string representation.
	 * @return string representation
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer(size * 3); // minimal guess at length
		buff.append('[');
		
		int max = size();
		if (max > 0) {
			buff.append(get(0));
		}
		
		for (int i = 1; i < max; i++) {
			buff.append(", ");
			buff.append(get(i));
		}
		
		buff.append(']');
		
		return buff.toString();
	}
	
//	/**
//	 * Converts to a list of Integer values. A bridge method to allow
//	 * incremental replacement of IntArrayClass by generic ArrayList<Integer>
//	 * and List<Integer> instances.
//	 */
//	public List toList() {
//		return new List() {
//			final IntArrayList d = IntArrayList.this;
//
//			public Object clone() {
//				return d.clone();
//			}
//
//			public boolean equals(Object obj) {
//				return d.equals(obj);
//			}
//
//			public int hashCode() {
//				return d.hashCode();
//			}
//
//			public Object get(int idx) {
//				return Integer.valueOf(d.get(idx));
//			}
//
//			public void clear() {
//				d.clear();
//			}
//
//			public int size() {
//				return d.size();
//			}
//
//			public boolean isEmpty() {
//				return d.isEmpty();
//			}
//
//			public String toString() {
//				return d.toString();
//			}
//
//			public boolean contains(Object o) {
//				boolean retVal = false;
//				if (o instanceof Integer) {
//					int i = ((Integer) o).intValue();
//					retVal = d.contains(i);
//				}
//				return retVal;
//			}
//
//			public Object[] toArray() {
//				return toArray(new Integer[d.size]);
//			}
//
//			public Object[] toArray(Object[] a) {
//				Integer[] aI = (Integer[]) a;
//				int[] ia = d.toArray();
//				if (ia.length > aI.length) {
//					aI = new Integer[ia.length];
//				}
//				for (int i = 0; i < ia.length; i++) {
//					aI[i] = Integer.valueOf(ia[i]);
//				}
//				return aI;
//			}
//
//			public boolean add(Object e) {
//				int i = ((Integer) e).intValue();
//				d.add(i);
//				return true;
//			}
//
//			public void add(int index, Object element) {
//				int i = ((Integer) element).intValue();
//				d.add(index, i);
//			}
//
//			public boolean addAll(Collection c) {
//				boolean retVal = false;
//				for (Iterator it = c.iterator(); it.hasNext();) {
//					Integer I = (Integer) it.next();
//					retVal = retVal || add(I);
//				}
//				return retVal;
//			}
//
//			public Object set(int index, Object element) {
//				Object retVal = get(index);
//				int ele = ((Integer) element).intValue();
//				d.set(index, ele);
//				return retVal;
//			}
//
//			public boolean remove(Object o) {
//				Integer I = (Integer) o;
//				int i = I.intValue();
//				boolean retVal = d.contains(i);
//				if (retVal) {
//					d.remove(i);
//				}
//				return retVal;
//			}
//
//			public Object remove(int index) {
//				int i = d.get(index);
//				Integer retVal = Integer.valueOf(i);
//				d.remove(index);
//				return retVal;
//			}
//
//			public int indexOf(Object o) {
//				int retVal = -1;
//				if (o instanceof Integer) {
//					int i = ((Integer) o).intValue();
//					retVal = d.indexOf(i);
//				}
//				return retVal;
//			}
//
//			public Iterator iterator() {
//				throw new Error("not implemented");
//			}
//
//			public boolean addAll(int index, Collection c) {
//				throw new Error("not implemented");
//			}
//
//			public boolean containsAll(Collection c) {
//				throw new Error("not implemented");
//			}
//
//			public boolean removeAll(Collection c) {
//				throw new Error("not implemented");
//			}
//
//			public boolean retainAll(Collection c) {
//				throw new Error("not implemented");
//			}
//
//			public int lastIndexOf(Object o) {
//				throw new Error("not implemented");
//			}
//
//			public ListIterator listIterator() {
//				throw new Error("not implemented");
//			}
//
//			public ListIterator listIterator(int index) {
//				throw new Error("not implemented");
//			}
//
//			public List subList(int fromIndex, int toIndex) {
//				throw new Error("not implemented");
//			}
//
//		};
//	}

}
