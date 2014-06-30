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

import java.io.Serializable;

/**
 * ClueSet types
 *
 * @author   rphall   
 * @version  $Revision: 1.2 $ $Date: 2010/03/27 21:29:47 $
 */
public class ClueSetType implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -5461429990823251738L;

	private final static int INT_TAG = 7;

	private final static int BOOLEAN_TAG = 11;

	/** Clues return integer values */
	public static final ClueSetType INT = new ClueSetType(INT_TAG);

	/** Clues return boolean values */
	public static final ClueSetType BOOLEAN = new ClueSetType(BOOLEAN_TAG);
	
	private final int tag;

	private ClueSetType(int tag) {
		this.tag = tag;
		// Preconditions
		if (tag != INT_TAG && tag != BOOLEAN_TAG) {
			throw new IllegalArgumentException("invalid tag value = '" + tag + "'");
		}
	}

	public String toString() {

		String retVal = null;
		switch (this.tag) {
			case INT_TAG :
				retVal = "int";
				break;
			case BOOLEAN_TAG :
				retVal = "boolean";
				break;
			default :
				throw new IllegalStateException("bad type tag '" + tag + "'");
		}

		return retVal;
	} // toString()

}

 
