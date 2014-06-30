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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.train.Trainer;
import com.choicemaker.cm.core.util.OperationFailedException;
import com.choicemaker.cm.core.util.StringUtils;
import com.choicemaker.cm.modelmaker.stats.Statistics;

/**
 * @author ajwinkel
 *
 */
public class CrossValidator {

	private static final float[] ERROR_RATES = 
		{.05f, .04f, .03f, .02f, .015f, .01f, .008f, .006f, .005f, .004f, .003f, .002f, .001f, 0f};

	private IProbabilityModel model;
	private float differThreshold, matchThreshold;

	private List pairs;
	private int numPieces;

	private FoldResult[] foldResults;
	private CrossValidationResult cvResult;
	
	public CrossValidator(IProbabilityModel model, float differThreshold, float matchThreshold, List pairs, int numPieces) {
		this.model = model;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;

		this.pairs = pairs;
		this.numPieces = numPieces;

		if (model == null || model.getMachineLearner() == null || !model.getMachineLearner().canTrain()) {
			throw new IllegalArgumentException("Illegal model");
		} else if (pairs == null || pairs.size() < 2) {
			throw new IllegalArgumentException("Null, empty, or nearly empty pair list");
		} else if (numPieces < 2) {
			throw new IllegalArgumentException("num folds must be at least 2: " + numPieces);
		}
	}

	public void run() {
		cvResult = null;
		foldResults = null;
		
		int maxPieceSize = (int) (0.5 + pairs.size() / (float)numPieces);
		
		// split the list into many pieces
		List[] folds = new List[numPieces];
		for (int i = 0; i < numPieces; i++) {
			folds[i] = new ArrayList(maxPieceSize);
		}
		for (int i = 0; i < pairs.size(); i++) {
			folds[i % numPieces].add(pairs.get(i));
		}
		
		// for each piece...
		foldResults = new FoldResult[numPieces];
		for (int i = 0; i < numPieces; i++) {
			List test = folds[i];
			List train = getAllPiecesBut(folds, i);
			
			foldResults[i] = doFold(train, test);
		}
		
		cvResult = new CrossValidationResult(foldResults);
	}

	private static DecimalFormat df = new DecimalFormat("##0.00");

