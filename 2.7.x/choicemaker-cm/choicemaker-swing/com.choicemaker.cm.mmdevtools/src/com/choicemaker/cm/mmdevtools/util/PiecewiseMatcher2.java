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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.ImmutableMarkedRecordPair;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.ImmutableRecordPair;
import com.choicemaker.cm.core.MarkedRecordPairSink;
import com.choicemaker.cm.core.RecordPairSink;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MutableMarkedRecordPair;
import com.choicemaker.cm.core.blocking.InMemoryBlocker;
import com.choicemaker.cm.core.util.BoundedQueue;
import com.choicemaker.cm.core.util.StringUtils;

/**
 * Description
 * 
 * @author  Martin Buechi
 * @version $Revision: 1.2 $ $Date: 2010/03/29 14:24:58 $
 */
public class PiecewiseMatcher2 {

	private static Logger logger = Logger.getLogger(PiecewiseMatcher2.class);

	public static final String INDEXED_SOURCE = "Indexed Source";
	public static final String Q_SOURCE = "Q Source";
	public static final String RECORDS_READ = "Records Read";
	public static final String RECORDS_TREATED_AS_SOURCE = "Records Treated as Souce";
	public static final String RECORDS_BLOCKED = "Total Records Blocked";
	public static final String MATCHES_CREATED = "Matches Created";

	public static final String DONE = "DONE";

	private RecordSource[] sources;
	private MarkedRecordPairSink sink;
	private InMemoryBlocker blocker;
	private ImmutableProbabilityModel probabilityModel;
	private float lowerThreshold;
	private float upperThreshold;
	private String user;
	private String src;
	private int numThreads;

	private PropertyChangeSupport propertyChangeListeners;
	private int numRecordsFromSmall;
	private int numRecordsFromLarge;
	private int numPairs;
	private boolean done;

