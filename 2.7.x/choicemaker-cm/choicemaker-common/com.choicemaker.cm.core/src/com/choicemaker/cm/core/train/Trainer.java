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
package com.choicemaker.cm.core.train;

import java.util.Collection;
import java.util.Iterator;

import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.BooleanActiveClues;
import com.choicemaker.cm.core.base.ClueDesc;
import com.choicemaker.cm.core.base.ClueSet;
import com.choicemaker.cm.core.base.ClueSetType;
import com.choicemaker.cm.core.base.Decision;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.MessageUtil;
import com.choicemaker.cm.core.util.OperationFailedException;

/**
 * Train and test models with data from a marked record pair source.
 *
 * @author    Martin Buechi
 * @author    S. Yoakum-Stover
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 20:49:13 $
 */

public class Trainer /* implements ITrainer */ {
	private IProbabilityModel model;
	private Collection src;
	private int noPairs;
	private int[] size;
	private float lowerThreshold;
	private float upperThreshold;

	/**
	 * Number of possible decision values, e.g. 2 for match/differ,
	 * 3 for match/differ/hold.
	 */
	private int decisionDomainSize;

	/**
	 * The number of times the ith clue fired on the training set.
	 */
	private int[] counts;

	/**
	 * The number of times the ith clue fired correctly on the training set.
	 */
	private int[] correctCounts;

	/**
	 * The number of times the ith clue fired incorrectly on the training set.
	 */
	private int[] incorrectCounts;

	/**
	 * The number of times the feature fired correctly divided by the number of pairs.
	 */
	private double[] firingPercentages;

	private int[] numCorrectFirings;

	private int[] numIncorrectFirings;

	// Count cache dirty counter supporting multiple evaluations
	private int evaluationNo;
	private int computeCountsNo;
	private int computeProbabilitiesNo;

	public Trainer(float lowerThreshold, float upperThreshold) {
		setLowerThreshold(lowerThreshold);
		setUpperThreshold(upperThreshold);
	}

	public void setModel(IProbabilityModel model) {
		this.model = model;
	}

	public void setSource(Collection src) {
		this.src = src;
	}

	/**
	 * Set the weights and clues to evaluate by training on the source.
	 *
	 * @param minClueFirings  The minimum number of times a clue must fire on the training set in order to
	 *          be retained in the primed probability model.
	 * @param numIterations  Number of iterations that the MaxumumEntropy estimater should execute.
	 * @param fileNameBase  Name (up to the extension) given to the output files that are created for the
	 *               MaximumEntropy estimator.
	 * @throws IOException  if there is a problem reading from the source.
	 */
	public Object train() throws OperationFailedException {
		computeFirings();
		computeCounts();
		boolean[] cluesToEvaluate = model.getCluesToEvaluate();
		ClueSet cs = model.getAccessor().getClueSet();
		ClueDesc[] desc = cs.getClueDesc();
		if (cs.getType() == ClueSetType.BOOLEAN) {
			int minClueFirings = model.getFiringThreshold();
			for (int i = 0; i < counts.length; ++i) {
				cluesToEvaluate[i] = cluesToEvaluate[i] && (desc[i].rule || correctCounts[i] >= minClueFirings);
			}
		}
		// model.setCluesToEvaluate(cluesToEvaluate) not needed because we got actual array above
		return model.getMachineLearner().train(src, firingPercentages);
	}

	/**
	 * Tests the model against the data from the source.
	 *
	 * @throws IOException  if there is a problem reading from the source.
	 */
	public void test() throws OperationFailedException {
		computeFirings();
	}

	/**
	 * Evaluates the clues on each of the record pairs in the training set.
	 * 
	 * @exception IOException
	 */
	private void computeFirings() throws OperationFailedException {
		++evaluationNo;
		decisionDomainSize = model.getDecisionDomainSize();
		ClueSet fs = model.getClueSet();
		boolean[] cluesToEvaluate = model.getCluesToEvaluate();
		int br = 0;
		Iterator i = src.iterator();
		while (i.hasNext()) {
			MutableMarkedRecordPair mp = (MutableMarkedRecordPair) i.next();
			mp.setActiveClues(fs.getActiveClues(mp.getQueryRecord(), mp.getMatchRecord(), cluesToEvaluate));
			if ((br = (br + 1) % 100) == 0 && Thread.currentThread().isInterrupted()) {
				break;
			}
		}
		noPairs = src.size();
		if (noPairs == 0) {
			throw new OperationFailedException(MessageUtil.m.formatMessage("train.trainer.source.is.empty"));
		}
	}

	/**
	 * Return the decision domain size. This is 3 if there are
	 * clues that predict hold and 2 otherwise.
	 *
	 * @return  the decision domain size.
	 */
	public int getDecisionDomainSize() {
		return decisionDomainSize;
	}

	/**
	 * Returns the number of times each clue fired on the source data.
	 *
	 * @return  the number of times each clue fired on the source data.
	 */
	public int[] getFirings() {
		computeCounts();
		return counts;
	}

	/**
	 * Returns the number of times each clue fired correctly on the source data.
	 *
	 * @return  the number of times each clue fired correctly on the source data.
	 */
	public int[] getCorrectFirings() {
		computeCounts();
		return correctCounts;
	}

