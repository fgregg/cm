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
 * This interface defines what edge properties to check for.
 * 
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public interface EdgeProperty {

	/** This returns true if the match pair has the desired property.
	 * 
	 * @param mr
	 * @return boolean
	 */
	public boolean hasProperty (MatchRecord2 mr);

}
