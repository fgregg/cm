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
package com.choicemaker.cm.transitivity.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.GraphCompactor;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This simple graph compactor merges all nodes with the same marking into
 * a single node and related Links into a single Link.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SimpleGraphCompactor implements GraphCompactor {
	
	//this is a map of marking to compacted nodes
	private HashMap compactedNodes;
	
	//this is mapping between a pair of INodes and Link
	private HashMap compactedLinks;
	

	/** This compact method does the following:
	 * 
	 * 1.	Build a map of nodes to marking.
	 * 2.	Walk through all the links and use this logic:
	 *  A. If neither node has a marking, add this link to return graph
	 *  B. If both nodes have the same marking, then this link belongs to
	 * 		a compacted node.
	 *  C. If one node is not marked, then replace the marked node
	 * 		with a compacted node.
	 *  D. If they have different marking, then this link is between two compacted
	 * 		nodes.
	 * 
	 */
	public CompositeEntity compact(CompositeEntity ce) throws TransitivityException {
		CompositeEntity ret = new CompositeEntity 
			(ce.getNodeId());
		
		//this is a map of marking to compacted nodes
		compactedNodes = new HashMap ();
		
		compactedLinks = new HashMap ();
		
		TreeSet alreadyAdded = new TreeSet ();
		
		//walk through all the links.
		List links = ce.getAllLinks();
		for (int i=0; i<links.size(); i++) {
			Link link = (Link) links.get(i);
			INode node1 = link.getNode1();
			INode node2 = link.getNode2();
			
			Integer I1 = node1.getMarking();
			Integer I2 = node2.getMarking();
			
			if (I1 == null && I2 == null) {
				//case A
				ret.addLink(link);
			} else if (I1 == null) {
				//case C
				CompositeEntity compacted = getFromCompactedNodes (I2);
				
				CompositePair pair = new CompositePair (node1.getNodeId(), I2);
				Link compLink = (Link) compactedLinks.get(pair);
				
				Link newLink = null;
				if (compLink == null) {
					newLink = new Link (node1, compacted, link.getLinkDefinition());
					compactedLinks.put(pair, newLink);
					ret.addLink(newLink);
				} else {
					ArrayList mrs = compLink.getLinkDefinition();
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link (node1, compacted, mrs);
					compactedLinks.put(pair, newLink);
				}
				
			} else if (I2 == null) {
				//case C
				CompositeEntity compacted = getFromCompactedNodes (I1);

				CompositePair pair = new CompositePair (node2.getNodeId(), I1);
				Link compLink = (Link) compactedLinks.get(pair);

				Link newLink = null;
				if (compLink == null) {
					newLink = new Link (node2, compacted, link.getLinkDefinition());
					compactedLinks.put(pair, newLink);
					ret.addLink(newLink);
				} else {
					ArrayList mrs = compLink.getLinkDefinition();
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link (node2, compacted, mrs);
					compactedLinks.put(pair, newLink);
				}
				
			} else if (I1.equals(I2)) {
				//case B
				CompositeEntity compacted = getFromCompactedNodes (I1);
					
				compacted.addLink(link);
				
				if (!alreadyAdded.contains(I1)) {
					ret.addNode(compacted);
					alreadyAdded.add(I1);
				}
				
			} else {
				//case D
				CompositeEntity compacted1 = getFromCompactedNodes (I1);
				CompositeEntity compacted2 = getFromCompactedNodes (I2);

				CompositePair cp = null;
				if (I1.compareTo(I2) < 0) 
					cp = new CompositePair (I1, I2);
				else 
					cp = new CompositePair (I2, I1);
				Link compLink = (Link) compactedLinks.get(cp);
				
				Link newLink = null;
				if (compLink == null) {
					newLink = new Link (compacted1, compacted2, 
						link.getLinkDefinition());
					compactedLinks.put(cp, newLink);
					ret.addLink(newLink);
				} else {
					ArrayList mrs = compLink.getLinkDefinition();
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link (compacted1, compacted2, mrs);
					compactedLinks.put(cp, newLink);
					compLink = null;
				}

			} //end of the different cases
			
		}
		
		return ret;
	}
	
	
	private CompositeEntity getFromCompactedNodes (Integer I) {
		CompositeEntity compacted = 
			(CompositeEntity) compactedNodes.get(I);
					
		if (compacted == null) {
			compacted = new CompositeEntity
				(I);
				//(UniqueSequence.getInstance().getNextInteger());

			compactedNodes.put(I, compacted);
		}
		return compacted;
	}
	
	

	/** Internal object that tracks links between two INodes.
	 * 
	 * @author pcheung
	 *
	 * ChoiceMaker Technologies, Inc.
	 */
	private class CompositePair implements Comparable {
		private Comparable id1;
		private Comparable id2;
		
		private CompositePair (Comparable id1, Comparable id2) {
			this.id1 = id1;
			this.id2 = id2;
		}

		public int compareTo(Object o) {
			if (o instanceof CompositePair) {
				CompositePair cp = (CompositePair) o;
				int i1 = this.id1.compareTo(cp.id1);
				int i2 = this.id2.compareTo(cp.id2);
				
				if (i1 == 0 && i2 == 0) {
					return 0;
				} else {
					if (i1 != 0) return i1;
					else return i2;
				}
			} else return 1;
		}
		
		public boolean equals(Object o) {
			if (o instanceof CompositePair) {
				CompositePair cp = (CompositePair) o;
				return this.id1.equals(cp.id1) && this.id2.equals(cp.id2);		
			} else {
				return false;
			}
		}
		
		public int hashCode() {
			return id1.hashCode() + id2.hashCode();
		}
		
	}


}
