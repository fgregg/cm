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

import java.util.ArrayList;

import com.choicemaker.util.LongArrayList;

/**
 * Represents a node in a suffix tree, specifically designed from elimination
 * of blocking sets.
 * 
 * This class is not thread-safe in the least.
 * 
 * TODO: create a subclass of SuffixTreeNode for non-leaf nodes.  Then can move all
 * the putChild, getChild, removeChild, etc. funcationality (as well as numKids and kids
 * instance variables) in that class.
 * 
 * @author Adam Winkel
 */

public class SuffixTreeNode implements IIDSet{

	/**
	 * We hardcode the effective load factor as .75f, and use the the value 
	 * of numKids * 100 as the threshold for expansion.
	 */
	public static final int LOAD_FACTOR = 75;

	/**
	 * The id of the record this SuffixTreeNode represents.
	 */
	private final long recordId;
	
	/**
	 * A pointer to this node's parent node.
	 * This will be null in the case of the root node or if this node
	 * has been removed from its parent.
	 */
	private final SuffixTreeNode parent;

	/**
	 * The next node in the linked list that this SuffixTreeNode
	 * is in its parent.
	 */
	private SuffixTreeNode next = null;

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
	private SuffixTreeNode[] kids = null;

	/**
	 * This constructor is only be called by createRootNode();
	 */
	private SuffixTreeNode() { 
		this.parent = null;
		this.recordId = -1;
	}

	/**
	 * Creates a new SuffixTreeNode with the specified parent and recordId.
	 */
	private SuffixTreeNode(SuffixTreeNode parent, long recordId) {
		this.parent = parent;
		this.recordId = recordId;
		
		if (parent == null || recordId == -1) {
			throw new IllegalArgumentException(Long.toString(recordId));
		}
	}

