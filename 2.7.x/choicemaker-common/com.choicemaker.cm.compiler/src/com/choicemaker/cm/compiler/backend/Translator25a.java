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
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.NamePool;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.ArrayType;
import com.choicemaker.cm.compiler.Tree.Binop;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.If;
import com.choicemaker.cm.compiler.Tree.Index;
import com.choicemaker.cm.compiler.Tree.Indexed;
import com.choicemaker.cm.compiler.Tree.Literal;
import com.choicemaker.cm.compiler.Tree.New;
import com.choicemaker.cm.compiler.Tree.NewArray;
import com.choicemaker.cm.compiler.Tree.Quantified;
import com.choicemaker.cm.compiler.Tree.Select;
import com.choicemaker.cm.compiler.Tree.VarDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Assign;
import com.choicemaker.cm.compiler.backend.TargetTree.Block;
import com.choicemaker.cm.compiler.backend.TargetTree.Break;
import com.choicemaker.cm.compiler.backend.TargetTree.Catch;
import com.choicemaker.cm.compiler.backend.TargetTree.Cond;
import com.choicemaker.cm.compiler.backend.TargetTree.For;
import com.choicemaker.cm.compiler.backend.TargetTree.JMethodDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Return;
import com.choicemaker.cm.compiler.backend.TargetTree.Taged;
import com.choicemaker.cm.compiler.backend.TargetTree.Try;
import com.choicemaker.cm.compiler.parser.TreeList;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * Translator for CSE (Common Subexpression Elimination).
 *
 * @author    Matthias Zenger
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:00:54 $
 */
public class Translator25a extends Translator25 implements TargetTags, Modifiers, ITranslator {
	
	// The main translator

	public Translator25a(ITargetPrinter targetPrinter, ICompilationUnit unit) throws CompilerException {
		super(targetPrinter,unit);
	}

	/*
	protected Tree[] _calcThenTree(Assign assign, NamePool namePool) {
		//Tree[] then = { new Assign(evalNumIdent, eval_num_ident(t.pos)), new Assign(exprIdent, t.expr)};
	}

	protected Tree[] _calcThenTree(Assign assign, NamePool namePool) {
		//Tree[] then = { new Assign(evalNumIdent, eval_num_ident(t.pos)), new Assign(exprIdent, t.expr)};
		TreeList ts = CSE(new Assign(exprIdent, t.expr), new NamePool());
		TreeList thencode = new TreeList();
		thencode.append(new Assign(evalNumIdent, eval_num_ident(t.pos)));
		thencode.append(ts.toArray());
		Tree[] then = thencode.toArray();
	}
	*/

