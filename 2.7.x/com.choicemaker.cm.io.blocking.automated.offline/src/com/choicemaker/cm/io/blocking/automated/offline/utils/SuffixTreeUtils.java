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
import java.util.Stack;

import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;
import com.choicemaker.cm.io.blocking.automated.offline.core.SuffixTreeNode;

/**
 * @author pcheung
 *
 */
public class SuffixTreeUtils {

	/** This methods adds recordIds to the suffix tree.  This DOES NOT check if recordIds is already
	 * subsumed in the tree.  The calling code must check for that.
	 * 
	 * @param root
	 * @param recordIds
	 * @param blockSetId
	 */
	public static void addBlockSet(SuffixTreeNode root, LongArrayList recordIds, int blockSetId) {
		SuffixTreeNode cur = root;

		int last = recordIds.size () - 1;
		for (int i = 0; i < last; i++) {
			long recordId = recordIds.get(i);
			SuffixTreeNode child = cur.getChild(recordId);
			if (child == null) {
				child = cur.putChild(recordId);
			}
			cur = child;
		}
		
		// the leaf node.
		cur.putChild(recordIds.get(last), blockSetId);
	}


	/** This method returns the number of descendants of this node that are leaves.
	 * 
	 * @param root
	 * @return
	 */
	public static int countLeaves (SuffixTreeNode node) {
		if (node.hasBlockingSetId()) return 1;
		else {
			int ret = 0;
			
			if (node.getNumKids() > 0) {
				ArrayList children = node.getAllChildren();
				for (int i=0; i<children.size(); i++) {
					ret += countLeaves ((SuffixTreeNode) children.get(i));
				}
			}
			
			return ret;
		}
	}
	
	
	/** This method takes a suffix tree and returns a list of all possible pair comparisons.
	 * For example, if the tree is [1 [2 [3] [4]]], then the possible comparisons are:
	 * (1,2), (1,3), (2,3), (1,4), (2,4)
	 * 
	 * The input is a root node with id = -1 and 1 child.
	 * 
	 * @param root
	 * @return
	 */
	public static ArrayList getPairs (SuffixTreeNode root) {
		ArrayList pairs = new ArrayList ();
		
		//there should only be one node
		SuffixTreeNode kid = (SuffixTreeNode) root.getAllChildren().get(0);
		
		Stack stack = new Stack ();
		getCompares(kid, stack, pairs);
		
		return pairs;
	}


	private static void getCompares (SuffixTreeNode node, Stack stack, ArrayList pairs) {
		if (!stack.empty()) {
			//compare this to everything in the stack
			for (int i=0; i<stack.size(); i++) {
				long id1 = ((Long) stack.get(i)).longValue();
				long id2 = node.getRecordId();
				PairID p = new PairID (id1, id2);
				pairs.add(p);
			}
		}
		
		//push this onto the stack
		stack.push(new Long (node.getRecordId()));

		//compare all the children		
		if (node.getNumKids() > 0) {
			ArrayList children = node.getAllChildren();
			for (int i=0; i<children.size(); i++) {
				SuffixTreeNode kid = (SuffixTreeNode) children.get(i);
				getCompares(kid, stack, pairs);
			}
		}
			
		//pop the stack since this is done
		stack.pop();
	}

}
