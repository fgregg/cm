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
package com.choicemaker.cm.ml.me.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.BooleanActiveClues;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.util.IntArrayList;

/**
 * Java implementation of Maximum Entropy estimator.
 * 
 * @author	Adam Winkel
 * @author  Martin Buechi
 * @author  S. Yoakum-Stover
 * @version $Revision: 1.2 $ $Date: 2010/03/24 23:17:38 $
 */
public class MeEstimator {

	private static final Logger logger = Logger.getLogger(MeEstimator.class.getName());

	private final IProbabilityModel model;
	private float[] finalWeights;

	/** The MRPs on which to train. */
	protected Collection src;

	protected Object warning;

	/** The number of times the feature fired correctly divided by the number of pairs. */
	protected double[] firingPercentages;

	/** The number of iterations the Max Ent should run. */
	protected int numIterations;


	/**
	 * Create an Estimator instance.
	 *
	 */
	public MeEstimator(IProbabilityModel model, Collection src, 
					   double[] firingPercentages, int numIterations) {
			
		this.model = model;
		this.src = src;
		this.firingPercentages = firingPercentages;
		this.numIterations = numIterations;
	}

	/**
	 * Returns the clue weights after training.
	 */
	public float[] getWeights() {
		if (finalWeights != null) {
			return finalWeights;
		}
		
		throw new IllegalStateException(
			"Cannot get weights before or during training or after failed training.");
	}

	/**
	 * 
	 */
	public Object getWarning() {
		return warning;
	}

	/**
	 * 
	 * @throws Exception if something bad happens.
	 */
	public void run() throws Exception {		
		// clear the weights. 
		finalWeights = new float[firingPercentages.length];
		Arrays.fill(finalWeights, 1.0f);
				
		initialize();
		
		double[] alpha = runImprovedIterativeScaling();

		finalWeights = alphasToWeights(alpha);
	}

	/**
	 * Tasks:
	 *    get the number of futures (decisions).
	 * 
	 *	  get the number of features (clues).
	 *    compute the expectation for each feature, K[i]
	 *
	 *    create the histories/events.
	 *
	 */
	private void initialize() {
				
		//
		// futures, indexed from 0 to (numFutures-1)
		//
		
		numFutures = model.getDecisionDomainSize();
		//numFutures = 2;
		
		//
		// features
		//
		
		boolean[] cluesToEvaluate = model.getTrainCluesToEvaluate();
		
		numTotalFeatures = firingPercentages.length;
		numFeatures = 0; // we'll fill this in as we go...
		featureToIndex = new int[numTotalFeatures];
		numFeatures = 0;
		for (int i = 0; i < firingPercentages.length; i++) {
			if (cluesToEvaluate[i]) {
				featureToIndex[i] = numFeatures++;
			}
		}
		
		indexToFeature = new int[numFeatures];
		int fNum = 0;
		for (int i = 0; i < firingPercentages.length; i++) {
			if (cluesToEvaluate[i]) {
				indexToFeature[fNum++] = i;
			}	
		}

		K = new double[numFeatures];
		for (int i = 0; i < firingPercentages.length; i++) {
			if (cluesToEvaluate[i]) {
				K[ featureToIndex[i] ] = firingPercentages[i];	
			}	
		}
		
		//
		// histories/events
		//
		// We first get the set of all UNIQUE histories.
		//

		int numTrainingExamples = 0;
		// 2013-08-07 rphall
		// int numEvents = 0;
		numHistories = 0;
		max_c = 0;
		
		HashMap hMap = new HashMap(src.size());
		
		IntArrayList[] cluesForFuture = new IntArrayList[numFutures];
		for (int i = 0; i < numFutures; i++) {
			cluesForFuture[i] = new IntArrayList(2);	
		}

		ClueDesc[] cd = model.getAccessor().getClueSet().getClueDesc();
		Iterator itPairs = src.iterator();
		while (itPairs.hasNext()) {
			MutableMarkedRecordPair p = (MutableMarkedRecordPair) itPairs.next();
			BooleanActiveClues af = (BooleanActiveClues)p.getActiveClues();
			
			for (int i = 0; i < numFutures; i ++) {
				cluesForFuture[i].clear();
			}			
						
			int size = af.size();
			for (int i = 0; i < size; i++) {
				int clueNo = af.get(i);
				if (cluesToEvaluate[clueNo]) {
					cluesForFuture[cd[clueNo].decision.toInt()].add(featureToIndex(clueNo));
				}
			}
			
			MeHistory history = new MeHistory(cluesForFuture);
			if (hMap.containsKey(history)) {
				history = (MeHistory)hMap.get(history);	
			} else {
				hMap.put(history, history);
				numHistories++;
				
				if (history.getHistoryMaxC() > max_c) {
					max_c = history.getHistoryMaxC();	
				}
			}
			
			//
			// Avoid adding a hold if there are no hold clues.
			//
			int future = p.getMarkedDecision().toInt();
			if (isLegalFuture(future)) {
				history.addEvent(future);
				// 2013-08-07 rphall
				// numEvents++;
			}
			numTrainingExamples++;
			
		}

		histories = new MeHistory[numHistories];

		int counter = 0;
		Iterator itHistories = hMap.values().iterator();
		while (itHistories.hasNext()) {
			MeHistory h = (MeHistory) itHistories.next();

			//
			// Andrew and I differ here.
			//
			//h.setHistoryProbability( h.getEventCount() / (double)numEvents );
			h.setHistoryProbability( h.getEventCount() / (double)numTrainingExamples );

			histories[counter++] = h;
		}
	}

