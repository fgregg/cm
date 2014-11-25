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
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 18:15:26 $
 */

public interface Source extends AutoCloseable {
	/**
	 * Opens the source for retrieving data.
	 *
	 * A source must be closed before it can be re-opened.
	 * Reopening the source will re-start the retrieval from the beginning.
	 * There is no guarantee that the same data will be returned or that
	 * the order is the same, but in most cases these two properties hold.
	 *
	 * @throws  IOException  if there is a problem accessing the data.
	 */
	void open() throws IOException;

	/**
	 * Closes the data source.
	 * Every data sources that is opened must eventually be closed again explicitly.
	 *
	 * @throws  IOException  if there is a problem accessing the data.
	 */
	void close() throws IOException;

	/**
	 * Answers whether the source has more data.
	 *
	 * @return whether the source has more data.
	 * @throws IOException  if there is a problem accessing the data.
	 */
	boolean hasNext() throws IOException;

	String getName();

	void setName(String name);

	ImmutableProbabilityModel getModel();

	void setModel(ImmutableProbabilityModel m);

	boolean hasSink();

	Sink getSink();
	
	String getFileName();	
}
