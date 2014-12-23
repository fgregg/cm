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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This represents a link between two nodes on a graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
@SuppressWarnings({"rawtypes" })
public class Link<T extends Comparable<T>> {

	private INode<T> node1;
	private INode<T> node2;
	
	/* This list contains all the MatchRecord2 objects that make up this Link.
	 * 
	 */
	private List<MatchRecord2<T>> matchRecords;


	/** This constructor takes in node1, node2, and a list of MatchRecord2.
	 * 
	 * @param node1
	 * @param node2
	 * @param mrs
	 */
	public Link (INode<T> node1, INode<T> node2, List<MatchRecord2<T>> mrs) {
		this.node1 = node1;
		this.node2 = node2;
		this.matchRecords = new LinkedList<>();
		this.matchRecords.addAll(mrs);
	}
	

	/** This returns the first node of this link.
	 * 
	 * @return INode
	 */
	public INode getNode1 () {
		return node1;
	}
	
	
	/** This returns the second node of this link.
	 * 
	 * @return INode
	 */
	public INode getNode2 () {
		return node2;
	}
	
	
	/** This returns a list of MatchRecord2 that defines this link.
	 * 
	 * @return ArrayList of MatchRecord2
	 */
	public List<MatchRecord2<T>> getLinkDefinition () {
		return Collections.unmodifiableList(matchRecords);
	}

}