	private static final double CONVERGENCE = .0000001;
	private static final double NONCONVERGENCE = .05;

	private double[] runImprovedIterativeScaling() {
		double[] alpha = new double[numFeatures];
		Arrays.fill(alpha, 1.0);
		
		double[][] K_j = initDouble(numFeatures, max_c + 1);
		double[] beta = new double[numFeatures];
				
		for (int i = 0; i < numIterations; i++) {
			
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("iteration: " + i);
			} else if (logger.isLoggable(Level.INFO)) {
				if (i % 100 == 0) {
					logger.info("iteration: " + i);
				}
			}
			
			fill(K_j, 0.0);
			
			for (int j = 0; j < numHistories; j++) {
				histories[j].updateExpectations(alpha, K_j);
			}
			
			computeBeta(K_j, K, beta);
			
			//
			// Andrew and I also differ here.  I take the absolute value
			// of the max, whereas he does not.
			//
			if ( Math.abs(max(beta) - 1.0) < CONVERGENCE &&
				 Math.abs(min(beta) - 1.0) < CONVERGENCE ) {
				return alpha;	 	
			}

			if ( i == numIterations - 1 ) {
				if (Math.abs(max(beta) - 1.0) > NONCONVERGENCE ||
					Math.abs(min(beta) - 1.0) > NONCONVERGENCE) {
					createNonConvergenceWarning(beta);		
				} else {
					warning = null;	
				}
			}

			multiply(alpha, beta);
		}
		
