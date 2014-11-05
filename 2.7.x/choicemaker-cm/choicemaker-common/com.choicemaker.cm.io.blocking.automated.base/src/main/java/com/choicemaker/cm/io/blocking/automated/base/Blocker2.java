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
import java.util.List;
import java.util.logging.Logger;

import com.choicemaker.cm.core.Accessor;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.Sink;

/**
 * Creates blockingSets (using a BlockingSetFactory) and provides
 * methods for retrieving blocked records from a database (the
 * {@link AutomatedBlocker} interface extends the
 * {@link com.choicemaker.cm.core.RecordSource} interface).<p>
 *
 * This class is a refactored version of the {@link Blocker} class. The
 * ABA algorithm has moved to a {@link BlockingSetFactory} class
 * for improved testability.
 * @author Martin Buechi
 * @author rphall (refactoring)
 * @version   $Revision: 1.1 $ $Date: 2010/03/24 21:30:24 $
 */
public class Blocker2 implements AutomatedBlocker {

	private static Logger logger = Logger.getLogger(Blocker2.class.getName());

	/**
	 * The name of a system property that can be set to "true" to force the
	 * comparison of BlockingSets produced by this class to BlockingSets
	 * produced by the original {@link Blocker} class.
	 */
	public static final String PN_SANITY_CHECK =
		"com.choicemaker.cm.io.blocking.automated.base.BlockerSanityCheck";

	// Don't use this variable directly; use isSanityCheckRequested() instead
	private static Boolean _isSanityCheckRequested = null;

	/**
	 * Checks the system property {@link #PN_SANITY_CHECK}
	 * and caches the result
	 */
	private static boolean isSanityCheckRequested() {
		if (_isSanityCheckRequested == null) {
			String value = System.getProperty(PN_SANITY_CHECK, "false");
			_isSanityCheckRequested = Boolean.valueOf(value);
		}
		boolean retVal = _isSanityCheckRequested.booleanValue();
		return retVal;
	}

	private final ImmutableProbabilityModel model;
	private final BlockingConfiguration blockingConfiguration;
	private final DatabaseAccessor databaseAccessor;
	private final CountSource countSource;
	private final Record q;
	private final int limitPerBlockingSet;
	private final int singleTableBlockingSetGraceLimit;
	private final int limitSingleBlockingSet;
	private List blockingSets;
	private int numberOfRecordsRetrieved;
	private String name;

