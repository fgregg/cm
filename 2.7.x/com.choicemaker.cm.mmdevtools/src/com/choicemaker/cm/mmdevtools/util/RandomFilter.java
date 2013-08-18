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
package com.choicemaker.cm.mmdevtools.util;

import java.util.Random;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordData;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public class RandomFilter implements Filter {

	private static Random random;
	
	public static Random getRandom() {
		if (random == null) {
			random = new Random();
		}
		return new Random();
	}

	protected float prob;

	public RandomFilter(float probability) {
		this.prob = probability;
		if (prob > 1 || prob < 0) {
			throw new IllegalArgumentException("" + probability);
		}
	}

	public boolean satisfy(Record r) {
		return getRandom().nextFloat() < prob;
	}

	public boolean satisfy(RecordData r) {
		return getRandom().nextFloat() < prob;
	}

}
