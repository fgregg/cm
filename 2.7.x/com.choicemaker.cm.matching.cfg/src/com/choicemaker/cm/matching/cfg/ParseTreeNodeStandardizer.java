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
package com.choicemaker.cm.matching.cfg;

/**
 * Base interface for standardizing parse trees, that is 
 * from a parse tree for an address (for example) pulling out the street name, suffix,
 * and direction, as well as house numbe, apartment type and number, city, state,
 * zip, and storing it in a manner that is more easily accessible, like
 * a ParsedDataHolder.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public interface ParseTreeNodeStandardizer {
	
	/**
	 * Pick out pieces of data from <code>node</code> and its children, if desired.
	 * Put the data into <code>holder</code> and return <code>holder</code>.
	 * 
	 * @param node the root of the parse tree to standardize
	 * @param holder the ParsedDataHolder in which to store the pieces
	 */
	public void standardize(ParseTreeNode node, ParsedData holder);
	
}
