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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.NamePool;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Type;
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
import com.choicemaker.cm.compiler.backend.TargetTree.Assign;
import com.choicemaker.cm.compiler.backend.TargetTree.Block;
import com.choicemaker.cm.compiler.backend.TargetTree.Break;
import com.choicemaker.cm.compiler.backend.TargetTree.Case;
import com.choicemaker.cm.compiler.backend.TargetTree.Catch;
import com.choicemaker.cm.compiler.backend.TargetTree.ClassDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Cond;
import com.choicemaker.cm.compiler.backend.TargetTree.Continue;
import com.choicemaker.cm.compiler.backend.TargetTree.DoWhile;
import com.choicemaker.cm.compiler.backend.TargetTree.For;
import com.choicemaker.cm.compiler.backend.TargetTree.JMethodDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Return;
import com.choicemaker.cm.compiler.backend.TargetTree.Switch;
import com.choicemaker.cm.compiler.backend.TargetTree.Taged;
import com.choicemaker.cm.compiler.backend.TargetTree.Try;
import com.choicemaker.cm.compiler.backend.TargetTree.While;
import com.choicemaker.cm.compiler.parser.TreeList;
import com.choicemaker.cm.core.compiler.CompilerException;


/** A class supporting an incremental abstract syntax tree-based
 *  common subexpression elimination in expressions and statement
 *  sequences.
 *  
 *  @author Matthias Zenger
 */
public class SubExpressions extends TreeGen implements TargetTree.Visitor {
	
	static class HashBucket {
		String name;
		int round;
		Tree tree;
		Set same;
		HashBucket next;
		
		HashBucket(Tree tree, HashBucket next) {
			this.tree = tree;
			this.same = null;
			this.next = next;
		}
		
		int entries() {
			return (same == null) ? 1 : (1 + same.size());
		}
		
		void add(Tree tree) {
			if (this.tree == tree)
				return;
			if (same == null)
				same = new HashSet();
			same.add(tree);
		}
	}
	
	private static final int HASH_TAB_SIZE = 251;
	private static final int DO_NOT_ENTER = -1;
	
	/** An array mapping hashcodes to HashBuckets which represent
	 *  common subexpression classes.
	 */
	HashBucket[] exprs = new HashBucket[HASH_TAB_SIZE];
	
	/** The hashcode of the current expression.
	 */
	int hashcode;
	
	/** The total number of subexpressions in the hashtable.
	 */
	int totalExprs;
	
	/** A map that contains mappings from expressions to HashBuckets;
	 *  i.e. this map can be used to find the common subexpression class
	 *  of a given expression.
	 */
	Map backref = new HashMap();
	
	/** A local name pool.
	 */
	NamePool pool;
	
	/** CSE is done incrementally in several rounds until a fixed-point
	 *  is reached. This variable refers to the current round id.
	 */
	int round;
	
	
	/** The constructor of the CSE visitor.
	 *  
	 *  @param unit     the current compilation unit
	 */
	public SubExpressions(ICompilationUnit unit, NamePool pool) throws CompilerException {
		super(unit);
		this.pool = pool;
	}
	
	/** This method determines if the given term is complex enough so that
	 *  common subexpression elimination produces more efficient code.
	 * 
	 *  @param level  the level of qualification (Select nodes)
	 *  @param tree   the term
	 *  @return       true, iff the term is complex enough
	 */
	private boolean complex(int level, Tree tree) {
		if ((tree.tag == LITERAL) ||
			(tree.tag == IDENT) ||
			(tree.tag == SELF))
			return false;
		else if (tree.tag == SELECT) {
			if (level >= 2)
				return true;
			else
				return complex(level + 1, ((Select)tree).qualifier);
		} else
			return true;
	}
	
	/** This method enters expression t with corresponding hashcode
	 *  into the hashtable if certain criteria are met.
	 */
	private void enter(Tree t, int hashcode) {
		this.hashcode = hashcode;
		if ((hashcode != DO_NOT_ENTER) && (t.type != null) &&
		    (t.type != Type.NONE) && (t.type != Type.ERROR) &&
		    (t.type != Type.VOID) && !t.type.isMethod() &&
		    !t.type.isPackage() && complex(0, t)) {
			totalExprs++;
			if (hashcode < 0)
				hashcode = -hashcode;
			HashBucket bucket = exprs[hashcode % exprs.length];
			if (bucket == null)
				exprs[hashcode % HASH_TAB_SIZE] = bucket =
					new HashBucket(t, null);
			else {
				while ((bucket != null) && !bucket.tree.isSameAs(t))
					bucket = bucket.next;
				if (bucket == null)
					exprs[hashcode % HASH_TAB_SIZE] = bucket =
						new HashBucket(t, exprs[hashcode % HASH_TAB_SIZE]);
				else
					bucket.add(t);
			}
			backref.put(t, bucket);
		}
	}
	
	// Visitor methods
	
