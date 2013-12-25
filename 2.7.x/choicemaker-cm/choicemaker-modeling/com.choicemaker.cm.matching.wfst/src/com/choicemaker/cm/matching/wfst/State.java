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
package com.choicemaker.cm.matching.wfst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class State implements Cloneable {

	public boolean start, end;
	public int no;
	public HashMap transes;

	public State(int n) {
		this(false, false, n);
	}

	public State(boolean s, boolean e, int n) {
		start = s;
		end = e;
		no = n;
		transes = new HashMap();
	}

	/**
	 * Clones the input HashMap
	 * @param s start
	 * @param e end
	 * @param n number
	 * @param t a map of Strings to Lists of Trans objects
	 */
	private State(boolean s, boolean e, int n, HashMap t) {
		start = s;
		end = e;
		no = n;
		if (t == null) {
			throw new IllegalArgumentException("null transition HashMap");
		}

		// Clone the input HashMap
		Set keys = t.keySet();
		transes = new HashMap();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String orginalKey = (String) i.next();
			String clonedKey = new String(orginalKey);
			List originalList = (List) t.get(orginalKey);
			LinkedList clonedList = null;
			if (originalList != null) {
				clonedList = new LinkedList();
				for (Iterator j = originalList.iterator(); j.hasNext();) {
					Trans originalTrans = (Trans) j.next();
					Trans clonedTrans = null;
					if (originalTrans != null) {
						clonedTrans = (Trans) originalTrans.clone();
					}
					clonedList.add(clonedTrans);
				} // for j over orginalList
			} // for non-null orignalList
			transes.put(clonedKey, clonedList);
		} // for i over keys

	} //ctor(boolean,boolean,int,HashMap)

	public Object clone() {
		return new State(this.start, this.end, this.no, this.transes);
	}

	public Iterator transit(String str) {
		LinkedList transes1 = (LinkedList) transes.get(str);
		if (transes1 != null) {
			return transes1.iterator();
		}
		return null;
	}

	//    public Trans transit(String str) {
	//	return (Trans)transes.get(str);
	//    }

	public void addTrans(Trans trans) {
		LinkedList list = (LinkedList) transes.get(trans.in);
		if (list == null) {
			list = new LinkedList();
		}
		list.add(trans);
		transes.put(trans.in, list);
	}

}

