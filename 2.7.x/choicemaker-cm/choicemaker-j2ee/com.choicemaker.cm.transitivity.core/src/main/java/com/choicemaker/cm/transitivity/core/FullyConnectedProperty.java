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

import java.util.List;


/**
 * This checks to see if the input graph is a fully connected graph.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({"rawtypes" })
public class FullyConnectedProperty implements SubGraphProperty {

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.SubGraphProperty#hasProperty(com.choicemaker.cm.transitivity.core.CompositeEntity)
	 */
	public boolean hasProperty(CompositeEntity ce) {
		int n = ce.getChildren().size();
		
		List links = ce.getAllLinks();
		int e = 0;
		for (int i=0; i<links.size(); i++) {
			Link l = (Link) links.get(i);
			List mrs = l.getLinkDefinition();
			e += mrs.size();
		}

		n = n*(n-1)/2;
		if (e == n) return true;
		else return false;
	}

}
