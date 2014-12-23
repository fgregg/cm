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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.GraphCompactor;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This simple graph compactor merges all nodes with the same marking into a
 * single node and related Links into a single Link.
 * 
 * @author pcheung
 *
 *         ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({
	"rawtypes", "unchecked" })
public class SimpleGraphCompactor<T extends Comparable<T>> implements
		GraphCompactor<T> {

	// this is a map of marking to compacted nodes
	private HashMap<Integer, CompositeEntity<T>> compactedNodes;

	// this is mapping between a pair of INodes and Link
	private HashMap<CompositePair<T>, Link<T>> compactedLinks;

	/**
	 * This compact method does the following:
	 * 
	 * 1. Build a map of nodes to marking. 2. Walk through all the links and use
	 * this logic: A. If neither node has a marking, add this link to return
	 * graph B. If both nodes have the same marking, then this link belongs to a
	 * compacted node. C. If one node is not marked, then replace the marked
	 * node with a compacted node. D. If they have different marking, then this
	 * link is between two compacted nodes.
	 * 
	 */
	public CompositeEntity<T> compact(CompositeEntity<T> ce)
			throws TransitivityException {
		CompositeEntity<T> ret = new CompositeEntity<T>(ce.getNodeId());

		// this is a map of marking to compacted nodes
		compactedNodes = new HashMap<>();

		compactedLinks = new HashMap<>();

		TreeSet<Integer> alreadyAdded = new TreeSet<>();

		// walk through all the links.
		List<Link<T>> links = ce.getAllLinks();
		for (int i = 0; i < links.size(); i++) {
			Link<T> link = links.get(i);
			INode node1 = link.getNode1();
			INode node2 = link.getNode2();

			Integer I1 = node1.getMarking();
			Integer I2 = node2.getMarking();

			if (I1 == null && I2 == null) {
				// case A
				ret.addLink(link);
			} else if (I1 == null) {
				// case C
				CompositeEntity compacted = getFromCompactedNodes(I2);

				CompositePair pair = new CompositePair(node1.getNodeId(), I2);
				Link compLink = (Link) compactedLinks.get(pair);

				Link newLink = null;
				if (compLink == null) {
					newLink =
						new Link(node1, compacted, link.getLinkDefinition());
					compactedLinks.put(pair, newLink);
					ret.addLink(newLink);
				} else {
					List<MatchRecord2<T>> mrs = new LinkedList<>();
					mrs.addAll(compLink.getLinkDefinition());
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link(node1, compacted, mrs);
					compactedLinks.put(pair, newLink);
				}

			} else if (I2 == null) {
				// case C
				CompositeEntity compacted = getFromCompactedNodes(I1);

				CompositePair pair = new CompositePair(node2.getNodeId(), I1);
				Link compLink = (Link) compactedLinks.get(pair);

				Link newLink = null;
				if (compLink == null) {
					newLink =
						new Link(node2, compacted, link.getLinkDefinition());
					compactedLinks.put(pair, newLink);
					ret.addLink(newLink);
				} else {
					List<MatchRecord2<T>> mrs = new LinkedList<>();
					mrs.addAll(compLink.getLinkDefinition());
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link(node2, compacted, mrs);
					compactedLinks.put(pair, newLink);
				}

			} else if (I1.equals(I2)) {
				// case B
				CompositeEntity compacted = getFromCompactedNodes(I1);

				compacted.addLink(link);

				if (!alreadyAdded.contains(I1)) {
					ret.addNode(compacted);
					alreadyAdded.add(I1);
				}

			} else {
				// case D
				CompositeEntity compacted1 = getFromCompactedNodes(I1);
				CompositeEntity compacted2 = getFromCompactedNodes(I2);

				CompositePair cp = null;
				if (I1.compareTo(I2) < 0)
					cp = new CompositePair(I1, I2);
				else
					cp = new CompositePair(I2, I1);
				Link compLink = (Link) compactedLinks.get(cp);

				Link newLink = null;
				if (compLink == null) {
					newLink =
						new Link(compacted1, compacted2,
								link.getLinkDefinition());
					compactedLinks.put(cp, newLink);
					ret.addLink(newLink);
				} else {
					List<MatchRecord2<T>> mrs = new LinkedList<>();
					mrs.addAll(compLink.getLinkDefinition());
					mrs.addAll(link.getLinkDefinition());
					newLink = new Link(compacted1, compacted2, mrs);
					compactedLinks.put(cp, newLink);
					compLink = null;
				}

			} // end of the different cases

		}

		return ret;
	}

	private CompositeEntity getFromCompactedNodes(Integer I) {
		CompositeEntity compacted = (CompositeEntity) compactedNodes.get(I);

		if (compacted == null) {
			compacted = new CompositeEntity(I);
			// (UniqueSequence.getInstance().getNextInteger());

			compactedNodes.put(I, compacted);
		}
		return compacted;
	}

	/**
	 * Internal object that tracks links between two INodes.
	 * 
	 * @author pcheung
	 *
	 *         ChoiceMaker Technologies, Inc.
	 */
	private class CompositePair<E extends Comparable<E>> implements
			Comparable<CompositePair<E>> {

		private E id1;
		private E id2;

		private CompositePair(E id1, E id2) {
			this.id1 = id1;
			this.id2 = id2;
		}

		public int compareTo(CompositePair<E> cp) {
			int i1 = this.id1.compareTo(cp.id1);
			int i2 = this.id2.compareTo(cp.id2);

			if (i1 == 0 && i2 == 0) {
				return 0;
			} else if (i1 != 0) {
				return i1;
			} else {
				return i2;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((id1 == null) ? 0 : id1.hashCode());
			result = prime * result + ((id2 == null) ? 0 : id2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CompositePair other = (CompositePair) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (id1 == null) {
				if (other.id1 != null) {
					return false;
				}
			} else if (!id1.equals(other.id1)) {
				return false;
			}
			if (id2 == null) {
				if (other.id2 != null) {
					return false;
				}
			} else if (!id2.equals(other.id2)) {
				return false;
			}
			return true;
		}

		private SimpleGraphCompactor getOuterType() {
			return SimpleGraphCompactor.this;
		}

		@Override
		public String toString() {
			return "CompositePair [id1=" + id1 + ", id2=" + id2 + "]";
		}

	}

}
