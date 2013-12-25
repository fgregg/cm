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

import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.EdgeProperty;
import com.choicemaker.cm.transitivity.core.Link;

/**
 * This object takes in a graph (CompositeEntity) and an EdgeProperty
 * and returns a graph satisfying that EdgeProperty.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class GraphFilter {

	private static GraphFilter filter = null;

	/**
	 * Private constructor, use getInstance instead.
	 *
	 */
	private GraphFilter () {
	}
	
	
	public static GraphFilter getInstance () {
		if (filter ==  null) filter = new GraphFilter ();
		return filter;
	}
	
	
	/** This method returns a graph where the edges statisfy the given
	 * EdgeProperty.
	 * 
	 * @param ce - input graph
	 * @param ep - property which the edges need to satisfy
	 * @return CompositeEnity - new graph.
	 */
	public CompositeEntity filter (CompositeEntity ce, EdgeProperty ep) {
		UniqueSequence seq = UniqueSequence.getInstance();
		CompositeEntity ret = new CompositeEntity (seq.getNextInteger());
		
		//get all the links
		List links = ce.getAllLinks();
		for (int i=0; i<links.size(); i++) {
			Link link = (Link) links.get(i);
			List mrs = link.getLinkDefinition();
			for (int j=0; j<mrs.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) mrs.get(j);
				
				//add the matchs that meets the property to the return object
				if (ep.hasProperty(mr)) ret.addMatchRecord(mr);
			}
		}
		
		return ret;
	}

}
