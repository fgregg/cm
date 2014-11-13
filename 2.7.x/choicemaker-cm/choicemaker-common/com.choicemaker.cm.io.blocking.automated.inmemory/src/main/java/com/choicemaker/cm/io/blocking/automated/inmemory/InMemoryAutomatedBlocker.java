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

import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.Sink;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.BlockingAccessor;
import com.choicemaker.cm.io.blocking.automated.base.BlockingConfiguration;
import com.choicemaker.cm.io.blocking.automated.base.CountSource;
import com.choicemaker.cm.io.blocking.automated.base.UnderspecifiedQueryException;

/**
 * @author ajwinkel
 *
 */
public class InMemoryAutomatedBlocker implements InMemoryBlocker {

	
	// passed by constructor.
	private ImmutableProbabilityModel model;
	private int limitPerBlockingSet;
	private int singleTableBlockingSetGraceLimit;
	private int limitSingleBlockingSet;
	private String dbConfiguration;
	private String blockingConfiguration;

	// created by init().
	private InMemoryDataSource imds;
	private CountSource countSource;
	
	public InMemoryAutomatedBlocker(ImmutableProbabilityModel model) throws IllegalArgumentException {
		this.model = model;
		try {
			this.limitPerBlockingSet = Integer.parseInt((String)model.properties().get("limitPerBlockingSet"));
			this.singleTableBlockingSetGraceLimit = Integer.parseInt((String)model.properties().get("singleTableBlockingSetGraceLimit"));
			this.limitSingleBlockingSet = Integer.parseInt((String)model.properties().get("limitSingleBlockingSet"));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Unable to find needed numeric blocking property for model");
		}
		this.dbConfiguration = model.getDatabaseConfigurationName();
		this.blockingConfiguration = model.getBlockingConfigurationName();
		
		if (dbConfiguration == null || blockingConfiguration == null) {
			throw new IllegalArgumentException("Unable to find dbConfiguration or blockingConfiguration property for model");
		}	
	}
	
	public InMemoryAutomatedBlocker(ImmutableProbabilityModel model,
									int limitPerBlockingSet,
									int singleTableBlockingSetGraceLimit,
									int limitSingleBlockingSet,
									String dbConfiguration,
									String blockingConfiguration) {
		this.model = model;
		this.limitPerBlockingSet = limitPerBlockingSet;
		this.singleTableBlockingSetGraceLimit = singleTableBlockingSetGraceLimit;
		this.limitSingleBlockingSet = limitSingleBlockingSet;
		this.dbConfiguration = dbConfiguration;
		this.blockingConfiguration = blockingConfiguration;
	}
	
	public void init(List records) {
		// the blocking configuration
		BlockingConfiguration bc = 
			((BlockingAccessor) model.getAccessor()).getBlockingConfiguration(blockingConfiguration, dbConfiguration);

		// the data source
		this.imds = new InMemoryDataSource(bc);
		this.imds.init(records);
						
		// the count source
		this.countSource = imds.createCountSource();		
	}
	
	public void clear() {
		this.imds = null;
		this.countSource = null;
	}

	public RecordSource block(Record q) {
		return block(q, -1);
	}

	public RecordSource block(Record q, int start) {
		AutomatedBlocker blocker = createBlocker(q, start);
		return new BlockerWrapper(blocker);
	}
	
	private AutomatedBlocker createBlocker(Record q, int start) {
		return new Blocker2(new InMemoryDatabaseAccessor(imds, start), 
						   model, 
						   q, 
						   limitPerBlockingSet, 
						   singleTableBlockingSetGraceLimit, 
						   limitSingleBlockingSet, 
						   countSource, 
						   dbConfiguration, 
						   blockingConfiguration);
	}
	
	/**
	 * The BlockerWrapper class wraps a Blocker to catch 
	 * UnderspecifiedQueryExceptions, which basically mean that
	 * the ABA couldn't produce a selective enough query.
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
			} catch (UnderspecifiedQueryException ex) { }
			
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

		public String getName() { throw new UnsupportedOperationException(); }
		public void setName(String name) { throw new UnsupportedOperationException(); }
		public ImmutableProbabilityModel getModel() { throw new UnsupportedOperationException(); }
		public void setModel(ImmutableProbabilityModel m) { throw new UnsupportedOperationException(); }
		public boolean hasSink() { throw new UnsupportedOperationException(); }
		public Sink getSink() { throw new UnsupportedOperationException(); }
		public String getFileName() { throw new UnsupportedOperationException(); }
		
	}

}
