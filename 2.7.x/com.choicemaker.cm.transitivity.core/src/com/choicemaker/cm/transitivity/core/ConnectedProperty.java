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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This method checks to see if a graph is connected by going through the adjacency 
 * list and creating set of related (reachable) nodes. 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class ConnectedProperty implements SubGraphProperty {
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.SubGraphProperty#hasProperty(com.choicemaker.cm.transitivity.core.CompositeEntity)
	 */
	public boolean hasProperty(CompositeEntity ce) {
		List children = ce.getChildren();
		INode firstChild = (INode) children.get(0);
		
		//find out all the nodes reachable from the first node using
		//breath first search
		TreeSet reachables = new TreeSet ();
		
		//set of already visited nodes
		TreeSet visited = new TreeSet ();
		
		findReachables (firstChild, ce, reachables, visited);
		
		//writeDebug (reachables);
		//writeDebug (visited);
		
		if (reachables.size() == children.size()) return true;
		else return false;
	}


	private void writeDebug (TreeSet set) {
		System.out.println ("{");
		Iterator it = set.iterator();
		while (it.hasNext()) {
			INode node = (INode) it.next();
			System.out.println (node.getNodeId().toString());
		}
		System.out.println ("}");
	}

	
	/** This method calculates the set of reachable nodes from the input
	 * using breath first search.
	 * 
	 * @param node - the current node
	 * @param graph - the graph containing the adjacency list
	 * @param reachables - set of reachable nodes
	 * @param visited - set of already visited nodes 
	 */
	private void findReachables (INode node, CompositeEntity graph, 
		Set reachables, Set visited) {
		
		visited.add(node);
		reachables.add(node);

		List adjacentNodes = graph.getAdjacency(node);
		int s = adjacentNodes.size();
		for (int i=0; i< s; i++) {
			INode next = (INode) adjacentNodes.get(i);
			reachables.add(next);
			
			if (!visited.contains(next)) 
				findReachables (next, graph, reachables, visited);
		}
		
	}

}