	/**
	 * @param databaseAccessor
	 * @param model
	 * @param q
	 * @see #createBlockingSets(BlockingConfiguration,Record,int,int,int,CountSource)
	 * for explanations of the limit parameters, return value, and possible
	 * exceptions.
	 */
	public Blocker2(
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
			(String) model.properties().get(ImmutableProbabilityModel.PN_DATABASE_CONFIGURATION),
			(String) model.properties().get(ImmutableProbabilityModel.PN_BLOCKING_CONFIGURATION));
	}

	/**
	 * Uses default limit values obtained from the model.
	 * @param databaseAccessor
	 * @param model
	 * @param q
	 */
	public Blocker2(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q) {
		this(
			databaseAccessor,
			model,
			q,
			Integer.parseInt(
				(String) model.properties().get("limitPerBlockingSet")),
			Integer.parseInt(
				(String) model.properties().get(
					"singleTableBlockingSetGraceLimit")),
			Integer.parseInt(
				(String) model.properties().get("limitSingleBlockingSet")));
	}

	/**
	 * Uses default limit values obtained from the model.
	 * @param databaseAccessor
	 * @param model
	 * @param q
	 * @param dbConfigurationName
	 * @param blockingConfigurationName
	 */
	public Blocker2(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q,
		String dbConfigurationName,
		String blockingConfigurationName) {
		this(
			databaseAccessor,
			model,
			q,
			Integer.parseInt(
				(String) model.properties().get("limitPerBlockingSet")),
			Integer.parseInt(
				(String) model.properties().get(
					"singleTableBlockingSetGraceLimit")),
			Integer.parseInt(
				(String) model.properties().get("limitSingleBlockingSet")),
			(CountSource) model.properties().get("countSource"),
			dbConfigurationName,
			blockingConfigurationName);
	}

	/**
	 * @param databaseAccessor
	 * @param model
	 * @param q
	 * @param dbConfigurationName
	 * @param blockingConfigurationName
	 * @see #createBlockingSets(BlockingConfiguration,Record,int,int,int,CountSource)
	 * for explanations of the limit parameters, return value, and possible
	 * exceptions.
	 */
	public Blocker2(
		DatabaseAccessor databaseAccessor,
		ImmutableProbabilityModel model,
		Record q,
		int limitPerBlockingSet,
		int singleTableBlockingSetGraceLimit,
		int limitSingleBlockingSet,
		CountSource countSource,
		String dbConfigurationName,
		String blockingConfigurationName) {
		this.countSource = countSource;
		this.databaseAccessor = databaseAccessor;
		this.model = model;
		this.blockingConfiguration =
			((BlockingAccessor) model.getAccessor()).getBlockingConfiguration(
				blockingConfigurationName,
				dbConfigurationName);
		this.q = q;
		this.limitPerBlockingSet = limitPerBlockingSet;
		this.singleTableBlockingSetGraceLimit =
			singleTableBlockingSetGraceLimit;
		this.limitSingleBlockingSet = limitSingleBlockingSet;
	}

	public void open() throws IOException {

		this.numberOfRecordsRetrieved = 0;
		this.blockingSets =
			BlockingSetFactory.createBlockingSets(
				this.blockingConfiguration,
				this.q,
				this.limitPerBlockingSet,
				this.singleTableBlockingSetGraceLimit,
				this.limitSingleBlockingSet,
				this.countSource);
		databaseAccessor.open(this);

		// If processing has gotten this far, (i.e. an IncompleteBlockingSetsException
		// was not thrown), then the results from this class should be the same as
		// the ones from the original Blocker class.
		if (Blocker2.isSanityCheckRequested()) {
			logger.warning("SanityCheck is slowing down blocking");
			logger.info(
				"Comparing BlockingSets to ones from original Blocker class...");
			doSanityCheck();
			logger.info(
				"... Finished comparing BlockingSets to ones from original Blocker class.");
		}

	}

	/**
	 * Throws an IllegalStateException if the Blocker class
	 * does not produce the same blocking sets as this class.
	 */
	private void doSanityCheck() throws IOException {
		DatabaseAccessor clone = null;
		try {
			clone = this.getDatabaseAccessor().cloneWithNewConnection();
		} catch (CloneNotSupportedException x) {
			String msg =
				"Unable to perform sanity check because database accessor can't be cloned";
			logger.warning(msg);
		}

		// Verify that the clone is different from the original
		// (a sanity check within a sanity check -- which is slightly insane)
		if (clone == this.getDatabaseAccessor()) {
			String msg =
				"Unable to perform sanity check because database accessor hasn't been cloned";
			logger.warning(msg);
			clone = null;
		}

		if (clone != null) {
			Blocker sanityCheck =
				new Blocker(
					clone,
					this.getModel(),
					this.getQueryRecord(),
					this.getLimitPerBlockingSet(),
					this.getSingleTableBlockingSetGraceLimit(),
					this.getLimitSingleBlockingSet(),
					this.getCountSource(),
					this.getBlockingConfiguration());
			sanityCheck.open();
			List newBlockingSets = this.getBlockingSets();
			List oldBlockingSets = sanityCheck.getBlockingSets();
			if (newBlockingSets.size() != oldBlockingSets.size()) {
				throw new IllegalStateException("Different sizes of blocking set collections");
			}
			for (int i = 0; i < newBlockingSets.size(); i++) {
				BlockingSet newBlockingSet =
					(BlockingSet) newBlockingSets.get(i);
				BlockingSet oldBlockingSet =
					(BlockingSet) oldBlockingSets.get(i);
				if (newBlockingSet == null && oldBlockingSet != null) {
					throw new IllegalStateException(
						"Blocking sets " + i + " are different");
				} else if (newBlockingSet != null && !newBlockingSet.equals(oldBlockingSet)) {
					throw new IllegalStateException(
						"Blocking sets " + i + " are different");
				}
			}
			sanityCheck.close();
		} // if clone
	} // end doSanityCheck()

	public Accessor getAccessor() {
		return model.getAccessor();
	}

	public BlockingAccessor getBlockingAccessor() {
		return (BlockingAccessor) model.getAccessor();
	}

	public List getBlockingSets() {
		return blockingSets;
	}

	public void close() throws IOException {
		databaseAccessor.close();
	}

	public boolean hasNext() {
		return databaseAccessor.hasNext();
	}

	public Record getNext() throws IOException {
		++this.numberOfRecordsRetrieved;
		return databaseAccessor.getNext();
	}

	public boolean hasSink() {
		return false;
	}

	public Sink getSink() {
		throw new UnsupportedOperationException("no sink");
	}

	public int getNumberOfRecordsRetrieved() {
		return this.numberOfRecordsRetrieved;
	}

	public String getFileName() {
		throw new UnsupportedOperationException("not file based");
	}

	public ImmutableProbabilityModel getModel() {
		return model;
	}

	public void setModel(ImmutableProbabilityModel m) {
		throw new UnsupportedOperationException("can't change model after construction");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BlockingConfiguration getBlockingConfiguration() {
		return blockingConfiguration;
	}

	public DatabaseAccessor getDatabaseAccessor() {
		return databaseAccessor;
	}

	public CountSource getCountSource() {
		return countSource;
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

}