	/**
	 * Returns the number of times each clue fired incorrectly on the source data.
	 *
	 * @return  the number of times each clue fired incorrectly on the source data.
	 */
	public int[] getIncorrectFirings() {
		computeCounts();
		return incorrectCounts;
	}

	public int getNumCorrectFirings(Decision d) {
		if (d.toInt() >= decisionDomainSize) {
			return 0; //what a hack!
		}
		return numCorrectFirings[d.toInt()];
	}

	public int getNumIncorrectFirings(Decision d) {
		if (d.toInt() >= decisionDomainSize) {
			return 0; //what a hack!
		}
		return numIncorrectFirings[d.toInt()];
	}

	public double[] getFiringPercentages() {
		computeCounts();
		return firingPercentages;
	}

	/**
	 * Returns the number of pairs used for training/testing.
	 *
	 * @return  The number of pairs used for training/testing.
	 */
	public int size() {
		return noPairs;
	}

	/**
	 * Returns the number of pairs predicting <code>d</code> used for training/testing.
	 *
	 * @param   d  The decision for which the size is requested. 
	 * @return  The number of pairs predicting <code>d</code> used for training/testing.
	 */
	public int size(Decision d) {
		computeCounts();
		return size[d.toInt()];
	}

	public Collection getSource() {
		return src;
	}

	public void computeProbabilitiesAndDecisions(float lt, float ut) {
		if (evaluationNo != computeProbabilitiesNo) {
			computeProbabilitiesNo = evaluationNo;
			Evaluator e = model.getEvaluator();
			Iterator i = src.iterator();
			while (i.hasNext()) {
				MutableMarkedRecordPair p = (MutableMarkedRecordPair) i.next();
				p.setProbability(e.getProbability(p.getActiveClues()));
				p.setCmDecision(e.getDecision(p.getActiveClues(), p.getProbability(), lt, ut));
			}
		}
	}

	public void computeDecisions(float lt, float ut) {
		Evaluator e = model.getEvaluator();
		Iterator i = src.iterator();
		while (i.hasNext()) {
			MutableMarkedRecordPair p = (MutableMarkedRecordPair) i.next();
			p.setCmDecision(e.getDecision(p.getActiveClues(), p.getProbability(), lt, ut));
		}
	}

	public void computeProbability(MutableMarkedRecordPair mrp) {
		mrp.getQueryRecord().computeValidityAndDerived();
		mrp.getMatchRecord().computeValidityAndDerived();
		mrp.setActiveClues(model.getClueSet().getActiveClues(mrp.getQueryRecord(), mrp.getMatchRecord(), model.getCluesToEvaluate()));
		Evaluator e = model.getEvaluator();
		mrp.setProbability(e.getProbability(mrp.getActiveClues()));
		mrp.setCmDecision(e.getDecision(mrp.getActiveClues(), mrp.getProbability(), lowerThreshold, upperThreshold));
	}

	/**
	 * Fills the counts array whose ith element represents the number of times the
	 * ith clue fired when evaluated on the training set. 
	 */
	private void computeCounts() {
		if (evaluationNo != computeCountsNo) {
			computeCountsNo = evaluationNo;
			ClueSet fs = model.getClueSet();
			ClueDesc[] cd = fs.getClueDesc();
			int numClues = fs.size();
			counts = new int[numClues];
			correctCounts = new int[numClues];
			incorrectCounts = new int[numClues];
			firingPercentages = new double[numClues];
			numCorrectFirings = new int[decisionDomainSize];
			numIncorrectFirings = new int[decisionDomainSize];
			size = new int[Decision.NUM_DECISIONS];
			Iterator iPair = src.iterator();
			while (iPair.hasNext()) {
				MutableMarkedRecordPair p = (MutableMarkedRecordPair) iPair.next();
				++size[p.getMarkedDecision().toInt()];
				ActiveClues af = p.getActiveClues();
				if (af instanceof BooleanActiveClues) {
					BooleanActiveClues bac = (BooleanActiveClues) af;
					int len = af.size();
					for (int j = 0; j < len; ++j) {
						int clueNum = bac.get(j);
						++counts[clueNum];
						Decision d = cd[clueNum].decision;
						if (p.getMarkedDecision() == d) {
							++correctCounts[clueNum];
							++numCorrectFirings[d.toInt()];
						} else {
							++incorrectCounts[clueNum];
							++numIncorrectFirings[d.toInt()];
						}
					}
				}
				int[] rules = af.getRules();
				for (int i = 0; i < rules.length; ++i) {
					++counts[rules[i]];
				}
			}
			for (int i = 0; i < firingPercentages.length; ++i) {
				firingPercentages[i] = (double) correctCounts[i] / (double) noPairs;
			}
		}
	}
	/**
	 * Returns the lowerThreshold.
	 * @return float
	 */
	public float getLowerThreshold() {
		return lowerThreshold;
	}

	/**
	 * Returns the upperThreshold.
	 * @return float
	 */
	public float getUpperThreshold() {
		return upperThreshold;
	}

	/**
	 * Sets the lowerThreshold.
	 * @param lowerThreshold The lowerThreshold to set
	 */
	public void setLowerThreshold(float lowerThreshold) {
		this.lowerThreshold = lowerThreshold;
	}

	/**
	 * Sets the upperThreshold.
	 * @param upperThreshold The upperThreshold to set
	 */
	public void setUpperThreshold(float upperThreshold) {
		this.upperThreshold = upperThreshold;
	}

}
