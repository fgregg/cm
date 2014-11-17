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
package com.choicemaker.cm.io.blocking.automated.util;

import java.util.Arrays;
import java.util.Comparator;

import com.choicemaker.cm.io.blocking.automated.IBlockingSet;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.BlockingSet;

/**
 * Compares two BlockingSets. The BlockingSet with the fewest BlockingValues
 * is ordered first. If both BlockingSets have the same number of BlockingValues,
 * then the BlockingValues from both sets are retrieved to a SortedList
 * (using the comparator defined by the BlockingValue class) and the lists
 * are compared.
 */
public class BlockingSetComparator implements Comparator<BlockingSet> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(BlockingSet o1, BlockingSet o2) {
		int retVal = 0;
		if (o1 != null && o2 != null) {
			IBlockingValue[] b1 = ((IBlockingSet) o1).getBlockingValues();
			IBlockingValue[] b2 = ((IBlockingSet) o2).getBlockingValues();
			if (b1.length < b2.length) {
				retVal = -1;
			} else if (b1.length > b2.length) {
				retVal = +1;
			} else {
				Arrays.sort(b1);
				Arrays.sort(b2);
				for (int i=0; retVal == 0 && i<b1.length; i++) {
					retVal = b1[i].compareTo(b2[i]);
				}
			}
		} else if (o1 != null) {
			retVal = -1;
		} else if (o2 != null) {
			retVal = 1;
		}
		
		return retVal;
	}

}

