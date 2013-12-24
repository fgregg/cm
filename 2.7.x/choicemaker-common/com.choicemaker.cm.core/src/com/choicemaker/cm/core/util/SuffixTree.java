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
package com.choicemaker.cm.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * .
 *
 * @author   Adam Winkel
 * @version  $Revision: 1.1 $ $Date: 2010/01/20 15:05:03 $
 */
public class SuffixTree {
	
	protected PrefixTree tree;

	public SuffixTree() {
		this.tree = new PrefixTree();
	}

	public SuffixTree(Collection strings) {
		this();
		addAll(strings);
	}
	
	public SuffixTree(String[] strings) {
		this();
		addAll(strings);	
	}

	public void addAll(Collection strings) {
		Iterator it = strings.iterator();
		while (it.hasNext()) {
			add((String)it.next());	
		}
	}

	public void addAll(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			add(strings[i]);
		}
	}
	
	public void add(String s) {
		tree.add(reverse(s));
	}
	
	public boolean contains(String s) {
		return tree.contains(reverse(s));	
	}
	
	public String getLongestSuffix(String s) {
		String prefix = tree.getLongestPrefix(reverse(s));
		
		if (prefix == null)
			return null;

		return reverse(prefix);		
	}

	public String getShortestSuffix(String s) {
		String prefix = tree.getLongestPrefix(reverse(s));
		
		if (prefix == null)
			return null;
		
		return reverse(prefix);
	}
	
	public List getAllSuffixes(String s) {
		List prefixes = tree.getAllPrefixes(reverse(s));
		
		List suffixes = new ArrayList();
		for (int i = 0; i < prefixes.size(); i++) {
			String prefix = (String)prefixes.get(i);
			suffixes.add(reverse(prefix));	
		}
		
		return suffixes;
	}
	
	private String reverse(String s) {
		return new StringBuffer(s).reverse().toString();	
	}
}
