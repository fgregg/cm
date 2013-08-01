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
 * Scope of symbol table.
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:35 $
 */
public class Scope {

	/** the owner of the scope
	 */
	public Symbol owner;

	/** all elements of this scope linked via the symbol entry's next pointer
	 */
	public ScopeEntry elems;

	/** the number of entries
	 */
	private int size;

	/** the hash table
	 */
	private ScopeEntry[] hashtable;

	/** size and mask of hash tables
	 */
	private static final int SIZE = 0x0080;
	private static final int MASK = 0x007F;

	/** construct a new scope
	 */
	public Scope(Symbol owner) {
		this.owner = owner;
		this.elems = ScopeEntry.NONE;
		this.hashtable = new ScopeEntry[SIZE];
		for (int i = 0; i < SIZE; i++)
			hashtable[i] = ScopeEntry.NONE;
	}

	/** construct a new scope based on an existing one
	 */
	public Scope(Symbol owner, Scope base) {
		this.owner = owner;
		this.elems = ScopeEntry.NONE;
		this.hashtable = new ScopeEntry[SIZE];
		for (int i = 0; i < SIZE; i++)
			hashtable[i] = base.hashtable[i];
	}

	/** enter a symbol
	 */
	public void enter(Symbol sym) {
		int i = sym.hashCode() & MASK;
		hashtable[i] = new ScopeEntry(this, sym, hashtable[i]);
		size++;
	}

	/** enter a symbol if new
	 */
	public void enterIfNew(Symbol sym) {
		if (!found(sym)) {
			enter(sym);
			size++;
		}
	}

	/** lookup a symbol and return true if found
	 */
	public boolean found(Symbol sym) {
		ScopeEntry e = hashtable[sym.hashCode() & MASK];
		while (e != ScopeEntry.NONE)
			if (e.sym == sym)
				return true;
			else
				e = e.tail;
		return false;
	}

	/** lookup a symbol table entry by name
	 */
	public ScopeEntry lookupEntry(String name) {
		ScopeEntry e = hashtable[name.hashCode() & MASK];
		while (e != ScopeEntry.NONE)
			if (e.sym.getName() == name)
				return e;
			else
				e = e.tail;
		return ScopeEntry.NONE;
	}

	/** lookup a symbol table entry by name
	 */
	public ScopeEntry localLookupEntry(String name) {
		ScopeEntry e = hashtable[name.hashCode() & MASK];
		while ((e != ScopeEntry.NONE) && (e.scope == this))
			if (e.sym.getName() == name)
				return e;
			else
				e = e.tail;
		return ScopeEntry.NONE;
	}

	/** lookup a symbol by name
	 */
	public Symbol lookup(String name) {
		return lookupEntry(name).sym;
	}

	/** lookup a symbol by name
	 */
	public Symbol localLookup(String name) {
		return localLookupEntry(name).sym;
	}

	/** lookup a symbol table entry by name and kind
	 */
	public ScopeEntry lookupKindEntry(int kind, String name) {
		ScopeEntry e = hashtable[name.hashCode() & MASK];
		while (e != ScopeEntry.NONE)
			if ((e.sym.getName() == name) && (e.sym.getKind() == kind))
				return e;
			else
				e = e.tail;
		return ScopeEntry.NONE;
	}

	/** lookup a symbol by name
	 */
	public Symbol lookupKind(int kind, String name) {
		return lookupKindEntry(kind, name).sym;
	}

	/** return the number of local entries
	 */
	public int size() {
		return size;
	}
}
