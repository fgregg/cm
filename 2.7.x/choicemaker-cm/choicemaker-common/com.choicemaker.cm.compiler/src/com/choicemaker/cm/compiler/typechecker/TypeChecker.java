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
package com.choicemaker.cm.compiler.typechecker;

import java.util.Stack;

import com.choicemaker.cm.compiler.ClassRepository;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.Scope;
import com.choicemaker.cm.compiler.ScopeEntry;
import com.choicemaker.cm.compiler.SemanticTags;
import com.choicemaker.cm.compiler.Symbol;
import com.choicemaker.cm.compiler.Symbol.ClassSymbol;
import com.choicemaker.cm.compiler.Symbol.ClueSetSymbol;
import com.choicemaker.cm.compiler.Symbol.ClueSymbol;
import com.choicemaker.cm.compiler.Symbol.PackageSymbol;
import com.choicemaker.cm.compiler.Symbol.VarSymbol;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Tree.Apply;
import com.choicemaker.cm.compiler.Tree.Binop;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.ClueSetDecl;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.If;
import com.choicemaker.cm.compiler.Tree.Index;
import com.choicemaker.cm.compiler.Tree.Indexed;
import com.choicemaker.cm.compiler.Tree.Let;
import com.choicemaker.cm.compiler.Tree.Literal;
import com.choicemaker.cm.compiler.Tree.New;
import com.choicemaker.cm.compiler.Tree.NewArray;
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
import com.choicemaker.cm.compiler.parser.DefaultVisitor;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.util.ChoiceMakerCoreMessages;

