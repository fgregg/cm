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
package com.choicemaker.cm.urm.base;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A type of the record object
 * <ul>
 * <li/>NONE - no data,
 * <li/>HOLDER - record holder object,
 * <li/>REF - record reference object,
 * <li/>GLOBAL_REF - record global reference object.
 * </ul>
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 2:09:30 PM
 * @see
 */
public class RecordType implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -1950748418053930284L;

	private String value;
	 
	public RecordType(String value) {
		this.value = value;
	}

	public static final RecordType NONE = new RecordType("NONE");
	public static final RecordType HOLDER = new RecordType("HOLDER");
	public static final RecordType REF = new RecordType("REF");
	public static final RecordType GLOBAL_REF = new RecordType("GLOBAL_REF");

	public String toString() {
		return value;
	}
	
	public static RecordType valueOf(String name) {
		name = name.intern();
		if (NONE.toString().intern() == name) {
			return NONE;
		} else if (HOLDER.toString().intern() == name) {
			return HOLDER;
		} else if (REF.toString().intern() == name) {
			return REF;
		} else if (GLOBAL_REF.toString().intern() == name) {
			return GLOBAL_REF;
		} else {
			throw new IllegalArgumentException(name + " is not a valid RecordType.");
		}
	} 

	private static int 	nextOrdinal = 0;
	private final int 	ordinal = nextOrdinal++;
	private static final RecordType[] VALUES = {NONE,HOLDER,REF,GLOBAL_REF};
	
	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	} 

}
