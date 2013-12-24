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

import com.choicemaker.cm.io.blocking.automated.base.BlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.base.BlockingField;
import com.choicemaker.cm.io.blocking.automated.base.BlockingValue;
import com.choicemaker.cm.io.blocking.automated.base.CountField;
import com.choicemaker.cm.io.blocking.automated.base.CountSource;

/**
 *
 * @author    
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 09:32:25 $
 */
public class CacheCountSource implements CountSource {
	private int mainTableSize;
	private CountField[] counts;

	public CacheCountSource(int mainTableSize, CountField[] counts) {
		this.mainTableSize = mainTableSize;
		this.counts = counts;
	}

	public long setCounts(BlockingConfiguration configuration, BlockingValue[] blockingValues) {
		for (int i = 0; i < blockingValues.length; ++i) {
			BlockingValue bv = blockingValues[i];
			BlockingField bf = bv.blockingField;
			int fieldNum = bf.dbField.number;
			if (fieldNum >= counts.length) {
				// conservative assumptions
				bv.tableSize = mainTableSize;
				bv.count = mainTableSize;
			} else {
				CountField f = counts[fieldNum];
				bv.tableSize = f.tableSize;
				Integer count = (Integer) f.m.get(bv.value);
				if (count != null) {
					bv.count = count.intValue();
				} else {
					bv.count = f.defaultCount;
				}
			}
		}
		return mainTableSize;
	}
}
