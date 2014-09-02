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
package com.choicemaker.cm.io.blocking.automated.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.util.PrintUtils;

/**
 * Creates blocking sets.
 * @author Martin Buechi (original Blocker code)
 * @author rphall (refactored to be testable)
 * @version $Revision: 1.1 $ $Date: 2010/03/24 21:30:24 $
 */
public class BlockingSetFactory {

	/*
	 * IMPLEMENTATION NOTE
	 * rphall 2008-06-19
	 *
	 * Methods from this class were refactored from the Blocker class
	 * in order to be testable via stubs or mock objects for interfaces
	 * such as BlockingAccessor and CountSource. (Because the constructor
	 * of the Blocker class requires a IProbabilityModel instance, the Blocker
	 * class require significant initialization that makes it difficult to test.)
	 */

	// Unused manifest constants.
	// (This is probably the wrong place to define these constants, anyway.)
	//
	// public static final String LIMIT_PER_BLOCKING_SET = "limitPerBlockingSet";
	// public static final String LIMIT_SINGLE_BLOCKING_SET =
	//	"limitSingleBlockingSet";

	/**
	 * The name of a system property that can be set to "true" to allow
	 * incomplete blocking sets to be returned from
	 * <code>createBlockingSets(..)</code> instead of throwing an
	 * IncompleteBlockingSetsException. This property should be "false"
	 * in production and during most development work. It should be
	 * "true" only for specific, short-term debugging efforts.
	 */
	public static final String PN_IGNORE_INCOMPLETE_BLOCKING_SETS =
		"com.choicemaker.cm.io.blocking.automated.base.IgnoreIncompleteBlockingSets";

	// Don't use this variable directly; use isIgnoreIncompleteBlockingSetsRequested() instead
	private static Boolean _isIgnoreIncompleteBlockingSetsRequested = null;

	/**
	 * Checks the system property {@link #PN_IGNORE_INCOMPLETE_BLOCKING_SETS}
	 * and caches the result
	 */
	private static boolean isIgnoreIncompleteBlockingSetsRequested() {
		if (_isIgnoreIncompleteBlockingSetsRequested == null) {
			String value =
				System.getProperty(PN_IGNORE_INCOMPLETE_BLOCKING_SETS, "false");
			_isIgnoreIncompleteBlockingSetsRequested = Boolean.valueOf(value);
		}
		boolean retVal =
			_isIgnoreIncompleteBlockingSetsRequested.booleanValue();
		return retVal;
	}

	private static Logger logger = Logger.getLogger(BlockingSetFactory.class.getName());

	private BlockingSetFactory() {
	}

