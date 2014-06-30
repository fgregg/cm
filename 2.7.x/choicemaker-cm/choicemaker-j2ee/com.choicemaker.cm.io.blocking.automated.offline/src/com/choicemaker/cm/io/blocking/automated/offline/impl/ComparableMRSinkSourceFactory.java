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

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;

/**
 * This is a wrapper on MatchRecord2SinkSourceFactory to make look like a 
 * IComparableSinkSourceFactory.
 * 
 * @author pcheung
 *
 */
public class ComparableMRSinkSourceFactory 	implements IComparableSinkSourceFactory {
	
	private IMatchRecord2SinkSourceFactory factory;
	
	public ComparableMRSinkSourceFactory (IMatchRecord2SinkSourceFactory factory) {
		this.factory = factory;
	}
	

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#getNextSink()
	 */
	public IComparableSink getNextSink() throws BlockingException {
		return new ComparableMRSink (factory.getNextSink());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#getNextSource()
	 */
	public IComparableSource getNextSource() throws BlockingException {
		return new ComparableMRSource(factory.getNextSource());
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#getSource(com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink)
	 */
	public IComparableSource getSource(IComparableSink sink) throws BlockingException {
		IMatchRecord2Sink o = (IMatchRecord2Sink) sink.getBaseObject();
		
		return new ComparableMRSource (factory.getSource(o));
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#getNumSink()
	 */
	public int getNumSink() {
		return factory.getNumSink();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#getNumSource()
	 */
	public int getNumSource() {
		return factory.getNumSource();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory#move(com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink, com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink)
	 */
	public void move(IComparableSink sink1, IComparableSink sink2) throws BlockingException {
		factory.move((IMatchRecord2Sink)sink1.getBaseObject(), (IMatchRecord2Sink)sink2.getBaseObject());
	}

}
