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
 *  invariant code motion technique.
 *  
 *  @author Matthias Zenger
 */
class Factorizer extends TreeGen implements TargetTree.Visitor {
	
	/** This set contains the names of variables that must not
	 *  be moved.
	 */
	private Set exclude;
	
	/** A supplier of fresh names.
	 */
	private NamePool pool;
	
	/** The result of the visitor. If res == null, then the expression
	 *  may be moved. Otherwise, res refers to the new (transformed)
	 *  expression.
	 */
	public Tree res;
	
	/** This list refers to all the expressions being moved. It contains
	 *  variable declarations.
	 */
	public TreeList factors = new TreeList();
	
	static int num = 0; //DEBUG
	
	/** The constructor
	 *  
	 *  @param unit     the current compilation unit
	 *  @param exclude  the variables that cannot be moved
	 *  @param pool     a supplier of fresh names
	 */
	public Factorizer(ICompilationUnit unit, Set exclude, NamePool pool) throws CompilerException {
		super(unit);
		this.exclude = exclude;
		this.pool = pool;
	}
	
	/** This method determines if the given term is complex enough so that
	 *  code motion produces more efficient code.
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
			if (level > 2)
				return true;
			else
				return complex(level + 1, ((Select)tree).qualifier);
		} else
			return true;
	}
	
	/** Factor out the given term, i.e. replace it with a variable name.
	 * 
	 *  @param   the term to move
	 *  @return  a variable representing the moved term
	 */
	private Tree factor(Tree tree) {
		if ((tree.type == null) ||
		    (tree.type == Type.NONE)) {
			//System.out.print("  cannot factor ");
			//new TargetPrinter25().printExpr(tree).flush();
			//System.out.println();
			return tree;
		} else if (tree.type.isMethod()) {
			if ((tree.tag == SELECT) &&
			    ((Select)tree).name.equals("equals")) {
				Tree res = new Select(tree.pos, factor(((Select)tree).qualifier), "equals");
				res.type = tree.type;
				return res;
			} else
				return tree;
		} else if ((tree.type == Type.NULL) ||
		           !complex(0, tree)) {
			return tree;
		} else {
			String name = pool.newName();
			//System.out.print((num++) + "  factor ");
			//new TargetPrinter25().printExpr(tree).flush();
			//System.out.println(" => " + name);
			Tree vdef = new VarDecl(tree.pos, 0, typeToTree(tree.pos, tree.type), name, tree);
			factors.append(vdef);
			Tree res = new Ident(tree.pos, name);
			res.type = tree.type;
			return res;
		}
	}
	
	// Visitor methods
	
	public void visit(Tree t) {
		throw new Error();
	}
	
	public void visit(Bad t) {
		res = null;
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
	
	public void visit(VarDecl t) {
		throw new Error();
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
		res = t;
	}
	
	public void visit(Apply t) throws CompilerException {
		t.fun.apply(this);
		boolean pure = (res == null);
		Tree fun = res;
		Tree[] args = new Tree[t.args.length];
		for (int i = 0; i < t.args.length; i++) {
			res = null;
			t.args[i].apply(this);
			pure = pure && (res == null);
			if (res != null)
				args[i] = res;
		}
		if (!pure) {
			for (int i = 0; i < args.length; i++)
				if (args[i] == null)
					args[i] = factor(t.args[i]);
			res = new Apply(t.pos, (fun == null) ? factor(t.fun) : fun, args);
			res.type = t.type;
		}
	}
	
	public void visit(New t) {
		res = t;
	}
	
	public void visit(NewArray t) {
		res = t;
	}
	
	public void visit(Typeop t) throws CompilerException {
		t.expr.apply(this);
		if (res != null) {
			res = new Typeop(t.pos, t.tag, t.tpe, res);
			res.type = t.type;
		}
	}
	
	public void visit(Unop t) throws CompilerException {
		t.arg.apply(this);
		if (res != null) {
			res = new Unop(t.pos, t.opcode, res);
			res.type = t.type;
		}
	}
	
	public void visit(Binop t) throws CompilerException {
		t.left.apply(this);
		if (res != null) {
			Tree left = res;
			res = null;
			t.right.apply(this);
			if (res != null)
				res = new Binop(t.pos, t.opcode, left, res);
			else
				res = new Binop(t.pos, t.opcode, left, factor(t.right));
			res.type = t.type;
		} else {
			t.right.apply(this);
			if (res != null) {
				res = new Binop(t.pos, t.opcode, factor(t.left), res);
				res.type = t.type;
			}
		}
	}
	
	public void visit(Indexed t) throws CompilerException {
		t.expr.apply(this);
		if (res != null) {
			Tree expr = res;
			res = null;
			t.index.apply(this);
			if (res != null)
				res = new Indexed(t.pos, expr, res);
			else
				res = new Indexed(t.pos, expr, factor(t.index));
			res.type = t.type;
		} else {
			t.index.apply(this);
			if (res != null) {
				res = new Indexed(t.pos, factor(t.expr), res);
				res.type = t.type;
			}
		}
	}
	
	public void visit(Select t) throws CompilerException {
		t.qualifier.apply(this);
		if (res != null) {
			res = new Select(t.pos, res, t.name);
			res.type = t.type;
		}
	}
	
	public void visit(Ident t) throws CompilerException {
		if (exclude.contains(t.name)) {
			res = new Ident(t.pos, t.name);
			res.type = t.type;
		} else
			res = null;
	}
	
	public void visit(Self t) {
		res = null;
	}
	
	public void visit(ArrayType t) {
		res = null;
	}
	
	public void visit(PrimitiveType t) {
		res = null;
	}
	
	public void visit(Literal t) {
		res = null;
	}

	public void visit(ClassDecl t) {
		throw new Error();
	}
	
	public void visit(JMethodDecl t) {
		throw new Error();
	}
	
	public void visit(Block t) {
		throw new Error();
	}
	
	public void visit(Cond t) {
		throw new Error();
	}
	
	public void visit(While t) {
		throw new Error();
	}
	
	public void visit(DoWhile t) {
		throw new Error();
	}
	
	public void visit(For t) {
		throw new Error();
	}
	
	public void visit(Taged t) {
		throw new Error();
	}
	
	public void visit(Switch t) {
		throw new Error();
	}
	
	public void visit(Case t) {
		throw new Error();
	}
	
	public void visit(Break t) {
		throw new Error();
	}
	
	public void visit(Continue t) {
		throw new Error();
	}
	
	public void visit(Return t) {
		throw new Error();
	}
	
	public void visit(Assign t) {
		throw new Error();
	}
	
	public void visit(Try t) {
		throw new Error();
	}
	
	public void visit(Catch t) {
		throw new Error();
	}
}
