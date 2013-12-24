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
package com.choicemaker.cm.analyzer.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.choicemaker.cm.core.util.IntArrayList;

/**
 * @author	Adam Winkel
 */
public class CollectionMarkedRecordPairFilter extends MarkedRecordPairFilter {

	private HashSet pairs;
	private boolean useCollection;

	public CollectionMarkedRecordPairFilter() {
		useCollection = false;
	}

	public CollectionMarkedRecordPairFilter(Collection pairs) {
		setAcceptedPairs(pairs);
	}

	public void setAcceptedPairs(Collection pairs) {
		this.pairs = new HashSet(pairs);
		useCollection = true;
	}

	public void reset() {
		useCollection = false;
		super.reset();
	}

	public int[] filterSource(Collection src) {
		if (useCollection) {
			IntArrayList indices = new IntArrayList();
		
			int index = 0;
			Iterator itSrc = src.iterator();
			while (itSrc.hasNext()) {
				Object obj = itSrc.next();
				if (pairs.contains(obj)) {
					indices.add(index);
				}
				index++;
			}
		
			return indices.toArray();
		} else {
			return super.filterSource(src);	
		}
	}

}
