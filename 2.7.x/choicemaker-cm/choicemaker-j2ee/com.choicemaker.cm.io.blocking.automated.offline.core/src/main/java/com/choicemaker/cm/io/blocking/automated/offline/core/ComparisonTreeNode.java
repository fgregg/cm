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
import java.util.ArrayList;
import java.util.List;

/**
 * @author pcheung
 *
 */
public class ComparisonTreeNode<T extends Comparable<T>> implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -8258266551802037565L;

	public static final char STAGE = 'S';
	public static final char MASTER = 'M';


	/**
	 * We hardcode the effective load factor as .75f, and use the the value 
	 * of numKids * 100 as the threshold for expansion.
	 */
	public static final int LOAD_FACTOR = 75;

	/**
	 * The id of the record this ComparisonTreeNode represents.
	 */
	private final T recordId;
	
	/** This indicates if the record is a staging or master record.
	 * 
	 */
	private char type;
	
	
	/**
	 * A pointer to this node's parent node.
	 * This will be null in the case of the root node or if this node
	 * has been removed from its parent.
	 */
//	private final ComparisonTreeNode parent;

	/**
	 * The next node in the linked list that this SuffixTreeNode
	 * is in its parent.
	 */
	private ComparisonTreeNode<T> next = null;

	/**
	 * The number of children of this SuffixTreeNode in the suffix tree.
	 * This should (at all times) be equal
	 */
	private int numKids = 0;

	/**
	 * Child buckets.  Each bucket that contains a non-zero number of kids
	 * contains a linked list (linked by the SuffixTreeNode.next field) of
	 * child nodes.
	 */
	private ComparisonTreeNode<T>[] kids = null;

	/**
	 * This constructor is only be called by createRootNode();
	 */
	private ComparisonTreeNode() { 
		this.recordId = null;
	}

	/**
	 * Creates a new ComparisonTreeNode with the specified parent and recordId.
	 */
	private ComparisonTreeNode(T c, char type) {
		this.recordId = c;
		this.type = type;

		if (c == null) {
			throw new IllegalArgumentException("Null Tree node.");
		} 
	}

	/**
	 * Returns true iff this ComparisonTreeNode has a valid blockingSetId.  That is,
	 * this node represents the end of a blocking set.
	 */		
	public boolean hasBlockingSetId() {
		return false;
	}
	
	public boolean isStage () {
		return (type == STAGE);
	}
	
	/**
	 * Returns the id of blocking set this node represents.
	 * 
	 * @throws UnsupportedOperationException if this is a leaf node.
	 */
	public int getBlockingSetId() {
		throw new UnsupportedOperationException("getBlockingSetId called on an internal node");
	}
	
	
	/** PC 4/5/05
	 *  
	 * This returns all the children of this node.
	 * 
	 * @return ArrayList
	 */
	public List<ComparisonTreeNode<T>> getAllChildren () {
		List<ComparisonTreeNode<T>> children = null;
		if (numKids > 0) {
			children = new ArrayList<>();
			for (int i=0; i< kids.length; i++) {
				if (kids[i] != null) {
					ComparisonTreeNode<T> kid = kids[i];
					children.add(kid);
					while (kid.next != null) {
						kid = kid.next;
						children.add(kid);
					}
				}
			}
		}
		return children;
	}
	
	
	/** PC 4/5/05
	 * 
	 * This returns the number of children this node has. 
	 * 
	 * @return int
	 */
	public int getNumKids () {
		return numKids;
	}
	

	/** PC 4/5/05
	 * 
	 * This returns a string represetation of the tree.  For example, it might look like
	 * 
	 * prefix 1 2 3 4 5
	 * 1 2 6
	 * 1 3 6 7
	 * 1 3 6 9
	 * 
	 * This basically writes out the tree as blocks.
	 * 
	 * @param prefix
	 * @return String
	 */
	public String writeTree (String prefix) {
		StringBuffer sb = new StringBuffer (prefix);
		
		if (recordId == null) sb.append("null");
		else {
			sb.append(type);
			sb.append(':');
			sb.append(recordId.toString());
		} 
		
		if (numKids == 0) {
			sb.append(Constants.LINE_SEPARATOR);
			return sb.toString();
		} else {
			List<ComparisonTreeNode<T>> children = getAllChildren();
			sb.append(' ');
			String temp = sb.toString();
			sb = new StringBuffer ();
			for (int i=0; i<children.size(); i++) {
				ComparisonTreeNode<T> kid = children.get(i);
				sb.append( kid.writeTree(temp) );
			}
			
			return sb.toString();
		}
	}


	/**
	 *  This returns a string representation of the tree where each node is surrounded by [ and ].
	 * For example: [S:2[S:4[S:6[M:8]][M:7]]].  Note that 4 has two children: 6 and 7.
	 * 
	 * @param sb
	 */
	public void writeTree2 (StringBuffer sb) {
		sb.append(Constants.OPEN_NODE);
		sb.append(type);
		sb.append(':');
		sb.append(recordId.toString());
		
		if (numKids > 0) {
			List<ComparisonTreeNode<T>> children = getAllChildren();
			for (int i=0; i<children.size(); i++) {
				ComparisonTreeNode<T> kid = children.get(i);
				kid.writeTree2(sb);
			}
		}

		sb.append(Constants.CLOSE_NODE);
	}

