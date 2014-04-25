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
package com.choicemaker.cm.modelmaker.stats;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.choicemaker.cm.core.ClueDesc;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Evaluator;
import com.choicemaker.cm.core.ExtDecision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.Thresholds;

/**
 * Not thread safe
 * fails for nohold rules
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/29 12:30:30 $
 */
public final class Statistics implements IStatistics {
	// types of pairs, depends upon active rules
	private static final int NONE = 0; // no rules or none rules only
	private static final int FIX = 1;
	private static final int NORMAL = 2;
	private static final int NOHOLD = 3;
	private static final int NUM_CATS = 4;

	// the source collection
	private Collection src;
	// size of src (cache)
	private int size;
	// the probability model
	private IProbabilityModel model;
	// the clue descriptors of the current model's clue set (cache)
	private ClueDesc[] clueDesc;
	// the pairs (from the source collection) ordered by type, human decision (differ, hold, match), probability increasing
	private MutableMarkedRecordPair[] pairs;
	// the current differ threshold
	private float differThreshold;
	// the current match threshold
	private float matchThreshold;
	// sort pairs presorted by type
	private Comparator simplePairComparator;
	// the confusion matrix including totals
	private int[][] confusionMatrix;
	// evaluator (cache)
	private Evaluator evaluator;
	// boundaries[record pair type][decision]
	private int[][] boundaries;
	// caches for all probability values in pairs. Used 0..numPoints-1.
	// threshold (increasing)
	private float[] threshold;
	// number of CM matches is numMatches[i] if threshold[i-1] < differThreshold <= threshold[i]
	private int[] numMatches;
	// false positive fraction is falsePositives[i] if threshold[i-1] < differThreshold <= threshold[i]
	private float[] falsePositives;
	// number of CM differs is numDiffers[i] if threshold[i-1] < differThreshold <= threshold[i]
	private int[] numDiffers;
	// false negative fraction is falseNegatives[i] if threshold[i-1] < differThreshold <= threshold[i]
	private float[] falseNegatives;
	// number of distinct different probabilities + sentinel, defines used range of above arrays.
	private int numPoints;
	// number of pairs human marked differ, hold, match
	private int[] humanCount;
	// correlation value, computed in init
	private float correlation;

	public Statistics(IProbabilityModel model, Collection src, float differThreshold, float matchThreshold) {
		this.model = model;
		this.clueDesc = model.getAccessor().getClueSet().getClueDesc();
		this.src = src;
		this.differThreshold = differThreshold;
		this.matchThreshold = matchThreshold;
		size = src.size();
	}

	// type of pair
	private int ruleType(int[] rules) {
		int size = rules.length;
		if (size == 0) {
			return NONE;
		} else {
			int fd = 7;
			for (int i = 0; i < size; ++i) {
				Decision dec = clueDesc[rules[i]].decision;
				if (dec == ExtDecision.NOHOLD) {
					return NOHOLD;
				} else if (dec == Decision.DIFFER) {
					fd &= 1;
				} else if (dec == Decision.HOLD) {
					fd &= 2;
				} else if (dec == Decision.MATCH) {
					fd &= 4;
				} else if (dec == ExtDecision.NODIFFER) {
					fd &= 6;
				} else if (dec == ExtDecision.NOMATCH) {
					fd &= 3;
				}
			}
			if (fd == 7) {
				return NONE;
			} else if (fd == 0 || fd == 1 || fd == 2 || fd == 4) {
				return FIX;
			} else {
				return NORMAL;
			}
		}
	}

