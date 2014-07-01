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
package com.choicemaker.cm.core.base;

import com.choicemaker.cm.core.Record;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 */
public class RecordRecordData extends RecordData {
	private Record record;
	
	public RecordRecordData(Record record) {
		this.record = record;
	}

	/**
	 * @see com.choicemaker.cm.core.base.RecordData#getFirstRecord()
	 */
	public Record getFirstRecord() {
		return record;
	}

	/**
	 * @see com.choicemaker.cm.core.base.RecordData#getSecondRecord()
	 */
	public Record getSecondRecord() {
		return null;
	}
}