		return alpha;
	}
	
	private void createNonConvergenceWarning(double[] beta) {		
		String msg = "Some clues may have runaway weights:\n";
		ClueDesc[] cd = model.getAccessor().getClueSet().getClueDesc();
		for (int i = 0; i < beta.length; i++) {
			if (Math.abs(beta[i] - 1.0) > NONCONVERGENCE && !cd[i].rule) {
				msg += "\t" + cd[i].getName() + "\n";
			}
		}
		
		warning = msg;
	}
	
	private float[] alphasToWeights(double[] alpha) {
		float[] weights = new float[firingPercentages.length];
		Arrays.fill(weights, 1.0f);
		
		for (int i = 0; i < numFeatures; i++) {
			weights[ indexToFeature(i) ] = (float) alpha[i];
		}
		
		return weights;
	}

	private void computeBeta(double[][] expectations, double[] empirical, double[] beta) {
		for (int i = 0; i < numFeatures; i++) {
			double coeffs[] = expectations[i];
			coeffs[0] = -empirical[i];	
			
			beta[i] = findRoot(coeffs);
		}
	}
	
	//
	// These four variables are only used in findRoot().
	// DO NOT USE THEM ELSEWHERE!
	//
	// NOTE: Changed from 200 to 400 to fix Dean's exception when running for 200k iterations...
	private static final int NEWTON_ITERATIONS = 400;
	private static final double NEWTON_ACCURACY = .0000001;
	private DoubleHolder f = new DoubleHolder();
	private DoubleHolder df = new DoubleHolder();

	/**
	 * We assume that the 0th coefficient is negative and all other coefficients
	 * are non-negative, with at least one coefficient (i > 1) non-zero.
	 * 
	 * @param coeffs the coefficients of the polynomial
	 * f(x) = coeffs[0] + coeffs[1] * x + coeffs[2] * x^2 + ...
	 */
	private double findRoot(double[] coeffs) {

		//
		// Find the last non-zero coefficient.
		// If there is no such i > 0, then the function has either no
		// roots, or an infinite number of roots.
		//
		// Any problem of this sort will be caught by the next block.
		//
		int lastCoeff = -1;
		for (int i = coeffs.length - 1; i > 0; i--) {
			if (coeffs[i] != 0) {
				lastCoeff = i;
				break;	
			}
		}
		
		//
		// Find the first coefficient that multiplies x that is not equal to zero.
		// If there isn't one, the functions is f(x) = c_0 , which has either no
		// roots or an infinite number of roots.
		//
		int firstCoeff = -1;
		for (int i = 1; i <= lastCoeff; i++) {
			if (coeffs[i] != 0) {
				firstCoeff = i;
				break;	
			}
		}
		if (firstCoeff < 0) {
			throw new IllegalStateException("Illegal polynomial!");
		}

		//
		// Derivation of this upper bound:
		//   0 = c0 + c1 * x + c2 * x^2 + c3 * x^3 + ...
		//   -c0 >= c_firstCoeff * x^firstCoeff
		//			(all other coeffs are positive)
		//   x <= ( -c0/c_firstCoeff )^(1/firstCoeff)
		//
		// NOTE: we assume that the lower bound is 0.  This holds
		// if c0 < 0 and ci >= 0 for i >= 1.
		//
		double upperBound = Math.pow( -coeffs[0] / coeffs[firstCoeff] , 1 / (double)firstCoeff );
		
		//
		// Check some boundary conditions.
		//
		evalFunctionAndDerivative(coeffs, lastCoeff, 0.0, f, df);
		double polyLow = f.value;
		evalFunctionAndDerivative(coeffs, lastCoeff, upperBound, f, df);
		double polyHigh = f.value;
		
		if (polyLow == 0) {
			return 0.0;
		} else if (polyLow > 0) {
			// This only happens if the zeroth coefficient is positive,
			//   which means the firing percentage is negative.  
			// This is all sorts of bad news.
			throw new IllegalStateException("f(0) > 0!");
		} else if (Math.abs(polyHigh) < NEWTON_ACCURACY) {
			return upperBound;
		} else if ( (polyLow > 0 && polyHigh > 0) || (polyLow < 0 && polyHigh < 0) ) {
			throw new IllegalStateException("Function values at 0 and upperBound on same side of x-axis!");
		}
		
		//
		// Set up for iterating.
		//
		// BUG 2009-06-25 rphall
		// This algorithm breaks down if f evaluates to infinity, which can happen.
		// This bug was not fixed because if f is infinity, there's usually other problems
		// with the training data. For example, the same training data that triggers this
		// bug will -- if the bug is fixed as indicated below -- later on trigger an
		// IllegaStateException in line 360 ("illegal polynomial"). Until both bugs
		// can be repaired -- or the issue with some training data can be
		// identified -- it doesn't seem prudent to hack a fix here.
		double lower = 0.0;
		double upper = upperBound;
		double guess = 0.5 * (lower + upper);
		double oldDx = Math.abs(upper - lower);
		double dx = oldDx;
		evalFunctionAndDerivative(coeffs, lastCoeff, guess, f, df);
//		// BUGFIX 2009-06-25 rphall
//		// Check if f is POSITIVE_INFINITY or NaN. If so, keep halving
//		// the guess until f is finite, or the guess is nearly zero. If the
//		// number of halvings is too many, set the guess to zero and
//		// return.
//		int _halvings = 0;
//		while( Double.compare(f.value,Double.POSITIVE_INFINITY) >=0 ) {
//			if (_halvings >= NEWTON_ITERATIONS) {
//				return lower;
//			}
//			if (Math.abs(guess - lower) < NEWTON_ACCURACY) {
//				return lower;
//			}
//			upper = upperBound / 2.0;
//			guess = 0.5 * (lower + upper);
//			oldDx = Math.abs(upper - lower);
//			dx = oldDx;
//			evalFunctionAndDerivative(coeffs, lastCoeff, guess, f, df);
//			++_halvings;
//		}
//		// ENDBUGFIX
//		// END BUG (when f is infinite)
		for (int i = 1; i < NEWTON_ITERATIONS; i++) {

//			double _p0 = (guess - upper);
//			double _p1 = _p0 * df.value;
//			double _p2 = _p1 - f.value;
//
//			double _q0 = (guess - lower);
//			double _q1 = _q0 * df.value;
//			double _q2 = _q1 - f.value;
//			
//			double _r = _p2 * _q2;
//			
//			double _s = Math.abs(2.0 * f.value);
//			double _t = Math.abs(oldDx * df.value);
//			
//			boolean _b1 = _r > 0.0;
//			boolean _b2 = _s > _t;
//			boolean _b3 = _b1 || _b2;
			
			if ( ((guess - upper) * df.value - f.value) *
				 ((guess - lower) * df.value - f.value) > 0.0 ||
				 Math.abs(2.0 * f.value) > Math.abs(oldDx * df.value) )   {
			
				oldDx = dx;
				dx = 0.5 * (upper - lower);
				guess = lower + dx;
			} else {
				oldDx = dx;
				dx = f.value / df.value;
				guess -= dx;
			}
			
			if (Math.abs(dx) < NEWTON_ACCURACY) {
				return guess;	
			}
			
			evalFunctionAndDerivative(coeffs, lastCoeff, guess, f, df);
			
			if (f.value < 0.0) {
				lower = guess;
			} else {
				upper = guess;
			}
		}
		
		throw new IllegalStateException("Maximum number of iterations reached in findRoot()");
	}

	/**
	 * Evaluate f(x) and f'(x) for
	 * 
	 * f(x) = coeffs[0] + coeffs[1] * x + ... + coeffs[to] * x^to
	 */
	private void evalFunctionAndDerivative(double[] coeffs, int to, double x,
											DoubleHolder poly, DoubleHolder deriv) {
		double df = 0;
		double f = coeffs[to];
		
		for (int i = to - 1; i >= 0; i--) {
			df = f + df * x;	
			f = coeffs[i] + f * x;
		}
		
		poly.value = f;
		deriv.value = df;
	}

	//
	// Variables we use during the execution.
	//
	
	private int numFutures;
	
	private int numFeatures;
	private int numTotalFeatures;
	private int[] featureToIndex;
	private int[] indexToFeature;

	private int numHistories;
	private MeHistory[] histories;
	private int max_c;


	/**
	 * K[i] is the expected value of the ith clue, i.e. its 
	 * firing percentage on the training corpus.
	 */
	private double[] K;
		
	//
	// Utility methods
	//

	private int featureToIndex(int featureNum) {
		return featureToIndex[featureNum];	
	}
	
	private int indexToFeature(int index) {
		return indexToFeature[index];	
	}

	/**
	 * For matching purposes, futures are numbered 0, 1, 2 for
	 * differ, match, and hold, respectively.  If a clueset has no 
	 * clues predicting hold, PMManager.getDecisionDomainSize() returns
	 * 2 (match and differ), so hold (2) is not legal.
	 */
	private boolean isLegalFuture(int future) {
		return future < numFutures;	
	}
	
	private double[][] initDouble(int rows, int cols) {
		double[][] m = new double[rows][];
		for (int i = 0; i < rows; i++) {
			m[i] = new double[cols];
		}		
		return m;
	}
		
	private void fill(double[][] m, double val) {
		for (int i = 0; i < m.length; i++) {
			Arrays.fill(m[i], val);
		}
	}
	
	/**
	 * In Matlab terms, x1 = x1 .* x2
	 * 
	 * x1 and x2 assumed to be the same length.
	 */
	private void multiply(double[] x1, double[] x2) {
		for (int i = 0; i < x1.length; i++) {
			x1[i] *= x2[i];	
		}	
	}
	
	private double max(double[] x) {
		double max = Double.MIN_VALUE;
		for (int i = 0; i < x.length; i++) {
			if (x[i] > max) {
				max = x[i];	
			}
		}
		return max;
	}
	
	private double min(double[] x) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < x.length; i++) {
			if (x[i] < min) {
				min = x[i];	
			}
		}
		return min;	
	}
	
	//
	// Utility classes
	//
	
	private class DoubleHolder {
		double value;	
	}

	private class MeHistory {

		/**
		 * The ith elements is an array of the indices of the features
		 * predicting the ith future.
		 */
		private int[][] featuresForFuture;
		
		/**
		 * The number of events sharing this history.  This includes
		 * all future values.
		 */
		private int eventsForHistory;
		
		/**
		 * After the two while loops over the events, this is equal to P~(h)
		 *
		 * historyProbability = eventsForHistory / numEvents;
		 */		
		private double historyProbability;
		
		private int historyMaxC;
		
		/**
		 * Take an array of IntArrayLists.
		 */
		public MeHistory(IntArrayList[] features) {
			
			eventsForHistory = 0;
			historyMaxC = 0;
			
			featuresForFuture = new int[numFutures][];
			for (int i = 0; i < numFutures; i++) {
				int numForFuture = features[i].size();
					
				featuresForFuture[i] = new int[numForFuture];	
				for (int j = 0; j < numForFuture; j++) {
					featuresForFuture[i][j] = features[i].get(j);	
				}
				
				if (numForFuture > historyMaxC) {
					historyMaxC = numForFuture;	
				}
			}
		}
		
		/**
		 * NOTE: we don't keep track of <code>future</code>, but it makes me 
		 * feel better to pass it along as well.
		 */
		public void addEvent(int future) {
			eventsForHistory++;
		}
		
		public int getEventCount() {
			return eventsForHistory;	
		}
		
		public int getHistoryMaxC() {
			return historyMaxC;
		}
		
		public void setHistoryProbability(double p) {
			historyProbability = p;	
		}
				
		/**
		 * P(f|h) = numerators[f] / denominator;
		 * 
		 * for each feature predicring future f, 
		 * K_j(i, c(h,f)) += P~(h) * P(f|h) * g_i(h, f)
		 * 
		 * However, g_i(h, f) is either 0 or 1, so we keep a list
		 * of the features predicting each future and loop over the list
		 * updating K_j as
		 * K_j(i, c(h,f)) += P~(h) * P(f|h) 
		 */
		public void updateExpectations(double[] alpha, double[][] expectations) {
			
			//
			// Optimization in the case where we have an example labeled hold
			// but no clues predicting hold.  Thus, this history will have no examples 
			// with valid future labels.  Thus, historyProbability
			//
			if (eventsForHistory == 0) {
				return;
			}
			
			double denominator = 0;
			double[] numerators = new double[numFutures];
			for (int i = 0; i < numFutures; i++) {
				numerators[i] = 1;
				int numForFuture = featuresForFuture[i].length;
				for (int j = 0; j < numForFuture; j++) {
					numerators[i] *= alpha[featuresForFuture[i][j]];
				}
				denominator += numerators[i];
			}
			
			double factor = historyProbability / denominator;
			for (int i = 0; i < numFutures; i++) {
				double delta = factor * numerators[i];
				int numForFuture = featuresForFuture[i].length;
				for (int j = 0; j < numForFuture; j++) {
					expectations[featuresForFuture[i][j]][numForFuture] += delta;
				}
			}	
		}
		
		public boolean equals(Object obj) {
			MeHistory h = (MeHistory)obj;
			int[][] fff = h.featuresForFuture;
			
			if (featuresForFuture.length != fff.length) {
				return false;	
			}
			for (int i = 0; i < numFutures; i++) {
				if (featuresForFuture[i].length != fff[i].length) {
					return false;	
				}
				for (int j = 0; j < featuresForFuture[i].length; j++) {
					if (featuresForFuture[i][j] != fff[i][j]) {
						return false;	
					}	
				}
			}
			return true;	
		}
	
		public int hashCode() {
			int hash = 0;
			
			for (int i = 0; i < numFutures; i++) {
				for (int j = 0; j < featuresForFuture[i].length; j++) {
					hash += (featuresForFuture[i][j] + 10) * (featuresForFuture[i][j] + 10);
				}	
			}
			
			return hash;
		}
	}
	
}
