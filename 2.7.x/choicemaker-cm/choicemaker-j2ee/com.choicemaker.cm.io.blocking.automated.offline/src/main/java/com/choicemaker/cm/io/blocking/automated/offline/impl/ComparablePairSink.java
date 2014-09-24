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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;

/**
 * This wrapper makes IPairIDSink look like IComparableSink.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
public class ComparablePairSink implements IComparableSink {

	private IPairIDSink sink;
	
	public ComparablePairSink (IPairIDSink sink) {
		this.sink = sink;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#writeComparables(java.util.Iterator)
	 */
	public void writeComparables(Iterator it) throws BlockingException {
		while (it.hasNext()) {
			writeComparable ((Comparable) it.next());
		}
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#writeComparable(java.lang.Comparable)
	 */
	public void writeComparable(Comparable C) throws BlockingException {
		if (C instanceof PairID) sink.writePair((PairID)C);
		else throw new BlockingException ("Invalid class " + C.getClass());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink#getBaseObject()
	 */
	public Object getBaseObject() {
		return sink;
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
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISink#flush()
	 */
	public void flush() throws BlockingException {
	}
	
}
