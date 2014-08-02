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
 * Represents a symbol in a context-free grammar.  Symbols 
 * include Variables, which can be used in the grammar's rules,
 * and Tokens, which represent the actual pieces of the name 
 * or address to be parsed.  Note that only the Variable and Token
 * classes may subclass Symbol directly.  Classes that violate 
 * this may cause unexpected behavior.
 * 
 * Some Variables are also TokenTypes, each of which corresponds
 * to a particular type of Token, e.g. numbers, letters, street
 * suffixes, etc.
 * 
 * For this implementation of CFG's, Tokens never appear in Rules
 * added directly to the grammar.  However, they may be included
 * in special "leaf" (or terminal) Rules of the form
 * 
 *     TokenType --> Token
 *  
 * in which a TokenType goes to *exactly one* Token.  This is also
 * the only form of rule for which TokenTypes are allowed to be 
 * on the left-hand side.
 * 
 * Since Tokens may not appear in rules that are added to grammars,
 * TokenType Variables may not appear on the left-hand side of 
 * Rules in a CFG.
 * 
 * @author   Adam Winkel
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:59 $
 * @see Variable
 * @see TokenType
 * @see Token
 * @see Rule
 * @see ContextFreeGrammar
 */
public abstract class Symbol {

	/**
	 * The String representation of this Symbol.
	 */
	protected String name;

	/**
	 * Creates a new Symbol with the specified display string.
	 * @param name the string that will represent this <code>Symbol</code>
	 * in <code>String</code> representations of rules, grammars, and parse
	 * trees.
	 */
	public Symbol(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}

		this.name = name;	
	}

	/**
	 * Returns a <code>String</code> representation of this object.
	 * @return a <code>String</code> representation of this object.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns true iff <code>obj</code> is equal to this <code>Symbol</code>
	 * This is abstract because equality requirements for subclasses of
	 * <code>Symbol</code> may differ.  For example, we do not want the 
	 * symbol &quot;WD&quot; to be equal to the token &quot;WD&quot;.
	 * 
	 * @param obj the object for comparison
	 * @return true iff <code>obj</code> is equal to this <code>Symbol</code>
	 */
	public abstract boolean equals(Object obj);
	
	/**
	 * Returns the hash code for this object.  Note that this implementation
	 * simply returns <code>display.hashCode()</code>.  Subclasses may wish
	 * to override this default behavior for better hashing performance.
	 * @return the hash code for this <code>Symbol</code>
	 */
	public int hashCode() {
		return name.hashCode();
	}

}
