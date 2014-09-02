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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.io.blocking.automated.util.PrintUtils;

/**
 * @author    Martin Buechi
 * @version   $Revision: 1.2 $ $Date: 2010/03/24 21:32:37 $
 * @deprecated Returns sometimes erroneous blockingSets;
 * use {@link Blocker2} instead.
 */
public class Blocker implements AutomatedBlocker {
	public static final String LIMIT_PER_BLOCKING_SET = "limitPerBlockingSet";
	public static final String LIMIT_SINGLE_BLOCKING_SET = "limitSingleBlockingSet";
	private static Logger logger = Logger.getLogger(Blocker.class.getName());

	private String name;
	private CountSource countSource;
	private DatabaseAccessor databaseAccessor;
	private ImmutableProbabilityModel model;
	private BlockingConfiguration blockingConfiguration;
	private Record q;
	private int limitPerBlockingSet;
	private int singleTableBlockingSetGraceLimit;
	private int limitSingleBlockingSet;
	private List possibleSubsets;
	private List blockingSets;
	private int numberOfRecordsRetrieved;

	Blocker(DatabaseAccessor databaseAccessor, ImmutableProbabilityModel model, Record q) {
		this(
			databaseAccessor,
			model,
			q,
			Integer.parseInt((String)model.properties().get("limitPerBlockingSet")),
			Integer.parseInt((String)model.properties().get("singleTableBlockingSetGraceLimit")),
			Integer.parseInt((String)model.properties().get("limitSingleBlockingSet")));
	}

	Blocker(DatabaseAccessor databaseAccessor,
				   ImmutableProbabilityModel model,
				   Record q,
				   String dbConfigurationName,
				   String blockingConfigurationName) {
		this(
		databaseAccessor,
		model,
		q,
		Integer.parseInt((String)model.properties().get("limitPerBlockingSet")),
		Integer.parseInt((String)model.properties().get("singleTableBlockingSetGraceLimit")),
		Integer.parseInt((String)model.properties().get("limitSingleBlockingSet")),
		(CountSource) model.properties().get("countSource"),
		dbConfigurationName,
		blockingConfigurationName
		);
	}

	Blocker(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet) {
		this(
			databaseAccessor,
			model,
			q,
			limitPerBlockingSet,
			singleTableBlockingSetGraceLimit,
			limitSingleBlockingSet,
			(CountSource) model.properties().get("countSource"),
			(String) model.properties().get("dbConfiguration"),
			(String) model.properties().get("blockingConfiguration"));
	}

	public Blocker(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet,
		CountSource countSource,
		String dbConfigurationName,
		String blockingConfigurationName) {
		this (
			databaseAccessor,
			model,
			q,
			limitPerBlockingSet,
			singleTableBlockingSetGraceLimit,
			limitSingleBlockingSet,
			countSource,
			((BlockingAccessor) model.getAccessor()).getBlockingConfiguration(
				blockingConfigurationName,
				dbConfigurationName));
	}

	// For testing; see doSanityCheck()
	Blocker(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet,
		CountSource countSource,
		BlockingConfiguration blockingConfiguration) {
		this.databaseAccessor = databaseAccessor;
		this.model = model;
		// 2014-04-24 rphall: Commented out unused local variable
		// Any side effects?
//		BlockingAccessor ba = (BlockingAccessor) model.getAccessor();
		this.q = q;
		this.limitPerBlockingSet = limitPerBlockingSet;
		this.singleTableBlockingSetGraceLimit =
			singleTableBlockingSetGraceLimit;
		this.limitSingleBlockingSet = limitSingleBlockingSet;
		this.countSource = countSource;
		this.blockingConfiguration = blockingConfiguration;
	}

