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
 * This object stores key information on a block for make blocks distinct.
 * 
 * @deprecated
 * 
 * @author pcheung
 *
 */
@Deprecated
public class BlockSummary {

	public int count; // number of entries in a block
	public long sum; // sum of the record ID's in a block

	@Override
	public int hashCode() {
		return (int) (sum ^ (sum >>> 32) ^ count);
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;

		if (o.getClass() == BlockSummary.class) {
			BlockSummary p = (BlockSummary) o;
			if ((count == p.count) && (sum == p.sum))
				ret = true;
		}

		return ret;
	}
}
