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
package com.choicemaker.cm.io.blocking.automated.offline.utils;

import java.io.Serializable;

import com.choicemaker.cm.core.util.LongArrayList;
import com.choicemaker.cm.io.blocking.automated.offline.core.BlockSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IValidator;

/**
 * This implementation of IValidator assumes that staging and master each has a distinct range of
 * id's and that they don't share any id's in common.
 * 
 * @author pcheung
 *
 */
public class Validator implements IValidator, Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -8622078470669618796L;

	// this indicates where the staging rows are. 
	// true if the stage records are before the split Index
	private boolean isBefore; 
	
	private int splitIndex; //the point at which record sources change
	
	private long [] stageRange; //range of record ID for stage record source.
	private long [] masterRange; //range of record ID for master record source.
	
	
	/** This constructor takes these two parameters:
	 *  
	 * @param isBefore - true if the stage records are before the split Index
	 * @param splitIndex - the point at which record sources change
	 */
	public Validator (boolean isBefore, RecordIDTranslator translator) {
		this.isBefore = isBefore;
		this.splitIndex = translator.getSplitIndex();
		
		if (isBefore) {
			stageRange = translator.getRange1();
			masterRange = translator.getRange2();
		} else {
			stageRange = translator.getRange2();
			masterRange = translator.getRange1();
		}

	}
	
	
	/** This defalt constructor is for a single record source.
	 * 
	 */
	public Validator () {
		this.isBefore = true;
		this.splitIndex = Integer.MAX_VALUE;
	}
	
	
	public void debug () {
		System.out.println ("Validator: " + isBefore);
		System.out.println ("Validator: " + splitIndex);
		System.out.println ("Validator: " + stageRange[0] + " " + stageRange[1]);
		System.out.println ("Validator: " + masterRange[0] + " " + masterRange[1]);
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


	/* (non-Javadoc)
	 * @see com.choicemaker.cm.io.blocking.automated.offline.core.IValidator#validPair(long, long)
	 */
	public boolean validPair(long id1, long id2) {
		return inRange (id1, id2, stageRange);
	}
	
	
	/** This checks that at least one id is in range.
	 * 
	 * @param id1
	 * @param id2
	 * @param range
	 * @return boolean - true if at least 1 id is in range.
	 */
	private boolean inRange (long id1, long id2, long [] range) {
		boolean ret = false;
		
		if (range[0] <= id1 && id1 <= range[1]) ret = true;
		if (range[0] <= id2 && id2 <= range[1]) ret = true;
		
		return ret;
	}


	/** This returns true if id1 is for a staging record.
	 * 
	 * @param id1
	 * @return boolean - true if this id is from staging.
	 */
	public boolean isStaging (long id1){
		boolean ret = false;
		if (stageRange[0] <= id1 && id1 <= stageRange[1]) ret = true;
		return ret;
	}

}
