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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonArray;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink;

/**
 * This is a sink of sinks. It uses round robin method to distribute the
 * ComparisonArrays evenly across the sinks.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class ComparisonArrayGroupSink implements IComparisonArraySink {

	// This is the array of round robin sinks.
	private IComparisonArraySink[] sinks = null;

	// This is the round robin counter
	private int current = 0;

	// this counts the number of ComparisonTreeNodes written so far
	private int count = 0;

	/**
	 * This constructor takes two parameters: 1. A factory to create a group of
	 * sinks. 2. The number of sinks to create.
	 * 
	 * @param factory
	 * @param num
	 */
	public ComparisonArrayGroupSink(ComparisonArraySinkSourceFactory factory,
			int num) throws BlockingException {

		sinks = new IComparisonArraySink[num];
		for (int i = 0; i < num; i++) {
			sinks[i] = factory.getNextSink();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySink
	 * #
	 * writeComparisonArray(com.choicemaker.cm.io.blocking.automated.offline.core
	 * .ComparisonArray)
	 */
	@Override
	public void writeComparisonArray(ComparisonArray cg)
			throws BlockingException {
		sinks[current].writeComparisonArray(cg);
		count++;
		current++;
		if (current == sinks.length)
			current = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#open()
	 */
	@Override
	public void open() throws BlockingException {
		for (int i = 0; i < sinks.length; i++) {
			sinks[i].open();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#append()
	 */
	@Override
	public void append() throws BlockingException {
		for (int i = 0; i < sinks.length; i++) {
			sinks[i].append();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#close()
	 */
	@Override
	public void close() throws BlockingException {
		for (int i = 0; i < sinks.length; i++) {
			sinks[i].close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getCount()
	 */
	@Override
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getInfo()
	 */
	@Override
	public String getInfo() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < sinks.length; i++) {
			sb.append((sinks[i].getInfo()));
			sb.append('|');
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#remove()
	 */
	@Override
	public void remove() throws BlockingException {
		for (int i = 0; i < sinks.length; i++) {
			sinks[i].remove();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	@Override
	public void flush() throws BlockingException {
	}

}
