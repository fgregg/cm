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
import java.util.List;
import java.util.Stack;

import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 */
public class SuffixTreeUtils {
	
	public static class IndexedID<T extends Comparable<? super T>> {
		public final T id;
		public final long index;
		public IndexedID(T id, long index) {
			this.id = id;
			this.index = index;
		}
	}

	/**
	 * This methods adds recordIds to the suffix tree. This DOES NOT check if
	 * recordIds is already subsumed in the tree. The calling code must check
	 * for that.
	 * 
	 * @param root
	 * @param recordIds
	 * @param blockSetId
	 */
	public static <T extends Comparable<? super T>> void addBlockSet(SuffixTreeNode<T> root,
			LongArrayList recordIds, int blockSetId) {
		SuffixTreeNode<T> cur = root;

		int last = recordIds.size() - 1;
		for (int i = 0; i < last; i++) {
			long recordId = recordIds.get(i);
			SuffixTreeNode<T> child = cur.getChild(recordId);
			if (child == null) {
				child = cur.putChild(recordId);
			}
			cur = child;
		}

		// the leaf node.
		cur.putChild(recordIds.get(last), blockSetId);
	}

	/**
	 * This method returns the number of descendants of this node that are
	 * leaves.
	 * 
	 * @param root
	 * @return
	 */
	public static <T extends Comparable<? super T>> int countLeaves(SuffixTreeNode<T> node) {
		if (node.hasBlockingSetId())
			return 1;
		else {
			int ret = 0;

			if (node.getNumKids() > 0) {
				List<SuffixTreeNode<T>> children = node.getAllChildren();
				for (int i = 0; i < children.size(); i++) {
					ret += countLeaves(children.get(i));
				}
			}

			return ret;
		}
	}

	/**
	 * This method takes a suffix tree and returns a list of all possible pair
	 * comparisons. For example, if the tree is [1 [2 [3] [4]]], then the
	 * possible comparisons are: (1,2), (1,3), (2,3), (1,4), (2,4)
	 * 
	 * The input is a root node with id = -1 and 1 child.
	 * 
	 * @param root
	 * @return
	 */
	public static <T extends Comparable<? super T>> List<PairID<T>> getPairs(
			SuffixTreeNode root) {
		List<IndexedID<T>> pairs = new ArrayList<>();

		// there should only be one node
		final List<SuffixTreeNode> kids = root.getAllChildren();
		final int count = kids.size();
		if (count != 1) {
			throw new IllegalArgumentException("Root has too many children: "
					+ count);
		}
		SuffixTreeNode kid = (SuffixTreeNode) root.getAllChildren().get(0);

		Stack<T> stack = new Stack<>();
		getCompares(kid, stack, pairs);

		return pairs;
	}

	private static <T extends Comparable<? super T>> void getCompares(
			SuffixTreeNode node, Stack<T> stack, List<IndexedID<T>> pairs) {
		if (!stack.empty()) {
			// compare this to everything in the stack
			for (int i = 0; i < stack.size(); i++) {
				T id1 = stack.get(i);
				long id2 = node.getRecordId();
				IndexedID<T> p = new IndexedID<T>(id1, id2);
				pairs.add(p);
			}
		}

		// push this onto the stack
		stack.push(new Long(node.getRecordId()));

		// compare all the children
		if (node.getNumKids() > 0) {
			ArrayList children = node.getAllChildren();
			for (int i = 0; i < children.size(); i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				getCompares(kid, stack, pairs);
			}
		}

		// pop the stack since this is done
		stack.pop();
	}

}
