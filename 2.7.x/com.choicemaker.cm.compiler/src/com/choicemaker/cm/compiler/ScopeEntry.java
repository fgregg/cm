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


/**
 * A class for representing symbol table entries
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.2 $ $Date: 2010/03/24 20:08:26 $
 */
public class ScopeEntry {

	/** the absent entry
	 */
	public static final ScopeEntry NONE = new ScopeEntry();

	/** the symbol of the entry
	 */
	public Symbol sym;

	/** the next entry in this scope
	 */
	public ScopeEntry next;

	/** the next entry in the hash bucket
	 */
	public ScopeEntry tail;

	/** the owner of this scope entry
	 */
	public Scope scope;

	/** the constructor
	 */
	ScopeEntry(Scope owner, Symbol sym, ScopeEntry tail) {
		this.sym = sym;
		this.tail = tail;
		this.scope = owner;
		next = owner.elems;
		owner.elems = this;
	}

	/** the constructor for the non-existent scope entry
	 */
	private ScopeEntry() {
		this.sym = Symbol.NONE;
	}

	/** get the next scope entry refering to the same name
	 */
	public ScopeEntry other() {
		if (tail == null)
			return this;
		ScopeEntry e = tail;
		while (e != NONE)
			if (e.sym.getName() == sym.getName())
				return e;
			else
				e = e.tail;
		return NONE;
	}

	/** get the next scope entry refering to the same name and
	 *  the given kind
	 */
	public ScopeEntry other(int kind) {
		if (tail == null)
			return this;
		ScopeEntry e = tail;
		while (e != NONE)
			if ((e.sym.getName() == sym.getName()) && (e.sym.getKind() == kind))
				return e;
			else
				e = e.tail;
		return NONE;
	}

	/** compute the hash code
	 */
	public int hashCode() {
		return sym.hashCode();
	}
	
	public boolean equals(Object o) {
		boolean b = false;
		if (o != null)
			if (o instanceof ScopeEntry) {
				ScopeEntry se = (ScopeEntry) o;
				b = se.sym.equals(this.sym);
			}
		return b;
	}

	/** return a string representation
	 */
	public String toString() {
		return sym.toString();
	}
}
