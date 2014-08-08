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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This is a sink that stores MatchRecord2.
 * 
 * @author pcheung
 *
 */
public interface IMatchRecord2Sink<T extends Comparable<? super T>> extends
		ISink {

	/** Writes out an ArrayList of MatchRecord2. */
	public void writeMatches(List<MatchRecord2<T>> matches)
			throws BlockingException;

	/** Writes out a Collection of MatchRecord2. */
	public void writeMatches(Collection<MatchRecord2<T>> c)
			throws BlockingException;

	/** Writes out an Iterator containing MatchRecord2. */
	public void writeMatches(Iterator<MatchRecord2<T>> it)
			throws BlockingException;

	/** Writes out a single MatchRecord2. */
	public void writeMatch(MatchRecord2<T> match) throws BlockingException;

}