	public void open() throws IOException {
		numberOfRecordsRetrieved = 0;
		BlockingValue[] blockingValues = blockingConfiguration.createBlockingValues(getQueryRecord());

		long mainTableSize = getCountSource().setCounts(blockingConfiguration, blockingValues);

		logger.fine("blockingValues numberOfRecordsRetrieved: " + blockingValues.length);

		for (int i=0; i<blockingValues.length; i++) {
			logger.fine(blockingValues[i].value + " " + blockingValues[i].count + " " + blockingValues[i].blockingField.dbField.name);
		}

		Arrays.sort(blockingValues);
		logger.fine("blockingValues size: " + blockingValues.length);
		for (int i = 0; i < blockingValues.length; i++) {
			PrintUtils.logBlockingValue(logger,"Blocking value " + i + " ", blockingValues[i]);
		}

		possibleSubsets = new ArrayList(256);
		possibleSubsets.add(new BlockingSet(mainTableSize));
		blockingSets = new ArrayList(64);

		logger.fine("Starting to form blocking sets...");
		for (int i = 0; i < blockingValues.length; ++i) {

			BlockingValue bv = blockingValues[i];
			PrintUtils.logBlockingValue(logger,"Blocking value " + i + " ", bv);

			boolean emptySet = true;
			int size = possibleSubsets.size();
			for (int j = 0; j < size; ++j) { // don't iterate over newly added subsets
				BlockingSet bs = (BlockingSet) possibleSubsets.get(j);

				if (bs != null && valid(bv, bs)) {

					BlockingSet nbs = new BlockingSet(bs, bv);
					PrintUtils.logBlockingSet(logger,"Candidate blocking set ", nbs);

					if(!emptySet && nbs.getNumTables() > bs.getNumTables() && bs.getExpectedCount() <= getSingleTableBlockingSetGraceLimit()) {
						addToBlockingSets(bs);
						int ordinal = getBlockingSets().size() - 1;
						String msg =
							"Formed a grace-limit blocking set (ordinal # "
								+ ordinal
								+ ") ";
						PrintUtils.logBlockingSet(logger,msg, bs);

						possibleSubsets.set(j, null); // don't consider in future
						//TODO: check singleton blocking set numberOfRecordsRetrieved
					} else if (nbs.getExpectedCount() <= getLimitPerBlockingSet()) {
						addToBlockingSets(nbs);
						int ordinal = getBlockingSets().size() - 1;
						if (emptySet) {
							String msg =
								"Formed a single-value blocking set (ordinal # "
									+ ordinal
									+ ") ";
							PrintUtils.logBlockingSet(logger,msg, nbs);
							break;
						}
						String msg =
							"Formed a compound-value blocking set (ordinal # "
								+ ordinal
								+ ") ";
						PrintUtils.logBlockingSet(logger,msg, nbs);
					} else {
						possibleSubsets.add(nbs);
						String msg =
							"Added candidate blocking set to collection of (oversized) possible blocking sets.";
						logger.fine(msg);
					}
				}
				emptySet = false;
			}
		}
		logger.fine(
			"...Finished forming blocking sets. Blocking set size == "
				+ getBlockingSets().size());

		if (getBlockingSets().isEmpty()) {
			logger.fine(
				"No blocking sets were formed yet. Looking for best possible subset of blocking values...");
			Iterator iPossibleSubsets = possibleSubsets.iterator();
			iPossibleSubsets.next(); // skip empty set
			BlockingSet best = null;
			long bestCount = Long.MIN_VALUE;
			while (iPossibleSubsets.hasNext()) {
				BlockingSet bs = (BlockingSet) iPossibleSubsets.next();
				long count = bs.getExpectedCount();
				if (count < getLimitSingleBlockingSet() && count > bestCount) {
					best = bs;
					bestCount = count;
				}
			}
			if (best != null) {
				PrintUtils.logBlockingSet(logger,
					"...Found a suitable subset of blocking values. Using it as the blocking set. ",
					best);
				getBlockingSets().add(best);
			} else {
				logger.fine("...No suitable subset of blocking values.");
				throw new UnderspecifiedQueryException("Query not specific enough; would return too many records.");
			}
		}

		logger.fine("Listing final blocking sets...");
		for (int i = 0; i < getBlockingSets().size(); i++) {
			BlockingSet b = (BlockingSet) getBlockingSets().get(i);
			PrintUtils.logBlockingSet(logger,"Blocking set " + i + " ", b);
		}
		logger.fine("...Finished listing final blocking sets");

		getDatabaseAccessor().open(this);
	}

