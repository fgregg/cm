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

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;

/**
 * This is a source that reads MatchRecord2.
 * 
 * @author pcheung
 *
 */
public interface IMatchRecord2Source extends ISource {
	
	/** Gets the next MatchRecord2. */
	public MatchRecord2 getNext () throws BlockingException;
	
	/** Returns the number of MatchRecords read so far. */
	public int getCount ();

}