	public void visit(ClueDecl t) throws CompilerException { // separate procedure only needed if referenced elsewhere
		String clueName = "Clue " + t.name;
		Tree clueNameNumException;
		Tree clueNameNumFired;
		Tree clueNumExpr;
		Tree[] args;
		VarDecl[] params;
		For outer = new For(null, null, null, null);
		For innermost = outer;
		Tree brea = null;
		boolean indexed;
		TreeList setIdxs = null;
		if (t.indices != null && t.indices.length != 0) {
			setIdxs = new TreeList();
			if (/* t.sym.type */ cluesetType == ClueSetType.BOOLEAN) {
				body.append(
					new VarDecl(
						Location.NOPOS,
						int_array(t.pos),
						t.name,
						new NewArray(
							Location.NOPOS,
							int_ident(t.pos),
							new Tree[] { new Literal(Location.NOPOS, INT, new Integer(t.indices.length))},
							null)));
				clueBody.append(
					new Assign(
						new Indexed(Location.NOPOS, new Ident(Location.NOPOS, t.name), zero_lit(t.pos)),
						minus_one_lit(t.pos)));
			}
			indexed = true;
			int numCreatedClues = 1;
			TreeList bArgs = new TreeList();
			bArgs.append(q_ident(t.pos));
			bArgs.append(m_ident(t.pos));
			TreeList bParams = new TreeList();
			bParams.append(qmVarDecl[0]);
			bParams.append(qmVarDecl[1]);
			for (int i = 0; i < t.indices.length; ++i) {
				Index ix = t.indices[i];
				String indexArrayName = "__" + t.name + "__idx__" + ix.name;
				body.append(
					new VarDecl(
						Location.NOPOS,
						Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.FINAL,
						new ArrayType(Location.NOPOS, ix.tpe),
						indexArrayName,
						ix.initializer));
				bArgs.append(
					new Indexed(
						Location.NOPOS,
						new Ident(Location.NOPOS, indexArrayName),
						new Ident(Location.NOPOS, "i" + i)));
				bParams.append(new VarDecl(Location.NOPOS, ix.tpe, ix.name, null));
				String loopIndexName = "i" + i;
				Ident loopIndex = new Ident(Location.NOPOS, loopIndexName);
				VarDecl[] inits = {
					new VarDecl(Location.NOPOS, int_ident(t.pos),
					loopIndexName, zero_lit(t.pos))
				};
				Tree cond =
					new Binop(
						Location.NOPOS,
						LT,
						loopIndex,
						new Select(Location.NOPOS, new Ident(Location.NOPOS, indexArrayName), "length"));
				Tree[] increments = { new Assign(loopIndex, new Binop(Location.NOPOS, PLUS, loopIndex, one_lit(t.pos)))};
				innermost.body = new For(inits, cond, increments, null);
				innermost = (For) innermost.body;
				numCreatedClues *= ((NewArray) ix.initializer).init.length;
				setIdxs.append(
					new Assign(
						new Indexed(
							Location.NOPOS,
							new Ident(Location.NOPOS, t.name),
							new Literal(Location.NOPOS, INT, new Integer(i))),
						new Ident(Location.NOPOS, "i" + i)));
			}
			addIndexedClueDesc(
				t.indices,
				0,
				clueNum,
				t,
				t.name,
				decisionTree(compToDecision(t.decision)),
				new Literal(Location.NOPOS, INT, new Integer(t.dispBeg)),
				new Literal(Location.NOPOS, INT, new Integer(t.dispEnd)));
			For f = (For) outer.body;
			Tree[] tmp1 = { f.inits[0], new Assign(clue_num_ident(t.pos), zero_lit(t.pos))};
			f.inits = tmp1;
			String label = "__t" + t.name;
			outer.body = new Taged(label, outer.body);
			brea = new Break(label);
			Tree[] tmp2 = {
			    innermost.increments[0],
				new Assign(clue_num_ident(t.pos), new Binop(Location.NOPOS, PLUS, clue_num_ident(t.pos), one_lit(t.pos)))
			};
			innermost.increments = tmp2;
			Literal clueNameLit = new Literal(Location.NOPOS, STRING, clueName);
			clueNumExpr =
				new Binop(
					Location.NOPOS,
					PLUS,
					new Literal(Location.NOPOS, INT, new Integer(clueNum + 1)),
					clue_num_ident(t.pos));
			Tree clueNameNum =
				new Binop(
					Location.NOPOS,
					PLUS,
					clueNameLit,
					new Binop(
						Location.NOPOS,
						PLUS,
						new Literal(Location.NOPOS, STRING, " ("),
						new Binop(Location.NOPOS, PLUS, clueNumExpr, new Literal(Location.NOPOS, STRING, ")"))));
			clueNameNumException =
				new Binop(Location.NOPOS, PLUS, clueNameNum, new Literal(Location.NOPOS, STRING, " exception: "));
			clueNameNumFired =
				new Binop(Location.NOPOS, PLUS, clueNameNum, new Literal(Location.NOPOS, STRING, " fired"));
			clueNum += numCreatedClues;
			numClues[compToDecision(t.decision).toInt()] += numCreatedClues;
			args = bArgs.toArray();
			params = (VarDecl[]) bParams.toArray(new VarDecl[bParams.length()]);
		} else {
			indexed = false;
			++clueNum;
			String clueNameNum = clueName + " (" + clueNum + ")";
			clueNameNumException = new Literal(t.pos, STRING, clueNameNum + " exception: ");
			clueNameNumFired = new Literal(t.pos, STRING, clueNameNum + " fired.");
			clueNumExpr = new Literal(t.pos, INT, new Integer(clueNum));
			args = qm(t.pos);
			params = qmVarDecl;
			Tree[] cdArgs = {
				new Literal(t.pos, INT, new Integer(clueNum)),
				new Literal(t.pos, STRING, t.name),
				decisionTree(compToDecision(t.decision)),
				new Literal(t.pos, BOOLEAN, Boolean.valueOf(t.rule)),
				new Literal(t.pos, BYTE, new Byte(modTrans(t.clueModifiers))),
				new Literal(t.pos, INT, new Integer(t.dispBeg)),
				new Literal(t.pos, INT, new Integer(t.dispEnd))
			};
			clueDesc.append(new New(t.pos, clue_desc_ident(t.pos), cdArgs));
			++numClues[compToDecision(t.decision).toInt()];
		}
		Tree[] cArgs = { clueNameNumException, ex_ident(t.pos) };
		Catch[] sCatch = { new Catch(ex_var_decl(t.pos), new Apply(t.pos, cat_error(t.pos), cArgs))};
		vars.add(Var.create(t));
		t.expr.apply(this);
		t.expr = res;
		vars.remove(vars.size() - 1);
		Tree c;
		if (cluesetType == ClueSetType.BOOLEAN || t.rule) {
			Tree[] dArgs = { clueNameNumFired };
			Tree[] aArgs = { clueNumExpr, new Literal(t.pos, BYTE, new Byte(modTrans(t.clueModifiers)))};
			Tree[] stats;
			stats = new Tree[brea == null ? 2 : 4];
			stats[0] = new Apply(t.pos, t.rule ? a_addrule(t.pos) : a_add(t.pos), aArgs);
			Tree logFired = new Apply(t.pos, cat_debug(t.pos), dArgs);
			if (brea == null) { // non-indexed clue
				stats[1] = logFired;
			} else { // indexed clue
				stats[1] = new Cond(cat_debug_enabled(t.pos), logFired, null);
				stats[2] = new Block(setIdxs.toArray());
				stats[3] = brea;
			}
			c = new Cond(
					new Apply(t.pos, new Ident(t.pos, "getClue" + t.name), args),
					new Block(stats),
					null);
		} else {
			Tree gc = new Apply(t.pos, new Ident(t.pos, "getClue" + t.name), args);
			if (t.type == Type.BOOLEAN)
				gc = new If(t.pos, gc, one_lit(t.pos), zero_lit(t.pos));
			c = new Assign(new Indexed(t.pos, a_value(t.pos), clueNumExpr), gc);
		}
		Try sTry = new Try(c, sCatch);
		Cond sCond = new Cond(new Indexed(t.pos, eval_ident(t.pos), clueNumExpr), sTry, null);
		innermost.body = sCond;
		clueBody.append(outer.body);
		String evalNum = "__evalNum" + t.name;
		Ident evalNumIdent = new Ident(t.pos, evalNum);
		String expr = "__expr" + t.name;
		Ident exprIdent = new Ident(t.pos, expr);
		//Tree[] then = { new Assign(evalNumIdent, eval_num_ident(t.pos)), new Assign(exprIdent, t.expr)};
		TreeList ts = CSE(new Assign(exprIdent, t.expr), new NamePool());
		TreeList thencode = new TreeList();
		thencode.append(new Assign(evalNumIdent, eval_num_ident(t.pos)));
		thencode.append(ts.toArray());
		Tree[] then = thencode.toArray();
		Tree co = indexed ? (Tree) true_lit(t.pos) : (Tree)new Binop(t.pos, NOTEQ, evalNumIdent, eval_num_ident(t.pos));
		Cond eval = new Cond(co, new Block(then), null);
		Tree[] aBody = { eval, new Return(new Ident(t.pos, expr))};
		body.append(new VarDecl(t.pos, Modifiers.PRIVATE, int_ident(t.pos), evalNum, null));
		Tree clueType = new Ident(t.pos, t.type.toString());
		body.append(new VarDecl(t.pos, Modifiers.PRIVATE, clueType, expr, null));
		body.append(new JMethodDecl(Modifiers.PRIVATE, "getClue" + t.name, clueType, params, exception_arr(t.pos), aBody));
	}

