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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.choicemaker.cm.analyzer.filter.Filter;
import com.choicemaker.cm.analyzer.sampler.PairSampler;
import com.choicemaker.cm.core.ActiveClues;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Evaluator;
import com.choicemaker.cm.core.IMarkedRecordPair;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordBinder;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 13:47:41 $
 */
public class InMemoryMatcher {
	public static final String NUM_RECORDS_FROM_SMALL = "numRecordsFromSmall";
	public static final String NUM_RECORDS_FROM_LARGE = "numRecordsFromLarge";
	public static final String NUM_PAIRS = "numPairs";
	public static final String DONE = "done";

	public static final int NO_SORT = 0;
	public static final int DECISION_PROBABILITY = 1;
	public static final int PROBABILITY = 2;

	private RecordSource smallRecordSource;
	private RecordSource largeRecordSource;
	private MarkedRecordPairSink sink;
	private InMemoryBlocker blocker;
	private Filter filter;
	private PairSampler sampler;
	private IProbabilityModel probabilityModel;
	private float lowerThreshold;
	private float upperThreshold;
	private String user;
	private String src;
	private String comment;
	private boolean excludeMatchesToSelf;
	private int numRecordsFromSmall;
	private int numRecordsFromLarge;
	private int numPairs;
	private boolean done;
	private int maxNumMatchesPerSourceRecord;
	private Comparator sorter;

	public InMemoryMatcher(
		RecordSource smallRecordSource,
		RecordSource largeRecordSource,
		MarkedRecordPairSink sink,
		InMemoryBlocker blocker,
		Filter filter,
		PairSampler sampler,
		IProbabilityModel probabilityModel,
		float lowerThreshold,
		float upperThreshold,
		String user,
		String src,
		String comment,
		boolean excludeMatchesToSelf,
		int maxNumMatchesPerSourceRecord,
		int sortOrder) {
		this.smallRecordSource = smallRecordSource;
		this.largeRecordSource = largeRecordSource;
		this.sink = sink;
		this.blocker = blocker;
		this.filter = filter;
		this.sampler = sampler;
		this.probabilityModel = probabilityModel;
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		this.user = user;
		this.src = src;
		this.comment = comment;
		this.excludeMatchesToSelf = excludeMatchesToSelf;
		this.maxNumMatchesPerSourceRecord =
			maxNumMatchesPerSourceRecord == 0 ? Integer.MAX_VALUE : maxNumMatchesPerSourceRecord;
		initSorter(sortOrder);
	}

	private void initSorter(int sortOrder) {
		if (sortOrder == DECISION_PROBABILITY) {
			sorter = new Comparator() {
				public int compare(Object o1, Object o2) {
					IMarkedRecordPair p1 = (IMarkedRecordPair) o1;
					IMarkedRecordPair p2 = (IMarkedRecordPair) o2;
					int d = p1.getMarkedDecision().compareTo(p2.getMarkedDecision());
					if (d == 0) {
						if (p1.getProbability() > p2.getProbability()) {
							return -1;
						} else if (p1.getProbability() < p2.getProbability()) {
							return 1;
						} else {
							return 0;
						}
					} else {
						return d;
					}
				}
			};
		} else if (sortOrder == PROBABILITY) {
			sorter = new Comparator() {
				public int compare(Object o1, Object o2) {
					IMarkedRecordPair p1 = (IMarkedRecordPair) o1;
					IMarkedRecordPair p2 = (IMarkedRecordPair) o2;
					if (p1.getProbability() > p2.getProbability()) {
						return -1;
					} else if (p1.getProbability() < p2.getProbability()) {
						return 1;
					} else {
						return 0;
					}
				}
			};
		}
	}

