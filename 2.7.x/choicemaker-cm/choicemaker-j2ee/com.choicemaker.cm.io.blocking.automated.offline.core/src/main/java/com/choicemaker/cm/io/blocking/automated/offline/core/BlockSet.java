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
import java.util.ArrayList;
import java.util.List;

import com.choicemaker.util.IntArrayList;

/**
 * This object represents a Blocking Set.  It stores the columns id and values, and a list of row ids in the 
 * set.
 *
 * @author pcheung
 *
 */
public class BlockSet<T extends Comparable<? super T>> implements Serializable, IIDSet<T>{

	/* As of 2010-03-10 */
	static final long serialVersionUID = 4684611552672342011L;

	IntArrayList columns;  //LinkedList containing BlockValue objects.
	List<T> ids; //This stores the Record ids belonging to this blocking set.
	
	public BlockSet () {
		columns = new IntArrayList (3);
		ids = new ArrayList<>(2);
	}
	
	public BlockSet (int column) {
		columns = new IntArrayList ();
		columns.add(column);
		ids = new ArrayList<> (2);
	}
	
	public void addColumn (int column) {
		columns.add(column);
	}
	
	public void addColumns (IntArrayList cols) {
		columns.addAll(cols);
	}
	
	public IntArrayList getColumns () {
		return columns;
	}
	
	public void addRecordID (T i) {
		ids.add(i);
	}
	
	public void setRecordIDs (List<T> list) {
		ids = list;
	}
	
	public List<T> getRecordIDs () {
		return ids;
	}
	
	public boolean equals (BlockSet<T> bs) {
		if (this.columns.equals(bs.columns) && this.ids.equals(bs.ids)) return true;
		else return false;
	}
	
//	public int countIDs () {
//		return ids.size();
//	}
}
