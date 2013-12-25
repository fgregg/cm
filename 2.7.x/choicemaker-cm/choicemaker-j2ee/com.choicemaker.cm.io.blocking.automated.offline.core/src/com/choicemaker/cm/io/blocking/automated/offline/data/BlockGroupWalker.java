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
package com.choicemaker.cm.io.blocking.automated.offline.data;

/**
 * This object helps walks through the BlockGroup object.
 * 
 * If the BlockGroup has N files and interval is I, the first call returns indexes (N-I+1) to N.
 * The next call is (N-2I+1) to (N-I).  In general, the nth call produces indexes in
 * (N-nI+1) to (N-(n-1)I).
 * 
 * @author pcheung
 *
 */
public class BlockGroupWalker {
	
	private int interval;
	private int maxSize;
	private int n = 0;
	
	public BlockGroupWalker (int interval, int maxSize) {
		this.interval = interval;
		this.maxSize = maxSize;
	}
	
	/** This returns the start index and end index for this interval.
	 * The first call returns (N-I+1) and N.
	 * The second call returns (N-2I+1) and  (N-I).
	 * The nth call returns (N-nI+1) to (N-(n-1)I).
	 * 
	 * If the start index falls below 1, it gets set to 1.
	 * 
	 * If the end index falls below 1, start index gets set to 0;
	 * 
	 * @return int []
	 */
	public int[] getNextPair () {
		n ++;
		
		int [] ret = new int[2];
		
		ret[0] = maxSize - n*interval + 1;
		ret[1] = maxSize - (n-1)*interval;
		
		if (ret[0] < 1) ret[0] = 1;
		
		//this happens if getNextPair is called too many times
		if (ret[1] < 1) ret[0] = 0;
		
		return ret;
	}

}