	public void match() throws IOException {
		boolean deduplicateSingleSource = largeRecordSource == null;
		smallRecordSource.setModel(probabilityModel);
		sink.setModel(probabilityModel);
		List records = new ArrayList();
		smallRecordSource.open();
		int ns = 0;
		while (smallRecordSource.hasNext()) {
			records.add(smallRecordSource.getNext());
			if (++ns % 100 == 0) {
				setNumRecordsFromSmall(ns);
				if (Thread.interrupted()) {
					smallRecordSource.close();
					return;
				}
			}
		}
		smallRecordSource.close();
		setNumRecordsFromSmall(ns);
		//filter.reset();
		blocker.init(records);
		sink.open();
		Evaluator evaluator = probabilityModel.getEvaluator();
		ClueSet clueSet = probabilityModel.getClueSet();
		boolean[] enabledClues = probabilityModel.getCluesToEvaluate();
		Date date = new Date();
		filter.resetLimiters();
		if (deduplicateSingleSource) {
			largeRecordSource = new RecordBinder(records);
		}
		largeRecordSource.setModel(probabilityModel);
		largeRecordSource.open();
		int nl = 0;
		int np = 0;
		List candidates = new ArrayList();

		boolean interrupted = false;
		while (!interrupted && largeRecordSource.hasNext()) {
			Record q = largeRecordSource.getNext();
			RecordSource rsBlocked = deduplicateSingleSource ? blocker.block(q, nl) : blocker.block(q);
			rsBlocked.open();
			if (++nl % 100 == 0) {
				setNumRecordsFromLarge(nl);
				if (Thread.interrupted()) {
					interrupted = true;
				}
			}
			while (rsBlocked.hasNext()) {
				Record m = rsBlocked.getNext();
				ActiveClues activeClues = clueSet.getActiveClues(q, m, enabledClues);
				float matchProbability = evaluator.getProbability(activeClues);
				Decision decision =
					evaluator.getDecision(activeClues, matchProbability, lowerThreshold, upperThreshold);
				MutableMarkedRecordPair markedRecordPair = new MutableMarkedRecordPair(q, m, decision, date, user, src, comment);
				markedRecordPair.setActiveClues(activeClues);
				markedRecordPair.setProbability(matchProbability);
				markedRecordPair.setCmDecision(decision);
				if ((!excludeMatchesToSelf || !q.getId().equals(m.getId())) && filter.satisfy(markedRecordPair)) {
					markedRecordPair.setComment(String.valueOf(matchProbability));
					candidates.add(markedRecordPair);
				}
			}
			rsBlocked.close();
			int size = Math.min(candidates.size(), maxNumMatchesPerSourceRecord);
			if (sorter != null && size != 0) {
				Collections.sort(candidates, sorter);
			}
			for (int i = 0; i < size; ++i) {
				
				if (sampler != null) {
					sampler.processPair((MutableMarkedRecordPair)candidates.get(i));					
				} else {
					sink.putMarkedRecordPair((MutableMarkedRecordPair) candidates.get(i));
				}				
				
				if (++np % 100 == 0) {
					if (sampler != null) {
						setNumPairs(sampler.getNumRetained());
					} else {
						setNumPairs(np);
					}
					
					if (Thread.interrupted()) {
						interrupted = true;
					}
				}
			}
			candidates.clear();
		}
		largeRecordSource.close();
		
		if (sampler != null) {
			List pairs = sampler.getRetainedPairs();
			for (int i = 0, n = pairs.size(); i < n; i++) {
				sink.put((MutableMarkedRecordPair)pairs.get(i));
			}
		}
		
		sink.close();

		setNumRecordsFromLarge(nl);
		if (sampler != null) {
			setNumPairs(sampler.getNumRetained());
		} else {
			setNumPairs(np);
		}
		
		setDone(true);
	}

	/**
	 * Returns the blocker.
	 * @return InMemoryBlocker
	 */
	public InMemoryBlocker getBlocker() {
		return blocker;
	}

	/**
	 * Returns the comment.
	 * @return String
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Returns the filter.
	 * @return Filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Returns the largeRecordSource.
	 * @return RecordSource
	 */
	public RecordSource getLargeRecordSource() {
		return largeRecordSource;
	}

