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
package com.choicemaker.util3;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Type-safe enum for comparison results on file pairs.
 * This class will be replaced with an enum, FILE_CONTENT_COMPARISON, when
 * the com.choicemaker.util package migrates to Java 1.5 or later.
 * 
 * @author rphall
 */

public final class FILE_CONTENT_COMPARISON0 implements Serializable {

	static final long serialVersionUID = 1;
	
	public static final FILE_CONTENT_COMPARISON0 ONLY_IN_PATH1 =
		new FILE_CONTENT_COMPARISON0("ONLY_IN_PATH1");

	public static final FILE_CONTENT_COMPARISON0 ONLY_IN_PATH2 =
		new FILE_CONTENT_COMPARISON0("ONLY_IN_PATH2");

	public static final FILE_CONTENT_COMPARISON0 DIFFERENT_CONTENT =
		new FILE_CONTENT_COMPARISON0("DIFFERENT_CONTENT");

	public static final FILE_CONTENT_COMPARISON0 SAME_CONTENT =
			new FILE_CONTENT_COMPARISON0("SAME_CONTENT");

	public static final FILE_CONTENT_COMPARISON0 UNREACHABLE_PATH1 =
			new FILE_CONTENT_COMPARISON0("UNREACHABLE_PATH1");

	public static final FILE_CONTENT_COMPARISON0 UNREACHABLE_PATH2 =
			new FILE_CONTENT_COMPARISON0("UNREACHABLE_PATH2");
	
	public static final FILE_CONTENT_COMPARISON0[] values() {
		return new FILE_CONTENT_COMPARISON0[] {
				ONLY_IN_PATH1, ONLY_IN_PATH2, DIFFERENT_CONTENT, SAME_CONTENT,
				UNREACHABLE_PATH1, UNREACHABLE_PATH2
		};
	}
	
	private static final Map<String, FILE_CONTENT_COMPARISON0> instances  = new HashMap<>(); {
		instances.put(ONLY_IN_PATH1.toString(), ONLY_IN_PATH1);
		instances.put(ONLY_IN_PATH2.toString(), ONLY_IN_PATH2);
		instances.put(DIFFERENT_CONTENT.toString(), DIFFERENT_CONTENT);
		instances.put(SAME_CONTENT.toString(), SAME_CONTENT);
		instances.put(UNREACHABLE_PATH1.toString(), UNREACHABLE_PATH1);
		instances.put(UNREACHABLE_PATH2.toString(), UNREACHABLE_PATH2);
	}

	private final String name;

	protected FILE_CONTENT_COMPARISON0(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * Returns the corresponding <code>FILE_CONTENT_COMPARISON0</code>.
	 * 
	 * @return The corresponding <code>FILE_CONTENT_COMPARISON0</code>.
	 * @throws IllegalArgumentException
	 *             if <code>name</code> is not a valid name.
	 */
	public static FILE_CONTENT_COMPARISON0 valueOf(String name) {
		FILE_CONTENT_COMPARISON0 retVal = instances.get(name);
		if (retVal == null) {
			throw new IllegalArgumentException("invalid name: '" + name + "'");
		}
		return retVal;
	}

	private Object readResolve() throws ObjectStreamException {
		return valueOf(name);
	}

	public boolean equals(Object o) {
		FILE_CONTENT_COMPARISON0 d = (FILE_CONTENT_COMPARISON0) o;
		boolean retVal = this.name.equals(d.name);
		return retVal;
	}

	public int hashCode() {
		return this.name.hashCode();
	}

}
