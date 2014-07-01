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
package com.choicemaker.cm.core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;

/**
 * @author ajwinkel
 *
 */
public class RecordMap {

	private Map map = new HashMap();

	public RecordMap() { }

	public RecordMap(List records) {
		for (int i = 0; i < records.size(); i++) {
			addRecord((Record)records.get(i));
		}
	}

	public RecordMap(RecordSource rs) throws IOException {
		rs.open();
		while (rs.hasNext()) {
			addRecord(rs.getNext());
		}
		rs.close();
	}

	public void addRecord(Record r) {
		Comparable id = r.getId();
		if (id == null) {
			throw new IllegalStateException("Attempt to add a record with a null ID");
		}

		// 2014-04-24 rphall: Commented out unused local variable.
//		String idString = id.toString();
		map.put(id, r);
	}

	public boolean hasRecord(Comparable id) {
		// 2014-04-24 rphall: Commented out unused local variable.
//		String idString = id.toString();
		return map.containsKey(id);
	}

	public Record getRecord(Comparable id) {
		String idString = id.toString();
		return (Record) map.get(idString);
	}

}
