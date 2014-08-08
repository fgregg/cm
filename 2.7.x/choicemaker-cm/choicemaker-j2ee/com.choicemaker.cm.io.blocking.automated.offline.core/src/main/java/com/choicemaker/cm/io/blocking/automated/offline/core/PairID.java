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

import java.util.ArrayList;
import java.util.List;

/**
 * This object stores two long record id's.
 * 
 * @author pcheung
 * 
 *
 */
public class PairID<T extends Comparable<? super T>> implements
		Comparable<PairID<T>>, IIDSet<T> {

	private T id1, id2;

	public PairID(T l1, T l2) {
		id1 = l1;
		id2 = l2;
	}

	public T getID1() {
		return id1;
	}

	public T getID2() {
		return id2;
	}

	@Override
	public int compareTo(PairID<T> p) {
		int ret = 0;
		if (id1.compareTo(p.id1) < 0)
			ret = -1;
		else if (id1.compareTo(p.id1) > 0)
			ret = 1;
		else {
			if (id2.compareTo(p.id2) < 0)
				ret = -1;
			else if (id2.compareTo(p.id2) > 0)
				ret = 1;
			else
				assert ret == 0;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IIDSet#getRecordIDs
	 * ()
	 */
	public List<T> getRecordIDs() {
		List<T> list = new ArrayList<>(2);
		list.add(id1);
		list.add(id2);
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id1 == null) ? 0 : id1.hashCode());
		result = prime * result + ((id2 == null) ? 0 : id2.hashCode());
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
		@SuppressWarnings("unchecked")
		PairID<T> other = (PairID<T>) obj;
		if (id1 == null) {
			if (other.id1 != null) {
				return false;
			}
		} else if (!id1.equals(other.id1)) {
			return false;
		}
		if (id2 == null) {
			if (other.id2 != null) {
				return false;
			}
		} else if (!id2.equals(other.id2)) {
			return false;
		}
		return true;
	}

}