	/** common subexpression elimination for top-level statement t
	 *  
	 *  @param t the expression to optimize
	 *  @param localpool a pool of free local variable names
	 *  @return the optimized code
	 */
	public TreeList CSE(Tree t, NamePool localpool) throws CompilerException {
		TreeList trees = new TreeList();
		trees.append(t);
		return CSE(trees, localpool);
	}
	
	/** common subexpression elimination for the top-level statements orig
	 *  
	 *  @param orig the expressions to optimize
	 *  @param localpool a pool of free local variable names
	 *  @return the optimized code
	 */
	public TreeList CSE(TreeList orig, NamePool localpool) throws CompilerException {
		TreeList trees = new TreeList();
		for (int i = 0; i < orig.length(); i++)
			trees.append(orig.get(i).deepCopy());
		for (int k = 0; k < 2; k++) {
			SubExpressions subexprs = new SubExpressions(unit, localpool);
			// extract common subexpressions
			trees.apply(subexprs);
			// do subexpression factoring and substitution
			int oldlen;
			// compute fixed-point
			do {
				oldlen = trees.length();
				trees = subexprs.subst(trees);
			} while (trees.length() > oldlen);
		}
		return trees;
	}

	/** unaliasing and chain-assignment elimination
	 *  
	 * @param tree the expression to optimize
	 * @return the optimized expression
	 */
	public Tree unalias(Tree tree) throws CompilerException {
		Unalias u = new Unalias();
		tree.apply(u);
		return u.res;
	}
	
