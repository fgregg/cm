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
package com.choicemaker.cm.gui.utils.viewer;


/**
 * Class that wraps a String in order to tag it so
 * that we can give it a different color when we go to display it in a table.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:46 $
 */
public class TypedValue {

	private String myValue;
	public boolean isValid;
	public boolean isUnique;
	public boolean isDerived;

//	private static Logger logger = Logger.getLogger(TypedValue.class);

	public TypedValue(String value, boolean isValid, boolean isUnique, boolean isDerived) {
		myValue = value;
		this.isValid = isValid;
		this.isUnique = isUnique;
		this.isDerived = isDerived;
	}

	public String toString() {
		return myValue;
	}
}
