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

import java.sql.ResultSet;
import java.util.Map;

/**
 * Base interface for all generated database readers, which
 * read data from result sets and translate them into object graphs
 * of holder class instances.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:55 $
 */
public interface DbReaderParallel extends DbReader {
	/**
	 * Opens a database reader in preparation for reading. It is automatically
	 * closed upon retrieval of the last entity.
	 *
	 * @param   rs  The result sets to be read from. <code>rs.length</code> must be
	 *            <code>getNoCursors()</code>.
	 * @throws  SQLException  if an exception occurs while reading from the result sets.
	 */
	void open(ResultSet[] rs) throws java.sql.SQLException;
	
	Map getIndices();
}
