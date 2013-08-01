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
package com.choicemaker.cm.compiler;

/**
 * Modifiers
 *
 * @author   Matthis Zenger
 * @author   Martin Buechi 
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public interface Modifiers {
	/** modifiers as defined by the JVM specification
	 */
	int PUBLIC = 0x0001;
	int PRIVATE = 0x0002;
	int PROTECTED = 0x0004;
	int STATIC = 0x0008;
	int FINAL = 0x0010;
	int SYNCHRONIZED = 0x0020;
	int VOLATILE = 0x0040;
	int TRANSIENT = 0x0080;
	int NATIVE = 0x0100;
	int INTERFACE = 0x0200;
	int ABSTRACT = 0x0400;
	int STRICTFP = 0x0800;

	/** modifier aliases
	 */
	int ACC_SUPER = 0x0020;

	/** symbol is compiler generated
	 */
	int SYNTHETIC = 0x10000;

	/** symbol is deprecated
	 */
	int DEPRECATED = 0x20000;

	/** symbol denotes a holder class
	 */
	int SCHEMA = 0x40000;

	/** symbol denotes a variable of a quantifier
	 */
	int QUANTVAR = 0x80000;

	/** report/note */
	int REPORT = 0x100000;
	int NOTE = 0x200000;
}
