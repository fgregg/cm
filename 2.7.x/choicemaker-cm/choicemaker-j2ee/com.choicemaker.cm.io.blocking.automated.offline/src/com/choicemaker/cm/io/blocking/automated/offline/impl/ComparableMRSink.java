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

import java.util.Iterator;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This wrapper object takes a MatchRecord2Sink and makes it look like IComparableSink.
 * 
 * @author pcheung
 *
 */
public class ComparableMRSink implements IComparableSink {
	
	private IMatchRecord2Sink sink;
	
	public ComparableMRSink (IMatchRecord2Sink sink) {
		this.sink = sink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#writeComparables(java.util.Iterator)
	 */
	public void writeComparables(Iterator it) throws BlockingException {
		sink.writeMatches(it);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#writeComparable(java.lang.Comparable)
	 */
	public void writeComparable(Comparable C) throws BlockingException {
		if (C instanceof MatchRecord2) sink.writeMatch((MatchRecord2)C);
		else throw new BlockingException ("Invalid class " + C.getClass());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#exists()
	 */
	public boolean exists() {
		return sink.exists();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#open()
	 */
	public void open() throws BlockingException {
		sink.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#append()
	 */
	public void append() throws BlockingException {
		sink.append();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#close()
	 */
	public void close() throws BlockingException {
		sink.close();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getCount()
	 */
	public int getCount() {
		return sink.getCount();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#getInfo()
	 */
	public String getInfo() {
		return sink.getInfo();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#remove()
	 */
	public void remove() throws BlockingException {
		sink.remove();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#getBaseObject()
	 */
	public Object getBaseObject() {
		return sink;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	public void flush() throws BlockingException {
	}
	
}
