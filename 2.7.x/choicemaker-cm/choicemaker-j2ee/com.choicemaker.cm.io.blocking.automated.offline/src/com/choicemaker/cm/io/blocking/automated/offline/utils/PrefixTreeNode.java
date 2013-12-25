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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


/**
 * @deprecated
 * 
 * Prefix tree has very bad performance due to searching. 
 * 
 *
 * Represents a node in a prefix tree, specifically designed from elimination
 * of blocking sets.
 * 
 * A Prefix Tree may looks like (2, 5, 7, 9, (10, 15)), where commas separate the levels.  
 * This tree represents two sets: (2, 5, 7, 9, 10) and (2, 5, 7, 9, 15).
 * 
 * Note that the tree is ordered.  Node 9 has two children.  Nodes 10 and 15 are childless. 
 * 
 * 
 * This class is not thread-safe in the least.
 * 
 * TODO: create a subclass of SuffixTreeNode for non-leaf nodes.  Then can move all
 * the putChild, getChild, removeChild, etc. funcationality (as well as numKids and kids
 * instance variables) in that class.
 * 
 * @author Put Cheung.  Based on the SuffixTreeNode by A Winkel.
 */

public class PrefixTreeNode {

	/**
	 * The id of the record this PrefixTreeNode represents.
	 */
	private final long recordId;
	
	/**
	 * A pointer to this node's parent node.
	 * This will be null in the case of the root node or if this node
	 * has been removed from its parent.
	 */
//	private final PrefixTreeNode parent;


	/**
	 * The number of children of this PrefixTreeNode in the prefix tree.
	 * This should (at all times) be equal
	 */
	private int numKids = 0;

	/**
	 * Child buckets.  Each bucket that contains a non-zero number of kids
	 * contains a linked list (linked by the SuffixTreeNode.next field) of
	 * child nodes.
	 */
	private PrefixTreeNode[] kids = null;
	
	/**
	 * This constructor is only be called by createRootNode();
	 */
	private PrefixTreeNode() { 
//		this.parent = null;
		this.recordId = -1;
	}

	/**
	 * Creates a new PrefixTreeNode with the specified parent and recordId.
	 */
	private PrefixTreeNode(PrefixTreeNode parent, long recordId) {
//		this.parent = parent;
		this.recordId = recordId;
		
		if (parent == null || recordId == -1) {
			throw new IllegalArgumentException(Long.toString(recordId));
		}
	}

	/**
	 * Returns true iff this PrefixTreeNode represents the end of a blocking set.
	 */		
	public boolean isLeaf() {
		return false;
	}
	
	/**
	 * Returns the id of blocking set this node represents.
	 * 
	 * @throws UnsupportedOperationException if this is a leaf node.
	 */
	public int getBlockingSetId() {
		throw new UnsupportedOperationException("getBlockingSetId called on an internal node");
	}

	/**
	 * Returns the child of this node with the specified record ID, or
	 * null if this node has no such child.
	 */		
	public PrefixTreeNode getChild(long childId) {
		if (kids == null) {
			return null;
		} else {
			PrefixTreeNode temp = new PrefixTreeNode(this, childId);
			PrefixTreeNodeComparator comparator = new PrefixTreeNodeComparator ();
			int ind = Arrays.binarySearch(kids, temp, comparator);
			
			if (ind >= 0 ) return kids[ind];
			else return null;
		}
	}
	
	/** This returns an ArrayList of all children with id less than or equal to the input.
	 * 
	 * @param id
	 * @return
	 */
	public ArrayList getChildren (long id) {
		if (kids == null) {
			return null;
		} else {
			PrefixTreeNode temp = new PrefixTreeNode(this, id);
			PrefixTreeNodeComparator comparator = new PrefixTreeNodeComparator ();
			int ind = Arrays.binarySearch(kids, temp, comparator);
			
			ArrayList A = new ArrayList ();
			int end = ind;
			if (ind < 0) end = -ind - 2;
			
			for (int i=0; i<=end; i++) {			
				A.add(kids[i]);
			}
	
			return A;
		}
	}
	
	
	public long getRecordId () {
		return recordId;
	}
	
	public int getNumChildren () {
		return numKids;
	}
	

	public PrefixTreeNode putChild(long childId) {
		return putChildImpl(new PrefixTreeNode(this, childId));
	}
	
	public PrefixTreeNode putLeafChild(long childId) {
		return putChildImpl(new LeafPrefixTreeNode(this, childId));
	}
	
