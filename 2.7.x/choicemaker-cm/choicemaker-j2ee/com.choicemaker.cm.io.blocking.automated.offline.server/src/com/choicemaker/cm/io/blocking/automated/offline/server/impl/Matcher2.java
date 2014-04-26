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

import java.rmi.RemoteException;
import java.util.ArrayList;

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
import com.choicemaker.cm.core.Decision;
import com.choicemaker.cm.core.Evaluator;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Match;
import com.choicemaker.cm.core.PMManager;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.io.blocking.automated.offline.core.ComparisonPair;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSet;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonSetOSSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.ChunkDataStore;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.utils.ControlChecker;

/**
 * This message bean compares the pairs given to it and sends a list of matches to the match writer bean.
 *
 * In this version, there is only one chunk data in memory and different processors work on different
 * trees/arrays of this chunk.
 *
 * @author pcheung
 *
 */
public class Matcher2 implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Matcher2.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + Matcher2.class.getName());

	protected static final int INTERVAL = 50000;

	private transient MessageDrivenContext mdc = null;
	protected transient EJBConfiguration configuration = null;
	protected transient OABAConfiguration oabaConfig = null;

	private transient Evaluator evaluator;
	//private transient ClueSet clueSet;
	//private transient boolean[] enabledClues;
	protected transient float low;
	protected transient float high;

	protected StartData data;

	//These two tracker are set only in log debug mode
	private long inHMLookup;
	private long inCompare;

	//number of comparisons made
	protected int compares;

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


	protected void setHighLow () {
		low = data.low;
		high = data.high;
	}


	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		BatchJob batchJob = null;

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					//start matching
					data = ((StartData) o);

					log.debug("Matcher2 In onMessage " + data.jobID + " " + data.ind
						+ " " + data.treeInd);

					oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);
					batchJob = configuration.findBatchJobById(data.jobID);
					IStatus status = configuration.getStatusLog(data);

					if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
						MessageBeanUtils.stopJob (batchJob, status, oabaConfig);

					} else {
						handleMatching (data, batchJob);
					}


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
			log.error(e);
			assert batchJob != null;
			try {
				batchJob.markAsFailed();
			} catch (RemoteException e1) {
				log.error(e1.toString(),e1);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	private void handleMatching (StartData data, BatchJob batchJob)
		throws BlockingException, RemoteException, NamingException, JMSException {

		IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
		// 2014-04-24 rphall: Commented out unused local variable.
//		ImmutableProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);

		//set up the clues and evaluators.
		this.evaluator = stageModel.getEvaluator();
		//this.clueSet = stageModel.getClueSet();
		//this.enabledClues = stageModel.getCluesToEvaluate();

		setHighLow ();

		String temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		temp = (String) stageModel.properties().get("maxMatchSize");
		// 2014-04-24 rphall: Commented out unused local variable.
//		int maxMatch = Integer.parseInt(temp);

		//max block size
		temp = (String) stageModel.properties().get("maxBlockSize");
		int maxBlock = Integer.parseInt(temp);

		//get the data store
		ChunkDataStore dataStore = ChunkDataStore.getInstance ();

		//get the right source
		IComparisonSetSource source = getSource (numProcessors, maxBlock);

		log.info (getID () + " matching " + source.getInfo());

		new ArrayList ();

		compares = 0;
		int sets = 0;

		inHMLookup = 0;
		inCompare = 0;

		source.open();

		IComparisonSet cSet;
		// 2014-04-24 rphall: Commented out unused local variables.
//		ComparisonPair p;
//		Record q, m;
//		MatchRecord2 match;

		int numMatches = 0;

		while (source.hasNext()) {
			sets ++;

			cSet = source.getNextSet();

			ArrayList matches =  handleComparisonSet (cSet, batchJob, dataStore, stageModel);
			numMatches += matches.size();
			writeMatches (matches);
		}

		source.close();

		log.info("Chunk: " + data.ind + "_" +  data.treeInd + ", sets: " + sets +
			", compares: " + compares + ", matches: " + numMatches);

		long t = System.currentTimeMillis();
		t = System.currentTimeMillis() - t;

		log.debug("Times: lookup " + inHMLookup + " compare: " + inCompare
			+ " writeMatches: " + t);

		MatchWriterData mwd = new MatchWriterData (data);
		mwd.numCompares = compares;
		mwd.timeWriting = t;
		mwd.inCompare = inCompare;
		mwd.inLookup = inHMLookup;
		mwd.numMatches = numMatches;

		sendToMatchScheduler (mwd);
	}


	/** This method handles the comparisons of a IComparisonSet.  It returns an
	 * ArrayList of MatchRecord2 produced by this IComparisonSet.
	 *
	 * @param cSet
	 * @param batchJob
	 * @param dataStore
	 * @param stageModel
	 * @return
	 * @throws RemoteException
	 * @throws BlockingException
	 */
	protected ArrayList handleComparisonSet (IComparisonSet cSet, BatchJob batchJob,
		ChunkDataStore dataStore, IProbabilityModel stageModel)
		throws RemoteException, BlockingException {

		boolean stop = batchJob.shouldStop();
		ComparisonPair p;
		Record q, m;
		MatchRecord2 match;

		ArrayList matches = new ArrayList ();

		while (cSet.hasNextPair() && !stop) {
			p = cSet.getNextPair();

			compares ++;

			stop = ControlChecker.checkStop (batchJob, compares, INTERVAL);

			q = getQ(dataStore, p);
			m = getM(dataStore, p);

			//major problem if this happens
			if (p.id1.equals(p.id2) && p.isStage) {
				log.error("id1 = id2: " + p.id1.toString());
				throw new BlockingException ("id1 = id2");
			}

			match = compareRecords (q, m, p.isStage, stageModel);
			if (match != null) {
				matches.add(match);
			}

			//debug
			/*
			if (isDebug(p)) {
				if (match != null) {
					log.info(q.getId().toString() + " " + m.getId().toString()
					+ " " + match.getProbability());
				} else {
					log.info(q.getId().toString() + " " + m.getId().toString()
					+ " null");
				}
			}
			*/
		}
		return matches;
	}




	/** This method returns true if the ComparisonPair contains a record we are interested
	 * in.
	 *
	 * @param p
	 * @return
	 */
	private boolean isDebug (ComparisonPair p) {
		Long l1 = new Long (1926738);
		Long l2 = new Long (1935180);
		Long l3 = new Long (1954035);
		Long l4 = new Long (1956168);
		Long l5 = new Long (1955320);
		Long l6 = new Long (1991943);

		if ((p.id1.equals(l1) && p.id2.equals(l2)) ||
			(p.id1.equals(l3) && p.id2.equals(l4)) ||
			(p.id1.equals(l5) && p.id2.equals(l6))
		) return true;
		else return false;
	}


	protected Record getQ (ChunkDataStore dataStore, ComparisonPair p) {
		long t = 0;
		if (log.isDebugEnabled()) t = System.currentTimeMillis();

		Record r = (Record) dataStore.getStage(p.id1);

		if (log.isDebugEnabled()) {
			t = System.currentTimeMillis() - t;
			inHMLookup += t;
		}

		return r;
	}


	protected Record getM (ChunkDataStore dataStore, ComparisonPair p) {
		long t = 0;
		if (log.isDebugEnabled()) t = System.currentTimeMillis();

		Record r = null;
		if (p.isStage) r =  (Record) dataStore.getStage(p.id2);
		else r =  (Record) dataStore.getMaster(p.id2);

		if (log.isDebugEnabled()) {
			t = System.currentTimeMillis() - t;
			inHMLookup += t;
		}

		return r;
	}


	/** This method returns the correct tree/array file for this chunk.
	 *
	 * @param num - number of processors
	 * @param chunkId - chunk id
	 * @param treeId - tree id
	 * @return
	 */
	protected IComparisonSetSource getSource (int num, int maxBlockSize) throws BlockingException {
		if (data.ind < data.numRegularChunks) {
			//regular
			ComparisonTreeGroupSinkSourceFactory factory =
				oabaConfig.getComparisonTreeGroupFactory(data.stageType, num);
			IComparisonTreeSource source = factory.getSource(data.ind, data.treeInd);
			if (source.exists()) {
				IComparisonSetSource setSource = new ComparisonTreeSetSource (source);
				return setSource;
			} else {
				throw new BlockingException ("Could not get source " + source.getInfo());
			}
		} else {
			//oversized
			int i = data.ind - data.numRegularChunks;
			ComparisonArrayGroupSinkSourceFactory factoryOS =
				oabaConfig.getComparisonArrayGroupFactoryOS(num);
			IComparisonArraySource sourceOS = factoryOS.getSource(i, data.treeInd);
			if (sourceOS.exists()) {
				IComparisonSetSource setSource = new ComparisonSetOSSource (sourceOS, maxBlockSize);
				return setSource;
			} else {
				throw new BlockingException ("Could not get source " + sourceOS.getInfo ());
			}
		}
	}


	/** This method writes the matches of a IComparisonSet to the file
	 * corresponding to this matcher bean.
	 *
	 * @param matches
	 * @param ind
	 * @param maxMatch
	 * @throws BlockingException
	 */
	protected void writeMatches (ArrayList matches) throws BlockingException {
		//first figure out the correct file for this processor
		IMatchRecord2Sink mSink = oabaConfig.getMatchChunkFactory().getSink(data.treeInd);
		IComparableSink sink =  new ComparableMRSink (mSink);

		//write matches to this file.
		sink.append();
		sink.writeComparables(matches.iterator());
		sink.close();

	}


	/** This method sends the message to the match result write bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	protected void sendToMatchScheduler (MatchWriterData data) throws NamingException, JMSException{
		Queue queue = configuration.getMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	}



	/** This method compares two records and returns a MatchRecord2 object.
	 *
	 * @param clueSet
	 * @param enabledClues
	 * @param evaluator
	 * @param q - first record
	 * @param m - second record
	 * @param isStage - indicates if the second record is staging or master
	 * @param low
	 * @param high
	 * @return
	 */
	protected MatchRecord2 compareRecords (Record q, Record m, boolean isStage, ImmutableProbabilityModel model) {
		long t = 0;
		if (log.isDebugEnabled()) t = System.currentTimeMillis();

		MatchRecord2 mr = null;

		if ((q != null) && (m != null)) {
/*
			ActiveClues activeClues = clueSet.getActiveClues(q, m, enabledClues);
			float matchProbability = evaluator.getProbability(activeClues);
			Decision decision = evaluator.getDecision(activeClues, matchProbability, low, high);
			String info = null;
*/

			Match match = evaluator.getMatch(q, m, low, high);
			//no match
			if (match == null) return null;
			Decision decision = match.decision;
			float matchProbability = match.probability;

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

			String noteInfo = MatchRecord2.getNotesAsDelimitedString(match.ac,model);
			if (decision == Decision.MATCH) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.MATCH, noteInfo);
			} else if (decision == Decision.DIFFER) {
			} else if (decision == Decision.HOLD) {
				mr = new MatchRecord2 (i1, i2, source, matchProbability, MatchRecord2.HOLD, noteInfo);
			}

		}

		if (log.isDebugEnabled()) {
			t = System.currentTimeMillis() - t;
			inCompare += t;
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
