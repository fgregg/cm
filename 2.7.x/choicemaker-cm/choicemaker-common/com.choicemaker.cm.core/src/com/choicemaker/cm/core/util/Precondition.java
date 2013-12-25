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
 * @author rphall
 * @version $Revision: 1.1 $ $Date: 2010/03/24 17:11:35 $
 */
public class Precondition {

	/**
	 * Default messsage about a false boolean argument.
	 * This message should move to a resource bundle.
	 */
	public static final String MSG_FALSE_BOOLEAN = "precondition violated";

	/**
	 * Default messsage about invalid null method argument.
	 * This message should move to a resource bundle.
	 */
	public static final String MSG_NULL_OBJECT = "null argument";

	public static void assertBoolean(boolean b) {
		if (!b) {
			throw new IllegalArgumentException(MSG_FALSE_BOOLEAN);
		}
	}

	public static void assertBoolean(String msg, boolean b) {
		if (!b) {
			msg = msg == null ? Precondition.MSG_FALSE_BOOLEAN : msg;
			throw new IllegalArgumentException(msg);
		}
	}

	public static void assertNonEmptyString(String s)
		throws IllegalArgumentException {
		if (!StringUtils.nonEmptyString(s)) {
			throw new IllegalArgumentException("blank or null String value");
		}
	}

	public static void assertNonNullArgument(Object o) {
		assertNonNullArgument(Precondition.MSG_NULL_OBJECT, o);
	}

	public static void assertNonNullArgument(String msg, Object o) {
		if (o == null) {
			msg = msg == null ? Precondition.MSG_NULL_OBJECT : msg;
			throw new IllegalArgumentException(msg);
		}
	}

	private Precondition() {
	}

}