	public boolean visitAorE(Quantified t) throws CompilerException {
		return false;
	}

/*
	public boolean visitAorE(Quantified t) throws CompilerException {
		String auxProcName = ((t.quantifier == EXISTS) ? "__exists" : "__all") + (auxProcNum++);
		if (!t.stop) {
			vars.add(Var.create(t));
			t.expr.apply(this);
			vars.remove(vars.size() - 1);
		} else
			res = t.expr;
		// implement code motion
		Set vars = new HashSet();
		for (int i = 0; i < t.vars.length; i++)
			vars.add(t.vars[i]);
		TreeList[] factors = new TreeList[t.vars.length];
		for (int i = 0; i < t.vars.length - 1; i++) {
			vars.remove(t.vars[i]);
			Factorizer f = new Factorizer(unit, vars, pool);
			res.apply(f);
			if (f.res != null)
				res = f.res;
			factors[i] = f.factors;
			vars.add(t.vars[i]);
		}
		factors[t.vars.length - 1] = new TreeList();
		NamePool localpool = new NamePool();
		Tree inner = (t.quantifier == EXISTS) ?
						new Cond(res, new Return(new Literal(t.pos, BOOLEAN, Boolean.TRUE)), null) :
						new Cond(new Unop(t.pos, NOT, res), new Return(new Literal(t.pos, BOOLEAN, Boolean.FALSE)), null);
		TreeList block = CSE(inner, localpool);
		inner = (block.length() == 1) ? block.get(0) : new Block(block.toArray());
		// create factorized code
		for (int i = t.vars.length - 1; i >= 0; --i) {
			String iteratorName = t.vars[i];
			((VarSymbol)t.sVars[i]).range.deepCopy().apply(this);
			Tree upperBound = new Select(t.pos, res, "length");
			factors[i] = CSE(factors[i], localpool);
			factors[i].append(inner);
			inner = new For(
						new Tree[]{new VarDecl(t.pos, int_ident(t.pos), iteratorName, zero_lit(t.pos))},
						new Binop(t.pos, LT, new Ident(t.pos, iteratorName), upperBound),
						new Tree[]{new Assign(new Ident(t.pos, iteratorName), new Binop(t.pos, PLUS, new Ident(t.pos, iteratorName), one_lit(t.pos)))},
						new Block(factors[i].toArray()));
		}
		TreeList aBody = new TreeList();
		// unalias variables
		inner = unalias(inner);
		aBody.append(inner);
		aBody.append(new Return(new Literal(t.pos, BOOLEAN, new Boolean(t.quantifier == ALL))));
		TreeList param = new TreeList();
		TreeList args = new TreeList();
		baseAndOuterParams(param, args);
		body.append(new JMethodDecl(
						Modifiers.PRIVATE,
						auxProcName,
						boolean_ident(t.pos),
						(VarDecl[])param.toArray(new VarDecl[param.length()]),
						exception_arr(t.pos),
						aBody.toArray()));
		res = new Apply(t.pos, new Ident(t.pos, auxProcName), args.toArray());
		res.type = t.type;
		return true;
	}
*/	

}

