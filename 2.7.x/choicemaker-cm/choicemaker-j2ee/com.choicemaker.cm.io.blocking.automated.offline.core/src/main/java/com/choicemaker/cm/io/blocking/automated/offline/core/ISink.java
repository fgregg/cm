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
 * A Sink is an interface designed for writing data.
 * 
 * @author pcheung
 *
 */
public interface ISink {

	/** True is this source file exists and not null. */
	public boolean exists();

	/** opens the sink for overwrite. */
	public void open() throws BlockingException;

	/** opens the sink for append. */
	public void append() throws BlockingException;

	/** Closes the sink. */
	public void close() throws BlockingException;

	/** Flushes the sink. */
	public void flush() throws BlockingException;

	/** Gets the number of blocks written to the sink thus far. */
	public int getCount();

	/** Gets the file name or other pertinent information if it is not a file. */
	public String getInfo();

	/** This method cleans up resources and removes the sink. */
	public void remove() throws BlockingException;

}
