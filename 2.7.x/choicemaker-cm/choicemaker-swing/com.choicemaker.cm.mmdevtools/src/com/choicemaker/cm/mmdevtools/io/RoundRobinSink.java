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
package com.choicemaker.cm.mmdevtools.io;

import java.io.IOException;

import com.choicemaker.cm.core.base.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.base.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.ImmutableRecordPair;
import com.choicemaker.cm.core.base.MarkedRecordPairSink;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordSink;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.core.base.Sink;
import com.choicemaker.cm.core.base.SinkFactory;
import com.choicemaker.cm.core.base.Source;
import com.choicemaker.cm.core.xmlconf.RecordSourceXmlConf;
import com.choicemaker.cm.core.xmlconf.XmlConfException;
import com.choicemaker.cm.io.xml.base.XmlRecordSinkFactory;

public class RoundRobinSink implements RecordSink, MarkedRecordPairSink {

	protected SinkFactory factory;
	protected int distrib;
	protected int sinkSize;

	protected int[] sizes;
	protected Sink[] sinks;
	protected int curSink;

	public RoundRobinSink(SinkFactory factory, int distrib, int sinkSize) {
		this.factory = factory;
		this.distrib = distrib;
		this.sinkSize = sinkSize;
	}

	public void put(Record r) throws IOException {
		((RecordSink)sinks[curSink]).put(r);
		sizes[curSink]++;
		if (sizes[curSink] == sinkSize) {
			sinks[curSink].close();
			sinks[curSink] = factory.getSink();
			sinks[curSink].open();
			sizes[curSink] = 0;
		}
		curSink = (curSink + 1) % distrib;
	}

	public void putMarkedRecordPair(ImmutableMarkedRecordPair mrp) throws IOException {
		((MarkedRecordPairSink)sinks[curSink]).put(mrp);
		sizes[curSink]++;
		if (sizes[curSink] == sinkSize) {
			sinks[curSink].close();
			sinks[curSink] = factory.getSink();
			sinks[curSink].open();
			sizes[curSink] = 0;
		}
		curSink = (curSink + 1) % distrib;
	}

	public void put(ImmutableRecordPair rp) throws IOException {
		putMarkedRecordPair((ImmutableMarkedRecordPair)rp);
	}
	
	public void saveSourceDescriptors() throws XmlConfException {
		if (factory instanceof XmlRecordSinkFactory) {
			Source[] sources = ((XmlRecordSinkFactory)factory).getSources();
			for (int i = 0; i < sources.length; i++) {
				RecordSourceXmlConf.add((RecordSource)sources[i]);
			}
		}
	}

	public void open() throws IOException {
		sizes = new int[distrib];
		sinks = new Sink[distrib];
		for (int i = 0; i < distrib; i++) {
			sinks[i] = factory.getSink();
			sinks[i].open();
		}
		curSink = 0;
	}

	public void close() throws IOException {
		for (int i = 0; i < distrib; i++) {
			sinks[i].close();
		}
		sizes = null;
		sinks = null;
		curSink = -1;
	}
		
	public String getName() {
		throw new UnsupportedOperationException();
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public ImmutableProbabilityModel getModel() {
		throw new UnsupportedOperationException();
	}

	public void setModel(ImmutableProbabilityModel m) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * NOP for now
	 * @see com.choicemaker.cm.core.base.Sink#flush()
	 */
	public void flush() {
	}

}