	/**
	 * Creates and adds a child node with the specified ID to this PrefixTreeNode.
	 * If a child node with the specified childId exists, an IllegalArgumentException is
	 * thrown, as such a situation probably indicates a bug in the algorithm using 
	 * the SuffixTreeNode class.
	 * 
	 * If the child has an id than the parent, throw an IllegalArgumentException because
	 * there is a flaw in the logic of the code.
	 * 
	 * @throws IllegalArgumentException if a node with childId already exists or if childId
	 * is an invalid record ID (see Constructor)
	 */
	private PrefixTreeNode putChildImpl(PrefixTreeNode kid) {
		long childId = kid.recordId;
		
		if (childId < recordId) {
			throw new IllegalArgumentException("Child node with id " + childId + " is less than "
			+ "current id " + recordId);
		}

		if (kids == null) {
			//create a new array of size 1
			kids = new PrefixTreeNode [1];
			kids[0] = kid;
		} else {
			PrefixTreeNodeComparator comparator = new PrefixTreeNodeComparator ();
			
			//find the place in the old array to insert the new record.
			int ind = Arrays.binarySearch(kids, kid, comparator);
			
			//Should not try to insert a child node with the same id as an existing node.
			if (ind >= 0 && kids[ind].recordId == childId) {
				throw new IllegalArgumentException("Child node with id " + childId + " already exists");
			}
			
			//process the return value for insertion
			if (ind < 0) ind = -ind - 1;
			
			//increase the array by 1 and then insert the new kid
			PrefixTreeNode [] newKids = new PrefixTreeNode [numKids + 1];
			for (int i=0; i< ind; i++) {
				newKids[i] = kids[i];
			}
			for (int i=numKids-1; i>=ind; i--) {
				newKids[i+1] = kids[i];
			}
			newKids[ind] = kid;
			
			kids = null;
			kids = newKids;
		}
		
		numKids++;
		
//		System.out.println ("done putChild " + this);

		return kid;
	}
	
	

		
	/**
	 * Factory method for creating the root of a suffix tree.
	 */
	public static PrefixTreeNode createRootNode() {
		return new PrefixTreeNode();
	}
	
	
	/** returns the record ID and number of children for debugging.
	 * 
	 */
	public String toString () {
		StringBuffer sb = new StringBuffer ();
		sb.append(recordId);
		
		sb.append (" |");
		if (kids != null) {
			for (int i=0; i<kids.length; i++) {
				if (kids[i] != null) {
					sb.append(kids[i].recordId);
					sb.append (' ');
				} else {
					sb.append ("null ");
				}
			}
		}
		return sb.toString();
	}
	
	
	
	/** This returns a string of its children.
	 * 
	 * @return
	 */
	public String writeDebug (int level) {
		StringBuffer sb = new StringBuffer ();
		sb.append(level);
		sb.append("|");
		sb.append(recordId);
		sb.append(" ");
		
		if (isLeaf()) sb.append ("\n");
		
		if (numKids > 0) {
			for (int i=0; i<numKids; i++) {
				sb.append (kids[i].writeDebug(level + 1) );
			}
		}
		
		return sb.toString();
	}
	
	
	/** This returns a string of its children.
	 * 
	 * @return
	 */
	public String writeDebug (String base) {
		StringBuffer sb = new StringBuffer ();
		sb.append(base);
		sb.append(" ");
		sb.append(recordId);
		sb.append(" ");
		
		if (isLeaf()) sb.append ("\n");
		
		if (numKids > 0) {
			String previous = sb.toString();
			sb = new StringBuffer();
			for (int i=0; i<numKids; i++) {
				sb.append (kids[i].writeDebug(previous) );
			}
		}
		
		return sb.toString();
	}
	
	
	public int getNumNodes () {
		int ret = numKids;
		
		for (int i=0; i<numKids; i++) {
			ret += kids[i].getNumNodes();
		}
		
		return ret;
	}
	
	
	
	private static class PrefixTreeNodeComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			PrefixTreeNode p1 = (PrefixTreeNode) o1;
			PrefixTreeNode p2 = (PrefixTreeNode) o2;
			
			if (p1.getRecordId() < p2.getRecordId()) return -1;
			else if (p1.getRecordId() > p2.getRecordId()) return 1;
			else return 0;
		}
	}
	
	
	
	/**
	 * Subclass that's used to represent leaf nodes.  In the context of this
	 * problem, leaf nodes represent unsubsumed blocking sets, and thus have no
	 * children.  (If a node has children, it is subsumed by the blocking sets represented
	 * by each of its children, and their children, and their children...)
	 */	
	private static class LeafPrefixTreeNode extends PrefixTreeNode {
		
		public LeafPrefixTreeNode(PrefixTreeNode parent, long recordId) {
			super(parent, recordId);
		}
		
		public boolean isLeaf() {
			return true;
		}
		
		public PrefixTreeNode getChild(long childId) {
			throw new UnsupportedOperationException("Leaf nodes have no children");
		}
		
		public PrefixTreeNode putChild(long childId) {
			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
		}
		
		public PrefixTreeNode putChild(long childId, int blockingSetId) {
			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
		}
	}

}
