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
package com.choicemaker.cm.io.blocking.automated.cachecount;

import com.choicemaker.cm.io.blocking.automated.CountSource;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.ICountField;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:32:25 $
 */
public class CacheCountSource implements CountSource {
	private int mainTableSize;
	private ICountField[] counts;

	public CacheCountSource(int mainTableSize, ICountField[] counts) {
		this.mainTableSize = mainTableSize;
		this.counts = counts;
	}

	public long setCounts(IBlockingConfiguration configuration, IBlockingValue[] blockingValues) {
		for (int i = 0; i < blockingValues.length; ++i) {
			IBlockingValue bv = blockingValues[i];
			IBlockingField bf = bv.getBlockingField();
			int fieldNum = bf.getDbField().getNumber();
			if (fieldNum >= counts.length) {
				// conservative assumptions
				bv.setTableSize(mainTableSize);
				bv.setCount(mainTableSize);
			} else {
				ICountField f = counts[fieldNum];
				bv.setTableSize(f.getTableSize());
				Integer count = f.getCountForValue(bv.getValue());
				if (count != null) {
					bv.setCount(count.intValue());
				} else {
					bv.setCount(f.getDefaultCount());
				}
			}
		}
		return mainTableSize;
	}
}
