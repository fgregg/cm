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
import java.util.logging.Logger;

import com.choicemaker.cm.core.InvalidProfileException;
import com.choicemaker.cm.core.Profile;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.base.BeanMatchCandidate;
import com.choicemaker.cm.core.base.BeanProfile;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.transitivity.core.TransitivityException;

/**
 * This object builds an Iterator of CompositeEntity from an Array of MatchCandidate.
 *
 * @author pcheung
 *
 * ChoiceMaker Technologies, Inc.
 */
public class CEFromMatchCandidatesBuilder extends CEFromMatchesBuilder{

	private static Logger logger = Logger.getLogger(CEFromMatchCandidatesBuilder.class.getName());
	private BeanMatchCandidate[] candidates;


	/** This constructor take these important parameters:
	 *
	 * @param q - the query record
	 * @param candidates - an array of BeanMatchCandidate
	 * @param modelName - name of the ProbabilityModel
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 */
	public CEFromMatchCandidatesBuilder (Record q, BeanMatchCandidate[] candidates, String modelName,
		float differThreshold, float matchThreshold) {

		this.q = q;
		this.candidates = candidates;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;

		this.model = PMManager.getModelInstance(modelName);
		this.evaluator = model.getEvaluator();
	}


	/** This version of the constructor takes a Profile containing the record instead of
	 * the record itself.
	 *
	 * @param p - profile that contains the record
	 * @param candidates - an array of BeanMatchCandidate
	 * @param modelName - name of the ProbabilityModel
	 * @param differThreshold - differ threshold
	 * @param matchThreshold - match threshold
	 * @throws InvalidProfileException
	 */
	public CEFromMatchCandidatesBuilder (Profile p, BeanMatchCandidate[] candidates, String modelName,
		float differThreshold, float matchThreshold) throws InvalidProfileException {

		this.candidates = candidates;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;

		this.model = PMManager.getModelInstance(modelName);
		this.q = p.getRecord(model);
		this.evaluator = model.getEvaluator();
	}


	/** This method returns an Iterator of CompositeEntities created by the record
	 * and the match candidates.
	 *
	 * @return Iterator
	 */
	public Iterator getCompositeEntities () throws TransitivityException {
		//this stores MatchRecord2
		ArrayList pairs = new ArrayList ();

		ArrayList records = new ArrayList ();

		try {
			//first get the matches between q and all the m's.
			for (int i=0; i<candidates.length; i++) {
				Object o = candidates[i].getProfile();
				Profile profile = new BeanProfile(o);
				Record m = profile.getRecord(model);

				// 2009-08-17 rphall
				// BUG FIX? clue notes added here
				final String[] notes = candidates[i].getNotes();
				final String noteInfo = MatchRecord2.getNotesAsDelimitedString(notes);
				MatchRecord2 mr = new MatchRecord2 (q.getId(), m.getId(),
					MatchRecord2.STAGE_SOURCE,
					candidates[i].getProbability(),
					translateDecision(candidates[i].getDecision()),noteInfo);
				// END BUG FIX?

				logger.fine("q " + q.getId().toString() + " m " + m.getId().toString());

				pairs.add(mr);

				records.add(m);
			}

			//second match the m records against each other.
			pairs.addAll( allMatch(records));
		} catch (InvalidProfileException e) {
			logger.severe(e.toString());
		}

		CompositeEntityBuilder ceb = new CompositeEntityBuilder (pairs);
		return ceb.getCompositeEntities();
	}


	private char translateDecision (int d) {
		if (d == BeanMatchCandidate.HOLD) return MatchRecord2.HOLD;
		else if (d == BeanMatchCandidate.MATCH) return MatchRecord2.MATCH;
		else return MatchRecord2.DIFFER;
	}


}
