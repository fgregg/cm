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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;

/**
 * This wrapper takes an ArrayList of Comparables and makes it look like a IComparableSource
 * 
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
public class ComparableArraySource implements IComparableSource {

	private List list;
	private int ind = 0;
	
	public ComparableArraySource (List list) {
		this.list = list;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getNext()
	 */
	public Comparable getNext() throws BlockingException {
		Comparable c = (Comparable) list.get(ind);
		ind ++;
		return c;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getCount()
	 */
	public int getCount() {
		return ind;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		//do nothing since the array list is already in memory
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		if (ind < list.size()) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		//do nothing
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return list.toString();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		list = null;
	}

}
