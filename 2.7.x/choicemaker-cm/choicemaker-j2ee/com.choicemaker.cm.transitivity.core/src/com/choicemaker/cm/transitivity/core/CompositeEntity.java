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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object represents a graph of records that are related to each other.
 * It is common to have a CompositeEntity to contain other CompositeEntity, because
 * a graph can be broken down into different connected components.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
public class CompositeEntity implements INode {
	
	private Integer marking;
	
	private Comparable id;
	
	//a list of INode
	// private TreeSet nodes = new TreeSet();
	// we can't use a TreeSet, because it doesn't support the get operation.

	private TreeMap nodes = new TreeMap ();
	
	//a list of edges in this graph
	private ArrayList links = new ArrayList (5);
	
	//a mapping of node to adjacency list of INode
	private HashMap adjacencyMap = new HashMap ();
	
//	private HashMap propertiesMap;
	
	
	/** This constructor takes in an id.
	 * 
	 * @param id
	 */
	public CompositeEntity (Comparable id) {
		this.id = id;
	}
	
	
	/** This method adds a MatchRecord2 to this graph.
	 * 
	 * @param mr
	 */
	public void addMatchRecord (MatchRecord2 mr) {
		//first, add the ids to the nodes set
		Comparable c1 = mr.getRecordID1();
		Entity ent = new Entity (c1, INode.STAGE_TYPE);
		Entity ent1 = (Entity) nodes.get(ent);
		if (ent1 == null) {
			nodes.put(ent,ent);
			ent1 = ent;
		}
		
		Comparable c2 = mr.getRecordID2();
		ent = new Entity (c2, mr.getRecord2Source());
		Entity ent2 = (Entity) nodes.get(ent);
		if (ent2 == null) {
			nodes.put(ent,ent);
			ent2 = ent;
		}
		
		//second, add them to each other's adjacency lists
		ArrayList l = (ArrayList) adjacencyMap.get(ent1);
		if (l == null) {
			l = new ArrayList (2);
			l.add(ent2);
			adjacencyMap.put(ent1, l);
		} else {
			l.add(ent2);
		}
		
		l = (ArrayList) adjacencyMap.get(ent2);
		if (l == null) {
			l = new ArrayList (2);
			l.add(ent1);
			adjacencyMap.put(ent2, l);
		} else {
			l.add(ent1);
		}
		
		//third, add a link
		l = new ArrayList (1);
		l.add(mr);
		Link link = new Link (ent1, ent2, l);
		links.add(link);
		
	}
	
	
	/** This method adds a node to the list of children.  Adjacency list and
	 * Link are not specified.
	 * 
	 * @param node - a new node on this graph
	 */
	public void addNode (INode node) {
		if (!nodes.containsKey(node)) nodes.put(node, node);
	}
	
	
	/** This returns the first node (smallest node) in the tree.
	 * 
	 * @return
	 */
	public INode getFirstNode () {
		return (INode) nodes.firstKey();
	}
	
	
	/** This method adds a Link to this graph.
	 * 
	 * @param link
	 */
	public void addLink (Link link) {
		INode node1 = link.getNode1();
		INode node2 = link.getNode2();

		//first, add the ids to the nodes set
		//if (!nodes.contains(node1)) nodes.add(node1);
		//if (!nodes.contains(node2)) nodes.add(node2);
		
		if (!nodes.containsKey(node1)) {
			nodes.put(node1, node1);
		}
		 
		if (!nodes.containsKey(node2)) {
			nodes.put(node2, node2);
		} 

		//second, add them to each other's adjacency lists
		ArrayList l = (ArrayList) adjacencyMap.get(node1);
		if (l == null) {
			l = new ArrayList (2);
			l.add(node2);
			adjacencyMap.put(node1, l);
		} else {
			l.add(node2);
		}
		
		l = (ArrayList) adjacencyMap.get(node2);
		if (l == null) {
			l = new ArrayList (2);
			l.add(node1);
			adjacencyMap.put(node2, l);
		} else {
			l.add(node1);
		}

		//third, add a link
		links.add(link);

	}
	
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getNodeId()
	 */
	public Comparable getNodeId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#hasChildren()
	 */
	public boolean hasChildren() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getChildren()
	 */
	public List getChildren() {
		return new ArrayList (nodes.values());
	}
	
	
	/** This method returns all the links.
	 * 
	 * @return List of Link
	 */
	public List getAllLinks () {
		return links;
	}
	
	
	/** This method returns the adjacency list of the give node.
	 * 
	 * @param node
	 * @return List of INode.
	 */
	public List getAdjacency (INode node) {
		return (ArrayList) adjacencyMap.get(node);
	}
	
	
	/* This method checks to see if the graph defined by this CompositeEntity has
	 * the given property.
	 * The property is cache in the object so that future calls don't require additional
	 * processing.
	 * 
	 * @param property
	 * @return boolean
	 */
/*
	public boolean hasProperty (LinkageProperty property) throws TransitivityException {
		if (propertiesMap == null) {
			propertiesMap = new HashMap ();
		}
		Boolean B = (Boolean) propertiesMap.get(property.getId());
		if (B == null) {
			B = new Boolean ( property.visit(this) );
			propertiesMap.put(property.getId(), B);
		}
		
		return B.booleanValue(); 
	}
*/


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof CompositeEntity) {
			CompositeEntity ce = (CompositeEntity) o;
			return this.id.compareTo(ce.id);
		} else {
			return -1;
		}
	}

	public boolean equals(Object o) {
		if (o instanceof CompositeEntity) {
			CompositeEntity ce = (CompositeEntity) o;
			return this.id.equals(ce.id);		
		} else {
			return false;
		}
	}

	public int hashCode() {
		return id.hashCode();
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#mark(java.lang.Integer)
	 */
	public void mark(Integer I) {
		marking = I;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getMarking()
	 */
	public Integer getMarking() {
		return marking;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.INode#getType()
	 */
	public char getType() {
		return INode.COMPOSIT_TYPE;
	}

}
