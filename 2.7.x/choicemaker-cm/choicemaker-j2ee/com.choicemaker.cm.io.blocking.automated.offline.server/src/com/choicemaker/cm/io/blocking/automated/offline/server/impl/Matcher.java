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
package com.choicemaker.cm.io.blocking.automated.offline.server.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ClueSet;
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.ActiveClues;
import com.choicemaker.cm.core.base.Evaluator;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSources;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSources;
import com.choicemaker.cm.io.blocking.automated.offline.impl.SimpleControl;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.utils.MemoryEstimator;

/**
 * This message bean compares the pairs given to it and sends a list of matches to the match writer bean.
 *
 * @deprecated
 *
 * @author pcheung
 *
 */
public class Matcher implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Matcher.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + Matcher.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
	private transient OABAConfiguration oabaConfig = null;

	private transient Evaluator evaluator;
	private transient ClueSet clueSet;
	private transient boolean[] enabledClues;
	private transient float low;
	private transient float high;

//	private long inReadHM;

	public void ejbCreate() {
		log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc)
		throws EJBException {
			this.mdc = mdc;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					//start matching
					data = ((StartData) o);

					log.debug("Matcher In onMessage " + data.ind);

					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					batchJob = configuration.findBatchJobById(data.jobID);

					IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
					IProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);
					//set up the clues and evaluators.
					this.evaluator = stageModel.getEvaluator();
					this.clueSet = stageModel.getClueSet();
					this.enabledClues = stageModel.getCluesToEvaluate();

					low = data.low;
					high = data.high;

					String temp = (String) stageModel.properties().get("maxMatchSize");
					int maxMatch = Integer.parseInt(temp);

					//max block size
					temp = (String) stageModel.properties().get("maxBlockSize");
					int maxBlock = Integer.parseInt(temp);

					//get the stage data
					IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
					IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
					IComparisonSetSources sources = new ComparisonTreeSetSources (
						oabaConfig.getComparisonTreeFactory(data.stageType));
					IComparisonSetSources sourcesO = new ComparisonSetOSSources (
						oabaConfig.getComparisonArrayFactoryOS(), maxBlock);

					RecordSource stage = null;
					RecordSource master = null;

					//now get to the right chunk
					IComparisonSetSource source = null;
					for (int i=0; i<=data.ind; i++) {
						stage = stageFactory.getNextSource();
						master = masterFactory.getNextSource();

						if (sources.hasNextSource()) source = sources.getNextSource ();
						else if (sourcesO.hasNextSource()) source = sourcesO.getNextSource();
						else throw new BlockingException ("Could not open any comparison set source for chunk " + i);
					}
					if (source == null) {
						throw new BlockingException("null source at index " + data.ind);
					}

					if (log.isInfoEnabled()) {
						String sourceInfo = source.getInfo();
						log.info (getID () + " matching chunk " + data.ind + " " + sourceInfo);
					}

					//start matching for this chunk
					HashMap stageMap = getRecords (stage, stageModel);
					HashMap masterMap = getRecords (master, masterModel);

					MemoryEstimator.writeMem();

					//result of the matches
					ArrayList matches = new ArrayList ();

					int compares = 0;
					int sets = 0;

					source.open();
					while (source.hasNext()) {
						sets ++;
						IComparisonSet cSet = source.getNextSet();
						while (cSet.hasNextPair()) {
							ComparisonPair p = cSet.getNextPair();

							compares ++;

							Record q = (Record) stageMap.get(p.id1);
							Record m;
							if (p.isStage) m =  (Record) stageMap.get(p.id2);
							else m =  (Record) masterMap.get(p.id2);

							MatchRecord2 match = compareRecords (q, m, p.isStage);
							if (match != null) {
								matches.add(match);
							}
						}
					}

					source.close();

					log.info("Chunk: " + data.ind + ", sets: " + sets + ", compares: " + compares +
						", matches: " + matches.size());

					//free up resources
					stageMap = null;
					masterMap = null;

