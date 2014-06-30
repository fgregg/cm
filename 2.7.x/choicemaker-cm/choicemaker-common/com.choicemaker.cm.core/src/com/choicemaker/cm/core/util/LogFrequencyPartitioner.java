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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.choicemaker.cm.core.base.Constants;

/**
 * Description
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/27 21:19:22 $
 */
public class LogFrequencyPartitioner {

	public static void main(String[] args) throws IOException {
		LogFrequencyPartitioner lfp = new LogFrequencyPartitioner();
		lfp.readFile(args[0]);
		lfp.computeBoundaries(Integer.parseInt(args[2]));
		lfp.writeFile(args[1]);
	}

	private int minFrequency;
	private int maxFrequency;
	private List pairs;
	private int[] boundary;

	public LogFrequencyPartitioner() {
		pairs = new ArrayList();
		minFrequency = Integer.MAX_VALUE;
		maxFrequency = Integer.MIN_VALUE;
	}

	public void addPair(String value, int count) {
		if (count > maxFrequency) {
			maxFrequency = count;
		}
		if (count < minFrequency && count >= 1) {
			minFrequency = count;
		}
		pairs.add(new Pair(value, count));
	}

	public void readFile(String fileName) throws IOException {
		FileReader fr = new FileReader(new File(fileName).getAbsoluteFile());
		BufferedReader in = new BufferedReader(fr);
		try {
			while (in.ready()) {
				String val = in.readLine().trim();
				int count = Integer.parseInt(in.readLine().trim());
				addPair(val, count);
			}
		} catch (NumberFormatException ex) {
			System.out.println (ex.toString());
		}
		in.close();
		fr.close();
	}

	public void computeBoundaries(int numPartitions) {
		boundary = new int[numPartitions];
		double f = Math.pow(((double) maxFrequency) / minFrequency, 1.00d / numPartitions);
		double b = maxFrequency;
		for (int i = numPartitions - 1; i >= 0; --i) {
			boundary[i] = (int)(b + 0.5);
			//System.out.println(boundary[i]);
			b = b / f;
		}
	}

	public int getFrequencyClass(int c) {
		int i = 0;
		while (c > boundary[i]) {
			++i;
		}
		return i;
	}

	public void writeFile(String fileName) throws IOException {
		writeFile(fileName, Constants.LINE_SEPARATOR, Constants.LINE_SEPARATOR);
	}

	public void writeFile(String fileName, String elementSep, String lineSep) throws IOException {
		FileOutputStream fs = new FileOutputStream(fileName);
		Writer w = new OutputStreamWriter(new BufferedOutputStream(fs));
		Iterator i = pairs.iterator();
		while (i.hasNext()) {
			Pair p = (Pair) i.next();
			w.write(p.val + elementSep);
			w.write(getFrequencyClass(p.count) + lineSep);
		}
		w.flush();
		w.close();
		fs.close();
	}
	
	private static class Pair {
		final String val;
		final int count;
		Pair(String val, int count) {
			this.val = val;
			this.count = count;
		}
	}


}
