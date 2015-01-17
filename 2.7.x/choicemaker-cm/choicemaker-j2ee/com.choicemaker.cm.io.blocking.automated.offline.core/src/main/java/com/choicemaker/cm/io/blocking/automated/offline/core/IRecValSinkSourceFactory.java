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

import com.choicemaker.cm.core.BlockingException;

/**
 * This object contains methods to create and remove IRecValSink and
 * IRecValSource.
 * 
 * @author pcheung
 *
 */
public interface IRecValSinkSourceFactory {

	/**
	 * This gets the next IRevValSink.
	 * 
	 * @return IRecValSink - the next sink.
	 * @throws BlockingException
	 */
	public IRecValSink getNextSink() throws BlockingException;

	/**
	 * This gets the next IRevValSource.
	 * 
	 * @return IRecValSource - the next source
	 * @throws BlockingException
	 */
	public IRecValSource getNextSource() throws BlockingException;

	/**
	 * This gets the number of sinks created.
	 * 
	 * @return int - the number of sinks created
	 */
	public int getNumSink();

	/**
	 * This gets the number of sources created.
	 * 
	 * @return int - the number of sources created
	 */
	public int getNumSource();

	/**
	 * This removes the given sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink(IRecValSink sink) throws BlockingException;

	/**
	 * This removes the given source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource(IRecValSource source) throws BlockingException;

	/**
	 * This creates a IRecValSource from the given sink.
	 * 
	 * @param sink
	 * @return IRecValSource - the source created from the given sink.
	 * @throws BlockingException
	 */
	public IRecValSource getSource(IRecValSink sink) throws BlockingException;

	/**
	 * This creates a IRecValSink from the given source.
	 * 
	 * @param source
	 * @return IRecValSink - the source created from the given source.
	 * @throws BlockingException
	 */
	public IRecValSink getSink(IRecValSource source) throws BlockingException;

}
