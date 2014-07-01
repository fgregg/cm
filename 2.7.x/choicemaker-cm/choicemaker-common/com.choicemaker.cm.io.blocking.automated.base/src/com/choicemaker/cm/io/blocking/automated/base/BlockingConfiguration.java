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
package com.choicemaker.cm.io.blocking.automated.base;

import java.util.ArrayList;

import com.choicemaker.cm.core.Record;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public abstract class BlockingConfiguration {
	protected String name;
	public DbTable[] dbTables;
	public DbField[] dbFields;
	public BlockingField[] blockingFields;

	private ArrayList[] values;

	public abstract BlockingValue[] createBlockingValues(Record q);

	protected void init(int numFields) {
		values = new ArrayList[numFields];
		for (int i = 0; i < numFields; ++i)
			values[i] = new ArrayList();
	}

	protected BlockingValue[] unionValues() {
		int size = 0;
		for (int i = 0; i < values.length; ++i) {
			size += values[i].size();
		}
		BlockingValue[] res = new BlockingValue[size];
		int out = 0;
		for (int i = 0; i < values.length; ++i) {
			ArrayList l = values[i];
			int s = l.size();
			for (int j = 0; j < s; ++j) {
				res[out++] = (BlockingValue) l.get(j);
			}
		}
		values = null;
		return res;
	}

	protected BlockingValue addField(int index, String value, BlockingValue[] thisBase) {
		BlockingValue res;
		value = value.intern();
		ArrayList l = values[index];
		int size = l.size();
		int i = 0;
		while (i < size && ((BlockingValue) l.get(i)).value != value) {
			++i;
		}
		if (i == size) {
			if (thisBase == null) {
				res = new BlockingValue(blockingFields[index], value);
			} else {
				res = new BlockingValue(blockingFields[index], value, new BlockingValue[][] { thisBase });
			}
			l.add(res);
		} else {
			res = (BlockingValue) l.get(i);
			if (thisBase != null) {
				int len = res.base.length;
				BlockingValue[][] newBase = new BlockingValue[len + 1][];
				System.arraycopy(res.base, 0, newBase, 0, len);
				newBase[len] = thisBase;
				res.base = newBase;
			}
		}
		

		return res;
	}

	public String getName() {
		return name;
	}
	
	public static class DbConfiguration {
		public String name;
		public QueryField[] qfs;
		public DbTable[] dbts;
		public DbField[] dbfs;
		public BlockingField[] bfs;
		
		public DbConfiguration(String name, QueryField[] qfs, DbTable[] dbts, DbField[] dbfs, BlockingField[] bfs) {
			this.name = name;
			this.qfs = qfs;
			this.dbts = dbts;
			this.dbfs = dbfs;
			this.bfs = bfs;
		}	
	}
}
