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
 * Semantic Tags
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public interface SemanticTags {

	// symbol kinds
	int SCM = 0x0001;
	int SET = 0x0002;
	int CLU = 0x0004;
	int PCK = 0x0008;
	int TYP = 0x0010;
	int MTH = 0x0020;
	int VAR = 0x0040;

	// derived kinds
	int VAL = VAR | 0x0010;
	int ANY = PCK | TYP | MTH | VAR;

	// synthetic kinds
	int BAD = 0x0100;
	int AMB = 0x0200;

	// type tags
	int NONE = 0;
	int ALL = 1;
	int ERROR = 2;
	int VOID = 3;
	int BYTE = 4;
	int CHAR = 5;
	int SHORT = 6;
	int INT = 7;
	int LONG = 8;
	int FLOAT = 9;
	int DOUBLE = 10;
	int BOOLEAN = 11;
	int CLASS = 12;
	int ARRAY = 13;
	int METHOD = 14;
	int NULL = 15;
	int PACKAGE = 16;
	int CLUESET = 17;
}
