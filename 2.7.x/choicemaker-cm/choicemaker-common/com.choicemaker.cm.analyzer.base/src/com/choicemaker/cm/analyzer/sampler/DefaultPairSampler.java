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
package com.choicemaker.cm.analyzer.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.choicemaker.cm.core.ActiveClues;
import com.choicemaker.cm.core.BooleanActiveClues;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.MutableMarkedRecordPair;
import com.choicemaker.cm.core.util.IntArrayList;
import com.choicemaker.cm.core.util.IntValuedHashMap;

/**
 * A default class that samples from a large number of pairs to create a set that
 * is suitable for marking.
 *
 * This class (and any PairSampler) is used in the following way:
 * - A sampler is created and instructed to sample x number of pairs (on the order of 100 or 1000).
 * - A large number of pairs (potentially millions) are produced, perhaps (but not necessarily) from blocking, and
 * 	passed to the PairSampler one at a time.
 * - The sampler gets to see each pair exactly once, as it almost certainly cannot hold more than a few hundred
 *  thousand pairs in memory at a time.
 * - The sampler does not know the total number of pairs it will see, only that at any time it should be
 *  ready to return its target number of pairs which, as a whole, are interesting.
 *
 * The following characteristics of a set of pairs to mark are desirable:
 * - variety of combinations of active clues
 * - variety of different records (a single record shouldn't be involved more than once or twice)
 * 		- this facilitates the above, but also facilitates parsing/normalization testing
 * - oversampling of pairs that make rarely-firing clues fire
 * - &quot;even&quot; distribution of probabilities (if the model has already been trained)
 *
 * @author Adam Winkel
 */
public class DefaultPairSampler implements PairSampler {

	private static final Random random = new Random();

	private static final int DEFAULT_NUM_BUCKETS = 20;
	private static final int DEFAULT_BULGE_FACTOR = 10;

	private static final double LOG2 = Math.log(2);

	private final IProbabilityModel model;
	private final ClueSet clueSet;
	private final boolean[] enabledClues;
	private final int target;

	private boolean initialized = false;

	private List retained;

	private int numSeen;
	private int numRetained;

	private int modCount = 0;

	private IntValuedHashMap activeCluesCounts;
	private IntValuedHashMap idCounts;

	private int numClues;
	private int[] totalClueFirings;
	private int[] retainedClueFirings;

	private int numProbabilityBuckets;
	private double probabilityBucketWidth;
	private int[] bucketTargets;

	private int[] totalProbabilityBuckets;
	private int[] retainedProbabilityBuckets;

	/**
	 * Create a DefaultPairSampler with the specified IProbabilityModel and target number of pairs to retain.
	 */
	public DefaultPairSampler(IProbabilityModel model, int target) {
		this.model = model;
		this.clueSet = model.getClueSet();
		this.enabledClues = model.getCluesToEvaluate();
		this.target = target;

		// Postconditions
		if (this.model == null) {
			throw new IllegalArgumentException("null model");
		}
		if (target <= 0) {
			throw new IllegalArgumentException("non-positive target: '" + target + "'");
		}

	}

	/**
	 * Returns the target number of pairs we are to sample.
	 */
	public int getTarget() {
		return target;
	}

	/**
	 * Returns the number of pairs that have been retained to this point.
	 */
	public int getNumRetained() {
		return numRetained;
	}

	/**
	 * Returns the number of unique records that are in the retained pairs.
	 * In general, the more unique records that make up the pairs, the better, though
	 * that is not always true.
	 */
	public int getNumUniqueRetainedRecords() {
		return idCounts.size();
	}

	/**
	 * Returns the number of unique active clue patterns caused by the retained pairs.
	 */
	public int getNumUniqueActiveClues() {
		return activeCluesCounts.size();
	}

	/**
	 * Returns a List containing the retained pairs.
	 */
	public List getRetainedPairs() {
		List ret = null;
		if (retained!=null) {
			ret = new ArrayList(retained.size());
			for (int i = 0, n = retained.size(); i < n; i++) {
				ret.add(((MrpWrapper)retained.get(i)).pair);
			}
		} else {
			ret = new ArrayList();
		}
		return ret;
	}

