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
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IPairIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.PairID;

/**
 * @author pcheung
 *
 */
public class ComparablePairSinkSourceFactory<T extends Comparable<? super T>>
		implements IComparableSinkSourceFactory<PairID<T>> {

	private IPairIDSinkSourceFactory<T> factory;

	public ComparablePairSinkSourceFactory(IPairIDSinkSourceFactory<T> factory) {
		this.factory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory#getNextSink()
	 */
	public IComparableSink<PairID<T>> getNextSink() throws BlockingException {
		return new ComparablePairSink<T>(factory.getNextSink());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory#getNextSource()
	 */
	public IComparableSource<PairID<T>> getNextSource() throws BlockingException {
		return new ComparablePairSource<T>(factory.getNextSource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory
	 * #getSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IComparableSink)
	 */
	public IComparableSource<PairID<T>> getSource(IComparableSink<PairID<T>> sink)
			throws BlockingException {
		@SuppressWarnings("unchecked")
		IPairIDSink<T> o = (IPairIDSink<T>) sink.getBaseObject();
		return new ComparablePairSource<T>(factory.getSource(o));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return factory.getNumSink();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return factory.getNumSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IComparableSinkSourceFactory
	 * #move(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IComparableSink,
	 * com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink)
	 */
	@SuppressWarnings("unchecked")
	public void move(IComparableSink<PairID<T>> sink1, IComparableSink<PairID<T>> sink2)
			throws BlockingException {
		factory.move((IPairIDSink<T>) sink1.getBaseObject(),
				(IPairIDSink<T>) sink2.getBaseObject());
	}

}
