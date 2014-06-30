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

import com.choicemaker.cm.core.base.BlockingException;

/**
 * This is used by GenericDedupService.  This interface creates temporary
 * Comparable sinks and sources for the service.
 * 
 * @author pcheung
 *
 */
public interface IComparableSinkSourceFactory {

	/** Gets the next IComparableSink in the sequence. */
	public IComparableSink getNextSink () throws BlockingException;
	
	/** Gets the next IComparableSource in the sequence. */
	public IComparableSource getNextSource () throws BlockingException;
	
	/** Gets the next IComparableSource from the sink. */
	public IComparableSource getSource (IComparableSink sink) throws BlockingException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();
	
	/** Gets the number of sequence sources created. */
	public int getNumSource ();

	/** Moves sink1 to sink2. */
	public void move (IComparableSink sink1, IComparableSink sink2) throws BlockingException;

}
