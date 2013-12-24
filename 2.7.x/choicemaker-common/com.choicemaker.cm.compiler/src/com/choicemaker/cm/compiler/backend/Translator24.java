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

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Symbol.ClueSymbol;
import com.choicemaker.cm.compiler.Symbol.VarSymbol;
import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.ArrayType;
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
import com.choicemaker.cm.compiler.Type.ObjectType;
import com.choicemaker.cm.compiler.backend.TargetTree.Assign;
import com.choicemaker.cm.compiler.backend.TargetTree.Block;
import com.choicemaker.cm.compiler.backend.TargetTree.Break;
import com.choicemaker.cm.compiler.backend.TargetTree.Catch;
import com.choicemaker.cm.compiler.backend.TargetTree.ClassDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Cond;
import com.choicemaker.cm.compiler.backend.TargetTree.Continue;
import com.choicemaker.cm.compiler.backend.TargetTree.For;
import com.choicemaker.cm.compiler.backend.TargetTree.JMethodDecl;
import com.choicemaker.cm.compiler.backend.TargetTree.Return;
import com.choicemaker.cm.compiler.backend.TargetTree.Taged;
import com.choicemaker.cm.compiler.backend.TargetTree.Try;
import com.choicemaker.cm.compiler.parser.DefaultVisitor;
import com.choicemaker.cm.compiler.parser.TreeList;
import com.choicemaker.cm.compiler.typechecker.DeriveType;
import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.ClueSetType;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ExtDecision;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.util.MessageUtil;

/**
 * Main translator class. Should be completely rewritten. See spreadsheet for todos.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:00:54 $
 */
public class Translator24 extends DefaultVisitor implements TargetTags, Modifiers, ITranslator {
	private static final int SS_NONE = 0;
	private static final int SS_Q = 1;
	private static final int SS_M = 2;

	/** The target printer used to generate code. */
	private final ITargetPrinter targetPrinter;
	
	/** Corresponding compilation unit. */
	private final ICompilationUnit unit;
	
	/** Target of compilation. */
	private TreeList target;
	/** Body of class. */
	private TreeList body;
	/** Body of method getActiveClues */
	private TreeList clueBody;
	/** Number of clues */
	private int size;
	/** Number of clue */
	private int clueNum;
	/** Return through global variable for visitors */
	private Tree res;
	/** Number of auxiliary procedure */
	private int auxProcNum;
	/** Base class type */
	private Ident baseClassType;
	/** Variable declaration of Q and M of base class type type */
	private VarDecl[] qmVarDecl;
	/** Outer vars */
	private List vars;
	/** Number of clues by decision. */
	private int[] numClues;
	/** Clue descriptors */
	TreeList clueDesc;
	/** Type of the clueset */
	private ClueSetType cluesetType;
	/** Whether clueset has decisions */
	private boolean hasDecision;

	TreeList[] shorthandFields;
	ListMap[] shorthandIndexes;
	int[] shorthandIndexNum;
	boolean replace;
	int shorthand;
	boolean insideShorthand;
	int simpleShorthand;

	/** Constants */
	private static final Ident CLUE_DESC_IDENT = new Ident(Location.NOPOS, "com.choicemaker.cm.core.ClueDesc");
	private static final Tree[] INTERFACES = { new Ident(Location.NOPOS, "com.choicemaker.cm.core.ClueSet")};
	private static final ArrayType CLUE_DESC_ARR_IDENT = new ArrayType(Location.NOPOS, CLUE_DESC_IDENT);
	private static final Ident INT_IDENT = new Ident(Location.NOPOS, "int");
	private static final ArrayType INT_ARRAY = new ArrayType(Location.NOPOS, INT_IDENT);
	private static final Ident SIZES_IDENT = new Ident(Location.NOPOS, "sizes");
	private static final Apply D_TO_INT_EXPR =
		new Apply(Location.NOPOS, new Select(Location.NOPOS, new Ident(Location.NOPOS, "d"), "toInt"), null);
	private static final Ident DECISION_IDENT = new Ident(Location.NOPOS, "com.choicemaker.cm.core.Decision");
	private static final Ident EXT_DECISION_IDENT = new Ident(Location.NOPOS, "com.choicemaker.cm.core.ExtDecision");
	private static final VarDecl[] DECISION_VAR_DECLS = { new VarDecl(Location.NOPOS, DECISION_IDENT, "d", null)};
	private static final Tree[] GET_CLUE_DESC_BODY = { new Return(new Ident(Location.NOPOS, "clueDescs"))};
	private static final Tree GET_CLUE_DESC =
		new JMethodDecl(Modifiers.PUBLIC, "getClueDesc", CLUE_DESC_ARR_IDENT, null, null, GET_CLUE_DESC_BODY);
	private static final Ident ACTIVE_CLUES_IDENT = new Ident(Location.NOPOS, "com.choicemaker.cm.core.ActiveClues");
	private static final Ident BOOLEAN_ACTIVE_CLUES_IDENT =
		new Ident(Location.NOPOS, "com.choicemaker.cm.core.BooleanActiveClues");
	private static final Ident INT_ACTIVE_CLUES_IDENT =
		new Ident(Location.NOPOS, "com.choicemaker.cm.core.IntActiveClues");
	private static final Ident RECORD_IDENT = new Ident(Location.NOPOS, "com.choicemaker.cm.core.Record");
	private static final Ident BOOLEAN_IDENT = new Ident(Location.NOPOS, "boolean");
	private static final Ident BYTE_IDENT = new Ident(Location.NOPOS, "byte");
	private static final VarDecl[] GET_ACTIVE_CLUES_PARAMS =
		{
			new VarDecl(Location.NOPOS, RECORD_IDENT, "qi", null),
			new VarDecl(Location.NOPOS, RECORD_IDENT, "mi", null),
			new VarDecl(Location.NOPOS, new ArrayType(Location.NOPOS, BOOLEAN_IDENT), "eval", null)};
	private static final Ident EVAL_NUM_IDENT = new Ident(Location.NOPOS, "__evalNum");
	private static final Literal ZERO_LIT = new Literal(Location.NOPOS, INT, new Integer(0));
	private static final Literal ONE_LIT = new Literal(Location.NOPOS, INT, new Integer(1));
	private static final Literal MINUS_ONE_LIT = new Literal(Location.NOPOS, INT, new Integer(-1));
	private static final Ident EVAL_IDENT = new Ident(Location.NOPOS, "eval");
	private static final Ident EXCEPTION_IDENT = new Ident(Location.NOPOS, "java.lang.Exception");
	private static final VarDecl EX_VAR_DECL = new VarDecl(Location.NOPOS, EXCEPTION_IDENT, "ex", null);
	private static final Tree[] EXCEPTION_ARR = { EXCEPTION_IDENT };
	private static final Ident EX_IDENT = new Ident(Location.NOPOS, "ex");
	private static final Ident CAT_IDENT = new Ident(Location.NOPOS, "cat");
	private static final Select CAT_ERROR = new Select(Location.NOPOS, CAT_IDENT, "error");
	private static final Select CAT_DEBUG = new Select(Location.NOPOS, CAT_IDENT, "debug");
	private static final Select A_ADD = new Select(Location.NOPOS, new Ident(Location.NOPOS, "a"), "add");
	private static final Select A_ADDRULE = new Select(Location.NOPOS, new Ident(Location.NOPOS, "a"), "addRule");
	private static final Select A_VALUE = new Select(Location.NOPOS, new Ident(Location.NOPOS, "a"), "values");
	private static final Tree[] DECISION_TREE = new Tree[ExtDecision.NUM_DECISIONS];
	static {
		for (int i = 0; i < ExtDecision.NUM_DECISIONS; ++i) {
			DECISION_TREE[i] =
				new Select(
					Location.NOPOS,
					i < Decision.NUM_DECISIONS ? DECISION_IDENT : EXT_DECISION_IDENT,
					ExtDecision.valueOf(i).toString().toUpperCase());
		}
	}
	private static final Ident Q_IDENT = new Ident(Location.NOPOS, "q");
	private static final Ident M_IDENT = new Ident(Location.NOPOS, "m");
	private static final Ident[] QM = { Q_IDENT, M_IDENT };
	private static final VarDecl[] E_VAR_DECLS =
		{
			new VarDecl(Location.NOPOS, BOOLEAN_IDENT, "__e", new Literal(Location.NOPOS, BOOLEAN, Boolean.FALSE)),
			new VarDecl(Location.NOPOS, BOOLEAN_IDENT, "__e", new Literal(Location.NOPOS, BOOLEAN, Boolean.TRUE))};
	private static final Ident E_IDENT = new Ident(Location.NOPOS, "__e");
	private static final Tree[] E_VAR_COND = { new Unop(Location.NOPOS, NOT, E_IDENT), E_IDENT };
	private static final Ident CLUE_NUM_IDENT = new Ident(Location.NOPOS, "clueNum");
	private static final Tree[] EMPTY_TREE_ARR = {
	};
	private static final Tree CAT_DEBUG_ENABLED =
		new Apply(Location.NOPOS, new Select(Location.NOPOS, CAT_IDENT, "isDebugEnabled"), EMPTY_TREE_ARR);
	private static final Ident TRUE_IDENT = new Ident(Location.NOPOS, "true");
	private static final Ident INT_ARRAY_LIST_IDENT =
		new Ident(Location.NOPOS, "com.choicemaker.cm.core.util.IntArrayList");
	private static final Tree STRING_VALUE_OF =
		new Select(Location.NOPOS, new Ident(Location.NOPOS, "java.lang.String"), "valueOf");
	private static final Ident ARRAY_LIST_IDENT = new Ident(Location.NOPOS, "java.util.ArrayList");
	private static final Ident[] QR_MR =
		new Ident[] { new Ident(Location.NOPOS, "qr"), new Ident(Location.NOPOS, "mr")};
	private static final String[] QR_MR_NAME = { "qr", "mr" };
	private static final Tree OBJECT_ARRAY =
		new ArrayType(Location.NOPOS, new Ident(Location.NOPOS, "java.lang.Object"));
	private static final Tree OBJECT_ARRAY2 =
		new ArrayType(Location.NOPOS, new ArrayType(Location.NOPOS, new Ident(Location.NOPOS, "java.lang.Object")));

