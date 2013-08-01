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

import com.choicemaker.cm.compiler.ClassRepository;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.SemanticTags;
import com.choicemaker.cm.compiler.Symbol;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Tree.ArrayType;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.PrimitiveType;
import com.choicemaker.cm.compiler.Tree.Select;
import com.choicemaker.cm.compiler.parser.DefaultVisitor;
import com.choicemaker.cm.core.compiler.CompilerException;
/**
 * Helper class for TypeChecker.
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public class DeriveType extends DefaultVisitor implements SemanticTags {

	/** the compilation unit of this compiler pass
	 */
	ICompilationUnit unit;

	/** the class repository of the compiler
	 */
	ClassRepository repository;

	/** the result type
	 */
	Type result;

	/** the required result type kind
	 */
	int kind = TYP;

	/** create a new DeriveType visitor
	 */
	public DeriveType(ICompilationUnit unit) {
		this.unit = unit;
		this.repository = unit.getCompilationEnv().repository;
	}

	/** return true if the class symbol sym is accessible from the
	 *  current package
	 */
	protected boolean accessible(Symbol sym) {
		return (sym.getOwner() == unit.getPackage()) || ((sym.modifiers() & Modifiers.PUBLIC) != 0);
	}

	/** create a tree from a qualified name
	 */
	protected Tree qualid(int pos, String name) {
		int i = name.lastIndexOf('.');
		if (i > 0)
			return new Select(pos, qualid(pos, name.substring(0, i)), name.substring(i + 1).intern());
		else
			return new Ident(pos, name.intern());
	}

	/** return the type for a type specification given as a string
	 */
	public Type typeOf(String s) throws CompilerException {
		s = s.trim();
		int dim = 0;
		// we make the assumption here that there are no whitespaces
		// between the opening and closing bracket
		while (s.endsWith("[]")) {
			dim++;
			s = s.substring(0, s.length() - 2).trim();
		}
		Type res;
		if (s.equals("byte"))
			res = Type.BYTE;
		else if (s.equals("short"))
			res = Type.SHORT;
		else if (s.equals("char"))
			res = Type.CHAR;
		else if (s.equals("int"))
			res = Type.INT;
		else if (s.equals("long"))
			res = Type.LONG;
		else if (s.equals("float"))
			res = Type.FLOAT;
		else if (s.equals("double"))
			res = Type.DOUBLE;
		else if (s.equals("boolean"))
			res = Type.BOOLEAN;
		else
			res = typeOf(qualid(Location.NOPOS, s));
		while (dim-- > 0)
			res = new Type.ArrayType(res);
		return res;
	}

	/** return the type for a type specification given as a tree
	 */
	public Type typeOf(Tree t) throws CompilerException {
		t.apply(this);
		return t.type = result;
	}

	public Type[] typeOf(Tree[] ts) throws CompilerException {
		if (ts == null)
			return null;
		Type[] res = new Type[ts.length];
		for (int i = 0; i < ts.length; i++) {
			ts[i].apply(this);
			res[i] = ts[i].type = result;
		}
		return res;
	}

	public void visit(Tree t) throws CompilerException {
		unit.error(t.pos, "not a legal type");
		result = Type.ERROR;
	}

	public void visit(Select t) throws CompilerException {
		int oldkind = kind;
		kind = PCK;
		Symbol s = typeOf(t.qualifier).sym();
		kind = oldkind;
		result = Type.ERROR;
		if (s.getKind() == TYP)
			unit.error(t.pos, "implementation restriction; cannot select type from class");
		else if ((s.getKind() == PCK) && (kind == PCK))
			result = repository.definePackage(t.toString()).getType();
		else if (s.getKind() == PCK) {
			s = s.members().lookup(t.name);
			if (s == Symbol.NONE)
				unit.error(t.pos, "class " + t.name + " is undefined in package " + t.qualifier);
			else
				result = s.getType();
		} else
			unit.error(t.pos, "cannot select " + t.name + " from " + s.getType());
	}

	public void visit(Ident t) throws CompilerException {
		result = Type.ERROR;
		if (kind == PCK)
			result = repository.definePackage(t.name).getType();
		else {
			// first check that we do not have an ambiguous class name
			if (unit.getAmbiguousImports().lookup(t.name) != Symbol.NONE)
				unit.error(t.pos, "ambiguous class reference to " + t.name);
			else {
				// start looking into the named import scope
				Symbol s = unit.getNamedImports().lookup(t.name);
				if ((s != Symbol.NONE) && accessible(s)) {
					result = s.getType();
					return;
				}
				// now look into the scope of the current package
				s = unit.getPackage().members().lookup(t.name);
				if (s != Symbol.NONE) {
					result = s.getType();
					return;
				}
				// finally check the star import scope
				s = unit.getStarImports().lookup(t.name);
				if ((s != Symbol.NONE) && accessible(s))
					result = s.getType();
				else
					unit.error(t.pos, "type " + t.name + " unknown");
			}
		}
	}

	public void visit(ArrayType t) throws CompilerException {
		result = new Type.ArrayType(typeOf(t.tpe));
	}

	public void visit(PrimitiveType t) throws CompilerException {
		switch (t.ttag) {
			case Tags.BYTE :
				result = Type.BYTE;
				break;
			case Tags.SHORT :
				result = Type.SHORT;
				break;
			case Tags.INT :
				result = Type.INT;
				break;
			case Tags.LONG :
				result = Type.LONG;
				break;
			case Tags.FLOAT :
				result = Type.FLOAT;
				break;
			case Tags.DOUBLE :
				result = Type.DOUBLE;
				break;
			case Tags.CHAR :
				result = Type.CHAR;
				break;
			case Tags.BOOLEAN :
				result = Type.BOOLEAN;
				break;
			case Tags.VOID :
				result = Type.VOID;
				break;
			default :
				throw new CompilerException("illegal primitive type " + t.ttag);
		}
	}
}
