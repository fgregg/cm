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

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.NameList;
import com.choicemaker.cm.compiler.Names;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.ArrayType;
import com.choicemaker.cm.compiler.Tree.Bad;
import com.choicemaker.cm.compiler.Tree.Binop;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.ClueSetDecl;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.If;
import com.choicemaker.cm.compiler.Tree.ImportDecl;
import com.choicemaker.cm.compiler.Tree.Index;
import com.choicemaker.cm.compiler.Tree.Indexed;
import com.choicemaker.cm.compiler.Tree.Let;
import com.choicemaker.cm.compiler.Tree.Literal;
import com.choicemaker.cm.compiler.Tree.MethodDecl;
import com.choicemaker.cm.compiler.Tree.New;
import com.choicemaker.cm.compiler.Tree.NewArray;
import com.choicemaker.cm.compiler.Tree.PackageDecl;
import com.choicemaker.cm.compiler.Tree.PrimitiveType;
import com.choicemaker.cm.compiler.Tree.Quantified;
import com.choicemaker.cm.compiler.Tree.Select;
import com.choicemaker.cm.compiler.Tree.Self;
import com.choicemaker.cm.compiler.Tree.Shorthand;
import com.choicemaker.cm.compiler.Tree.Typeop;
import com.choicemaker.cm.compiler.Tree.Unop;
import com.choicemaker.cm.compiler.Tree.Valid;
import com.choicemaker.cm.compiler.Tree.VarDecl;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.util.MessageUtil;

/**
 * The syntactic analyzer of the ClueMaker compiler
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:14:09 $
 */
public final class Parser implements Tokens {

	/** the lexical analyzer of this parser
	 */
	private Scanner s;

	/** the compilation unit
	 */
	private ICompilationUnit unit;

	private int lastClueDeclEndPos;
	
	private boolean rDoubleIndex;

	/** precedence table
	 */
	private static int precedence[] = new int[130];

	static {
		for (int i = 0; i < precedence.length; i++)
			precedence[i] = -1;
		precedence[BARBAR] = 0;
		precedence[AMPAMP] = 1;
		precedence[BAR] = 2;
		precedence[CARET] = 3;
		precedence[AMP] = 4;
		precedence[EQEQ] = 5;
		precedence[BANGEQ] = 5;
		precedence[LT] = 6;
		precedence[GT] = 6;
		precedence[LTEQ] = 6;
		precedence[GTEQ] = 6;
		precedence[LTLT] = 7;
		precedence[GTGT] = 7;
		precedence[GTGTGT] = 7;
		precedence[PLUS] = 8;
		precedence[SUB] = 8;
		precedence[STAR] = 9;
		precedence[SLASH] = 9;
		precedence[PERCENT] = 9;
	}

	/** syntactical categories
	 */
	private static final int TYPE = 0x0001;
	private static final int EXPR = 0x0002;

	/** create a new ClueMaker parser
	 */
	public Parser(Scanner s) {
		this.s = s;
		this.unit = s.unit;
	}

	/** issue a syntactical error at the given position and skip
	 *  erroneous section
	 */
	private Tree error(int pos, String message) throws CompilerException {
		unit.error(pos, message);
		skip();
		return new Bad(pos);
	}

	/** issue a syntactical error at the given position and skip
	 *  erroneous section
	 */
	private void simpleError(int pos, String message) throws CompilerException {
		unit.error(pos, message);
	}

	/** issue a syntactical error at the given position and skip
	 *  erroneous section
	 */
	private Tree error(String message) throws CompilerException {
		return error(s.pos, message);
	}

	/** illegal start of an expression or type
	 */
	private Tree error(int pos, int category) throws CompilerException {
		return error(
			pos,
			(((category & EXPR) != 0)
				? MessageUtil.m.formatMessage("compiler.parser.illegal.start.expression")
				: (((category & TYPE) != 0)
					? MessageUtil.m.formatMessage("compiler.parser.illegal.start.type")
					: MessageUtil.m.formatMessage("compiler.parser.illegal.start.identifier"))));
	}

	/** illegal start of an expression or type at the current location
	 */
	private Tree error(int category) throws CompilerException {
		return error(s.pos, category);
	}

