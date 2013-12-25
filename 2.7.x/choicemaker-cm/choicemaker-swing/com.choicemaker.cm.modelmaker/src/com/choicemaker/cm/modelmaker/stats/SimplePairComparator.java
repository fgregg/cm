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
package com.choicemaker.cm.modelmaker.stats;

import java.util.Comparator;

import com.choicemaker.cm.core.IMarkedRecordPair;
import com.choicemaker.cm.core.MutableMarkedRecordPair;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 12:26:37 $
 */
public class SimplePairComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		return comparePairs((MutableMarkedRecordPair) o1, (MutableMarkedRecordPair) o2);
	}

	protected final int comparePairs(IMarkedRecordPair p1, IMarkedRecordPair p2) {
		int d = p1.getMarkedDecision().compareTo(p2.getMarkedDecision());
		if (d == 0) {
			if (p1.getProbability() < p2.getProbability()) {
				return -1;
			} else if (p1.getProbability() > p2.getProbability()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return -d;
		}
	}
}