	// fix if multiple at same
	// change to differ after, to match at. is asymmetry taken into account?
	private void init() {
		if (pairs == null) {
			// Correlation
			// x: human probability
			// y: CM probability
			double sXY = 0;
			double uX = 0;
			double uY = 0;
			double sX2 = 0;
			double sY2 = 0;

			simplePairComparator = new SimplePairComparator();
			MutableMarkedRecordPair[][] p = new MutableMarkedRecordPair[NUM_CATS][size];
			int[] s = new int[NUM_CATS];
			Iterator iSrc = src.iterator();
			for (int i = 0; i < size; ++i) {
				MutableMarkedRecordPair pair = (MutableMarkedRecordPair) iSrc.next();
				int cat = ruleType(pair.getActiveClues().getRules());
				p[cat][s[cat]++] = pair;
				// Correlation
				float hp = (pair.getMarkedDecision() == Decision.DIFFER ? 0f : (pair.getMarkedDecision() == Decision.HOLD ? 0.5f : 1f));
				float cp = pair.getProbability();
				sXY += hp * cp;
				uX += hp;
				uY += cp;
				sX2 += hp * hp;
				sY2 += cp * cp;
			}
			// correlation
			uX /= size;
			uY /= size;
			double rXY = sXY / size - uX * uY;
			double rX = Math.sqrt(sX2 / size - uX * uX);
			double rY = Math.sqrt(sY2 / size - uY * uY);
			correlation = (float) (rXY / rX / rY);

			boundaries = new int[NUM_CATS][Decision.NUM_DECISIONS + 1];
			for (int i = 0; i < NUM_CATS; ++i) {
				Arrays.sort(p[i], 0, s[i], simplePairComparator);
				setBoundaries(p[i], 0, s[i], boundaries[i]);
			}
			pairs = p[0];
			int l = s[0];
			for (int i = 1; i < NUM_CATS; ++i) {
				System.arraycopy(p[i], 0, pairs, l, s[i]);
				int[] b = boundaries[i];
				for (int j = 0; j <= Decision.NUM_DECISIONS; ++j) {
					b[j] += l;
				}
				l += s[i];
			}

			humanCount = new int[Decision.NUM_DECISIONS];
			for (int i = 0; i < NUM_CATS; ++i) {
				for (int j = 0; j < Decision.NUM_DECISIONS; ++j) {
					int[] b = boundaries[i];
					humanCount[j] += b[j + 1] - b[j];
				}
			}

			p = null;
			l = boundaries[NONE][3] + boundaries[NORMAL][3] - boundaries[NORMAL][0] + 1;
			threshold = new float[l + 1];
			numMatches = new int[l + 1];
			falsePositives = new float[l + 1];
			numDiffers = new int[l + 1];
			falseNegatives = new float[l + 1];
			Evaluator evaluator = getEvaluator();

			// NONE
			int numMatches0 = boundaries[NONE][3] - boundaries[NONE][0];
			int numDiffers0 = 0;
			int fp = boundaries[NONE][2] - boundaries[NONE][0];
			int fn = 0;
			// FIX
			int end = boundaries[FIX][3];
			for (int i = boundaries[FIX][0]; i < end; ++i) {
				MutableMarkedRecordPair pa = pairs[i];
				if (pa.getCmDecision() == Decision.DIFFER) {
					++numDiffers0;
					if (pa.getMarkedDecision() != Decision.DIFFER) {
						++fn;
					}
				} else if (pa.getCmDecision() == Decision.MATCH) {
					++numMatches0;
					if (pa.getMarkedDecision() != Decision.MATCH) {
						++fp;
					}
				}
			}
			// NORMAL
			end = boundaries[NORMAL][3];
			for (int i = boundaries[NORMAL][0]; i < end; ++i) {
				MutableMarkedRecordPair pa = pairs[i];
				Decision cmDecision = evaluator.getDecision(pa.getActiveClues(), pa.getProbability(), -1, -1);
				if (cmDecision == Decision.MATCH) {
					++numMatches0;
					if (pa.getMarkedDecision() != Decision.MATCH) {
						++fp;
					}
				} else if (cmDecision == Decision.DIFFER) {
					++numDiffers0;
					if (pa.getMarkedDecision() != Decision.DIFFER) {
						++fn;
					}
				}
			}

			numMatches[0] = numMatches0;
			numDiffers[0] = numDiffers0;

			int[][] pos = new int[3][Decision.NUM_DECISIONS];
			for (int c = 0; c < 3; c += 2) {
				for (int d = 0; d < Decision.NUM_DECISIONS; ++d) {
					pos[c][d] = boundaries[c][d];
				}
			}
			int minCat = 0;
			int minDec = 0;
			int curPos = 0;
			float lastThreshold = 0;
			for (int i = 1; i < l; ++i) {
				float min = Float.MAX_VALUE;
				for (int c = 0; c < 3; c += 2) {
					for (int d = 0; d < Decision.NUM_DECISIONS; ++d) {
						int tpos = pos[c][d];
						if (tpos < boundaries[c][d + 1] && pairs[tpos].getProbability() < min) {
							minCat = c;
							minDec = d;
							min = pairs[tpos].getProbability();
						}
					}
				}
				MutableMarkedRecordPair pa = pairs[pos[minCat][minDec]++];
				if (min != lastThreshold) {
					lastThreshold = threshold[curPos] = min;
					falseNegatives[curPos] = numDiffers[curPos] == 0 ? 0f : fn / (float) numDiffers[curPos];
					falsePositives[curPos] = numMatches[curPos] == 0 ? 0f : fp / (float) numMatches[curPos];
					++curPos;
					numDiffers[curPos] = numDiffers[curPos - 1];
					numMatches[curPos] = numMatches[curPos - 1];
				}
				if (minCat == NONE) {
					++numDiffers[curPos];
					if (pa.getMarkedDecision() != Decision.DIFFER) {
						++fn;
					}
					--numMatches[curPos];
					if (pa.getMarkedDecision() != Decision.MATCH) {
						--fp;
					}
				} else { // minCat == NORMAL
					Decision cmDecision = evaluator.getDecision(pa.getActiveClues(), 0, 1, 1);
					if (cmDecision == Decision.DIFFER) {
						++numDiffers[curPos];
						if (pa.getMarkedDecision() != Decision.DIFFER) {
							++fn;
						}
					}
					cmDecision = evaluator.getDecision(pa.getActiveClues(), 0, 0, 0);
					if (cmDecision == Decision.MATCH) {
						--numMatches[curPos];
						if (pa.getMarkedDecision() != Decision.MATCH) {
							--fp;
						}
					}
				}
			}
			falseNegatives[curPos] = numDiffers[curPos] == 0 ? 0f : fn / (float) numDiffers[curPos];
			falsePositives[curPos] = numMatches[curPos] == 0 ? 0f : fp / (float) numMatches[curPos];
			threshold[curPos] = 1;
			numPoints = curPos + 1;
		}
	}

