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
package com.choicemaker.cm.io.blocking.automated;

import java.io.IOException;

import javax.sql.DataSource;

import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.Record;

/**
 *
 * @author
 * @version $Revision: 1.2 $ $Date: 2010/03/24 21:35:40 $
 */
public interface DatabaseAccessor {

	String EXTENSION_POINT =
		ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;

	void open(AutomatedBlocker blocker, String databaseConfiguration)
			throws IOException;

	void close() throws IOException;

	boolean hasNext();

	Record getNext() throws IOException;

	void setDataSource(DataSource dataSource);

	void setCondition(Object condition);

	/**
	 * Implementations should return a DatabaseAccessor with the same DataSource
	 * and condition Object as this object (the one being cloned), but with a
	 * <code>java.sql.Connection</code> member that hasn't been opened yet and
	 * which is independent of the <code>Connection</code> held by this object
	 * (the one being cloned).
	 * 
	 * @exception CloneNotSupportException
	 *                if this object can't be cloned.
	 */
	DatabaseAccessor cloneWithNewConnection() throws CloneNotSupportedException;
}
