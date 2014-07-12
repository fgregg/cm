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

import java.util.HashMap;

import com.choicemaker.cm.compiler.Characters;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Names;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;

/**
 * The lexical analyzer of the ClueMaker compiler
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:14:09 $
 */
public class Scanner implements Characters, Tokens {

	/** the next token
	 */
	public int token;

	/** the location of the token
	 */
	public int pos = 0;

	/** the first character position after the previous token
	 */
	public int lastpos = 0;

	/** the name of an identifier or token
	 */
	public String name;

	/** the value of a number
	 */
	public long intVal;
	public double floatVal;

	/** the input buffer:
	 */
	protected char[] buf;
	protected int bp;

	/** the current character
	 */
	protected char ch;

	/** the line and column position of the current character
	 */
	public int line;
	public int column;

	/** The position of the beginning of the last comment. */
	public int lastCommentPos;

	/** the global compilation environment
	 */
	public ICompilationUnit unit;

	/** create a new scanner for a given source code in a given
	 *  compilation environment
	 */
	public Scanner(ICompilationUnit unit) throws CompilerException {
		this.unit = unit;
		buf = unit.getSource().getBuffer();
		bp = 0;
		line = 1;
		column = 1;
		ch = buf[0];
		nextToken();
	}

	/** generate an error at the given position
	 */
	protected void error(int pos, String message) throws CompilerException {
		unit.error(pos, message);
		token = ERROR;
	}

	/** generate an error at the current token position
	 */
	protected void error(String message) throws CompilerException {
		error(pos, message);
	}

	/** convert a character into an int
	 */
	private int char2int(char ch, int base) {
		if ('0' <= ch && ch <= '9' && ch < '0' + base)
			return ch - '0';
		else if ('A' <= ch && ch < 'A' + base - 10)
			return ch - 'A' + 10;
		else if ('a' <= ch && ch < 'a' + base - 10)
			return ch - 'a' + 10;
		else
			return -1;
	}

