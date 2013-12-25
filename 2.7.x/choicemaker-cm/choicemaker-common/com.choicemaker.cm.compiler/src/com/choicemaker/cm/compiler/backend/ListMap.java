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
package com.choicemaker.cm.compiler.backend;

import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Type;
import com.choicemaker.cm.compiler.Symbol.VarSymbol;

/**
 * Helper class for translating shorthands.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
class ListMap {
	public List keys;
	public List vars;
	public List bounds;

	public ListMap() {
		keys = new ArrayList();
		vars = new ArrayList();
		bounds = new ArrayList();
	}

	public void put(Tree key, Tree var, Tree bound) {
		keys.add(key);
		vars.add(var);
		bounds.add(bound);
	}

	public Tree getVar(Tree key) {
		int idx = keys.indexOf(key);
		if (idx == -1) {
			return null;
		} else {
			return getVar(idx);
		}
	}

	public Tree getBound(Tree key) {
		int idx = keys.indexOf(key);
		if (idx == -1) {
			return null;
		} else {
			return getBound(idx);
		}
	}

	public Tree getKey(int idx) {
		return (Tree) keys.get(idx);
	}

	public Tree getVar(int idx) {
		return (Tree) vars.get(idx);
	}

	public Tree getBound(int idx) {
		return (Tree) bounds.get(idx);
	}

	public int size() {
		return keys.size();
	}

	public String[] getVarNames() {
		String[] r = new String[size()];
		for (int i = 0; i < size(); ++i) {
			r[i] = getVar(i).toString();
		}
		return r;
	}

	public VarSymbol[] getBoundSymbols() {
		VarSymbol[] r = new VarSymbol[size()];
		for (int i = 0; i < size(); ++i) {
			r[i] = new VarSymbol(getVar(i).toString(), Type.INT, 0, null);
			r[i].range = getBound(i);
		}
		return r;
	}
}
