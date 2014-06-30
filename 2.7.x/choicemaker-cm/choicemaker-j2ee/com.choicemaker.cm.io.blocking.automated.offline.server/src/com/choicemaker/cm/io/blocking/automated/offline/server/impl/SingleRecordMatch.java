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

import java.util.Iterator;
import java.util.SortedSet;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

// import com.choicemaker.cm.core.base.Accessor;

import com.choicemaker.cm.core.base.BlockingException;
import com.choicemaker.cm.core.base.IProbabilityModel;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidate;
import com.choicemaker.cm.core.base.MatchCandidateFactory;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.Record;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.core.base.RecordSource;
import com.choicemaker.cm.io.blocking.automated.base.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchDedupService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchingService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.RecValService2;
import com.choicemaker.cm.server.ejb.impl.CountsUpdate;


/**
 * This message bean performs single record matching on the staging record source.
 *
 * @author pcheung
 *
 */
public class SingleRecordMatch implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(SingleRecordMatch.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + SingleRecordMatch.class.getName());

	public static final String DATABASE_ACCESSOR = "com.choicemaker.cm.io.blocking.automated.base.databaseAccessor";
	public static final String MATCH_CANDIDATE = "com.choicemaker.cm.core.base.matchCandidate";

	private transient MessageDrivenContext mdc = null;
	private EJBConfiguration configuration = null;
	private transient QueueConnection connection = null;

	public SingleRecordMatch() {
//	log.debug("constuctor");
	}

	public void setMessageDrivenContext(MessageDrivenContext mdc) {
//		log.debug("setMessageDrivenContext()");
		this.mdc = mdc;
	}


	public void ejbCreate() {
//	log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();

		} catch (Exception e) {
	  log.error(e.toString(),e);
		}
