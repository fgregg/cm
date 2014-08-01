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
public interface IClueSetGenerator {

	/**
	 * Returns the package of the generated clue set.
	 * @return   The package of the generated code.
	 */
	public abstract String getPackage();

	/**
	 * Returns the directory for the generated ClueMaker code
	 * of the clue set.
	 * @return   The root directory for the generated source code.
	 */
	public abstract String getClueSetDirectory() throws GenException;

	/**
	 * Returns the name of the clue set.
	 * @return   The name of the clue set.
	 */
	public abstract String getClueSetName();

	/**
	 * Returns the name of the record schema used by the clue set.
	 */
	public abstract String getSchemaName();

	/**
	 * Returns the JDOM Document representing the ChoiceMaker schema.
	 */
	public abstract Document getRecordSchema();

	/**
	 * Returns the root element of the JDOM Document representing the ChoiceMaker schema:
	 * e.g. <code>ChoiceMakerSchema</code>
	 */
	public abstract Element getRootElement();

	/**
	 * Returns the root record of the ChoiceMaker schema;
	 * e.g. the toplevel <code>nodeType</code> in the schema
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

	public abstract void error(String message);

	public abstract void warning(String message);

	public abstract boolean hasErrors();

	/**
	 * Generate file containing ClueMaker code for a clue set.
	 */
	public abstract void generate();

}