	public void visit(Tree t) {
		throw new Error();
	}
	
	public void visit(Bad t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(PackageDecl t) {
		throw new Error();
	}
	
	public void visit(ImportDecl t) {
		throw new Error();
	}
	
	public void visit(ClueSetDecl t) {
		throw new Error();
	}
	
	public void visit(ClueDecl t) {
		throw new Error();
	}
	
	public void visit(Index t) {
		throw new Error();
	}
	
	public void visit(MethodDecl t) {
		throw new Error();
	}
	
	public void visit(VarDecl t) throws CompilerException {
		if (t.initializer != null)
			t.initializer.apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Quantified t) {
		throw new Error();
	}
	
	public void visit(Let t) {
		throw new Error();
	}
	
	public void visit(Shorthand t) {
		throw new Error();
	}
	
	public void visit(Valid t) {
		throw new Error();
	}
	
	public void visit(If t) throws CompilerException {
		t.cond.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		int hcode = hashcode;
		t.thenp.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		hcode = hcode * 31415 + hashcode;
		t.elsep.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		hcode = hcode * 27183 + hashcode;
		enter(t, hcode << 3);
	}
	
	public void visit(Apply t) throws CompilerException {
		t.fun.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		int hcode = hashcode * 199;
		for (int i = 0; i < t.args.length; i++) {
			t.args[i].apply(this);
			if (hashcode == DO_NOT_ENTER)
				return;
			hcode = hcode * 31 + hashcode;
		}
		enter(t, (hcode << 3) | 0x01);
	}
	
	public void visit(New t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(NewArray t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Typeop t) throws CompilerException {
		t.expr.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		int hcode = hashcode;
		if (t.tag == CAST)
			enter(t, (hcode << 3) | 0x02);
		else
			enter(t, (hcode << 3) | 0x03);
	}
	
	public void visit(Unop t) throws CompilerException {
		t.arg.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		enter(t, (((hashcode * 31) + t.opcode) << 3) | 0x04);
	}
	
	public void visit(Binop t) throws CompilerException {
		t.left.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		int hcode = hashcode;
		t.right.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		hcode = ((hcode * 31415 + hashcode) * 31) + t.opcode;
		enter(t, (hcode << 3) | 0x05);
	}
	
	public void visit(Indexed t) throws CompilerException {
		t.expr.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		int hcode = hashcode;
		t.index.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		hcode = (hcode * 31415) + hashcode;
		enter(t, (hcode << 3) | 0x06);
	}
	
	public void visit(Select t) throws CompilerException {
		t.qualifier.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		enter(t, ((hashcode * 31415 + t.name.hashCode()) << 3) | 0x07);
	}
	
	public void visit(Ident t) {
		enter(t, t.name.hashCode());
	}
	
	public void visit(Self t) {
		enter(t, t.stag << 3);
	}
	
	public void visit(ArrayType t) throws CompilerException {
		t.tpe.apply(this);
		if (hashcode == DO_NOT_ENTER)
			return;
		enter(t, (hashcode << 3) | 0x01);
	}
	
	public void visit(PrimitiveType t) {
		enter(t, (t.ttag << 6) | 0x02);
	}
	
	public void visit(Literal t) {
		enter(t, (t.value.hashCode() << 3) + t.ltag);
	}

	public void visit(ClassDecl t) throws CompilerException {
		throw new Error();
	}
	
	public void visit(JMethodDecl t) throws CompilerException {
		throw new Error();
	}
	
	public void visit(Block t) throws CompilerException {
		for (int i = 0; i < t.stats.length; i++)
			t.stats[i].apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Cond t) throws CompilerException {
		t.cond.apply(this);
		t.thenp.apply(this);
		if (t.elsep != null)
			t.elsep.apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(While t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(DoWhile t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(For t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Taged t) throws CompilerException {
		t.stat.apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Switch t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Case t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Break t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Continue t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Return t) throws CompilerException {
		t.expr.apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Assign t) throws CompilerException {
		t.rhs.apply(this);
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Try t) {
		hashcode = DO_NOT_ENTER;
	}
	
	public void visit(Catch t) {
		hashcode = DO_NOT_ENTER;
	}
	
	TreeList subst(TreeList ts) throws CompilerException {
		TreeList res = new TreeList();
		round++;
		Tree[] decls = ts.toArray();
		for (int i = 0; i < decls.length; i++) {
			Substitute s = new Substitute();
			decls[i].apply(s);
			res.append(s.factors.toArray());
			res.append(decls[i]);
		}
		return res;
	}
	
	/** A visitor for substituting subexpressions.
	 */
	class Substitute implements TargetTree.Visitor {
		TreeList factors = new TreeList();
		Tree res;

		private boolean subst(Tree t) {
			HashBucket clazz = (HashBucket)backref.get(t);
			if (clazz == null)
				return false;
			else if (clazz.entries() < 2)
				return false;
			else if (clazz.name == null) {
				clazz.name = pool.newName("cse$");
				clazz.round = round;
				Tree vdef = new VarDecl(clazz.tree.pos, 0, typeToTree(clazz.tree.pos, clazz.tree.type),
										clazz.name, clazz.tree);
				factors.append(vdef);
			} else if (clazz.round < round)
				return false;
			res = new Ident(t.pos, clazz.name);
			res.type = clazz.tree.type;
			return true;
		}
		
		public void visit(Tree t) {
			res = t;
		}
	
		public void visit(Bad t) {
			res = t;
		}
	
		public void visit(PackageDecl t) {
			res = t;
		}
	
		public void visit(ImportDecl t) {
			res = t;
		}
	
		public void visit(ClueSetDecl t) {
			res = t;
		}
	
		public void visit(ClueDecl t) {
			res = t;
		}
	
		public void visit(Index t) {
			res = t;
		}
	
		public void visit(MethodDecl t) {
			res = t;
		}
	
		public void visit(VarDecl t) throws CompilerException {
			t.initializer.apply(this);
			t.initializer = res;
			res = t;
		}
	
		public void visit(Quantified t) {
			res = t;
		}
	
		public void visit(Let t) {
			res = t;
		}
	
		public void visit(Shorthand t) {
			res = t;
		}
	
		public void visit(Valid t) {
			res = t;
		}
	
		public void visit(If t) throws CompilerException {
			if (subst(t))
				return;
			t.cond.apply(this);
			t.cond = res;
			t.thenp.apply(this);
			t.thenp = res;
			t.elsep.apply(this);
			t.elsep = res;
			res = t;
		}
	
		public void visit(Apply t) throws CompilerException {
			if (subst(t))
				return;
			t.fun.apply(this);
			t.fun = res;
			for (int i = 0; i < t.args.length; i++) {
				t.args[i].apply(this);
				t.args[i] = res;
			}
			res = t;
		}
	
		public void visit(New t) {
			res = t;
		}
	
		public void visit(NewArray t) {
			res = t;
		}
	
		public void visit(Typeop t) throws CompilerException {
			if (subst(t))
				return;
			t.expr.apply(this);
			t.expr = res;
			res = t;
		}
	
		public void visit(Unop t) throws CompilerException {
			if (subst(t))
				return;
			t.arg.apply(this);
			t.arg = res;
			res = t;
		}
	
		public void visit(Binop t) throws CompilerException {
			if (subst(t))
				return;
			t.left.apply(this);
			t.left = res;
			t.right.apply(this);
			t.right = res;
			res = t;
		}
	
		public void visit(Indexed t) throws CompilerException {
			if (subst(t))
				return;
			t.expr.apply(this);
			t.expr = res;
			t.index.apply(this);
			t.index = res;
			res = t;
		}
	
		public void visit(Select t) throws CompilerException {
			if (subst(t))
				return;
			t.qualifier.apply(this);
			t.qualifier = res;
			res = t;
		}
	
		public void visit(Ident t) {
			res = t;
		}
	
		public void visit(Self t) {
			res = t;
		}
	
		public void visit(ArrayType t) {
			res = t;
		}
	
		public void visit(PrimitiveType t) {
			res = t;
		}
	
		public void visit(Literal t) {
			res = t;
		}

		public void visit(ClassDecl t) {
			res = t;
		}
	
		public void visit(JMethodDecl t) {
			res = t;
		}
	
		public void visit(Block t) throws CompilerException {
			if (subst(t))
				return;
			for (int i = 0; i < t.stats.length; i++) {
				t.stats[i].apply(this);
				t.stats[i] = res;
			}
			res = t;
		}
		
		public void visit(Cond t) throws CompilerException {
			if (subst(t))
				return;
			t.cond.apply(this);
			t.cond = res;
			t.thenp.apply(this);
			t.thenp = res;
			if (t.elsep != null) {
				t.elsep.apply(this);
				t.elsep = res;
			}
			res = t;
		}
	
		public void visit(While t) {
			res = t;
		}
	
		public void visit(DoWhile t) {
			res = t;
		}
	
		public void visit(For t) {
			res = t;
		}
	
		public void visit(Taged t) throws CompilerException {
			t.stat.apply(this);
			t.stat = res;
			res = t;
		}
	
		public void visit(Switch t) {
			res = t;
		}
	
		public void visit(Case t) {
			res = t;
		}
	
		public void visit(Break t) {
			res = t;
		}
	
		public void visit(Continue t) {
			res = t;
		}
	
		public void visit(Return t) throws CompilerException {
			t.expr.apply(this);
			t.expr = res;
			res = t;
		}
	
		public void visit(Assign t) throws CompilerException {
			t.rhs.apply(this);
			if (res == null)
				throw new Error();
			t.rhs = res;
			res = t;
		}
	
		public void visit(Try t) {
			res = t;
		}
	
		public void visit(Catch t) {
			res = t;
		}
	}
}
