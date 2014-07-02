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
package com.choicemaker.cm.compiler;

import java.io.Writer;
import java.util.List;

import com.choicemaker.cm.compiler.Symbol.ClassSymbol;
import com.choicemaker.cm.compiler.Symbol.PackageSymbol;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * Objects of that class represent a single compilation unit;
 * The current version of ClueMaker compiles only a single
 * compilation unit.
 *
 * @author   Matthias Zenger
 * @author   Martin Buechi
 * @author   rphall
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public interface ICompilationUnit {
	public abstract void compile() throws CompilerException;
	public abstract void syntacticAnalysis() throws CompilerException;
	public abstract void semanticAnalysis() throws CompilerException;
	public abstract void codeGeneration() throws CompilerException;
	/** issue an error for this compilation context
	 */
	public abstract void error(String message) throws CompilerException;
	/** issue a warning for this compilation context
	 */
	public abstract void warning(String message);
	/** issue an error for a specific line of this compilation context
	 */
	public abstract void error(int pos, String message) throws CompilerException;
	/** issue a warning for a specific source code file
	 */
	public abstract void warning(Sourcecode source, String message);
	/** issue a warning for a specific line of a specific source code file
	 */
	public abstract void warning(int pos, String message);
	public abstract void conclusion(Writer w);
	/**
	 * Call-back method for the Generator to populate the symbol table
	 * with the holder classes.
	 *
	 * All classes are in the same package as the clue set file generated
	 * by the ClueMaker compiler.
	 *
	 * Currently, the non-qualified name is passed, e.g., Sample__patient.
	 * I can easily change this to pass the fully qualified name instead.
	 *
	 * @param   className  The name of the class.
	 */
	public abstract void addClassType(String className);
	/**
	 * Adds a field to a class type created with addClassType.
	 *
	 * @param   className  The name of the class.
	 * @param   typeName  The name of the type.
	 * @param   fieldName  The name of the field.
	 */
	public abstract void addField(
		String className,
		String typeName,
		String fieldName) throws CompilerException;
	/**
	 * Adds a field to a class type created with addClassType.
	 *
	 * The typeName must be added via addClassType prior to calling this method.
	 *
	 * @param   className  The name of the class.
	 * @param   typeName  The name of the type.
	 * @param   recordName  The name of the record.
	 */
	public abstract void addNestedRecord(
		String className,
		String typeName,
		String fieldName) throws CompilerException;
	/**
	 * Defines the type for Q and M.
	 *
	 * @param  name  The name of the type. Must be created with addClassType
	 *           prior to calling this method.
	 */
	public abstract void setBaseType(String name);
	public abstract void addGeneratedJavaSourceFile(String fileName);
	public abstract ClassSymbol createSameBaseClass(ClassSymbol c);
	public abstract ClassSymbol createExistsBaseClass(ClassSymbol c);
	public abstract ClassSymbol createDoubleIndexBaseClass(ClassSymbol c);
	/**
	 * Get the value of packageName.
	 * @return value of packageName.
	 */
	public abstract String getPackageName();
	/**
	 * Set the value of packageName.
	 * @param v  Value to assign to packageName.
	 */
	public abstract void setPackageName(String v);
	public abstract void setAccessorClass(String accessorClass);
	public abstract String getAccessorClass();
	// FIXME REMOVEME public abstract void setAmbiguousImports(Scope ambiguousImports);
	public abstract Scope getAmbiguousImports();
	// FIXME REMOVEME public abstract void setBaseClass(Symbol baseClass);
	public abstract Symbol getBaseClass();
	public abstract void setClueSetFileName(String clueSetFileName);
	public abstract String getClueSetFileName();
	// FIXME REMOVEME public abstract void setDecls(Tree[] decls);
	public abstract Tree[] getDecls();
	public abstract void setDoubleIndexBaseClass(Symbol doubleIndexBaseClass);
	public abstract Symbol getDoubleIndexBaseClass();
	// FIXME REMOVEME public abstract void setCompilationEnv(CompilationEnv env);
	public abstract CompilationEnv getCompilationEnv();
	// FIXME REMOVEME public abstract void setErrors(int errors);
	public abstract int getErrors();
	public abstract void setExistsBaseClass(Symbol existsBaseClass);
	public abstract Symbol getExistsBaseClass();
	// FIXME REMOVEME public abstract void setGeneratedJavaSourceFiles(List generatedJavaSourceFiles);
	public abstract List getGeneratedJavaSourceFiles();
	public abstract void setIntern(boolean intern);
	public abstract boolean isIntern();
	// FIXME REMOVEME public abstract void setNamedImports(Scope namedImports);
	public abstract Scope getNamedImports();
	// FIXME REMOVEME public abstract void setPackage(PackageSymbol pckage);
	public abstract PackageSymbol getPackage();
	public abstract void setSchemaName(String schemaName);
	public abstract String getSchemaName();
	// FIXME REMOVEME public abstract void setSource(Sourcecode source);
	public abstract Sourcecode getSource();
	// FIXME REMOVEME public abstract void setStarImports(Scope starImports);
	public abstract Scope getStarImports();
	public abstract void setTarget(Tree[] target);
	public abstract Tree[] getTarget();
	// FIXME REMOVEME public abstract void setWarnings(int warnings);
	public abstract int getWarnings();
}
