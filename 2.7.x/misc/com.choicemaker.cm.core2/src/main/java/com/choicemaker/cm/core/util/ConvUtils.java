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
package com.choicemaker.cm.core.util;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class ConvUtils {
	public static Object convertString2Object(String value, String type) {
		type = type.intern();
		if (type == "String") {
			return value;
		} else if (type == "byte") {
			return Byte.valueOf(value);
		} else if (type == "short") {
			return Short.valueOf(value);
		} else if (type == "char") {
			return new Character(value.length() > 0 ? value.charAt(0) : '\0');
		} else if (type == "int") {
			return Integer.valueOf(value);
		} else if (type == "long") {
			return Long.valueOf(value);
		} else if (type == "float") {
			return Float.valueOf(value);
		} else if (type == "double") {
			return Double.valueOf(value);
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}
}
