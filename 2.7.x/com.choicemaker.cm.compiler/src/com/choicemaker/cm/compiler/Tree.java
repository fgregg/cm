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

import com.choicemaker.cm.compiler.Symbol.ClueSetSymbol;
import com.choicemaker.cm.compiler.Symbol.ClueSymbol;
import com.choicemaker.cm.compiler.Symbol.MethodSymbol;
import com.choicemaker.cm.compiler.Symbol.VarSymbol;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * the representation of the abstract syntax of ClueMaker
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:09:55 $
 */
public abstract class Tree implements Tags {

	/** the tag of this tree node
	 */
	public int tag;

	/** the source location of this tree node
	 */
	public int pos = Location.NOPOS;

	/** the type of this tree node
	 */
	public Type type = Type.NONE;

	/** application of visitors
	 */
	public abstract void apply(Visitor v) throws CompilerException;

	/** constructor
	 */
	protected Tree(int tag) {
		this.tag = tag;
	}

	protected Tree() {
	}

	/** set the location
	 */
	public Tree at(int pos) {
		this.pos = pos;
		return this;
	}

	/** is this tree node an expression?
	 */
	public boolean isExpr() {
		return false;
	}

	/** does this tree node denote a type?
	 */
	public boolean isType() {
		return false;
	}

	/** returns the associated symbol
	 */
	public Symbol symbol() {
		return Symbol.NONE;
	}

	public abstract Tree deepCopy();

	protected void deepCopyTo(Tree t) {
		t.tag = tag;
		t.pos = pos;
		t.type = type;
	}

	public boolean isBlock() {
		return false;
	}

	public boolean isSameAs(Tree t) {
		return false;
	}
	
	public static boolean isSameAs(Tree[] xs, Tree[] ys) {
		boolean same = (xs.length == ys.length);
		for (int i = 0; same && (i < xs.length); i++)
			same = xs[i].isSameAs(ys[i]);
		return same;
	}

	public static Tree[] deepCopyArray(Tree[] from, Tree[] to) {
		for (int i = 0; i < from.length; ++i) {
			to[i] = from[i].deepCopy();
		}
		return to;
	}

	/** the visitor interface
	 */
	public static interface Visitor {
		void visit(Bad t) throws CompilerException;
		void visit(PackageDecl t) throws CompilerException;
		void visit(ImportDecl t) throws CompilerException;
		void visit(ClueSetDecl t) throws CompilerException;
		void visit(ClueDecl t) throws CompilerException;
		void visit(Index t) throws CompilerException;
		void visit(MethodDecl t) throws CompilerException;
		void visit(VarDecl t) throws CompilerException;
		void visit(Quantified t) throws CompilerException;
		void visit(Let t) throws CompilerException;
		void visit(Shorthand t) throws CompilerException;
		void visit(Valid t) throws CompilerException;
		void visit(If t) throws CompilerException;
		void visit(Apply t) throws CompilerException;
		void visit(New t) throws CompilerException;
		void visit(NewArray t) throws CompilerException;
		void visit(Typeop t) throws CompilerException;
		void visit(Unop t) throws CompilerException;
		void visit(Binop t) throws CompilerException;
		void visit(Indexed t) throws CompilerException;
		void visit(Select t) throws CompilerException;
		void visit(Ident t) throws CompilerException;
		void visit(Self t) throws CompilerException;
		void visit(ArrayType t) throws CompilerException;
		void visit(PrimitiveType t) throws CompilerException;
		void visit(Literal t) throws CompilerException;
	}

	// the different tree nodes are following

	public static class Bad extends Tree {
		public Bad(int pos) {
			super(ERROR);
			this.pos = pos;
		}

		private Bad() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isType() {
			return true;
		}

		public Tree deepCopy() {
			Bad t = new Bad();
			deepCopyTo(t);
			return t;
		}
	}

	public static class PackageDecl extends Tree {
		public Tree pckage;

		public PackageDecl(int pos, Tree pckage) {
			super(PACKAGE);
			this.pos = pos;
			this.pckage = pckage;
		}