	private void setBoundaries(ImmutableMarkedRecordPair[] p, int from, int to, int[] b) {
		b[0] = from;
		b[3] = to;
		int i = from;
		while (i < to && p[i].getMarkedDecision() == Decision.DIFFER) {
			++i;
		}
		b[1] = i;
		while (i < to && p[i].getMarkedDecision() == Decision.HOLD) {
			++i;
		}
		b[2] = i;
	}

	private Evaluator getEvaluator() {
		if (evaluator == null) {
			evaluator = model.getEvaluator();
		}
		return evaluator;
	}

	// don't compute in init() so that we don't have to re-run init upon change of threshold
	public int[][] getConfusionMatrix() {
		if (confusionMatrix == null) {
			init();
			confusionMatrix = new int[Decision.NUM_DECISIONS + 1][Decision.NUM_DECISIONS + 1];
			// NONE
			for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
				int hPos = find(boundaries[NONE][i], boundaries[NONE][i + 1], differThreshold);
				if (hPos < pairs.length
					&& pairs[hPos].getProbability() <= differThreshold
					&& hPos < boundaries[NONE][i + 1])
					++hPos;
				int mPos = find(hPos, boundaries[NONE][i + 1], matchThreshold);
				if (mPos < pairs.length && pairs[mPos].getProbability() <= matchThreshold && mPos < boundaries[NONE][i + 1])
					++mPos;
				int dd = i == 0 ? 0 : (i == 1 ? 2 : 1);
				confusionMatrix[dd][Decision.DIFFER.toInt()] += hPos - boundaries[NONE][i];
				confusionMatrix[dd][Decision.HOLD.toInt()] += mPos - hPos;
				confusionMatrix[dd][Decision.MATCH.toInt()] += boundaries[NONE][i + 1] - mPos;
			}
			// FIX, NORMAL, NOHOLD
			for (int i = boundaries[FIX][0]; i < pairs.length; ++i) {
				MutableMarkedRecordPair p = pairs[i];
				++confusionMatrix[p.getMarkedDecision().toInt()][p.getCmDecision().toInt()];
			}
			for (int i = 0; i < Decision.NUM_DECISIONS; ++i) {
				for (int j = 0; j < Decision.NUM_DECISIONS; ++j) {
					confusionMatrix[Decision.NUM_DECISIONS][i] += confusionMatrix[j][i];
					confusionMatrix[i][Decision.NUM_DECISIONS] += confusionMatrix[i][j];
				}
				confusionMatrix[Decision.NUM_DECISIONS][Decision.NUM_DECISIONS]
					+= confusionMatrix[i][Decision.NUM_DECISIONS];
			}
		}
		return confusionMatrix;
	}

	public StatPoint getCurrentStatPoint() {
		StatPoint res = new StatPoint();
		res.differThreshold = differThreshold;
		res.matchThreshold = matchThreshold;
		computeStatPoint(res);
		int[][] cm = getConfusionMatrix();
		// 2006-01-02 rphall
		// HACK this calculation should be fixed in computeStatPoint, not here
		if (cm[3][0] != 0) {
			res.falseNegatives = (cm[1][0] + cm[2][0]) / (float) cm[3][0];
		} else {
			res.falseNegatives = Float.NaN;
		}
		if (cm[3][1] != 0) {
			res.falsePositives = (cm[0][1] + cm[2][1]) / (float) cm[3][1];
		} else {
			res.falsePositives = Float.NaN;
		}
		if (cm[0][3] != 0) {
			res.differRecall = cm[0][0] / (float) cm[0][3];
		} else {
			res.differRecall = Float.NaN;
		}
		if (cm[1][3] != 0) {
			res.matchRecall = cm[1][1] / (float) cm[1][3];
		} else {
			res.matchRecall = Float.NaN;
		}
		if (cm[3][3] != 0) {
			res.humanReview = cm[3][2] / (float) cm[3][3];
		} else {
			res.humanReview = Float.NaN;
		}
		// ENDHACK
		res.precision = (cm[0][0] + cm[1][1] + cm[2][2]) / (float) cm[3][3];
		res.recall = res.precision;
		res.correlation = correlation;
		return res;
	}

	// differ = 0, hold = 1, match = 2
	// temporary fix while Decision.toInt() broken
	private int decisionToInt(Decision dec) {
		if (dec == Decision.DIFFER) {
			return 0;
		} else if (dec == Decision.MATCH) {
			return 2;
		} else {
			return 1;
		}
	}

	public void computeStatPoint(StatPoint pt) {
		init();
		int dPos = -1;
		int mPos = -1;
		if (!Float.isNaN(pt.differThreshold)) {
			dPos = thresholdToIdx(pt.differThreshold);
		} else if (!Float.isNaN(pt.falseNegatives)) {
			dPos = falseNegativesToIdx(pt.falseNegatives);
		}
		if (!Float.isNaN(pt.matchThreshold)) {
			mPos = thresholdToIdx(pt.matchThreshold);
		} else if (!Float.isNaN(pt.falsePositives)) {
			mPos = falsePositivesToIdx(pt.falsePositives);
		}
		if (dPos == -1) {
			if (mPos == -1) {
				if (!Float.isNaN(pt.humanReview)) {
					int[] res = humanReviewToIdxs(pt.humanReview);
					dPos = res[0];
					mPos = res[1];
				}
			} else {
				if (!Float.isNaN(pt.humanReview)) {
					dPos = humanReviewMatchToIdx(mPos, pt.humanReview);
				}
			}
		} else {
			if (!Float.isNaN(pt.humanReview)) {
				mPos = humanReviewDifferToIdx(dPos, pt.humanReview);
			}
		}
		if (dPos != -1) {
			pt.falseNegatives = falseNegatives[dPos];
			pt.differThreshold = threshold[dPos];
			pt.differRecall = getDifferRecall(dPos);
		}
		if (mPos != -1) {
			pt.falsePositives = falsePositives[mPos];
			pt.matchThreshold = threshold[mPos];
			pt.matchRecall = getMatchRecall(mPos);
		}
		pt.humanReview = humanReview(dPos, mPos);
	}

	private int humanReviewDifferToIdx(int dPos, float hr) {
		int nm = (int) ((1 - hr) * size - numDiffers[dPos] + 1);
		if (numMatches[dPos] < nm) {
			return -1;
		} else {
			int low = dPos;
			int high = numPoints - 1;
			while (low <= high) {
				int mid = (low + high) / 2;
				int midVal = numMatches[mid];
				if (midVal > nm) {
					low = mid + 1;
				} else if (midVal < nm) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}
			return -1;
		}
	}

	private int humanReviewMatchToIdx(int mPos, float hr) {
		int nd = (int) ((1 - hr) * size - numMatches[mPos] + 1);
		if (numDiffers[mPos] < nd) {
			return -1;
		} else {
			int low = 0;
			int high = mPos;
			while (low <= high) {
				int mid = (low + high) / 2;
				int midVal = numDiffers[mid];
				if (midVal < nd) {
					low = mid + 1;
				} else if (midVal > nd) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}
			return -1;
		}
	}

	private int[] humanReviewToIdxs(float hr) {
		return humanReviewToIdxs(hr, numPoints - 1, 0);
	}

	private int[] humanReviewToIdxs(float hr, int dPos, int mPos) {
		int[] res = new int[2];
		res[0] = -1;
		res[1] = -1;
		if (humanReview(dPos, mPos) > hr) {
			//System.out.println("A");
			return res;
		} else {
			int end1 = numPoints - 1;
			while (true) {
				if (falseNegatives[dPos] > falsePositives[mPos]) {
					if (dPos > mPos || dPos > 0 && humanReview(dPos - 1, mPos) <= hr) {
						--dPos;
					} else {
						while (mPos < end1 && humanReview(dPos, mPos + 1) <= hr)
							++mPos;
						if (humanReview(dPos, mPos) <= hr) {
							res[0] = dPos;
							res[1] = mPos;
						}
						// System.out.println("B");
						return res;
					}
				} else {
					if (dPos > mPos || mPos < end1 && humanReview(dPos, mPos + 1) <= hr) {
						++mPos;
					} else {
						while (dPos > 0 && humanReview(dPos - 1, mPos) <= hr)
							--dPos;
						if (humanReview(dPos, mPos) <= hr) {
							res[0] = dPos;
							res[1] = mPos;
						}
						//System.out.println("C");
						return res;
					}
				}
			}
		}
	}

	private float humanReview(int dPos, int mPos) {
		if (dPos != -1 && mPos != -1) {
			return size == 0 ? 0f : Math.max((float) (size - numDiffers[dPos] - numMatches[mPos]) / (float) size, 0f);
		} else {
			return Float.NaN;
		}
	}

	// pre:  t >= 0
	// post: threshold[res] <= t && (res == numPoints - 1 || t < threshold[res + 1])
	private int thresholdToIdx(float t) {
		int low = 0;
		int high = numPoints - 1;
		while (low < high) {
			int mid = (low + high) / 2;
			float midVal = threshold[mid];
			if (midVal < t) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		return high;
	}

	private int falseNegativesToIdx(float fn) {
		return falseNegativesToIdx(fn, numPoints - 1);
	}

	// linear search
	// falseNegatives[res] <= fn && all(i : res < i <= startpos => falseNegatives[i] > fn)
	// res == - 1 if (all i : 0 < i <= startpos => falseNegatives[i] > fn)
	private int falseNegativesToIdx(float fn, int startpos) {
		int i = startpos;
		while (i >= 0 && falseNegatives[i] > fn) {
			--i;
		}
		return i;
	}

	private int falsePositivesToIdx(float fn) {
		return falsePositivesToIdx(fn, 0);
	}

	// dual to falseNegativesToIdx
	private int falsePositivesToIdx(float fn, int startpos) {
		int len = numPoints;
		int i = startpos;
		while (i < len && falsePositives[i] > fn) {
			++i;
		}
		return i == len ? -1 : i;
	}

	private float getDifferRecall(int pos) {
		return numDiffers[pos] * (1 - falseNegatives[pos]) / humanCount[0];
	}

	private float getMatchRecall(int pos) {
		return numMatches[pos] * (1 - falsePositives[pos]) / humanCount[2];
	}

	private static final int MAX_NUM_POINTS = 100;

	public float[][] getThresholdVsAccuracy() {
		init();
		int np = numPoints <= MAX_NUM_POINTS ? numPoints : MAX_NUM_POINTS;
		float[][] res = new float[np][5];
		float step = numPoints / (float) np;
		float val = 0;
		for (int i = 0; i < np; ++i) {
			int pos = (int) val;
			float[] p = res[i];
			p[0] = threshold[pos];
			p[1] = falseNegatives[pos];
			p[2] = getDifferRecall(pos);
			p[3] = falsePositives[pos];
			p[4] = getMatchRecall(pos);
			if (i == np - 2) {
				val = numPoints - 1;
			} else {
				val += step;
			}
		}
		return res;
	}

	public float[][] getHoldPercentageVsAccuracy(float[] errorRates) {

		/*
		for (int i = 0; i < errorRates.length; i++) {
			System.out.println("errorRates[" + i + "]: " + errorRates[i]);
		}
		*/

		Calendar rightNow = Calendar.getInstance();
		double startTime = rightNow.getTimeInMillis();
		System.out.println("=====Time at start: " + startTime);


		init();
		int np = errorRates.length;
		float[][] res = new float[np][4];
		int mPos = 0;
		int dPos = numPoints - 1;
		int maxAssignedPos = -1;
		for (int i = 0; i < np; ++i) { // contains break
			float errorRate = errorRates[i];
			int dPos1;
			int mPos1;
			if (mPos != -1)
				mPos = falsePositivesToIdx(errorRate, mPos);
			if (dPos != -1)
				dPos = falseNegativesToIdx(errorRate, dPos);
			if (dPos != -1 && mPos != -1 && dPos > mPos) {
				if (mPos > maxAssignedPos || maxAssignedPos > dPos) {
					int maxAssigned = -1;
					for (int j = mPos; j <= dPos; ++j) {
						int assigned = numDiffers[j] + numMatches[j];
						if (assigned > maxAssigned) {
							maxAssigned = assigned;
							maxAssignedPos = j;
						}
					}
				}
				dPos1 = mPos1 = maxAssignedPos;
			} else {
				dPos1 = dPos;
				mPos1 = mPos;
			}
			float[] p = res[i];
			p[0] = errorRates[i];
			p[1] = humanReview(dPos1, mPos1);
			p[2] = dPos == -1 ? Float.NaN : threshold[dPos1];
			p[3] = mPos == -1 ? Float.NaN : threshold[mPos1];
		}


		Calendar rightNow2 = Calendar.getInstance();

		double finishTime = rightNow2.getTimeInMillis();
		System.out.println("=====Time at finish: " + finishTime);

		double timeTaken = finishTime - startTime;
		System.out.println("=====Time Taken: " + timeTaken);


		return res;
	}

	public int[][] getHistogram(int numBins) {
		init();
		int[][] res = new int[Decision.NUM_DECISIONS][numBins];
		int[][] prev = new int[NUM_CATS][Decision.NUM_DECISIONS];
		for (int j = 0; j < NUM_CATS; ++j) {
			for (int k = 0; k < Decision.NUM_DECISIONS; ++k) {
				prev[j][k] = boundaries[j][k];
			}
		}
		float step = 1f / numBins;
		float to = 0;
		for (int i = 0; i < numBins; ++i) {
			if (i + 1 < numBins) {
				to = to + step;
			} else {
				to = 1; // avoid rounding errors
			}
			for (int j = 0; j < NUM_CATS; ++j) {
				for (int k = 0; k < Decision.NUM_DECISIONS; ++k) {
					int pos;
					if (to == 1) {
						pos = boundaries[j][k + 1];
					} else {
						pos = find(prev[j][k], boundaries[j][k + 1], to);
						if (pos < pairs.length && pairs[pos].getProbability() <= to && pos < boundaries[j][k + 1])
							++pos;
					}
					res[k][i] += pos - prev[j][k];
					prev[j][k] = pos;
				}
			}
		}
		return res;
	}

	// threshold[res] <= t && (t == to || threshold[res + 1] > t)
	private int find(int from, int to, float t) {
		int low = from;
		int high = to;
		while (low < high) {
			int mid = (low + high) / 2;
			float midVal = pairs[mid].getProbability();
			if (midVal < t) {
				low = mid + 1;
			} else {
				high = mid;
			}
		}
		if (high > from && high < pairs.length && pairs[high].getProbability() > t) {
			return high - 1;
		} else {
			return high;
		}
	}

	public void setThresholds(Thresholds t) {
		differThreshold = t.getDifferThreshold();
		matchThreshold = t.getMatchThreshold();
		confusionMatrix = null;
	}
}
