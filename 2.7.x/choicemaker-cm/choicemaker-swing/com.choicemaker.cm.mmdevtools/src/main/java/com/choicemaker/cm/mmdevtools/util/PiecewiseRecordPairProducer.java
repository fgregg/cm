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
package com.choicemaker.cm.mmdevtools.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordPairSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.MutableRecordPair;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;

/**
 * Performs piecewise blocking on the specified array of RecordSources and puts each pair to the 
 * specified RecordPairSink.
 * 
 * A follow-on task is to (in a piecewise fashion) block for two different RecordSources.
 */
public class PiecewiseRecordPairProducer implements Runnable {

	private static Logger logger = Logger.getLogger(PiecewiseRecordPairProducer.class);

	private RecordSource[] sources;
	private InMemoryBlocker blocker;
	private ImmutableProbabilityModel model;
	private RecordPairSink sink;
	
	private long numRead;
	private long numPairsProduced;

	public PiecewiseRecordPairProducer(RecordSource[] sources, InMemoryBlocker blocker, ImmutableProbabilityModel model, RecordPairSink sink) {
		this.sources = sources;
		this.model = model;
		for (int i = 0; i < sources.length; i++) {
			this.sources[i].setModel(this.model);
		}

		this.blocker = blocker;
		
		this.sink = sink;
		sink.setModel(model);
	}
	
	public long getNumRead() {
		return numRead;
	}
	
	public long getNumPairsProduced() {
		return numPairsProduced;
	}

	public void run() {
		numRead = 0;
		numPairsProduced = 0;

		try {
			sink.open();

			List records = new ArrayList();
			for (int i = 0; i < sources.length; i++) {
				logger.info("Reading in " + sources[i].getName());
				readRecordSource(sources[i], records);
			
				logger.info("Indexing " + sources[i].getName());			
				blocker.init(records);
			
				logger.info("Matching " + sources[i].getName() + " against itself.");
				block(records);
			
				for (int j = i+1; j < sources.length; j++) {
					logger.info("Matching " + sources[i].getName() + " against " + sources[j].getName());
					block(sources[j]);
				}
			
				blocker.clear();
				records.clear();
			}
		} catch (IOException ex) {
			// not sure of a better way to handle this...
			ex.printStackTrace();
		} finally {
			if (sink != null) {
				try {
					sink.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Read all the records from the specified record source, and and place them in the specified list.
	 */
	private void readRecordSource(RecordSource rs, List list) throws IOException {
		try {
			rs.open();
			while (rs.hasNext()) {
				Record r = rs.getNext();
				list.add(r);
				numRead++;
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Block the records for currently-indexed Records.  In this case, 
	 * the in-memory blocker allows us to only consider records with a higher index
	 * in the List.  This avoid returning both <1, 2>, and <2, 1>
	 */
	private void block(List records) throws IOException {
		for (int i = 0, n = records.size(); i < n; i++) {
			Record q = (Record) records.get(i);
			
			RecordSource rsBlocked = blocker.block(q, i+1);
			block(q, rsBlocked);
		}
	}
	
	/**
	 * Block each record of the specified RecordSource against the currently-indexed Records.
	 */
	private void block(RecordSource rs) throws IOException {
		try {
			rs.open();
			while (rs.hasNext()) {
				Record q = rs.getNext();
				numRead++;
				
				RecordSource blocked = blocker.block(q);
				block(q, blocked);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void block(Record q, RecordSource blocked) throws IOException {
		Object qId = q.getId();

		try {
			blocked.open();
			while (blocked.hasNext()) {
				Record m = blocked.getNext();
				// this will break if ID's are null, and in some cases, we may not even want to do this...
				if (qId.equals(m.getId())) {
					continue;
				}
				sink.put(new MutableRecordPair(q, m));
				numPairsProduced++;
			}
		} finally {
			if (blocked != null) {
				try {
					blocked.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
