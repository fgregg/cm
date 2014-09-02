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

import java.util.Iterator;

import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;

/**
 * This object takes a CompositeEntitySource and wraps it as an Iterator.
 * 
 * @author pcheung
 *
 */
public class CompositeEntityIterator implements Iterator {
	
	private static final Logger log = Logger.getLogger(CompositeEntityIterator.class.getName());

	private CompositeEntitySource source;
	
	public CompositeEntityIterator (CompositeEntitySource source) {
		this.source = source;
		try {
			source.open();
		} catch (BlockingException e) {
			log.severe(e.toString());
		}
	}
	
	public void finalize () {
		try {
			source.close();
		} catch (BlockingException e) {
			log.severe(e.toString());
		}
	}
	

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new RuntimeException ("This method is not implemented.");
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		boolean ret = false;
		try {
			ret = source.hasNext();
		} catch (BlockingException e) {
			log.severe(e.toString());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		Object o = null;
		try {
			o = source.getNext();
		} catch (BlockingException e) {
			log.severe(e.toString());
		}
		return o;
	}

}