	/** skip tokens until a suitable stop token is found
	 */
	private void skip() throws CompilerException {
		int nbraces = 0;
		int nparens = 0;
		while (true) {
			switch (s.token) {
				case EOF :
				case CLASS :
				case INTERFACE :
				case CLUESET :
					return;
				case SEMI :
					if ((nbraces + nparens) == 0)
						return;
					break;
				case LBRACE :
					nbraces++;
					break;
				case RBRACE :
					if (nbraces == 0)
						return;
					nbraces--;
					break;
				case LPAREN :
					nparens++;
					break;
				case RPAREN :
					if (nparens > 0)
						nparens--;
					break;
			}
			s.nextToken();
		}
	}

	/** accept a specific token
	 */
	private int accept(int token) throws CompilerException {
		int pos = s.pos;
		if (s.token == token)
			s.nextToken();
		else {
			error(MessageUtil.m.formatMessage("compiler.parser.token.expected", Scanner.tokenToString(token)));
			if (s.token == token)
				s.nextToken();
		}
		return pos;
	}

	/** read an identifier
	 */
	private String ident() throws CompilerException {
		if (s.token == IDENTIFIER) {
			String name = s.name;
			s.nextToken();
			return name;
		} else {
			accept(IDENTIFIER);
			return Names.ERROR;
		}
	}

	/** parse a compilation unit
	 */
	public Tree[] compilationUnit() throws CompilerException {
		// parse imports
		TreeList content = new TreeList();
		String pn = "";
		if (s.token == PACKAGE) {
			int pos = s.current();
			Tree pkge = new Ident(s.pos, (pn = ident()));
			while (s.token == DOT) {
				int dpos = s.current();
				String idn = ident();
				pn += "." + idn;
				pkge = new Select(dpos, pkge, idn);
			}
			PackageDecl pckg = new PackageDecl(pos, pkge);
			content.append(pckg);
			accept(SEMI);
		} else {
			pn = "gen";
			PackageDecl pckg = new PackageDecl(Location.NOPOS, new Ident(Location.NOPOS, "gen"));
			content.append(pckg);
		}
		unit.setPackageName(pn);
		outer : while (s.token == IMPORT) {
			int pos = s.current();
			Tree clazz = new Ident(s.pos, ident());
			while (s.token == DOT) {
				int dpos = s.current();
				if (s.token == STAR) {
					s.nextToken();
					content.append(new ImportDecl(pos, clazz, true));
					accept(SEMI);
					continue outer;
				} else
					clazz = new Select(dpos, clazz, ident());
			}
			content.append(new ImportDecl(pos, clazz, false));
			accept(SEMI);
		}
		// parse clueset
		accept(CLUESET);
		int pos = s.pos;
		Tree type = typeIdent();
		boolean decision = false;
		if (s.token == DECISION) {
			decision = true;
			s.nextToken();
		}
		String id = ident();
		accept(USES);
		String uses = ident();
		TreeList body = new TreeList();
		lastClueDeclEndPos = accept(LBRACE);
		while ((s.token != RBRACE) && (s.token != EOF)) {
			if (s.token == CLUE || s.token == RULE) {
				body.append(clueDeclaration(0));
			} else if (s.token == REPORT) {
				s.nextToken();
				body.append(clueDeclaration(Modifiers.REPORT));
			} else if (s.token == NOTE) {
				s.nextToken();
				body.append(clueDeclaration(Modifiers.NOTE));
			} else if (s.token == SEMI) {
				s.nextToken();
			} else if(s.token == FINAL) {
				s.nextToken();
				Tree tpe = type();
				String varName = ident();
				body.append(new VarDecl(accept(EQ), Modifiers.FINAL, tpe, varName, expr()));
			} else {
				body.append(embeddedMethod());
			}
		}
		accept(RBRACE);
		if (s.token != EOF) {
			error(s.pos, "Code after end of clueset");
		}
		content.append(new ClueSetDecl(pos, id, type, decision, uses, body.toArray()));
		return content.toArray();
	}

