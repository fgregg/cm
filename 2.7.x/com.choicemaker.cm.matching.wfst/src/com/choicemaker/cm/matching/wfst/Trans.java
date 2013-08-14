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

class Trans implements Cloneable {

    public int from, to;
    public String in, out;
    public int weight;
    //    public double weight;

    public Trans(int f, int t, String i, String o, int w) {
	from = f;
	to = t;
	in = i;
	out = o;
	weight = w;
    }

    public Trans(int f, int t, String i, String n) {
	this(f, t, i, n, 0);
    }
    
    public Object clone() {
    	String i = new String(this.in);
    	String o = new String(this.out);
    	return new Trans(this.from,this.to,i,o,this.weight);
    }

    public String toString() {
	return "" + " " + from + " " + to + " " + in + " " + out + " " + weight;
    }

}

