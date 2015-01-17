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

import javax.sql.DataSource;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecValSource;

/**
 * @author pcheung
 *
 */
public class RecValDBFactory implements IRecValSinkSourceFactory {

	private DataSource ds;
	private int indSink = 0;
	private int indSource = 0;
	private int startingPoint = 0;

	public RecValDBFactory(DataSource ds, int startingPoint) {
		this.indSink = startingPoint;
		this.indSource = startingPoint;
		this.startingPoint = startingPoint;
		this.ds = ds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory#getNextSink()
	 */
	@Override
	public IRecValSink getNextSink() throws BlockingException {
		RecValDBSink sink = new RecValDBSink(ds, indSink);
		indSink++;
		return sink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory#getNextSource()
	 */
	@Override
	public IRecValSource getNextSource() throws BlockingException {
		RecValDBSource source = new RecValDBSource(ds, indSource);
		indSource++;
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory#getNumSink()
	 */
	@Override
	public int getNumSink() {
		return indSink - startingPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory#getNumSource()
	 */
	@Override
	public int getNumSource() {
		return indSource - startingPoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory
	 * #removeSink(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecValSink)
	 */
	@Override
	public void removeSink(IRecValSink sink) throws BlockingException {
		sink.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory
	 * #removeSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecValSource)
	 */
	@Override
	public void removeSource(IRecValSource source) throws BlockingException {
		source.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory
	 * #getSource(com.choicemaker.cm.io.blocking.automated
	 * .offline.core.IRecValSink)
	 */
	@Override
	public IRecValSource getSource(IRecValSink sink) throws BlockingException {
		int id = Integer.parseInt(sink.getInfo());
		RecValDBSource source = new RecValDBSource(ds, id);
		return source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.
	 * IRecValSinkSourceFactory
	 * #getSink(com.choicemaker.cm.io.blocking.automated.
	 * offline.core.IRecValSource)
	 */
	@Override
	public IRecValSink getSink(IRecValSource source) throws BlockingException {
		int id = Integer.parseInt(source.getInfo());
		RecValDBSink sink = new RecValDBSink(ds, id);
		return sink;
	}

}
