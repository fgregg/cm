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
package com.choicemaker.cm.core;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * Evaluation of matches.
 *
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 21:06:47 $
 */
public class RecordDecisionMaker {

	private static Logger logger = Logger.getLogger(RecordDecisionMaker.class);
	private static Logger profiler = Logger.getLogger("profile." + RecordDecisionMaker.class);

	/**
	 * Returns the sorted set of all records from source <code>src</code> that match the
	 * query record <code>q</code> with a probability of at least <code>lowerThreshold</code>.
	 *
	 * @param   q  The query record.
	 * @param   src  The record source of match records.
	 * @param   model  The probability model used for the matching.
	 * @param   lt  The differ threshold (minimum match probability for a match record to be returned).
	 * @param   ut  The match threshold.
	 * @return  The sorted (probability descending) set of <code>Match<code>es.
	 */
	public SortedSet getMatches(Record q, RecordSource src, ImmutableProbabilityModel model, float lt, float ut)
		throws java.io.IOException {
		int numMatched = 0;
		int numAdded = 0;
		SortedSet matches = new TreeSet();
		try {
			long t = System.currentTimeMillis();
			src.open();
			t = System.currentTimeMillis() - t;
			profiler.info("Time in Blocker.open() " + t);
			
			t = System.currentTimeMillis();
			Evaluator eval = model.getEvaluator();
			t = System.currentTimeMillis() - t;
			profiler.info("Time in model.getEvaluator() " + t);
						
			t = System.currentTimeMillis();
			while (src.hasNext()) {
				++numMatched;
				Match m = eval.getMatch(q, src.getNext(), lt, ut);
				if (m != null) {
					++numAdded;
					matches.add(m);
				}
			}
			t = System.currentTimeMillis() - t;
			profiler.info("Time in matching " + t);
			
		} finally {
			src.close();
		}
		if (logger.isInfoEnabled()) {
			logger.info("Number matched: " + numMatched + ", number above lower threshold: " + numAdded);
		}
		return matches;
	}
	
	public static SortedSet getPairs(Record q, RecordSource src, ImmutableProbabilityModel model, float lt, float ut) throws IOException {
		int numMatched = 0;
		SortedSet matches = new TreeSet();
		try {
			src.open();
			Evaluator eval = model.getEvaluator();
			ClueSet cs = model.getClueSet();
			boolean[] toEval = model.getCluesToEvaluate();
			while (src.hasNext()) {
				++numMatched;
				Record m = src.getNext();
				ActiveClues a = cs.getActiveClues(q, m, toEval);
				float p = eval.getProbability(a);
				Decision d = eval.getDecision(a, p, lt, ut);
				matches.add(new Match(d, p, m.getId(), m, a));
			}
		} finally {
			src.close();
		}
		if (logger.isInfoEnabled()) {
			logger.info("Number matched: " + numMatched);
		}
		return matches;
	}
}
