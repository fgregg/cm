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

import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This class checks to see if the edge is a match or hold edge.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class MatchHoldEdgeProperty implements EdgeProperty {
	
	private static MatchHoldEdgeProperty property;
	
	private MatchHoldEdgeProperty () {
	}
	
	public static MatchHoldEdgeProperty getInstance () {
		if (property == null) property = new MatchHoldEdgeProperty ();
		return property;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.EdgeProperty#hasProperty(com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2)
	 */
	public boolean hasProperty(MatchRecord2 mr) {
		if (mr.getMatchType() == MatchRecord2.MATCH || 
			mr.getMatchType() == MatchRecord2.HOLD) return true;
		else return false;
	}

}
