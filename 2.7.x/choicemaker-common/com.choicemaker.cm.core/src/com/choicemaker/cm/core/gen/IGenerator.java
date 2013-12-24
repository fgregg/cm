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
package com.choicemaker.cm.core.gen;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author rphall
 */
public interface IGenerator {
	/**
	 * Returns the package of the generated code.
	 * Plugins may also generate code in subpackages.
	 *
	 * @return   The package of the generated code.
	 * @throws   GenException  if the data cannot be read.
	 */
	public abstract String getPackage();
	public abstract String getExternalPackage();
	public abstract boolean isIntern();
	/**
	 * Returns the root directory for the generated source code.
	 *
	 * If this returns <code>/tmp</code> and <code>getPackage</code>
	 * returns <code>cust.gend</code>, then the source code should
	 * be placed into <code>/tmp/cust/gend</code>. This value
	 * is returned by <code>getSourceCodeRoot</code>.
	 *
	 * @return   The root directory for the generated source code.
	 * @throws   GenException  if the data cannot be read.
	 * @see      getSourceCodePackageRoot
	 * @see      getPackage
	 */
	public abstract String getSourceCodeRoot() throws GenException;
	/**
	 * Returns the directory for the generated source code.
	 *
	 * @return   The directory for the generated source code.
	 * @throws   GenException  if the data cannot be read.
	 */
	public abstract String getSourceCodePackageRoot() throws GenException;
	public abstract String getExternalSourceCodePackageRoot()
		throws GenException;
	/**
	 * Returns the name of the clue set.
	 *
	 * @return   The name of the clue set.
	 */
	public abstract String getClueSetName();
	public abstract String getSchemaName();
	/**
	 * Returns the JDOM Document representing the ChoiceMaker schema.
	 *
	 * @return  The JDOM Document representing the ChoiceMaker schema.
	 */
	public abstract Document getDocument();
	/**
	 * Returns the root element of the JDOM Document representing the ChoiceMaker schema.
	 *
	 * @return  The root element of the JDOM Document representing the ChoiceMaker schema.
	 */
	public abstract Element getRootElement();
	/**
	 * Returns the root record of the ChoiceMaker schema.
	 *
	 * @return  The root record of the ChoiceMaker schema.
	 */
	public abstract Element getRootRecord();
	/**
	 * Returns the maximal record stacking depth.
	 *
	 * For example, if there is a main record patient, which has
	 * a nested record contacts, which in turn has a nested record
	 * race (and this is the deepest stacking), this method returns 3.
	 *
	 * @return   The maximal record stacking depth.
	 */
	public abstract int getStackingDepth();
	/**
	 * Returns the import statements to be added to generated code.
	 *
	 * @return   The import statements to be added to generated code.
	 */
	public abstract String getImports();
	/**
	 * Adds a generated Java source file to the list of generated files.
	 *
	 * @param  fileName  The fully qualified file name of the Java source file.
	 */
	public abstract void addGeneratedFile(String fileName);
	public abstract void error(String message);
	public abstract void warning(String message);
	public abstract boolean hasErrors();
	/**
	 * Generate files.
	 */
	public abstract void generate();
	/**
	 * Adds to the import section of the Accessor class.
	 *
	 * @param   imp  The text to be added to the import section.
	 */
	public abstract void addAccessorImport(String imp);
	/**
	 * Adds to the implements list of the Accessor class.
	 *
	 * @param   imp  The text to be added to the implements list.
	 *            Must start with a comma, e.g., <code>", OraAccessor"</code>.
	 */
	public abstract void addAccessorImplements(String imp);
	/**
	 * Adds to the body section of the Accessor class.
	 *
	 * @param   decls  The text to be added to the body section.
	 */
	public abstract void addAccessorBody(String decls);
}
