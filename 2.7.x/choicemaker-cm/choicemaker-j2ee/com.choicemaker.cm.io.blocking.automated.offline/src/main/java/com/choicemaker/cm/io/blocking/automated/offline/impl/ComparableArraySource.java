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
 * This wrapper takes a List of Comparables and makes it look like a
 * IComparableSource
 * 
 * @author pcheung
 *
 */
public class ComparableArraySource<T extends Comparable<T>> implements
		IComparableSource<T> {

	private List<T> list;
	private int ind = 0;

	public ComparableArraySource(List<T> list) {
		this.list = list;
	}

	@Override
	public T next() {
		T c = list.get(ind);
		ind++;
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource
	 * #getCount()
	 */
	@Override
	public int getCount() {
		return ind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	@Override
	public void open() throws BlockingException {
		// do nothing since the array list is already in memory
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		if (ind < list.size())
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	@Override
	public void close() throws BlockingException {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	@Override
	public String getInfo() {
		return list.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	@Override
	public void delete() throws BlockingException {
		list = null;
	}

}
