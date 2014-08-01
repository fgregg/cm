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

import java.io.IOException;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;

/**
 * Composite source of records.
 * A composite source is a collection of other sources.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:55 $
 */
public class CompositeRecordSource extends CompositeSource implements RecordSource {
	/**
	 * Returns the next record.
	 *
	 * @throws  IOException if the current source throws an <code>IOException</code>.
	 * @throws  NullPointerException if there are no more records.
	 * @return  the next record. 
	 */
	public Record getNext() throws java.io.IOException {
		Record r = ((RecordSource) getCurSource()).getNext();
		nextValid();
		return r;
	}

	/**
	 * Adds a record source at the end of the collection.
	 */
	public void add(RecordSource s) {
		super.add(s);
	}

	public void add(RecordSource s, boolean saveAsRel) {
		super.add(s, saveAsRel);	
	}

	public RecordSource getSource(int index) {
		return (RecordSource) getSourceAtIndex(index);
	}

}
