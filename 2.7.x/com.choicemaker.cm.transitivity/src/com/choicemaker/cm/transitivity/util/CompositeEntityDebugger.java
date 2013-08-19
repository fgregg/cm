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
import java.util.List;

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.CompositeEntity;
import com.choicemaker.cm.transitivity.core.INode;
import com.choicemaker.cm.transitivity.core.Link;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object contains debugging code that writes out CompositeEntity information
 * to System.out.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class CompositeEntityDebugger {
	
	
	/** This method writes the content of the input CompositeEntity out to System.out.
	 * It is used for debugging purposes.
	 * 
	 * @param ce
	 * @throws TransitivityException
	 */
	public static void writeCompositeEntity (CompositeEntity ce) throws TransitivityException {
		System.out.println ("<-- " + ce.getNodeId());
		
		ArrayList ces = new ArrayList ();

		StringBuffer sb = new StringBuffer ("nodes: ");
		List children = ce.getChildren();
		for (int i=0; i<children.size(); i++) {
			INode node = (INode) children.get(i);
			sb.append(node.getNodeId().toString());
			sb.append(" (");
			
			Integer I = node.getMarking();
			if (I != null) sb.append(I.toString());
			
			sb.append(") ");
			if (node instanceof CompositeEntity) ces.add(node);
		}
		System.out.println (sb.toString());
		List links = ce.getAllLinks();
		for (int i=0; i<links.size(); i++) {
			Link link = (Link) links.get(i);
			System.out.println ("link: " + link.getNode1().getNodeId() + " (" +
				link.getNode1().getMarking() + ") " +
				link.getNode2().getNodeId() + " (" + link.getNode2().getMarking() +
				")");
				
			List mrs = link.getLinkDefinition();
			for (int j=0; j<mrs.size(); j++) {
				MatchRecord2 mr = (MatchRecord2) mrs.get(j);
				System.out.println (mr.getRecordID1() + " " + mr.getRecordID2() +
					" " + mr.getProbability() + " " + mr.getMatchType());
			}
		}
		
		for (int i=0; i<ces.size(); i++) {
			CompositeEntity sub = (CompositeEntity) ces.get(i);
			writeCompositeEntity(sub);
		}
		
		System.out.println ("-->");
	}

}
