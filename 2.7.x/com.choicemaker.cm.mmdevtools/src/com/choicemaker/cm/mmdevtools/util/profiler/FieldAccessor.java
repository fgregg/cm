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
package com.choicemaker.cm.mmdevtools.util.profiler;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import com.choicemaker.cm.core.Descriptor;
import com.choicemaker.cm.core.Record;

/**
 * @author Owner
 *
 */
public class FieldAccessor {

	private Descriptor descriptor;
	private String fieldName;
	private int column;

	public FieldAccessor(Descriptor descriptor, int column) {
		this.descriptor = descriptor;
		this.column = column;
		
		if (column < 0 || column >= descriptor.getColumnCount()) {
			throw new IllegalArgumentException("Column " + column + " out of range");
		}
		
		this.fieldName = descriptor.getColumnDefinitions()[column].getFieldName();
	}

	public FieldAccessor(Descriptor descriptor, String fieldName) {
		this.descriptor = descriptor;
		this.fieldName = fieldName;

		this.column = this.descriptor.getColumnIndexByName(fieldName);
		if (this.column < 0) {
			throw new IllegalArgumentException("Field " + fieldName + " unknown for node " + descriptor.getName());
		}
	}

	public Descriptor getDescriptor() {
		return descriptor;
	}
	
	public String getRecordName() {
		return descriptor.getRecordName();
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public int getColumn() {
		return column;
	}

	public int getRowCount(Record r) {
		return descriptor.getRowCount(r);
	}
	
	public Object getValue(Record r, int row) {
		return descriptor.getValue(r, row, column);
	}

	public String getValueAsString(Record r, int row) {
		return descriptor.getValueAsString(r, row, column);
	}
	
	public boolean getValidity(Record r, int row) {
		return descriptor.getValidity(r, row, column);
	}
	
	public String toString() {
		return getRecordName() + '.' + fieldName;
	}

	/**
	 * Returns the Class of the given property.  Returns null if can't find it.
	 */
	public static Class getPropertyType(FieldAccessor fa) throws IntrospectionException {
		Descriptor d = fa.getDescriptor();
		Class cls = d.getHandledClass();
		String prop = fa.getFieldName();
		
		BeanInfo bi = Introspector.getBeanInfo(cls);
		PropertyDescriptor[] pds = bi.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getName().equalsIgnoreCase(prop)) {
				return pds[i].getPropertyType();
			}
		}
		
		return null;
	}

}
