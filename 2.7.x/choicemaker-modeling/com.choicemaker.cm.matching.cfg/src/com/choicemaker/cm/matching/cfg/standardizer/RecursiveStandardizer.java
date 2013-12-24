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
package com.choicemaker.cm.matching.cfg.standardizer;

import java.util.*;

import com.choicemaker.cm.matching.cfg.*;

/**
 * The RecursiveStandardizer serves as a base class for the NameStandardizer
 * and AddressStandardizer classes.
 * 
 * The RecursiveStandardizer recursively walks the parse tree in a left-to-right,
 * depth-first fashion.  At each node, if this RecursiveStandardizer has
 * a ParseTreeNodeStandardizer registered to standardize nodes of the current type,
 * that Standardizer's standardize() method is invoked on the node.  Otherwise, 
 * this RecursiveStandardizer walks each child tree depth-first, in left-to-right order.
 * 
 * Note that if a RecursiveStandardizer has no registered child standardizers, or
 * if a RecursiveStandardizer doesn't have any child standizers for the types of
 * nodes in the parse tree, nothing will happen, and <code>standardize()</code>
 * will return with <code>holder</code> unchanged.
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 * @see com.choicemaker.cm.matching.en.us.address.AddressStandardizer
 * @see com.choicemaker.cm.matching.en.us.name.NameStandardizer
 */
public class RecursiveStandardizer implements ParseTreeNodeStandardizer {

	/** Map from Variables to child standardizers. */
	protected Map nodeStandardizers = new HashMap();
	
	/**
	 * Creates a new RecursiveStandardizer with no child standardizers.
	 */
	public RecursiveStandardizer() {
		// nothing to do...	
	}
	
	/**
	 * Registers <code>standardizer</code> as the ParseTreeNodeStandardizer
	 * to invoke when encountering a node whose rule's LHS is <code>symbol</code>.
	 * 
	 * @param variable the LHS of the rule of the node
	 * @param standardizer the newly-registered standardizer
	 */
	public void putStandardizer(Variable variable, ParseTreeNodeStandardizer standardizer) {
		nodeStandardizers.put(variable, standardizer);
	}
	
	/**
	 * Returns the standardizer for the specified Symbol
	 */
	public ParseTreeNodeStandardizer getStandardizer(Variable v) {
		return (ParseTreeNodeStandardizer) nodeStandardizers.get(v);	
	}

	/**
	 * Begin the recursive descent down the parse tree rooted at <code>node</code>,
	 * calling a registered Standardizer when appropriate, and storing all 
	 * standardized data in <code>holder</code>.
	 */
	public void standardize(ParseTreeNode node, ParsedData holder) {
		Variable v = node.getRule().getLhs();
		if (v == null) {
			throw new IllegalStateException();
		}
		
		ParseTreeNodeStandardizer ns = getStandardizer(v);
		if (ns != null) {
			ns.standardize(node, holder);
		} else {
			int numKids = node.getNumChildren();
			for (int i = 0; i < numKids; i++) {
				standardize(node.getChild(i), holder);		
			}
		}
	}

}
