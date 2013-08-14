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

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Filter implements Cloneable {

	private char blank, delim;
	private String epsilon;
	private State[] states;
	private State initialState;
	private String leftBracket, rightBracket;
	
	static public Filter readFilter(BufferedReader reader) {
		Filter filter = null;
		String line = null;
		StringTokenizer st = null;
		try {
			line = reader.readLine();
			st = new StringTokenizer(line);
			int no = Integer.parseInt(st.nextToken());
			int start = Integer.parseInt(st.nextToken());
			char blank = (st.nextToken()).charAt(0);
			char delim = (st.nextToken()).charAt(0);

			line = reader.readLine();
			st = new StringTokenizer(line);
			String epsilon = st.nextToken();
			String leftBracket = st.nextToken();
			String rightBracket = st.nextToken();

			State[] states = new State[no];
			for (int i = 0; i < states.length; ++i) {
				states[i] = new State(i);
			}
			states[start] = new State(true, false, start);

			line = reader.readLine();
			st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				int end = Integer.parseInt(st.nextToken());
				states[end] = new State(false, true, end);
			}

			line = reader.readLine();
			while (line != null) {
				st = new StringTokenizer(line);
				int from = Integer.parseInt(st.nextToken());
				int to = Integer.parseInt(st.nextToken());
				String in = st.nextToken();
				String out = st.nextToken();
				int weight =
					st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
				if (out.equals(epsilon)) {
					out = "";
				}
				states[from].addTrans(new Trans(from, to, in, out, weight));
				line = reader.readLine();
			}
			filter =
				new Filter(
					states,
					states[0],
					blank,
					delim,
					epsilon,
					leftBracket,
					rightBracket);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return filter;
	}

	public Filter(
		State[] s,
		State i,
		char b,
		char d,
		String e,
		String l,
		String r) {
		this.states = s;
		this.initialState = i;
		this.blank = b;
		this.delim = d;
		this.epsilon = e;
		this.leftBracket = l;
		this.rightBracket = r;
	}
	
	public Object clone() {
		char b = this.blank;
		char d = this.delim;
		String e = new String(this.epsilon);
		State[] sts = null;
		if (this.states != null) {
			sts = new State[this.states.length];
			for (int i=0; i<this.states.length; i++) {
				if (this.states[i] != null) {
					sts[i] = (State) this.states[i].clone();
				}
			}
		}
		State iS = null;
		if (this.initialState != null) {
			iS = (State) this.initialState.clone();
		}
		String lb = new String(leftBracket);
		String rb = new String(rightBracket);
		
		return new Filter(sts,iS,b,d,e,lb,rb);
	}

	public char getBlank() {
		return blank;
	}
	public char getDelim() {
		return delim;
	}
	public String getLeftBracket() {
		return leftBracket;
	}
	public String getRightBracket() {
		return rightBracket;
	}
	public String getEpsilon() {
		return epsilon;
	}

	public LinkedList filter(List inlist) {
		LinkedList outlist = new LinkedList();
		Iterator iter = inlist.iterator();
		while (iter.hasNext()) {
			String str = (String) (iter.next());
			int weight = ((Integer) (iter.next())).intValue();
			outlist.addAll(filter(str, weight));
		}
		return outlist;
	}

	public LinkedList filter(String inStr) {
		return filter(inStr, 0);
	}

	public LinkedList filter(String inStr, int weight) {
		String str = "";
		if (inStr != null) {
			str = inStr;
		}
		str = str.replace(' ', blank);
		LinkedList recs = filterInitial(0);
		while (!emptyString(str)) {
			recs = filter1(headString(str), recs);
			str = tailString(str);
		}
		return recs2strings(filterFinal(recs));
	}

	private LinkedList filterInitial(int weight) {
		LinkedList outrecs = new LinkedList();
		LinkedList outs = new LinkedList();
		Out out = new Out(new LinkedList(), weight);
		outs.add(out);
		outrecs.addLast(new Rec(initialState.no, outs));
		return outrecs;
	}

	private LinkedList filterFinal(LinkedList recs) {
		Iterator inrecs = recs.iterator();
		recs = new LinkedList();
		while (inrecs.hasNext()) {
			Rec rec = (Rec) inrecs.next();
			if (finalRec(rec)) {
				recs.addLast(rec);
			}
		}
		return recs;
	}

	private boolean finalRec(Rec rec) {
		return states[rec.no].end;
	}

	////////////////////////////////////////////////////////////////////

	private LinkedList filter1(String str, LinkedList recs) {
		if (str.equals(Character.toString(delim))) {
			return recs;
		}
		Iterator inrecs = recs.iterator();
		recs = new LinkedList();
		while (inrecs.hasNext()) {
			Rec rec = (Rec) inrecs.next();
			recs = mergeRecs(recs, filter2(str, rec));
		}
		return recs;
	}

	private LinkedList filter2(String str, Rec rec) {
		State from = states[rec.no];
		Iterator outs = (rec.outs).iterator();
		LinkedList recs = new LinkedList();
		while (outs.hasNext()) {
			Out out = (Out) outs.next();
			recs = mergeRecs(recs, filter3(str, from, out));
		}
		return recs;
	}

	private LinkedList filter3(String str, State from, Out out) {
		LinkedList recs = new LinkedList();
		Iterator transes = from.transit(str);
		if (transes == null) {
			return recs;
		}
		while (transes.hasNext()) {
			LinkedList list = (LinkedList) (out.list).clone();
			Trans trans = (Trans) transes.next();
			list.addLast(trans.out);
			Out out1 = new Out(list, out.weight + trans.weight);
			LinkedList outs = new LinkedList();
			outs.add(out1);
			Rec rec = new Rec(trans.to, outs);
			LinkedList recs1 = new LinkedList();
			recs1.addLast(rec);
			recs = mergeRecs(recs, recs1);
		}
		return recs;
	}

	////////////////////////////////////////////////////////////////////

	private LinkedList mergeRecs(LinkedList recs1, LinkedList recs2) {
		LinkedList recs = new LinkedList();
		while (true) {
			if (recs1.isEmpty()) {
				recs.addAll(recs2);
				return recs;
			}
			if (recs2.isEmpty()) {
				recs.addAll(recs1);
				return recs;
			}
			while (!recs1.isEmpty() && !recs2.isEmpty()) {
				Rec rec1 = (Rec) recs1.getFirst();
				Rec rec2 = (Rec) recs2.getFirst();
				if (rec1.no < rec2.no) {
					recs.addLast(rec1);
					recs1.removeFirst();
				} else if (rec1.no > rec2.no) {
					recs.addLast(rec2);
					recs2.removeFirst();
				} else { // rec1.no == rec2.no
					LinkedList outs = new LinkedList();
					// Need to remove duplicate outs
					outs.addAll(rec1.outs);
					outs.addAll(rec2.outs);
					recs.addLast(new Rec(rec1.no, outs));
					recs1.removeFirst();
					recs2.removeFirst();
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////

	private LinkedList recs2strings(LinkedList recs) {
		LinkedList strings = new LinkedList();
		if (recs == null) {
			return strings;
		}
		Iterator outrecs = recs.iterator();
		while (outrecs.hasNext()) {
			Iterator outs = (((Rec) outrecs.next()).outs).iterator();
			while (outs.hasNext()) {
				Out out = (Out) outs.next();
				String str = detokenize(out.list);
				strings.addLast(str);
				strings.addLast(new Integer(out.weight));
			}
		}
		return strings;
	}

	private String detokenize(List list) {
		Iterator it = list.iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			String str = (String) it.next();
			if (!str.equals("")) {
				sb.append(delim).append(str);
			}
		}
		return sb.toString();
	}

	////////////////////////////////////////////////////////////////////

	private boolean emptyString(String str) {
		return str.equals("");
	}

	private String tailString(String str) {
		return str.substring(1);
	}

	private String headString(String str) {
		return str.substring(0, 1);
	}

}


