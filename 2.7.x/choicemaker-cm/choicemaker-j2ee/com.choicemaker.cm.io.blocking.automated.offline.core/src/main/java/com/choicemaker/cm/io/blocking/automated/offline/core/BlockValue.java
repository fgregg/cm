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
package com.choicemaker.cm.io.blocking.automated.offline.core;

import java.io.Serializable;

/**
 * This object represents a Blocking Value. It stores the column ID and Column
 * Value.
 *
 * @author pcheung
 *
 */
public class BlockValue implements Serializable {

	/* As of 2010-03-10 */
	static final long serialVersionUID = -561020089283323100L;

	int id;
	int value;

	/*
	 * public BlockValue (int columnID, int columnValue) { id = columnID; value
	 * = columnValue; }
	 * 
	 * public void setColumnID (int i) { id = i; }
	 * 
	 * public void setColumnValue (int val) { value = val; }
	 * 
	 * public int getColumnID () { return id; }
	 * 
	 * public int getCloumnValue () { return value; }
	 * 
	 * public boolean equals (BlockValue bv2) { if ( id == bv2.getColumnID() &&
	 * value == bv2.getCloumnValue() ) return true; else return false; }
	 */

}
