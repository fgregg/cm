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
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;

/**
 * FIXME what values does this class add? Can it be eliminated?
 *
 * @author pcheung
 *
 */
public class ComparisonSetSource<T extends Comparable<T>> implements
		IComparisonSetSource<T> {

	private IComparisonArraySource<T> source;

	public ComparisonSetSource(IComparisonArraySource<T> source) {
		this.source = source;
	}

	@Override
	public IComparisonSet<T> next() throws BlockingException {
		return source.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#exists()
	 */
	@Override
	public boolean exists() {
		return source.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.ISource#open()
	 */
	@Override
	public void open() throws BlockingException {
		source.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#hasNext()
	 */
	@Override
	public boolean hasNext() throws BlockingException {
		return source.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#close()
	 */
	@Override
	public void close() throws BlockingException {
		source.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	@Override
	public String getInfo() {
		return source.getInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	@Override
	public void delete() throws BlockingException {
		source.delete();
	}

}