	final private Type stringType;

	public Translator24(ITargetPrinter targetPrinter, ICompilationUnit unit) throws CompilerException {
		this.unit = unit;
		this.targetPrinter = targetPrinter;
		this.stringType = new DeriveType(unit).typeOf("java.lang.String");
		
		// Preconditions
		if (this.targetPrinter == null) {
			throw new IllegalArgumentException("null target printer");
		}
		if (this.unit == null) {
			throw new IllegalArgumentException("null compilation unit");
		}
	}

	public void toJava() {
		throw new UnsupportedOperationException("not implemented");
	}

	public void translate() throws CompilerException {
		vars = new ArrayList();
		baseClassType = new Ident(Location.NOPOS, unit.getBaseClass().toString());
		qmVarDecl = new VarDecl[2];
		qmVarDecl[0] = new VarDecl(Location.NOPOS, baseClassType, "q", null);
		qmVarDecl[1] = new VarDecl(Location.NOPOS, baseClassType, "m", null);
		target = new TreeList();
		body = new TreeList();
		for (int i = 0; i < unit.getDecls().length; ++i) {
			unit.getDecls()[i].apply(this);
		}
		unit.setTarget(target.toArray());
		targetPrinter.printUnit(unit);
	}

	public void visit(PackageDecl t) {
		target.append(
			new PackageDecl(
				t.pos,
				new Select(Location.NOPOS, new Select(Location.NOPOS, t.pckage, "internal"), unit.getSchemaName())));
	}

	public void visit(ImportDecl t) {
		target.append(t);
	}

