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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This is a sink that stores MatchRecord2.
 * 
 * @author pcheung
 *
 */
public interface IMatchRecord2Sink extends ISink {

	/** Writes out an ArrayList of MatchRecord2. */
	public void writeMatches (ArrayList matches) throws BlockingException;
	
	/** Writes out a Collection of MatchRecord2. */
	public void writeMatches (Collection c) throws BlockingException;
	
	/** Writes out an Iterator containing MatchRecord2. */
	public void writeMatches (Iterator it) throws BlockingException;
	
	/** Writes out a single MatchRecord2. */
	public void writeMatch (MatchRecord2 match) throws BlockingException;
	
}