	/** clue declaration
	 */
	private Tree clueDeclaration(int clueModifier) throws CompilerException {
		int pos = s.pos;
		boolean rule = false;
		if (s.token == RULE) {
			rule = true;
		} else if (s.token != CLUE) {
			error(
				MessageUtil.m.formatMessage(
					"compiler.parser.token.or.token.expected",
					Scanner.tokenToString(RULE),
					Scanner.tokenToString(CLUE)));
		}
		s.nextToken();
		String id = ident();
		accept(LBRACE);
		int decision = decision(rule);
		TreeList indices = new TreeList();
		Tree expr;
		if (s.token == FOREACH) {
			s.nextToken();
			accept(LPAREN);
			while (true) {
				indices.append(index());
				if (s.token != COMMA)
					break;
				else
					s.nextToken();
			}
			accept(SEMI);
			expr = expr();
			accept(RPAREN);

		} else
			expr = expr();
		accept(SEMI);
		int dispBeg = s.lastCommentPos > lastClueDeclEndPos ? s.lastCommentPos : pos;
		int dispEnd = s.pos;
		accept(RBRACE);
		ClueDecl res =
			new ClueDecl(
				pos,
				clueModifier,
				rule,
				id,
				decision,
				(Index[]) indices.toArray(new Index[indices.length()]),
				expr,
				Location.line(dispBeg),
				Location.line(dispEnd));
		lastClueDeclEndPos = dispEnd;
		return res;
	}

	/** decision tag
	 */
	private int decision(boolean rule) throws CompilerException {
		switch (s.token) {
			case DIFFER :
				s.nextToken();
				return Tags.DIFFER;
			case MATCH :
				s.nextToken();
				return Tags.MATCH;
			case HOLD :
				s.nextToken();
				return Tags.HOLD;
			default :
				if (rule) {
					switch (s.token) {
						case NODIFFER :
							s.nextToken();
							return Tags.NODIFFER;
						case NOMATCH :
							s.nextToken();
							return Tags.NOMATCH;
						case NOHOLD :
							s.nextToken();
							return Tags.NOHOLD;
						case NONEDEC :
							s.nextToken();
							return Tags.NONEDEC;
						default :
							simpleError(s.pos, MessageUtil.m.formatMessage("compiler.parser.extdecision.expected"));
							return Tags.ERROR;
					}
				} else {
					return Tags.NODEC;
				}
		}
	}

	/** foreach index
	 */
	private Index index() throws CompilerException {
		return new Index(s.pos, type(), ident(), new NewArray(accept(COLON), null, null, initializer()));
	}

	/** embedded java method
	 */
	private Tree embeddedMethod() throws CompilerException {
		return new MethodDecl(s.pos, type(), ident(), formalParams(), throwsOpt(), methodBody());
	}

	private VarDecl[] formalParams() throws CompilerException {
		TreeList ts = new TreeList();
		accept(LPAREN);
		if (s.token != RPAREN) {
			ts.append(new VarDecl(s.pos, type(), ident(), null));
			while (s.token == COMMA)
				ts.append(new VarDecl(s.current(), type(), ident(), null));
		}
		accept(RPAREN);
		return (VarDecl[]) ts.toArray(new VarDecl[ts.length()]);
	}

	private Tree[] throwsOpt() throws CompilerException {
		TreeList ts = new TreeList();
		if (s.token == THROWS)
			do {
				s.nextToken();
				ts.append(qualIdent());
			} while (s.token == COMMA);
		return ts.toArray();
	}

	private String methodBody() throws CompilerException {
		int start = s.getIndex();
		accept(LBRACE);
		int nbraces = 0;
		while (!((nbraces == 0) && (s.token == RBRACE))) {
			if (s.token == LBRACE)
				nbraces++;
			else if (s.token == RBRACE)
				nbraces--;
			else if (s.token == EOF)
				break;
			s.nextToken();
		}
		int end = s.getIndex();
		accept(RBRACE);
		return Names.fromArray(s.buf, start, end - start);
	}

	/** parse an expression
	 */
	private Tree expr() throws CompilerException {
		return term(EXPR);
	}

	private Tree[] exprs() throws CompilerException {
		TreeList ts = new TreeList();
		ts.append(expr());
		while (s.token == COMMA) {
			s.nextToken();
			ts.append(expr());
		}
		return ts.toArray();
	}

	private Tree[] args() throws CompilerException {
		accept(LPAREN);
		Tree[] ts = (s.token != RPAREN) ? exprs() : new Tree[0];
		accept(RPAREN);
		return ts;
	}