		private PackageDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			PackageDecl t = new PackageDecl();
			deepCopyTo(t);
			t.pckage = pckage.deepCopy();
			return t;
		}

	}

	public static class ImportDecl extends Tree {
		public Tree pckage;
		public boolean starImport;

		public ImportDecl(int pos, Tree pckage, boolean starImport) {
			super(IMPORT);
			this.pos = pos;
			this.pckage = pckage;
			this.starImport = starImport;
		}

		private ImportDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			ImportDecl t = new ImportDecl();
			deepCopyTo(t);
			t.pckage = pckage.deepCopy();
			t.starImport = starImport;
			return t;
		}
	}

	public static class ClueSetDecl extends Tree {
		public String name;
		public Tree tpe;
		public boolean decision;
		public String uses;
		public Tree[] body;
		public ClueSetSymbol sym;

		public ClueSetDecl(int pos, String name, Tree tpe, boolean decision, String uses, Tree[] body) {
			super(CLUESET);
			this.pos = pos;
			this.name = name;
			this.tpe = tpe;
			this.decision = decision;
			this.uses = uses;
			this.body = body;
		}

		private ClueSetDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Symbol symbol() {
			return sym;
		}

		public Tree deepCopy() {
			ClueSetDecl t = new ClueSetDecl();
			deepCopyTo(t);
			t.name = name;
			t.tpe = tpe;
			t.decision = decision;
			t.uses = uses;
			deepCopyArray(body, t.body = new Tree[body.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class ClueDecl extends Tree {
		public int clueModifiers;
		public boolean rule;
		public String name;
		public int decision; // DIFFER | MATCH | HOLD
		public Index[] indices;
		public Tree expr;
		public ClueSymbol sym;
		public int dispBeg;
		public int dispEnd;

		public ClueDecl(
			int pos,
			int clueModifiers,
			boolean rule,
			String name,
			int decision,
			Index[] indices,
			Tree expr,
			int dispBeg,
			int dispEnd) {
			super(CLUE);
			this.pos = pos;
			this.clueModifiers = clueModifiers;
			this.rule = rule;
			this.name = name;
			this.decision = decision;
			this.indices = indices;
			this.expr = expr;
			this.dispBeg = dispBeg;
			this.dispEnd = dispEnd;
		}

		private ClueDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Symbol symbol() {
			return sym;
		}

		public Tree deepCopy() {
			ClueDecl t = new ClueDecl();
			deepCopyTo(t);
			t.rule = rule;
			t.clueModifiers = clueModifiers;
			t.name = name;
			t.decision = decision;
			deepCopyArray(indices, t.indices = new Index[indices.length]);
			t.expr = expr.deepCopy();
			t.sym = sym;
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class Index extends Tree {
		public Tree tpe;
		public String name;
		public Tree initializer;
		public VarSymbol sym;
		private boolean simple;

		public Index(int pos, Tree tpe, String name, NewArray initializer) {
			super(INDEX);
			this.pos = pos;
			this.tpe = tpe;
			this.name = name;
			this.initializer = initializer;
			simple = true;
			Tree[] init = initializer.init;
			for (int i = 0; i < init.length && simple; i++) {
				simple &= init[i] instanceof Literal || isMinMaxValue(init[i]);
			}
		}

		private static boolean isMinMaxValue(Tree t) {
			if (t instanceof Select) {
				Select s = (Select) t;
				if (s.qualifier instanceof Ident && ("MIN_VALUE".equals(s.name) || "MAX_VALUE".equals(s.name))) {
					String name = ((Ident) s.qualifier).name.intern();
					return name == "Byte"
						|| name == "Short"
						|| name == "Character"
						|| name == "Integer"
						|| name == "Long"
						|| name == "Float"
						|| name == "Double";
				}
			}
			return false;
		}

		public boolean isSimple() {
			return simple;
		}

		public Tree[] getInitializers() {
			return ((NewArray) initializer).init;
		}

		private Index() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Symbol symbol() {
			return sym;
		}

		public Tree deepCopy() {
			Index t = new Index();
			deepCopyTo(t);
			t.tpe = tpe.deepCopy();
			t.name = name;
			t.initializer = initializer.deepCopy();
			t.sym = sym;
			t.simple = simple;
			return t;
		}
	}

	public static class MethodDecl extends Tree {
		public Tree restpe;
		public String name;
		public VarDecl[] params;
		public Tree[] thrown;
		public String body;
		public MethodSymbol sym;

		public MethodDecl(int pos, Tree restpe, String name, VarDecl[] params, Tree[] thrown, String body) {
			super(METHOD);
			this.pos = pos;
			this.restpe = restpe;
			this.name = name;
			this.params = params;
			this.thrown = thrown;
			this.body = body;
		}

		private MethodDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Symbol symbol() {
			return sym;
		}

		public Tree deepCopy() {
			MethodDecl t = new MethodDecl();
			deepCopyTo(t);
			t.restpe = restpe.deepCopy();
			t.name = name;
			deepCopyArray(params, t.params = new VarDecl[params.length]);
			deepCopyArray(thrown, t.thrown = new Tree[thrown.length]);
			t.body = body;
			t.sym = sym;
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class VarDecl extends Tree {
		public int modifiers;
		public Tree tpe;
		public String name;
		public Tree initializer;
		public VarSymbol sym;

		public VarDecl(int pos, int modifiers, Tree tpe, String name, Tree initializer) {
			super(VAR);
			this.pos = pos;
			this.modifiers = modifiers;
			this.tpe = tpe;
			this.name = name;
			this.initializer = initializer;
		}

		private VarDecl() {
		}

		public VarDecl(int pos, Tree tpe, String name, Tree initializer) {
			this(pos, 0, tpe, name, initializer);
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Symbol symbol() {
			return sym;
		}

		public Tree deepCopy() {
			VarDecl t = new VarDecl();
			deepCopyTo(t);
			t.modifiers = modifiers;
			t.tpe = tpe.deepCopy();
			t.name = name;
			t.initializer = initializer.deepCopy();
			t.sym = sym;
			return t;
		}
	}

	public static class Quantified extends Tree {
		public int quantifier;
		public String[] vars;
		public Tree expr;
		public Tree valueExpr;
		public Symbol[] sVars;
		public boolean stop;

		public Quantified(int pos, int quantifier, String[] vars, Tree expr, Tree valueExpr) {
			super(QUANTIFIED);
			this.pos = pos;
			this.quantifier = quantifier;
			this.vars = vars;
			this.expr = expr;
			this.valueExpr = valueExpr;
		}

		public Quantified(int pos, int quantifier, String[] vars, Tree expr) {
			this(pos, quantifier, vars, expr, null);
		}

		private Quantified() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Quantified t = new Quantified();
			deepCopyTo(t);
			t.quantifier = quantifier;
			t.vars = (String[]) vars.clone();
			t.expr = expr.deepCopy();
			if (valueExpr != null) {
				t.valueExpr = valueExpr.deepCopy();
			}
			t.sVars = sVars;
			t.stop = stop;
			return t;
		}
	}

	public static class Let extends Tree {
		public VarDecl[] binders;
		public Tree expr;

		public Let(int pos, VarDecl[] binders, Tree expr) {
			super(LET);
			this.pos = pos;
			this.binders = binders;
			this.expr = expr;
		}

		private Let() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Let t = new Let();
			deepCopyTo(t);
			deepCopyArray(binders, t.binders = new VarDecl[binders.length]);
			t.expr = expr.deepCopy();
			return t;
		}
	}

	public static class Shorthand extends Tree {
		public int form;
		public Tree[] exprs;
		public Tree cond;
		public Tree numPerConjunct;
		public Tree minNumMoved;
		public Type baseType;

		public Shorthand(int pos, int form, Tree[] exprs, Tree cond) {
			super(SHORTHAND);
			this.pos = pos;
			this.form = form;
			this.exprs = exprs;
			this.cond = cond;
		}

		public Shorthand(int pos, int form, Tree[] exprs, Tree cond, Tree numPerConjunct, Tree minNumMoved) {
			this(pos, form, exprs, cond);
			this.numPerConjunct = numPerConjunct;
			this.minNumMoved = minNumMoved;
		}

		private Shorthand() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Shorthand t = new Shorthand();
			deepCopyTo(t);
			t.form = form;
			deepCopyArray(exprs, t.exprs = new Tree[exprs.length]);
			if (cond != null) {
				t.cond = cond.deepCopy();
			}
			if (numPerConjunct != null) {
				t.numPerConjunct = numPerConjunct.deepCopy();
			}
			if (minNumMoved != null) {
				t.minNumMoved = minNumMoved.deepCopy();
			}
			t.baseType = baseType;
			return t;
		}
	}

	public static class Valid extends Tree {
		public Tree access;

		public Valid(int pos, Tree access) {
			super(VALID);
			this.pos = pos;
			this.access = access;
		}

		private Valid() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Valid t = new Valid();
			deepCopyTo(t);
			t.access = access.deepCopy();
			return t;
		}
	}

	public static class If extends Tree {
		public Tree cond;
		public Tree thenp;
		public Tree elsep;

		public If(int pos, Tree cond, Tree thenp, Tree elsep) {
			super(IF);
			this.pos = pos;
			this.cond = cond;
			this.thenp = thenp;
			this.elsep = elsep;
		}

		private If() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof If) {
				If i = (If)t;
				return cond.isSameAs(i.cond) &&
					   thenp.isSameAs(i.thenp) &&
					   elsep.isSameAs(i.elsep);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			If t = new If();
			deepCopyTo(t);
			t.cond = cond.deepCopy();
			t.thenp = thenp.deepCopy();
			t.elsep = elsep.deepCopy();
			return t;
		}
	}

	public static class Apply extends Tree {
		public Tree fun;
		public Tree[] args;

		public Apply(int pos, Tree fun, Tree[] args) {
			super(APPLY);
			this.pos = pos;
			this.fun = fun;
			this.args = args;
		}

		private Apply() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Apply) {
				Apply i = (Apply)t;
				return fun.isSameAs(i.fun) && isSameAs(args, i.args);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			Apply t = new Apply();
			deepCopyTo(t);
			t.fun = fun.deepCopy();
			deepCopyArray(args, t.args = new Tree[args.length]);
			t.pos = pos;
			t.type = type;
			return t;
		}
	}

	public static class New extends Tree {
		public Tree clazz;
		public Tree[] args;

		public New(int pos, Tree clazz, Tree[] args) {
			super(NEW);
			this.pos = pos;
			this.clazz = clazz;
			this.args = args;
		}

		private New() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			New t = new New();
			deepCopyTo(t);
			t.clazz = clazz.deepCopy();
			deepCopyArray(args, t.args = new Tree[args.length]);
			return t;
		}
	}

	public static class NewArray extends Tree {
		public Tree clazz;
		public Tree[] dims;
		public Tree[] init;

		private NewArray() {
		}

		public NewArray(int pos, Tree clazz, Tree[] dims, Tree[] init) {
			super(NEWARRAY);
			this.pos = pos;
			this.clazz = clazz;
			this.dims = dims;
			this.init = init;
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			NewArray t = new NewArray();
			deepCopyTo(t);
			t.clazz = clazz.deepCopy();
			deepCopyArray(dims, t.dims = new Tree[dims.length]);
			deepCopyArray(init, t.init = new Tree[init.length]);
			return t;
		}
	}

	public static class Typeop extends Tree {
		// tag = CAST | TEST
		public Tree tpe;
		public Tree expr;

		private Typeop() {
		}

		public Typeop(int pos, int opcode, Tree tpe, Tree expr) {
			super(opcode);
			this.pos = pos;
			this.tpe = tpe;
			this.expr = expr;
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Typeop) {
				Typeop i = (Typeop)t;
				return tpe.isSameAs(i.tpe) && expr.isSameAs(i.expr);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			Typeop t = new Typeop();
			deepCopyTo(t);
			t.tpe = tpe.deepCopy();
			t.expr = expr.deepCopy();
			return t;
		}
	}

	public static class Unop extends Tree {
		public int opcode; // NOT | COMP | PLUS | MINUS
		public Tree arg;

		private Unop() {
		}

		public Unop(int pos, int opcode, Tree arg) {
			super(UNOP);
			this.pos = pos;
			this.opcode = opcode;
			this.arg = arg;
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Unop) {
				Unop i = (Unop)t;
				return (opcode == i.opcode) && arg.isSameAs(i.arg);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			Unop t = new Unop();
			deepCopyTo(t);
			t.opcode = opcode;
			t.arg = arg.deepCopy();
			t.pos = pos;
			t.type = type;
			return t;
		}
	}

	public static class Binop extends Tree {
		public int opcode; // MULT | DIV | MOD | PLUS | MINUS | LSHIFT |
		// RSHIFT | URSHIFT | LT | GT | LTEQ | GTEQ |
		// EQEQ | NOTEQ | AND | OR | XOR | ANDAND |
		// OROR
		public Tree left;
		public Tree right;

		public Binop(int pos, int opcode, Tree left, Tree right) {
			super(BINOP);
			this.pos = pos;
			this.opcode = opcode;
			this.left = left;
			this.right = right;
		}

		private Binop() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Binop) {
				Binop i = (Binop)t;
				return (opcode == i.opcode) &&
					   left.isSameAs(i.left) &&
					   right.isSameAs(i.right);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			Binop t = new Binop();
			deepCopyTo(t);
			t.opcode = opcode;
			t.left = left.deepCopy();
			t.right = right.deepCopy();
			t.pos = pos;
			t.type = type;
			return t;
		}
	}

	public static class Indexed extends Tree {
		public Tree expr;
		public Tree index;
		public Tree mIndex;
		public MultiClueIndex multiClue;

		public static class MultiClueIndex {
			public Ident multiClue;
			public Index[] indices;
			public int indexNum;
			public MultiClueIndex(Ident multiClue, Index[] indices, int indexNum) {
				this.multiClue = multiClue;
				this.indices = indices;
				this.indexNum = indexNum;
			}
		}

		public Indexed(int pos, Tree expr, Tree index) {
			super(INDEXED);
			this.pos = pos;
			this.expr = expr;
			this.index = index;
		}

		public Indexed(int pos, Tree expr, Tree qIndex, Tree mIndex) {
			this(pos, expr, qIndex);
			this.mIndex = mIndex;
		}

		private Indexed() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Indexed t = new Indexed();
			deepCopyTo(t);
			t.expr = expr.deepCopy();
			t.index = index.deepCopy();
			t.mIndex = mIndex == null ? null : mIndex.deepCopy();
			return t;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Indexed) {
				Indexed i = (Indexed)t;
				return expr.isSameAs(i.expr) && index.isSameAs(i.index);
			} else
				return false;
		}
		
		public boolean equals(Object o) {
			if (o instanceof Indexed) {
				Indexed oi = (Indexed) o;
				return expr.equals(oi.expr)
					&& index.equals(oi.index)
					&& (mIndex == null ? oi.mIndex == null : mIndex.equals(oi.mIndex));
			} else {
				return false;
			}
		}
		
		public int hashCode() {
			return 42;
		}
	}

	public static class Select extends Tree {
		public Tree qualifier;
		public String name;
		public Symbol sym;

		public Select(int pos, Tree qualifier, String name) {
			super(SELECT);
			this.pos = pos;
			this.qualifier = qualifier;
			this.name = name;
		}

		private Select() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return qualifier.isExpr();
		}

		public boolean isType() {
			return qualifier.isType();
		}

		public Symbol symbol() {
			return sym;
		}

		public String toString() {
			return qualifier + "." + name;
		}

		public Tree deepCopy() {
			Select t = new Select();
			deepCopyTo(t);
			t.qualifier = qualifier.deepCopy();
			t.name = name;
			t.sym = sym;
			return t;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Select) {
				Select i = (Select)t;
				return qualifier.isSameAs(i.qualifier) &&
					   name.equals(i.name);
			} else
				return false;
		}
		
		public boolean equals(Object o) {
			if (o instanceof Select) {
				Select os = (Select) o;
				return name.equals(os.name) && qualifier.equals(os.qualifier);
			} else {
				return false;
			}
		}
		
		public int hashCode() {
			return name.hashCode();
		}
	}

	public static class Ident extends Tree {
		public String name;
		public Symbol sym;

		public Ident(int pos, String name) {
			super(IDENT);
			this.pos = pos;
			this.name = name;
		}

		private Ident() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public boolean isType() {
			return true;
		}

		public Symbol symbol() {
			return sym;
		}

		public String toString() {
			return name;
		}

		public Tree deepCopy() {
			Ident t = new Ident();
			deepCopyTo(t);
			t.name = name;
			t.sym = sym;
			return t;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Ident) {
				Ident i = (Ident)t;
				return name.equals(i.name);
			} else
				return false;
		}

		public boolean equals(Object o) {
			if (o instanceof Ident) {
				return name.equals(((Ident) o).name);
			} else {
				return false;
			}
		}
		
		public int hashCode() {
			return name.hashCode();			
		}
	}

	public static class Self extends Tree {
		public int stag;

		public Self(int pos, int stag) {
			super(SELF);
			this.pos = pos;
			this.stag = stag;
		}

		private Self() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Self t = new Self();
			deepCopyTo(t);
			t.stag = stag;
			return t;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Self) {
				Self i = (Self)t;
				return stag == i.stag;
			} else
				return false;
		}
		
		public boolean equals(Object o) {
			return o instanceof Self && stag == ((Self) o).stag;
		}
		
		public int hashCode() {
			return stag;
		}
	}

	public static class ArrayType extends Tree {
		public Tree tpe;

		public ArrayType(int pos, Tree tpe) {
			super(ARRAYTYPE);
			this.pos = pos;
			this.tpe = tpe;
		}

		private ArrayType() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isType() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof ArrayType) {
				ArrayType i = (ArrayType)t;
				return tpe.isSameAs(i.tpe);
			} else
				return false;
		}

		public Tree deepCopy() {
			ArrayType t = new ArrayType();
			deepCopyTo(t);
			t.tpe = tpe.deepCopy();
			return t;
		}
	}

	public static class PrimitiveType extends Tree {
		public int ttag;

		public PrimitiveType(int pos, int ttag) {
			super(PRIMTYPE);
			this.pos = pos;
			this.ttag = ttag;
		}

		private PrimitiveType() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isType() {
			return true;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof PrimitiveType) {
				PrimitiveType i = (PrimitiveType)t;
				return ttag == i.ttag;
			} else
				return false;
		}
		
		public Tree deepCopy() {
			PrimitiveType t = new PrimitiveType();
			deepCopyTo(t);
			t.ttag = ttag;
			return t;
		}
	}

	public static class Literal extends Tree {
		public int ltag;
		public Object value;

		public Literal(int pos, int ltag, Object value) {
			super(LITERAL);
			this.pos = pos;
			this.ltag = ltag;
			this.value = value;
		}

		private Literal() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isExpr() {
			return true;
		}

		public Tree deepCopy() {
			Literal t = new Literal();
			deepCopyTo(t);
			t.ltag = ltag;
			t.value = value;
			return t;
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Literal) {
				Literal i = (Literal)t;
				return (ltag == i.ltag) && value.equals(i.value);
			} else
				return false;
		}

		public boolean equals(Object o) {
			if (o instanceof Literal) {
				Literal ol = (Literal) o;
				return ltag == ol.ltag && (value == null ? ol.value == null : value.equals(ol.value));
			}
			return false;
		}
		
		public int hashCode() {
			return ltag;
		}
	}
}
