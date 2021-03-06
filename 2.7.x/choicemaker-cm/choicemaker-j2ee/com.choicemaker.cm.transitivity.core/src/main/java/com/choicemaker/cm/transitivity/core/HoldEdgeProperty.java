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

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This class checks to see if the edge is a hold edge.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({"rawtypes" })
public class HoldEdgeProperty implements EdgeProperty {
	
	private static HoldEdgeProperty property;
	
	private HoldEdgeProperty () {
	}
	
	public static HoldEdgeProperty getInstance () {
		if (property == null) property = new HoldEdgeProperty ();
		return property;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.transitivity.core.EdgeProperty#hasProperty(com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2)
	 */
	public boolean hasProperty(MatchRecord2 mr) {
		if (mr.getMatchType() == Decision.HOLD) return true;
		else return false;
	}

}
