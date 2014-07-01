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
import com.choicemaker.cm.compiler.NamePool;
import com.choicemaker.cm.compiler.Symbol.ClueSymbol;
import com.choicemaker.cm.compiler.Symbol.VarSymbol;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
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
import com.choicemaker.cm.compiler.Type;
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
import com.choicemaker.cm.compiler.parser.TreeList;
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
 * @author    Matthias Zenger
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:00:54 $
 */
public class Translator25 extends TreeGen implements TargetTags, Modifiers, ITranslator {
	
	/** A pool yielding fresh names.
	 */
	protected NamePool pool = new NamePool();
	
	/** The target printer used to generate code.
	 */
	protected final ITargetPrinter targetPrinter;
	
	/** Corresponding compilation unit.
	 */
	protected final ICompilationUnit unit;
	
	/** Target of compilation.
	 */
	protected TreeList target;
	
	/** Body of class.
	 */
	protected TreeList body;
	
	/** Body of method getActiveClues
	 */
	protected TreeList clueBody;
	
	/** Number of clues
	 */
	protected int size;
	
	/** Number of clue
	 */
	protected int clueNum;
	
	/** Return through global variable for visitors
	 */
	protected Tree res;
	
	/** Number of auxiliary procedure
	 */
	protected int auxProcNum;
	
	/** Base class type
	 */
	protected Ident baseClassType;
	
	/** Variable declaration of Q and M of base class type type
	 */
	protected VarDecl[] qmVarDecl;
	
	/** Outer vars
	 */
	protected List vars;
	
	/** Number of clues by decision.
	 */
	protected int[] numClues;
	
	/** Clue descriptors
	 */
	TreeList clueDesc;
	
	/** Type of the clueset
	 */
	protected ClueSetType cluesetType;
	
	/** Whether clueset has decisions
	 */
	protected boolean hasDecision;
	
	/** Shorthand management
	 */
	TreeList[] shorthandFields;
	ListMap[] shorthandIndexes;
	int[] shorthandIndexNum;
	boolean replace;
	int shorthand;
	boolean insideShorthand;
	int simpleShorthand;

	/** Shorthand modes
	 */
	protected static final int SS_NONE = 0;
	protected static final int SS_Q = 1;
	protected static final int SS_M = 2;
    
	// Factory methods
    
