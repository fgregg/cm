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
 * A Source is an interface designed for reading data.
 *   
 * @author pcheung
 *
 */
public interface ISource {

	/** True is this source file exists and not null. */
	public boolean exists ();
	
	/** Opens and initializes the source for reading. */
	public void open () throws BlockingException;

	/** True if there is more data in the source. */
	public boolean hasNext () throws BlockingException;
	
	/** Closes the source. */
	public void close () throws BlockingException;

	/** Gets the file name or other pertinent information if it is not a file. */
	public String getInfo ();

	/** This method cleans up resources and removes the source. */
	public void remove () throws BlockingException;


}
