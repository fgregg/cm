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
 * @author pcheung
 *
 */
public interface IPairIDSinkSourceFactory<T extends Comparable<? super T>> {

	/** Gets the next IPairIDSink in the sequence. */
	public IPairIDSink<T> getNextSink () throws BlockingException;
	
	/** Gets the next IPairIDSource in the sequence. */
	public IPairIDSource<T> getNextSource () throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();
	
	/** Creates an IPairIDSource for an existing IPairIDSink. */
	public IPairIDSource<T> getSource (IPairIDSink<T> sink) throws BlockingException;

	/** Creates an IPairIDSink for an existing IPairIDSource. */
	public IPairIDSink<T> getSink (IPairIDSource<T> source) throws BlockingException;

	/** Removes this sink.
	 * 
	 * @param sink
	 * @throws BlockingException
	 */
	public void removeSink (IPairIDSink<T> sink) throws BlockingException;


	/** Removes this source.
	 * 
	 * @param source
	 * @throws BlockingException
	 */
	public void removeSource (IPairIDSource<T> source) throws BlockingException;
	
	
	public void move (IPairIDSink<T> sink1, IPairIDSink<T> sink2) throws BlockingException;

}
