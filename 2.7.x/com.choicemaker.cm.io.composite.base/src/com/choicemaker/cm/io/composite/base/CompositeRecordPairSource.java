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
package com.choicemaker.cm.io.composite.base;

import com.choicemaker.cm.core.*;

/**
 * Composite source of record pairs.
 * A composite source is a collection of other sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 08:56:16 $
 */

public class CompositeRecordPairSource extends CompositeSource implements RecordPairSource {
	/**
	 * Returns the next record pair.
	 *
	 * @throws  IOException if the current source throws an <code>IOException</code>.
	 * @throws  NullPointerException if there are no more record pairs.
	 * @return  the next record pair. 
	 */
	public ImmutableRecordPair getNext() throws java.io.IOException {
		ImmutableRecordPair r = ((RecordPairSource) getCurSource()).getNext();
		nextValid();
		return r;
	}

	/**
	 * Adds a record pair source at the end of the collection.
	 */
	public void add(RecordPairSource s) {
		super.add(s);
	}

	public void add(RecordPairSource s, boolean saveAsRel) {
		super.add(s, saveAsRel);	
	}

	public RecordPairSource getSource(int index) {
		return (RecordPairSource) getSourceAtIndex(index);
	}
}
