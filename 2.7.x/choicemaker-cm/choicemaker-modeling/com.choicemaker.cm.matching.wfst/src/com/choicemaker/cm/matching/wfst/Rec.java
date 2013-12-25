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

import java.util.LinkedList;

class Rec {
    public int no;
    public LinkedList outs;

    public Rec(int n, LinkedList o) {
	no = n;
	outs = o;
    }

    public boolean equals(Rec rec) {
	return no == rec.no && outs.equals(rec.outs);
    }

    public String toString() {
	return "no=" + no + ", outs=" + outs;
    }

}

 