	private Tree optSemiCond() throws CompilerException {
		if (s.token == SEMI) {
			s.nextToken();
			return expr();
		} else {
			return null;
		}
	}
	/** parse a type
	 */
	private Tree type() throws CompilerException {
		return term(TYPE);
	}

	private Tree term(int category) throws CompilerException {
		Tree res = binopTerm(category);
		if ((s.token == QUES) && ((category & EXPR) != 0) && res.isExpr()) {
			int pos = s.current();
			Tree thenp = term(EXPR);
			accept(COLON);
			Tree elsep = term(EXPR);
			return new If(pos, res, thenp, elsep);
		} else
			return res;
	}

	private Tree binopTerm(int category) throws CompilerException {
		Tree res = instanceOfTerm(category);
		if (res != null && (precedence[s.token] >= 0) && ((category & EXPR) != 0) && res.isExpr()) {
			Tree[] operands = new Tree[11];
			int[] operators = new int[10];
			int sp = 0;
			operands[sp] = res;
			while (precedence[s.token] >= 0) {
				operators[sp++] = s.token;
				int pos = s.current();
				operands[sp] = instanceOfTerm(EXPR);
				while ((sp > 0) && (precedence[operators[sp - 1]] >= precedence[s.token]))
					operands[--sp] = createBinop(pos, operands[sp], operators[sp], operands[sp + 1]);
			}
			return operands[sp];
		} else {
			return res;
		}
	}

	private Tree instanceOfTerm(int category) throws CompilerException {
		Tree res = simpleTerm(category);
		if ((s.token == INSTANCEOF) && ((category & EXPR) != 0) && res.isExpr())
			return new Typeop(s.current(), Tags.TEST, type(), res);
		else
			return res;
	}

