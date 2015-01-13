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

import java.io.Serializable;

import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.core.IValidatorBase;
import com.choicemaker.util.LongArrayList;

/**
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes"})
public class ValidatorBase implements IValidatorBase, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 6294000047456026375L;

	// this indicates where the staging rows are. 
	// true if the stage records are before the split Index
	private boolean isBefore; 
	
	private int splitIndex; //the point at which record sources change


	/** This constructor takes these two parameters:
	 *  
	 * @param isBefore - true if the stage records are before the split Index
	 * @param IRecordIdTranslator2 - the record ID to internal id translator
	 */
	public ValidatorBase (boolean isBefore, IRecordIdTranslator2 translator) {
		this.isBefore = isBefore;
		this.splitIndex = translator.getSplitIndex();
	}


	/** This method checks to see if all the id in the blocking set is from the "master" source.
	 * If so, return false.  A valid blocking set need to be consisted of at least 1 staging record.
	 */
	public boolean validBlockSet(BlockSet bs) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		boolean ret = false;
		
		if (splitIndex == 0) {
			ret = true;
		} else {
			LongArrayList list = bs.getRecordIDs();
			int s = list.size();
			for (int i=0; i<s; i++) {
				long l = list.get(i);
				if (l > max) max = l;
				if (l < min) min = l;
			}
		
			if (isBefore) {
				if (min < splitIndex) ret = true;
				else ret = false;
			} else {
				if (max >= splitIndex) ret = true;
				else ret = false;
			}
		
		}
		
		return ret;
	}

}
