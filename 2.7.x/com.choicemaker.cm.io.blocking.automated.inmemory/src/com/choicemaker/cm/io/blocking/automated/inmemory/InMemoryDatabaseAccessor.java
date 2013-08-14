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
package com.choicemaker.cm.io.blocking.automated.inmemory;

import java.io.IOException;
import java.util.Iterator;

import javax.sql.DataSource;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;

/**
 * @author ajwinkel
 *
 */
public class InMemoryDatabaseAccessor implements DatabaseAccessor {

	protected InMemoryDataSource imds;
	protected int start;

	protected Iterator itBlocked;
	
	public InMemoryDatabaseAccessor(InMemoryDataSource imds, int start) {
		this.imds = imds;
		this.start = start;
	}

	public DatabaseAccessor cloneWithNewConnection()
		throws CloneNotSupportedException {
		throw new CloneNotSupportedException("not yet implemented");
	}

	public void setDataSource(DataSource dataSource) { 
		throw new UnsupportedOperationException();
	}

	public void setCondition(Object condition) { 
		throw new UnsupportedOperationException();
	}

	public void open(AutomatedBlocker blocker) throws IOException {
		itBlocked = imds.select(blocker.getBlockingSets(), start);
	}

	public boolean hasNext() {
		return itBlocked.hasNext();
	}

	public Record getNext() throws IOException {
		return (Record)itBlocked.next();
	}

	public void close() throws IOException {
		itBlocked = null;
	}

}
