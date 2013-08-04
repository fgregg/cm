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
 * A type that defines how the result of the batch analysis will be organized for output: 
 * <ul>
 * <li/>XML - as xml file, 
 * <li/>H3L - as a list of triplets &lt; i,j,k&gt; where i is a connected
 * (by hold or match) record set ID, j is a linked record set ID, k is a record ID. 
 * <li/>R3L - as a list of triplets &lt; i,j,k&gt; where i is a record ID,
 * j is a linked record set ID, k is a connected record set ID.
 * </ul> 
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:00:14 PM
 * @see
 */
public class AnalysisResultFormat implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -2031428476147936672L;

	private String value;

	/**
	 * Constructs a <code>ClrFormatType</code>
	 * <p> 
	 * 
	 * @param value
	 */
	public AnalysisResultFormat(String value) {
		super();
		this.value = value;
	}

	public static final AnalysisResultFormat XML = new AnalysisResultFormat("XML");
	public static final AnalysisResultFormat H3L = new AnalysisResultFormat("H3L");
	public static final AnalysisResultFormat R3L = new AnalysisResultFormat("R3L");

	/**
	 * <code>toString</code>
	 * <p>
	 * 
	 * @return
	 */
	public String toString() {
		return value;
	}

	/**
	 * <code>valueOf</code>
	 * <p> 
	 * 
	 * @param name
	 * @return
	 */
	public static AnalysisResultFormat valueOf(String name) {
		name = name.intern();
		if (XML.toString().intern() == name) {
			return XML;
		} else if (H3L.toString().intern() == name) {
			return H3L;
		} else if (R3L.toString().intern() == name) {
			return R3L;
		} else {
			throw new IllegalArgumentException(
				name + " is not a valid AnalysisResultFormat.");
		}
	}
	
	private static int 	nextOrdinal = 0;
	private final int 	ordinal = nextOrdinal++;
	private static final AnalysisResultFormat[] VALUES = {XML,H3L,R3L};
	
	Object readResolve() throws ObjectStreamException {
		return VALUES[ordinal];
	} 

}
