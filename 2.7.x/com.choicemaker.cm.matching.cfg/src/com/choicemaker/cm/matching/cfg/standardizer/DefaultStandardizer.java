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

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.matching.cfg.*;

/**
 * The DefaultStandardizer class standardizes a parse tree (or a subtree of
 * a parse tree) by joining the tokens in a left-to-right, depth-first
 * order.  Spaces are inserted between tokens according to the specified
 * space-insertion policy.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 */
public class DefaultStandardizer implements ParseTreeNodeStandardizer {
	
	/** Insert spaces between all tokens */
	public static final int ALL = 1;
	
	/** 
	 * Insert spaces between tokens of the same type only.  
	 * This is the default. 
	 */
	public static final int LIKE_ONLY = 2;
	
	/** Never insert spaces between tokens. */
	public static final int NONE = 3;
	
	/** The field key in which to store the standardized data **/
	protected String fieldName;
	
	/** The current space insert policy */
	protected int spacePolicy;
	
	/**
	 * Creates a new DefaultStandardizer which stores its normalized data
	 * in the field identified by <code>fieldName</code>, and has the
	 * defuault space insert policy: <code>LIKE_ONLY</code>.
	 * 
	 * @param fieldName the key of the field in which to store normalized
	 * data.
	 */
	public DefaultStandardizer(String fieldName) {
		this(fieldName, LIKE_ONLY);
	}

	/**
	 * Creates a new DefaultStandardizer which stores its noramalized data
	 * in the field identified by <code>fieldName</code>, and has the 
	 * specified space insert policy.
	 * 
	 * @param fieldName the key of the field in which to store normalized data.
	 * @param spacePolicy the space insert policy to use.
	 */
	public DefaultStandardizer(String fieldName, int spacePolicy) {
		this.fieldName = fieldName;
		setSpaceInsertPolicy(spacePolicy);
	}
	
	/**
	 * Returns the space insert policy.
	 */
	public int getSpaceInsertPolicy() {
		return spacePolicy;
	}
	
	/**
	 * Sets the space insert policy.
	 */
	public void setSpaceInsertPolicy(int spacePolicy) {
		this.spacePolicy = spacePolicy;
	}

	/**
	 * Standardize <code>node</code>, and store the standardized data in 
	 * the <code>fieldName</code> field in ParsedDataHolder <code>holder</code>.
	 * 
	 * @param node the ParseTreeNode from which to begin standardizing
	 * @param holder the ParsedDataHolder in which to store standardized data
	 */	
	public void standardize(ParseTreeNode node, ParsedData holder) {
		holder.append(fieldName, joinTokens(node));
	}

	/**
	 * Joins the Tokens of the parse tree whose root is <code>node</code> in
	 * the manner prescribed by the current space insert policy.  The tokens
	 * are joined in order from left to right, by performing a depth-first
	 * search.
	 * 
	 * @param node the root of the ParseTree to start the join
	 */
	protected String joinTokens(ParseTreeNode node) {
		StringBuffer sBuff = new StringBuffer();
		joinTokens(node, sBuff, null);
		return sBuff.toString();
	}
	
	/**
	 * Helper method for <code>joinTokens(ParseTreeNode)</code>.
	 * 
	 * @see #joinTokens(ParseTreeNode)
	 */
	protected TokenType joinTokens(ParseTreeNode node, StringBuffer sBuff, TokenType last) {		
		if (node.isLeaf()) {
			Rule r = node.getRule();
			TokenType type = (TokenType) r.getLhs();
			Token tok = (Token) r.getRhsSymbol(0);

			if (last != null && (spacePolicy == ALL || (spacePolicy == LIKE_ONLY && sBuff.length() > 0 && type.equals(last)))) {
				sBuff.append(' ');
			}
			
			sBuff.append(type.getStandardToken(tok));
			return type;
		} else {
			int numKids = node.getNumChildren();		
			for (int i = 0; i < numKids; i++) {
				last = joinTokens(node.getChild(i), sBuff, last);
			}
			
			return last;
		}
	}
	
	/**
	 * Returns an L-to-R enumeration of the Tokens in the parse tree.
	 */
	protected List getTokens(ParseTreeNode node) {
		List list = new ArrayList();
		getTokens(node, list);
		return list;
	}

	/**
	 * Adds an L-to-R enumeration of <code>node</code>'s tokens to <code>list</code>.
	 */	
	protected void getTokens(ParseTreeNode node, List list) {
		if (node.isLeaf()) {
			Rule r = node.getRule();
			TokenType type = (TokenType) r.getLhs();
			Token tok = (Token) r.getRhsSymbol(0);
			list.add(type.getStandardToken(tok));
		} else {
			int numKids = node.getNumChildren();		
			for (int i = 0; i < numKids; i++) {
				getTokens(node.getChild(i), list);
			}
		}
	}

}
