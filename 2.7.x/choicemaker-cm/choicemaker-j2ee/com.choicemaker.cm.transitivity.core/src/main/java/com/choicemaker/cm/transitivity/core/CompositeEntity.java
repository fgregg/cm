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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This object represents a subgraph of records that are related to each other,
 * typically through match or hold relationships that meet some connectivity
 * criteria such as simply connected, bi-connected or fully connected.
 * 
 * @author pcheung
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class CompositeEntity<T extends Comparable<T>> implements INode<T> {

	private Integer marking;

	private T id;

	private TreeMap nodes = new TreeMap();

	// a list of edges in this graph
	private List<Link<T>> links = new LinkedList<>();

	// a mapping of node to adjacency list of INode
	private HashMap adjacencyMap = new HashMap();

	/**
	 * This constructor takes in an id.
	 */
	public CompositeEntity(T id) {
		this.id = id;
	}

	/**
	 * This method adds a MatchRecord2 to this graph.
	 */
	public void addMatchRecord(MatchRecord2<T> mr) {
		// first, add the ids to the nodes set
		T c1 = mr.getRecordID1();
		Entity<T> ent = new Entity<T>(c1, INode.STAGE_TYPE);
		Entity ent1 = (Entity) nodes.get(ent);
		if (ent1 == null) {
			nodes.put(ent, ent);
			ent1 = ent;
		}

		Comparable c2 = mr.getRecordID2();
		ent = new Entity(c2, mr.getRecord2Role().getCharSymbol());
		Entity ent2 = (Entity) nodes.get(ent);
		if (ent2 == null) {
			nodes.put(ent, ent);
			ent2 = ent;
		}

		// second, add them to each other's adjacency lists
		ArrayList l = (ArrayList) adjacencyMap.get(ent1);
		if (l == null) {
			l = new ArrayList(2);
			l.add(ent2);
			adjacencyMap.put(ent1, l);
		} else {
			l.add(ent2);
		}

		l = (ArrayList) adjacencyMap.get(ent2);
		if (l == null) {
			l = new ArrayList(2);
			l.add(ent1);
			adjacencyMap.put(ent2, l);
		} else {
			l.add(ent1);
		}

		// third, add a link
		l = new ArrayList(1);
		l.add(mr);
		Link<T> link = new Link<T>(ent1, ent2, l);
		links.add(link);

	}

	/**
	 * This method adds a node to the list of children. Adjacency list and Link
	 * are not specified.
	 * 
	 * @param node
	 *            - a new node on this graph
	 */
	public void addNode(INode node) {
		if (!nodes.containsKey(node))
			nodes.put(node, node);
	}

	/**
	 * This returns the first node (smallest node) in the tree.
	 */
	public INode getFirstNode() {
		return (INode) nodes.firstKey();
	}

	/**
	 * This method adds a Link to this graph.
	 */
	public void addLink(Link link) {
		INode node1 = link.getNode1();
		INode node2 = link.getNode2();

		if (!nodes.containsKey(node1)) {
			nodes.put(node1, node1);
		}

		if (!nodes.containsKey(node2)) {
			nodes.put(node2, node2);
		}

		// second, add them to each other's adjacency lists
		ArrayList l = (ArrayList) adjacencyMap.get(node1);
		if (l == null) {
			l = new ArrayList(2);
			l.add(node2);
			adjacencyMap.put(node1, l);
		} else {
			l.add(node2);
		}

		l = (ArrayList) adjacencyMap.get(node2);
		if (l == null) {
			l = new ArrayList(2);
			l.add(node1);
			adjacencyMap.put(node2, l);
		} else {
			l.add(node1);
		}

		// third, add a link
		links.add(link);

	}

	public T getNodeId() {
		return id;
	}

	public boolean hasChildren() {
		return true;
	}

	public List getChildren() {
		return new ArrayList(nodes.values());
	}

	/**
	 * This method returns all the links.
	 * 
	 * @return List of Link
	 */
	public List<Link<T>> getAllLinks() {
		return links;
	}

	/**
	 * This method returns the adjacency list of the give node.
	 * 
	 * @param node
	 * @return List of INode.
	 */
	public List getAdjacency(INode node) {
		return (ArrayList) adjacencyMap.get(node);
	}

	public int compareTo(INode<T> o) {
		if (o instanceof CompositeEntity) {
			CompositeEntity<T> ce = (CompositeEntity<T>) o;
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

	public void mark(Integer I) {
		marking = I;
	}

	public Integer getMarking() {
		return marking;
	}

	public char getType() {
		return INode.COMPOSIT_TYPE;
	}

}
