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
 * This implementation wraps a List and walks through the list.
 * 
 * @author pcheung
 *
 */
public class ComparisonListSet implements IComparisonSet {
	
	private static final long serialVersionUID = 1L;
	private List list;
	private int ind;
	private int size;
	
	
	/** This constructor takes in a list of ComparisonPair.
	 * 
	 * @param list
	 */
	public ComparisonListSet (List list) {
		this.list = list;
		ind = 0;
		size = list.size ();
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#hasNextPair()
	 */
	public boolean hasNextPair() {
		if (ind < size) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#getNextPair()
	 */
	public ComparisonPair getNextPair() {
		ComparisonPair ret = (ComparisonPair) list.get(ind);
		ind ++;
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet#writeDebug()
	 */
	public String writeDebug() {
		StringBuffer sb = new StringBuffer ();
		sb.append(Constants.LINE_SEPARATOR);
		for (int i=0; i<size; i++) {
			ComparisonPair p = (ComparisonPair) list.get(i);
			sb.append('(');
			sb.append(p.id1.toString());
			sb.append(',');
			sb.append(p.id2.toString());
			sb.append(')');
		}
		sb.append(Constants.LINE_SEPARATOR);
		return sb.toString();
	}

}
