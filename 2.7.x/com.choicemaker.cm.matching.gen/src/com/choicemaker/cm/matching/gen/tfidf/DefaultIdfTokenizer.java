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

import java.util.*;

/**
 * @author ajwinkel
 *
 */
public class DefaultIdfTokenizer implements Tokenizer {

	public String[] tokenize(String s) {
		List temp = new ArrayList();
		int len = s.length();
		StringBuffer buff = new StringBuffer(len);
		int index = 0;
		while (index < len) {
			char c = s.charAt(index);
			if (Character.isLetter(c) && c < 128) {
				buff.append(c);
			} else if (buff.length() > 0) {
				temp.add(buff.toString().intern());
				buff.setLength(0);
			}
			index++;
		}
		if (buff.length() > 0) {
			temp.add(buff.toString().intern());
		}
		
		len = temp.size();
		List chunks = new ArrayList();
		for (int i = 0; i < len; i++) {
			String w = (String) temp.get(i);
			if (w.length() > 1) {
				chunks.add(w);
			} else {
				buff.setLength(0);
				buff.append(w);
				do {
					if (++i == len) {
						i--;
						break;
					}
					
					w = (String) temp.get(i);
					if (w.length() == 1) {
						buff.append(w);
					} else {
						i--;
						break;
					}
				} while (true);
				
				chunks.add(buff.toString().intern());
			}
		}
		
		String[] ret = new String[chunks.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (String) chunks.get(i);
		}
		
		return ret;
	}

	public static String join(List strings) {
		if (strings.size() == 0) {
			return null;
		}
		
		StringBuffer b = new StringBuffer();
		b.append((String)strings.get(0));
		
		for (int i = 1; i < strings.size(); i++) {
			b.append(' ');
			b.append((String)strings.get(i));
		}
		
		return b.toString().intern();
	}

	public static int setOverlap(Set s1, Set s2) {
		return setOverlap(s1, s2, 3);
	}

	public static int setOverlap(Set s1, Set s2, int minLength) {
		int count = 0;
		Iterator itS1 = s1.iterator();
		while (itS1.hasNext()) {
			String s = (String) itS1.next();
			if (s2.contains(s) && s.length() >= minLength) {
				count++;
			}
		}
		return count;
	}

}