/*
					MatchWriterData mwd = new MatchWriterData (data);
					mwd.matches = matches;
					mwd.numCompares = compares;

					//send to the write matches bean
					long t = System.currentTimeMillis();
					sendToWriteMatches (mwd);
					t = System.currentTimeMillis() - t;

					log.info("time for message sending " + t);
*/

					long t = System.currentTimeMillis();
					writeMatches (matches, data.ind, maxMatch);
					t = System.currentTimeMillis() - t;

					log.debug("Time in writeMatches " + t);

					MatchWriterData mwd = new MatchWriterData (data);
					mwd.numCompares = compares;
					mwd.timeWriting = t;
					mwd.numMatches = matches.size();

					sendToMatchScheduler (mwd);

					//free up resources
					matches = null;

				} else {
					log.warn("wrong type: " + inMessage.getClass().getName());
				}

			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			try {
				if (batchJob != null) batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	private void writeMatches (ArrayList matches, int ind, int maxMatch) throws BlockingException {
		IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(ind);
		IComparableSink sink =  new ComparableMRSink (mSink);

		IMatchRecord2SinkSourceFactory factory = oabaConfig.getMatchTempFactory(ind);
		ComparableMRSinkSourceFactory fac = new ComparableMRSinkSourceFactory (factory);

		IComparableSource source = new ComparableArraySource (matches);

		SimpleControl control = new SimpleControl ();
		GenericDedupService service = new GenericDedupService (source, sink, fac, maxMatch, control);
		service.runDedup();

		int i = service.getNumBefore();
		int j = service.getNumAfter();

		log.info("numBefore: " + i + " numAfter: " + j);
	}


	/** This method sends the message to the match result write bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	private void sendToMatchScheduler (MatchWriterData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	}


	/** This method gets the data in the RecordSource and puts them into a hash map.
	 *
	 * @param rs - RecordSource
	 * @param accessProvider - ProbabilityModel
	 * @return
	 */
	private HashMap getRecords (RecordSource rs, IProbabilityModel model) throws BlockingException {
//		long t = System.currentTimeMillis();

		HashMap records = new HashMap ();

		try {
			if (rs != null && model != null) {
				rs.setModel(model);
				rs.open();

				// put the whole chunk dataset into memory.
				while (rs.hasNext()) {
					Record r = rs.getNext();
					Object O = r.getId();

					records.put(O, r);
				}

				rs.close();
			}
		} catch (IOException ex) {
			throw new BlockingException (ex.toString());
		}

//		inReadHM += System.currentTimeMillis() - t;

		return records;
	}


	/** This method compares two records and returns a MatchRecord2 object.
	 *
	 * @param q - first record
	 * @param m - second record
	 * @param isStage - indicates if the second record is staging or master
	 * @return
	 */
	private MatchRecord2 compareRecords (Record q, Record m, boolean isStage) {

		MatchRecord2 mr = null;

		if ((q != null) && (m != null)) {
			ActiveClues activeClues = clueSet.getActiveClues(q, m, enabledClues);
			float matchProbability = evaluator.getProbability(activeClues);
			Decision decision = evaluator.getDecision(activeClues, matchProbability, low, high);

			//char source = 'D';
			char source = MatchRecord2.MASTER_SOURCE;

			Comparable i1 = q.getId();
			Comparable i2 = m.getId();

			if (isStage) {
				//source = 'S';
				source = MatchRecord2.STAGE_SOURCE;

				//make sure the smaller id is first
				if (i1.compareTo(i2) > 0) {
					Comparable i3 = i1;
					i1 = i2;
					i2 = i3;
				}
			}

			// 2009-08-17 rphall
			// BUG? clue notes are dropped here
			final String noteInfo = null;
			if (decision == Decision.MATCH) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.MATCH,noteInfo);
			} else if (decision == Decision.DIFFER) {
			} else if (decision == Decision.HOLD) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.HOLD,noteInfo);
			}
			// END BUG?

		}

		return mr;
	}


	/** This returns an unique id for each instance of the object.
	 *
	 * @return
	 */
	public String getID () {
		String str = this.toString();
		int i = str.indexOf('@');
		return str.substring(i+1);
	}

}
