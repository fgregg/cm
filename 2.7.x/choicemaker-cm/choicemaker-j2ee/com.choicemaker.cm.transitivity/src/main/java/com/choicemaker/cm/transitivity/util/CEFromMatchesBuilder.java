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

import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecordUtils;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object builds an iterator of CompositeEntity from a query record and an
 * Iterator of Match objects.
 *
 * @author pcheung
 *
 *         ChoiceMaker Technologies, Inc.
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
public class CEFromMatchesBuilder {

	private static Logger logger = Logger.getLogger(CEFromMatchesBuilder.class
			.getName());

	protected Record q;
	protected Iterator matches;
	protected Evaluator evaluator;
	protected ImmutableProbabilityModel model;
	protected float differThreshold;
	protected float matchThreshold;

	public CEFromMatchesBuilder() {
	}

	/**
	 * @param q
	 *            - the query record
	 * @param matches
	 *            - an Iterator of Match. Records that match q
	 * @param modelName
	 *            - name of the ProbabilityModel
	 * @param differThreshold
	 *            - differ threshold
	 * @param matchThreshold
	 *            - match threshold
	 */
	public CEFromMatchesBuilder(Record q, Iterator matches, String modelName,
			float differThreshold, float matchThreshold) {

		this.q = q;
		this.matches = matches;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;

		this.model = PMManager.getModelInstance(modelName);
		this.evaluator = model.getEvaluator();
	}

	/**
	 * Returns an Iterator of CompositeEntities created by the record and the
	 * matches.
	 */
	public Iterator getCompositeEntities() throws TransitivityException {

		// First, get the matches between q and all the m's.
		List<MatchRecord2> pairs = new ArrayList<>();
		List<Record> records = new ArrayList<>();
		while (matches.hasNext()) {
			Match m = (Match) matches.next();
			final String noteInfo =
				MatchRecordUtils.getNotesAsDelimitedString(m.ac, this.model);
			MatchRecord2 mr =
				new MatchRecord2(q.getId(), m.id, RECORD_SOURCE_ROLE.STAGING,
						m.probability, m.decision, noteInfo);
			pairs.add(mr);
			records.add(m.m);
		}

		// Second, match the m records against each other.
		List l = allMatch(records);
		pairs.addAll(l);

		logger.fine("number of pairs: " + pairs.size());

		CompositeEntityBuilder ceb = new CompositeEntityBuilder(pairs);
		return ceb.getCompositeEntities();
	}

	/** Matches each record in a list with all the other records in the list */
	protected List allMatch(List records) {
		ArrayList ret = new ArrayList();

		int s = records.size();
		for (int i = 0; i < s - 1; i++) {
			Record r1 = (Record) records.get(i);

			for (int j = i + 1; j < s; j++) {
				Record r2 = (Record) records.get(j);
				MatchRecord2 mr = match(r1, r2);
				if (mr != null)
					ret.add(mr);

				logger.fine("r1 " + r1.getId().toString() + " r2 "
						+ r2.getId().toString());
			}
		}
		return ret;
	}

	/** Compares two records and returns a MatchRecord2 object */
	protected MatchRecord2 match(Record r1, Record r2) {
		MatchRecord2 retVal = null;
		Match match =
			evaluator.getMatch(r1, r2, differThreshold, matchThreshold);
		if (match != null) {
			final ClueSet clueSet = model.getClueSet();
			final boolean[] enabledClues = model.getCluesToEvaluate();
			final boolean isStage = true;
			retVal =
				MatchRecordUtils.compareRecords(clueSet, enabledClues, model, r1, r2,
						isStage, differThreshold, matchThreshold);
		}
		return retVal;
	}

	protected char translateDecision(Decision d) {
		if (d.equals(Decision.HOLD))
			return MatchRecord2.HOLD;
		else if (d.equals(Decision.MATCH))
			return MatchRecord2.MATCH;
		else
			return MatchRecord2.DIFFER;
	}

}
