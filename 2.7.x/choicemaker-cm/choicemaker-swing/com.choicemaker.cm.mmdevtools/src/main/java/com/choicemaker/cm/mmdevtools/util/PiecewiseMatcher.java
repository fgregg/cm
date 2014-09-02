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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.logging.Logger;

import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.util.StringUtils;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:24:58 $
 */
public class PiecewiseMatcher {

	private static Logger logger = Logger.getLogger(PiecewiseMatcher.class.getName());

	public static final String INDEXED_SOURCE = "Indexed Source";
	public static final String Q_SOURCE = "Q Source";
	public static final String RECORDS_READ = "Records Read";
	public static final String RECORDS_TREATED_AS_SOURCE = "Records Treated as Souce";
	public static final String RECORDS_BLOCKED = "Total Records Blocked";
	public static final String MATCHES_CREATED = "Matches Created";

	public static final String DONE = "DONE";

	private RecordSource[] sources;
	private MarkedRecordPairSink sink;
	private InMemoryBlocker blocker;
	private IProbabilityModel probabilityModel;
	private float lowerThreshold;
	private float upperThreshold;
	private String user;
	private String src;
	private String comment;

	private Evaluator evaluator;
	private ClueSet clueSet;
	private boolean[] enabledClues;

	private PropertyChangeSupport propertyChangeListeners;
//	private int numRecordsFromSmall;
//	private int numRecordsFromLarge;
//	private int numPairs;
//	private boolean done;
//	private int maxNumMatchesPerSourceRecord;
	
	private Counter recordsRead;
	private Counter recordsTreatedAsSource;
	private Counter recordsBlocked;
	private Counter matches;

//	private DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);

	public PiecewiseMatcher() {
		propertyChangeListeners = new PropertyChangeSupport(this);
	}

	public PiecewiseMatcher(RecordSource[] sources, 
				   MarkedRecordPairSink sink, 
				   InMemoryBlocker blocker,
				   IProbabilityModel probabilityModel,
				   float lowerThreshold,
				   float upperThreshold,
				   String user,
				   String src,
				   String comment) {
		this();

		this.sources = sources;
		this.sink = sink;
		this.blocker = blocker;
		this.probabilityModel = probabilityModel;
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		this.user = user;
		this.src = src;
		this.comment = comment;
		
		evaluator = probabilityModel.getEvaluator();
		clueSet = probabilityModel.getClueSet();
		enabledClues = probabilityModel.getCluesToEvaluate();
		
		recordsRead = new Counter(RECORDS_READ, 1000);
		recordsTreatedAsSource = new Counter(RECORDS_TREATED_AS_SOURCE, 1000);
		recordsBlocked = new Counter(RECORDS_BLOCKED, 1000);
		matches = new Counter(MATCHES_CREATED, 100);
	}

	public void match() throws IOException {
				
		for (int i = 0; i < sources.length; i++) {
			sources[i].setModel(probabilityModel);
		}
		
		sink.setModel(probabilityModel);
		sink.open();

		List records = new ArrayList();
		for (int i = 0; i < sources.length; i++) {
			logger.info("Reading in " + sources[i].getName());
			readRecordSource(sources[i], records);
			
			logger.info("Indexing " + sources[i].getName());			
			blocker.init(records);
			
			logger.info("Matching " + sources[i].getName() + " against itself.");
			firePropertyChange(INDEXED_SOURCE, null, sources[i].getName());
			match(records);
			
			for (int j = i+1; j < sources.length; j++) {
				logger.info("Matching " + sources[i].getName() + " against " + sources[j].getName());
				firePropertyChange(Q_SOURCE, null, sources[j].getName());
				match(sources[j]);
			}
			
			blocker.clear();
			
			firePropertyChange(Q_SOURCE, null, "");
		}

		sink.close();
		
		recordsRead.update();
		recordsTreatedAsSource.update();
		recordsBlocked.update();
		matches.getCount();
		
		logger.info("Done.");
		logger.info("Records read:              " + StringUtils.padLeft("" + recordsRead.getCount(), 20, ' '));
		logger.info("Records treated as source: " + StringUtils.padLeft("" + recordsTreatedAsSource.getCount(), 20, ' '));
		logger.info("Records blocked:           " + StringUtils.padLeft("" + recordsBlocked.getCount(), 20, ' '));
		logger.info("Matches (one-sided):       " + StringUtils.padLeft("" + matches.getCount(), 20, ' '));

		setDone();		
	}

	private void match(List records) throws IOException {
		int len = records.size();
		for (int i = 0; i < len; i++) {
			Record q = (Record) records.get(i);
			
			recordsTreatedAsSource.increment();
			RecordSource rsBlocked = blocker.block(q, i+1);
			match(q, rsBlocked);
		}
	}
	
	private void match(RecordSource rs) throws IOException {
		rs.open();
		while (rs.hasNext()) {
			Record q = rs.getNext();
			recordsRead.increment();
			
			recordsTreatedAsSource.increment();
			RecordSource rsBlocked = blocker.block(q);
			match(q, rsBlocked);
		}
		rs.close();
	}

	private void match(Record q, RecordSource rsBlocked) throws IOException {
		rsBlocked.open();

		while (rsBlocked.hasNext()) {
			Record m = rsBlocked.getNext();
			recordsBlocked.increment();

			if (q.getId().equals(m.getId())) {
				continue;
			}
		
			ActiveClues activeClues = clueSet.getActiveClues(q, m, enabledClues);
			float matchProbability = evaluator.getProbability(activeClues);
			Decision decision = evaluator.getDecision(activeClues, matchProbability, lowerThreshold, upperThreshold);

			if (decision == Decision.MATCH || decision == Decision.HOLD) {
				MutableMarkedRecordPair mrp = new MutableMarkedRecordPair(q, m, decision, new Date(), user, src, comment);
				mrp.setActiveClues(activeClues);
				mrp.setProbability(matchProbability);
				mrp.setCmDecision(decision);
				mrp.setComment(String.valueOf(matchProbability));
			
				sink.put(mrp);
				matches.increment();
			}
		}
		
		rsBlocked.close();
	}

	private void readRecordSource(RecordSource rs, List list) throws IOException {
		list.clear();
		
		rs.open();
		while (rs.hasNext()) {
			Record r = rs.getNext();
			list.add(r);
			recordsRead.increment();
		}
		rs.close();
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	private void firePropertyChange(final String propertyName, Object oldValue, Object newValue) {
		propertyChangeListeners.firePropertyChange(propertyName, oldValue, newValue);
	}


	//
	// Output Methods
	//

	private void setDone() {
		firePropertyChange(DONE, Boolean.FALSE, Boolean.TRUE);
	}
	
	//
	//
	//
	
	private class Counter {
		private String propertyName;
		private int count;
		private int updateFreq;
		private int nextUpdate;
		public Counter(String propertyName, int updateFreq) {
			this.propertyName = propertyName;
			this.updateFreq = updateFreq;
		}
//		public void reset() {
//			count = 0;
//			nextUpdate = updateFreq;
//			update();
//		}
//		public void add(int i) {
//			count += i;
//			if (count >= nextUpdate) {
//				update();
//				while (nextUpdate < count) {
//					nextUpdate += updateFreq;
//				}
//			}
//		}
		public void increment() {
			if (++count >= nextUpdate) {
				update();
				nextUpdate += updateFreq;
			}
		}
		public void update() {
			firePropertyChange(propertyName, null, new Integer(count));
		}
		public int getCount() {
			return count;
		}
	}
	
}
