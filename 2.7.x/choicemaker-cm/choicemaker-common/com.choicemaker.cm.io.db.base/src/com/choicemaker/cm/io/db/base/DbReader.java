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
package com.choicemaker.cm.io.db.base;

import com.choicemaker.cm.core.base.Record;

/**
 * Base interface for all generated database readers, which
 * read data from result sets and translate them into object graphs
 * of holder class instances.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:55 $
 */
public interface DbReader {
	/**
	 * Returns the next entity.
	 *
	 * @return  The next entity.
	 * @throws  SQLException  if an exception occurs while reading from the result sets.
	 */
	Record getNext() throws java.sql.SQLException;

	/**
	 * Answers whether there are more entities to be retrieved.
	 *
	 * @return  whether there are more entities to be retrieved.
	 */
	boolean hasNext();

	/**
	 * Returns the number of cursors to be passed.
	 *
	 * @return  the number of cursors to be passed.
	 */
	int getNoCursors();

	DbView[] getViews();

	String getMasterId();

	String getName();
}
