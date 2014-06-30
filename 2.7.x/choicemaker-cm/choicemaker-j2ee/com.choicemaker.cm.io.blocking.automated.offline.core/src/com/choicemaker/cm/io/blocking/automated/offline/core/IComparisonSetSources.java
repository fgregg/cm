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
 * This is a collection of IComparisonSetSources.
 * 
 * @author pcheung
 *
 */
public interface IComparisonSetSources {
	
	/** This returns the next comparison set source.
	 * 
	 * @return IComparisonSetSource
	 */
	public IComparisonSetSource getNextSource ();
	
	
	/** This returns true if this collection has more sources.
	 * 
	 * @return boolean
	 */
	public boolean hasNextSource () throws BlockingException;
	
	
	/** This method cleans up and frees up resources.
	 * 
	 *
	 */
	public void cleanUp () throws BlockingException;

}