	private Tree simpleTerm(int category) throws CompilerException {
		// parse prefix operators
		if ((category & EXPR) != 0) {
			int token = s.token;
			switch (token) {
				case BANG :
				case TILDE :
				case PLUS :
				case SUB :
					return createUnop(s.current(), token, simpleTerm(EXPR));
			}
		}
		// parse primary expressions
		boolean allowApply = false;
		boolean allowClass = false;
		boolean allowSelect = true;
		boolean allowIndexed = true;
		Tree res = null;
		if ((category & TYPE) != 0)
			switch (s.token) {
				case IDENTIFIER :
					res = new Ident(s.pos, ident());
					allowClass = true;
					allowApply = ((category & EXPR) != 0);
					break;
				case BYTE :
				case SHORT :
				case INT :
				case LONG :
				case FLOAT :
				case DOUBLE :
				case CHAR :
				case BOOLEAN :
				case VOID :
					res = typeIdent();
					allowClass = ((category & EXPR) != 0);
					allowSelect = false;
					allowIndexed = false;
			}
		if ((res == null) && ((category & EXPR) != 0)) {
			category = EXPR;
			switch (s.token) {
				case LPAREN :
					res = parenTerm(category);
					allowApply = true;
					category = EXPR;
					break;
				case IDENTIFIER :
					res = new Ident(s.pos, ident());
					allowApply = true;
					allowClass = true;
					break;
				case NEW :
					s.nextToken();
					res = template();
					break;
				case EXISTS :
				case ALL :
				case COUNT :
				case COUNTUNIQUE :
				case MINIMUM :
				case MAXIMUM :
					int quantifier;
					switch (s.token) {
						case EXISTS :
							quantifier = Tags.EXISTS;
							break;
						case ALL :
							quantifier = Tags.ALL;
							break;
						case COUNT :
							quantifier = Tags.COUNT;
							break;
						case COUNTUNIQUE :
							quantifier = Tags.COUNTUNIQUE;
							break;
						case MINIMUM :
							quantifier = Tags.MINIMUM;
							break;
						default :
							quantifier = Tags.MAXIMUM;
							break;
					}
					int qpos = s.current();
					accept(LPAREN);
					NameList nl = new NameList();
					while (s.token == IDENTIFIER) {
						nl.append(ident());
						if (s.token == COMMA)
							s.nextToken();
						else
							break;
					}
					accept(SEMI);
					Tree expr = expr();
					if (quantifier == Tags.MINIMUM || quantifier == Tags.MAXIMUM) {
						accept(SEMI);
						res = new Quantified(qpos, quantifier, nl.toArray(), expr, expr());
					} else {
						res = new Quantified(qpos, quantifier, nl.toArray(), expr);
					}
					accept(RPAREN);
					allowSelect = false;
					allowIndexed = false;
					break;
				case LET :
					int lpos = s.current();
					accept(LPAREN);
					TreeList binders = new TreeList();
					while (s.token != EOF) {
						Tree tpe = type();
						String id = ident();
						binders.append(new VarDecl(accept(EQ), tpe, id, expr()));
						if (s.token == COMMA)
							s.nextToken();
						else
							break;
					}
					accept(SEMI);
					res = new Let(lpos, (VarDecl[]) binders.toArray(new VarDecl[binders.length()]), expr());
					accept(RPAREN);
					break;
				case SAME :
				case DIFFERENT :
				case COMPARE :
					{
						int form;
						switch (s.token) {
							case SAME :
								form = Tags.SAME;
								break;
							case DIFFERENT :
								form = Tags.DIFFERENT;
								break;
							default :
								form = Tags.COMPARE;
								break;
						}
						int spos = s.current();
						accept(LPAREN);
						res = new Shorthand(spos, form, exprs(), optSemiCond());
						accept(RPAREN);
						break;
					}
				case SWAPSAME :
				{
					int spos = s.current();
					accept(LPAREN);
					Tree numPerConjunct = expr();
					accept(SEMI);
					Tree minNumMoved = expr();
					accept(SEMI);
					res = new Shorthand(spos, Tags.SWAPSAME, exprs(), null, numPerConjunct, minNumMoved);
					accept(RPAREN);
					break;
				}	
				case AND_SHORTHAND :
				case OR_SHORTHAND :
				case XOR_SHORTHAND :
					{
						int form;
						switch (s.token) {
							case AND_SHORTHAND :
								form = Tags.AND_SHORTHAND;
								break;
							case OR_SHORTHAND :
								form = Tags.OR_SHORTHAND;
								break;
							default :
								form = Tags.XOR_SHORTHAND;
								break;
						}
						int spos = s.current();
						accept(LPAREN);
						rDoubleIndex = true;
						res = new Shorthand(spos, form, new Tree[] { expr()}, null);
						rDoubleIndex = false;
						accept(RPAREN);
						break;
					}
				case VALID :
					int vpos = s.current();
					accept(LPAREN);
					res = simpleTerm(EXPR);
					accept(RPAREN);
					if (res.tag != Tags.SELECT)
						simpleError(res.pos, MessageUtil.m.formatMessage("compiler.parser.field.selection.expected"));
					res = new Valid(vpos, res);
					break;
				case Q :
					res = new Self(s.current(), Tags.Q);
					break;
				case M :
					res = new Self(s.current(), Tags.M);
					break;
				case R :
					res = new Self(s.current(), Tags.R);
					break;
				case BYTE :
				case SHORT :
				case INT :
				case LONG :
				case FLOAT :
				case DOUBLE :
				case CHAR :
				case BOOLEAN :
				case VOID :
					res = typeIdent();
					while (s.token == LBRACKET) {
						s.nextToken();
						res = new ArrayType(accept(RBRACKET), res);
					}
					accept(DOT);
					allowIndexed = false;
					return new Select(accept(CLASS), res, "class".intern());
				case STRINGLITERAL :
					res = new Literal(s.pos, Tags.STRING, s.name);
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case CHARLITERAL :
					res = new Literal(s.pos, Tags.CHAR, new Character((char) s.intVal));
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case INTLITERAL :
					res = new Literal(s.pos, Tags.INT, new Integer((int) s.intVal));
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case LONGLITERAL :
					res = new Literal(s.pos, Tags.LONG, new Long(s.intVal));
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case FLOATLITERAL :
					res = new Literal(s.pos, Tags.FLOAT, new Float((float) s.floatVal));
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case DOUBLELITERAL :
					res = new Literal(s.pos, Tags.DOUBLE, new Double(s.floatVal));
					s.nextToken();
					allowSelect = false;
					allowIndexed = false;
					return res;
				case TRUE :
					return new Literal(s.current(), Tags.BOOLEAN, Boolean.valueOf(true));
				case FALSE :
					return new Literal(s.current(), Tags.BOOLEAN, Boolean.valueOf(false));
				case NULL :
					return new Ident(s.current(), "null".intern());
				default :
					res = error(category);
			}
		}
		outer : while (true) {
			switch (s.token) {
				case DOT :
					if (((category & EXPR) != 0) && !allowSelect && allowClass) {
						s.nextToken();
						res = new Select(accept(CLASS), res, "class".intern());
						allowClass = false;
						allowSelect = true;
						allowApply = false;
						allowIndexed = false;
						category = EXPR;
					} else if (!allowSelect)
						break outer;
					int dpos = s.current();
					if (((category & EXPR) != 0) && allowClass && (s.token == CLASS)) {
						res = new Select(s.current(), res, "class".intern());
						allowClass = false;
						allowSelect = true;
						allowApply = false;
						allowIndexed = false;
						category = EXPR;
					} else if (s.token != CLASS) {
						res = new Select(dpos, res, ident());
						allowApply = true;
						allowSelect = true;
						allowIndexed = true;
					} else {
						simpleError(s.pos, MessageUtil.m.formatMessage("compiler.parser.class.not.selectable"));
						s.nextToken();
					}
					break;
				case LBRACKET :
					s.nextToken();
					if (((category & TYPE) != 0) && (s.token == RBRACKET)) {
						res = new ArrayType(s.current(), res);
						allowClass = true;
						allowSelect = false;
						allowApply = false;
						allowIndexed = false;
					} else if (!allowIndexed) {
						error(MessageUtil.m.formatMessage("compiler.parser.misplaced.bracket"));
						s.nextToken();
					} else if ((category & EXPR) != 0) {
						if(s.token == RBRACKET) {
							res = new Indexed(s.pos, res, null, null);
							s.nextToken();
						} else {
							res = new Indexed(s.pos, res, expr(), barOptExpr());
							accept(RBRACKET);
						}
						allowClass = false;
						allowSelect = true;
						allowApply = false;
						allowIndexed = true;
						category = EXPR;
					} else {
						error(MessageUtil.m.formatMessage("compiler.parser.misplaced.bracket"));
						s.nextToken();
					}
					break;
				case LPAREN :
					if (!allowApply)
						break outer;
					if ((category & EXPR) != 0) {
						res = new Apply(s.pos, res, args());
						allowApply = false;
						allowClass = false;
						allowIndexed = true;
						allowSelect = true;
						category = EXPR;
						break;
					} else
						break outer;
				default :
					break outer;
			}
		}
		return res;
	}
	