	protected Tree clue_desc_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.ClueDesc");
	}
   	
	protected Tree[] interfaces(int pos) {
		return new Tree[]{qualid(pos, "com.choicemaker.cm.core.ClueSet")};
	}
    
	protected Tree clue_desc_arr_ident(int pos) {
		return new ArrayType(pos, clue_desc_ident(pos));
	}
    
	protected Tree int_ident(int pos) {
		return new Ident(pos, "int");
	}
    
	protected Tree int_array(int pos) {
		return new ArrayType(pos, int_ident(pos));
	}
    
	protected Tree sizes_ident(int pos) {
		return new Ident(pos, "sizes");
	}
    
	protected Tree d_to_int_expr(int pos) {
		return new Apply(pos, new Select(pos, new Ident(pos, "d"), "toInt"), null);
	}
    
	protected Tree decision_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.Decision");
	}
    
	protected Tree ext_decision_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.ExtDecision");
	}
    
	protected VarDecl[] decision_var_decls(int pos) {
		return new VarDecl[]{new VarDecl(pos, decision_ident(pos), "d", null)};
	}
   	
	protected Tree[] get_clue_desc_body(int pos) {
		return new Tree[]{new Return(new Ident(Location.NOPOS, "clueDescs"))};
	}
    
	protected Tree get_clue_desc(int pos) {
		return new JMethodDecl(Modifiers.PUBLIC, "getClueDesc", clue_desc_arr_ident(pos), null, null, get_clue_desc_body(pos));
	}
    
	protected Tree active_clues_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.base.ActiveClues");
	}
    
	protected Tree boolean_active_clues_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.base.BooleanActiveClues");
	}
    
	protected Tree int_active_clues_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.base.IntActiveClues");
	}
    
	protected Tree record_ident(int pos) {
		return qualid(pos, "com.choicemaker.cm.core.Record");
	}
  	
	protected Tree boolean_ident(int pos) {
		return new Ident(pos, "boolean");
	}
    
	protected Tree byte_ident(int pos) {
		return new Ident(pos, "byte");
	}
    
	protected VarDecl[] get_active_clues_params(int pos) {
		return new VarDecl[]{
			new VarDecl(pos, record_ident(pos), "qi", null),
			new VarDecl(pos, record_ident(pos), "mi", null),
			new VarDecl(pos, new ArrayType(pos, boolean_ident(pos)), "eval", null)};
	}
   	
	protected Tree eval_num_ident(int pos) {
		return new Ident(pos, "__evalNum");
	}
    
	protected Tree zero_lit(int pos) {
		return new Literal(pos, INT, new Integer(0));
	}
    
	protected Tree one_lit(int pos) {
		return new Literal(pos, INT, new Integer(1));
	}
    
	protected Tree minus_one_lit(int pos) {
		return new Literal(pos, INT, new Integer(-1));
	}
  	
	protected Tree eval_ident(int pos) {
		return new Ident(pos, "eval");
	}
    
	protected Tree exception_ident(int pos) {
		return qualid(pos, "java.lang.Exception");
	}
    
	protected VarDecl ex_var_decl(int pos) {
		return new VarDecl(pos, exception_ident(pos), "ex", null);
	}
    
	protected Tree[] exception_arr(int pos) {
		return new Tree[]{ exception_ident(pos) };
	}
    
	protected Tree ex_ident(int pos) {
		return new Ident(pos, "ex");
	}
    
	protected Tree cat_ident(int pos) {
		return new Ident(pos, "cat");
	}
    
	protected Tree cat_error(int pos) {
		return new Select(pos, cat_ident(pos), "error");
	}
    
	protected Tree cat_debug(int pos) {
		return new Select(pos, cat_ident(pos), "debug");
	}
    
	protected Tree a_add(int pos) {
		return new Select(pos, new Ident(pos, "a"), "add");
	}
    
	protected Tree a_addrule(int pos) {
		return new Select(pos, new Ident(pos, "a"), "addRule");
	}
    
	protected Tree a_value(int pos) {
		return new Select(pos, new Ident(pos, "a"), "values");
	}
    
	protected Tree[] decision_tree(int pos) {
		Tree[] res = new Tree[ExtDecision.NUM_DECISIONS_EXT];
		for (int i = 0; i < ExtDecision.NUM_DECISIONS_EXT; ++i)
			res[i] = new Select(
				pos,
				i < Decision.NUM_DECISIONS ? decision_ident(pos) : ext_decision_ident(pos),
				ExtDecision.valueOf(i).toString().toUpperCase());
		return res;
	}
    
	protected Tree q_ident(int pos) {
		return new Ident(pos, "q");
	}
    
	protected Tree m_ident(int pos) {
		return new Ident(pos, "m");
	}
    
	protected Tree[] qm(int pos) {
		return new Tree[]{ q_ident(pos), m_ident(pos) };
	}
    
	protected VarDecl[] e_var_decls(int pos) {
		return new VarDecl[]{
			new VarDecl(pos, boolean_ident(pos), "__e", new Literal(pos, BOOLEAN, Boolean.FALSE)),
			new VarDecl(pos, boolean_ident(pos), "__e", new Literal(pos, BOOLEAN, Boolean.TRUE))};
	}
    
	protected Tree e_ident(int pos) {
		return new Ident(pos, "__e");
	}
    
	protected Tree[] e_var_cond(int pos) {
		return new Tree[]{ new Unop(pos, NOT, e_ident(pos)), e_ident(pos) };
	}
    
	protected Tree clue_num_ident(int pos) {
		return new Ident(pos, "clueNum");
	}
    
	protected static final Tree[] EMPTY_TREE_ARR = {};
    
	protected Tree cat_debug_enabled(int pos) {
		return new Apply(pos, new Select(pos, cat_ident(pos), "isDebugEnabled"), EMPTY_TREE_ARR);
	}
    
	protected Tree true_lit(int pos) {
		return new Literal(pos, BOOLEAN, Boolean.TRUE);
	}
    
	protected Tree int_array_list_ident(int pos) {
		return qualid(pos, "com.choicemaker.util.IntArrayList");
	}
    
	protected Tree string_value_of(int pos) {
		return new Select(pos, qualid(pos, "java.lang.String"), "valueOf");
	}
    
	protected Tree array_list_ident(int pos) {
		return qualid(pos, "java.util.ArrayList");
	}
    
	protected Ident[] qr_mr(int pos) {
		return new Ident[] { new Ident(pos, "qr"), new Ident(pos, "mr")};
	}
    
	protected static final String[] QR_MR_NAME = { "qr", "mr" };
    
	protected Tree object_array(int pos) {
		return new ArrayType(pos, qualid(pos, "java.lang.Object"));
	}
    
	protected Tree object_array2(int pos) {
		return new ArrayType(pos, new ArrayType(pos, qualid(pos, "java.lang.Object")));
	}
	
	
	// The main translator

	public Translator25(ITargetPrinter targetPrinter, ICompilationUnit unit) throws CompilerException {
		super(unit);
		this.targetPrinter = targetPrinter;
		this.unit = unit;
		
		// Preconditions
		if (this.targetPrinter == null) {
			throw new IllegalArgumentException("null target printer");
		}
		if (this.unit == null) {
			throw new IllegalArgumentException("null compilation unit");
		}
	}

	public void translate() throws CompilerException {
		toJava();
		targetPrinter.printUnit(unit);
	}
	
	public void toJava() throws CompilerException {
		vars = new ArrayList();
		baseClassType = new Ident(Location.NOPOS, unit.getBaseClass().toString());
		qmVarDecl = new VarDecl[2];
		qmVarDecl[0] = new VarDecl(Location.NOPOS, baseClassType, "q", null);
		qmVarDecl[1] = new VarDecl(Location.NOPOS, baseClassType, "m", null);
		target = new TreeList();
		body = new TreeList();
		for (int i = 0; i < unit.getDecls().length; ++i)
			unit.getDecls()[i].apply(this);
		unit.setTarget(target.toArray());
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
		target.append(new ImportDecl(Location.NOPOS, qualid(Location.NOPOS, "com.choicemaker.cm.core"), true));
		target.append(new ImportDecl(Location.NOPOS, qualid(Location.NOPOS, "com.choicemaker.cm.core.base"), true));
		target.append(new ImportDecl(Location.NOPOS, qualid(Location.NOPOS, "org.apache.log4j"), true));
		createAuxiliaryMembersBegin(t);
		clueBody = new TreeList();
		createClueBodyHeader(t.body.length);
		clueNum = -1;
		for (int i = 0; i < t.body.length; ++i) {
			t.body[i].apply(this);
			if (res instanceof VarDecl) {
				VarDecl vd = (VarDecl)res;
				body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE, vd.tpe, vd.name, null));
			}
		}
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE,
				array_list_ident(t.pos),
				"__lacc",
				new New(t.pos, array_list_ident(t.pos), new Tree[0])));
		createClueBodyFooter();
		createAuxiliaryMembersEnd(t);
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"getActiveClues",
				active_clues_ident(t.pos),
				get_active_clues_params(t.pos),
				null,
				clueBody.toArray()));
		target.append(
			new ClassDecl(Modifiers.PUBLIC | Modifiers.FINAL,
			              t.name + "ClueSet",
			              null,
			              interfaces(t.pos),
			              body.toArray()));
	}

	protected void createClueBodyHeader(int numClues) {
		clueBody.append(
			new Assign(
				eval_num_ident(Location.NOPOS),
				new Binop(Location.NOPOS, PLUS, eval_num_ident(Location.NOPOS), one_lit(Location.NOPOS))));
		// 2009-04-05 rphall
		// DEBUG ClassCastException with Cluesets invoked within Cluesets
		// cat.debug( "PatientImpl.class == '" + PatientImpl.class + "'");
		String[] arStrDebugMsgs = new String[] {
			baseClassType + ".class.getName()",
			baseClassType + ".class.toString()",
			baseClassType + ".class.getClassLoader().toString()"
			// 2010-02-03 rphall
			// BUG qi may be null?
//			baseClassType + ".class.getClassLoader().toString()",
//			"qi.toString()",
//			"qi.getClass().toString()",
//			"qi.getClass().getClassLoader().toString()"
			// ENDBUG
		};

		for (int idxDebugMsg = 0; idxDebugMsg < arStrDebugMsgs.length; idxDebugMsg++) {		
			String strDebugMsg = arStrDebugMsgs[idxDebugMsg];
			Tree treeDebugKey = new Literal(Location.NOPOS, STRING, strDebugMsg + " == ");
			Tree treeDebugValue = new Ident(Location.NOPOS, strDebugMsg);
			Tree treeDebugMsg = new Binop(Location.NOPOS, PLUS, treeDebugKey, treeDebugValue);
			Tree[] argsDebugMsg = { treeDebugMsg };
			Tree debugClassCastIssue = new Apply(Location.NOPOS, cat_debug(Location.NOPOS), argsDebugMsg);
			clueBody.append(debugClassCastIssue);
		}
		// END DEBUG
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
		Tree cst = null;
		Tree initialSize = null;
		if (cluesetType == ClueSetType.BOOLEAN) {
			cst = boolean_active_clues_ident(Location.NOPOS);
			initialSize = new Literal(Location.NOPOS, INT, new Integer(numClues / 4));
		} else if (cluesetType == ClueSetType.INT) {
			cst = int_active_clues_ident(Location.NOPOS);
			initialSize = new Ident(Location.NOPOS, "aSize");
		}
		clueBody.append(
			new VarDecl(Location.NOPOS,
			            cst,
                        "a",
                        new New(Location.NOPOS, cst, new Tree[]{initialSize})));
	}

	protected void createClueBodyFooter() {
		clueBody.append(new Return(new Ident(Location.NOPOS, "a")));
	}

	protected void createAuxiliaryMembersBegin(ClueSetDecl t) {
		body.append(
			new VarDecl(
				t.pos,
				Modifiers.PRIVATE + Modifiers.STATIC,
				qualid(t.pos, "org.apache.log4j.Logger"),
				"cat",
				new Apply(t.pos,
				          qualid(Location.NOPOS, "org.apache.log4j.Logger.getLogger"),
				          new Tree[]{
				          	  // This is a hack; I currently don't see a way to avoid it without
				          	  // generating a new method:
				          	  new Ident(t.pos, t.name + "ClueSet.class")
				              //new Apply(t.pos,
				              //          qualid(t.pos, "java.lang.Class.forName"),
				              //          new Tree[]{new Literal(t.pos, Tags.STRING,  t.name + "ClueSet")})
				          })));
		body.append(
			new VarDecl(
				t.pos,
				Modifiers.PRIVATE,
				int_ident(t.pos),
                "__evalNum",
                null));
		numClues = new int[ExtDecision.NUM_DECISIONS_EXT];
		clueDesc = new TreeList();
	}

	protected void createAuxiliaryMembersEnd(ClueSetDecl t) {
		body.append(
			new VarDecl(
				t.pos,
				Modifiers.PRIVATE | Modifiers.STATIC,
				clue_desc_arr_ident(t.pos),
				"clueDescs",
				new NewArray(0, null, null, clueDesc.toArray())));
		Tree[] clueSizes = new Tree[ExtDecision.NUM_DECISIONS_EXT];
		size = 0;
		for (int i = 0; i < ExtDecision.NUM_DECISIONS_EXT; ++i) {
			clueSizes[i] = new Literal(Location.NOPOS, INT, new Integer(numClues[i]));
			size += numClues[i];
		}
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE | Modifiers.STATIC,
				int_array(t.pos),
				"sizes",
				new NewArray(0, null, null, clueSizes)));
		Tree[] mb1 = { new Return(new Indexed(Location.NOPOS, sizes_ident(t.pos), d_to_int_expr(t.pos)))};
		body.append(new JMethodDecl(Modifiers.PUBLIC, "size", int_ident(t.pos), decision_var_decls(t.pos), null, mb1));
