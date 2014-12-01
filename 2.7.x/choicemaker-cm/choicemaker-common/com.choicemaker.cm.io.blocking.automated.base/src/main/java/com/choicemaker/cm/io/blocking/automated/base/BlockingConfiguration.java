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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.IBlockingField;
import com.choicemaker.cm.io.blocking.automated.IBlockingValue;
import com.choicemaker.cm.io.blocking.automated.IDbField;
import com.choicemaker.cm.io.blocking.automated.IDbTable;
import com.choicemaker.cm.io.blocking.automated.IQueryField;

/**
 *
 * @author    
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:47 $
 */
public abstract class BlockingConfiguration implements IBlockingConfiguration,
	Serializable {
	
	private static final long serialVersionUID = 271;

	public String name;
	public IDbTable[] dbTables;
	public IDbField[] dbFields;
	public IBlockingField[] blockingFields;

	private ArrayList<IBlockingValue>[] values;

	public abstract IBlockingValue[] createBlockingValues(Record q);

	@SuppressWarnings("unchecked")
	protected void init(int numFields) {
		values = new ArrayList[numFields];
		for (int i = 0; i < numFields; ++i)
			values[i] = new ArrayList<>();
	}

	protected IBlockingValue[] unionValues() {
		int size = 0;
		for (int i = 0; i < values.length; ++i) {
			size += values[i].size();
		}
		IBlockingValue[] res = new IBlockingValue[size];
		int out = 0;
		for (int i = 0; i < values.length; ++i) {
			ArrayList<IBlockingValue> l = values[i];
			int s = l.size();
			for (int j = 0; j < s; ++j) {
				res[out++] = (IBlockingValue) l.get(j);
			}
		}
		values = null;
		return res;
	}

	protected IBlockingValue addField(int index, String value,
			IBlockingValue[] thisBase) {
		BlockingValue res;
		value = value.intern();
		ArrayList<IBlockingValue> l = values[index];
		int size = l.size();
		int i = 0;
		while (i < size && ((IBlockingValue) l.get(i)).getValue() != value) {
			++i;
		}
		if (i == size) {
			if (thisBase == null) {
				res = new BlockingValue(getBlockingFields()[index], value);
			} else {
				res =
					new BlockingValue(getBlockingFields()[index], value,
							new IBlockingValue[][] { thisBase });
			}
			l.add(res);
		} else {
			res = (BlockingValue) l.get(i);
			if (thisBase != null) {
				int len = res.getBase().length;
				IBlockingValue[][] newBase = new IBlockingValue[len + 1][];
				System.arraycopy(res.getBase(), 0, newBase, 0, len);
				newBase[len] = thisBase;
				res.setBase(newBase);
			}
		}		

		return res;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public IDbTable[] getDbTables() {
		return dbTables;
	}

	@Override
	public IDbField[] getDbFields() {
		return dbFields;
	}

	@Override
	public IBlockingField[] getBlockingFields() {
		return blockingFields;
	}

	@Override
	public String toString() {
		return "BlockingConfiguration [name=" + name + ", dbTables="
				+ Arrays.toString(dbTables) + ", dbFields="
				+ Arrays.toString(dbFields) + ", blockingFields="
				+ Arrays.toString(blockingFields) + ", values="
				+ Arrays.toString(values) + "]";
	}

	public static class DbConfiguration {
		public String name;
		public IQueryField[] qfs;
		public IDbTable[] dbts;
		public IDbField[] dbfs;
		public IBlockingField[] bfs;
		
		public DbConfiguration(String name, IQueryField[] qfs, IDbTable[] dbts, IDbField[] dbfs, IBlockingField[] bfs) {
			this.name = name;
			this.qfs = qfs;
			this.dbts = dbts;
			this.dbfs = dbfs;
			this.bfs = bfs;
		}

		@Override
		public String toString() {
			return "DbConfiguration [name=" + name + ", qfs="
					+ Arrays.toString(qfs) + ", dbts=" + Arrays.toString(dbts)
					+ ", dbfs=" + Arrays.toString(dbfs) + ", bfs="
					+ Arrays.toString(bfs) + "]";
		}	
	}
}
