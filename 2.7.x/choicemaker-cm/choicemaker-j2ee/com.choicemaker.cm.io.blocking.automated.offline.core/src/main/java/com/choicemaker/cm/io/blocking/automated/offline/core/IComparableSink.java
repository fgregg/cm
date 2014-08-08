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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;

/**
 * @author pcheung
 *
 */
public interface IComparableSink<T extends Comparable<? super T>> extends ISink {

	/** Writes out an Iterator containing Comparable. */
	public void writeComparables(Iterator<T> it) throws BlockingException;

	/** Writes out a single Comparable object. */
	public void writeComparable(T C) throws BlockingException;

	/**
	 * This is the underlying object on which this wrapper is built.
	 * 
	 * @return Object
	 */
	public ISink getBaseObject();

}