	private Tree barOptExpr() throws CompilerException {
		if(s.token == COMMA) {
			s.nextToken();
			return expr();
		} else {
			return null;
		}
	}

	private Tree template() throws CompilerException {
		int pos = s.pos;
		Tree res = typeIdent();
		if (s.token == LBRACKET) {
			s.nextToken();
			if (s.token == RBRACKET) {
				res = new Indexed(s.current(), res, null);
				while (s.token == LBRACKET) {
					int lpos = s.current();
					accept(RBRACKET);
					res = new ArrayType(lpos, res);
				}
				return new NewArray(pos, res, null, initializer());
			} else {
				TreeList dims = new TreeList();
				dims.append(expr());
				accept(RBRACKET);
				while (s.token == LBRACKET) {
					s.nextToken();
					if (s.token == RBRACKET) {
						res = new ArrayType(s.current(), res);
						break;
					} else {
						dims.append(expr());
						accept(RBRACKET);
					}
				}
				while (s.token == LBRACKET) {
					int ipos = s.current();
					accept(RBRACKET);
					res = new ArrayType(ipos, res);
				}
				return new NewArray(pos, res, dims.toArray(), null);
			}
		} else if (s.token == LPAREN)
			return new New(pos, res, args());
		else
			return error(MessageUtil.m.formatMessage("compiler.parser.token.or.token.expected", "(", "["));
	}

	private Tree[] initializer() throws CompilerException {
		TreeList ts = new TreeList();
		accept(LBRACE);
		if (s.token != RBRACE) {
			ts.append(initElem());
			while (s.token == COMMA) {
				s.nextToken();
				ts.append(initElem());
			}
		}
		accept(RBRACE);
		return ts.toArray();
	}

