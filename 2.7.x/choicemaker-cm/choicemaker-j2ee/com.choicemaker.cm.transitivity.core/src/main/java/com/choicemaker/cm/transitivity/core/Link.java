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

/**
 * This represents a link between two nodes on a graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies Inc.
 */
@SuppressWarnings({"rawtypes" })
public class Link {

	private INode node1;
	private INode node2;
	
	/* This list contains all the MatchRecord2 objects that make up this Link.
	 * 
	 */
	private ArrayList matchRecords = new ArrayList ();


	/** This constructor takes in node1, node2, and a list of MatchRecord2.
	 * 
	 * @param node1
	 * @param node2
	 * @param mrs
	 */
	public Link (INode node1, INode node2, ArrayList mrs) {
		this.node1 = node1;
		this.node2 = node2;
		this.matchRecords = mrs;
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
	public ArrayList getLinkDefinition () {
		return matchRecords;
	}

}