	/** read the next character in a character or string literal
	 */
	protected void scanLitChar(StringBuffer lit) throws CompilerException {
		if (ch == '\\') {
			ch = buf[++bp];
			column++;
			if (('0' <= ch) && (ch <= '7')) {
				char lch = ch;
				int oct = char2int(ch, 8);
				ch = buf[++bp];
				column++;
				if (('0' <= ch) && (ch <= '7')) {
					oct = oct * 8 + char2int(ch, 8);
					ch = buf[++bp];
					column++;
					if ((lch <= '3') && ('0' <= ch) && (ch <= '7')) {
						oct = oct * 8 + char2int(ch, 8);
						ch = buf[++bp];
						column++;
					}
				}
				lit.append((char) oct);
			} else if (ch != FE) {
				switch (ch) {
					case 'b' :
					case 't' :
					case 'n' :
					case 'r' :
					case 'f' :
					case '\"' :
					case '\'' :
					case BACKSLASH :
						lit.append(BACKSLASH);
						lit.append(ch);
						break;
					default :
						error(
							Location.encode(line, column) - 1,
							ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.illegal.escape.sequence"));
						lit.append(ch);
				}
				ch = buf[++bp];
				column++;
			}
		} else if (ch != FE) {
			lit.append(ch);
			ch = buf[++bp];
			column++;
		}
	}

	/** read the fractional part of a floating point literal; convert
	 *  buf[index..] into a floating point value and set field floatVal
	 *  accordingly
	 */
	protected strictfp void scanFraction(int index) throws CompilerException {
		while (char2int(ch, 10) >= 0) {
			ch = buf[++bp];
			column++;
		}
		token = DOUBLELITERAL;
		if ((ch == 'e') || (ch == 'E')) {
			ch = buf[++bp];
			column++;
			if ((ch == '+') || (ch == '-')) {
				char sign = ch;
				ch = buf[++bp];
				column++;
				if (('0' > ch) || (ch > '9')) {
					ch = sign;
					bp--;
					column--;
				}
			}
			while (char2int(ch, 10) >= 0) {
				ch = buf[++bp];
				column++;
			}
		}
		double limit = Double.MAX_VALUE;
		if ((ch == 'd') || (ch == 'D')) {
			ch = buf[++bp];
			column++;
		} else if ((ch == 'f') || (ch == 'F')) {
			token = FLOATLITERAL;
			limit = Float.MAX_VALUE;
			ch = buf[++bp];
			column++;
		}
		try {
			floatVal = Double.valueOf(new String(buf, index, bp - index)).doubleValue();
			if (floatVal > limit)
				error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.floating.number.too.large"));
		} catch (NumberFormatException e) {
			error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.malformed.floating.number"));
		}
	}

	/** convert buf[index..index+len-1] into an integer value and set the
	 *  intVal field. base denotes the base of the integer representation.
	 *  It is either 8, 10, or 16. max refers to the maximal number before
	 *  an overflow occurs.
	 */
	protected void scanInt(int index, int len, int base, long max) throws CompilerException {
		intVal = 0;
		int divider = (base == 10) ? 1 : 2;
		for (int i = index; i < index + len; i++) {
			int d = char2int(buf[i], base);
			if (d < 0) {
				error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.lexical.error.in.int"));
				return;
			}
			if ((intVal < 0)
				|| (max / (base / divider) < intVal)
				|| (max - (d / divider) < (intVal * (base / divider) - (token == USUB ? 1 : 0)))) {
				error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.integer.too.large"));
				return;
			}
			intVal = intVal * base + d;
		}
	}

	/** read a number literal and convert buf[index..] by setting either the
	 *  intVal or floatVal fields. base denotes the base of the number; it
	 *  can be either 8, 10, or 16.
	 */
	protected strictfp void scanNumber(int index, int base) throws CompilerException {
		while (char2int(ch, (base == 8) ? 10 : base) >= 0) {
			ch = buf[++bp];
			column++;
		}
		if ((base <= 10) && (ch == '.')) {
			ch = buf[++bp];
			column++;
			scanFraction(index);
		} else if (
			(base <= 10) && ((ch == 'e') || (ch == 'E') || (ch == 'f') || (ch == 'F') || (ch == 'd') || (ch == 'D')))
			scanFraction(index);
		else if ((ch == 'l') || (ch == 'L')) {
			scanInt(index, bp - index, base, Long.MAX_VALUE);
			ch = buf[++bp];
			column++;
			token = LONGLITERAL;
		} else {
			scanInt(index, bp - index, base, Integer.MAX_VALUE);
			intVal = (int) intVal;
			token = INTLITERAL;
		}
	}

	/** return true if ch can be part of an operator
	 */
	protected boolean isOperChar(char ch) {
		switch (ch) {
			case '!' :
			case '%' :
			case '&' :
			case '*' :
			case '?' :
			case '+' :
			case '-' :
			case ':' :
			case '<' :
			case '=' :
			case '>' :
			case '^' :
			case '|' :
			case '~' :
				return true;
		}
		return false;
	}

	/** read the longest possible sequence of special characters and convert
	 *  it into to token
	 */
	protected void scanOperator() {
		int index = bp;
		name = Names.fromArray(buf, index, 1);
		while (true) {
			token = nameToToken(name);
			ch = buf[++bp];
			column++;
			if (!isOperChar(ch))
				break;
			String newname = Names.fromArray(buf, index, bp + 1 - index);
			if (nameToToken(newname) == IDENTIFIER)
				break;
			name = newname;
		}
	}

	/** skip the current token and return last position
	 */
	public int current() throws CompilerException {
		int lpos = pos;
		nextToken();
		return lpos;
	}

	/** get index
	 */
	public int getIndex() {
		return bp;
	}

	/** read the next token
	 */
	public void nextToken() throws CompilerException {
		lastpos = Location.encode(line, column);
		while (true) {
			pos = Location.encode(line, column);
			int index = bp;
			switch (ch) {
				case ' ' :
					ch = buf[++bp];
					column++;
					break;
				case '\t' :
					column = ((column - 1) / unit.getCompilationEnv().tabsize * unit.getCompilationEnv().tabsize) + unit.getCompilationEnv().tabsize;
					ch = buf[++bp];
					column++;
					break;
				case LF :
				case FF :
					line++;
					column = 1;
					ch = buf[++bp];
					break;
				case CR :
					line++;
					column = 1;
					if ((ch = buf[++bp]) == LF)
						ch = buf[++bp];
					break;
				case '.' :
					ch = buf[++bp];
					column++;
					if ('0' <= ch && ch <= '9')
						scanFraction(index);
					else
						token = DOT;
					return;
				case ',' :
					ch = buf[++bp];
					column++;
					token = COMMA;
					return;
				case ';' :
					ch = buf[++bp];
					column++;
					token = SEMI;
					return;
				case '(' :
					ch = buf[++bp];
					column++;
					token = LPAREN;
					return;
				case ')' :
					ch = buf[++bp];
					column++;
					token = RPAREN;
					return;
				case '[' :
					ch = buf[++bp];
					column++;
					token = LBRACKET;
					return;
				case ']' :
					ch = buf[++bp];
					column++;
					token = RBRACKET;
					return;
				case '{' :
					ch = buf[++bp];
					column++;
					token = LBRACE;
					return;
				case '}' :
					ch = buf[++bp];
					column++;
					token = RBRACE;
					return;
				case '$' :
				case '_' :
				case 'A' :
				case 'B' :
				case 'C' :
				case 'D' :
				case 'E' :
				case 'F' :
				case 'G' :
				case 'H' :
				case 'I' :
				case 'J' :
				case 'K' :
				case 'L' :
				case 'M' :
				case 'N' :
				case 'O' :
				case 'P' :
				case 'Q' :
				case 'R' :
				case 'S' :
				case 'T' :
				case 'U' :
				case 'V' :
				case 'W' :
				case 'X' :
				case 'Y' :
				case 'Z' :
				case 'a' :
				case 'b' :
				case 'c' :
				case 'd' :
				case 'e' :
				case 'f' :
				case 'g' :
				case 'h' :
				case 'i' :
				case 'j' :
				case 'k' :
				case 'l' :
				case 'm' :
				case 'n' :
				case 'o' :
				case 'p' :
				case 'q' :
				case 'r' :
				case 's' :
				case 't' :
				case 'u' :
				case 'v' :
				case 'w' :
				case 'x' :
				case 'y' :
				case 'z' :
					while (true) {
						ch = buf[++bp];
						column++;
						switch (ch) {
							case '$' :
							case '_' :
							case 'A' :
							case 'B' :
							case 'C' :
							case 'D' :
							case 'E' :
							case 'F' :
							case 'G' :
							case 'H' :
							case 'I' :
							case 'J' :
							case 'K' :
							case 'L' :
							case 'M' :
							case 'N' :
							case 'O' :
							case 'P' :
							case 'Q' :
							case 'R' :
							case 'S' :
							case 'T' :
							case 'U' :
							case 'V' :
							case 'W' :
							case 'X' :
							case 'Y' :
							case 'Z' :
							case 'a' :
							case 'b' :
							case 'c' :
							case 'd' :
							case 'e' :
							case 'f' :
							case 'g' :
							case 'h' :
							case 'i' :
							case 'j' :
							case 'k' :
							case 'l' :
							case 'm' :
							case 'n' :
							case 'o' :
							case 'p' :
							case 'q' :
							case 'r' :
							case 's' :
							case 't' :
							case 'u' :
							case 'v' :
							case 'w' :
							case 'x' :
							case 'y' :
							case 'z' :
							case '0' :
							case '1' :
							case '2' :
							case '3' :
							case '4' :
							case '5' :
							case '6' :
							case '7' :
							case '8' :
							case '9' :
								continue;
							default :
								name = Names.fromArray(buf, index, bp - index);
								token = nameToToken(name);
								return;
						}
					}
				case '0' :
					ch = buf[++bp];
					column++;
					if ((ch == 'X') || (ch == 'x')) {
						ch = buf[++bp];
						column++;
						scanNumber(index + 2, 16);
					} else
						scanNumber(index, 8);
					return;
				case '1' :
				case '2' :
				case '3' :
				case '4' :
				case '5' :
				case '6' :
				case '7' :
				case '8' :
				case '9' :
					scanNumber(index, 10);
					return;
				case '/' :
					ch = buf[++bp];
					column++;
					if (ch == '/') {
						do {
							ch = buf[++bp];
							column++;
						} while ((ch != CR) && (ch != LF) && (ch != FE));
						break;
					} else if (ch == '*') {
						ch = buf[++bp];
						if (ch == '*') {
							lastCommentPos = pos;
						}
						column++;
						do {
							do {
								if (ch == CR) {
									line++;
									column = 0;
									ch = buf[++bp];
									column++;
									if (ch == LF) {
										column = 0;
										ch = buf[++bp];
										column++;
									}
								} else if (ch == LF) {
									line++;
									column = 0;
									ch = buf[++bp];
									column++;
								} else if (ch == '\t') {
									column =
										((column - 1) / unit.getCompilationEnv().tabsize * unit.getCompilationEnv().tabsize) + unit.getCompilationEnv().tabsize + 1;
									ch = buf[++bp];
								} else {
									ch = buf[++bp];
									column++;
								}
							} while ((ch != '*') && (ch != FE));
							while (ch == '*') {
								ch = buf[++bp];
								column++;
							}
						}
						while ((ch != '/') && (ch != FE));
						if (ch == '/') {
							ch = buf[++bp];
							column++;
							break;
						} else {
							error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.unclosed.comment"));
							return;
						}
					} else {
						if (ch == '=') {
							name = "/=".intern();
							token = SLASHEQ;
							ch = buf[++bp];
							column++;
						} else {
							name = "/".intern();
							token = SLASH;
						}
						return;
					}
				case '\'' :
					ch = buf[++bp];
					column++;
					StringBuffer clit = new StringBuffer();
					scanLitChar(clit);
					if (ch == '\'') {
						token = CHARLITERAL;
						ch = buf[++bp];
						column++;
						if ((intVal = clit.charAt(0)) == '\\')
							switch (clit.charAt(1)) {
								case 'n' :
									intVal = '\n';
									break;
								case 't' :
									intVal = '\t';
									break;
								case 'b' :
									intVal = '\b';
									break;
								case 'r' :
									intVal = '\r';
									break;
								case 'f' :
									intVal = '\f';
									break;
							}
					} else
						error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.unclosed.character.literal"));
					return;
				case '\"' :
					ch = buf[++bp];
					column++;
					StringBuffer lit = new StringBuffer();
					while ((ch != '\"') && (ch != CR) && (ch != LF) && (ch != FE))
						scanLitChar(lit);
					if (ch == '\"') {
						token = STRINGLITERAL;
						name = lit.toString().intern();
						ch = buf[++bp];
						column++;
					} else
						error(
							Location.encode(line, column),
							ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.open.character.literal"));
					return;
				case FE :
					token = EOF;
					return;
				default :
					if (isOperChar(ch))
						scanOperator();
					else {
						ch = buf[++bp];
						column++;
						error(ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.illegal.character"));
					}
					return;
			}
		}
	}

	/** the names of all tokens
	 */
	protected static String[] tokenName = new String[256];

	/** keyword array; maps from strings to tokens
	 */
	public static HashMap keys = new HashMap();

	/** lookup a name in the token table
	 */
	public static int nameToToken(String name) {
		Integer i = (Integer) keys.get(name);
		return (i == null) ? IDENTIFIER : i.intValue();
	}

	/** convert tokens into strings
	 */
	public static String tokenToString(int token) {
		switch (token) {
			case SEMI :
				return "';'";
			case DOT :
				return "'.'";
			case COMMA :
				return "','";
			case LPAREN :
				return "'('";
			case RPAREN :
				return "')'";
			case LBRACKET :
				return "'['";
			case RBRACKET :
				return "']'";
			case LBRACE :
				return "'{'";
			case RBRACE :
				return "'}'";
			case IDENTIFIER :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.identifier");
			case CHARLITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.character.literal");
			case STRINGLITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.string.literal");
			case INTLITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.integer.literal");
			case LONGLITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.long.literal");
			case FLOATLITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.float.literal");
			case DOUBLELITERAL :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.double.literal");
			case ERROR :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.bad.input");
			case EOF :
				return ChoiceMakerCoreMessages.m.formatMessage("compiler.scanner.end.of.input");
			default :
				return tokenName[token];
		}
	}

	/** enter a new keyword into the tables
	 */
	protected static void enterKeyword(String s, int tokenId) {
		String n = Names.fromString(s);
		tokenName[tokenId] = n;
		keys.put(n, new Integer(tokenId));
	}

	/** initialize tables
	 */
	static {
		// enter java keywords
		enterKeyword("+", PLUS);
		enterKeyword("-", SUB);
		enterKeyword("!", BANG);
		enterKeyword("%", PERCENT);
		enterKeyword("^", CARET);
		enterKeyword("&", AMP);
		enterKeyword("*", STAR);
		enterKeyword("|", BAR);
		enterKeyword("~", TILDE);
		enterKeyword("/", SLASH);
		enterKeyword(">", GT);
		enterKeyword("<", LT);
		enterKeyword("?", QUES);
		enterKeyword(":", COLON);
		enterKeyword("=", EQ);
		enterKeyword("++", PLUSPLUS);
		enterKeyword("--", SUBSUB);
		enterKeyword("==", EQEQ);
		enterKeyword("<=", LTEQ);
		enterKeyword(">=", GTEQ);
		enterKeyword("!=", BANGEQ);
		enterKeyword("<<", LTLT);
		enterKeyword(">>", GTGT);
		enterKeyword(">>>", GTGTGT);
		enterKeyword("+=", PLUSEQ);
		enterKeyword("-=", SUBEQ);
		enterKeyword("*=", STAREQ);
		enterKeyword("/=", SLASHEQ);
		enterKeyword("&=", AMPEQ);
		enterKeyword("|=", BAREQ);
		enterKeyword("^=", CARETEQ);
		enterKeyword("%=", PERCENTEQ);
		enterKeyword("<<=", LTLTEQ);
		enterKeyword(">>=", GTGTEQ);
		enterKeyword(">>>=", GTGTGTEQ);
		enterKeyword("||", BARBAR);
		enterKeyword("&&", AMPAMP);
		enterKeyword("abstract", ABSTRACT);
		enterKeyword("assert", ASSERT);
		enterKeyword("break", BREAK);
		enterKeyword("case", CASE);
		enterKeyword("catch", CATCH);
		enterKeyword("class", CLASS);
		enterKeyword("const", CONST);
		enterKeyword("continue", CONTINUE);
		enterKeyword("default", DEFAULT);
		enterKeyword("do", DO);
		enterKeyword("else", ELSE);
		enterKeyword("extends", EXTENDS);
		enterKeyword("final", FINAL);
		enterKeyword("finally", FINALLY);
		enterKeyword("for", FOR);
		enterKeyword("goto", GOTO);
		enterKeyword("if", IF);
		enterKeyword("implements", IMPLEMENTS);
		enterKeyword("import", IMPORT);
		enterKeyword("interface", INTERFACE);
		enterKeyword("native", NATIVE);
		enterKeyword("new", NEW);
		enterKeyword("package", PACKAGE);
		enterKeyword("private", PRIVATE);
		enterKeyword("protected", PROTECTED);
		enterKeyword("public", PUBLIC);
		enterKeyword("return", RETURN);
		enterKeyword("static", STATIC);
		enterKeyword("strictfp", STRICTFP);
		enterKeyword("super", SUPER);
		enterKeyword("switch", SWITCH);
		enterKeyword("synchronized", SYNCHRONIZED);
		enterKeyword("this", THIS);
		enterKeyword("volatile", VOLATILE);
		enterKeyword("throw", THROW);
		enterKeyword("throws", THROWS);
		enterKeyword("transient", TRANSIENT);
		enterKeyword("try", TRY);
		enterKeyword("while", WHILE);
		enterKeyword("instanceof", INSTANCEOF);
		enterKeyword("boolean", BOOLEAN);
		enterKeyword("byte", BYTE);
		enterKeyword("char", CHAR);
		enterKeyword("double", DOUBLE);
		enterKeyword("float", FLOAT);
		enterKeyword("int", INT);
		enterKeyword("long", LONG);
		enterKeyword("short", SHORT);
		enterKeyword("void", VOID);
		enterKeyword("true", TRUE);
		enterKeyword("false", FALSE);
		enterKeyword("null", NULL);
		// enter cluemaker keywords
		enterKeyword("clueset", CLUESET);
		enterKeyword("uses", USES);
		enterKeyword("clue", CLUE);
		enterKeyword("foreach", FOREACH);
		enterKeyword("differ", DIFFER);
		enterKeyword("match", MATCH);
		enterKeyword("hold", HOLD);
		enterKeyword("q", Q);
		enterKeyword("m", M);
		enterKeyword("r", R);
		enterKeyword("let", LET);
		enterKeyword("valid", VALID);
		enterKeyword("exists", EXISTS);
		enterKeyword("all", ALL);
		enterKeyword("count", COUNT);
		enterKeyword("same", SAME);
		enterKeyword("compare", COMPARE);
		enterKeyword("different", DIFFERENT);
		enterKeyword("swapsame", SWAPSAME);
		enterKeyword("swapdifferent", SWAPDIFFERENT);
		enterKeyword("report", REPORT);
		enterKeyword("note", NOTE);
		enterKeyword("rule", RULE);
		enterKeyword("nodiffer", NODIFFER);
		enterKeyword("nomatch", NOMATCH);
		enterKeyword("nohold", NOHOLD);
		enterKeyword("none", NONEDEC);
		enterKeyword("decision", DECISION);
		enterKeyword("minimum", MINIMUM);
		enterKeyword("maximum", MAXIMUM);
		enterKeyword("and", AND_SHORTHAND);
		enterKeyword("or", OR_SHORTHAND);
		enterKeyword("xor", XOR_SHORTHAND);
		enterKeyword("countunique", COUNTUNIQUE);
	}
}