	/**
	 * Returns true iff this SuffixTreeNode has a valid blockingSetId.  That is,
	 * this node represents the end of a blocking set.
	 */		
	public boolean hasBlockingSetId() {
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
	
	
	/** PC 4/5/05
	 *  
	 * This returns all the children of this node.
	 * 
	 * @return ArrayList
	 */ 
	public ArrayList getAllChildren () {
		ArrayList children = null;
		if (numKids > 0) {
			children = new ArrayList ();
			for (int i=0; i< kids.length; i++) {
				if (kids[i] != null) {
					SuffixTreeNode kid = kids[i];
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
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet#getRecordIDs()
	 */
	public LongArrayList getRecordIDs() {
		LongArrayList list = new LongArrayList (numKids + 1);
		list.add(recordId);
		
		if (numKids > 0) {
			ArrayList children = getAllChildren();
			for (int i=0; i<numKids; i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				list.addAll( kid.getRecordIDs());
			}
		}
		 
		return list;
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
	public String writeSuffixTree (String prefix) {
		StringBuffer sb = new StringBuffer (prefix);
		sb.append(recordId);
		
		if (numKids == 0) {
			sb.append(Constants.LINE_SEPARATOR);
			return sb.toString();
		} else {
			ArrayList children = getAllChildren();
			sb.append(' ');
			String temp = sb.toString();
			sb = new StringBuffer ();
			for (int i=0; i<children.size(); i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				sb.append( kid.writeSuffixTree(temp) );
			}
			
			return sb.toString();
		}
	}


	/**
	 *  This returns a string representation of the tree where each node is surrounded by [ and ].
	 * For example: [2[4[6[8]][7]]].  Note that 4 has two children: 6 and 7.
	 * 
	 * @param sb
	 */
	public void writeSuffixTree2 (StringBuffer sb) {
		sb.append(Constants.OPEN_NODE);
		sb.append(recordId);
		
		if (numKids > 0) {
			ArrayList children = getAllChildren();
			for (int i=0; i<children.size(); i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				kid.writeSuffixTree2(sb);
			}
		}

		sb.append(Constants.CLOSE_NODE);
	}

/*
	public String writeSuffixTree2 (String prefix) {
		StringBuffer sb = new StringBuffer (prefix);
		sb.append(Constants.OPEN_NODE);
		sb.append(recordId);
		
		if (numKids > 0) {
			ArrayList children = getAllChildren();
			for (int i=0; i<children.size(); i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				sb.append( kid.writeSuffixTree2("") );
			}
		}

		sb.append(Constants.CLOSE_NODE);
		return sb.toString();
	}
*/

	/**
	 * Returns the child of this node with the specified record ID, or
	 * null if this node has no such child.
	 */		
	public SuffixTreeNode getChild(long childId) {
		if (kids == null) {
			return null;
		} else {
//			int bucket = (int)(childId % kids.length);
			int bucket = getBucket(childId, kids.length);
			
			SuffixTreeNode e = kids[bucket];
			while (e != null && e.recordId != childId) {
				e = e.next;
			}
	
			return e;
		}
	}
	
	
	private int getBucket (long id, int length) {
		int ret = 0;
		int i = (int) id;
//		if (id > 0) ret = i % length;
//		else ret = -i % length;

		ret = i % length;
		if (ret < 0) ret = -ret;
		
		return ret;
	}
	
	public long getRecordId () {
		return recordId;
	}
	
	
	public SuffixTreeNode putChild(long childId) {
		return putChildImpl(new SuffixTreeNode(this, childId));
	}
	
	public SuffixTreeNode putChild(long childId, int blockingSetId) {
		return putChildImpl(new LeafSuffixTreeNode(this, childId, blockingSetId));
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
	private SuffixTreeNode putChildImpl(SuffixTreeNode kid) {
		long childId = kid.recordId;

		ensureCapacity();
//		int bucket = (int) (childId % kids.length);
		int bucket = getBucket(childId,kids.length);

		// check that no child node with the given ID previously existed.
		SuffixTreeNode node = kids[bucket];
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
	private void ensureCapacity() {
		if (kids == null) {
			kids = new SuffixTreeNode[2];
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
		SuffixTreeNode[] oldKids = kids;
		
		SuffixTreeNode[] newKids = new SuffixTreeNode[newCapacity];
		for (int i = 0, n = oldKids.length; i < n; i++) {
			SuffixTreeNode e = oldKids[i];
			if (e != null) {
				oldKids[i] = null;
				do {
					SuffixTreeNode next = e.next;
					
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
	 * Removes a leaf node from it's parent node, and the parent node from
	 * its parent, until we run into a parent that has more than 1 child.
	 * 
	 * If this node doesn't have a parent, an IllegalStateException will be thrown. 
	 * Similarly, if this node has some non-zero number of kids, an illegal
	 * 
	 */
	public void removeFromParentRecursive() {
		if (!isRemovableFromParent()) {
			throw new IllegalStateException("This node is not removable from its parent!");
		}
		
		SuffixTreeNode p = parent;
		p.removeChild(this);
		
		if (p.isRemovableFromParent()) {
			p.removeFromParentRecursive();
		}
	}

	/**
	 * A node is removable from its parent if its parent exists (is not null),
	 * and it has no kids.
	 */
	private boolean isRemovableFromParent() {
		return parent != null && numKids == 0;
	}
	
	/**
	 * Removes <code>child</code> from this node.  If <code>child</code> is 
	 * not a child of this node, bad things will happen.
	 * 
	 * Public methods calling this method (presently only removeFromParentRecursive)
	 * must ensure that this method is never called with an argument that is
	 * NOT a child of this node.
	 */
	private void removeChild(SuffixTreeNode child) {
//		int bucket = (int)(child.recordId % kids.length);
		int bucket = getBucket (child.recordId, kids.length);
		
		SuffixTreeNode prev = kids[bucket];
		SuffixTreeNode e = prev;
		
		for (;;) {
			SuffixTreeNode next = e.next;
			if (e == child) {
				if (prev == e) {
					kids[bucket] = next;
				} else {
					prev.next = next;
				}				
				numKids--;
				return;
			}
			
			prev = e;
			e = next;
		}
	}
		
	/**
	 * Factory method for creating the root of a suffix tree.
	 */
	public static SuffixTreeNode createRootNode() {
		return new SuffixTreeNode();
	}
	
	/**
	 * Subclass that's used to represent leaf nodes.  In the context of this
	 * problem, leaf nodes represent unsubsumed blocking sets, and thus have no
	 * children.  (If a node has children, it is subsumed by the blocking sets represented
	 * by each of its children, and their children, and their children...)
	 */	
	private static class LeafSuffixTreeNode extends SuffixTreeNode {
		
		private final int blockingSetId;
		
		public LeafSuffixTreeNode(SuffixTreeNode parent, long recordId, int blockingSetId) {
			super(parent, recordId);
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
		
		public SuffixTreeNode getChild(long childId) {
			throw new UnsupportedOperationException("Leaf nodes have no children");
		}
		
		public SuffixTreeNode putChild(long childId) {
			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
		}
		
		public SuffixTreeNode putChild(long childId, int blockingSetId) {
			throw new UnsupportedOperationException("Attempt to add a child to a leaf node");
		}
	}

}
