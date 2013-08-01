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
 * all tags used in the abstract syntax tree
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public interface Tags {

	// errors
	int ERROR = 0;

	// no tree
	int NONE = 1;

	// syntactical forms
	int IMPORT = 2;
	int CLUESET = 3;
	int CLUE = 4;
	int FOREACH = 5;
	int INDEX = 6;
	int METHOD = 7;
	int VAR = 8;
	int QUANTIFIED = 9;
	int LET = 10;
	int SHORTHAND = 11;
	int VALID = 12;
	int APPLY = 13;
	int NEW = 14;
	int NEWARRAY = 15;
	int CAST = 16;
	int TEST = 17;
	int UNOP = 18;
	int BINOP = 19;
	int INDEXED = 20;
	int SELECT = 21;
	int IDENT = 22;
	int SELF = 23;
	int ARRAYTYPE = 24;
	int PRIMTYPE = 25;
	int LITERAL = 26;
	int IF = 27;
	int PACKAGE = 28;
	int FINAL = 205;

	// primitive types
	int BYTE = 40;
	int SHORT = 41;
	int INT = 42;
	int LONG = 43;
	int FLOAT = 44;
	int DOUBLE = 45;
	int CHAR = 46;
	int BOOLEAN = 47;
	int VOID = 48;
	int STRING = 49;
	int UNKNOWN  = 300;

	// decisions
	int DIFFER = 50;
	int MATCH = 51;
	int HOLD = 52;

	// quantifiers
	int EXISTS = 53;
	int ALL = 54;
	int MINIMUM = 202;
	int MAXIMUM = 203;

	// shorthands
	int SAME = 55;
	int DIFFERENT = 56;
	int COMPARE = 200;

	int COUNT = 57;
	int COUNTUNIQUE = 204;

	int SWAPSAME = 59;
	int SWAPDIFFERENT = 60;
	int AND_SHORTHAND = 206;
	int OR_SHORTHAND = 207;
	int XOR_SHORTHAND = 208;

	// unary opcodes
	int NOT = 63;
	int COMP = 64;

	// unary and binary opcodes
	int PLUS = 65;
	int MINUS = 66;

	// binary opcodes
	int MULT = 70;
	int DIV = 71;
	int MOD = 72;
	int LSHIFT = 73;
	int RSHIFT = 74;
	int URSHIFT = 75;
	int LT = 76;
	int GT = 77;
	int LTEQ = 78;
	int GTEQ = 79;
	int EQEQ = 80;
	int NOTEQ = 81;
	int AND = 82;
	int OR = 83;
	int ANDAND = 84;
	int OROR = 85;
	int XOR = 86;

	// self tags
	int Q = 90;
	int M = 91;
	int R = 92;

	// clue modifiers
	int REPORT = 95;
	int NOTE = 96;

	// rule
	int RULE = 97;

	// pseudo-decisions
	int NODIFFER = 98;
	int NOMATCH = 99;
	int NOHOLD = 100;
	int NONEDEC = 101;
	
	// no decision
	int NODEC = 102;
	
	// codes > 200 may be in use above!
	//	codes > 300 may be in use above!
}
