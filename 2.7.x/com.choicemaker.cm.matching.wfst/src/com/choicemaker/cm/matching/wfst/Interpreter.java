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
package com.choicemaker.cm.matching.wfst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

public class Interpreter {

    private char blank, delim;
    private String leftBracket, rightBracket;

    public Interpreter(Filter filter) {
	blank = filter.getBlank();
	delim = filter.getDelim();
	leftBracket = filter.getLeftBracket();
	rightBracket = filter.getRightBracket();
    }

    public LinkedList beststrings(List instrings) {
	LinkedList outstrings = new LinkedList();
	if (instrings == null || instrings.isEmpty()) { return outstrings; }
        int minWeight = Integer.MAX_VALUE;
	Iterator iter = instrings.iterator();
	while (iter.hasNext()) {
	    String str = (String)iter.next();
	    int weight = ((Integer)iter.next()).intValue();
            minWeight = Math.min(minWeight, weight);
	}
	iter = instrings.iterator();
	while (iter.hasNext()) {
	    String str = (String)iter.next();
	    Integer weight = (Integer)iter.next();
            if (weight.intValue() <= minWeight) {
		outstrings.add(str);
		outstrings.add(weight);
	    }
	}
	return outstrings;
    }

    public LinkedList interpret(List strings) {
	LinkedList sems = new LinkedList();
	if (strings == null) { return sems; }
	Iterator iter = strings.iterator();
	while (iter.hasNext()) {
	    String str = (String)iter.next();
	    String weight = ((Integer)iter.next()).toString();
        HashMap sem = list2sem(retokenize(str));
	    sem.put("weight", weight);
	    sems.add(sem);
	}
	return sems;
    }

    private LinkedList retokenize(String str) {
	LinkedList out = new LinkedList();
	StringTokenizer st =
	    new StringTokenizer(str.replace(blank, ' '), Character.toString(delim));
	while (st.hasMoreTokens()) {
	    String tok = st.nextToken();
	    out.add(tok);
	}
	return out;
    }

    private HashMap list2sem(List list) {
		HashMap sem = new HashMap();
    	if (list == null) {
    		return sem;
    	} else {
			Stack stack = new Stack();
			Iterator it = list.iterator();
			StringBuffer sb = new StringBuffer();
			while (it.hasNext()) {
				String instr = (String)it.next();
				if (instr.equals(leftBracket)) {
					stack.push(sb.toString());
					sb.setLength(0);
				} else if (instr.equals(rightBracket)) {
					String key = (String)it.next();
					sem.put(key, sb.toString());
					sb.insert(0,(String)stack.pop());
				} else {
					sb.append(instr);
				}
			}
			return sem;
    	}
    }

}

