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
package com.choicemaker.cm.transitivity.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * This checks to see if the input graph is a simple connected graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class SimpleConnectedProperty implements SubGraphProperty {

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.SubGraphProperty#hasProperty(com.choicemaker.cm.transitivity.core.CompositeEntity)
	 */
	public boolean hasProperty(CompositeEntity ce) {
		int numChildren = ce.getChildren().size();
		INode fNode = ce.getFirstNode();
		Set seenNodes = new HashSet ();

		getAllAccessibleNodes (ce, seenNodes, fNode);
		
		//System.out.println (numChildren + " " + seenNodes.size());

		if (numChildren == seenNodes.size()) return true;
		else return false;
	}
	
	
	/** This method recursively collects all nodes reachable from the given node.
	 * 
	 * @param ce
	 * @param seenNodes
	 * @param currentNode
	 */
	private void getAllAccessibleNodes (CompositeEntity ce, Set seenNodes, INode currentNode) {
		if (!seenNodes.contains(currentNode)) {
			seenNodes.add(currentNode);
					
			List al = ce.getAdjacency(currentNode);
			for (int i=0; i<al.size(); i++) {
				INode node = (INode) al.get(i);
				
				getAllAccessibleNodes (ce, seenNodes, node);
			}
		}
	}
	

}
