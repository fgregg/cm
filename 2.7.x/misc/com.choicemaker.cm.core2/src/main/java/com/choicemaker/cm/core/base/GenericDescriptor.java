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
package com.choicemaker.cm.core.base;

import java.util.HashMap;

import com.choicemaker.cm.core.ColumnDefinition;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;

/**
 * TODO: remove this class
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/10/28 18:38:16 $
 * @deprecated Never used
 */
public class GenericDescriptor implements Descriptor {
	private static final long serialVersionUID = 1L;

	public static final DerivedSource SOURCE = DerivedSource.valueOf("gui");
	
	private int[] path;
	private GenericDescriptor[] children;
	private ColumnDefinition[] columnDefinitions;
	private String name;
	private String recordName;
	private boolean stackable;
	private boolean[] editable;
	private HashMap m;
	
	public GenericDescriptor(Descriptor d) {
		this(d, new int[0]);
	}
	
	private GenericDescriptor(Descriptor d, int[] path) {
		this.path = path;
		int[] newPath = new int[path.length + 1];
		System.arraycopy(path, 0, newPath, 0, path.length);
		Descriptor[] childDescriptors = d.getChildren();
		this.children = new GenericDescriptor[childDescriptors.length];
		for (int i = 0; i < childDescriptors.length; i++) {
			newPath[newPath.length - 1] = i;
			children[i] = new GenericDescriptor(childDescriptors[i], newPath);
			newPath = (int[])newPath.clone();
		} 
		this.columnDefinitions = d.getColumnDefinitions();
		this.name = d.getName();
		this.recordName = d.getRecordName();
		this.stackable = d.isStackable();
		this.editable = d.getEditable(SOURCE);
	}

	public ColumnDefinition[] getColumnDefinitions() {
		return columnDefinitions;
	}

	public Descriptor[] getChildren() {
		return children;
	}

	public Record[][] getChildRecords(Record ri) {
		return null;
	}

	private GenericRecord getRecord(Record r, int row) {
		if(!stackable) {
			return (GenericRecord)r;
		} else {
			return (GenericRecord)getRecord((GenericRecord)r, row, 0);
		}	
	}
	
	private Object getRecord(GenericRecord r, int row, int pathIndex) {
		GenericRecord[] ts = r.getChildren()[path[pathIndex]];
		if(pathIndex == path.length - 1) {
			if(ts.length > row) {
				return ts[row];
			} else {
				return new Integer(row - ts.length);
			}
		} else {
			for (int i = 0; i < ts.length; i++) {
				Object o = getRecord(ts[i], row, pathIndex + 1);
				if(o instanceof Record) {
					return o;
				}
				row = ((Integer)o).intValue();
			}
			throw new IndexOutOfBoundsException();
		}
	}
	
	private Object[] getEnclosingRecord(GenericRecord r, int row, int pathIndex) {
		if(pathIndex == path.length - 1) {
			return new Object[] {r, new Integer(row)};
		} else {
			GenericRecord[] ts = r.getChildren()[path[pathIndex]];
			if(pathIndex == path.length - 2) {
				int len = ts.length;
				if(len > row) {
					return new Object[] {r, new Integer(row)};
				} else {
					return new Object[] { new Integer(len - row) };
				}
			} else {
				for (int i = 0; i < ts.length; i++) {
					Object[] o = getEnclosingRecord(ts[i], row, pathIndex + 1);
					if(o.length == 2) {
						return o;
					} else {
						row = ((Integer)o[0]).intValue();
					}
				}
				throw new IndexOutOfBoundsException();
			}
		}
	}
	
	public String getValueAsString(Record r, int row, int col) {
		String val = getRecord(r, row).getValues()[col];
		return val;
	}
	
	public Object getValue(Record r, int row, int col) {
		return getValueAsString(r, row, col);
	}

	public boolean getValidity(Record r, int row, int col) {
		return getRecord(r, row).getValidity()[col];
	}

	public boolean setValue(Record r, int row, int col, String value) {
		getRecord(r, row).getValues()[col] = value;
		return true;
	}

	public void deleteRow(Record r, int row) {
		if(!stackable) {
			throw new UnsupportedOperationException("Root node not stackable.");
		}
		GenericRecord q = (GenericRecord)r;
		Object[] pos = getEnclosingRecord(q, row, 0);
		int pe = path[path.length - 1];
		GenericRecord[][] pc = ((GenericRecord)pos[0]).getChildren();
		GenericRecord[] o = pc[pe];
		GenericRecord[] n = new GenericRecord[o.length - 1];
		int pi = ((Integer)pos[1]).intValue();
		System.arraycopy(o, 0, n, 0, pi);
		System.arraycopy(o, pi + 1, n, pi, n.length - pi);
		pc[pe] = n;
	}

	public void addRow(int position, boolean above, Record r) {
		if(!stackable) {
			throw new UnsupportedOperationException("Root node not stackable.");
		}
		GenericRecord q = (GenericRecord)r;
		Object[] pos = getEnclosingRecord(q, position, 0);
		int pe = path[path.length - 1];
		GenericRecord[][] pc = ((GenericRecord)pos[0]).getChildren();
		GenericRecord[] o = pc[pe];
		GenericRecord[] n = new GenericRecord[o.length + 1];
		int pi = ((Integer)pos[1]).intValue();
		if(!above) {
			++pi;
		}
		System.arraycopy(o, 0, n, 0, pi);
		n[pi] = (GenericRecord)r;
		System.arraycopy(o, pi, n, pi + 1, o.length - pi);
		pc[pe] = n;
	}

	public int getColumnCount() {
		return columnDefinitions.length;
	}

	public int getRowCount(Record r) {
		if(!stackable) {
			return 1;
		} else {
			return getRowCount((GenericRecord)r, 0);
		}
	}

	private int getRowCount(GenericRecord r, int pathIndex) {
		if(pathIndex == path.length - 1) {
			return r.getChildren()[path[pathIndex]].length;
		} else {
			GenericRecord[] ts = r.getChildren()[path[pathIndex]];
			int res = 0;
			for (int i = 0; i < ts.length; i++) {
				res += getRowCount(ts[i], pathIndex + 1);
			}
			return res;
		}
	}

	public String getName() {
		return name;
	}

	public String getRecordName() {
		return recordName;
	}

	public boolean isStackable() {
		return stackable;
	}

	public int getColumnIndexByName(String name) {
		if (m == null) {
			m = new HashMap(columnDefinitions.length);
			for (int i = 0; i < columnDefinitions.length; ++i) {
				m.put(columnDefinitions[i].getFieldName(), new Integer(i));
			}
		}
		Object o = m.get(name);
		if (o == null) {
			return -1;
		} else {
			return ((Integer) o).intValue();
		}
	}

	public boolean[] getEditable(DerivedSource src) {
		return editable;
	}

	public Class getHandledClass() {
		return GenericRecord.class;
	}
}
