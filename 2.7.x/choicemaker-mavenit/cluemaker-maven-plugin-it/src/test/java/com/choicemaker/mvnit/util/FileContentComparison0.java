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
package com.choicemaker.mvnit.util;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Java 1.4, type-safe enum for comparison results on file pairs. This class
 * will be replaced with a proper enum, FileContentComparison, and moved to the
 * com.choicemaker.util package when that package migrates to Java 1.7 or later.
 * 
 * @author rphall
 */

public final class FileContentComparison0 implements Serializable {

	static final long serialVersionUID = 1;

	public static final FileContentComparison0 ONLY_IN_PATH1 =
		new FileContentComparison0("ONLY_IN_PATH1");

	public static final FileContentComparison0 ONLY_IN_PATH2 =
		new FileContentComparison0("ONLY_IN_PATH2");

	public static final FileContentComparison0 DIFFERENT_CONTENT =
		new FileContentComparison0("DIFFERENT_CONTENT");

	public static final FileContentComparison0 SAME_CONTENT =
		new FileContentComparison0("SAME_CONTENT");

	public static final FileContentComparison0 UNREACHABLE_PATH1 =
		new FileContentComparison0("UNREACHABLE_PATH1");

	public static final FileContentComparison0 UNREACHABLE_PATH2 =
		new FileContentComparison0("UNREACHABLE_PATH2");

	public static final FileContentComparison0[] values() {
		return new FileContentComparison0[] {
				ONLY_IN_PATH1, ONLY_IN_PATH2, DIFFERENT_CONTENT, SAME_CONTENT,
				UNREACHABLE_PATH1, UNREACHABLE_PATH2 };
	}

	private static final Map<String, FileContentComparison0> instances =
		new HashMap<>();
	static {
		instances.put(ONLY_IN_PATH1.toString(), ONLY_IN_PATH1);
		instances.put(ONLY_IN_PATH2.toString(), ONLY_IN_PATH2);
		instances.put(DIFFERENT_CONTENT.toString(), DIFFERENT_CONTENT);
		instances.put(SAME_CONTENT.toString(), SAME_CONTENT);
		instances.put(UNREACHABLE_PATH1.toString(), UNREACHABLE_PATH1);
		instances.put(UNREACHABLE_PATH2.toString(), UNREACHABLE_PATH2);
	}

	private final String name;

	protected FileContentComparison0(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	/**
	 * Returns the corresponding <code>FileContentComparison0</code>.
	 * 
	 * @return The corresponding <code>FileContentComparison0</code>.
	 * @throws IllegalArgumentException
	 *             if <code>name</code> is not a valid name.
	 */
	public static FileContentComparison0 valueOf(String name) {
		FileContentComparison0 retVal = instances.get(name);
		if (retVal == null) {
			throw new IllegalArgumentException("invalid name: '" + name + "'");
		}
		return retVal;
	}

	private Object readResolve() throws ObjectStreamException {
		return valueOf(name);
	}

	public boolean equals(Object o) {
		FileContentComparison0 d = (FileContentComparison0) o;
		boolean retVal = this.name.equals(d.name);
		return retVal;
	}

	public int hashCode() {
		return this.name.hashCode();
	}

}