/**
 * ClueMaker type checker
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public class TypeChecker extends DefaultVisitor implements Tags {

	/** the class repository of the compiler
	 */
	ClassRepository repository;

	/** a tool transforming tree descriptions of types into the local
	 *  Type abstraction
	 */
	DeriveType derive;

	// the following attributes describe the environment of the expression
	// to typecheck

	/** the compilation unit of this compiler pass
	 */
	ICompilationUnit unit;

	/** the current clue set symbol
	 */
	ClueSetSymbol set;

	/** the current type of r
	 */
	Type typeOfR = Type.NONE;

	/** the current scope
	 */
	Scope scope;

	/** the prototype (type expected from environment)
	 */
	Type prototype;

	// the following attribute is the result of typechecking the current
	// expression

	/** the corresponding type
	 */
	Type result;

	private boolean insideSimpleShorthand;

	/** Whether the clueset has decision */
	boolean hasDecision;
	
	/** Type of the clueset */
	private Type cluesetType;

	/** create a new EnterClues compiler pass for the given
	 *  compilation unit
	 */
	public TypeChecker(ICompilationUnit unit) {
		this.unit = unit;
		this.repository = unit.getCompilationEnv().repository;
		this.derive = new DeriveType(unit);
	}

	public void typecheck() throws CompilerException {
		this.set = null;
		this.scope = null;
		this.result = Type.ERROR;
		this.prototype = Type.ANY;
		typecheck(unit.getDecls());
	}

	public Type typecheck(Tree t) throws CompilerException {
		Type old = result;
		Type oldp = prototype;
		Scope olds = scope;
		result = Type.ERROR;
		prototype = Type.ANY;
		t.apply(this);
		t.type = result;
		result = old;
		prototype = oldp;
		scope = olds;
		return t.type;
	}

	public Type typecheck(Tree t, Type p, Scope s) throws CompilerException {
		return typecheck(t, p, s, false);
	}

	public Type typecheck(Tree t, Type p, Scope s, boolean boolWidensToByte) throws CompilerException {
		Type old = result;
		Type oldp = prototype;
		Scope olds = scope;
		scope = s;
		prototype = p;
		result = Type.ERROR;
		t.apply(this);
		t.type = result;
		result = old;
		scope = olds;
		prototype = oldp;
		if (t.type.subtype(p)
			|| boolWidensToByte
			&& t.type == Type.BOOLEAN
			&& Type.BYTE.subtype(p)
			|| boolWidensToByte
			&& t instanceof VarDecl)
			return t.type;
		else {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.expected.but.found", p, t.type));
			return t.type = Type.ERROR;
		}
	}

	public Type[] typecheck(Tree[] ts) throws CompilerException {
		if (ts == null)
			return Type.EMPTY;
		Type[] res = new Type[ts.length];
		for (int i = 0; i < ts.length; i++)
			res[i] = typecheck(ts[i]);
		return res;
	}

	public Type[] typecheck(Tree[] ts, Type p, Scope s) throws CompilerException {
		return typecheck(ts, p, s, false);
	}

	public Type[] typecheck(Tree[] ts, Type p, Scope s, boolean boolWidensToByte) throws CompilerException {
		if (ts == null)
			return Type.EMPTY;
		Type[] res = new Type[ts.length];
		for (int i = 0; i < ts.length; i++)
			res[i] = typecheck(ts[i], p, s, boolWidensToByte);
		return res;
	}

	protected boolean accessible(Symbol sym) {
		// check if the symbol is in the same package;
		// probably we have to check more here
		if (sym.innermostPackage() == unit.getPackage())
			return (sym.modifiers() & Modifiers.PRIVATE) == 0;
		else
			return (sym.modifiers() & Modifiers.PUBLIC) != 0;
	}

	public void visit(Tree t) {
		result = prototype;
	}

	public void visit(ClueSetDecl t) throws CompilerException {
		// declare the current clue set symbol
		this.set = t.sym;
		// typecheck the body of the clue set
		hasDecision = t.decision;
		Type type;
		if (t.tpe instanceof PrimitiveType) {
			type = derive.typeOf(t.tpe);
		} else {
			type = Type.DOUBLE;
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.primitivetype.expected"));
		}
		cluesetType = type;
		typecheck(t.body, type, set.members(), true);
		result = type;
	}

	public void visit(ClueDecl t) throws CompilerException {
		// enter clue if not already defined
		if (scope.lookup(t.name) != Symbol.NONE)
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.duplicate.clue", t.name));
		else
			scope.enter(t.sym);
		// create a local scope for this clue
		Scope local = new Scope(t.sym, scope);
		if (hasDecision && t.decision == NODEC || t.rule && t.decision == NODEC) {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.decision.expected"));
		}
		// enter indices
		typecheck(t.indices, Type.NONE, local);
		// typecheck the logical expression
		Type cst = t.rule ? Type.BOOLEAN : prototype;
		Type exprtype = typecheck(t.expr, cst, local, true);
		if (exprtype != Type.BOOLEAN) {
			if ((t.clueModifiers & Modifiers.REPORT) != 0) {
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.report.booleanonly"));
			}
			if ((t.clueModifiers & Modifiers.NOTE) != 0) {
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.note.booleanonly"));
			}
		}
		t.sym.setType(exprtype);
		result = exprtype;
	}

	public void visit(VarDecl t) throws CompilerException {
		// create a symbol for this variable
		t.sym = new VarSymbol(t.name, derive.typeOf(t.tpe), 0, scope.owner);
		// enter the symbol into the current scope if new
		if (scope.lookup(t.name) != Symbol.NONE)
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.variable.already.defined", t.name));
		else
			scope.enter(t.sym);
		// typecheck the initializer
		typecheck(t.initializer, t.sym.getType(), scope);
		result = Type.NONE;
	}

	public void visit(Index t) throws CompilerException {
		// create a symbol for this index
		t.sym = new VarSymbol(t.name, derive.typeOf(t.tpe), 0, scope.owner);
		// enter the symbol into the current scope if new
		if (scope.localLookup(t.name) != Symbol.NONE)
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.duplicate.index", t.name));
		else
			scope.enter(t.sym);
		// typecheck the initializer
		typecheck(t.initializer, new Type.ArrayType(t.sym.getType()), scope);
		result = Type.NONE;
	}

	public void visit(Quantified t) throws CompilerException {
		// create a local scope for this quantifier
		Scope local = new Scope(scope.owner, scope);
		t.sVars = new Symbol[t.vars.length];
		// create new symbols for the variables
		for (int i = 0; i < t.vars.length; i++) {
			Symbol sym = new VarSymbol(t.vars[i], Type.INT, Modifiers.QUANTVAR, local.owner, insideSimpleShorthand);
			t.sVars[i] = sym;
			local.enter(sym);
		}
		// typecheck body in the local scope
		typecheck(t.expr, Type.BOOLEAN, local);
		if (t.valueExpr != null) {
			result = typecheck(t.valueExpr, Type.DOUBLE, local);
		}
		for (int i = 0; i < t.sVars.length; ++i) {
			if (((VarSymbol) t.sVars[i]).range == null) {
				unit.error(
					t.pos,
					ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.bound.variable.not.used", t.vars[i]));
			}
		}
		if (t.valueExpr == null) {
			result = (t.quantifier == COUNT || t.quantifier == COUNTUNIQUE) ? Type.INT : Type.BOOLEAN;
		}
	}

	public void visit(Let t) throws CompilerException {
		// create a local scope for this let binder
		Scope local = new Scope(scope.owner, scope);
		// enter the bound variables
		typecheck(t.binders, Type.NONE, local);
		// check the body
		result = typecheck(t.expr, prototype, local);
	}

	public void visit(Shorthand t) throws CompilerException {
		Type old = typeOfR;
		switch (t.form) {
			case AND_SHORTHAND :
			case OR_SHORTHAND :
			case XOR_SHORTHAND :
				insideSimpleShorthand = true;
				typeOfR = unit.getDoubleIndexBaseClass().getType();
				typecheck(t.exprs, Type.BOOLEAN, scope);
				insideSimpleShorthand = false;
				break;
			case SWAPSAME :
				{
					typeOfR = unit.getExistsBaseClass().getType();
					typecheck(t.numPerConjunct, Type.INT, scope);
					typecheck(t.minNumMoved, Type.INT, scope);
					typeOfR = unit.getExistsBaseClass().getType();
					Type[] ts = typecheck(t.exprs, Type.ANY, scope);
					Type baseType = ts.length > 0 ? ts[0] : Type.VOID;
					for (int i = 0; i < ts.length; i++) {
						if (ts[i] == Type.VOID) {
							unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.illegal.void.type"));
						} else if (baseType.isPrim() ^ ts[i].isPrim()) {
							unit.error(t.pos, "Incompatible types");
						} else if (baseType.isPrim() && ts[i].isPrim()) {
							if (baseType.subtype(ts[i])) {
								baseType = ts[i];
							} else if (!ts[i].subtype(baseType)) {
								unit.error(t.pos, "Incompatible types");
							}
						}
					}
					t.baseType = baseType;
					break;
				}
			default :
				typeOfR = unit.getExistsBaseClass().getType();
				Type[] ts = typecheck(t.exprs, Type.ANY, scope);
				for (int i = 0; i < ts.length; i++)
					if (ts[i] == Type.VOID)
						unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.illegal.void.type"));
		}
		if (t.cond != null) {
			typecheck(t.cond, Type.BOOLEAN, scope);
		}
		typeOfR = old;
		if (t.form == Tags.COMPARE) {
			result = Type.BYTE;
		} else {
			result = Type.BOOLEAN;
		}
	}

	public void visit(Valid t) throws CompilerException {
		typecheck(t.access, Type.ANY, scope);
		if (t.access != null
			&& t.access.symbol() != null
			&& t.access.symbol().getOwner() != null
			&& (t.access.symbol().getOwner().modifiers() & Modifiers.SCHEMA) != 0)
			result = Type.BOOLEAN;
		else {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.only.schema.fields.valid"));
			result = Type.ERROR;
		}
	}

	public void visit(If t) throws CompilerException {
		// this is an "almost-like-the-spec" implementation; implementing
		// this according to �15.25 of the JLS would not make much sense
		// check condition
		typecheck(t.cond, Type.BOOLEAN, scope);
		// check then part
		typecheck(t.thenp, prototype, scope);
		// check else part
		typecheck(t.elsep, prototype, scope);
		// find common supertype
		if (t.thenp.type.subtype(t.elsep.type))
			result = t.elsep.type;
		else if (t.elsep.type.subtype(t.thenp.type))
			result = t.thenp.type;
		else {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.unable.type.conditional.expression"));
			result = Type.ERROR;
		}
	}

	public void visit(Apply t) throws CompilerException {
		// typecheck arguments
		Type[] argtypes = typecheck(t.args, Type.ANY, scope);
		// we assume here that methods throwing exceptions cannot
		// be called; Martin, is that right?
		Type mtype = typecheck(t.fun, new Type.MethodType(argtypes, Type.ERROR, Type.EMPTY), scope);
		// return result type of method
		if (mtype == Type.ERROR)
			result = mtype;
		else
			result = mtype.restype();
	}

	public void visit(New t) throws CompilerException {
		// typecheck arguments
		Type[] argtypes = typecheck(t.args, Type.ANY, scope);
		// again we assume here that constructors throwing exceptions
		// cannot be called
		Type mtype = new Type.MethodType(argtypes, Type.VOID, Type.EMPTY);
		// check type
		Type ctpe = derive.typeOf(t.clazz);
		if (ctpe == Type.ERROR)
			result = Type.ERROR;
		// check that we have an object type that can be instantiated
		else if (ctpe.isObject() && ((ctpe.sym().modifiers() & (Modifiers.ABSTRACT | Modifiers.INTERFACE)) == 0)) {
			// find constructor
			ScopeEntry e = ctpe.sym().members().lookupKindEntry(SemanticTags.MTH, "<init>");
			while (e != ScopeEntry.NONE) {
				if (mtype.subtype(e.sym.getType()) && accessible(e.sym))
					break;
				e = e.other(SemanticTags.MTH);
			}
			// no constructor found
			if (e == ScopeEntry.NONE) {
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.constructor.not.found"));
				result = Type.ERROR;
			} else
				result = ctpe;
		} else {
			unit.error(t.clazz.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.instantiate", ctpe));
			result = Type.ERROR;
		}
	}

	public void visit(NewArray t) throws CompilerException {
		if ((t.clazz == null) && (t.dims == null)) {
			if (prototype.isArray()) {
				Type tpe = prototype.elemtype();
				for (int i = 0; i < t.init.length; i++)
					typecheck(t.init[i], tpe, scope);
				result = prototype;
			} else {
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.derive.array.lit.type"));
				result = Type.ERROR;
			}
		} else if (t.dims == null) {
			Type tpe = derive.typeOf(t.clazz);
			if (tpe.isArray()) {
				Type etpe = tpe.elemtype();
				for (int i = 0; i < t.init.length; i++)
					typecheck(t.init[i], etpe, scope);
				result = tpe;
			} else {
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.array.type.expected"));
				result = Type.ERROR;
			}
		} else {
			for (int i = 0; i < t.dims.length; i++)
				typecheck(t.dims[i], Type.INT, scope);
			result = derive.typeOf(t.clazz);
		}
	}

	public void visit(Typeop t) throws CompilerException {
		Type tpe = derive.typeOf(t.tpe);
		Type etpe = typecheck(t.expr, Type.ANY, scope);
		if (tpe.subtype(etpe)
			|| etpe.subtype(tpe)
			|| ((tpe.sym().modifiers() & Modifiers.INTERFACE) != 0)
			|| ((etpe.sym().modifiers() & Modifiers.INTERFACE) != 0))
			switch (t.tag) {
				case TEST :
					result = Type.BOOLEAN;
					break;
				case CAST :
					result = tpe;
					break;
				default :
					throw new CompilerException("illegal type operator " + t.tag);
			} else {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.apply.type.operator"));
			result = Type.ERROR;
		}
	}

	public void visit(Unop t) throws CompilerException {
		result = Type.ERROR;
		switch (t.opcode) {
			case NOT :
				result = typecheck(t.arg, Type.BOOLEAN, scope);
				break;
			case COMP :
				if (isIntegral(typecheck(t.arg, Type.ANY, scope)))
					result = promote(t.arg.type);
				else
					unit.error(t.arg.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.value.integral.expected"));
				break;
			case PLUS :
			case MINUS :
				if (isNumeric(typecheck(t.arg, Type.ANY, scope)))
					result = promote(t.arg.type);
				else
					unit.error(
						t.arg.pos,
						ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.primitive.numeric.value.expected"));
				break;
			default :
				throw new CompilerException("illegal unary operator " + t.opcode);
		}
	}

	/** implements binary numeric promotion according to �5.6.2 of
	 *  the JLS
	 */
	protected Type promote(Type left, Type right) {
		if ((left == Type.ERROR) || (right == Type.ERROR))
			return Type.ERROR;
		else if ((left == Type.DOUBLE) || (right == Type.DOUBLE))
			return Type.DOUBLE;
		else if ((left == Type.FLOAT) || (right == Type.FLOAT))
			return Type.FLOAT;
		else if ((left == Type.LONG) || (right == Type.LONG))
			return Type.LONG;
		else
			return Type.INT;
	}

	/** implements unary numeric promotion according to �5.6.1 of
	 *  the JLS
	 */
	protected Type promote(Type type) {
		if (type == Type.ERROR)
			return Type.ERROR;
		else if ((type == Type.BYTE) || (type == Type.SHORT) || (type == Type.CHAR))
			return Type.INT;
		else
			return type;
	}

	protected boolean isNumeric(Type t) {
		return t.isPrim() && (t != Type.BOOLEAN) && (t != Type.ANY);
	}

	protected boolean isIntegral(Type t) {
		return isNumeric(t) && (t != Type.FLOAT) && (t != Type.DOUBLE);
	}

	public void visit(Binop t) throws CompilerException {
		Type left = typecheck(t.left, Type.ANY, scope);
		Type right = typecheck(t.right, Type.ANY, scope);
		result = Type.ERROR;
		switch (t.opcode) {
			case PLUS :
				if ((left == Type.ERROR) || (right == Type.ERROR)) {
					result = Type.ERROR;
					break;
				} else if (left.equals(repository.stringClass.getType()) || right.equals(repository.stringClass.getType())) {
					result = repository.stringClass.getType();
					break;
				}
			case MINUS :
			case MULT :
			case DIV :
			case MOD :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.ERROR;
				else if (isNumeric(left) && isNumeric(right))
					result = promote(left, right);
				else
					unit.error(
						t.pos,
						ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.illegal.operation.on.types", left, right));
				break;
			case LSHIFT :
			case RSHIFT :
			case URSHIFT :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.ERROR;
				else if (isIntegral(left) && isIntegral(right))
					result = promote(left);
				else
					unit.error(
						t.pos,
						ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.integer.primitive.numeric.required"));
				break;
			case LT :
			case GT :
			case LTEQ :
			case GTEQ :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.BOOLEAN;
				else if (isNumeric(left) && isNumeric(right))
					result = Type.BOOLEAN;
				else
					unit.error(
						t.pos,
						ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.only.primitive.numeric.comparable"));
				break;
			case EQEQ :
			case NOTEQ :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.BOOLEAN;
				else if (left.equals(Type.BOOLEAN)) {
					if (!right.equals(Type.BOOLEAN))
						unit.error(t.right.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.boolean.required"));
					else
						result = Type.BOOLEAN;
				} else if (isNumeric(left)) {
					if (!isNumeric(right))
						unit.error(
							t.right.pos,
							ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.primitive.numeric.required"));
					else
						result = Type.BOOLEAN;
				} else if (left.isRef()) {
					if (left.subtype(right)
						|| right.subtype(left)
						|| ((left.sym().modifiers() & Modifiers.INTERFACE) != 0)
						|| ((right.sym().modifiers() & Modifiers.INTERFACE) != 0))
						result = Type.BOOLEAN;
					else
						unit.error(
							t.right.pos,
							ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.primitive.numeric.required"));
				} else
					unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.compare"));
				break;
			case AND :
			case OR :
			case XOR :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.BOOLEAN;
				else if (left.equals(Type.BOOLEAN)) {
					if (!right.equals(Type.BOOLEAN))
						unit.error(t.right.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.boolean.required"));
					else
						result = Type.BOOLEAN;
				} else if (isNumeric(left) && isNumeric(right))
					result = promote(left, right);
				else
					unit.error(
						t.pos,
						ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.illegal.operation.on.types", left, right));
				break;
			case ANDAND :
			case OROR :
				if ((left == Type.ERROR) || (right == Type.ERROR))
					result = Type.BOOLEAN;
				else if ((left.equals(Type.BOOLEAN)) && (right.equals(Type.BOOLEAN)))
					result = Type.BOOLEAN;
				else
					unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.boolean.operands.required"));
				break;
			default :
				throw new CompilerException("illegal binary operator");
		}
	}

	public void visit(Indexed t) throws CompilerException {
		presetMultiClueAccess(t, 1);
		if (t.multiClue != null) {
			if (t.index != null) {
				Index index = t.multiClue.indices[t.multiClue.indexNum];
				t.index = checkMultiClueIndex(index, t.index, t.pos);
				if (t.mIndex != null) {
					t.mIndex = checkMultiClueIndex(index, t.mIndex, t.pos);
				}
			}
			if(cluesetType != Type.BOOLEAN && (t.index == null || t.mIndex != null)) {
				unit.error(t.pos, "Only single index notation allowed for non-boolean clue references");
			}
			t.expr.apply(this);
			result = t.multiClue.multiClue.type;
		} else {
			boolean doubleIndexed = t.mIndex != null;
			if (t.index != null) {
				typecheck(t.index, Type.INT, scope);
			} else {
				unit.error(t.pos, "Expected int.");
				return;
			}
			if (doubleIndexed) {
				typecheck(t.mIndex, Type.INT, scope);
			}
			boolean insideDoubleIndex = false;
			if (!doubleIndexed
				&& quantifierIndex(t.index)
				&& ((VarSymbol) t.index.symbol()).definedInsideSimpleShorthand
				&& accessOfR(t.expr)) {
				insideDoubleIndex = true;
				t.mIndex = t.index;
			}
			typecheck(t.expr, new Type.ArrayType(prototype, doubleIndexed || insideDoubleIndex), scope);
			useIndex(t.index, t.expr, doubleIndexed, true);
			if (doubleIndexed) {
				useIndex(t.mIndex, t.expr, doubleIndexed, false);
			}
			result = t.expr.type.elemtype();
		}
	}

	private Tree checkMultiClueIndex(Index index, Tree idx, int p) throws CompilerException {
		if (index.isSimple()) {
			Tree[] inits = index.getInitializers();
			int pos = 0;
			while (pos < inits.length && !inits[pos].equals(idx)) {
				++pos;
			}
			if (pos < inits.length) {
				idx = new Literal(Location.NOPOS, Tags.INT, new Integer(pos));
			} else {
				unit.error(p, "Unknown index.");
			}
		} else {
			typecheck(idx, Type.INT, scope);
		}
		return idx;
	}

	private void presetMultiClueAccess(Indexed idx, int level) throws CompilerException {
		if (idx.multiClue == null) {
			Tree expr = idx.expr;
			if (expr instanceof Ident) {
				typecheck(expr, Type.ANY, scope);
				Symbol s = expr.symbol();
				if (s instanceof ClueSymbol && ((ClueSymbol)s).decl.indices.length > 0) {
					Index[] indices = ((ClueSymbol) s).decl.indices;
					if (indices.length == level) {
						idx.multiClue = new Indexed.MultiClueIndex((Ident) expr, indices, 0);
					}
				}
			} else if (expr instanceof Indexed) {
				Indexed ie = (Indexed) expr;
				presetMultiClueAccess((Indexed) expr, level + 1);
				if (ie.multiClue != null) {
					idx.multiClue =
						new Indexed.MultiClueIndex(
							ie.multiClue.multiClue,
							ie.multiClue.indices,
							ie.multiClue.indexNum + 1);
				}
			}
		}
	}

	private boolean accessOfR(Tree expr) {
		if (expr instanceof Tree.Self) {
			if (((Tree.Self) expr).stag == Tags.R) {
				return true;
			}
		} else if (expr instanceof Tree.Indexed) {
			return accessOfR(((Tree.Indexed) expr).expr);
		} else if (expr instanceof Tree.Select) {
			return accessOfR(((Select) expr).qualifier);
		}
		return false;
	}

	private boolean quantifierIndex(Tree index) {
		return index instanceof Ident
			&& index.symbol() instanceof VarSymbol
			&& (index.symbol().getModifiers() & Modifiers.QUANTVAR) != 0;
	}

	private void useIndex(Tree index, Tree expr, boolean doubleIndexed, boolean q) throws CompilerException {
		if (quantifierIndex(index)) {
			VarSymbol s = (VarSymbol) index.symbol();
			if (doubleIndexed) {
				expr = setIndex(expr, q);
			}
			if (s.range == null) {
				s.range = expr;
			} else if (!s.range.equals(expr)) {
				unit.error(expr.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.incompatible.ranges", s));
			}
		}
	}

	public static Tree setIndex(Tree expr, boolean q) {
		if (expr instanceof Tree.Self) {
			if (((Tree.Self) expr).stag == Tags.R) {
				return new Tree.Self(expr.pos, q ? Tags.Q : Tags.M);
			}
		} else if (expr instanceof Tree.Indexed) {
			Tree.Indexed idx = (Tree.Indexed) expr;
			return new Tree.Indexed(expr.pos, setIndex(idx.expr, q), q ? idx.index : idx.mIndex);
		} else if (expr instanceof Tree.Select) {
			Select s = (Select) expr;
			return new Tree.Select(expr.pos, setIndex(s.qualifier, q), s.name);
		}
		return expr;
	}

	public void visit(Select t) throws CompilerException {
		// typecheck qualifier
		Type tpe = typecheck(t.qualifier, Type.ANY, scope);
		t.sym = Symbol.BAD;
		// we are finished if the qualifier could not be typechecked
		if (tpe.tag == SemanticTags.ERROR)
			result = Type.ERROR;
		// is the qualifier a package?
		else if (tpe.isPackage()) {
			// find type
			Symbol s = tpe.sym().members().lookup(t.name);
			if (s == Symbol.NONE) {
				PackageSymbol pck = repository.definePackage(tpe.sym().fullname() + "." + t.name);
				if (pck.exists()) {
					t.sym = pck;
					result = pck.getType();
				} else
					unit.error(
						t.pos,
						ChoiceMakerCoreMessages.m.formatMessage(
							"compiler.typechecker.cannot.resolve.identifier.in",
							t.name,
							t.qualifier));
			} else if (accessible(s))
				t.sym = s;
			else
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.access", s));
			result = t.sym.getType();
			// is the qualifier an object?
		} else if (tpe.isObject()) {
			// do we select from a class (i.e. only static members are
			// selectable)?
			boolean fromType = (t.qualifier.symbol().getKind() == SemanticTags.TYP);
			// find member (field or method)
			Type proto = Type.ANY;
			int kind = SemanticTags.VAR;
			if (prototype.isMethod()) {
				kind = SemanticTags.MTH;
				proto = prototype;
			}
			// start lookup in the current class
			ClassSymbol c = (ClassSymbol) tpe.sym();
			// the pool specifies superclasses and interfaces to search
			Stack pool = new Stack();
			ClassSymbol[] intfs = c.interfaces();
			for (int i = 0; i < intfs.length; i++)
				pool.push(intfs[i]);
			// begin lookup
			outer : while (true) {
				ScopeEntry e = c.members().lookupKindEntry(kind, t.name);
				while (e != ScopeEntry.NONE) {
					if (e.sym.getType().subtype(proto)
						&& accessible(e.sym)
						&& (!fromType || ((e.sym.modifiers() & Modifiers.STATIC) != 0))) {
						t.sym = e.sym;
						break outer;
					}
					e = e.other(kind);
				}
				if (c == repository.objectClass) {
					if (pool.empty()) {
						unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.member.not.found", t.name));
						break;
					}
					c = (ClassSymbol) pool.pop();
				} else {
					c = c.superclass();
					intfs = c.interfaces();
					for (int i = 0; i < intfs.length; i++)
						pool.push(intfs[i]);
				}
			}
			result = t.sym.getType();
			// unable to select from a value that is not a package or an object
		} else {
			unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.select", t.name, tpe));
			result = Type.ERROR;
		}
	}

	public void visit(Ident t) throws CompilerException {
		if (t.name == "null") {
			t.sym = Symbol.NULL;
			result = Type.NULL;
			return;
		}
		Type proto = Type.ANY;
		result = Type.ERROR;
		int kind = SemanticTags.VAR;
		if (prototype.isMethod()) {
			kind = SemanticTags.MTH;
			proto = prototype;
		}
		t.sym = Symbol.BAD;
		// try to find the identifier in the local scopes
		ScopeEntry e = scope.lookupEntry(t.name);
		while (e != ScopeEntry.NONE) {
			if (e.sym.getType().subtype(proto) && accessible(e.sym)) {
				t.sym = e.sym;
				break;
			}
			e = e.other();
		}
		// if we did not find it yet, we have to assume it is a
		// package or a class
		if (t.sym == Symbol.BAD) {
			if (kind == SemanticTags.MTH)
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.method.not.found"));
			else if (unit.getAmbiguousImports().lookup(t.name) != Symbol.NONE)
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.ambiguous.class.reference"));
			else {
				// start looking into the named import scope
				Symbol s = unit.getNamedImports().lookup(t.name);
				if ((s != Symbol.NONE) && accessible(s)) {
					t.sym = s;
					result = s.getType();
					return;
				}
				// now look into the scope of the current package
				s = unit.getPackage().members().lookup(t.name);
				if (s != Symbol.NONE) {
					t.sym = s;
					result = s.getType();
					return;
				}
				// finally check the star import scope
				s = unit.getStarImports().lookup(t.name);
				if ((s != Symbol.NONE) && accessible(s)) {
					t.sym = s;
					result = s.getType();
					// assume it is a package
				} else {
					PackageSymbol pck = repository.definePackage(t.name);
					if (pck.exists())
						result = pck.getType();
					else {
						unit.error(
							t.pos,
							ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.cannot.resolve.identifier", t.name));
						result = Type.ERROR;
					}
				}
				return;
			}
		}
		result = t.sym.getType();
	}

	public void visit(Self t) throws CompilerException {
		switch (t.stag) {
			case Q :
			case M :
				result = unit.getBaseClass().getType();
				break;
			case R :
				if (typeOfR == Type.NONE) {
					unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.r.outside.shorthand"));
					result = Type.ERROR;
				} else
					result = typeOfR;
				break;
			default :
				throw new CompilerException("illegal self tag " + t.stag);
		}
	}

	public void visit(Literal t) throws CompilerException {
		switch (t.ltag) {
			case INT :
				int i = ((Number) t.value).intValue();
				if ((Byte.MIN_VALUE <= i) && (i <= Byte.MAX_VALUE))
					result = Type.BYTE;
				else if ((Short.MIN_VALUE <= i) && (i <= Short.MAX_VALUE))
					result = Type.SHORT;
				else
					result = Type.INT;
				break;
			case LONG :
				result = Type.LONG;
				break;
			case FLOAT :
				result = Type.FLOAT;
				break;
			case DOUBLE :
				result = Type.DOUBLE;
				break;
			case CHAR :
				result = Type.CHAR;
				break;
			case BOOLEAN :
				result = Type.BOOLEAN;
				break;
			case STRING :
				result = repository.stringClass.getType();
				break;
			default :
				unit.error(t.pos, ChoiceMakerCoreMessages.m.formatMessage("compiler.typechecker.unknown.literal"));
				result = Type.ERROR;
		}
	}
}
