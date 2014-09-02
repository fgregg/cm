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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * This checks to see if the input graph is a biconnected component.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class BiConnectedProperty implements SubGraphProperty {

	private static final Logger log = Logger.getLogger(BiConnectedProperty.class.getName());


	//this keep track of node information during the DFS search. 
	private HashMap nodeInfoMap;

	//this id is used to assign id to nodes during the DFS search 
	private int id;
	
	//this is the number of bi-connected components.
	private int numBiComp;
	
	private Stack S;

	
	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.SubGraphProperty#hasProperty(com.choicemaker.cm.transitivity.core.CompositeEntity)
	 */
	public boolean hasProperty(CompositeEntity cluster) {
		//initialize values
		nodeInfoMap = new HashMap ();
		id = 1;
		S = new Stack ();
		numBiComp = 0;
		
		try {
			Iterator it = cluster.getChildren().iterator();
			while (it.hasNext()) {
				INode node = (INode) it.next();
				if (!nodeInfoMap.containsKey(node)) {
					DFSBiConnected2 (node, null, cluster);
				} 
			}
		} catch (TransitivityException e) {
			log.error(e,e);
			return false;
		}
		
		log.debug ("Number of BiConnected Components " + numBiComp);
		
		if (numBiComp == 1) return true;
		else return false;
	}


	/**
 	 * This object uses a Depth First Search algorithm to determine if a graph is
 	 * bi-connected.
 	 * 
 	 * This is taken from 
 	 * An Inside Guide To Algorithms: Their Application,
 	 * Adaptation, Design, and Analysis.
 	 * By Alan R Siegel and Richard Cole
	 * Section 10.1
	 * 
	 * @param node - the current node that it is visiting
	 * @param parent - the parent of this current node
	 * @param c
	 * @throws TransitivityException
	 */
	private void DFSBiConnected2 (INode node, INode parent, CompositeEntity c)
		throws TransitivityException {
		
		List reachables = c.getAdjacency (node);
		if (reachables == null || reachables.size() == 0) 
			throw new TransitivityException ("No edges for " + node.toString());
			
		NodeInfo v = (NodeInfo) nodeInfoMap.get (node);
		if (v == null) {
			v = new NodeInfo ();
//			v.visited = true;
			v.pre = id++;
			v.low = v.pre;
			
			nodeInfoMap.put(node, v);
		}
			
		int size = reachables.size();
		for (int i=0; i< size; i++) {
			INode next = (INode) reachables.get(i);
			
			//not parent
			if (parent == null || !next.equals(parent)) {

				NodeInfo w = (NodeInfo) nodeInfoMap.get (next);
				
				if (w == null) {
					//not visited
					S.push(new Pair(node, next));
					
					DFSBiConnected2 (next, node, c);
					
					w = (NodeInfo) nodeInfoMap.get (next);
					if (w.low >= v.pre) {
						numBiComp ++;
						log.debug ("New BiConnected Component");
						
						boolean stop = false;
						while (!stop) {
							Pair p = (Pair) S.pop();
							log.debug (p.v.getNodeId() + " " + p.w.getNodeId());
							
							if (p.v.equals(node)) stop = true;
						}
						log.debug ("End BiConnected Component");
					}
					
					v.low = Math.min(v.low, w.low);
				} else {
					if (w.pre < v.pre) {
						//back edge
						S.push(new Pair (node, next));
						v.low = Math.min(v.low, w.pre);
					} 
				}
			}
		} //end for
	}


	private class NodeInfo {
//		/**
//		 *  This is true if the node has been visited.
//		*/
//		private boolean visited;
	
		/**
		* The id of the node, aka, pre
		*/
		private int pre;
	
		/**
		* The minimum reachable id from this node, aka, low.
		*/
		private int low;
	}
	

	private class Pair {
		
		Pair (INode v, INode w) {
			this.v = v;
			this.w = w;
		}
		
		private INode v;
		private INode w;
	}



}
