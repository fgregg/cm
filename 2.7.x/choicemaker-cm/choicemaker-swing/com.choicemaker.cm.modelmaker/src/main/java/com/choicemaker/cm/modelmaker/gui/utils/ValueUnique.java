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
package com.choicemaker.cm.modelmaker.gui.utils;

import java.awt.Color;

/**
 * Class that wraps a String in order to tag it so
 * that we can give it a different color when we go to display it in a table.
 * 
 * @author S. Yoakum-Stover
 * @version $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:03:10 $
 */
public class ValueUnique {

	private String myValue;
	public Color color = new Color(1f, 0f, 0f);


	public ValueUnique(String value) {
		myValue = value;
	}

	public String toString() {
		return myValue;
	}
}
