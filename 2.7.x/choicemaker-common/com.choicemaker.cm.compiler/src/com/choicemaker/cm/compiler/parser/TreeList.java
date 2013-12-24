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
package com.choicemaker.cm.compiler.parser;

import com.choicemaker.cm.compiler.Tree;
import com.choicemaker.cm.core.compiler.CompilerException;

/**
 * class for incrementally assembling tree arrays
 *
 * @author   Matthias Zenger
 * @version  $Revision: 1.1.1.1 $ $Date: 2009/05/03 16:02:36 $
 */
public final class TreeList {

	/** the tree list
	 */
	private Tree[] trees = new Tree[4];

	/** the length of the list
	 */
	private int len = 0;

	/** append a tree to the list
	 */
	public void append(Tree tree) {
		if (len == trees.length) {
			Tree[] ts = new Tree[len * 2];
			System.arraycopy(trees, 0, ts, 0, len);
			trees = ts;
		}
		trees[len++] = tree;
	}

	/** append a tree array to the list
	 */
	public void append(Tree[] ts) {
		for (int j = 0; j < ts.length; j++)
			append(ts[j]);
	}

	/** remove the last n entries
	 */
	public Tree remove(int n) {
		return trees[len -= n];
	}

	/** return the current length
	 */
	public int length() {
		return len;
	}

	/** access i-th entry
	 */
	public Tree get(int i) {
		return trees[i];
	}

	/** apply a visitor to all elements of this list
	 */
	public void apply(Tree.Visitor v) throws CompilerException {
		for (int i = 0; i < len; i++)
			trees[i].apply(v);
	}
	
	/** convert tree list to array
	 */
	public Tree[] toArray() {
		Tree[] ts = new Tree[len];
		System.arraycopy(trees, 0, ts, 0, len);
		return ts;
	}

	/** convert tree list to array
	 */
	public Tree[] toArray(Tree[] ts) {
		System.arraycopy(trees, 0, ts, 0, len);
		return ts;
	}
}
