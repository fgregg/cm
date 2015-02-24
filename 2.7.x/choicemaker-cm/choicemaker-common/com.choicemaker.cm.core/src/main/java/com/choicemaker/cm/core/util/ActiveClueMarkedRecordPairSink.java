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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Arrays;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.BooleanActiveClues;

/**
 * @author ajwinkel
 *
 */
public class ActiveClueMarkedRecordPairSink implements MarkedRecordPairSink {

	public static final int AC_NONE = 901;
	public static final int AC_BIT_VECTOR = 902;
	public static final int AC_CLUE_INDICES = 903;

	private static final DecimalFormat df = new DecimalFormat("##0.00");

	private File file;
	private ImmutableProbabilityModel model;
	private boolean exportRecordIds;
	private boolean exportProbability;
	private boolean exportDecision;
	private int exportActiveCluesPolicy;
	private char delim;

	private Writer writer;
	private int numClues;
	private int count; // count of the number of items on a given line
	
	public ActiveClueMarkedRecordPairSink(
		File file, 
		ImmutableProbabilityModel model,
		boolean exportRecordIds,
		boolean exportProbability,
		boolean exportDecision,
		int     exportActiveCluesPolicy,
		char	delim) {
		
		this.file = file;
		this.model = model;
		this.exportRecordIds = exportRecordIds;
		this.exportProbability = exportProbability;
		this.exportDecision = exportDecision;
		this.exportActiveCluesPolicy = exportActiveCluesPolicy;
		this.delim = delim;
	}

	public void open() throws IOException {
		writer = new BufferedWriter(new FileWriter(file));
		
		numClues = -1;
		if (exportActiveCluesPolicy == AC_BIT_VECTOR) {
			numClues = model.getAccessor().getClueSet().size();
		}
	}
	
	public void putMarkedRecordPair(ImmutableMarkedRecordPair mrp) throws IOException {
		put(mrp);
	}
	
	public void put(ImmutableRecordPair rp) throws IOException {		
		count = 0;
		
		if (exportRecordIds) {
			write(rp.getQueryRecord().getId().toString());
			write(rp.getMatchRecord().getId().toString());
		}
		if (exportProbability) {
			write(df.format(rp.getProbability()));
		}
		if (exportDecision) {
			write(rp.getCmDecision().toString());
		}
		if (exportActiveCluesPolicy == AC_BIT_VECTOR) {
			writeACBitVector(rp.getActiveClues());
		} else if (exportActiveCluesPolicy == AC_CLUE_INDICES) {
			writeACClueIndices(rp.getActiveClues());
		}
		
		writer.write(Constants.LINE_SEPARATOR);		
	}

	private void write(String value) throws IOException {
		if (count++ > 0) {
			writer.write(delim);
		}
		writer.write(value);
	}
	
	private void writeACBitVector(ActiveClues ac) throws IOException {
		BooleanActiveClues bac = (BooleanActiveClues)ac;
		if (bac.size() > 0 || bac.sizeRules() > 0) {
			for (int i = 0; i < numClues; i++) {
				if (count++ > 0) {
					writer.write(delim);
				}
				if (bac.containsClueOrRule(i)) {
					writer.write("1");
				} else {
					writer.write("0");
				}
			}
		}
	}
	
	private void writeACClueIndices(ActiveClues ac) throws IOException {
		BooleanActiveClues bac = (BooleanActiveClues)ac;
		int[] clues = bac.getCluesAndRules();
		Arrays.sort(clues);
		for (int i = 0; i < clues.length; i++) {
			if (count++ > 0) {
				writer.write(delim);
			}
			writer.write(String.valueOf(clues[i]));
		}
	}

	public void close() throws IOException {
		writer.close();
		
		writer = null;
		numClues = -1;
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
	}

	public ImmutableProbabilityModel getModel() {
		return model; 
	}

	public String getName() { throw new UnsupportedOperationException(); }
	public void setName(String name) { throw new UnsupportedOperationException(); }

	/**
	 * NOP for now
	 * @see com.choicemaker.cm.Sink#flush()
	 */
	public void flush() throws IOException {
	}
		
}
