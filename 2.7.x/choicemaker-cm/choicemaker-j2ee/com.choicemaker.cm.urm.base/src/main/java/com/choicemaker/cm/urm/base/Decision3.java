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

import java.io.Serializable;

/**
 * Match, hold (potential match), or differ decision.
 * <p>  
 *
 * @author emoussikaev
 * @version Revision: 2.5  Date: Nov 1, 2005 12:16:24 PM
 * @see
 */
public class Decision3 implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = 7850040340990851496L;

	private String value;
	
	/**
	 * Constructs a <code>Decision3</code> using the string value.
	 * <p> 
	 * 
	 * @param value
	 */
	public Decision3(String value) {
		this.value = value;
	}

	public static final Decision3 DIFFER = new Decision3("differ");
	public static final Decision3 MATCH = new Decision3("match");
	public static final Decision3 HOLD = new Decision3("hold");
	
	/**
	 * Returns the string representation of the <code>Decision3</code>. 
	 *
	 * @return  The string representation of the <code>Decision</code>.
	 */
	public String toString() {
		return this.value;
	}

	/**
	 * Returns <code>Decision3</code> corresponding to the <code>name</code>.
	 * <p> 
	 * 
	 * @param	name A string represntation of a code>Decision3</code>  
	 * @return	The corresponding <code>Decision3</code>.
	 * @throws	IllegalArgumentException if <code>name</code> doesn't represent a valid decision.
	 */
	public static Decision3 valueOf(String name) {
		name = name.intern();
		if (DIFFER.toString().intern() == name) {
			return DIFFER;
		} else if (HOLD.toString().intern() == name) {
			return HOLD;
		} else if (MATCH.toString().intern() == name) {
			return MATCH;
		} else {
			throw new IllegalArgumentException(name + " is not a valid Decision3.");
		}
	}
	
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Decision3 other = (Decision3) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
 	
	/**
	 * Obsolete method for {@link #equals(Object)}. Used for testing only.
	 * @deprecated
	 */
	public boolean equals_00(Object o){
		if (o instanceof Decision3) {
			Decision3 d = (Decision3) o;
			return this.value.equals(d.value);
		} else return false;
	}
	
}
