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
package com.choicemaker.cm.io.blocking.automated.offline.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.choicemaker.util.IntArrayList;

/**
 * This object takes in an int array containing set root and return elements in
 * each set.  It sorts the roots ids to identify which elements belong in the
 * same set.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class InMemorySetIterator {
	
	private static final Logger log = Logger.getLogger(InMemorySetIterator.class.getName());

	/** This inner class is used to sort the array by set id.
	 * 
	 * @author pcheung
	 *
	 */
	private class Pair implements Comparable {
		private int id;
		private int set;
		
		public Pair (int i, int j) {
			id = i;
			set = j;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			Pair p = (Pair) o;
			if (this.set < p.set) return -1;
			else if (this.set > p.set) return 1;
			else return 0;
		}
	}
	
	
	/** This is the sorted array.
	 * 
	 */
	private Object [] array;
	
	/**
	 * The position in the current array.  We use this to build sets.
	 */
	private int position = 0;
	
	/**
	 * This next set.
	 */
	private IntArrayList next = null;
	
	
	
	/** This constructor takes in an int array of roots and builds sets with the same
	 * root.  The input is SetJoiner.flatten's output.
	 * 
	 * @param roots
	 */
	public InMemorySetIterator (int [] roots) {
		int size = roots.length;
		ArrayList pairs = new ArrayList(size);
		
		for (int i=0; i<size; i++) {
			if (roots[i] != SetJoiner.DEFAULT) {
				Pair p = new Pair (i, roots[i]);
				pairs.add(p);
			}
		}
		array = pairs.toArray();
		Arrays.sort (array);
		
		log.fine("array size: " + size);
		if (log.isLoggable(Level.FINE)) debug ();
	}
	
	
	private void debug () {
		StringBuffer sb = new StringBuffer ("id: ");
		int size = array.length;
		for (int i=0; i< size; i++) {
			Pair p = (Pair) array[i];
			sb.append(p.id);
			sb.append (' ');
		}
		log.fine(sb.toString());

		sb = new StringBuffer ("set: ");
		for (int i=0; i< size; i++) {
			Pair p = (Pair) array[i];
			sb.append(p.set);
			sb.append (' ');
		}
		log.fine(sb.toString());
	}
	
	
	/** This method gets the next set.
	 * 
	 * @return IntArrayList - this is null if there is no more sets
	 */
	private IntArrayList readNext () {
		int size = array.length;
		if (position >= size) return null;
		
		IntArrayList list = new IntArrayList ();
		
		Pair p = (Pair) array[position];
		int set = p.set;
		list.add(p.id);
		
		position ++;
		
		//special error case when the last set only has 1 element.
		if (position >= size) 
			throw new IllegalStateException ("Invalid root array.  The final set only has one element.");
		
		p = (Pair) array[position];
		while (position < size && p.set == set) {
			list.add(p.id);
			position ++;
			if (position < size) p = (Pair) array[position];
		}

		//special error case when the last set only has 1 element.
		if (list.size() == 1) 
			throw new IllegalStateException ("Invalid root array.  A set only has one element.");
		
		return list;
	}
	
	
	/**
	 * This checks to see if there are more sets.
	 * 
	 * @return true - if there are more sets.
	 */
	public boolean hasNext () {
		if (next != null) return true;
		else {
			next = readNext ();
			return (next != null);
		}
	}
	
	
	/** This returns the next set.
	 * 
	 * @return IntArrayList = next set.
	 */
	public IntArrayList getNext () {
		if (next == null) next = readNext ();
		
		IntArrayList ret = next;
		next = null;
		return ret;
	}
	
	

}