	/**
	 * Creates blockingSets for an input record <code>q</code>, given a
	 * specified BlockingConfiguration <code>bc</code>.
	 * @param bc a non-null blocking configuration
	 * @param q a non-null query record
	 * @param limitPerBlockingSet The maximum expected size for any
	 * blockingSet among the returned blocking sets.
	 * @param singleTableBlockingSetGraceLimit Overides limitPerBlockingSet
	 * to permit larger blocking sets if they block on a small number of tables.
	 * In typical cases, "small number of tables" means one (1) table. This
	 * parameter is an optimization that allows the ABA to avoid additional
	 * joins across database tables, at the cost of allowing some blocking
	 * sets to larger than <code>limitPerBlockingSet</code>. This optimization
	 * can be turned off by setting <code>singleTableBlockingSetGraceLimit</code>
	 * to be equal or less than <code>limitPerBlockingSet</code>.
	 * @param limitSingleBlockingSet Overides limitPerBlockingSet and
	 * singleTableBlockingSetGraceLimit to allow the fallback creation of a
	 * singleBlockingSet in the case that the other constraints have been too
	 * restrictive. In order for this limit to have any effect, it must be larger
	 * than at least limitPerBlockingSet; its effect is more transparent if it
	 * is also larger than singleTableBlockingSetGraceLimit. By setting this
	 * value to be high, say 5 times <code>limitPerBlockingSet</code>, one
	 * can avoid having the ABA throw <code>IncompleteBlockingSetsException</code>
	 * and <code>UnderspecifiedQueryException</code> for the occasional
	 * legitimate query that has few blocking values, without the performance
	 * impact that increasing <code>limitPerBlockingSet</code> would have.
	 * @param countSource a non-null source of counts for field-value pairs.
	 * @return a non-null and non-empty list of blockingSets.
	 * @throws IncompleteBlockingSetsException if a list of blocking sets can
	 * not be formed that includes every blocking value (or its base) for the input
	 * record. This exception is a subclass of UnderspecifiedQueryException, so
	 * it needs to be caught separately only if detailed diagnostics should be
	 * presented to an end user.
	 * @throws UnderspecifiedQueryException if the input record does not have
	 * enough (or sufficiently uncommon) blocking values to form any blocking
	 * sets that meet the specified size limits. This exception is a subclass of
	 * IOException, so it needs to be caught separately if detailed diagnostics
	 * should be presenteed to an end user. Since an underspeficied query is
	 * something that an end user might be able to correct, it is probably a
	 * good idea to catch this exception separately.
	 * @throws IOException if blockingSets can not be created
	 * because of a system error that prevents the count source from
	 * being accessed.
	 */
	public static List createBlockingSets(
		BlockingConfiguration bc,
		Record q,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet,
		CountSource countSource)
		throws
			IncompleteBlockingSetsException,
			UnderspecifiedQueryException,
			IOException {

		BlockingValue[] blockingValues = bc.createBlockingValues(q);
		long mainTableSize = countSource.setCounts(bc, blockingValues);

		return createBlockingSetsInternal_1(
			blockingValues,
			mainTableSize,
			limitPerBlockingSet,
			singleTableBlockingSetGraceLimit,
			limitSingleBlockingSet);
	}

	/**
	 * Assumes that the blocking values have already been initialized
	 * with valid count values. This method is useful mainly for testing
	 * the ABA algorithm.
	 * @param blockingValues an array of blocking values with valid
	 * count values
	 * @param mainTableSize the normalization value by which count
	 * values are divided to estimate field-value frequencies.
	 * @see #createBlockingSets(BlockingConfiguration,Record,int,int,int,CountSource)
	 * for explanations of the limit parameters, return value, and possible
	 * exceptions.
	 */
	public static List createBlockingSetsInternal_1(
		BlockingValue[] blockingValues,
		long mainTableSize,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet)
		throws IncompleteBlockingSetsException, UnderspecifiedQueryException {

		// The list of blocking sets that will be returned
		final List blockingSets = new ArrayList(64);

		// A list of blocking values that would have been used,
		// if they hadn't been discarded by the
		// singleTableBlockingSetGraceLimit optimization.
		// This list is referenced at the end of this method
		// as part of a check to see if an IncompleteBlockingSets
		// exception should be thrown.
		final Set notUsedBecauseOfOptimization = new HashSet();

		// Initialize (oversized) blocking sets with an empty blocking set
		List oversized = new ArrayList(256);
		oversized.add(new BlockingSet(mainTableSize));
		createBlockingSetsInternal_2(
			blockingValues,
			limitPerBlockingSet,
			singleTableBlockingSetGraceLimit,
			limitSingleBlockingSet,
			blockingSets,
			oversized,
			notUsedBecauseOfOptimization);

		if (blockingSets.isEmpty()) {
			logger.debug(
				"No blocking sets were formed yet. Looking for best possible subset of blocking values...");
			Iterator iPossibleSubsets = oversized.iterator();
			iPossibleSubsets.next(); // skip empty set
			BlockingSet best = null;
			long bestCount = Long.MIN_VALUE;
			while (iPossibleSubsets.hasNext()) {
				BlockingSet currentSubset =
					(BlockingSet) iPossibleSubsets.next();
				long count = currentSubset.getExpectedCount();
				if (count < limitSingleBlockingSet && count > bestCount) {
					best = currentSubset;
					bestCount = count;
				}
			}
			if (best != null) {
				PrintUtils.logBlockingSet(
					logger,
					"...Found a suitable subset of blocking values. Using it as the blocking set. ",
					best);
				blockingSets.add(best);
			} else {
				logger.debug("...No suitable subset of blocking values.");
				throw new UnderspecifiedQueryException("Query not specific enough; would return too many records.");
			}
		}

		// rphall 2008-06-27
		// Fixes a bug with deceptive collections of blockingSets that
		// omit some blocking values (or their bases).
		BlockingValue[] missingBlockingValues =
			findBlockingValuesMissingFromBlockingSets(
				blockingValues,
				blockingSets,
				notUsedBecauseOfOptimization);
		// Log the values that are missing
		if (missingBlockingValues.length > 0) {
			if (logger.isInfoEnabled()) {
				String msg =
					prettyPrintMissingBlockingValues(missingBlockingValues);
				logger.info(msg);
				logger.info(
					"Looking for larger blocking sets that use the missing blocking values...");
			}
			createBlockingSetsInternal_2(
				missingBlockingValues,
				limitSingleBlockingSet,
				limitSingleBlockingSet,
				limitSingleBlockingSet,
				blockingSets,
				oversized,
				notUsedBecauseOfOptimization);
			logger.info(
				"...Finished looking for larger blocking sets that use the missing blocking values.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Listing final blocking sets...");
			for (int i = 0; i < blockingSets.size(); i++) {
				BlockingSet b = (BlockingSet) blockingSets.get(i);
				PrintUtils.logBlockingSet(
					logger,
					Level.INFO,
					"Blocking set " + i + " ",
					b);
			}
			logger.debug("...Finished listing final blocking sets");
		}

		// Throw an exception if any blocking values aren't used
		missingBlockingValues =
			findBlockingValuesMissingFromBlockingSets(
				blockingValues,
				blockingSets,
				notUsedBecauseOfOptimization);
		if (logger.isInfoEnabled() && missingBlockingValues.length > 0) {
			String msg =
				prettyPrintMissingBlockingValues(missingBlockingValues);
			if (isIgnoreIncompleteBlockingSetsRequested()) {
				msg = "IGNORED: " + msg;
				logger.info(msg);
			} else {
				throw new IncompleteBlockingSetsException(msg);
			}
		}

		return blockingSets;
	}