//	log.debug("...finished ejbCreate");
	}



	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data;

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				long t = System.currentTimeMillis();

				log.info ("Starting Sinlge Record Match with maxSingle = " + data.maxCountSingle);

				OABAConfiguration oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);

				BatchJob batchJob = configuration.findBatchJobById(data.jobID);

				//final file
				IMatchRecord2Sink mSink = oabaConfig.getCompositeMatchSink(data.jobID);

				//run OABA on the staging data set.
				handleStageBatch (data, oabaConfig, mSink, batchJob);

				log.info("Time in dedup stage " + (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();

				//run single record match between stage and master.
				handleSingleMatching (data, oabaConfig, mSink);

				log.info("Time in single matching " + (System.currentTimeMillis() - t));

				batchJob.setDescription(mSink.getInfo());

			} else {
				log.warn("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.error(e.toString(),e);
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.error(e.toString(),e);
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	} // onMessage(Message)


	/** This method performs batch matching on only the staging source.
	 *
	 * @param data
	 */
	private void handleStageBatch (StartData data,
		OABAConfiguration oabaConfig,
		IMatchRecord2Sink mSinkFinal, BatchJob batchJob) throws Exception {

		IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);

		IStatus status = configuration.getStatusLog(data);

		RecordIDTranslator2 translator = new RecordIDTranslator2 (oabaConfig.getTransIDFactory());

		//OABA parameters
		String temp = (String) stageModel.properties().get("maxBlockSize");
		int maxBlock = Integer.parseInt(temp);

		temp = (String) stageModel.properties().get("maxOversized");
		int maxOversized = Integer.parseInt(temp);

		temp = (String) stageModel.properties().get("minFields");
		int minFields = Integer.parseInt(temp);

		temp = (String) stageModel.properties().get("maxChunkSize");
		int maxChunk = Integer.parseInt(temp);

		temp = (String) stageModel.properties().get("maxMatchSize");
		int maxMatch = Integer.parseInt(temp);


		//create rec_id, val_id files
		RecValService2 rvService = new RecValService2 (data.staging, null,
			stageModel, null,
			oabaConfig.getRecValFactory(), translator, status);
		rvService.runService();
		data.numBlockFields = rvService.getNumBlockingFields();

		ValidatorBase validator = new ValidatorBase (true, translator);
		data.validator = validator;

		//blocking
		//using BlockGroup to speed up dedup later
		BlockGroup bGroup = new BlockGroup (oabaConfig.getBlockGroupFactory(), maxBlock);

		IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
		IBlockSink osSpecial =  osFactory.getNextSink();

		//Start blocking
		OABABlockingService blockingService = new OABABlockingService (maxBlock, bGroup,
			oabaConfig.getOversizedGroupFactory(),
			osSpecial, null, oabaConfig.getRecValFactory(), data.numBlockFields,
			data.validator, status, batchJob, minFields, maxOversized);
		blockingService.runService();
		log.info ("Done blocking " + blockingService.getTimeElapsed());
		log.info ("Num Blocks " + blockingService.getNumBlocks());

		//start block dedup
		IBlockSink bSink = oabaConfig.getBlockFactory().getNextSink();
		BlockDedupService dedupService = new BlockDedupService (bGroup, bSink, maxBlock, status);
		dedupService.runService();
		log.info ("Done block dedup " + dedupService.getTimeElapsed());
		log.info ("Num Blocks Before " + dedupService.getNumBlocksIn());
		log.info ("Num Blocks After " + dedupService.getNumBlocksOut());

		//start oversized dedup
		IBlockSource osSource = osFactory.getSource(osSpecial);
		IBlockSink osDedup = osFactory.getNextSink();

		OversizedDedupService osDedupService =
			new OversizedDedupService (osSource, osDedup,
			oabaConfig.getOversizedTempFactory(),
			status, batchJob);
		osDedupService.runService();
		log.info( "Done oversized dedup " + osDedupService.getTimeElapsed());
		log.info ("Num OS Before " + osDedupService.getNumBlocksIn());
		log.info ("Num OS After Exact " + osDedupService.getNumAfterExact());
		log.info ("Num OS Done " + osDedupService.getNumBlocksOut());
		sendToUpdateStatus (data.jobID, 30);

		//create the proper block source
		IBlockSinkSourceFactory bFactory = oabaConfig.getBlockFactory();
		bSink = bFactory.getNextSink();
		IBlockSource source = bFactory.getSource(bSink);

		//create the proper oversized source
		IBlockSource source2 = osFactory.getSource(osDedup);

		//create chunks
		ChunkService2 chunkService = new ChunkService2 (source, source2, data.staging, null,
			stageModel, null, translator,
			oabaConfig.getChunkIDFactory(),
			oabaConfig.getStageDataFactory(), oabaConfig.getMasterDataFactory(),
			oabaConfig.getCGFactory(), maxChunk, status );

		chunkService.runService();
		log.info( "Number of chunks " + chunkService.getNumChunks());
		log.info( "Done creating chunks " + chunkService.getTimeElapsed());

		translator.cleanUp();

		data.numChunks = chunkService.getNumChunks();

		//match sink
		IMatchRecord2SinkSourceFactory mFactory = oabaConfig.getMatchTempFactory();
		IMatchRecord2Sink mSink = mFactory.getNextSink();

		//matcher is the code that does the matching.
		BlockMatcher2 matcher = new BlockMatcher2 ();

		MatchingService2 matchingService = new MatchingService2 (oabaConfig.getStageDataFactory(),
			oabaConfig.getMasterDataFactory(), oabaConfig.getCGFactory(), stageModel, null,
			mSink, matcher, data.low,
			data.high, maxBlock, status);
		matchingService.runService();
		log.info( "Done matching " + matchingService.getTimeElapsed());

		//dedup match file
		IMatchRecord2Source mSource = mFactory.getSource(mSink);

		MatchDedupService2 mDedupService = new MatchDedupService2 (mSource, mSinkFinal,
			mFactory, maxMatch, status);
		mDedupService.runService ();
		log.info( "Done match dedup " + mDedupService.getTimeElapsed());
	}



	/** This method takes one record at a time from the staging source and performs matching against
	 * the master source.  It's basically like findMatches.
	 *
	 * @param data
	 * @throws Exception
	 */
	private void handleSingleMatching (StartData data,
		OABAConfiguration oabaConfig, IMatchRecord2Sink mSinkFinal) throws Exception {

		IProbabilityModel model = PMManager.getModelInstance(data.masterModelName);
		if (model == null) {
			log.error("Invalid probability accessProvider: " + data.masterModelName);
			throw new BlockingException (data.masterModelName);
		}

		// BUG 2009-08-21 rphall
		// The following code can cause unnecessary recalculations of counts
		// that are persistent in a database. For example, if CM Server is
		// restarted, this will update counts that are already valid in the DB.
		// Recalculating counts can be quite slow for large databases; e.g.
		// 30 minutes for a 4.3M record database.
		//
		// It is really not the responsibility of this service to update counts.
		// That responsibility belongs to an administrative service.
		////init counts
		//new CountsUpdate().updateCounts(configuration.getDataSource(), true);
		// END BUG
		// BUG FIX 2009-08-21 rphall
		// Treat the flag isCountsUpdated as a check for whether
		// counts have been cached in memory. If they haven't been,
		// then cache them.
		// FIXME cache the counts only if they haven't been already cached.
		// (The unnecesary caching is not a show stopper however, since there
		// are other bottlenecks besides this redundant re-caching.)
		new CountsUpdate().cacheCounts(configuration.getDataSource());
		// END FIXME
		// END BUGFIX

		// 2014-04-24 rphall: Commented out unused local variable
		// Any side effects?
//		Accessor accessor = model.getAccessor();

		RecordDecisionMaker dm = new RecordDecisionMaker();
		DatabaseAccessor databaseAccessor;

		IExtension dbaExt = Platform.getPluginRegistry().getExtension(DATABASE_ACCESSOR, (String) model.properties().get(DATABASE_ACCESSOR));
		databaseAccessor = (DatabaseAccessor) dbaExt.getConfigurationElements()[0].createExecutableExtension("class");
		databaseAccessor.setCondition("");
		databaseAccessor.setDataSource(configuration.getDataSource());

		RecordSource stage = data.staging;
		stage.setModel(model);
		stage.open();

		mSinkFinal.append();

		MatchCandidateFactory matchCandidateFactory = (MatchCandidateFactory)
			Platform.getPluginRegistry()
			.getExtension(MATCH_CANDIDATE, "com.choicemaker.cm.core.base.beanMatchCandidate")
			.getConfigurationElements()[0]
			.createExecutableExtension("class");
		log.debug("MatchCandidateFactory class: " + matchCandidateFactory.getClass().getName());

		while (stage.hasNext()) {
			Record q = stage.getNext();

			SortedSet s;
			AutomatedBlocker rs = new Blocker2(databaseAccessor, model, q);

			log.debug (q.getId() + " " + rs + " " + model);

			s = dm.getMatches(q, rs, model, data.low, data.high);

			Iterator iS = s.iterator();
			while (iS.hasNext()) {
				Match match = (Match) iS.next();

				char type = 'M';
				if (match.probability < data.high) type = 'H';

				// 2009-08-17 rphall
				// BUG FIX? clue notes added here
				final String noteInfo = MatchRecord2.getNotesAsDelimitedString(match.ac,model);
				MatchRecord2 mr2 = new MatchRecord2 (q.getId(), match.id, 'D', match.probability,
					type,noteInfo);
				// END BUG FIX?

				//now write match candidates to file.
				mSinkFinal.writeMatch(mr2);
			}

		}

		stage.close ();
		mSinkFinal.close();

		//mark as done
		sendToUpdateStatus (data.jobID, 100);
	}


	private void writeToSink (MatchCandidate [] candidates, IMatchRecord2Sink sink) {
//		MatchRecord2 match = new MatchRecord2 ();
//		sink.writeMatch(match);
	}



	/** This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException {

		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		configuration.sendMessage(queue, data);
	}


	public void ejbRemove() {
//		log.debug("ejbRemove()");
	}


}