	private DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);

	public PiecewiseMatcher2(RecordSource[] sources, 
				   MarkedRecordPairSink sink, 
				   InMemoryBlocker blocker,
				   ImmutableProbabilityModel probabilityModel,
				   float lowerThreshold,
				   float upperThreshold,
				   String user,
				   String src, 
				   int numThreads) {
		propertyChangeListeners = new PropertyChangeSupport(this);

		this.sources = sources;
		this.sink = sink;
		this.blocker = blocker;
		this.probabilityModel = probabilityModel;
		this.lowerThreshold = lowerThreshold;
		this.upperThreshold = upperThreshold;
		this.user = user;
		this.src = src;
		this.numThreads = numThreads;
	}

	public void match() throws IOException {
			
		RecordPairQueue queue = new RecordPairQueue(10000);
		
		PiecewiseRecordPairProducer producer = 
			new PiecewiseRecordPairProducer(this.sources, this.blocker, this.probabilityModel, queue.getSink());
		Thread producerThread = new Thread(producer);
		
		MarkedRecordPairSink synchronizedMrpSink = new SynchronizedMarkedRecordPairSink(this.sink);
		synchronizedMrpSink.open();
		
		RecordPairConsumer[] consumers = new RecordPairConsumer[numThreads];
		Thread[] consumerThreads = new Thread[consumers.length];
		for (int i = 0; i < consumers.length; i++) {
			consumers[i] = 
				new RecordPairConsumer(queue, 
									   this.probabilityModel, 
									   this.lowerThreshold, 
									   this.upperThreshold, 
									   this.user, 
									   this.src,
									   synchronizedMrpSink);
			consumerThreads[i] = new Thread(consumers[i]);
		}

		logger.info("Starting producer thread.");
		producerThread.start();
		
		logger.info("Starting consumer threads.");
		for (int i = 0; i < consumerThreads.length; i++) {
			consumerThreads[i].start();
		}
		
		logger.info("Started all threads.");
		
		try {
			producerThread.join();
		} catch (InterruptedException ex) { 
			ex.printStackTrace();
		}
		logger.info("Producer thread terminated.");
		
		for (int i = 0; i < consumerThreads.length; i++) {
			try {
				consumerThreads[i].join();
			} catch (InterruptedException ex) {
				// should never happen
			}
		}
		logger.info("Consumer threads terminated.");
		
		synchronizedMrpSink.close();
		
		logger.info("Done matching.");
		
		long numRead = producer.getNumRead();
		long numPairsProduced = producer.getNumPairsProduced();
		
		long numPairsCompared = 0;
		long numPairsOutput = 0;
		StringBuffer numComparedBuff = new StringBuffer(" [");
		StringBuffer numPairsOutputBuff = new StringBuffer(" [");
		for (int i = 0; i < consumers.length; i++) {
			numPairsCompared += consumers[i].getNumCompared();
			numPairsOutput += consumers[i].getNumPairsOutput();
			
			numComparedBuff.append(consumers[i].getNumCompared() + ", ");
			numPairsOutputBuff.append(consumers[i].getNumPairsOutput() + ", ");
		}
		numComparedBuff.setLength(numComparedBuff.length() - 2);
		numComparedBuff.append("]");
		numPairsOutputBuff.setLength(numPairsOutputBuff.length() - 2);
		numPairsOutputBuff.append("]");
		
		logger.info("Records read:                 " + StringUtils.padLeft("" + numRead, 20, ' '));
		logger.info("Pairs produced:               " + StringUtils.padLeft("" + numPairsProduced, 20, ' '));
		logger.info("Pairs compared:               " + StringUtils.padLeft("" + numPairsCompared, 20, ' '));
		logger.info("    by Thread: " + numComparedBuff);
		logger.info("Pairs output (M, H, & note):  " + StringUtils.padLeft("" + numPairsOutput, 20, ' '));
		logger.info("    by Thread: " + numPairsOutputBuff);

		setDone();		
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	private void firePropertyChange(final String propertyName, Object oldValue, Object newValue) {
		propertyChangeListeners.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	//
	// Output Methods
	//

	private void setDone() {
		firePropertyChange(DONE, Boolean.FALSE, Boolean.TRUE);
	}

	//
	// 
	//
	
	static class RecordPairConsumer implements Runnable {
	
		private RecordPairQueue queue;
		private ImmutableProbabilityModel model;
		private float lt;
		private float ut;
		private String user;
		private String src;
		private MarkedRecordPairSink sink;
	
		private long numCompared;
		private long numPairsOutput;
	
		public RecordPairConsumer(RecordPairQueue queue, 
								  ImmutableProbabilityModel model,
								  float lowerThreshold,
								  float upperThreshold,
								  String user,
								  String src,
								  MarkedRecordPairSink sink) {
			this.queue = queue;
			this.model = model;
			this.lt = lowerThreshold;
			this.ut = upperThreshold;
			this.user = user;
			this.src = src;
			this.sink = sink;	
		}
		
		public long getNumCompared() {
			return numCompared;
		}
		
		public long getNumPairsOutput() {
			return numPairsOutput;
		}
		
		public void run() {
			numCompared = 0;
			numPairsOutput = 0;
			
			Evaluator evaluator = null;
			synchronized (model) {
				evaluator = model.getEvaluator();
			}
			
			try {
				ImmutableRecordPair pair;
				while ((pair = queue.getNext()) != null) {
					Match m = evaluator.getMatch(pair.getQueryRecord(), pair.getMatchRecord(), lt, ut);
					numCompared++;

					if (m != null) {
						MutableMarkedRecordPair mrp = new MutableMarkedRecordPair(pair.getQueryRecord(), pair.getMatchRecord(), m.decision, new Date(), user, src, String.valueOf(m.probability));
						mrp.setActiveClues(m.ac);
						mrp.setProbability(m.probability);
						mrp.setCmDecision(m.decision);
			
						sink.put(mrp);
						numPairsOutput++;
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	static class SynchronizedMarkedRecordPairSink implements MarkedRecordPairSink {

		private MarkedRecordPairSink inner;

		public SynchronizedMarkedRecordPairSink(MarkedRecordPairSink sink) {
			this.inner = sink;
		}

		public synchronized void putMarkedRecordPair(ImmutableMarkedRecordPair r) throws IOException {
			inner.putMarkedRecordPair(r);
		}

		public synchronized void put(ImmutableRecordPair r) throws IOException {
			inner.put(r);
		}

		public synchronized void open() throws IOException {
			inner.open();
		}

		public synchronized void close() throws IOException {
			inner.close();
		}

		public synchronized String getName() {
			return inner.getName();
		}

		public synchronized void setName(String name) {
			inner.setName(name);
		}

		public synchronized ImmutableProbabilityModel getModel() {
			return inner.getModel();
		}

		public synchronized void setModel(ImmutableProbabilityModel m) {
			inner.setModel(m);
		}
		
		/**
		 * NOP for now
		 * @see com.choicemaker.cm.Sink#flush()
		 */
		public void flush() throws IOException {
		}
		
	}
	
	static class RecordPairQueue {
	
		private BoundedQueue buffer;
	
		private RecordPairSink sink;
	
		public RecordPairQueue(int size) {
			this.buffer = new BoundedQueue(size);
			
			this.sink = new RPQSink();
		}

		/**
		 * Suitable for passing to a PiecewiseRecordPairProducer.
		 */
		public RecordPairSink getSink() {
			return sink;
		}
		
		/**
		 * The EvaluatorThreads call this 
		 */
		public ImmutableRecordPair getNext() {
			return (ImmutableRecordPair)buffer.get();
		}
		
		private class RPQSink implements RecordPairSink {

			/**
			 * This call can be ignored...
			 */
			public void open() { }

			public void put(ImmutableRecordPair r) {
				buffer.put(r);
			}

			public void close() {
				buffer.close();
			}

			/**
			 * Ignorable...
			 */
			public void setModel(ImmutableProbabilityModel m) { }

			public ImmutableProbabilityModel getModel() { throw new UnsupportedOperationException(); }
			public String getName() { throw new UnsupportedOperationException(); }
			public void setName(String name) { throw new UnsupportedOperationException(); }
			
			/**
			 * NOP for now
			 * @see com.choicemaker.cm.Sink#flush()
			 */
			public void flush() throws IOException {
			}
		
		}
		
		
	}
	
}
