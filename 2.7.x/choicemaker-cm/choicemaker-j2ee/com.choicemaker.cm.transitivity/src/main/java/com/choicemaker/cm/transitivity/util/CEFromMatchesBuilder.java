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
package com.choicemaker.cm.transitivity.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;

import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object builds an iterator of CompositeEntity from a query record and an
 * Iterator of Match objects.
 *
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class CEFromMatchesBuilder {

	private static Logger logger = Logger.getLogger(CEFromMatchesBuilder.class.getName());

	protected Record q;
	protected Iterator matches;
	protected Evaluator evaluator;
	protected IProbabilityModel model;
	protected float differThreshold;
	protected float matchThreshold;


	public CEFromMatchesBuilder () {
	}


	/** This constructor take these important parameters:
	 *
	 * @param q - the query record
	 * @param matches - an Iterator of Match.  Records that match q
	 * @param modelName - name of the ProbabilityModel
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 */
	public CEFromMatchesBuilder (Record q, Iterator matches, String modelName,
		float differThreshold, float matchThreshold) {

		this.q = q;
		this.matches = matches;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;

		this.model = PMManager.getModelInstance(modelName);
		this.evaluator = model.getEvaluator();
	}


	/** This method returns an Iterator of CompositeEntities created by the record
	 * and the matches.
	 *
	 * @return Iterator
	 */
	public Iterator getCompositeEntities () throws TransitivityException {
		//this stores MatchRecord2
		ArrayList pairs = new ArrayList ();

		ArrayList records = new ArrayList ();

		//first get the matches between q and all the m's.
		while (matches.hasNext()) {
			Match m = (Match) matches.next();

			// 2009-08-17 rphall
			// BUG FIX? clue notes added here
			final String noteInfo = MatchRecord2.getNotesAsDelimitedString(m.ac,this.model);
			MatchRecord2 mr = new MatchRecord2 (q.getId(), m.id, MatchRecord2.STAGE_SOURCE,
				m.probability, translateDecision(m.decision),noteInfo);
			pairs.add(mr);
			// END BUG FIX?

			records.add(m.m);
		}

		//second match the m records against each other.
		List l = allMatch(records);
		pairs.addAll(l);

		logger.fine("number of pairs: " + pairs.size());

		CompositeEntityBuilder ceb = new CompositeEntityBuilder (pairs);
		return ceb.getCompositeEntities();
	}


	/** This method matches each record in the list with all the other records.
	 *
	 * @param records
	 * @return
	 */
	protected List allMatch (List records) {
		ArrayList ret = new ArrayList ();

		int s = records.size();
		for (int i=0; i<s-1; i++) {
			Record r1 = (Record) records.get(i);

			for (int j=i+1; j<s; j++) {
				Record r2 = (Record) records.get(j);
				MatchRecord2 mr = match (r1, r2);
				if (mr != null) ret.add(mr);

				logger.fine("r1 " + r1.getId().toString() + " r2 " + r2.getId().toString());
			}
		}
		return ret;
	}


	/** This method compares two records and returns a MatchRecord2 object.
	 *
	 * @param r1
	 * @param r2
	 * @return MatchRecord2
	 */
	protected MatchRecord2 match (Record r1, Record r2) {
		Match match = evaluator.getMatch(r1, r2, differThreshold, matchThreshold);

		if (match == null) return null;

		Decision decision = match.decision;
		float matchProbability = match.probability;
		char source = MatchRecord2.STAGE_SOURCE;

		Comparable i1 = r1.getId();
		Comparable i2 = r2.getId();

		//make sure the smaller id is first
		if (i1.compareTo(i2) > 0) {
			Comparable i3 = i1;
			i1 = i2;
			i2 = i3;
		}

		String noteInfo = MatchRecord2.getNotesAsDelimitedString(match.ac,model);
		MatchRecord2 mr = null;
		if (decision == Decision.MATCH) {
			mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.MATCH, noteInfo);
		} else if (decision == Decision.HOLD) {
			mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.HOLD, noteInfo);
		}

		return mr;
	}


	protected char translateDecision (Decision d) {
		if (d.equals(Decision.HOLD)) return MatchRecord2.HOLD;
		else if (d.equals(Decision.MATCH)) return MatchRecord2.MATCH;
		else return MatchRecord2.DIFFER;
	}


}
