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

import com.choicemaker.cm.core.util.LongArrayList;

/**
 * This object stores two long record id's.
 * 
 * @author pcheung
 * 
 *
 */
public class PairID implements Comparable, IIDSet {
	
	private long id1, id2;
	
	public PairID (long l1, long l2) {
		id1 = l1;
		id2 = l2;
	}
	
	public long getID1 () {
		return id1;
	}
	
	public long getID2 () {
		return id2;
	}

	
	public int compareTo (Object o) {
		int ret = 0;
		PairID p = (PairID) o;
		
		if (id1 < p.id1) ret = -1;
		else if (id1 > p.id1) ret = 1;
		else if (id1 == p.id1) {
			if (id2 < p.id2) ret = -1;
			else if (id2 > p.id2) ret = 1;
			else if (id2 == p.id2) ret = 0; 
		}
		return ret;
	}
	
	
	public boolean equals (Object o) {
		boolean ret = false;
		
		if (o.getClass() == PairID.class) {
			PairID p = (PairID) o;
			if ((id1 == p.id1) && (id2 == p.id2)) ret = true;
		}
		
		return ret;
	}
	
	
	public int hashCode () {
		return (int)(id1 ^(id1>>>32) ^ id2 ^ (id2>>>32) );
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet#getRecordIDs()
	 */
	public LongArrayList getRecordIDs() {
		LongArrayList list = new LongArrayList (2);
		list.add(id1);
		list.add(id2);
		return list;
	}


}
