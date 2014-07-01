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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.choicemaker.cm.compiler.ClassRepository;
import com.choicemaker.cm.compiler.ICompilationUnit;
import com.choicemaker.cm.compiler.Modifiers;
import com.choicemaker.cm.compiler.ScopeEntry;
import com.choicemaker.cm.compiler.SemanticTags;
import com.choicemaker.cm.compiler.Symbol;
import com.choicemaker.cm.compiler.Symbol.ClassSymbol;
import com.choicemaker.cm.compiler.Symbol.ClueSetSymbol;
import com.choicemaker.cm.compiler.Symbol.ClueSymbol;
import com.choicemaker.cm.compiler.Symbol.MethodSymbol;
import com.choicemaker.cm.compiler.Symbol.PackageSymbol;
import com.choicemaker.cm.compiler.Tags;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.ClueSetDecl;
import com.choicemaker.cm.compiler.Tree.ImportDecl;
import com.choicemaker.cm.compiler.Tree.MethodDecl;
import com.choicemaker.cm.compiler.Tree.VarDecl;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Type.MethodType;
import com.choicemaker.cm.compiler.gen.GeneratorImpl;
import com.choicemaker.cm.compiler.parser.DefaultVisitor;
import com.choicemaker.cm.core.compiler.CompilerException;
import com.choicemaker.cm.core.gen.GenException;

/**
 * a tool transforming tree descriptions of types into the local
 * Type abstraction
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:14:09 $
 */
public class EnterClues extends DefaultVisitor implements Tags {

	/** the compilation unit of this compiler pass
	 */
	ICompilationUnit unit;

	/** the class repository of the compiler
	 */
	ClassRepository repository;

	/** a tool transforming tree descriptions of types into the local
	 *  Type abstraction
	 */
	DeriveType derive;

	/** the current clue set symbol
	 */
	ClueSetSymbol set;

	//    String cPkge = "";

	/** create a new EnterClues compiler pass for the given
	 *  compilation unit
	 */
	public EnterClues(ICompilationUnit unit) {
		this.unit = unit;
		this.repository = unit.getCompilationEnv().repository;
		this.derive = new DeriveType(unit);
	}

	public void enter() throws CompilerException {
		applyTo(unit.getDecls());
	}

	public void applyTo(Tree t) throws CompilerException {
		t.apply(this);
	}

	public void applyTo(Tree[] ts) throws CompilerException {
		if (ts == null)
			return;
		for (int i = 0; i < ts.length; i++)
			ts[i].apply(this);
	}

	public void visit(Tree t) {
	}

	//     public void visit(PackageDecl t) {
	// 	Tree pckage = t.pckage;
	// 	while(pckage instanceof Select) {
	// 	    cPkge = "." + ((Select)pckage).name + cPkge;
	// 	    pckage = ((Select)pckage).qualifier;
	// 	}
	// 	cPkge = ((Ident)pckage).name + cPkge;
	//     }

	public void visit(ImportDecl t) {
		if (t.starImport) {
			PackageSymbol p = repository.definePackage(t.pckage.toString());
			ScopeEntry e = p.members().elems;
			while (e != ScopeEntry.NONE) {
				Symbol s = unit.getStarImports().lookup(e.sym.getName());
				if ((s != e.sym) && (s != Symbol.NONE))
					unit.getAmbiguousImports().enterIfNew(e.sym);
				else if (s != e.sym)
					unit.getStarImports().enter(e.sym);
				e = e.next;
			}
		} else {
			ClassSymbol c = repository.defineClass(t.pckage.toString());
			c.load();
			Symbol s = unit.getNamedImports().lookup(c.getName());
			if ((s != c) && (s != Symbol.NONE))
				unit.getAmbiguousImports().enterIfNew(c);
			else if (s != c)
				unit.getNamedImports().enter(c);
		}
	}

	public void visit(ClueSetDecl t) throws CompilerException {
		if (t.name == null || !(t.name + ".clues").equals(unit.getSource().getShortName())) {
			unit.error("Clueset " + t.name + " must be defined in a file named " + t.name + ".clues.");
		}
		// process the schema descriptor of this clueset; as a side-effect,
		// holder classes will be created in the local package and entered
		// into the symbol table
		unit.setSchemaName(t.uses);
		GeneratorImpl g =
			new GeneratorImpl(
				unit,
				t.name,
				new File(unit.getSource().toString()).getAbsoluteFile().getParent() + File.separator + t.uses + ".schema",
				t.uses,
				unit.getPackageName());
		g.generate();
		if (unit.getErrors() == 0) {
			try {
				unit.setClueSetFileName(g.getSourceCodePackageRoot() + File.separator + t.name + "ClueSet.java");
			} catch (GenException ex) {
			}
			unit.addGeneratedJavaSourceFile(unit.getClueSetFileName());
//			unit.sameBaseClass = unit.createSameBaseClass((ClassSymbol) unit.baseClass);
			// 2009-03-12 rphall
			// DEBUG ClassCastException (mid-refactoring)
			Symbol _dbg_baseClass = unit.getBaseClass();
			ClassSymbol _dbg_classSymbol = (ClassSymbol) _dbg_baseClass;
			unit.setExistsBaseClass(unit.createExistsBaseClass(_dbg_classSymbol));
			// END DEBUG
			unit.setDoubleIndexBaseClass(unit.createDoubleIndexBaseClass((ClassSymbol) unit.getBaseClass()));
			// create a symbol and enter the body in the scope
			t.sym = set = new ClueSetSymbol(t.name, unit.getPackage(), (ClassSymbol) unit.getBaseClass());
			applyTo(t.body);
		}
	}

	public void visit(ClueDecl t) {
		t.sym = new ClueSymbol(t.name, set, t);
		// we check that later:
		//if (set.members().lookup(t.name) == Symbol.NONE)
		//    set.members().enter(t.sym);
		//else
		//    unit.error(t.pos, "duplicate definition of clue " + t.name);
	}

	public void visit(MethodDecl t) throws CompilerException {
		// enter parameters and thrown clause first
		applyTo(t.params);
		applyTo(t.thrown);
		// construct method type and check that parameter names are not
		// used more than once
		Type[] args = new Type[t.params.length];
		Set argset = new HashSet();
		for (int i = 0; i < args.length; i++) {
			if (argset.contains(t.params[i].name))
				unit.error(t.params[i].pos, "duplicate definition of parameter " + t.params[i].name);
			args[i] = t.params[i].tpe.type;
		}
		Type[] thrown = new Type[t.thrown == null ? 0 : t.thrown.length];
		for (int i = 0; i < thrown.length; i++)
			thrown[i] = t.thrown[i].type;
		t.sym =
			new MethodSymbol(
				t.name,
				new MethodType(args, derive.typeOf(t.restpe), thrown),
				Modifiers.PUBLIC | Modifiers.STATIC,
				set);
		// check that method does not get implemented twice
		ScopeEntry e = set.members().lookupKindEntry(SemanticTags.MTH, t.name);
		while (e != ScopeEntry.NONE) {
			if (e.sym.getType().equals(t.sym.getType())) {
				unit.error(t.pos, "duplicate definition of method " + t.name);
				break;
			}
			e = e.other(SemanticTags.MTH);
		}
		// enter method into scope of current clue set
		if (e == ScopeEntry.NONE)
			set.members().enter(t.sym);
	}

	public void visit(VarDecl t) throws CompilerException {
		// get parameter type
		derive.typeOf(t.tpe);
		// since we do not check java methods, we do not generate symbols here
	}
}
