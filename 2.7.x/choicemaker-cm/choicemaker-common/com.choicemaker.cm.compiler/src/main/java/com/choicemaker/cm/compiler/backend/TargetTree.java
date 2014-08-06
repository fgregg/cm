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
package com.choicemaker.cm.compiler.backend;

import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * The representation of the abstract syntax of ClueMaker's target language (Java) *
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public abstract class TargetTree extends Tree implements TargetTags {

	/** application of visitors
	 */
	public void apply(Tree.Visitor v) throws CompilerException {
		apply((Visitor) v);
	}

	public abstract void apply(Visitor v) throws CompilerException;

	/** constructor
	 */
	protected TargetTree(int tag) {
		super(tag);
	}

	protected TargetTree() {
	}

	/** the visitor interface
	 */
	public interface Visitor extends Tree.Visitor {
		void visit(ClassDecl t) throws CompilerException;
		void visit(JMethodDecl t) throws CompilerException;
		void visit(Block t) throws CompilerException;
		void visit(Cond t) throws CompilerException;
		void visit(While t) throws CompilerException;
		void visit(DoWhile t) throws CompilerException;
		void visit(For t) throws CompilerException;
		void visit(Taged t) throws CompilerException;
		void visit(Switch t) throws CompilerException;
		void visit(Case t) throws CompilerException;
		void visit(Break t) throws CompilerException;
		void visit(Continue t) throws CompilerException;
		void visit(Return t) throws CompilerException;
		void visit(Assign t) throws CompilerException;
		void visit(Try t) throws CompilerException;
		void visit(Catch t) throws CompilerException;
	}

	public static class ClassDecl extends TargetTree {
		public int modifiers;
		public String name;
		public Tree superclass;
		public Tree[] interfaces;
		public Tree[] body;

		public ClassDecl(int modifiers, String name, Tree superclass, Tree[] interfaces, Tree[] body) {
			super(CLASSDECL);
			this.modifiers = modifiers;
			this.name = name;
			this.superclass = superclass;
			this.interfaces = interfaces;
			this.body = body;
		}

		private ClassDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			ClassDecl t = new ClassDecl();
			deepCopyTo(t);
			t.modifiers = modifiers;
			t.name = name;
			t.superclass = superclass.deepCopy();
			deepCopyArray(interfaces, t.interfaces = new Tree[interfaces.length]);
			deepCopyArray(body, t.body = new Tree[body.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class JMethodDecl extends TargetTree {
		public int modifiers;
		public String name;
		public Tree restpe;
		public VarDecl[] params;
		public Tree[] thrown;
		public Tree[] body;

		public JMethodDecl(int modifiers, String name, Tree restpe, VarDecl[] params, Tree[] thrown, Tree[] body) {
			super(JMETHODDECL);
			this.modifiers = modifiers;
			this.name = name;
			this.restpe = restpe;
			this.params = params;
			this.thrown = thrown;
			this.body = body;
		}

		private JMethodDecl() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			JMethodDecl t = new JMethodDecl();
			deepCopyTo(t);
			t.modifiers = modifiers;
			t.name = name;
			t.restpe = restpe.deepCopy();
			deepCopyArray(params, t.params = new VarDecl[params.length]);
			deepCopyArray(thrown, t.thrown = new Tree[thrown.length]);
			deepCopyArray(body, t.body = new Tree[body.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class Block extends TargetTree {
		public Tree[] stats;

		public Block(Tree[] stats) {
			super(BLOCK);
			this.stats = stats;
		}

		private Block() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Block t = new Block();
			deepCopyTo(t);
			deepCopyArray(stats, t.stats = new Tree[stats.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class Cond extends TargetTree {
		public Tree cond;
		public Tree thenp;
		public Tree elsep;

		public Cond(Tree cond, Tree thenp, Tree elsep) {
			super(COND);
			this.cond = cond;
			this.thenp = thenp;
			this.elsep = elsep;
		}

		private Cond() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public boolean isSameAs(Tree t) {
			if (t instanceof Cond) {
				Cond i = (Cond)t;
				return cond.isSameAs(i.cond) &&
					   thenp.isSameAs(i.thenp) &&
					   elsep.isSameAs(i.elsep);
			} else
				return false;
		}
		
		public Tree deepCopy() {
			Cond t = new Cond();
			deepCopyTo(t);
			t.cond = cond.deepCopy();
			t.thenp = thenp.deepCopy();
			if (elsep != null)
				t.elsep = elsep.deepCopy();
			return t;
		}
	}

	public static class Try extends TargetTree {
		public Tree body;
		public Catch[] catches;

		public Try(Tree body, Catch[] catches) {
			super(TRY);
			this.body = body;
			this.catches = catches;
		}

		private Try() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Try t = new Try();
			deepCopyTo(t);
			t.body = body.deepCopy();
			deepCopyArray(catches, t.catches = new Catch[catches.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class Catch extends TargetTree {
		public VarDecl ex;
		public Tree body;

		public Catch(VarDecl ex, Tree body) {
			super(CATCH);
			this.ex = ex;
			this.body = body;
		}

		private Catch() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Catch t = new Catch();
			deepCopyTo(t);
			t.ex = (VarDecl) ex.deepCopy();
			t.body = body.deepCopy();
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class While extends TargetTree {
		public Tree cond;
		public Tree body;

		public While(Tree cond, Tree body) {
			super(WHILE);
			this.cond = cond;
			this.body = body;
		}

		private While() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			While t = new While();
			deepCopyTo(t);
			t.cond = cond.deepCopy();
			t.body = body.deepCopy();
			return t;
		}
	}

	public static class DoWhile extends TargetTree {
		public Tree cond;
		public Tree body;

		public DoWhile(Tree cond, Tree body) {
			super(DOWHILE);
			this.cond = cond;
			this.body = body;
		}

		private DoWhile() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			DoWhile t = new DoWhile();
			deepCopyTo(t);
			t.cond = cond.deepCopy();
			t.body = body.deepCopy();
			return t;
		}
	}

	public static class For extends TargetTree {
		public Tree[] inits;
		public Tree cond;
		public Tree[] increments;
		public Tree body;

		public For(Tree[] inits, Tree cond, Tree[] increments, Tree body) {
			super(FOR);
			this.inits = inits;
			this.cond = cond;
			this.increments = increments;
			this.body = body;
		}

		private For() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			For t = new For();
			deepCopyTo(t);
			deepCopyArray(inits, t.inits = new Tree[inits.length]);
			t.cond = cond.deepCopy();
			deepCopyArray(increments, t.increments = new Tree[increments.length]);
			t.body = body.deepCopy();
			return t;
		}
	}

	public static class Switch extends TargetTree {
		public Tree selector;
		public Case[] cases;

		public Switch(Tree selector, Case[] cases) {
			super(SWITCH);
			this.selector = selector;
			this.cases = cases;
		}

		private Switch() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Switch t = new Switch();
			deepCopyTo(t);
			t.selector = selector.deepCopy();
			deepCopyArray(cases, t.cases = new Case[cases.length]);
			return t;
		}

		public boolean isBlock() {
			return true;
		}
	}

	public static class Case extends TargetTree {
		public Tree[] guard;
		public Tree[] body;

		public Case(Tree[] guard, Tree[] body) {
			super(CASE);
			this.guard = guard;
			this.body = body;
		}

		private Case() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Case t = new Case();
			deepCopyTo(t);
			deepCopyArray(guard, t.guard = new Tree[guard.length]);
			deepCopyArray(body, t.body = new Tree[body.length]);
			return t;
		}
	}

	public static class Taged extends TargetTree {
		public String label;
		public Tree stat;

		public Taged(String label, Tree stat) {
			super(TAGED);
			this.label = label;
			this.stat = stat;
		}

		private Taged() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Taged t = new Taged();
			deepCopyTo(t);
			t.label = label;
			t.stat = stat.deepCopy();
			return t;
		}
	}

	public static class Break extends TargetTree {
		public String label;

		public Break(String label) {
			super(BREAK);
			this.label = label;
		}

		private Break() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Break t = new Break();
			deepCopyTo(t);
			t.label = label;
			return t;
		}
	}

	public static class Continue extends TargetTree {
		public String label;

		public Continue(String label) {
			super(CONTINUE);
			this.label = label;
		}

		private Continue() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Continue t = new Continue();
			deepCopyTo(t);
			t.label = label;
			return t;
		}
	}

	public static class Return extends TargetTree {
		public Tree expr;

		public Return(Tree expr) {
			super(RETURN);
			this.expr = expr;
		}

		private Return() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Return t = new Return();
			deepCopyTo(t);
			t.expr = expr.deepCopy();
			return t;
		}
	}

	public static class Assign extends TargetTree {
		public Tree lhs;
		public Tree rhs;

		public Assign(Tree lhs, Tree rhs) {
			super(ASSIGN);
			this.lhs = lhs;
			this.rhs = rhs;
		}

		private Assign() {
		}

		public void apply(Visitor v) throws CompilerException {
			v.visit(this);
		}

		public Tree deepCopy() {
			Assign t = new Assign();
			deepCopyTo(t);
			t.lhs = lhs.deepCopy();
			t.rhs = rhs.deepCopy();
			return t;
		}
	}
}