	/**
	 * Another variation on createBlockingSets that exposes the significant
	 * parameters and intermediate results of the Automated Blocking
	 * Algorithm. This method is useful mainly for testing the ABA and for
	 * recursive calculation of blocking sets using different limits on blocking-set
	 * size.
	 * @param blockingValues
	 * The blocking values that will be used to create blocking sets
	 * @param limitPerBlockingSet The maximum expected size for any
	 * blockingSet among the returned blocking sets.
	 * @param singleTableBlockingSetGraceLimit Overides limitPerBlockingSet
	 * to permit larger blocking sets if they block on a small number of tables.
	 * In typical cases, "small number of tables" means one (1) table. This
	 * parameter is an optimization that allows the ABA to avoid additional
	 * joins across database tables, at the cost of allowing some blocking
	 * sets to larger than <code>limitPerBlockingSet</code>. This optimization
	 * can be turned off by setting <code>singleTableBlockingSetGraceLimit</code>
	 * to be equal or less than <code>limitPerBlockingSet</code>.
	 * @param limitSingleBlockingSet Overides limitPerBlockingSet and
	 * singleTableBlockingSetGraceLimit to allow the fallback creation of a
	 * singleBlockingSet in the case that the other constraints have been too
	 * restrictive. In order for this limit to have any effect, it must be larger
	 * than at least limitPerBlockingSet; its effect is more transparent if it
	 * is also larger than singleTableBlockingSetGraceLimit. By setting this
	 * value to be high, say 5 times <code>limitPerBlockingSet</code>, one
	 * can avoid having the ABA throw <code>IncompleteBlockingSetsException</code>
	 * and <code>UnderspecifiedQueryException</code> for the occasional
	 * legitimate query that has few blocking values, without the performance
	 * impact that increasing <code>limitPerBlockingSet</code> would have.
	 * @param blockingSets
	 * Should be empty upon input. Upon output,
	 * the list of blocking sets that could be formed from the
	 * blocking values and the list of existing oversized blocking
	 * sets.
	 * @param oversized
	 * Upon input, a list of existing oversized blocking sets that
	 * will be refined by the blocking values into blocking sets.
	 * This list should have an empty blocking set as its first (and
	 * possibly only) entry. Upon output, this list will contain
	 * any oversized blocking sets that could not be refined
	 * by the blocking values.
	 * @param notUsedBecauseOfOptimization
	 * Should be empty upon input. Upon output,
	 * a list of blocking values that would have been used,
	 * if they hadn't been discarded by the
	 * singleTableBlockingSetGraceLimit optimization.
	 */
	public static void createBlockingSetsInternal_2(
		BlockingValue[] blockingValues,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet,
		List blockingSets,
		List oversized,
		Set notUsedBecauseOfOptimization) {

		// Sort the blockingValues by increasing count
		Arrays.sort(blockingValues);
		if (logger.isDebugEnabled()) {
			logger.debug("blockingValues size: " + blockingValues.length);
			for (int i = 0; i < blockingValues.length; i++) {
				PrintUtils.logBlockingValue(
					logger,
					Level.INFO,
					"Blocking value " + i + " ",
					blockingValues[i]);
			}
		}

		logger.debug("Starting to form blocking sets...");
		for (int i = 0; i < blockingValues.length; ++i) {

			BlockingValue bv = blockingValues[i];
			PrintUtils.logBlockingValue(
				logger,
				"Blocking value " + i + " ",
				bv);

			// rphall 2008-06-27
			//
			// The emptySet flag is used to track whether the first
			// possibleSubset is being considered. Not incidently, the
			// first possibleSubset is the "empty" blocking set (with
			// no blocking values). The emptySet flag should be renamed
			// and moved into the loop over oversized blocking sets for better
			// clarity; i.e. it should be defined as
			//
			//     boolean emptyBlockingSet = j==0 ? true : false ;
			//
			boolean emptySet = true;

			// rphall 2008-06-27
			//
			// Note that the size of oversized blocking sets may increase as
			// the following loop is iterated. The terminating condition
			// for the loop is set outside of this loop; i.e. that J be less
			// than the size of oversized blocking sets at the start of this loop.
			// This prevents newly added subsets from being considered
			// as possible subsets, which is a very good thing. It means
			// that a blocking value can NOT be used more than once to form
			// a possibleSubset (which goes to the core of the ABA). It
			// also ensures that this loop will terminate.
			//
			int size = oversized.size();
			for (int j = 0; j < size; ++j) {

				BlockingSet currentSubset = (BlockingSet) oversized.get(j);
				PrintUtils.logBlockingSet(
					logger,
					"Refining blocking set " + j + " ",
					currentSubset);

				if (currentSubset != null && valid(bv, currentSubset)) {

					BlockingSet candidate = new BlockingSet(currentSubset, bv);
					PrintUtils.logBlockingSet(
						logger,
						"Candidate blocking set ",
						candidate);

					// Check if the candidate blocking set would block on more
					// tables than the current subset and if the current
					// subset is sufficiently small by itself
					if (!emptySet
						&& candidate.getNumTables() > currentSubset.getNumTables()
						&& currentSubset.getExpectedCount()
							<= singleTableBlockingSetGraceLimit) {

						addToBlockingSets(blockingSets, currentSubset);
						notUsedBecauseOfOptimization.add(bv);

						// don't consider in future
						oversized.set(j, null);

						if (logger.isDebugEnabled()) {
							int blockingSetOrdinal = blockingSets.size() - 1;
							String msg =
								"Formed a grace-limit blocking set (ordinal # "
									+ blockingSetOrdinal
									+ ") ";
							PrintUtils.logBlockingSet(
								logger,
								msg,
								currentSubset);
						}

						// Check if the candidate blocking set is small enough
					} else if (
						candidate.getExpectedCount() <= limitPerBlockingSet) {
						addToBlockingSets(blockingSets, candidate);
						if (emptySet) {
							if (logger.isDebugEnabled()) {
								int blockingSetOrdinal =
									blockingSets.size() - 1;
								String msg =
									"Formed a single-value blocking set (ordinal # "
										+ blockingSetOrdinal
										+ ") ";
								PrintUtils.logBlockingSet(
									logger,
									msg,
									candidate);
							}
							// restart with the next blocking value
							break;
						} else {
							if (logger.isDebugEnabled()) {
								int blockingSetOrdinal =
									blockingSets.size() - 1;
								// 2014-04-24 rphall: Commented out unused local variable.
//								int subsetOrdinal = j;
								String msg =
									"Formed a compound-value blocking set (ordinal # "
										+ blockingSetOrdinal
										+ ") with blockingValue "
										+ i
										+ " and oversized set "
										+ j
										+ ": ";
								PrintUtils.logBlockingSet(
									logger,
									msg,
									candidate);
							}
						}

						// Otherwise, add the candidate blocking set to collection
						// of (oversized) possible blocking sets
					} else {
						oversized.add(candidate);
						if (logger.isDebugEnabled()) {
							String msg =
								"Added candidate blocking set to collection of (oversized) possible blocking sets.";
							logger.debug(msg);
						}
					}

					// end currentSubset != null && candidate is valid
				}
				emptySet = false;

				// end iteration over possible subsets
			}
			// end iteration over blocking values
		}
		logger.debug(
			"...Finished forming blocking sets. Blocking set size == "
				+ blockingSets.size());

	}

