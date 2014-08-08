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

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

//
// Helper Class
//

public class EquivalenceClass implements Comparable<EquivalenceClass> {

	private SortedSet<Long> members = new TreeSet<>();

	public EquivalenceClass() {
	}

	public void addMember(Long objId) throws IllegalArgumentException {
		if (!members.add(objId)) {
			throw new IllegalArgumentException(
					"Already contains member with ID: " + objId);
		}
	}

	public int size() {
		return members.size();
	}

	public SortedSet<Long> getMemberIds() {
		return Collections.unmodifiableSortedSet(members);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((members == null) ? 0 : members.hashCode());
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
		EquivalenceClass other = (EquivalenceClass) obj;
		if (members == null) {
			if (other.members != null) {
				return false;
			}
		} else if (!members.equals(other.members)) {
			return false;
		}
		return true;
	}

	public final int compareTo(EquivalenceClass other) {
		Iterator<Long> i = this.members.iterator();
		Iterator<Long> j = other.members.iterator();

		int retVal = 0;
		while (retVal == 0 && (i.hasNext() || j.hasNext())) {
			if (!i.hasNext() && j.hasNext()) {
				retVal = -1;
				break;
			} else if (i.hasNext() && !j.hasNext()) {
				retVal = 1;
				break;
			} else {
				Long f = i.next();
				Long g = j.next();
				retVal = f.compareTo(g);
			}
		}

		return retVal;
	}

	public SortedSet<Long> getMembers() {
		return Collections.unmodifiableSortedSet(members);
	}

}
