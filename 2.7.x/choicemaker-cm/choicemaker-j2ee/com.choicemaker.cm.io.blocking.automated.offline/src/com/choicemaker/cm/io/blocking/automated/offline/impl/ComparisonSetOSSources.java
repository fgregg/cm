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

import java.util.ArrayList;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources;

/**
 * This is a set of sources for oversized comparison sets.
 * 
 * @author pcheung
 *
 */
public class ComparisonSetOSSources implements IComparisonSetSources {
	private ComparisonArraySinkSourceFactory sFactory;
	private IComparisonArraySource next;
	private ArrayList sources = new ArrayList ();
	private int maxBlockSize;

	public ComparisonSetOSSources (ComparisonArraySinkSourceFactory sFactory, int maxBlockSize) {
		this.sFactory = sFactory;
		this.maxBlockSize = maxBlockSize;
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources#getNextSource()
	 */
	public IComparisonSetSource getNextSource() {
		return new ComparisonSetOSSource (next, maxBlockSize);
	}

	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources#cleanUp()
	 */
	public void cleanUp() throws BlockingException {
		for (int i=0; i<sources.size(); i++) {
			next = (IComparisonArraySource) sources.get(i);
			next.remove();
		}
	}


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources#hasNextSource()
	 */
	public boolean hasNextSource() throws BlockingException {
		next = sFactory.getNextSource();
		sources.add(next);
		return next.exists();
	}

}