//		body.append(new VarDecl(Location.NOPOS, Modifiers.PRIVATE + Modifiers.STATIC, int_ident(t.pos),
//		  	        "precFired", new Literal(Location.NOPOS, INT, new Integer(0))));
//		body.append(new VarDecl(Location.NOPOS,	Modifiers.PRIVATE + Modifiers.STATIC, int_ident(t.pos),
//					"inStep", new Literal(Location.NOPOS, INT, new Integer(0))));
		body.append(
			new VarDecl(
				Location.NOPOS,
				Modifiers.PRIVATE | Modifiers.STATIC,
				int_ident(t.pos),
				"aSize",
				new Literal(Location.NOPOS, INT, new Integer(size))));
		Tree[] mb2 = { new Return(new Literal(Location.NOPOS, INT, new Integer(size)))};
		body.append(new JMethodDecl(Modifiers.PUBLIC, "size", int_ident(t.pos), null, null, mb2));
		Tree cluesettype = qualid(t.pos, "com.choicemaker.cm.core.ClueSetType");
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"getType",
				cluesettype,
				null, null,
				new Tree[] {
					new Return(
						qualid(
							t.pos,
							"com.choicemaker.cm.core.ClueSetType." +
							    t.type.getConstant()))}));
		body.append(
			new JMethodDecl(
				Modifiers.PUBLIC,
				"hasDecision",
				boolean_ident(t.pos),
				null, null,
				new Tree[]{
					new Return(new Literal(Location.NOPOS, BOOLEAN, hasDecision ? Boolean.TRUE : Boolean.FALSE))
				}));
		body.append(get_clue_desc(t.pos));
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
		Tree[] then = { new Assign(evalNumIdent, eval_num_ident(t.pos)), new Assign(exprIdent, t.expr)};
		Tree co = indexed ? (Tree) true_lit(t.pos) : (Tree)new Binop(t.pos, NOTEQ, evalNumIdent, eval_num_ident(t.pos));
		Cond eval = new Cond(co, new Block(then), null);
		Tree[] aBody = { eval, new Return(new Ident(t.pos, expr))};
		body.append(new VarDecl(t.pos, Modifiers.PRIVATE, int_ident(t.pos), evalNum, null));
		Tree clueType = new Ident(t.pos, t.type.toString());
		body.append(new VarDecl(t.pos, Modifiers.PRIVATE, clueType, expr, null));
		body.append(new JMethodDecl(Modifiers.PRIVATE, "getClue" + t.name, clueType, params, exception_arr(t.pos), aBody));
	}

	protected boolean isReport(int modifier) {
		return modifier != 0;
	}

	protected byte modTrans(int modifier) {
		if ((modifier & Modifiers.REPORT) != 0) {
			return ClueDesc.REPORT;
		} else if ((modifier & Modifiers.NOTE) != 0) {
			return ClueDesc.NOTE;
		} else {
			return ClueDesc.NONE;
		}
	}

	protected int addIndexedClueDesc(
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
				Tree[] args = {
						new Literal(cd.pos, INT, new Integer(clueNum)),
						new Literal(cd.pos, STRING, n),
						d,
						new Literal(cd.pos, BOOLEAN, Boolean.valueOf(cd.rule)),
						new Literal(cd.pos, BYTE, new Byte(modTrans(cd.clueModifiers))),
						beg,
						end
				};
				clueDesc.append(new New(cd.pos, clue_desc_ident(cd.pos), args));
			} else
				clueNum = addIndexedClueDesc(idx, indexNo + 1, clueNum, cd, n, d, beg, end);
		}
		return clueNum;
	}

	protected Tree decisionTree(Decision d) {
		return decision_tree(Location.NOPOS)[d.toInt()];
	}

	protected Decision compToDecision(int i) {
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
	
	public boolean visitAorE(Quantified t) throws CompilerException {
		return false;
	}

/*	
	public void visitAorE(Quantified t) throws CompilerException {
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
		Tree inner = (t.quantifier == EXISTS) ?
						new Cond(res, new Return(new Literal(t.pos, BOOLEAN, Boolean.TRUE)), null) :
						new Cond(new Unop(t.pos, NOT, res), new Return(new Literal(t.pos, BOOLEAN, Boolean.FALSE)), null);
		for (int i = t.vars.length - 1; i >= 0; --i) {
			String iteratorName = t.vars[i];
			((VarSymbol)t.sVars[i]).range.deepCopy().apply(this);
			Tree upperBound = new Select(t.pos, res, "length");
			factors[i].append(inner);
			inner = new For(
						new Tree[]{new VarDecl(t.pos, int_ident(t.pos), iteratorName, zero_lit(t.pos))},
						new Binop(t.pos, LT, new Ident(t.pos, iteratorName), upperBound),
						new Tree[]{new Assign(new Ident(t.pos, iteratorName), new Binop(t.pos, PLUS, new Ident(t.pos, iteratorName), one_lit(t.pos)))},
						new Block(factors[i].toArray()));
		}
		TreeList aBody = new TreeList();
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
	}
*/
	public void visit(Quantified t) throws CompilerException {
		if (insideShorthand)
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.quant.inside.limitation"));
		if ((t.quantifier == EXISTS) || (t.quantifier == ALL)) {
			if (visitAorE(t)) //return only if it was handled
				return;
		}
		String auxProcName = "__quant" + (auxProcNum++);
		String auxProcNameLabel = auxProcName + "l";
		String auxProcNameSet = auxProcName + "s";
		TreeList aBody = new TreeList();
		switch (t.quantifier) {
			case COUNTUNIQUE:
				if (t.vars.length > 1) {
					body.append(
						new VarDecl(
							t.pos,
							int_array_list_ident(t.pos),
							auxProcNameSet,
							new New(t.pos, int_array_list_ident(t.pos), new Tree[0])));
					aBody.append(
						new Apply(t.pos, new Select(t.pos, new Ident(t.pos, auxProcNameSet), "clear"), new Tree[0]));
				}
				// fall through
			case COUNT:
				aBody.append(new VarDecl(t.pos, int_ident(t.pos), "__e", zero_lit(t.pos)));
				break;
			case EXISTS:
				aBody.append(new VarDecl(t.pos, boolean_ident(t.pos), "__e", new Literal(t.pos, BOOLEAN, Boolean.FALSE)));
				break;
			case ALL:
				aBody.append(new VarDecl(t.pos, boolean_ident(t.pos), "__e", new Literal(t.pos, BOOLEAN, Boolean.TRUE)));
				break;
			case MINIMUM:
				aBody.append(
					new VarDecl(
						t.pos,
						new Ident(t.pos, t.valueExpr.type.toString()),
						"__e",
						new Select(t.pos, qualid(t.pos, t.valueExpr.type.getObjectType()), "MAX_VALUE")));
				break;
			case MAXIMUM:
				aBody.append(
					new VarDecl(
						t.pos,
						new Ident(t.pos, t.valueExpr.type.toString()),
						"__e",
						new Select(t.pos, qualid(t.pos, t.valueExpr.type.getObjectType()), "MIN_VALUE")));
				break;
		}
		if (!t.stop) {
			vars.add(Var.create(t));
			t.expr.apply(this);
			vars.remove(vars.size() - 1);
		} else
			res = t.expr;
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
									? (Tree) new Ident(t.pos, t.vars[i])
									: new Binop(
										Location.NOPOS,
										Tags.OR,
										new Literal(t.pos, Tags.INT, new Integer(i << 24)),
										new Ident(t.pos, t.vars[i]))};
						at[i - 1] = new Apply(t.pos, new Select(t.pos, new Ident(t.pos, auxProcNameSet), "add"), il);
						checkCond[i] =
							new Unop(
								Location.NOPOS,
								Tags.NOT,
								new Apply(t.pos, new Select(t.pos, new Ident(t.pos, auxProcNameSet), "contains"), il));
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
								new Assign(e_ident(t.pos), new Binop(Location.NOPOS, PLUS, e_ident(t.pos), one_lit(t.pos))),
								addToList,
								new Continue(auxProcNameLabel)}),
						null);

				break;
			case COUNT :
				inner = new Cond(res, new Assign(e_ident(t.pos), new Binop(Location.NOPOS, PLUS, e_ident(t.pos), one_lit(t.pos))), null);
				break;
			case EXISTS:
			case ALL:
				inner = new Assign(e_ident(t.pos), res);
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
								new Ident(t.pos, t.valueExpr.type.toString()),
								"__v",
								res),
							new Cond(
								new Binop(t.pos, comparator, vident, eident),
								new Assign(eident, vident),
								null)});
				inner = new Cond(cond, asgn, null);
				break;
		}
		for (int i = t.vars.length - 1; i >= 0; --i) {
			String iteratorName = t.vars[i];
			Ident iteratorIdent = new Ident(Location.NOPOS, iteratorName);
			Tree[] inits = {
				new VarDecl(Location.NOPOS, int_ident(t.pos), iteratorName, zero_lit(t.pos))
			};
			((VarSymbol)t.sVars[i]).range.deepCopy().apply(this);
			Tree upperBound = new Select(t.pos, res, "length");
			Tree cond = new Binop(t.pos, LT, iteratorIdent, upperBound);
			if (t.quantifier == EXISTS) {
				cond = new Binop(t.pos, ANDAND, cond, new Unop(t.pos, NOT, e_ident(t.pos)));
			} else if (t.quantifier == ALL) {
				cond = new Binop(t.pos, ANDAND, cond, e_ident(t.pos));
			}
			Tree[] increments = {
			  new Assign(iteratorIdent, new Binop(t.pos, PLUS, iteratorIdent, one_lit(t.pos)))
			};
			if (i > 0 && checkCond != null) {
				inner = new Cond(checkCond[i], inner, null);
			}
			inner = new For(inits, cond, increments, inner);
			if (i == 0) {
				inner = new Taged(auxProcNameLabel, inner);
			}
		}
		aBody.append(inner);
		aBody.append(new Return(e_ident(t.pos)));
		TreeList param = new TreeList();
		TreeList args = new TreeList();
		baseAndOuterParams(param, args);
		VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
		Tree returnType = null;
		switch (t.quantifier) {
			case COUNTUNIQUE:
			case COUNT:
				returnType = int_ident(t.pos);
				break;
			case EXISTS:
			case ALL:
				returnType = boolean_ident(t.pos);
				break;
			case MINIMUM:
			case MAXIMUM:
				returnType = qualid(t.pos, t.valueExpr.type.toString());
		}
		body.append(
			new JMethodDecl(Modifiers.PRIVATE, auxProcName, returnType, params, exception_arr(t.pos), aBody.toArray()));
		res = new Apply(t.pos, new Ident(t.pos, auxProcName), args.toArray());
		res.type = t.type;
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
			Tree ass = new Assign(new Ident(t.pos, t.name), t.initializer);
			Tree clueNameNumException = new Literal(t.pos, STRING, "Expression " + t.name + " exception: ");
			Tree[] cArgs = { clueNameNumException, ex_ident(t.pos) };
			Catch[] cat = { new Catch(ex_var_decl(t.pos), new Apply(t.pos, cat_error(t.pos), cArgs))};
			clueBody.append(new Try(ass, cat));
		}
	}

	public void visit(Let t) throws CompilerException {
		if (insideShorthand)
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.let.inside.limitation"));
		if (t.binders != null)
			for (int i = 0; i < t.binders.length; ++i) {
				t.binders[i].apply(this);
				t.binders[i] = (VarDecl)res;
			}
		vars.add(Var.create(t));
		t.expr.apply(this);
		t.expr = res;
		vars.remove(vars.size() - 1);
		int procNum = auxProcNum++;
		String auxProcName = "let" + procNum;
		Ident auxProcIdent = new Ident(t.pos, auxProcName);
		TreeList param = new TreeList();
		TreeList args = new TreeList();
		baseAndOuterParams(param, args);
		for (int j = 0; j < t.binders.length; ++j) {
			VarDecl b = t.binders[j];
			param.append(new VarDecl(t.pos, b.tpe, b.name, null));
			args.append(b.initializer);
		}
		VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
		body.append(
			new JMethodDecl(
				Modifiers.PRIVATE,
				auxProcName,
				typeToTree(t.pos, t.type),
				params,
				exception_arr(t.pos),
				new Tree[]{ new Return(t.expr) }));
		res = new Apply(t.pos, auxProcIdent, args.toArray());
		res.type = t.type;
	}

	protected void baseAndOuterParams(TreeList param, TreeList args) {
		param.append(qmVarDecl[0]);
		args.append(q_ident(Location.NOPOS));
		param.append(qmVarDecl[1]);
		args.append(m_ident(Location.NOPOS));
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
					new Typeop(Location.NOPOS, CAST, byte_ident(t.pos), minus_one_lit(t.pos)),
					new Typeop(Location.NOPOS, CAST, byte_ident(t.pos), zero_lit(t.pos)));
			sameShorthand.apply(this);
			res = new If(Location.NOPOS, res, new Typeop(Location.NOPOS, CAST, byte_ident(t.pos), one_lit(t.pos)), differentIf);
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
							object_array2(t.pos),
							QR_MR_NAME[i],
							new NewArray(
								Location.NOPOS,
								object_array(t.pos),
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
						inner = new Cond(concatAnd(true_lit(t.pos), shortHandFieldValid(i)), inner, null);
						VarSymbol[] vs = shorthandIndexes[i].getBoundSymbols();
						for (int k = vs.length - 1; k >= 0; --k) {
							String iteratorName = vs[k].getName();
							Ident iteratorIdent = new Ident(Location.NOPOS, iteratorName);
							Tree[] inits = { new VarDecl(Location.NOPOS, int_ident(t.pos), iteratorName, zero_lit(t.pos))};
							vs[k].range.deepCopy().apply(this);
							Tree upperBound = new Select(Location.NOPOS, res, "length");
							Tree cond = new Binop(Location.NOPOS, LT, iteratorIdent, upperBound);
							Tree[] increments =
								{ new Assign(iteratorIdent, new Binop(Location.NOPOS, PLUS, iteratorIdent, one_lit(t.pos)))};
							inner = new For(inits, cond, increments, inner);
						}
						aBody.append(inner);
						aBody.append(
							new Assign(
								new Indexed(
									Location.NOPOS,
									qr_mr(t.pos)[i],
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
							new Tree[] { qr_mr(t.pos)[0], qr_mr(t.pos)[1], npc, mm })));
				TreeList param = new TreeList();
				TreeList args = new TreeList();
				baseAndOuterParams(param, args);
				VarDecl[] params = (VarDecl[]) param.toArray(new VarDecl[param.length()]);
				body.append(
					new JMethodDecl(
						Modifiers.PRIVATE,
						auxProcName,
						boolean_ident(t.pos),
						params,
						exception_arr(t.pos),
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
					if (compareWithEquals(t.exprs[i]))
						ex.append(callEquals(m, new Tree[]{q}));
					else
						ex.append(new Binop(Location.NOPOS, EQEQ, q, m));
				}
				insideShorthand = false;
				Tree[] v =
					{ concatAnd(true_lit(t.pos), shortHandFieldValid(0)), concatAnd(true_lit(t.pos), shortHandFieldValid(1))};
				for (int i = 0; i < 2; ++i) {
					if (cond[i] != null) {
						v[i] = new Binop(Location.NOPOS, ANDAND, v[i], cond[i]);
						v[i].type = Type.BOOLEAN;
					}
				}
				Tree e = concatAnd(true_lit(t.pos), ex.toArray());
				if (t.form == SAME) {
					Tree tmp = new Binop(t.pos, ANDAND, new Binop(t.pos, ANDAND, v[0], v[1]), e);
					tmp.type = Type.BOOLEAN;
					if (shorthandIndexes[0].size() == 0) {
						res = tmp;
					} else {
						int size = shorthandIndexes[0].size() + shorthandIndexes[1].size();
						String[] vars = new String[size];
						concatArray(shorthandIndexes[0].getVarNames(), shorthandIndexes[1].getVarNames(), vars);
						Quantified exi = new Quantified(Location.NOPOS, EXISTS, vars, tmp);
						exi.type = Type.BOOLEAN;
						VarSymbol[] vs = new VarSymbol[size];
						concatArray(shorthandIndexes[0].getBoundSymbols(), shorthandIndexes[1].getBoundSymbols(), vs);
						exi.sVars = vs;
						exi.stop = true;
						exi.apply(this);
					}
				} else { // different
					if (shorthandIndexes[0].size() == 0) {
						res = new Binop(
								Location.NOPOS,
								ANDAND,
								new Binop(Location.NOPOS, ANDAND, v[0], v[1]),
								new Unop(Location.NOPOS, NOT, e));
						res.type = Type.BOOLEAN;
					} else {
						int size = shorthandIndexes[0].size() + shorthandIndexes[1].size();
						String[][] vars = {
							shorthandIndexes[0].getVarNames(),
							shorthandIndexes[1].getVarNames()
						};
						VarSymbol[][] vs = {
							shorthandIndexes[0].getBoundSymbols(),
							shorthandIndexes[1].getBoundSymbols()
						};
						Quantified[] exi = {
							new Quantified(Location.NOPOS, EXISTS, vars[0], v[0]),
							new Quantified(Location.NOPOS, EXISTS, vars[1], v[1])
						};
						exi[0].sVars = vs[0];
						exi[1].sVars = vs[1];
						exi[0].stop = true;
						exi[1].stop = true;
						Tree b = new Binop(t.pos, ANDAND, new Binop(t.pos, ANDAND, v[0], v[1]), e);
						b.type = Type.BOOLEAN;
						b = new Unop(Location.NOPOS, NOT, b);
						b.type = Type.BOOLEAN;
						String[] allvars = new String[size];
						concatArray(vars[0], vars[1], allvars);
						Quantified al = new Quantified(Location.NOPOS, ALL, allvars, b);
						al.type = Type.BOOLEAN;
						VarSymbol[] allvs = new VarSymbol[size];
						concatArray(vs[0], vs[1], allvs);
						al.sVars = allvs;
						al.stop = true;
						new Binop(Location.NOPOS, ANDAND, new Binop(Location.NOPOS, ANDAND, exi[0], exi[1]), al).apply(
							this);
						res.type = Type.BOOLEAN;
					}
				}
			}
		}
	}

	protected void initShorthand() {
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

	protected static Object[] concatArray(Object[] a, Object[] b, Object[] to) {
		System.arraycopy(a, 0, to, 0, a.length);
		System.arraycopy(b, 0, to, a.length, b.length);
		return to;
	}

	protected Tree[] shortHandFieldValid(int n) throws CompilerException {
		Tree[] r = new Tree[shorthandFields[n].length()];
		for (int i = 0; i < shorthandFields[n].length(); ++i) {
			new Valid(Location.NOPOS, shorthandFields[n].get(i)).apply(this);
			r[i] = res;
		}
		return r;
	}

	protected Tree concatAnd(Tree t, Tree[] l) {
		int i = 0;
		if ((t instanceof Literal) &&
			((Literal)t).value.equals(Boolean.TRUE)) {
			if (l.length == 0) {
				return t;
			} else {
				t = l[0];
				i = 1;
			}
		}
		for (; i < l.length; ++i) {
			t = new Binop(t.pos, ANDAND, t, l[i]);
			t.type = Type.BOOLEAN;
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

	protected boolean compareWithEquals(Tree t) {
		return t.type.isRef()
			&& t.type != Type.NULL
			&& !(unit.isIntern()
				&& t.type == stringType
				&& t instanceof Select
				&& (((Select) t).sym.getOwner().getModifiers() & SCHEMA) != 0);
	}

	public void visit(Binop t) throws CompilerException {
		t.left.apply(this);
		t.left = res;
		t.right.apply(this);
		t.right = res;
		res = t;
		if ((t.opcode == EQEQ || t.opcode == NOTEQ) && compareWithEquals(t.left) && compareWithEquals(t.right)) {
			Tree[] args = { t.right };
			Tree t1 = callEquals(t.left, args);
			if (t.opcode == EQEQ) {
				res = t1;
			} else { // NOTEQ
				res = new Unop(Location.NOPOS, NOT, t1);
				res.type = Type.BOOLEAN;
			}
		}
	}

	public void visit(Indexed t) throws CompilerException {
		if (t.multiClue != null) {
			if (/* t.multiClue.multiClue.sym.type */ cluesetType == ClueSetType.BOOLEAN) {
				Tree cond = true_lit(t.pos);
				if (t.index != null) {
					cond = getCond(t, t.index, t.mIndex == null ? EQEQ : LTEQ);
					if (t.mIndex != null) {
						cond = new Binop(Location.NOPOS, ANDAND, cond, getCond(t, t.mIndex, GTEQ));
						cond.type = Type.BOOLEAN;
					}
				} else if (t.multiClue.indexNum == 0) {
					cond =
						new Binop(
							Location.NOPOS,
							Tags.NOTEQ,
							new Indexed(Location.NOPOS, t.multiClue.multiClue, zero_lit(t.pos)),
							minus_one_lit(t.pos));
					cond.type = Type.BOOLEAN;
				}
				if (t.expr instanceof Indexed) {
					t.expr.apply(this);
					cond = new Binop(Location.NOPOS, ANDAND, cond, res);
					cond.type = Type.BOOLEAN;
				}
				res = cond;
			} else {
				TreeList args = new TreeList();
				args.append(q_ident(t.pos));
				args.append(m_ident(t.pos));
				Index[] indices = t.multiClue.indices;
				String clueName = t.multiClue.multiClue.name;
				Tree[] indexRef = new Tree[indices.length];
				Indexed ixs = t;
				for (int i = indices.length - 1; i >= 0; --i) {
					indexRef[i] = ixs.index;
					if (i != 0)
						ixs = (Indexed)ixs.expr;
				}
				for (int i = 0; i < indices.length; ++i) {
					Index ix = indices[i];
					String indexArrayName = "__" + clueName + "__idx__" + ix.name;
					args.append(new Indexed(t.pos, new Ident(t.pos, indexArrayName), indexRef[i]));
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
			if ((t.mIndex != null) && (simpleShorthand == SS_M))
				t.index = t.mIndex;
			t.index.apply(this);
			t.index = res;
			res = t;
		}
	}

	protected Tree getCond(Indexed t, Tree idx, int opcode) throws CompilerException {
		idx.apply(this);
		return new Binop(
			Location.NOPOS,
			opcode,
			res,
			new Indexed(
				t.pos,
				t.multiClue.multiClue,
				new Literal(t.pos, INT, new Integer(t.multiClue.indexNum))));
	}

	public void visit(Select t) throws CompilerException {
		Tree tmp = t;
		t.qualifier.apply(this);
		t.qualifier = res;
		if (replace && (t.sym.getOwner().getModifiers() & SCHEMA) != 0) {
			if (t.type instanceof ObjectType && (((ObjectType) t.type).sym.getModifiers() & SCHEMA) != 0) {
				// find index
				Tree idx = getIndex(tmp, t);
				// create indexed access
				res = new Indexed(t.pos, t, idx);
				// fix type
				t.type = new Type.ArrayType(t.type);
			} else {
				replace = false;
				TreeList sf = shorthandFields[shorthand];
				int i = sf.length() - 1;
				while (i >= 0 && !t.equals(sf.get(i)))
					--i;
				if (i < 0)
					sf.append(t);
				res = t;
			}
		} else {
			replace = false;
			res = t;
		}
	}

	protected Tree getIndex(Tree t, Tree bound) {
		Tree idx = shorthandIndexes[shorthand].getVar(t);
		if (idx != null) {
			return idx.deepCopy(); //CHECK
		} else {
			Tree idx2 = new Ident(t.pos,
				(shorthand == 0 ? "__i" : "__j") + (shorthandIndexNum[shorthand]++));
			idx2.type = Type.INT;
			shorthandIndexes[shorthand].put(t, idx2, bound);
			return idx2;
		}
	}

	public void visit(Ident t) throws CompilerException {
		if (t.sym instanceof ClueSymbol) {
			ClueDecl clue = ((ClueSymbol)t.sym).decl;
			if ((clue.indices != null) && (clue.indices.length != 0))
				unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.indexed.referencing.limitation"));
			Tree[] args = { q_ident(t.pos), m_ident(t.pos) };
			res = new Apply(t.pos, new Ident(t.pos, "getClue" + t.name), args);
			res.type = t.type;
		} else
			res = t;
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

	public void visit(Valid t) throws CompilerException {
		if (insideShorthand)
			unit.error(t.pos, MessageUtil.m.formatMessage("compiler.translator.no.valid.inside.limitation"));
		Select s = (Select) t.access;
		s.qualifier.apply(this);
		res = new Select(s.pos, res, "__v_" + s.name);
		res.type = Type.BOOLEAN;
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
}
