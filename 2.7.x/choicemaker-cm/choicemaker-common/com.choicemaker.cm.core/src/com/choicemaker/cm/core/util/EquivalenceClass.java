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
package com.choicemaker.cm.core.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


//
// Helper Class
//

public class EquivalenceClass implements Comparable {

	private SortedSet members = new TreeSet();
	
	public EquivalenceClass() { }
	
	public void addMember(Object objId) throws IllegalArgumentException {
		if (!members.add(objId)) {
			throw new IllegalArgumentException("Already contains member with ID: " + objId);
		}
	}

	public int size() {
		return members.size();
	}
	
	public SortedSet getMemberIds() {
		return Collections.unmodifiableSortedSet(members);
	}
			
	public final boolean equals(Object obj) {
		// return super.equals(obj);
		boolean retVal = false;
		if (obj instanceof EquivalenceClass) {
			EquivalenceClass that = (EquivalenceClass) obj;
			retVal = this.members.equals(that.members);
		}
		return retVal;
	}

	public final int compareTo(Object obj) {
		
		// May throw ClassCastException per spec
		EquivalenceClass other = (EquivalenceClass)obj;
		
		Iterator i=this.members.iterator();
		Iterator j=other.members.iterator();

		int retVal = 0;
		while (retVal == 0 && (i.hasNext() || j.hasNext()) ) {
			if (!i.hasNext() && j.hasNext()) {
				retVal = -1;
				break;
			} else if (i.hasNext() && !j.hasNext()) {
				retVal = 1;
				break;
			} else {
				Comparable f = (Comparable)i.next();
				Comparable g = (Comparable)j.next();
				retVal = f.compareTo(g);
			}
		}

		return retVal;
	}
	
	public SortedSet getMembers () {
		return Collections.unmodifiableSortedSet(members);
	}
	
	public int hashCode() {
		return this.members.hashCode();
	}

}
