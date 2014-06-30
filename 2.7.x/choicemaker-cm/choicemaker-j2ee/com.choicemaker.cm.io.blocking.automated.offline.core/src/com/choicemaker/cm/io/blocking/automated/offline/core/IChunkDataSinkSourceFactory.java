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
import com.choicemaker.cm.core.base.RecordSink;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.xmlconf.XmlConfException;

/**
 * This Object handles creating RecordSink to put chunk data.
 * 
 * @author pcheung
 *
 */
public interface IChunkDataSinkSourceFactory {

	/** Gets the next record sink. */	
	public RecordSink getNextSink () throws XmlConfException;
	
	/** Gets the number of sequence sinks created. */
	public int getNumSink ();

	/** Gets the next record source. This only returns a source from a previously created sink. */	
	public RecordSource getNextSource() throws XmlConfException;
	
	/** Gets the number of sequence source created. */
	public int getNumSource ();

	/** Removes the record sinks in memory. */	
	public void removeAllSinks () throws BlockingException;
	

	/** This removes records sinks from 1 to numChunks
	 * 
	 * @param numChunks
	 * @throws BlockingException
	 */
	public void removeAllSinks (int numChunks) throws BlockingException;
}
