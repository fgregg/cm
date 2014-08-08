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

import com.choicemaker.util.LongArrayList;

/**
 * This object stores two long record id's.
 * 
 * @author pcheung
 * 
 *
 */
public class PairID implements Comparable<PairID>, IIDSet {
	
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

	@Override
	public int compareTo (PairID p) {
		if (p == null) {
			throw new IllegalArgumentException("null pair");
		}
		int ret = 0;
		if (id1 < p.id1) ret = -1;
		else if (id1 > p.id1) ret = 1;
		else if (id1 == p.id1) {
			if (id2 < p.id2) ret = -1;
			else if (id2 > p.id2) ret = 1;
			else if (id2 == p.id2) ret = 0; 
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id1 ^ (id1 >>> 32));
		result = prime * result + (int) (id2 ^ (id2 >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PairID other = (PairID) obj;
		if (id1 != other.id1) {
			return false;
		}
		if (id2 != other.id2) {
			return false;
		}
		return true;
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
