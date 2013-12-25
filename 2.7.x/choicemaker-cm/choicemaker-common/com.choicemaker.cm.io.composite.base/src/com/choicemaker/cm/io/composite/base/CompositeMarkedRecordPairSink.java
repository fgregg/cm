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
package com.choicemaker.cm.io.composite.base;

import java.io.IOException;

import com.choicemaker.cm.core.*;

/**
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/28 08:56:16 $
 */
public class CompositeMarkedRecordPairSink implements MarkedRecordPairSink {
	private String name;
	private ImmutableProbabilityModel model;
	private MarkedRecordPairSource[] constituents;
	private int[] constituentSizes;
	private MarkedRecordPairSink curSink;
	private int curSource;
	private int curIdx;

	public CompositeMarkedRecordPairSink(CompositeMarkedRecordPairSource src) {
		name = src.getName();
		model = src.getModel();
		constituents = src.getConstituents();
		constituentSizes = src.getSizes();
	}

	public void open() throws IOException {
		curSource = 0;
		curIdx = 0;
		curSink = (MarkedRecordPairSink) constituents[curSource].getSink();
		curSink.open();
	}

	public void close() throws IOException {
		curSink.close();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
		for (int i = 0; i < constituents.length; ++i) {
			constituents[i].setModel(m);
		}
	}

	public void put(ImmutableRecordPair r) throws IOException {
		putMarkedRecordPair((ImmutableMarkedRecordPair) r);
	}

	public void putMarkedRecordPair(ImmutableMarkedRecordPair r) throws IOException {
		while (curIdx == constituentSizes[curSource]) {
			curSink.close();
			++curSource;
			curIdx = 0;
			curSink = (MarkedRecordPairSink) constituents[curSource].getSink();
			curSink.open();
		}
		curSink.putMarkedRecordPair(r);
		++curIdx;
	}

	/**
	 * NOP for now
	 * @see com.choicemaker.cm.Sink#flush()
	 */
	public void flush() throws IOException {
	}
		
}
