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
package com.choicemaker.cm.io.blocking.automated.offline.impl;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;

/**
 * This wrapper object takes MatchRecord2Source and makes it look like a IComparableSource.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
public class ComparableMRSource implements IComparableSource {
	
	private IMatchRecord2Source source;
	
	public ComparableMRSource (IMatchRecord2Source source) {
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getNext()
	 */
	public Comparable getNext() throws BlockingException {
		return source.getNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource#getCount()
	 */
	public int getCount() {
		return source.getCount();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	public boolean exists() {
		return source.exists();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	public void open() throws BlockingException {
		source.open();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	public boolean hasNext() throws BlockingException {
		return source.hasNext();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	public void close() throws BlockingException {
		source.close ();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return source.getInfo();
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void remove() throws BlockingException {
		source.remove();
	}

}
