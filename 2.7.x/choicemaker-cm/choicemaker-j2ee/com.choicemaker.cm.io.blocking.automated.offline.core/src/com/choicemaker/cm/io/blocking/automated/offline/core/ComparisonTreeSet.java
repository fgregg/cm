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
import java.util.Stack;

/** This object creates a list of pairs to compare from a tree of ComparisonTreeNode.
 * 
 * @author pcheung
 *
 */
public class ComparisonTreeSet implements IComparisonSet {
	
	private ArrayList pairs = new ArrayList ();
	private int ind = 0;
	
	/** This consructor takes the root node and builds a list of pairs to compare.
	 * The root node has id = null and 1 child.
	 * 
	 * @param root
	 */
	public ComparisonTreeSet (ComparisonTreeNode root) {
		//there should only be one node
		ComparisonTreeNode kid = (ComparisonTreeNode) root.getAllChildren().get(0);
		
		Stack stack = new Stack ();
		getCompares(kid, stack);
	}
	
	
	private void getCompares (ComparisonTreeNode node, Stack stack) {
		if (!stack.empty()) {
			//compare this to everything in the stack
			for (int i=0; i<stack.size(); i++) {
				ComparisonPair p = new ComparisonPair ();
				p.id1 = (Comparable) stack.get(i);
				p.id2 = node.getRecordId();
				p.isStage = node.isStage();
				pairs.add(p);
			}
		}
		
		//push this onto the stack
		//do not allow the first id to be master, because we don't compare 
		//master to master.
		if (node.isStage()) stack.push(node.getRecordId());

		//compare all the children		
		if (node.getNumKids() > 0) {
			ArrayList children = node.getAllChildren();
			for (int i=0; i<children.size(); i++) {
				ComparisonTreeNode kid = (ComparisonTreeNode) children.get(i);
				getCompares(kid, stack);
			}
		}
			
		//pop the stack since this is done
		if (node.isStage()) stack.pop();
	}
	
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#getNextPair()
	 */
	public ComparisonPair getNextPair() {
		ComparisonPair ret = (ComparisonPair) pairs.get(ind);
		ind ++;
		return ret;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#hasNextPair()
	 */
	public boolean hasNextPair() {
		if (ind < pairs.size()) return true;
		else return false;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#writeDebug()
	 */
	public String writeDebug() {
		StringBuffer sb = new StringBuffer ();
		sb.append(Constants.LINE_SEPARATOR);
		for (int i=0; i<pairs.size(); i++) {
			ComparisonPair p = (ComparisonPair) pairs.get(i);
			sb.append('(');
			sb.append(p.id1.toString());
			sb.append(',');
			sb.append(p.id2.toString());
			sb.append(')');
		}
		sb.append(Constants.LINE_SEPARATOR);
		return sb.toString();
	}

}