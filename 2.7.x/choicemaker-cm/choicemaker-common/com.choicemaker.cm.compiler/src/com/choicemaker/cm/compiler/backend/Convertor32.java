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


import com.choicemaker.cm.compiler.ICompilationUnit;
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
import com.choicemaker.cm.core.compiler.CompilerException;



/** Convertor from 3-value expresiion to positive and negative preconditions
 *  
 *  @author Elmer Moussikaev
 */
class Convertor32 extends TreeGen implements TargetTree.Visitor {

	/** Flag that defines if convertion to positive or negative precondition 
	 */
	public boolean isPosConv;
	
	/** The result of the visitor. 3 value tree converted to Boolen tree.
	 */
	public Tree resExp;
	
	/** If resExp is null then the result expression is just "true" or "false".
	 *  ResConst stores this value. 
	 */
	public boolean resConst;
	
	/** Flag that defines if negation should be applied to the visiting tree
	 */
	public boolean neg;

	static int num = 0; //DEBUG
	
	/** The constructor
	 *  
	 *  @param unit     the current compilation unit
	 *  @param pc  		flag that defines if conversion positive or negative
	 *  @param negation flag that defines if negation should be applied to the expression
	 */
	public Convertor32(ICompilationUnit unit, boolean pc, boolean negation) throws CompilerException {
		super(unit);
		isPosConv = pc;
		this.neg = negation;
	}
	
	
	
	// Visitor methods
	
	public void visit(Tree t) {
		throw new Error();
	}
	
	public void visit(Bad t) {
		throw new Error();
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
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(Apply t) throws CompilerException {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(New t) {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(NewArray t) {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(Typeop t) throws CompilerException {
		t.expr.apply(this);
	}
	
	public void visit(Unop t) throws CompilerException {
		boolean negation = this.neg;
		if( t.opcode == Tags.NOT )	{
			this.neg = !this.neg; 		
			t.arg.apply(this);
		}
		else {
			if(this.neg)
				resExp = new Unop(t.pos, Tags.NOT, t);
			else	
				resExp = t;
		}
		this.neg = negation;
	}
	
	public void visit(Binop t) throws CompilerException {
		Tree 		exp1;
		boolean		const1;
		boolean negation = this.neg;
		if(!negation&&t.opcode == Tags.OROR || negation&&t.opcode == Tags.ANDAND){
			t.left.apply(this);
			exp1 = resExp;
			const1 = resConst;
			this.neg = negation;
			t.right.apply(this);
			if(exp1 == null){
				if(const1){
					resExp = null;resConst = true; // T || any = T
				}
				// else  F|| any = any so the result of t.right.apply(this) is already the result of || expression
			}else{
				if(resExp == null){
					if(!resConst)
						resExp = exp1; // exp1 || F = exp1
					//else   exp1 || T = T so the result of t.right.apply(this) is already the result of || expression 
				} else {
					resExp = new Binop(t.pos, Tags.OROR, exp1, resExp);
				}
			}
			 
		} else if (!negation&&t.opcode == Tags.ANDAND || negation&&t.opcode == Tags.OROR) {
			t.left.apply(this);
			exp1 = resExp;
			const1 = resConst;
			this.neg = negation;
			t.right.apply(this);
			if(exp1 == null){
				if(!const1){
					resExp = null; resConst = false; // F && any = F
				}
				//else T&& any = any so the result of t.right.apply(this) is already the result of && expression 
			}
			else {
				if(resExp == null){
					if(resConst){
						resExp = exp1;// exp1 && T = exp1
					}
					//else exp1 && F = F  so the result of t.right.apply(this) is already the result of && expression					
				}
				else{
					resExp = new Binop(t.pos, Tags.ANDAND, exp1, resExp);
				}
			}
		}
		this.neg = negation;
	}
	
	public void visit(Literal t) {
		if (t.ltag == Tags.UNKNOWN){
			resExp = null; resConst = !isPosConv;
		}
		else {
			if(this.neg)
				resExp = new Unop(t.pos, Tags.NOT, t);
			else	
				resExp = t;
		}
	}

	
	public void visit(Indexed t) throws CompilerException {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(Select t) throws CompilerException {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(Ident t) throws CompilerException {
		if(this.neg)
			resExp = new Unop(t.pos, Tags.NOT, t);
		else	
			resExp = t;
	}
	
	public void visit(Self t) {
		throw new Error();
	}
	
	public void visit(ArrayType t) {
		throw new Error();
	}
	
	public void visit(PrimitiveType t) {
		throw new Error();
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
