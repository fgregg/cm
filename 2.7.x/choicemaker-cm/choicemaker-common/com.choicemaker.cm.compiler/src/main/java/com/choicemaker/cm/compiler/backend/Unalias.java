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
import java.util.Map;

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


/** A visitor for unaliasing variables.
 * 
 *  @author Matthias Zenger
 */
class Unalias implements TargetTree.Visitor, TargetTags {
	Tree res;
	Map aliases = new HashMap();


	public void visit(Tree t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Bad t) {
		res = t;
	}
	
	public void visit(PackageDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(ImportDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(ClueSetDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(ClueDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Index t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(MethodDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(VarDecl t) throws CompilerException {
		if (t.initializer.tag == IDENT) {
			aliases.put(t.name, ((Tree.Ident)t.initializer).name);
			res = null;
		} else {
			t.initializer.apply(this);
			t.initializer = res;
			res = t;
		}
	}
		
	public void visit(Quantified t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Let t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Shorthand t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Valid t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(If t) throws CompilerException {
		t.cond.apply(this);
		t.cond = res;
		t.thenp.apply(this);
		t.thenp = res;
		t.elsep.apply(this);
		t.elsep = res;
		res = t;
	}
	
	public void visit(Apply t) throws CompilerException {
		t.fun.apply(this);
		t.fun = res;
		for (int i = 0; i < t.args.length; i++) {
			t.args[i].apply(this);
			t.args[i] = res;
		}
		res = t;
	}
	
	public void visit(New t) throws CompilerException {
		for (int i = 0; i < t.args.length; i++) {
			t.args[i].apply(this);
			t.args[i] = res;
		}
		res = t;
	}
		
	public void visit(NewArray t) {
		res = t;
	}
	
	public void visit(Typeop t) throws CompilerException {
		t.expr.apply(this);
		t.expr = res;
		res = t;
	}
	
	public void visit(Unop t) throws CompilerException {
		t.arg.apply(this);
		t.arg = res;
		res = t;
	}
	
	public void visit(Binop t) throws CompilerException {
		t.left.apply(this);
		t.left = res;
		t.right.apply(this);
		t.right = res;
		res = t;
	}
		
	public void visit(Indexed t) throws CompilerException {
		t.expr.apply(this);
		t.expr = res;
		t.index.apply(this);
		t.index = res;
		res = t;
	}
	
	public void visit(Select t) throws CompilerException {
		t.qualifier.apply(this);
		t.qualifier = res;
		res = t;
	}
		
	public void visit(Ident t) throws CompilerException {
		String alias = null;
		String target = t.name;
		while (target != null) {
			alias = target;
			target = (String)aliases.get(alias);
		}
		res = new Ident(t.pos, alias);
		res.type = t.type;
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

	public void visit(ClassDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(JMethodDecl t) throws CompilerException {
		throw new CompilerException("invalid compilation path");
	}
	
	public void visit(Block t) throws CompilerException {
		TreeList newstats = new TreeList();
		for (int i = 0; i < t.stats.length; i++) {
			t.stats[i].apply(this);
			if (res != null)
				newstats.append(res);
		}
		t.stats = newstats.toArray();
		res = t;
	}
		
	public void visit(Cond t) throws CompilerException {
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
	
	public void visit(While t) throws CompilerException {
		t.cond.apply(this);
		t.cond = res;
		t.body.apply(this);
		t.body = res;
		res = t;
	}
	
	public void visit(DoWhile t) throws CompilerException {
		t.cond.apply(this);
		t.cond = res;
		t.body.apply(this);
		t.body = res;
		res = t;
	}
	
	public void visit(For t) throws CompilerException {
		TreeList ts = new TreeList();
		for (int i = 0; i < t.inits.length; i++) {
			t.inits[i].apply(this);
			if (res != null)
				ts.append(res);
		}
		t.inits = ts.toArray();
		t.cond.apply(this);
		t.cond = res;
		ts = new TreeList();
		for (int i = 0; i < t.increments.length; i++) {
			t.increments[i].apply(this);
			if (res != null)
				ts.append(res);
		}
		t.increments = ts.toArray();
		t.body.apply(this);
		t.body = res;
		res = t;
	}
	
	public void visit(Taged t) throws CompilerException {
		t.stat.apply(this);
		t.stat = res;
		res = t;
	}
	
	public void visit(Switch t) throws CompilerException {
		t.selector.apply(this);
		t.selector = res;
		for (int i = 0; i < t.cases.length; i++) {
			t.cases[i].apply(this);
			t.cases[i] = (TargetTree.Case)res;
		}
		res = t;
	}
	
	public void visit(Case t) throws CompilerException {
		for (int i = 0; i < t.guard.length; i++) {
			t.guard[i].apply(this);
			t.guard[i] = res;
		}
		TreeList newstats = new TreeList();
		for (int i = 0; i < t.body.length; i++) {
			t.body[i].apply(this);
			if (res != null)
				newstats.append(res);
		}
		t.body = newstats.toArray();
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
		if (aliases.get(t.lhs) != null)
			throw new CompilerException("invalid compilation path");
		t.rhs.apply(this);
		if (res == null)
			throw new CompilerException("invalid compilation path");
		t.rhs = res;
		res = t;
	}
	
	public void visit(Try t) throws CompilerException {
		t.body.apply(this);
		t.body = res;
		for (int i = 0; i < t.catches.length; i++) {
			t.catches[i].apply(this);
			t.catches[i] = (TargetTree.Catch)res;
		}
		res = t;
	}
	
	public void visit(Catch t) throws CompilerException {
		t.body.apply(this);
		t.body = res;
		res = t;
	}
}