	/**
	 * Returns the indices of the retained pairs.
	 *
	 * The returned values are the order in which each retained pair was seen.
	 */
	public int[] getRetainedIndices() {
		IntArrayList indices = new IntArrayList();
		for (Iterator it = retained.iterator(); it.hasNext(); ) {
			indices.add(((MrpWrapper)it.next()).index);
		}
		indices.sort();
		return indices.toArray();
	}

	/**
	 * Process an incoming pair.
	 */
	public void processPair(MutableMarkedRecordPair pair) {
		maybeInit();

		// Check whether the pair has active clues computed
		if (pair.getActiveClues() == null) {
			pair.setActiveClues(this.clueSet.getActiveClues(pair.getQueryRecord(), pair.getMatchRecord(), this.enabledClues));
		}

		float score = getScore(pair, false);
		if (getNumRetained() < getTarget()) {
			retained.add(new MrpWrapper(pair, numSeen, score));
			addPairToStats(pair);
		} else if (shouldRetainPair(score)) {
			MutableMarkedRecordPair removed =
				exchangeForRandom(new MrpWrapper(pair, numSeen, score));
			removePairFromStats(removed);
			addPairToStats(pair);

			Collections.sort(retained);
		} else {
			passOverPair(pair);
		}
	}

	private boolean shouldRetainPair(float score) {
		int randomPos = getRandomPosition();

		MrpWrapper wrapper = (MrpWrapper)retained.get(randomPos);
		return score > wrapper.score;
	}

	private MutableMarkedRecordPair exchangeForRandom(MrpWrapper newPair) {
		int randomPos = getRandomPosition();

		MrpWrapper oldWrapper = (MrpWrapper)retained.get(randomPos);
		retained.set(randomPos, newPair);

		return oldWrapper.pair;
	}

	private int getRandomPosition() {
		float f = random.nextFloat();
		f *= random.nextFloat();
		f *= random.nextFloat();
		f *= random.nextFloat();

		return (int) (f * retained.size());
	}

	/**
	 * Computes the score for the specified pair.
	 * returned value must be between 0 and 1.
	 */
	private float getScore(MutableMarkedRecordPair pair, boolean in) {
		float good = 1;
		float bad = 1;

		// score goes down geometrically with the number of pairs for a given record currently retained
		int qCount = idCounts.getInt(pair.getQueryRecord().getId());
		int mCount = idCounts.getInt(pair.getMatchRecord().getId());
		if (!in) {
			qCount++;
			mCount++;
		}
		bad *= Math.sqrt(qCount * mCount);

		// score goes down geometrically with the number of pairs with the same active clues
		IntArrayWrapper wrapper = wrapActiveClues(pair.getActiveClues());
		int acCount = activeCluesCounts.getInt(wrapper);
		if (!in) {
			acCount++;
		}
		bad *= Math.sqrt(acCount);

		// adjust score by the rare clues that fire for this pair
		int[] ac = wrapper.wrappee;
		for (int i = 0, n = ac.length; i < n; i++) {
			int total = totalClueFirings[i];
			int _retained = retainedClueFirings[i];

			if (in) {
				total--;
				_retained--;
			}

			if (total < 10) {
				good += 10 - total;
			} else if (_retained < 5) {
				good += 5 - _retained;
			}
		}

		// adjust score for rare probability buckets
		int bucket = (int) (pair.getProbability() / probabilityBucketWidth);
		//if (bucket >= numProbabilityBuckets) {
		//	bucket = numProbabilityBuckets - 1;
		//}
		int curInBucket = retainedProbabilityBuckets[bucket];
		int targetInBucket = bucketTargets[bucket];
		if (!in) {
			curInBucket++;
		}
		if (curInBucket < targetInBucket) {
			double f = 2 * (targetInBucket - curInBucket) / (double)targetInBucket;
			good += f * f;
		} else if (curInBucket > targetInBucket) {
			double f = (curInBucket - targetInBucket) / (double)targetInBucket;
			bad += f * f;
		}

		return good / (good + bad);
	}

