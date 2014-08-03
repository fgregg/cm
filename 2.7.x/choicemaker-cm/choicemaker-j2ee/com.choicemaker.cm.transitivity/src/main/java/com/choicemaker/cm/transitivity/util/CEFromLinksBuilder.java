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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object takes a List of Links
 * and creates an Iterator of CompositeEntity.
 * 
 * It goes through the links to group together nodes that are related. 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class CEFromLinksBuilder {

	/* Map of Node ID to CompositeEntity.  
	 * Many different ID's can map to the same cluster.
	 * 
	 */
	private HashMap clusterMap;
	
	//List of Link
	private List links;


	/** This constructor takes in a list Links.
	 * 
	 * @param matches
	 */
	public CEFromLinksBuilder (List links) {
		clusterMap = new HashMap();
		this.links = links;
	}


	/**
	 * This method links all the MatchRecord2 objects and returns an Iterator 
	 * of CompositeEntity.
	 * 
	 * @return Iterator - an Iterator of CompositeEntity
	 * @throws TransitivityException
	 */
	public Iterator getCompositeEntities () throws TransitivityException {
		int s = links.size();
		for (int i=0; i<s; i++) {
			Link link = (Link) links.get(i);
			linking (link);
		}
		TreeSet set =  new TreeSet (clusterMap.values());
		return set.iterator();
		
	}


	/* This method puts Node IDs c1 and c2 of Link into the same cluster.
	 * 
	 * 1.	If c1 == c2 or c1 == null, or c2 == null, it throws an exception.  This 
	 * 		should never happen.  When this happens, it means that there is something 
	 * 		wrong with IMatchRecord2Source or the OABA.
	 * 
	 * 2.	Get the clusters for these two record ID's.
	 * 	A.	If these clusters are null, then create a new cluster and map to these two ids.
	 * 	B.	If one is null, link the new id to existing cluster.
	 *  C.	If neither is null, then merge them and 
	 * 
	 * @param mr - MatchRecord2 object
	 */
	private void linking (Link link) {
		Comparable c1 = link.getNode1();
		Comparable c2 = link.getNode2();
		
		if (c1 == null || c2 == null || c1.equals(c2)) {
			throw new IllegalArgumentException();
		}
		
		CompositeEntity ent1 = (CompositeEntity) clusterMap.get(c1);
		CompositeEntity ent2 = (CompositeEntity) clusterMap.get(c2);
		
		if (ent1 == null && ent2 == null) {
			createNew (link);
		} else if (ent1 == null) {
			addToCluster (ent2, link);
		} else if (ent2 == null) {
			addToCluster (ent1, link);
		} else if (ent1 == ent2) {
			//this happens when we add an edge to an existing 
			addToCluster (ent1, link);
		} else {
			mergeClusters (ent1, ent2, link);
		}
		
	}


	/* This creates a new cluster and map them to the two nodes.
	 * 
	 */
	private void createNew (Link link) {
		Comparable c1 = link.getNode1();
		Comparable c2 = link.getNode2();

		UniqueSequence seq = UniqueSequence.getInstance ();
		CompositeEntity ce = new CompositeEntity (seq.getNextInteger());
		ce.addLink(link);

		clusterMap.put(c1, ce);		
		clusterMap.put(c2, ce);		
	}


	/* This method add a MatchRecord2 to an existing cluster.
	 * 
	 */
	private void addToCluster (CompositeEntity ent, Link link) {
		ent.addLink(link);

		Comparable c1 = link.getNode1();
		Comparable c2 = link.getNode2();

		clusterMap.put(c1, ent);
		clusterMap.put(c2, ent);
	}


	/* This method merges two clusters by takes all the MatchRecords from the second one
	 * and adding them to the first one.  Finally, it also adds the input MatchRecord2.
	 * 
	 */
	private void mergeClusters (CompositeEntity ce1, CompositeEntity ce2, 
		Link link) {
			
		//add the current to the first.
		ce1.addLink(link);
		Comparable c1 = link.getNode1();
		Comparable c2 = link.getNode2();
		clusterMap.put(c1, ce1);
		clusterMap.put(c2, ce1);
		
		//add all the links from the second to the first.
		List links = ce2.getAllLinks();
		for (int i=0; i<links.size(); i++) {
			ce1.addLink((Link) links.get(i));
		}
		
		//map all the nodes from the second to first
		List children = ce2.getChildren();
		for (int i=0; i<children.size(); i++) {
			INode node = (INode) children.get(i);
			clusterMap.put(node, ce1);
		}
		
		//free up memory
		ce2 = null;
	}


}
