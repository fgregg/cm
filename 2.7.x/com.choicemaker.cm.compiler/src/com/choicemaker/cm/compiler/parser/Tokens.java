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
package com.choicemaker.cm.compiler.parser;

/**
 * Tokens
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
interface Tokens {

	/** some meta tokens
	 */
	int ERROR = -1;
	int EOF = 0;

	/** all java tokens
	 */
	int ABSTRACT = 2;
	int AMP = 3;
	int AMPAMP = 4;
	int AMPEQ = 5;
	int ASSERT = 6;
	int BANG = 7;
	int BANGEQ = 8;
	int BAR = 9;
	int BARBAR = 10;
	int BAREQ = 11;
	int BOOLEAN = 12;
	int BREAK = 13;
	int BYTE = 14;
	int CARET = 15;
	int CARETEQ = 16;
	int CASE = 17;
	int CATCH = 18;
	int CHAR = 19;
	int CHARLITERAL = 20;
	int CLASS = 21;
	int COLON = 22;
	int COMMA = 23;
	int CONST = 24;
	int CONTINUE = 25;
	int DEFAULT = 26;
	int DO = 27;
	int DOT = 28;
	int DOUBLE = 29;
	int DOUBLELITERAL = 30;
	int ELSE = 31;
	int EQ = 32;
	int EQEQ = 32;
	int EXTENDS = 33;
	int FINAL = 34;
	int FINALLY = 35;
	int FLOAT = 36;
	int FLOATLITERAL = 37;
	int FOR = 38;
	int GOTO = 39;
	int GT = 40;
	int GTEQ = 41;
	int GTGT = 42;
	int GTGTEQ = 43;
	int GTGTGT = 44;
	int GTGTGTEQ = 45;
	int IDENTIFIER = 46;
	int IF = 47;
	int IMPLEMENTS = 48;
	int IMPORT = 49;
	int INSTANCEOF = 50;
	int INT = 51;
	int INTERFACE = 52;
	int INTLITERAL = 53;
	int LBRACE = 54;
	int LBRACKET = 55;
	int LONG = 56;
	int LONGLITERAL = 57;
	int LPAREN = 58;
	int LT = 59;
	int LTEQ = 60;
	int LTLT = 61;
	int LTLTEQ = 62;
	int NATIVE = 63;
	int NEW = 64;
	int PACKAGE = 65;
	int PERCENT = 66;
	int PERCENTEQ = 67;
	int PLUS = 68;
	int PLUSEQ = 69;
	int PLUSPLUS = 70;
	int PRIVATE = 71;
	int PROTECTED = 72;
	int PUBLIC = 73;
	int QUES = 74;
	int RBRACE = 75;
	int RBRACKET = 76;
	int RETURN = 77;
	int RPAREN = 78;
	int SEMI = 79;
	int SHORT = 80;
	int SLASH = 81;
	int SLASHEQ = 82;
	int STAR = 83;
	int STAREQ = 84;
	int STATIC = 85;
	int STRICTFP = 86;
	int STRINGLITERAL = 87;
	int SUB = 88;
	int SUBEQ = 89;
	int SUBSUB = 90;
	int SUPER = 91;
	int SWITCH = 92;
	int SYNCHRONIZED = 93;
	int THIS = 94;
	int THROW = 95;
	int THROWS = 96;
	int TILDE = 97;
	int TRANSIENT = 98;
	int TRY = 99;
	int USUB = 100;
	int VOID = 101;
	int VOLATILE = 102;
	int WHILE = 103;
	int TRUE = 104;
	int FALSE = 105;
	int NULL = 106;

	/** cluemaker tokens
	 */
	int CLUESET = 110;
	int USES = 111;
	int CLUE = 112;
	int FOREACH = 113;
	int DIFFER = 114;
	int MATCH = 115;
	int HOLD = 116;
	int Q = 117;
	int M = 118;
	int LET = 119;
	int VALID = 120;
	int EXISTS = 121;
	int ALL = 122;
	int SAME = 123;
	int DIFFERENT = 124;
	int SWAPSAME = 127;
	int SWAPDIFFERENT = 128;
	int R = 129;
	int COUNT = 130;
	int REPORT = 131;
	int NOTE = 132;
	int RULE = 133;
	int NODIFFER = 134;
	int NOMATCH = 135;
	int NOHOLD = 136;
	int NONEDEC = 137;
	int DECISION = 139;
	int COMPARE = 140;
	int MINIMUM = 141;
	int MAXIMUM = 142;
	int AND_SHORTHAND = 143;
	int OR_SHORTHAND = 144;
	int XOR_SHORTHAND = 145;
	int COUNTUNIQUE = 146;
}