/*
	public String writeTree2 (String prefix) {
		StringBuffer sb = new StringBuffer (prefix);
		sb.append(Constants.OPEN_NODE);
		sb.append(type);
		sb.append(':');
		sb.append(recordId.toString());
		
		if (numKids > 0) {
			ArrayList children = getAllChildren();
			for (int i=0; i<children.size(); i++) {
				ComparisonTreeNode kid = (ComparisonTreeNode) children.get(i);
				sb.append( kid.writeTree2("") );
			}
		}

		sb.append(Constants.CLOSE_NODE);
		return sb.toString();
	}
*/
	
/*	
	private void readObject (ObjectInputStream stream) throws IOException, ClassNotFoundException {
		char c = stream.readChar();
		System.out.println (c);
		boolean stop = false;
		while (!stop) {
			if (c == Constants.OPEN_NODE) {
				c = stream.readChar();
				this.type = c;
				
				c = stream.readChar(); //this is a ':'
			}
		}
	}
	
	
	private void writeObject (ObjectOutputStream stream) throws IOException {
		stream.writeChars(writeTree2(""));
	}
*/

	private int getBucket (T id, int length) {
		int ret = 0;
		int i = id.hashCode();
//		if (id > 0) ret = i % length;
//		else ret = -i % length;

		ret = i % length;
		if (ret < 0) ret = -ret;
		
		return ret;
	}
	
	public T getRecordId () {
		return recordId;
	}
	
	
	public ComparisonTreeNode<T> putChild(T childId, char type) {
		return putChildImpl(new ComparisonTreeNode<T>(childId, type));
	}
	
	public ComparisonTreeNode<T> putChild(T childId, char type, int blockingSetId) {
		return putChildImpl(new LeafComparisonTreeNode<T>(childId, type, blockingSetId));
	}
	
	/**
	 * Creates and adds a child node with the specified ID to this SuffixTreeNode.
	 * If a child node with the specified childId exists, an IllegalArgumentException is
	 * thrown, as such a situation probably indicates a bug in the algorithm using 
	 * the SuffixTreeNode class.
	 * 
	 * @throws IllegalArgumentException if a node with childId already exists or if childId
	 * is an invalid record ID (see Constructor)
	 */
	private ComparisonTreeNode<T> putChildImpl(ComparisonTreeNode<T> kid) {
		T childId = kid.recordId;

		ensureCapacity();
//		int bucket = (int) (childId % kids.length);
		int bucket = getBucket(childId,kids.length);

		// check that no child node with the given ID previously existed.
		ComparisonTreeNode<T> node = kids[bucket];
		while (node != null) {
			if (node.recordId == childId) {
				throw new IllegalArgumentException("Child node with id " + childId + " already exists");
			}
			node = node.next;
		}

		// stick it on the front of the list
		kid.next = kids[bucket];
		kids[bucket] = kid;
		numKids++;

		return kid;
	}
	
	/**
	 * Ensures that this SuffixTreeNode has can add one more node without violating
	 * the hashtable/load factor contract.  This contract says that a hash structure
	 * should obey the following formula: numKids / numBuckets  <=  LOAD_FACTOR.
	 * 
	 * This SuffixTreeNode's kids array will be grown up to and until the length
	 * is Integer.MAX_VALUE, at which point the performance will begin to degrade
	 * as hash buckets turn into long linked lists.
	 */
	@SuppressWarnings("unchecked")
	private void ensureCapacity() {
		if (kids == null) {
			kids = new ComparisonTreeNode[2];
		} else if (((numKids + 1) * 100) > (kids.length * LOAD_FACTOR)) {
			if (kids.length < Integer.MAX_VALUE) {
				resize(kids.length + kids.length);
			}
		}
	}
	
	/**
	 * Expands the kids array to the specified length, and rehashes the child nodes 
	 * to the new kids array.
	 * 
	 * Should only be called from ensureCapacity(), and only then if the kids array is
	 * non-null.
	 */
	private void resize(int newCapacity) {
		ComparisonTreeNode<T>[] oldKids = kids;
		
		@SuppressWarnings("unchecked")
		ComparisonTreeNode<T>[] newKids = new ComparisonTreeNode[newCapacity];
		for (int i = 0, n = oldKids.length; i < n; i++) {
			ComparisonTreeNode<T> e = oldKids[i];
			if (e != null) {
				oldKids[i] = null;
				do {
					ComparisonTreeNode<T> next = e.next;
					
					// insert it at the front of the new bucket
//					int bucket = (int)(e.recordId % newCapacity);
					int bucket = getBucket(e.recordId, newCapacity);
					
					e.next = newKids[bucket];
					newKids[bucket] = e;
					
					e = next;
				} while (e != null);
			}
		}
		
		kids = newKids;
	}
	
		
	/**
	 * Factory method for creating the root of a suffix tree.
	 */
	public static <T extends Comparable<T>> ComparisonTreeNode<T> createRootNode() {
		return new ComparisonTreeNode<T>();
	}
	
	/**
	 * Subclass that's used to represent leaf nodes.  In the context of this
	 * problem, leaf nodes represent unsubsumed blocking sets, and thus have no
	 * children.  (If a node has children, it is subsumed by the blocking sets represented
	 * by each of its children, and their children, and their children...)
	 */	
	private static class LeafComparisonTreeNode<T extends Comparable<T>> extends ComparisonTreeNode<T> {
		
		private static final long serialVersionUID = 1L;
		private final int blockingSetId;
		
		public LeafComparisonTreeNode(T recordId, char type, int blockingSetId) {
			super(recordId, type);
			this.blockingSetId = blockingSetId;
			
			if (blockingSetId < 0) {
				throw new IllegalArgumentException("blockingSetId < 0: " + blockingSetId);
			}
		}
		
		public boolean hasBlockingSetId() {
			return true;
		}
		
		public int getBlockingSetId() {
			return blockingSetId;
		}
		
//		public SuffixTreeNode getChild(long childId) {
//			throw new UnsupportedOperationException("Leaf nodes have no children");
//		}
		
//		public ComparisonTreeNode putChild(long childId) {
//			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
//		}
		
//		public ComparisonTreeNode putChild(long childId, T blockingSetId) {
//			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
//		}

	}

}
