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

import java.util.List;

/**
 * This is a generic representation of a set of IDS.  This could be a BlockSet containing one block
 * or IDs or SuffixTreeNode containing a tree of blocks.
 * 
 * @author pcheung
 *
 */
public interface IIDSet<T extends Comparable<? super T>> {
	
	/** This method a LongArrayList of all the IDs in this set, array, or tree.
	 * Note that the set of IDs returns is not guaranteed for uniqueness.
	 * 
	 * @return LongArrayList
	 */
	public List<T> getRecordIDs (); 

}
