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

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;


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
 * @deprecated
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
public class BiConnectedChecker {
	
	private static final Logger log = Logger.getLogger(BiConnectedChecker.class.getName());

	
	private CompositeEntity cluster;
	private CompositeEntity matchOnly;
	private HashMap nodeInfoMap;
	private int id;
	private Stack S;
	private int numBiComp;


	protected BiConnectedChecker (CompositeEntity c) {
		this.cluster = c;
	}
	
	
	/* This creates a new CompositeEntity from cluster that only has match links.
	 * 
	 */
	private void getMatchOnlyCluster () {
		matchOnly = new CompositeEntity (cluster.getNodeId());
		List l = cluster.getAllLinks();
		for (int i=0; i<l.size(); i++) {
			Link link = (Link) l.get(i);
			List l2 = link.getLinkDefinition();
			for (int j=0; j<l2.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) l2.get(j);
				if (mr.getMatchType() == MatchRecord2.MATCH) matchOnly.addMatchRecord(mr);
			}
		}
	}
	
	
	/** This method tests to see if the whole cluster is a single bi-connected cluster consisting
	 * of only Match edges.
	 * 
	 * 
	 * @return true if this cluster is bi-connected.
	 * 
	 */
	protected boolean isBiConnectedMatchOnly () throws TransitivityException {
		if (matchOnly == null) getMatchOnlyCluster();
		
		//some special conditions
		int s = matchOnly.getChildren().size(); 
		if (s != cluster.getChildren().size()) return false;
		if (s == 2) return true;
		else if (s < 2) return false;

		//initialize values
		nodeInfoMap = new HashMap ();
		id = 1;
		S = new Stack ();
		numBiComp = 0;
		
		Iterator it = matchOnly.getChildren().iterator();
		while (it.hasNext()) {
			INode node = (INode) it.next();
			if (!nodeInfoMap.containsKey(node)) {
				DFSBiConnected2 (node, null, matchOnly);
			} 
		}
		
		log.debug ("Number of BiConnected Components " + numBiComp);
		
		if (numBiComp == 1) return true;
		else return false;
	}
	


	/** This method tests to see if the whole cluster is a single bi-connected cluster consisting
	 * of Match and Hold edges.
	 * 
	 * This is taken from 
	 * An Inside Guide To Algorithms: Their Application,
	 * Adaptation, Design, and Analysis.
	 * By Alan R Siegel and Richard Cole
	 * Section 10.1
	 * 
	 * @return true if this cluster is bi-connected.
	 * 
	 */
	protected boolean isBiConnectedMatchHold () throws TransitivityException {

		int s = cluster.getChildren().size(); 
		if (s == 2) return true;
		else if (s < 2) return false;

		//initialize values
		nodeInfoMap = new HashMap ();
		id = 1;
		S = new Stack ();
		numBiComp = 0;
		
		Iterator it = cluster.getChildren().iterator();
		while (it.hasNext()) {
			INode node = (INode) it.next();
			if (!nodeInfoMap.containsKey(node)) {
				DFSBiConnected2 (node, null, cluster);
			} 
		}
		
		log.debug ("Number of BiConnected Components " + numBiComp);
		
		if (numBiComp == 1) return true;
		else return false;
	}
	
	
	
	

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
