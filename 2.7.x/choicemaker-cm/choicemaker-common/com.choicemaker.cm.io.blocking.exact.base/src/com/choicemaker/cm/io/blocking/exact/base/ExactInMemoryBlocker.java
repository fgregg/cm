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
package com.choicemaker.cm.io.blocking.exact.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.RecordBinder;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:54 $
 */
public abstract class ExactInMemoryBlocker {
	protected static final List EMPTY_LIST = new ArrayList(0);
	private static final HashSet EMPTY_SET = new HashSet();

	private Map keyToRecordSet = new HashMap();
	private PositionMap positionMap;
	
	public ExactInMemoryBlocker(PositionMap positionMap) {
		this.positionMap = positionMap;
	}
	
	public void init(List targetRecords) {
		keyToRecordSet = new HashMap();
		for (Iterator iTargetRecords = targetRecords.iterator(); iTargetRecords.hasNext();) {
			Object r = iTargetRecords.next();
			List keys = getTargetKeys(r);
			for (int i = keys.size() - 1; i >= 0; i--) {
				Object k = keys.get(i);
				Set s = (Set) keyToRecordSet.get(k);
				if (s == null) {
					s = new HashSet(2);
					keyToRecordSet.put(k, s);
				}
				s.add(r);
			}
		}
	}

	public void clear() {
		keyToRecordSet = new HashMap();
	}

	public RecordSource block(Record q, HashSet res) {
		List keys = getSourceKeys(q);
		for (int i = keys.size() - 1; i >= 0; i--) {
			HashSet s = (HashSet) keyToRecordSet.get(keys.get(i));
			if (s != null) {
				res.addAll(s);
			}
		}
		return new RecordBinder(res);
	}

	public RecordSource block(Record q, HashSet res, int start) {
		List keys = getSourceKeys(q);
		for (int i = keys.size() - 1; i >= 0; i--) {
			HashSet s = (HashSet) keyToRecordSet.get(keys.get(i));
			if (s != null) {
				for (Iterator iS = s.iterator(); iS.hasNext();) {
					Object o = iS.next();
					if(positionMap.getPos(o) >= start) {
						res.add(o);
					}
				}
				
			}
		}
		return new RecordBinder(res);
	}

	protected abstract List getSourceKeys(Object o);

	protected abstract List getTargetKeys(Object o);
}