	/**
	 * Update the scores of all currently-retained pairs and reorder
	 * the them by score.
	 */
	private void updateScores() {
		for (int i = 0, n = retained.size(); i < n; i++) {
			MrpWrapper mrpWrapper = (MrpWrapper) retained.get(i);
			mrpWrapper.score = getScore(mrpWrapper.pair, true);
		}

		Collections.sort(retained);
	}

	//
	// Actions we can take with a pair:  either retain or pass over an incoming pair, and
	//  remove an existing pair to stay at the target
	//

	/**
	 * Update this DefaultPairSampler's statistics as we prepare
	 * to retain the specified pair.
	 */
	private void addPairToStats(MutableMarkedRecordPair pair) {

		// update record id counts
		idCounts.increment(pair.getQueryRecord().getId());
		idCounts.increment(pair.getMatchRecord().getId());

		// update active clue counts
		IntArrayWrapper clueWrapper = wrapActiveClues(pair.getActiveClues());
		activeCluesCounts.increment(clueWrapper);

		// update the clue firings
		clueWrapper.updateClueFirings(totalClueFirings, +1);
		clueWrapper.updateClueFirings(retainedClueFirings, +1);

		// update probability buckets
		int bucket = (int) (pair.getProbability() / probabilityBucketWidth);
		totalProbabilityBuckets[bucket]++;
		retainedProbabilityBuckets[bucket]++;

		// update counts of seen and retained
		numSeen++;
		numRetained++;

		if (target < 20) {
			updateScores();
		} else {
			if (numRetained >= target && (modCount++ % (target / 20) == 0)) {
				updateScores();
			}
		}

	}

	/**
	 * Update this DefaultPairSampler's statistics as we process the
	 * specified pair, but do not retain it.
	 */
	private void passOverPair(MutableMarkedRecordPair pair) {
		// update clue firings
		IntArrayWrapper wrapper = wrapActiveClues(pair.getActiveClues());
		wrapper.updateClueFirings(totalClueFirings, +1);

		// update probability buckets
		int bucket = (int) (pair.getProbability() / probabilityBucketWidth);
		totalProbabilityBuckets[bucket]++;

		// update count of seen
		numSeen++;

		if (numSeen % target == 0) {
			updateScores();
		}
	}

	/**
	 * Update this DefaultPairSampler's statistics as we remove
	 * the specified pair from the retained pairs.  This is only
	 * called when we are retaining another pair in favor of this
	 * pair.
	 */
	private void removePairFromStats(MutableMarkedRecordPair pair) {

		// update the record id counts
		Object qId = pair.getQueryRecord().getId();
		int qCount = idCounts.getInt(qId);
		if (qCount == 1) {
			idCounts.remove(qId);
		} else {
			idCounts.putInt(qId, qCount - 1);
		}

		Object mId = pair.getMatchRecord().getId();
		int mCount = idCounts.getInt(mId);
		if (mCount == 1) {
			idCounts.remove(mId);
		} else {
			idCounts.putInt(mId, mCount - 1);
		}

		// update active clue
		IntArrayWrapper acWrapper = wrapActiveClues(pair.getActiveClues());
		int acCount = activeCluesCounts.getInt(acWrapper);
		if (acCount == 1) {
			activeCluesCounts.remove(acWrapper);
		} else {
			activeCluesCounts.putInt(acWrapper, acCount - 1);
		}

		// update clue firings
		acWrapper.updateClueFirings(retainedClueFirings, -1);

		// update probability buckets
		int bucket = (int) (pair.getProbability() / probabilityBucketWidth);
		retainedProbabilityBuckets[bucket]--;

		// update retained count
		numRetained--;

		if (numRetained >= target && (modCount++ % 50 == 0)) {
			updateScores();
		}
	}

	//
	// Initialization code.
	//

	/**
	 * Called by processPair() each time processPair() is called.
	 *
	 * Checks whether or not it has been called before and executes
	 * init() exactly once.
	 */
	private void maybeInit() {
		if (!initialized) {
			init();
		}
	}