	private Tree initElem() throws CompilerException {
		if (s.token == LBRACE)
			return new NewArray(s.pos, null, null, initializer());
		else
			return expr();
	}

	private Tree parenTerm(int category) throws CompilerException {
		int pos = accept(LPAREN);
		Tree res = term(category | TYPE);
		accept(RPAREN);
		if (res.isType() && ((category & EXPR) != 0))
			switch (s.token) {
				case IDENTIFIER :
				case CHARLITERAL :
				case INTLITERAL :
				case LONGLITERAL :
				case FLOATLITERAL :
				case DOUBLELITERAL :
				case STRINGLITERAL :
				case NEW :
				case THIS :
				case SUPER :
				case NULL :
				case TILDE :
				case BANG :
				case LPAREN :
					return new Typeop(pos, Tags.CAST, res, simpleTerm(EXPR));
				case PLUS :
				case SUB :
					if (!res.isExpr())
						return new Typeop(pos, Tags.CAST, res, simpleTerm(EXPR));
			}
		return res;
	}

	/** create binary operation
	 */
	private Tree createBinop(int pos, Tree left, int token, Tree right) {
		int opcode;
		switch (token) {
			case PLUS :
				opcode = Tags.PLUS;
				break;
			case SUB :
				opcode = Tags.MINUS;
				break;
			case STAR :
				opcode = Tags.MULT;
				break;
			case SLASH :
				opcode = Tags.DIV;
				break;
			case PERCENT :
				opcode = Tags.MOD;
				break;
			case BAR :
				opcode = Tags.OR;
				break;
			case AMP :
				opcode = Tags.AND;
				break;
			case CARET :
				opcode = Tags.XOR;
				break;
			case BARBAR :
				opcode = Tags.OROR;
				break;
			case AMPAMP :
				opcode = Tags.ANDAND;
				break;
			case EQEQ :
				opcode = Tags.EQEQ;
				break;
			case BANGEQ :
				opcode = Tags.NOTEQ;
				break;
			case LT :
				opcode = Tags.LT;
				break;
			case GT :
				opcode = Tags.GT;
				break;
			case LTEQ :
				opcode = Tags.LTEQ;
				break;
			case GTEQ :
				opcode = Tags.GTEQ;
				break;
			case LTLT :
				opcode = Tags.LSHIFT;
				break;
			case GTGT :
				opcode = Tags.RSHIFT;
				break;
			case GTGTGT :
				opcode = Tags.URSHIFT;
				break;
			default :
				throw new Error("createBinop");
		}
		return new Binop(pos, opcode, left, right);
	}

	/** create unary operation
	 */
	private Tree createUnop(int pos, int token, Tree expr) {
		int opcode;
		switch (token) {
			case PLUS :
				opcode = Tags.PLUS;
				break;
			case SUB :
				opcode = Tags.MINUS;
				break;
			case TILDE :
				opcode = Tags.COMP;
				break;
			case BANG :
				opcode = Tags.NOT;
				break;
			default :
				throw new Error("createUnop");
		}
		return new Unop(pos, opcode, expr);
	}

	/** a qualified identifier
	 */
	private Tree qualIdent() throws CompilerException {
		Tree res = new Ident(s.pos, ident());
		while (s.token == DOT)
			res = new Select(s.current(), res, ident());
		return res;
	}

	/** type identifiers
	 */
	private Tree typeIdent() throws CompilerException {
		switch (s.token) {
			case IDENTIFIER :
				return qualIdent();
			case BYTE :
				return new PrimitiveType(s.current(), Tags.BYTE);
			case SHORT :
				return new PrimitiveType(s.current(), Tags.SHORT);
			case INT :
				return new PrimitiveType(s.current(), Tags.INT);
			case LONG :
				return new PrimitiveType(s.current(), Tags.LONG);
			case FLOAT :
				return new PrimitiveType(s.current(), Tags.FLOAT);
			case DOUBLE :
				return new PrimitiveType(s.current(), Tags.DOUBLE);
			case CHAR :
				return new PrimitiveType(s.current(), Tags.CHAR);
			case BOOLEAN :
				return new PrimitiveType(s.current(), Tags.BOOLEAN);
			case VOID :
				return new PrimitiveType(s.current(), Tags.VOID);
			default :
				return error(s.current(), TYPE);
		}
	}
}
