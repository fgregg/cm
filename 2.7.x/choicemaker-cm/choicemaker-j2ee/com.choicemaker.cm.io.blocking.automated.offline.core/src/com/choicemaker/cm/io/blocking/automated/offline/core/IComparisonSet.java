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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.io.Serializable;

/**
 * This represents a set of pairs to be compared.  
 * 
 * @author pcheung
 *
 */
public interface IComparisonSet extends Serializable {
	
	/** This returns true if there are more pairs to compare in this set.
	 * 
	 * @return boolean
	 */
	public boolean hasNextPair ();
	
	
	/** This gets the next pair of ids to be compared.  It returns a Pair object.  You should call 
	 * hasNextPair before calling this method.
	 * 
	 * @return ComparisonPair
	 */
	public ComparisonPair getNextPair ();


	/** This method returns a string of all the elements in this comparison set for
	 * debugging purposes.
	 * 
	 * @return
	 */
	public String writeDebug ();
	
}
