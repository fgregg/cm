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

/**This interface is used in the chunking code to tranform data structure of internal id to 
 * staging and master ids.
 * 
 * @author pcheung
 *
 */
public interface ITransformer <T extends Comparable<? super T>> {
	
	/**
	 * This method prepares the tranformer.
	 *
	 */
	public void init() throws BlockingException;
	
	
	/** The method returns the internal id at which master id begin.
	 * 
	 * @return int
	 */
	public int getSplitIndex ();


	/** This method tells the transformer to use the next sink.  This occurs when there is enough
	 * data in the current chunk.
	 * 
	 * @throws BlockingException
	 */
	public void useNextSink () throws BlockingException;
	
	
	/** Call this when you are done with the transformer.  It release resources.
	 * 
	 * @throws BlockingException
	 */
	public void close () throws BlockingException;
	
	
	/** This transforms the given ID set and writes the stage and master ids to the current sink.
	 * It is the reponsibility of each implementation to check that the appropiate sink is use to
	 * for the given implementation of IIDSet.
	 * 
	 * @param bs
	 * @throws BlockingException
	 */
	public void transform (IIDSet<T> bs) throws BlockingException;
	
	
	/** This method frees up all the resources.  It is called when there is nothing to transform.
	 * 
	 *
	 */
	public void cleanUp  () throws BlockingException;
	
	
}
