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

/**
 * This interface handles the writing of ComparisonGroups.
 * 
 * @author pcheung
 *
 */
public interface IComparisonArraySink<T extends Comparable<? super T>> extends ISink {

	/** Writes the BlockSet to the sink. */
	public void writeComparisonArray (ComparisonArray<T> cg) throws BlockingException;


}