	static boolean valid(BlockingValue bv, BlockingSet bs) {
		BlockingField bf = bv.blockingField;
		QueryField qf = bf.queryField;
		DbField dbf = bf.dbField;

		boolean retVal = true;
		int size = bs.numFields();
		for (int i = 0; i < size; ++i) {

			BlockingValue cbv = bs.getBlockingValue(i);
			BlockingField cbf = cbv.blockingField;

			// multiple use of same DbField (implied by multiple use of same BlockingField)
			if (dbf == cbf.dbField) {
				logger.debug(
					"invalid BlockingValue for BlockingSet: multiple use of same DbField");
				retVal = false;
				break;
			}
			// multiple use of same QueryField
			if (qf == cbf.queryField) {
				logger.debug(
					"invalid BlockingValue for BlockingSet: multiple use of same QueryField");
				retVal = false;
				break;
			}
			// illegal combinations
			if (illegalCombination(bs, bf.illegalCombinations)) {
				logger.debug(
					"invalid BlockingValue for BlockingSet: Illegal BlockingField combination");
				retVal = false;
				break;
			}
			if (illegalCombination(bs, qf.illegalCombinations)) {
				logger.debug(
					"invalid BlockingValue for BlockingSet: Illegal QueryField combination");
				retVal = false;
				break;
			}
			if (illegalCombination(bs, dbf.illegalCombinations)) {
				logger.debug(
					"invalid BlockingValue for BlockingSet: Illegal DbField combination");
				retVal = false;
				break;
			}
		}
		return retVal;
	}

