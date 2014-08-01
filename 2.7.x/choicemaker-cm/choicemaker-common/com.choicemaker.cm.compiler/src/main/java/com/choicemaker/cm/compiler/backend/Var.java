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

import com.choicemaker.cm.compiler.Location;
import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.compiler.Tree.ClueDecl;
import com.choicemaker.cm.compiler.Tree.Ident;
import com.choicemaker.cm.compiler.Tree.Index;
import com.choicemaker.cm.compiler.Tree.Let;
import com.choicemaker.cm.compiler.Tree.Quantified;
import com.choicemaker.cm.compiler.Tree.VarDecl;

/**
 * Helper class for Translator25.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
class Var {
	private static Tree INT_IDENT = new Ident(Location.NOPOS, "int");
	private static Var[] zeroLengthArray = new Var[0];
	public Tree tpe;
	public String name;

	public Var(Tree tpe, String name) {
		this.tpe = tpe;
		this.name = name;
	}

	public static Var[] create(ClueDecl c) {
		Index[] indices = c.indices;
		if (indices == null) {
			return zeroLengthArray;
		}
		Var[] r = new Var[indices.length];
		for (int i = 0; i < indices.length; ++i) {
			Index idx = indices[i];
			r[i] = new Var(idx.tpe, idx.name);
		}
		return r;
	}

	public static Var[] create(Let l) {
		VarDecl[] binders = l.binders;
		Var[] r = new Var[binders.length];
		for (int i = 0; i < binders.length; ++i) {
			VarDecl v = binders[i];
			r[i] = new Var(v.tpe, v.name);
		}
		return r;
	}

	public static Var[] create(Quantified q) {
		String[] vars = q.vars;
		Var[] r = new Var[vars.length];
		for (int i = 0; i < vars.length; ++i) {
			r[i] = new Var(INT_IDENT, vars[i]);
		}
		return r;
	}
}
