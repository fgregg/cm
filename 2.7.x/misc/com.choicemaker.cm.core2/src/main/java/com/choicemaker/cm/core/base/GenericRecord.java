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

import java.lang.reflect.Array;

import com.choicemaker.cm.core.BaseRecord;
import com.choicemaker.cm.core.DerivedSource;
import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;

/**
 * TODO Remove this class
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/10/28 18:38:16 $
 * @deprecated Never used
 */
public class GenericRecord implements Record, Cloneable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String[] values;
	private boolean[] validity;
	private GenericRecord[][] children;
	
	public Object clone() {
		return new GenericRecord(this);
	}
	
	public GenericRecord(GenericRecord from) {
		id = from.id;
		values = (String[])from.values.clone();
		validity = (boolean[])from.validity.clone();
		children = new GenericRecord[from.children.length][];
		for (int i = 0; i < children.length; i++) {
			children[i] = new GenericRecord[from.children[i].length];
			for (int j = 0; j < children[i].length; j++) {
				children[i][j] = new GenericRecord(from.children[i][j]);
			}
		}
	}

	public GenericRecord(Record r, Descriptor d) {
		this(r, d, 0);
		Object o = r.getId();
		id = o != null ? o.toString() : null;
	}

	public GenericRecord(Descriptor d) {
		this(null, d, 0);
		id = "0";
	}

	private GenericRecord(Record r, Descriptor d, int row) {
		int numCols = d.getColumnCount();
		values = new String[numCols];
		validity = new boolean[numCols];
		for (int i = 0; i < numCols; ++i) {
			values[i] = r == null ? "" : d.getValueAsString(r, row, i);
			validity[i] = r == null ? true : d.getValidity(r, row, i);
		}
		Descriptor[] childDescriptors = d.getChildren();
		children = new GenericRecord[childDescriptors.length][];
		for (int i = 0; i < childDescriptors.length; i++) {
			Descriptor cd = childDescriptors[i];
			int numRows = r == null ? 1 : cd.getRowCount(r);
			children[i] = new GenericRecord[numRows];
			for (int j = 0; j < numRows; ++j) {
				children[i][j] = new GenericRecord(r, cd, j);
			}
		}
	}

	public Record toSpecificRecord(Descriptor d) {
		try {
			GenericDescriptor gd = new GenericDescriptor(d);
			Record r = (Record)getInstance(d);
			toSpecificRecord(r, r, gd, d, 0);
			r.computeValidityAndDerived(DerivedSource.valueOf("generic"));
			return r;
		} catch (Exception ex) {
			throw new IllegalStateException(ex.toString());
		}
	}

	private void toSpecificRecord(Record o, BaseRecord r, GenericDescriptor gd, Descriptor d, int row) throws Exception {
		int numCols = d.getColumnCount();
		for(int i = 0; i < numCols; ++i) {
			d.setValue(o, row, i, gd.getValueAsString(this, row, i));
		}
		Descriptor[] ds = d.getChildren();
		GenericDescriptor[] gds = (GenericDescriptor[])gd.getChildren();
		for (int i = 0; i < ds.length; i++) {
			Descriptor td = ds[i];
			GenericDescriptor tgd = gds[i];
			int numRows = tgd.getRowCount(this);
			BaseRecord[] brs = (BaseRecord[])Array.newInstance(td.getHandledClass(), numRows);
			r.getClass().getField(td.getRecordName()).set(r, brs);
			for(int j = 0; j < numRows; ++j) {
				BaseRecord rr = getInstance(td);
				brs[j] = rr;
				toSpecificRecord(o, rr, tgd, td, j);
			}
		}
	}
	
	private BaseRecord getInstance(Descriptor d) throws Exception {
		Class baseClass = d.getHandledClass();
		return (BaseRecord) baseClass.getMethod("instance", new Class[0]).invoke(baseClass, new Object[0]);
	}

	public Comparable getId() {
		return id;
	}

	public void computeValidityAndDerived() {
		throw new UnsupportedOperationException();
	}

	public DerivedSource getDerivedSource() {
		throw new UnsupportedOperationException();
	}

	public void computeValidityAndDerived(DerivedSource src) {
		throw new UnsupportedOperationException();
	}

	public void resetValidityAndDerived(DerivedSource src) {
		throw new UnsupportedOperationException();
	}

	public GenericRecord[][] getChildren() {
		return children;
	}

	public String[] getValues() {
		return values;
	}

	public void setChildren(GenericRecord[][] records) {
		children = records;
	}

	public void setId(String string) {
		id = string;
	}

	public void setValues(String[] strings) {
		values = strings;
	}

	public boolean[] getValidity() {
		return validity;
	}

	public void setValidity(boolean[] bs) {
		validity = bs;
	}
}
