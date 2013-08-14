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
package com.choicemaker.cm.matching.gen.tfidf;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author    Adam Winkel
 * @version   
 */
public class TokenList {

	protected List tokens = new ArrayList();

	public TokenList() { }

	public TokenList(String[] s) {
		addAll(s);
	}
	
	public void add(String s) {
		tokens.add(s);
	}
	
	public void addAll(String[] s) {
		for (int i = 0; i < s.length; i++) {
			tokens.add(s[i]);
		}
	}

	public int size() {
		return tokens.size();
	}

	public String get(int i) {
		return (String) tokens.get(i);
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append('[');
		if (size() > 0) {
			buff.append(get(0));
		}
		for (int i = 1; i < size(); i++) {
			buff.append(' ');
			buff.append(get(i));	
		}
		buff.append(']');
		return buff.toString();
	}

}