	private boolean valid(BlockingValue bv, BlockingSet bs) {
		BlockingField bf = bv.blockingField;
		QueryField qf = bf.queryField;
		DbField dbf = bf.dbField;

		int size = bs.numFields();
		for (int i = 0; i < size; ++i) {
			BlockingValue cbv = bs.getBlockingValue(i);
			BlockingField cbf = cbv.blockingField;

			// multiple use of same DbField (implied by multiple use of same BlockingField)
			if (dbf == cbf.dbField) {
				logger.fine("invalid BlockingValue for BlockingSet: multiple use of same DbField");
				return false;
			}
			// multiple use of same QueryField
			if (qf == cbf.queryField) {
				logger.fine("invalid BlockingValue for BlockingSet: multiple use of same QueryField");
				return false;
			}
			// illegal combinations
			if (illegalCombination(bs, bf.illegalCombinations)) {
				logger.fine("invalid BlockingValue for BlockingSet: Illegal BlockingField combination");
				return false;
			}
			if (illegalCombination(bs, qf.illegalCombinations)) {
				logger.fine("invalid BlockingValue for BlockingSet: Illegal QueryField combination");
				return false;
			}
			if (illegalCombination(bs, dbf.illegalCombinations)) {
				logger.fine("invalid BlockingValue for BlockingSet: Illegal DbField combination");
				return false;
			}
		}
		return true;
	}

	private boolean illegalCombination(BlockingSet bs, Field[][] illegalCombinations) {
		for (int i = 0; i < illegalCombinations.length; ++i) {
			Field[] ic = illegalCombinations[i];
			int j = 0;
			while (j < ic.length && bs.containsField(ic[j])) {
				++j;
			}
			if (j == ic.length) {
				return true;
			}
		}
		return false;
	}

	private void addToBlockingSets(BlockingSet nbs) {
		Iterator iBlockingSets = getBlockingSets().iterator();
		while (iBlockingSets.hasNext()) {
			BlockingSet cbs = (BlockingSet) iBlockingSets.next();
			if (nbs.returnsSupersetOf(cbs)) {
				iBlockingSets.remove();
			} else if (cbs.returnsSupersetOf(nbs)) {
				return;
			}
		}
		getBlockingSets().add(nbs);
	}

	public void close() throws IOException {
		getDatabaseAccessor().close();
	}

	public boolean hasNext() {
		return getDatabaseAccessor().hasNext();
	}

	public Record getNext() throws IOException {
		++numberOfRecordsRetrieved;
		return getDatabaseAccessor().getNext();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		this.model = m;
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		throw new UnsupportedOperationException("no sink");
	}

	public int getNumberOfRecordsRetrieved() {
		return numberOfRecordsRetrieved;
	}

	public String getFileName() {
		throw new UnsupportedOperationException();
	}

	public DatabaseAccessor getDatabaseAccessor() {
		return databaseAccessor;
	}

	public BlockingConfiguration getBlockingConfiguration() {
		return blockingConfiguration;
	}

	public Record getQueryRecord() {
		return q;
	}

	public int getLimitPerBlockingSet() {
		return limitPerBlockingSet;
	}

	public int getSingleTableBlockingSetGraceLimit() {
		return singleTableBlockingSetGraceLimit;
	}

	public int getLimitSingleBlockingSet() {
		return limitSingleBlockingSet;
	}

	public List getBlockingSets() {
		return blockingSets;
	}

	public CountSource getCountSource() {
		return countSource;
	}

}

