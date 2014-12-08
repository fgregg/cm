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
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.EdgeProperty;
import com.choicemaker.cm.transitivity.core.Entity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.SubGraphProperty;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object marks the input graph, indicting which nodes should be compacted.
 * The nodes that are marked belong to subgraphs created from the edges that satisfy 
 * the edge property, which satsify the sub graph property. 
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GraphAnalyzer {
	
	private CompositeEntity graph;
	private EdgeProperty ep;
	private SubGraphProperty sgp;
	
	
	/** This constructor takes in a graph, edge property, and sub graph property.
	 * 
	 * @param graph - a graph
	 * @param ep - EdgeProperty
	 * @param sgp - SubGraphProperty
	 */
	public GraphAnalyzer (CompositeEntity graph, EdgeProperty ep, 
		SubGraphProperty sgp) {
			
		this.graph = graph;
		this.ep = ep;
		this.sgp = sgp;
	}


	/** This method marks the input graph.  It does the following:
	 * 
	 * 1.	Create subgraphs from edges that satisfy the EdgeProperty.
	 * 2.	Check to see if the graphs in step 1 satisfy the SubGraphProperty.
	 * 3.	Mark the sub graph in step 2 with an Integer.
	 *
	 */
	public void analyze () throws TransitivityException {
		//list of edges that meet the edge property
		List edges = getEdges ();
		
		//hashmap of node and marking
		HashMap markings = getRelatedNodes (edges);
		
		//free up mem
		edges = null;

		markOriginalGraph (markings);
	}
	
	
	/** This method returns all edges in the graph where the edge property is met.
	 * 
	 * @return List of MatchRecord2
	 */
	private List getEdges () {
		//array of edges that meet the edge property
		ArrayList edges = new ArrayList ();

		List links = graph.getAllLinks();
		int s = links.size();

		//find all edges that satisfy this property.		
		for (int i=0; i<s; i++) {
			Link link = (Link) links.get(i);
			List defs = link.getLinkDefinition();
			for (int j=0; j<defs.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) defs.get(j);
				if (ep.hasProperty(mr)) edges.add(mr);
			}
		}
		return edges;		
	}
	
	
	/** This method returns a map of node id and marking id.  If two nodes
	 * have the same marking id then they belong to the same sub graph.
	 * 
	 * @return HashMap of node to marking
	 */
	private HashMap getRelatedNodes (List edges) throws TransitivityException {
		//hashmap of node and marking
		HashMap markings = new HashMap ();

		//get the list of sub graphs back and check for sub graph property
		CompositeEntityBuilder ceb = new CompositeEntityBuilder (edges);
		
		Iterator it = ceb.getCompositeEntities();
		while (it.hasNext()) {
			CompositeEntity ce = (CompositeEntity) it.next();
			
			if (sgp.hasProperty(ce)) {
				//mark the nodes
				Integer I = UniqueSequence.getInstance().getNextInteger();
				List children = ce.getChildren();
				for (int i=0; i<children.size(); i++) {
					INode node = (INode) children.get(i);
					markings.put(node, I);
				}
			}
		}
		
		return markings;	
	}
	
	
	/** This method marks the original graph with the marking ids in the map.
	 * 
	 * @param markings
	 */
	private void markOriginalGraph (HashMap markings) {
		//mark the original graph
		List children = graph.getChildren();
		for (int i=0; i<children.size(); i++) {
			INode node = (INode) children.get(i);
			
			if (node instanceof Entity) {
				Integer I = (Integer) markings.get(node);
				if (I != null) node.mark(I);
			} else if (node instanceof CompositeEntity) {
				List children2 = node.getChildren();
				boolean found = false;
				int i2 = 0;
				int s2 = children2.size();
				while (!found && (i2< s2)) {
					INode node2 = (INode) children2.get(i2);
					Integer I = (Integer) markings.get(node2);
					if (I != null) {
						found = true;
						node.mark(I);
					}
					i2 ++;
				}
			}
		}
	}

}
