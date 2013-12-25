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
package com.choicemaker.cm.analyzer.matcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import com.choicemaker.cm.core.Constants;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSink;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:48:14 $
 */
public class SimpleRecordSink implements MarkedRecordPairSink {
	private static DecimalFormat df = new DecimalFormat("##0.0000");
	
	private File file;
	private BufferedWriter w;
	private FileWriter fw;
	
	public SimpleRecordSink(File file) {
		this.file = file;
	}

	/**
	 * @see com.choicemaker.cm.core.MarkedRecordPairSink#putMarkedRecordPair(com.choicemaker.cm.core.MarkedRecordPair)
	 */
	public void putMarkedRecordPair(ImmutableMarkedRecordPair r) throws IOException {
		w.write(r.getQueryRecord().getId() + "," + r.getMatchRecord().getId() + "," + r.getMarkedDecision() + "," + df.format(r.getProbability()) + Constants.LINE_SEPARATOR);
	}

	/**
	 * @see com.choicemaker.cm.core.RecordPairSink#put(com.choicemaker.cm.core.RecordPair)
	 */
	public void put(ImmutableRecordPair r) throws IOException {
		putMarkedRecordPair((ImmutableMarkedRecordPair)r);
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#open()
	 */
	public void open() throws IOException {
		fw = new FileWriter(file);
		w = new BufferedWriter(fw);
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#close()
	 */
	public void close() throws IOException {
		w.flush();
		fw.flush();
		w.close();
		fw.close();
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#flush()
	 */
	public void flush() throws IOException {
		w.flush();
		fw.flush();
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#getName()
	 */
	public String getName() {
		return null;
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#setName(java.lang.String)
	 */
	public void setName(String name) {
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#getModel()
	 */
	public ImmutableProbabilityModel getModel() {
		return null;
	}

	/**
	 * @see com.choicemaker.cm.core.Sink#setModel(com.choicemaker.cm.core.ProbabilityModel)
	 */
	public void setModel(ImmutableProbabilityModel m) {
	}

}