	public void printResults() {
		int numResults = foldResults.length;
		int frSize = ERROR_RATES.length;
		
		FoldResult fr0 = foldResults[0];
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(fr0.getAccuracy(i) * 100), 8, ' '));			
		}
		System.out.println();
		for (int i = 0; i < frSize; i++) {
			System.out.print("--------");			
		}
		System.out.println();
		
		for (int j = 0; j < numResults; j++) {
			FoldResult frj = foldResults[j];
			for (int i = 0; i < frSize; i++) {
				System.out.print(StringUtils.padLeft(df.format(frj.getHumanReviewPercent(i) * 100), 8, ' '));
			}
			System.out.println();
		}
		
		System.out.println("\nAverage Human Review Percentages");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.getHumanReviewAvg(i) * 100), 8, ' '));
		}
		System.out.println("\nAverage Differ Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.getDifferThresholdAvg(i) * 100), 8, ' '));
		}
		System.out.println("\nAverage Match Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.getMatchThresholdAvg(i) * 100), 8, ' '));
		}
		System.out.println();


		System.out.println("\nMin Human Review Percentages");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.humanReviewMins[i] * 100), 8, ' '));
		}
		System.out.println("\nMax Differ Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.differThresholdMaxes[i] * 100), 8, ' '));
		}
		System.out.println("\nMin Match Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.matchThresholdMins[i] * 100), 8, ' '));
		}
		System.out.println();


		System.out.println("\nMax Human Review Percentages");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.humanReviewMaxes[i] * 100), 8, ' '));
		}
		System.out.println("\nMin Differ Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.differThresholdMins[i] * 100), 8, ' '));
		}
		System.out.println("\nMax Match Thresholds");
		for (int i = 0; i < frSize; i++) {
			System.out.print(StringUtils.padLeft(df.format(cvResult.matchThresholdMaxes[i] * 100), 8, ' '));
		}
		System.out.println();

	}

	private FoldResult doFold(List train, List test) {
		Trainer t = new Trainer(differThreshold, matchThreshold);
		t.setModel(model);

		t.setSource(train);		
		try {
			t.train();
		} catch (OperationFailedException ex) {
			ex.printStackTrace();
		}
		
		t.setSource(test);
		try {
			t.test();
		} catch (OperationFailedException ex) {
			ex.printStackTrace();
		}
		
		Statistics stats = new Statistics(model, test, differThreshold, matchThreshold);
		float[][] reviewVsAccuracy = stats.getHoldPercentageVsAccuracy(ERROR_RATES);
		
		return new FoldResult(reviewVsAccuracy);
	}


	private List getAllPiecesBut(List[] folds, int but) {
		int size = 0;
		for (int i = 0; i < folds.length; i++) {
			if (i != but) {
				size += folds[i].size();
			}
		}
		
		List allBut = new ArrayList(size);
		for (int i = 0; i < folds.length; i++) {
			if (i != but) {
				allBut.addAll(folds[i]);
			}
		}
		
		return allBut;
	}
	
	public static class FoldResult {
		private float[][] reviewVsAccuracy;
		
		/**
		 * See the Statistics object for more information about the structure of reviewVsAccuracy
		 */
		public FoldResult(float[][] reviewVsAccuracy) {
			this.reviewVsAccuracy = reviewVsAccuracy;
		}
		public int getNumPoints() {
			return reviewVsAccuracy.length;
		}
		public float getErrorTolerance(int i) {
			return reviewVsAccuracy[i][0];
		}
		public float getAccuracy(int i) {
			return 1 - reviewVsAccuracy[i][0];
		}
		public float getHumanReviewPercent(int i) {
			return reviewVsAccuracy[i][1];
		}
		public float getDifferThreshold(int i) {
			return reviewVsAccuracy[i][2];
		}
		public float getMatchThreshold(int i) {
			return reviewVsAccuracy[i][3];
		}
	}
	
	public static class CrossValidationResult {
		private FoldResult[] frs;
		private int numFrs;
		private int numPoints;
		
		private float[] errorTolerances;
		
		private float[][] humanReviewPercentages;
		private float[][] differThresholds;
		private float[][] matchThresholds;
		
		private float[] humanReviewAverages;
		private float[] differThresholdAverages;
		private float[] matchThresholdAverages;
		
		private float[] humanReviewMins;
		private float[] differThresholdMins;
		private float[] matchThresholdMins;

		private float[] humanReviewMaxes;
		private float[] differThresholdMaxes;
		private float[] matchThresholdMaxes;
		
		public CrossValidationResult(FoldResult[] frs) { 
			if (frs == null || frs.length == 0) {
				throw new IllegalArgumentException("FoldResults must be non-null");
			}
			
			this.frs = frs;
			this.numFrs = frs.length;
			this.numPoints = frs[0].getNumPoints();
			
			try {
				init();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new IllegalArgumentException("Unknown exception...");
			}
		}
		
		private void init() throws Exception {
			errorTolerances = getColumnAverages(getPropertyMatrix(frs, "errorTolerance"));
			humanReviewPercentages = getPropertyMatrix(frs, "humanReviewPercent");
			differThresholds = getPropertyMatrix(frs, "differThreshold");
			matchThresholds = getPropertyMatrix(frs, "matchThreshold");
			
			humanReviewAverages = getColumnAverages(humanReviewPercentages);
			differThresholdAverages = getColumnAverages(differThresholds);
			matchThresholdAverages = getColumnAverages(matchThresholds);

			humanReviewMins = getColumnMins(humanReviewPercentages);
			differThresholdMins = getColumnMins(differThresholds);
			matchThresholdMins = getColumnMins(matchThresholds);

			humanReviewMaxes = getColumnMaxes(humanReviewPercentages);
			differThresholdMaxes = getColumnMaxes(differThresholds);
			matchThresholdMaxes = getColumnMaxes(matchThresholds);
		}
		
		public int getNumFolds() {
			return numFrs;
		}
		public int getNumPoints() {
			return numPoints;
		}
		public float getErrorTolerance(int pt) {
			return errorTolerances[pt];
		}
		public float getAccuracy(int pt) {
			return 1 - errorTolerances[pt];
		}
		
		public float getHumanReviewAvg(int pt) {
			return humanReviewAverages[pt];
		}
		public float getDifferThresholdAvg(int pt) {
			return differThresholdAverages[pt];
		}
		public float getMatchThresholdAvg(int pt) {
			return matchThresholdAverages[pt];
		}
		
		public static float[] getRow(float[][] m, int row) {
			return m[row];
		}
				
		public static float[] getColumn(float[][] m, int col) {
			int numRows = m.length;
			float[] v = new float[numRows];
			for (int i = 0; i < numRows; i++) {
				v[i] = m[i][col];
			}
			return v;
		}
		
		public static float[] getColumnMins(float[][] m) {
			int numCols = m[0].length;
			float[] mins = new float[numCols];
			for (int i = 0; i < numCols; i++) {
				mins[i] = min(getColumn(m, i));
			}
			return mins;			
		}

		public static float[] getColumnMaxes(float[][] m) {
			int numCols = m[0].length;
			float[] maxes = new float[numCols];
			for (int i = 0; i < numCols; i++) {
				maxes[i] = max(getColumn(m, i));
			}
			return maxes;			
		}
		
		public static float[] getColumnAverages(float[][] m) {
			int numCols = m[0].length;
			float[] avgs = new float[numCols];
			for (int i = 0; i < numCols; i++) {
				avgs[i] = getColumnAverage(m, i);
			}
			return avgs;
		}
		
		public static float getColumnAverage(float[][] m, int col) {
			return average(getColumn(m, col));
		}
		
		public static float getColumnAverageRemoveHiAndLo(float[][] m, int col) {
			return averageRemoveHiAndLo(getColumn(m, col));
		}
		
		public static float max(float[] v) {
			float max = -Float.MAX_VALUE;
			for (int i = v.length - 1; i >= 0; i--) {
				if (v[i] > max) {
					max = v[i];
				}
			}
			return max;
		}

		public static float min(float[] v) {
			float min = Float.MAX_VALUE;
			for (int i = v.length - 1; i >= 0; i--) {
				if (v[i] < min) {
					min = v[i];
				}
			}
			return min;
		}

		
		public static float average(float[] v) {
			float total = 0f;
			for (int i = v.length - 1; i >= 0; i--) {
				total += v[i];
			}
			return total / v.length;
		}
		
		public static float averageRemoveHiAndLo(float[] v) {
			float lo = Float.MAX_VALUE;
			float hi = -Float.MAX_VALUE;
			float total = 0;
			for (int i = v.length - 1; i >= 0; i--) {
				float vi = v[i];
				total += vi;
				if (vi < lo) {
					vi = lo;
				}
				if (vi > hi) {
					vi = hi;
				}
			}
			
			return (total - lo - hi) / v.length;
		}
		
		public static float[][] getPropertyMatrix(FoldResult[] frs, String pName) throws Exception {
			Method getter = getGetter(FoldResult.class, pName);

			int numRows = frs.length;
			int numCols = frs[0].getNumPoints();
			float[][] ret = new float[numRows][numCols];
			
			for (int i = 0; i < numRows; i++) {
				for (int j = 0; j < numCols; j++) {
					ret[i][j] = ((Float)getter.invoke(frs[i], new Object[] {new Integer(j)})).floatValue();
				}
			}
			
			return ret;
		}
		
		public static float[] getPropertyArray(FoldResult[] frs, String pName, int pt) throws Exception {
			return getPropertyMatrix(frs, pName)[pt];
		}
		
		public static Method getGetter(Class cls, String pName) throws NoSuchMethodException {
			String mName = "get" + Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
			return cls.getMethod(mName, new Class[] {Integer.TYPE});
		}
		
	}

}