	private void init() {
		retained = new ArrayList(target + 1);

		this.numSeen = 0;
		this.numRetained = 0;

		activeCluesCounts = new IntValuedHashMap();
		idCounts = new IntValuedHashMap();

		numClues = model.getCluesToEvaluate().length;
		totalClueFirings = new int[numClues];
		retainedClueFirings = new int[numClues];

		numProbabilityBuckets = 20;
		probabilityBucketWidth = (1.0 / numProbabilityBuckets) + .0001; // added a delta to make sure we wouldn't have problem due to rounding...
		bucketTargets = defaultCreateBucketTargets(target, DEFAULT_NUM_BUCKETS, DEFAULT_BULGE_FACTOR);
		totalProbabilityBuckets = new int[numProbabilityBuckets];
		retainedProbabilityBuckets = new int[numProbabilityBuckets];

		initialized = true;
	}

	/**
	 * Bulge factor should be greater than zero.
	 *
	 * @param bulgeFactor the amount that the extreme buckets (0% and 100%) should be &quot;fatter&quot;
	 * than the middle bucket (nearest to 50%)
	 */
	private static int[] defaultCreateBucketTargets(int targetPairs, int numBuckets, double bulgeFactor) {
		double[] work = new double[numBuckets];

		// fill in the estimates
		for (int i = 0; i < numBuckets; i++) {
			double middle = (2*i + 1)/ (2.0*numBuckets);
			// entropy fxn:  - x lg x - (1-x) lg (1-x)
			work[i] = 1 - entropy(middle) + (1 / bulgeFactor);
		}

		// normalize
		double total = 0;
		for (int i = 0; i < numBuckets; i++) {
			total += work[i];
		}

		int[] targets = new int[numBuckets];
		for (int i = 0; i < numBuckets; i++) {
			targets[i] = (int) ( work[i] * targetPairs / total );
			if (targets[i] < 1) {
				targets[i] = 1;
			}
		}

		return targets;
	}

	//
	// static math helper stuff
	//

	/**
	 * Returns the binary entropy function of <code>x</code>.
	 *
	 * This function is +1 at 0.5 and is zero at 0 and 1.
	 */
	private static double entropy(double x) {
		if (x < 0 || x > 1) {
			throw new IllegalArgumentException("x < 0 or x > 1");
		}
		return - x*log2(x) - (1-x)*log2(1-x);
	}

	/**
	 * Returns the base 2 logarithm of <code>x</code>.
	 * Uses the change of base theorem.
	 */
	private static double log2(double x) {
		return Math.log(x) / LOG2;
	}


	//
	// helper methods and classes
	//

	private static IntArrayWrapper wrapActiveClues(ActiveClues ac) {
		if (ac instanceof BooleanActiveClues) {
			BooleanActiveClues bac = (BooleanActiveClues) ac;
			return new IntArrayWrapper(bac.getCluesAndRules());
		} else {
			throw new IllegalStateException("Can't use non-boolean cluesets yet...");
		}
	}

	private static class IntArrayWrapper {
		int[] wrappee;
		public IntArrayWrapper(int[] wrappee) {
			this.wrappee = wrappee;
		}

		public void updateClueFirings(int[] clueFirings, int howMuch) {
			for (int i = 0, n = wrappee.length; i < n; i++) {
				clueFirings[ wrappee[i] ] += howMuch;
			}
		}

		public boolean equals(Object other) {
			IntArrayWrapper w = (IntArrayWrapper) other;
			if (wrappee.length == w.wrappee.length) {
				for (int i = 0, n = wrappee.length; i < n; i++) {
					if (wrappee[i] != w.wrappee[i]) {
						return false;
					}
				}
				return true;
			}

			return false;
		}

		public int hashCode() {
			int ret = 0;
			for (int i = 0, n = wrappee.length; i < n; i++) {
				int bits = i % 32;
				int e = wrappee[i];
				ret &= (e >>> bits) & (e << (32 - bits));
			}
			return ret;
		}
	}

	private static class MrpWrapper implements Comparable {
		MutableMarkedRecordPair pair;
		final int index;
		float score;
		public MrpWrapper(MutableMarkedRecordPair pair, int index, float score) {
			this.pair = pair;
			this.index = index;
			this.score = score;
		}
		public int compareTo(Object other) {
			float diff = score - ((MrpWrapper)other).score;
			return diff < 0 ? -1 :
				diff > 0 ? 1 :
				0;
		}
	}

}
