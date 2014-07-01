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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:54 $
 */
public class PositionMap {
	private List records;
	private HashMap map;
	
	public int getPos(Object r) {
		if(map == null) {
			map = new HashMap();
			int pos = 0;
			for (Iterator iRecords = records.iterator(); iRecords.hasNext();) {
				map.put(iRecords.next(), new Integer(pos++));
			}
		}
		return ((Integer)map.get(r)).intValue();
	}

	public void setRecords(List records) {
		this.records = records;
		this.map = null;
	}

	public void clear() {
		this.records = null;
		this.map = null;
	}

}
