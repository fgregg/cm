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
package com.choicemaker.cm.io.blocking.automated.inmemory;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import com.choicemaker.cm.args.AbaSettings;
import com.choicemaker.cm.args.RecordAccess;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.IBlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.UnderspecifiedQueryException;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;

/**
 * @author ajwinkel
 */
public class InMemoryAutomatedBlocker implements InMemoryBlocker {

	// passed by constructor.
	private final ImmutableProbabilityModel model;
	private final int limitPerBlockingSet;
	private final int singleTableBlockingSetGraceLimit;
	private final int limitSingleBlockingSet;
	private final String dbConfiguration;
	private final String blockingConfiguration;

	// created by init().
	private InMemoryDataSource imds;
	private AbaStatistics abaStatistics;

	public InMemoryAutomatedBlocker(ImmutableProbabilityModel model,
			RecordAccess dbParams, AbaSettings abaSettings) {
		this(model, abaSettings.getLimitPerBlockingSet(), abaSettings
				.getSingleTableBlockingSetGraceLimit(), abaSettings
				.getLimitSingleBlockingSet(), model
				.getDatabaseConfigurationName(), dbParams
				.getBlockingConfiguration());

	}

	public InMemoryAutomatedBlocker(ImmutableProbabilityModel model,
			int limitPerBlockingSet, int singleTableBlockingSetGraceLimit,
			int limitSingleBlockingSet, String dbConfiguration,
			String blockingConfiguration) {
		if (model == null || dbConfiguration == null
				|| blockingConfiguration == null) {
			throw new IllegalArgumentException("null argument");
		}
		if (dbConfiguration.isEmpty() || blockingConfiguration.isEmpty()) {
			throw new IllegalArgumentException("blank argument");
		}
		if (limitPerBlockingSet < 1 || singleTableBlockingSetGraceLimit < 1
				|| limitSingleBlockingSet < 1) {
			throw new IllegalArgumentException("illegal limit");
		}
		this.model = model;
		this.limitPerBlockingSet = limitPerBlockingSet;
		this.singleTableBlockingSetGraceLimit =
			singleTableBlockingSetGraceLimit;
		this.limitSingleBlockingSet = limitSingleBlockingSet;
		this.dbConfiguration = dbConfiguration;
		this.blockingConfiguration = blockingConfiguration;
	}

	public void init(List records) {
		// the blocking configuration
		IBlockingConfiguration bc =
			((BlockingAccessor) model.getAccessor()).getBlockingConfiguration(
					blockingConfiguration, dbConfiguration);

		// the data source
		this.imds = new InMemoryDataSource(bc);
		this.imds.init(records);

		// the count source
		this.abaStatistics = imds.createCountSource();
	}

	public void clear() {
		this.imds = null;
		this.abaStatistics = null;
	}

	public RecordSource block(Record q) {
		return block(q, -1);
	}

	public RecordSource block(Record q, int start) {
		AutomatedBlocker blocker = createBlocker(q, start);
		return new BlockerWrapper(blocker);
	}

	private AutomatedBlocker createBlocker(Record q, int start) {
		return new Blocker2(new InMemoryDatabaseAccessor(imds, start), model,
				q, limitPerBlockingSet, singleTableBlockingSetGraceLimit,
				limitSingleBlockingSet, abaStatistics, dbConfiguration,
				blockingConfiguration);
	}

	/**
	 * The BlockerWrapper class wraps a Blocker to catch
	 * UnderspecifiedQueryExceptions, which basically mean that the ABA couldn't
	 * produce a selective enough query.
	 * 
	 * In this case, we simply return no records.
	 */
	private static class BlockerWrapper implements RecordSource {

		private AutomatedBlocker blocker;
		private boolean threwException;

		public BlockerWrapper(AutomatedBlocker blocker) {
			this.blocker = blocker;

			if (blocker == null) {
				throw new IllegalArgumentException();
			}
		}

		public void open() throws IOException {
			threwException = true;
			try {
				blocker.open();
				threwException = false;
			} catch (UnderspecifiedQueryException ex) {
			}

		}

		public boolean hasNext() throws IOException {
			return !threwException && blocker.hasNext();
		}

		public Record getNext() throws IOException {
			if (!threwException) {
				return blocker.getNext();
			} else {
				throw new NoSuchElementException();
			}
		}

		public void close() throws IOException {
			blocker.close();
			blocker = null;
		}

		public String getName() {
			throw new UnsupportedOperationException();
		}

		public void setName(String name) {
			throw new UnsupportedOperationException();
		}

		public ImmutableProbabilityModel getModel() {
			throw new UnsupportedOperationException();
		}

		public void setModel(ImmutableProbabilityModel m) {
			throw new UnsupportedOperationException();
		}

		public boolean hasSink() {
			throw new UnsupportedOperationException();
		}

		public Sink getSink() {
			throw new UnsupportedOperationException();
		}

		public String getFileName() {
			throw new UnsupportedOperationException();
		}

	}

}
