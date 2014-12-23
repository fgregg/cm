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
package com.choicemaker.cm.transitivity.util;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.ISource;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.transitivity.core.CompositeEntity;

/**
 * This object takes a IMatchRecord2Source that contains separator objects and
 * returns one CompositeEntity at a time.
 * 
 * This is more efficient that CompositeEntityBuilder, because it doesn't need
 * to do set union/find. It relies on the separator to know when a
 * CompositeEntity is complete.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class CompositeEntitySource<T extends Comparable<T>> implements
		ISource<CompositeEntity<T>> {

	private IMatchRecord2Source<T> source;
	private MatchRecord2<T> separator = null;

	private CompositeEntity<T> nextCE = null;

	private int count;

	/**
	 * This constructor takes in a IMatchRecord2Source from which to build
	 * CompositeEntities.
	 * 
	 * @param source
	 */
	public CompositeEntitySource(IMatchRecord2Source source) {
		this.source = source;
	}

	public void open() throws BlockingException {
		source.open();
	}

	public void close() throws BlockingException {
		source.close();
	}

	public boolean exists() {
		return source.exists();
	}

	public boolean hasNext() throws BlockingException {
		if (this.nextCE == null) {
			this.nextCE = readNext();
		}
		return this.nextCE != null;
	}
	
	public CompositeEntity<T> next() throws BlockingException {
		return getNext();
	}

	public CompositeEntity<T> getNext() throws BlockingException {
		if (this.nextCE == null) {
			this.nextCE = readNext();
		}
		CompositeEntity ce = this.nextCE;
		this.nextCE = null;

		return ce;
	}

	/**
	 * This method reads the next CompositeEntity from the IMatchRecord2. A
	 * Composite Entity is consists of the set of MatchRecord2 between the
	 * separators.
	 * 
	 * @return CompositeEntity
	 * @throws BlockingException
	 */
	private CompositeEntity readNext() throws BlockingException {
		CompositeEntity ce = new CompositeEntity(new Integer(count));

		boolean stop = false;

		while (source.hasNext() && !stop) {
			MatchRecord2 mr = source.getNext();

			if (separator == null) {
				Comparable c = mr.getRecordID1();
				separator = (MatchRecord2<T>) MatchRecord2Factory.getSeparator(c);
			}

			if (!mr.equals(separator)) {
				ce.addMatchRecord(mr);
			} else {
				stop = true;
			}
		}

		if (ce.getAllLinks().size() == 0)
			ce = null;

		count++;

		return ce;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#getInfo()
	 */
	public String getInfo() {
		return source.getInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.choicemaker.cm.io.blocking.automated.offline.core.ISource#remove()
	 */
	public void delete() throws BlockingException {
		source.delete();
	}

	public int getCount() {
		return count++;
	}

}
