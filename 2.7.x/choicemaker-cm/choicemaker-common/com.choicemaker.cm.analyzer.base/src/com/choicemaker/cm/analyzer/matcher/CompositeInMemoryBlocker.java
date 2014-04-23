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

import java.io.IOException;
import java.util.List;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;

/**
 * Description
 *
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:47:57 $
 */
public class CompositeInMemoryBlocker implements InMemoryBlocker {
//	private static Logger logger = Logger.getLogger(InMemoryBlocker.class);

	private InMemoryBlocker[] constituents;

	public CompositeInMemoryBlocker(InMemoryBlocker[] constituents) {
		this.constituents = constituents;
	}

	public void init(List records) {
		for (int i = 0; i < constituents.length; i++) {
			constituents[i].init(records);
		}
	}

	public void clear() {
		for (int i = 0; i < constituents.length; i++) {
			constituents[i].clear();
		}
	}

	/**
	 * @see com.choicemaker.cm.train.matcher.InMemoryBlocker#block(com.choicemaker.cm.core.Record)
	 */
	public RecordSource block(Record q) {
		RecordSource[] is = new RecordSource[constituents.length];
		for (int i = 0; i < constituents.length; ++i) {
			is[i] = constituents[i].block(q);
		}
		return new FastCompositeRecordSource(is);
	}

	/**
	 * @see com.choicemaker.cm.train.matcher.InMemoryBlocker#block(com.choicemaker.cm.core.Record, int)
	 */
	public RecordSource block(Record q, int start) {
		RecordSource[] is = new RecordSource[constituents.length];
		for (int i = 0; i < constituents.length; ++i) {
			is[i] = constituents[i].block(q, start);
		}
		return new FastCompositeRecordSource(is);
	}

	private static class FastCompositeRecordSource implements RecordSource {
		private RecordSource[] constituents;
		private int curIdx;
		private RecordSource curIter;

		FastCompositeRecordSource(RecordSource[] constituents) {
			this.constituents = constituents;
		}

		public boolean hasNext() throws IOException {
			return curIter != null && curIter.hasNext();
		}
		public Record getNext() throws IOException {
			Record res = curIter.getNext();
			advance();
			return res;
		}
		private void advance() throws IOException {
			while(!curIter.hasNext()) {
				curIter.close();
				++curIdx;
				if(curIdx < constituents.length) {
					curIter = constituents[curIdx];
					curIter.open();
				} else {
					curIter = null;
					return;
				}
			}
		}
		public void open() throws IOException {
			curIdx = 0;
			if(constituents.length > 0) {
				curIter = constituents[0];
				curIter.open();
				advance();
			}
		}
		public void close() throws IOException {
			if(curIter != null) {
				curIter.close();
			}
			curIter = null;
		}
		public String getName() {
			return null;
		}
		public void setName(String name) {
		}
		public ImmutableProbabilityModel getModel() {
			return null;
		}
		public void setModel(ImmutableProbabilityModel m) {
		}
		public boolean hasSink() {
			return false;
		}
		public Sink getSink() {
			return null;
		}
		public String getFileName() {
			return null;
		}
	}
}