	/**
	 * Returns the lowerThreshold.
	 * @return float
	 */
	public float getLowerThreshold() {
		return lowerThreshold;
	}

	/**
	 * Returns the probabilityModel.
	 * @return ProbabilityModel
	 */
	public ImmutableProbabilityModel getProbabilityModel() {
		return probabilityModel;
	}

	/**
	 * Returns the sink.
	 * @return MarkedRecordPairSink
	 */
	public MarkedRecordPairSink getSink() {
		return sink;
	}

	/**
	 * Returns the smallRecordSource.
	 * @return RecordSource
	 */
	public RecordSource getSmallRecordSource() {
		return smallRecordSource;
	}

	/**
	 * Returns the src.
	 * @return String
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * Returns the upperThreshold.
	 * @return float
	 */
	public float getUpperThreshold() {
		return upperThreshold;
	}

	/**
	 * Returns the user.
	 * @return String
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the blocker.
	 * @param blocker The blocker to set
	 */
	public void setBlocker(InMemoryBlocker blocker) {
		this.blocker = blocker;
	}

	/**
	 * Sets the comment.
	 * @param comment The comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Sets the filter.
	 * @param filter The filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Sets the largeRecordSource.
	 * @param largeRecordSource The largeRecordSource to set
	 */
	public void setLargeRecordSource(RecordSource largeRecordSource) {
		this.largeRecordSource = largeRecordSource;
	}

	/**
	 * Sets the lowerThreshold.
	 * @param lowerThreshold The lowerThreshold to set
	 */
	public void setLowerThreshold(float lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}

	/**
	 * Sets the probabilityModel.
	 * @param probabilityModel The probabilityModel to set
	 */
	public void setProbabilityModel(IProbabilityModel probabilityModel) {
		this.probabilityModel = probabilityModel;
	}

	/**
	 * Sets the sink.
	 * @param sink The sink to set
	 */
	public void setSink(MarkedRecordPairSink sink) {
		this.sink = sink;
	}

	/**
	 * Sets the smallRecordSource.
	 * @param smallRecordSource The smallRecordSource to set
	 */
	public void setSmallRecordSource(RecordSource smallRecordSource) {
		this.smallRecordSource = smallRecordSource;
	}

	/**
	 * Sets the src.
	 * @param src The src to set
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * Sets the upperThreshold.
	 * @param upperThreshold The upperThreshold to set
	 */
	public void setUpperThreshold(float upperThreshold) {
		this.upperThreshold = upperThreshold;
	}

	/**
	 * Sets the user.
	 * @param user The user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the done.
	 * @return boolean
	 */
	public synchronized boolean isDone() {
		return done;
	}

	/**
	 * Returns the numPairs.
	 * @return int
	 */
	public int getNumPairs() {
		return numPairs;
	}

	/**
	 * Returns the numRecordsFromLarge.
	 * @return int
	 */
	public int getNumRecordsFromLarge() {
		return numRecordsFromLarge;
	}

	/**
	 * Returns the numRecordsFromSmall.
	 * @return int
	 */
	public int getNumRecordsFromSmall() {
		return numRecordsFromSmall;
	}

	/**
	 * Sets the done.
	 * @param done The done to set
	 */
	public synchronized void setDone(boolean done) {
		this.done = done;
	}

	/**
	 * Sets the numPairs.
	 * @param numPairs The numPairs to set
	 */
	public void setNumPairs(int numPairs) {
		this.numPairs = numPairs;
	}

	/**
	 * Sets the numRecordsFromLarge.
	 * @param numRecordsFromLarge The numRecordsFromLarge to set
	 */
	public void setNumRecordsFromLarge(int numRecordsFromLarge) {
		this.numRecordsFromLarge = numRecordsFromLarge;
	}

	/**
	 * Sets the numRecordsFromSmall.
	 * @param numRecordsFromSmall The numRecordsFromSmall to set
	 */
	public void setNumRecordsFromSmall(int numRecordsFromSmall) {
		this.numRecordsFromSmall = numRecordsFromSmall;
	}

}