	public void visit(ClueSetDecl t) throws CompilerException {
		hasDecision = t.decision;
		try {
			cluesetType = TypeConverter.convert(t.type);
		} catch(CompilerException x) {
			unit.error(t.pos, x.getMessage());
			return;
		}
		target.append(new ImportDecl(Location.NOPOS, new Ident(Location.NOPOS, "com.choicemaker.cm.core"), true));
		target.append(new ImportDecl(Location.NOPOS, new Ident(Location.NOPOS, "org.apache.log4j"), true));
		createAuxiliaryMembersBegin(t);
		clueBody = new TreeList();
		createClueBodyHeader(t.body.length);
		clueNum = -1;
		for (int i = 0; i < t.body.length; ++i) {
			t.body[i].apply(this);
			if (res instanceof VarDecl) {
				VarDecl vd = (VarDecl) res;
				body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE, vd.tpe, vd.name, null));
			}
		}
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE,
				ARRAY_LIST_IDENT,
				"__lacc",
				new New(Location.NOPOS, ARRAY_LIST_IDENT, new Tree[0])));
		createClueBodyFooter();
		createAuxiliaryMembersEnd(t);
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"getActiveClues",
				ACTIVE_CLUES_IDENT,
				GET_ACTIVE_CLUES_PARAMS,
				null,
				clueBody.toArray()));
		target.append(
			new ClassDecl(Modifiers.PUBLIC + Modifiers.FINAL, t.name + "ClueSet", null, INTERFACES, body.toArray()));
	}

	private void createClueBodyHeader(int numClues) {
		clueBody.append(new Assign(EVAL_NUM_IDENT, new Binop(Location.NOPOS, PLUS, EVAL_NUM_IDENT, ONE_LIT)));
		clueBody.append(
			new VarDecl(
				Location.NOPOS,
				baseClassType,
				"q",
				new Typeop(Location.NOPOS, CAST, baseClassType, new Ident(Location.NOPOS, "qi"))));
		clueBody.append(
			new VarDecl(
				Location.NOPOS,
				baseClassType,
				"m",
				new Typeop(Location.NOPOS, CAST, baseClassType, new Ident(Location.NOPOS, "mi"))));
		Ident cst = null;
		Tree initialSize = null;
		if (cluesetType == ClueSetType.BOOLEAN) {
			cst = BOOLEAN_ACTIVE_CLUES_IDENT;
			initialSize = new Literal(Location.NOPOS, INT, new Integer(numClues / 4));
		} else if (cluesetType == ClueSetType.INT) {
			cst = INT_ACTIVE_CLUES_IDENT;
			initialSize = new Ident(Location.NOPOS, "aSize");
		}
		clueBody.append(
			new VarDecl(Location.NOPOS, cst, "a", new New(Location.NOPOS, cst, new Tree[] { initialSize })));
	}

	private void createClueBodyFooter() {
		clueBody.append(new Return(new Ident(Location.NOPOS, "a")));
	}

	private void createAuxiliaryMembersBegin(ClueSetDecl t) {
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE + Modifiers.STATIC,
				new Ident(Location.NOPOS, "org.apache.log4j.Logger"),
				"cat",
				new Ident(Location.NOPOS, "org.apache.log4j.Logger.getLogger(" + t.name + "ClueSet.class)")));
		body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE, INT_IDENT, "__evalNum", null));
		numClues = new int[ExtDecision.NUM_DECISIONS];
		clueDesc = new TreeList();
	}

	private void createAuxiliaryMembersEnd(ClueSetDecl t) {
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE + Modifiers.STATIC,
				CLUE_DESC_ARR_IDENT,
				"clueDescs",
				new NewArray(0, null, null, clueDesc.toArray())));
		Tree[] clueSizes = new Tree[ExtDecision.NUM_DECISIONS];
		size = 0;
		for (int i = 0; i < ExtDecision.NUM_DECISIONS; ++i) {
			clueSizes[i] = new Literal(Location.NOPOS, INT, new Integer(numClues[i]));
			size += numClues[i];
		}
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE + Modifiers.STATIC,
				INT_ARRAY,
				"sizes",
				new NewArray(0, null, null, clueSizes)));
		Tree[] mb1 = { new Return(new Indexed(Location.NOPOS, SIZES_IDENT, D_TO_INT_EXPR))};
		body.append(new JMethodDecl(Modifiers.PUBLIC, "size", INT_IDENT, DECISION_VAR_DECLS, null, mb1));
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE + Modifiers.STATIC,
				INT_IDENT,
				"aSize",
				new Literal(Location.NOPOS, INT, new Integer(size))));
		Tree[] mb2 = { new Return(new Literal(Location.NOPOS, INT, new Integer(size)))};
		body.append(new JMethodDecl(Modifiers.PUBLIC, "size", INT_IDENT, null, null, mb2));
		Ident cluesettype = new Ident(Location.NOPOS, "com.choicemaker.cm.core.ClueSetType");
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"getType",
				cluesettype,
				null,
				null,
				new Tree[] {
					new Return(
						new Ident(
							Location.NOPOS,
							"com.choicemaker.cm.core.ClueSetType." + t.type.getConstant()))}));
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"hasDecision",
				BOOLEAN_IDENT,
				null,
				null,
				new Tree[] { new Return(new Literal(Location.NOPOS, BOOLEAN, hasDecision ? Boolean.TRUE : Boolean.FALSE))}));
		body.append(GET_CLUE_DESC);
	}

	public void visit(MethodDecl t) {
		body.append(t);
	}

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
						INT_ARRAY,
						t.name,
						new NewArray(
							Location.NOPOS,
							INT_IDENT,
							new Tree[] { new Literal(Location.NOPOS, INT, new Integer(t.indices.length))},
							null)));
				clueBody.append(
					new Assign(
						new Indexed(Location.NOPOS, new Ident(Location.NOPOS, t.name), ZERO_LIT),
						MINUS_ONE_LIT));
			}
			indexed = true;
			int numCreatedClues = 1;
			TreeList bArgs = new TreeList();
			bArgs.append(Q_IDENT);
			bArgs.append(M_IDENT);
			TreeList bParams = new TreeList();
			bParams.append(qmVarDecl[0]);
			bParams.append(qmVarDecl[1]);
			for (int i = 0; i < t.indices.length; ++i) {
				Index ix = t.indices[i];
				String indexArrayName = "__" + t.name + "__idx__" + ix.name;
				body.append(
					new VarDecl(
						Location.NOPOS,
						Modifiers.PRIVATE + Modifiers.STATIC + Modifiers.FINAL,
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
				VarDecl[] inits = { new VarDecl(Location.NOPOS, INT_IDENT, loopIndexName, ZERO_LIT)};
				Tree cond =
					new Binop(
						Location.NOPOS,
						LT,
						loopIndex,
						new Select(Location.NOPOS, new Ident(Location.NOPOS, indexArrayName), "length"));
				Tree[] increments = { new Assign(loopIndex, new Binop(Location.NOPOS, PLUS, loopIndex, ONE_LIT))};
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
			Tree[] tmp1 = { f.inits[0], new Assign(CLUE_NUM_IDENT, ZERO_LIT)};
			f.inits = tmp1;
			String label = "__t" + t.name;
			outer.body = new Taged(label, outer.body);
			brea = new Break(label);
			Tree[] tmp2 =
				{
					innermost.increments[0],
					new Assign(CLUE_NUM_IDENT, new Binop(Location.NOPOS, PLUS, CLUE_NUM_IDENT, ONE_LIT))};
			innermost.increments = tmp2;
			Literal clueNameLit = new Literal(Location.NOPOS, STRING, clueName);
			clueNumExpr =
				new Binop(
					Location.NOPOS,
					PLUS,
					new Literal(Location.NOPOS, INT, new Integer(clueNum + 1)),
					CLUE_NUM_IDENT);
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
			clueNameNumException = new Literal(Location.NOPOS, STRING, clueNameNum + " exception: ");
			clueNameNumFired = new Literal(Location.NOPOS, STRING, clueNameNum + " fired.");
			clueNumExpr = new Literal(Location.NOPOS, INT, new Integer(clueNum));
			args = QM;
			params = qmVarDecl;
			Tree[] cdArgs =
				{
					new Literal(Location.NOPOS, INT, new Integer(clueNum)),
					new Literal(Location.NOPOS, STRING, t.name),
					decisionTree(compToDecision(t.decision)),
					new Literal(Location.NOPOS, BOOLEAN, Boolean.valueOf(t.rule)),
					new Literal(Location.NOPOS, BYTE, new Byte(modTrans(t.clueModifiers))),
					new Literal(Location.NOPOS, INT, new Integer(t.dispBeg)),
					new Literal(Location.NOPOS, INT, new Integer(t.dispEnd))};
			clueDesc.append(new New(Location.NOPOS, CLUE_DESC_IDENT, cdArgs));
			++numClues[compToDecision(t.decision).toInt()];
		}
		Tree[] cArgs = { clueNameNumException, EX_IDENT };
		Catch[] sCatch = { new Catch(EX_VAR_DECL, new Apply(Location.NOPOS, CAT_ERROR, cArgs))};
		vars.add(Var.create(t));
		t.expr.apply(this);
		t.expr = res;
		vars.remove(vars.size() - 1);
		Tree c;
		if (cluesetType == ClueSetType.BOOLEAN || t.rule) {
			Tree[] dArgs = { clueNameNumFired };
			Tree[] aArgs = { clueNumExpr, new Literal(Location.NOPOS, BYTE, new Byte(modTrans(t.clueModifiers)))};
			Tree[] stats;
			stats = new Tree[brea == null ? 2 : 4];
			stats[0] = new Apply(Location.NOPOS, t.rule ? A_ADDRULE : A_ADD, aArgs);
			Tree logFired = new Apply(Location.NOPOS, CAT_DEBUG, dArgs);
			if (brea == null) { // non-indexed clue
				stats[1] = logFired;
			} else { // indexed clue
				stats[1] = new Cond(CAT_DEBUG_ENABLED, logFired, null);
				stats[2] = new Block(setIdxs.toArray());
				stats[3] = brea;
			}
			c =
				new Cond(
					new Apply(Location.NOPOS, new Ident(Location.NOPOS, "getClue" + t.name), args),
					new Block(stats),
					null);
		} else {
			Tree gc = new Apply(Location.NOPOS, new Ident(Location.NOPOS, "getClue" + t.name), args);
			if (t.type == Type.BOOLEAN) {
				gc = new If(Location.NOPOS, gc, ONE_LIT, ZERO_LIT);
			}
			c = new Assign(new Indexed(Location.NOPOS, A_VALUE, clueNumExpr), gc);
		}
		Try sTry = new Try(c, sCatch);
		Cond sCond = new Cond(new Indexed(Location.NOPOS, EVAL_IDENT, clueNumExpr), sTry, null);
		innermost.body = sCond;
		clueBody.append(outer.body);
		String evalNum = "__evalNum" + t.name;
		Ident evalNumIdent = new Ident(Location.NOPOS, evalNum);
		String expr = "__expr" + t.name;
		Ident exprIdent = new Ident(Location.NOPOS, expr);
		Tree[] then = { new Assign(evalNumIdent, EVAL_NUM_IDENT), new Assign(exprIdent, t.expr)};
		Tree co = indexed ? (Tree) TRUE_IDENT : (Tree) new Binop(Location.NOPOS, NOTEQ, evalNumIdent, EVAL_NUM_IDENT);
		Cond eval = new Cond(co, new Block(then), null);
		Tree[] aBody = { eval, new Return(new Ident(Location.NOPOS, expr))};
		body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE, INT_IDENT, evalNum, null));
		Tree clueType = new Ident(Location.NOPOS, t.type.toString());
		body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE, clueType, expr, null));
		body.append(new JMethodDecl(Modifiers.PRIVATE, "getClue" + t.name, clueType, params, EXCEPTION_ARR, aBody));
	}

	private boolean isReport(int modifier) {
		return modifier != 0;
	}

	private byte modTrans(int modifier) {
		if ((modifier & Modifiers.REPORT) != 0) {
			return ClueDesc.REPORT;
		} else if ((modifier & Modifiers.NOTE) != 0) {
			return ClueDesc.NOTE;
		} else {
			return ClueDesc.NONE;
		}
	}

	private int addIndexedClueDesc(
		Index[] idx,
		int indexNo,
		int clueNum,
		ClueDecl cd,
		String name,
		Tree d,
		Tree beg,
		Tree end) {
		Index index = idx[indexNo];
		Tree[] init = ((NewArray) index.initializer).init;
		for (int i = 0; i < init.length; ++i) {
			String n = name + "[";
			if (index.isSimple()) {
				if (init[i] instanceof Literal) {
					Object v = ((Literal) init[i]).value;
					if (v instanceof Character) {
						v = "'" + v + "'";
					} else if (v instanceof String) {
						v = "\"" + v + "\"";
					}
					n += v;
				} else {
					Select s = (Select)init[i];
					n += ((Ident)s.qualifier).name + "." + s.name;
				}
			} else {
				n += i;
			}
			n += "]";
			if (indexNo == idx.length - 1) {
				++clueNum;
				Tree[] args =
					{
						new Literal(Location.NOPOS, INT, new Integer(clueNum)),
						new Literal(Location.NOPOS, STRING, n),
						d,
						new Literal(Location.NOPOS, BOOLEAN, Boolean.valueOf(cd.rule)),
						new Literal(Location.NOPOS, BYTE, new Byte(modTrans(cd.clueModifiers))),
						beg,
						end };
				clueDesc.append(new New(Location.NOPOS, CLUE_DESC_IDENT, args));
			} else {
				clueNum = addIndexedClueDesc(idx, indexNo + 1, clueNum, cd, n, d, beg, end);
			}
		}
		return clueNum;
	}

	private Tree decisionTree(Decision d) {
		return DECISION_TREE[d.toInt()];
	}

	private Decision compToDecision(int i) {
		switch (i) {
			case Tags.DIFFER :
				return Decision.DIFFER;
			case Tags.MATCH :
				return Decision.MATCH;
			case Tags.HOLD :
				return Decision.HOLD;
			case Tags.NODIFFER :
				return ExtDecision.NODIFFER;
			case Tags.NOMATCH :
				return ExtDecision.NOMATCH;
			case Tags.NOHOLD :
				return ExtDecision.NOHOLD;
			default :
				return ExtDecision.NONE;
		}
	}

	public void visit(Tree t) {
		if (t.isExpr()) {
			res = t;
		}
	}

	public void visit(Quantified t) throws CompilerException {
		if (insideShorthand) {
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.quant.inside.limitation"));
		}
		int procNum = auxProcNum++;
		String auxProcName = "__quant" + procNum;
		Ident auxProcIdent = new Ident(Location.NOPOS, auxProcName);
		String auxProcNameLabel = auxProcName + "l";
		Ident auxProcLabelIdent = new Ident(Location.NOPOS, auxProcNameLabel);
		String auxProcNameSet = auxProcName + "s";
		Ident auxProcSetIdent = new Ident(Location.NOPOS, auxProcNameSet);
		TreeList aBody = new TreeList();
		switch (t.quantifier) {
			case COUNTUNIQUE :
				if (t.vars.length > 1) {
					body.append(
						new VarDecl(
							Location.NOPOS,
							INT_ARRAY_LIST_IDENT,
							auxProcNameSet,
							new New(Location.NOPOS, INT_ARRAY_LIST_IDENT, new Tree[0])));
					aBody.append(
						new Apply(Location.NOPOS, new Select(Location.NOPOS, auxProcSetIdent, "clear"), new Tree[0]));
				}
				// fall through
			case COUNT :
				aBody.append(new VarDecl(Location.NOPOS, INT_IDENT, "__e", ZERO_LIT));
				break;
			case EXISTS :
				aBody.append(E_VAR_DECLS[0]);
				break;
			case ALL :
				aBody.append(E_VAR_DECLS[1]);
				break;
			case MINIMUM :
			case MAXIMUM :
				String mm;
				if (t.quantifier == MINIMUM) {
					mm = "MAX_VALUE";
				} else {
					mm = "MIN_VALUE";
				}
				aBody.append(
					new VarDecl(
						Location.NOPOS,
						new Ident(Location.NOPOS, t.valueExpr.type.toString()),
						"__e",
						new Ident(Location.NOPOS, t.valueExpr.type.getObjectType() + "." + mm)));
				break;
		}
		if (!t.stop) {
			vars.add(Var.create(t));
			t.expr.apply(this);
			vars.remove(vars.size() - 1);
		} else {
			res = t.expr;
		}
		Tree inner = null;
		Tree[] checkCond = null;
		switch (t.quantifier) {
			case COUNTUNIQUE :
				Tree addToList;
				if (t.vars.length > 1) {
					Tree[] at = new Tree[t.vars.length - 1];
					checkCond = new Tree[t.vars.length];
					for (int i = 1; i < t.vars.length; ++i) {
						Tree[] il =
							new Tree[] {
								i == 1
									? (Tree) new Ident(Location.NOPOS, t.vars[i])
									: new Binop(
										Location.NOPOS,
										Tags.OR,
										new Literal(Location.NOPOS, Tags.INT, new Integer(i << 24)),
										new Ident(Location.NOPOS, t.vars[i]))};
						at[i - 1] = new Apply(Location.NOPOS, new Select(Location.NOPOS, auxProcSetIdent, "add"), il);
						checkCond[i] =
							new Unop(
								Location.NOPOS,
								Tags.NOT,
								new Apply(Location.NOPOS, new Select(Location.NOPOS, auxProcSetIdent, "contains"), il));
					}
					addToList = new Block(at);
				} else {
					addToList = new Block(new Tree[0]);
				}
				inner =
					new Cond(
						res,
						new Block(
							new Tree[] {
								new Assign(E_IDENT, new Binop(Location.NOPOS, PLUS, E_IDENT, ONE_LIT)),
								addToList,
								new Continue(auxProcNameLabel)}),
						null);

				break;
			case COUNT :
				inner = new Cond(res, new Assign(E_IDENT, new Binop(Location.NOPOS, PLUS, E_IDENT, ONE_LIT)), null);
				break;
			case EXISTS :
			case ALL :
				inner = new Assign(E_IDENT, res);
				break;
			case MINIMUM :
			case MAXIMUM :
				int comparator;
				if (t.quantifier == MINIMUM) {
					comparator = LT;
				} else {
					comparator = GT;
				}
				Tree cond = res;
				t.valueExpr.apply(this);
				Tree vident = new Ident(Location.NOPOS, "__v");
				Tree eident = new Ident(Location.NOPOS, "__e");
				Tree asgn =
					new Block(
						new Tree[] {
							new VarDecl(
								Location.NOPOS,
								new Ident(Location.NOPOS, t.valueExpr.type.toString()),
								"__v",
								res),
							new Cond(
								new Binop(Location.NOPOS, comparator, vident, eident),
								new Assign(eident, vident),
								null)});
				inner = new Cond(cond, asgn, null);
				break;
		}
		for (int i = t.vars.length - 1; i >= 0; --i) {
			String iteratorName = t.vars[i];
			Ident iteratorIdent = new Ident(Location.NOPOS, iteratorName);
			Tree[] inits = { new VarDecl(Location.NOPOS, INT_IDENT, iteratorName, ZERO_LIT)};
			((VarSymbol) t.sVars[i]).range.deepCopy().apply(this);
			Tree upperBound = new Select(Location.NOPOS, res, "length");
			Tree cond = new Binop(Location.NOPOS, LT, iteratorIdent, upperBound);
			if (t.quantifier == EXISTS) {
				cond = new Binop(Location.NOPOS, ANDAND, cond, E_VAR_COND[0]);
			} else if (t.quantifier == ALL) {
				cond = new Binop(Location.NOPOS, ANDAND, cond, E_VAR_COND[1]);
			}
			Tree[] increments = { new Assign(iteratorIdent, new Binop(Location.NOPOS, PLUS, iteratorIdent, ONE_LIT))};
			if (i > 0 && checkCond != null) {
				inner = new Cond(checkCond[i], inner, null);
			}
			inner = new For(inits, cond, increments, inner);
			if (i == 0) {
				inner = new Taged(auxProcNameLabel, inner);
			}
		}
		aBody.append(inner);
		aBody.append(new Return(E_IDENT));
		TreeList param = new TreeList();
		TreeList args = new TreeList();
		baseAndOuterParams(param, args);
		VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
		Tree returnType = null;
		switch (t.quantifier) {
			case COUNTUNIQUE :
			case COUNT :
				returnType = INT_IDENT;
				break;
			case EXISTS :
			case ALL :
				returnType = BOOLEAN_IDENT;
				break;
			case MINIMUM :
			case MAXIMUM :
				returnType = new Ident(Location.NOPOS, t.valueExpr.type.toString());
		}
		body.append(
			new JMethodDecl(Modifiers.PRIVATE, auxProcName, returnType, params, EXCEPTION_ARR, aBody.toArray()));
		res = new Apply(Location.NOPOS, auxProcIdent, args.toArray());
	}

	public void visit(Index t) throws CompilerException {
		t.tpe.apply(this);
		t.tpe = res;
		t.initializer.apply(this);
		t.initializer = res;
		res = t;
	}

	public void visit(VarDecl t) throws CompilerException {
		t.initializer.apply(this);
		t.initializer = res;
		res = t;
		if ((t.modifiers & Modifiers.FINAL) != 0) {
			Tree ass = new Assign(new Ident(Location.NOPOS, t.name), t.initializer);
			Tree clueNameNumException = new Literal(Location.NOPOS, STRING, "Expression " + t.name + " exception: ");
			Tree[] cArgs = { clueNameNumException, EX_IDENT };
			Catch[] cat = { new Catch(EX_VAR_DECL, new Apply(Location.NOPOS, CAT_ERROR, cArgs))};
			clueBody.append(new Try(ass, cat));
		}
	}

	public void visit(Let t) throws CompilerException {
		if (insideShorthand) {
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.let.inside.limitation"));
		}
		if (t.binders != null) {
			for (int i = 0; i < t.binders.length; ++i) {
				t.binders[i].apply(this);
				t.binders[i] = (VarDecl) res;
			}
		}
		vars.add(Var.create(t));
		t.expr.apply(this);
		t.expr = res;
		vars.remove(vars.size() - 1);
		int procNum = auxProcNum++;
		String auxProcName = "let" + procNum;
		Ident auxProcIdent = new Ident(Location.NOPOS, auxProcName);
		TreeList param = new TreeList();
		TreeList args = new TreeList();
		baseAndOuterParams(param, args);
		for (int j = 0; j < t.binders.length; ++j) {
			VarDecl b = t.binders[j];
			param.append(new VarDecl(Location.NOPOS, b.tpe, b.name, null));
			args.append(b.initializer);
		}
		Tree[] aBody = { new Return(t.expr)};
		VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
		body.append(
			new JMethodDecl(
				Modifiers.PRIVATE,
				auxProcName,
				new Ident(Location.NOPOS, t.type.toString()),
				params,
				EXCEPTION_ARR,
				aBody));
		res = new Apply(Location.NOPOS, auxProcIdent, args.toArray());
	}

	private void baseAndOuterParams(TreeList param, TreeList args) {
		param.append(qmVarDecl[0]);
		args.append(Q_IDENT);
		param.append(qmVarDecl[1]);
		args.append(M_IDENT);
		for (int i = 0; i < vars.size(); ++i) {
			Var[] v = (Var[]) vars.get(i);
			for (int j = 0; j < v.length; ++j) {
				Var b = v[j];
				param.append(new VarDecl(Location.NOPOS, b.tpe, b.name, null));
				args.append(new Ident(Location.NOPOS, b.name));
			}
		}
	}

	public void visit(Shorthand t) throws CompilerException {
		if (t.form == COMPARE) {
			Shorthand sameShorthand = (Shorthand) t.deepCopy();
			sameShorthand.form = SAME;
			t.form = DIFFERENT;
			t.apply(this);
			Tree differentIf =
				new If(
					Location.NOPOS,
					res,
					new Typeop(Location.NOPOS, CAST, BYTE_IDENT, MINUS_ONE_LIT),
					new Typeop(Location.NOPOS, CAST, BYTE_IDENT, ZERO_LIT));
			sameShorthand.apply(this);
			res = new If(Location.NOPOS, res, new Typeop(Location.NOPOS, CAST, BYTE_IDENT, ONE_LIT), differentIf);
		} else if (t.form == AND_SHORTHAND || t.form == OR_SHORTHAND || t.form == XOR_SHORTHAND) {
			simpleShorthand = SS_Q;
			t.exprs[0].deepCopy().apply(this);
			Tree qExpr = res;
			simpleShorthand = SS_M;
			t.exprs[0].apply(this);
			simpleShorthand = SS_NONE;
			int opcode;
			switch (t.form) {
				case AND_SHORTHAND :
					opcode = ANDAND;
					break;
				case OR_SHORTHAND :
					opcode = OROR;
					break;
				default :
					opcode = XOR;
			}
			res = new Binop(Location.NOPOS, opcode, qExpr, res);
		} else {
			if (insideShorthand) {
				unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.shorthand.inside.limitation"));
				res = t;
				return;
			}
			if (t.form == Tags.SWAPSAME) {
				insideShorthand = true;
				String auxProcName = "__swap" + (auxProcNum++);
				Ident auxProcIdent = new Ident(Location.NOPOS, auxProcName);
				Tree objType = null;
				if (t.baseType.isPrim()) {
					objType = new Ident(Location.NOPOS, t.baseType.getObjectType());
				}
				TreeList aBody = new TreeList();
				for (int i = 0; i < 2; ++i) {
					shorthand = i;
					aBody.append(
						new VarDecl(
							Location.NOPOS,
							OBJECT_ARRAY2,
							QR_MR_NAME[i],
							new NewArray(
								Location.NOPOS,
								OBJECT_ARRAY,
								new Tree[] { new Literal(Location.NOPOS, Tags.INT, new Integer(t.exprs.length))},
								null)));
					for (int j = 0; j < t.exprs.length; j++) {
						aBody.append(new Apply(Location.NOPOS, new Ident(Location.NOPOS, "__lacc.clear"), new Tree[0]));
						initShorthand();
						Tree exp = i == 0 ? t.exprs[j].deepCopy() : t.exprs[j];
						insideShorthand = true;
						exp.apply(this);
						insideShorthand = false;
						Tree toAdd = res;
						if (objType != null) {
							toAdd = new New(Location.NOPOS, objType, new Tree[] { toAdd });
						}
						Tree inner =
							new Apply(Location.NOPOS, new Ident(Location.NOPOS, "__lacc.add"), new Tree[] { toAdd });
						inner = new Cond(concatAnd(TRUE_IDENT, shortHandFieldValid(i)), inner, null);
						VarSymbol[] vs = shorthandIndexes[i].getBoundSymbols();
						for (int k = vs.length - 1; k >= 0; --k) {
							String iteratorName = vs[k].getName();
							Ident iteratorIdent = new Ident(Location.NOPOS, iteratorName);
							Tree[] inits = { new VarDecl(Location.NOPOS, INT_IDENT, iteratorName, ZERO_LIT)};
							vs[k].range.deepCopy().apply(this);
							Tree upperBound = new Select(Location.NOPOS, res, "length");
							Tree cond = new Binop(Location.NOPOS, LT, iteratorIdent, upperBound);
							Tree[] increments =
								{ new Assign(iteratorIdent, new Binop(Location.NOPOS, PLUS, iteratorIdent, ONE_LIT))};
							inner = new For(inits, cond, increments, inner);
						}
						aBody.append(inner);
						aBody.append(
							new Assign(
								new Indexed(
									Location.NOPOS,
									QR_MR[i],
									new Literal(Location.NOPOS, Tags.INT, new Integer(j))),
								new Apply(Location.NOPOS, new Ident(Location.NOPOS, "__lacc.toArray"), new Tree[0])));
					}
				}
				t.numPerConjunct.apply(this);
				Tree npc = res;
				t.minNumMoved.apply(this);
				Tree mm = res;
				aBody.append(
					new Return(
						new Apply(
							Location.NOPOS,
							new Ident(Location.NOPOS, "com.choicemaker.cm.core.util.Swap.swapsame"),
							new Tree[] { QR_MR[0], QR_MR[1], npc, mm })));
				TreeList param = new TreeList();
				TreeList args = new TreeList();
				baseAndOuterParams(param, args);
				VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
				body.append(
					new JMethodDecl(
						Modifiers.PRIVATE,
						auxProcName,
						BOOLEAN_IDENT,
						params,
						EXCEPTION_ARR,
						aBody.toArray()));
				res = new Apply(Location.NOPOS, auxProcIdent, args.toArray());
			} else {
				initShorthand();
				TreeList ex = new TreeList();
				insideShorthand = true;

				Tree[] cond = new Tree[2];
				if (t.cond != null) {
					for (int i = 0; i < 2; ++i) {
						shorthand = i;
						t.cond.deepCopy().apply(this);
						cond[i] = res;
					}
				}
				for (int i = 0; i < t.exprs.length; ++i) {
					shorthand = 0;
					t.exprs[i].deepCopy().apply(this);
					Tree q = res;
					shorthand = 1;
					t.exprs[i].apply(this);
					Tree m = res;
					if (compareWithEquals(t.exprs[i])) {
						Tree[] args = { q };
						ex.append(new Apply(Location.NOPOS, new Select(Location.NOPOS, m, "equals"), args));
					} else {
						ex.append(new Binop(Location.NOPOS, EQEQ, q, m));
					}
				}
				insideShorthand = false;
				Tree[] v =
					{ concatAnd(TRUE_IDENT, shortHandFieldValid(0)), concatAnd(TRUE_IDENT, shortHandFieldValid(1))};
				for (int i = 0; i < 2; ++i) {
					if (cond[i] != null) {
						v[i] = new Binop(Location.NOPOS, ANDAND, v[i], cond[i]);
					}
				}
				Tree e = concatAnd(TRUE_IDENT, ex.toArray());
				if (t.form == SAME) {
					Tree tmp = new Binop(Location.NOPOS, ANDAND, new Binop(Location.NOPOS, ANDAND, v[0], v[1]), e);
					if (shorthandIndexes[0].size() == 0) {
						res = tmp;
					} else {
						int size = shorthandIndexes[0].size() + shorthandIndexes[1].size();
						String[] vars = new String[size];
						concatArray(shorthandIndexes[0].getVarNames(), shorthandIndexes[1].getVarNames(), vars);
						Quantified exi = new Quantified(Location.NOPOS, EXISTS, vars, tmp);
						VarSymbol[] vs = new VarSymbol[size];
						concatArray(shorthandIndexes[0].getBoundSymbols(), shorthandIndexes[1].getBoundSymbols(), vs);
						exi.sVars = vs;
						exi.stop = true;
						exi.apply(this);
					}
				} else { // different
					if (shorthandIndexes[0].size() == 0) {
						res =
							new Binop(
								Location.NOPOS,
								ANDAND,
								new Binop(Location.NOPOS, ANDAND, v[0], v[1]),
								new Unop(Location.NOPOS, NOT, e));
					} else {
						int size = shorthandIndexes[0].size() + shorthandIndexes[1].size();
						String[][] vars = { shorthandIndexes[0].getVarNames(), shorthandIndexes[1].getVarNames()};
						VarSymbol[][] vs =
							{ shorthandIndexes[0].getBoundSymbols(), shorthandIndexes[1].getBoundSymbols()};
						Quantified[] exi =
							{
								new Quantified(Location.NOPOS, EXISTS, vars[0], v[0]),
								new Quantified(Location.NOPOS, EXISTS, vars[1], v[1])};
						exi[0].sVars = vs[0];
						exi[1].sVars = vs[1];
						exi[0].stop = true;
						exi[1].stop = true;
						Tree b =
							new Unop(
								Location.NOPOS,
								NOT,
								new Binop(Location.NOPOS, ANDAND, new Binop(Location.NOPOS, ANDAND, v[0], v[1]), e));
						String[] allvars = new String[size];
						concatArray(vars[0], vars[1], allvars);
						Quantified al = new Quantified(Location.NOPOS, ALL, allvars, b);
						VarSymbol[] allvs = new VarSymbol[size];
						concatArray(vs[0], vs[1], allvs);
						al.sVars = allvs;
						al.stop = true;
						new Binop(Location.NOPOS, ANDAND, new Binop(Location.NOPOS, ANDAND, exi[0], exi[1]), al).apply(
							this);
					}
				}
			}
		}
	}

	private void initShorthand() {
		shorthandFields = new TreeList[2];
		shorthandFields[0] = new TreeList();
		shorthandFields[1] = new TreeList();
		shorthandIndexes = new ListMap[2];
		shorthandIndexes[0] = new ListMap();
		shorthandIndexes[1] = new ListMap();
		shorthandIndexNum = new int[2];
		shorthandIndexNum[0] = 0;
		shorthandIndexNum[1] = 0;
	}

	private static Object[] concatArray(Object[] a, Object[] b, Object[] to) {
		System.arraycopy(a, 0, to, 0, a.length);
		System.arraycopy(b, 0, to, a.length, b.length);
		return to;
	}

	private Tree[] shortHandFieldValid(int n) throws CompilerException {
		Tree[] r = new Tree[shorthandFields[n].length()];
		for (int i = 0; i < shorthandFields[n].length(); ++i) {
			new Valid(Location.NOPOS, shorthandFields[n].get(i)).apply(this);
			r[i] = res;
		}
		return r;
	}

	private Tree concatAnd(Tree t, Tree[] l) {
		int i = 0;
		if (t == TRUE_IDENT) {
			if (l.length == 0) {
				return t;
			} else {
				t = l[0];
				i = 1;
			}
		}
		for (; i < l.length; ++i) {
			t = new Binop(Location.NOPOS, ANDAND, t, l[i]);
		}
		return t;
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
		if (t.args != null) {
			for (int i = 0; i < t.args.length; ++i) {
				t.args[i].apply(this);
				t.args[i] = res;
			}
		}
		res = t;
	}

	public void visit(New t) throws CompilerException {
		t.clazz.apply(this);
		t.clazz = res;
		if (t.args != null) {
			for (int i = 0; i < t.args.length; ++i) {
				t.args[i].apply(this);
				t.args[i] = res;
			}
		}
		res = t;
	}

	public void visit(NewArray t) throws CompilerException {
		t.clazz.apply(this);
		t.clazz = res;
		if (t.dims != null) {
			for (int i = 0; i < t.dims.length; ++i) {
				t.dims[i].apply(this);
				t.dims[i] = res;
			}
		}
		if (t.init != null) {
			for (int i = 0; i < t.init.length; ++i) {
				t.init[i].apply(this);
				t.init[i] = res;
			}
		}
		res = t;
	}

	public void visit(Typeop t) throws CompilerException {
		t.tpe.apply(this);
		t.tpe = res;
		t.expr.apply(this);
		t.expr = res;
		res = t;
	}

	public void visit(Unop t) throws CompilerException {
		t.arg.apply(this);
		t.arg = res;
		res = t;
	}

	private boolean compareWithEquals(Tree t) {
		return t.type.isRef()
			&& t.type != Type.NULL
			&& !(unit.isIntern()
				&& t.type == stringType
				&& t instanceof Select
				&& (((Select) t).sym.getOwner().modifiers() & SCHEMA) != 0);
	}

	public void visit(Binop t) throws CompilerException {
		t.left.apply(this);
		t.left = res;
		t.right.apply(this);
		t.right = res;
		res = t;
		if ((t.opcode == EQEQ || t.opcode == NOTEQ) && compareWithEquals(t.left) && compareWithEquals(t.right)) {
			Tree[] args = { t.right };
			Tree t1 = new Apply(Location.NOPOS, new Select(Location.NOPOS, t.left, "equals"), args);
			if (t.opcode == EQEQ) {
				res = t1;
			} else { // NOTEQ
				res = new Unop(Location.NOPOS, NOT, t1);
			}
		}
	}

	public void visit(Indexed t) throws CompilerException {
		if (t.multiClue != null) {
			if (/* t.multiClue.multiClue.sym.type */ cluesetType == ClueSetType.BOOLEAN) {
				Tree cond = TRUE_IDENT;
				if (t.index != null) {
					cond = getCond(t, t.index, t.mIndex == null ? EQEQ : LTEQ);
					if (t.mIndex != null) {
						cond = new Binop(Location.NOPOS, ANDAND, cond, getCond(t, t.mIndex, GTEQ));
					}
				} else if (t.multiClue.indexNum == 0) {
					cond =
						new Binop(
							Location.NOPOS,
							Tags.NOTEQ,
							new Indexed(Location.NOPOS, t.multiClue.multiClue, ZERO_LIT),
							MINUS_ONE_LIT);
				}
				if (t.expr instanceof Indexed) {
					t.expr.apply(this);
					cond = new Binop(Location.NOPOS, ANDAND, cond, res);
				}
				res = cond;
			} else {
				TreeList args = new TreeList();
				args.append(Q_IDENT);
				args.append(M_IDENT);
				Index[] indices = t.multiClue.indices;
				String clueName = t.multiClue.multiClue.name;
				Tree[] indexRef = new Tree[indices.length];
				Indexed ixs = t;
				for (int i = indices.length - 1; i >= 0; --i) {
					indexRef[i] = ixs.index;
					if (i != 0) {
						ixs = (Indexed) ixs.expr;
					}
				}
				for (int i = 0; i < indices.length; ++i) {
					Index ix = indices[i];
					String indexArrayName = "__" + clueName + "__idx__" + ix.name;
					args.append(new Indexed(Location.NOPOS, new Ident(Location.NOPOS, indexArrayName), indexRef[i]));
				}
				res =
					new Apply(
						Location.NOPOS,
						new Ident(Location.NOPOS, "getClue" + t.multiClue.multiClue.name),
						args.toArray());
			}
		} else {
			t.expr.apply(this);
			t.expr = res;
			if (t.mIndex != null && simpleShorthand == SS_M) {
				t.index = t.mIndex;
			}
			t.index.apply(this);
			t.index = res;
			res = t;
		}
	}

	private Tree getCond(Indexed t, Tree idx, int opcode) throws CompilerException {
		idx.apply(this);
		return new Binop(
			Location.NOPOS,
			opcode,
			res,
			new Indexed(
				Location.NOPOS,
				t.multiClue.multiClue,
				new Literal(Location.NOPOS, INT, new Integer(t.multiClue.indexNum))));
	}

	public void visit(Select t) throws CompilerException {
		Tree tmp = t;
		t.qualifier.apply(this);
		t.qualifier = res;
		if (replace && (t.sym.getOwner().modifiers() & SCHEMA) != 0) {
			if (t.type instanceof ObjectType && (((ObjectType) t.type).sym().modifiers() & SCHEMA) != 0) {
				Tree idx = getIndex(tmp, t);
				res = new Indexed(Location.NOPOS, t, idx);
			} else {
				replace = false;
				TreeList sf = shorthandFields[shorthand];
				int i = sf.length() - 1;
				while (i >= 0 && !t.equals(sf.get(i))) {
					--i;
				}
				if (i < 0) {
					sf.append(t);
				}
				res = t;
			}
		} else {
			replace = false;
			res = t;
		}
	}

	private Tree getIndex(Tree t, Tree bound) {
		Tree idx = shorthandIndexes[shorthand].getVar(t);
		if (idx != null) {
			return idx;
		} else {
			Tree idx2 = new Ident(Location.NOPOS, (shorthand == 0 ? "__i" : "__j") + (shorthandIndexNum[shorthand]++));
			shorthandIndexes[shorthand].put(t, idx2, bound);
			return idx2;
		}
	}

	public void visit(Ident t) throws CompilerException {
		if (t.sym instanceof ClueSymbol) {
			ClueDecl clue = ((ClueSymbol) t.sym).decl;
			if (clue.indices != null && clue.indices.length != 0) {
				unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.indexed.referencing.limitation"));
			}
			Tree[] args = { Q_IDENT, M_IDENT };
			res = new Apply(Location.NOPOS, new Ident(Location.NOPOS, "getClue" + t.name), args);
		} else {
			res = t;
		}
	}

	public void visit(Self t) throws CompilerException {
		res = t;
		if (t.stag == Tags.R) {
			if (simpleShorthand == SS_NONE) {
				t.stag = Tags.Q + shorthand;
				replace = true;
			} else if (simpleShorthand == SS_Q) {
				res = new Self(Location.NOPOS, Tags.Q);
			} else {
				res = new Self(Location.NOPOS, Tags.M);
			}
		}
	}

	public void visit(ArrayType t) throws CompilerException {
		t.tpe.apply(this);
		t.tpe = res;
		res = t;
	}

	public void visit(PrimitiveType t) {
		res = t;
	}

	public void visit(Literal t) {
		res = t;
	}

	public void visit(Valid t) throws CompilerException {
		if (insideShorthand) {
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.valid.inside.limitation"));
		}
		Select s = (Select) t.access;
		s.qualifier.apply(this);
		res = new Select(s.pos, res, "__v_" + s.name);
	}
}
