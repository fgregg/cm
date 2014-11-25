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
package com.choicemaker.cm.core;
import java.io.IOException;

/**
 * Base interface of record-related sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:54:54 $
 */

public interface Sink extends AutoCloseable {
	/**
	 * Opens the sink for writing data.
	 *
	 * A sink must be closed before it can be re-opened.
	 * Reopening the sink will re-start the retrieval from the beginning.
	 * There is no guarantee that the same data will be returned or that
	 * the order is the same, but in most cases these two properties hold.
	 *
	 * @throws  IOException  if there is a problem opening the sink.
	 */
	void open() throws IOException;

	/**
	 * Closes the data sink.
	 * Every data sinks that is opened must eventually be closed again explicitly.
	 *
	 * @throws  IOException  if there is a problem closing the sink.
	 */
	void close() throws IOException;

	/**
	 * Flushes the data sink.
	 * If the sink is backed by persistent storage, this method forces the sink
	 * to write any cached data to the storage. Otherwise this method does nothing.
	 *
	 * @throws  IOException  if there is a problem writing the data.
	 */
	void flush() throws IOException;

	String getName();

	void setName(String name);

	ImmutableProbabilityModel getModel();

	void setModel(ImmutableProbabilityModel m);
}