	static boolean illegalCombination(
		BlockingSet bs,
		Field[][] illegalCombinations) {
		boolean retVal = false;
		for (int i = 0; i < illegalCombinations.length; ++i) {
			Field[] ic = illegalCombinations[i];
			int j = 0;
			while (j < ic.length && bs.containsField(ic[j])) {
				++j;
			}
			if (j == ic.length) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}

	static void addToBlockingSets(List blockingSets, BlockingSet candidate) {
		boolean isCandidateAlreadyInBlockingSet = false;
		Iterator iBlockingSets = blockingSets.iterator();
		while (iBlockingSets.hasNext()) {
			BlockingSet currentBlockingSet = (BlockingSet) iBlockingSets.next();
			if (candidate.returnsSupersetOf(currentBlockingSet)) {
				logger.debug(
					"Candidate blocking set is a superset of an existing blocking set");
				iBlockingSets.remove();
			} else if (currentBlockingSet.returnsSupersetOf(candidate)) {
				logger.debug(
					"Candidate blocking set is a subset of an existing blocking set");
				isCandidateAlreadyInBlockingSet = true;
				break;
			}
		}
		if (!isCandidateAlreadyInBlockingSet) {
			blockingSets.add(candidate);
		}
	}

	/**
	 * Finds the blocking values (or their derived values) that are
	 * missing from some list of blocking sets
	 */
	static BlockingValue[] findBlockingValuesMissingFromBlockingSets(
		BlockingValue[] blockingValues,
		List blockingSets,
		Set notUsedBecauseOfOptimization)
		throws IncompleteBlockingSetsException {

		// This routine handles blockingValues that have complex bases;
		// i.e. derivedblockingValues that based on more than one
		// parent blockingValue. An example might be
		// a city-and-state blocking value, which would be
		// based on a city blockingValue and a state blockingValue
		// (although states are often poor blocking values).
		// Another example might a local phone number plus
		// an area code (although area codes are often poor
		// for the same reason that states are --  low cardinality).
		// If derived value is used by some blocking set,
		// then its base values are considered used by the
		// same blocking  set, and therefore neither the derived
		// nor the base value is considered missing.
		//
		// For example, if the soundex value of a first name
		// is used by some blocking set, then both the soundex
		// value and the base first name value are not considered
		// missing.
		//
		// As another example, if a compound city-and-state
		// blocking value is used, then the compound value
		// and the base city and state values are not considered
		// missing.

		// A set that doesn't shrink
		Set allBlockingValues = new HashSet();

		// A set that shrinks (FIXME Are iterators over this set dicey?)
		Set missingBlockingValues = new HashSet();

		// Iniitialize allBlockingValues and missingBlockingValues
		for (int i = 0; i < blockingValues.length; i++) {
			allBlockingValues.add(blockingValues[i]);
			missingBlockingValues.add(blockingValues[i]);
		}

		// Remove a blocking value (and its bases) from the set of missing values
		// if the value was optimized away
		for (Iterator iter = allBlockingValues.iterator();
			missingBlockingValues.size() > 0 && iter.hasNext();
			) {
			BlockingValue valueToCheck = (BlockingValue) iter.next();
			if (missingBlockingValues.contains(valueToCheck)
				&& notUsedBecauseOfOptimization.contains(valueToCheck)) {
				missingBlockingValues.remove(valueToCheck);
				BlockingValue[][] base = valueToCheck.base;
				for (int r = 0;
					missingBlockingValues.size() > 0 && r < base.length;
					r++) {
					for (int s = 0;
						missingBlockingValues.size() > 0 && s < base[r].length;
						s++) {
						missingBlockingValues.remove(base[r][s]);
					}
				}
			}
		}

		// Remove a blocking value (and its bases) from the set of missing values
		// if the value was used in some blocking set
		for (int i = 0;
			missingBlockingValues.size() > 0 && i < blockingSets.size();
			i++) {
			BlockingSet b = (BlockingSet) blockingSets.get(i);
			BlockingValue[] bv = b.getBlockingValues();
			for (int j = 0;
				missingBlockingValues.size() > 0 && j < bv.length;
				j++) {
				missingBlockingValues.remove(bv[j]);
				BlockingValue[][] base = bv[j].base;
				for (int r = 0;
					missingBlockingValues.size() > 0 && r < base.length;
					r++) {
					for (int s = 0;
						missingBlockingValues.size() > 0 && s < base[r].length;
						s++) {
						missingBlockingValues.remove(base[r][s]);
					}
				}
			}
		}

		// Log the values that are missing
		if (logger.isDebugEnabled() && missingBlockingValues.size() > 0) {
			String msg =
				prettyPrintMissingBlockingValues(missingBlockingValues);
			logger.debug(msg);
		}

		BlockingValue[] retVal =
			(BlockingValue[]) missingBlockingValues.toArray(
				new BlockingValue[missingBlockingValues.size()]);
		return retVal;
	}

	private static String prettyPrintMissingBlockingValues(Collection missingBlockingValues) {
		BlockingValue[] array =
			(BlockingValue[]) missingBlockingValues.toArray(
				new BlockingValue[0]);
		return prettyPrintMissingBlockingValues(array);
	}

	private static String prettyPrintMissingBlockingValues(BlockingValue[] missingBlockingValues) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.print("Missing blocking values: { ");
		for (int i = 0; i < missingBlockingValues.length; i++) {
			PrintUtils.printBlockingValue(pw, missingBlockingValues[i]);
			pw.print(" ");
		}
		pw.print("}");
		String retVal = sw.toString();
		return retVal;
	}

}

