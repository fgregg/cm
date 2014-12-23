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
public interface ISource<T> {

	/** True is this source file exists and not null. */
	boolean exists();

	/** Opens and initializes the source for reading. */
	void open() throws BlockingException;

	/** True if there is more data in the source. */
	boolean hasNext() throws BlockingException;

	/** Returns the next datum from the source */
	T next() throws BlockingException;

	/** Closes the source. */
	void close() throws BlockingException;

	/** Gets the file name or other pertinent information if it is not a file. */
	String getInfo();

	/** This method cleans up resources and removes the source. */
	void delete() throws BlockingException;

}
