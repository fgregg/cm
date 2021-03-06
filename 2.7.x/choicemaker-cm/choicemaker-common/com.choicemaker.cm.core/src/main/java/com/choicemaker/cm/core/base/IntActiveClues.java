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

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.1 $ $Date: 2010/01/20 15:05:04 $
 * TODO no array elements for rules
 */
public class IntActiveClues extends ActiveClues {
	public int[] values;
	
	public IntActiveClues(int size) {
		values = new int[size];
	}

	/**
	 * @see com.choicemaker.cm.core.base.ActiveClues#size()
	 */
	public int size() {
		return values.length;
	}

}
