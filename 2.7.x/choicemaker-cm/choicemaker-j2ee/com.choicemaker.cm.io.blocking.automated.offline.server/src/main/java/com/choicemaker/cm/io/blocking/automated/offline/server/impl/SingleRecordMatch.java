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

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidateFactory;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.io.blocking.automated.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchDedupService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchingService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.RecValService2;
import com.choicemaker.cm.server.util.CountsUpdate;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.platform.CMPlatformUtils;

// import com.choicemaker.cm.core.base.Accessor;

/**
 * This message bean performs single record matching on the staging record
 * source.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/singleMatchQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class SingleRecordMatch implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(SingleRecordMatch.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ SingleRecordMatch.class.getName());

	public static final String DATABASE_ACCESSOR =
		ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;
	public static final String MATCH_CANDIDATE =
		ChoiceMakerExtensionPoint.CM_CORE_MATCHCANDIDATE;

	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

	@Resource
	private MessageDrivenContext mdc;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	JMSContext jmsContext;

	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();
				final long jobId = data.jobID;
				BatchJob batchJob =
					configuration.findBatchJobById(em, BatchJobBean.class,
							jobId);
				BatchParameters params =
					configuration.findBatchParamsByJobId(em, jobId);
				OABAConfiguration oabaConfig =
					new OABAConfiguration(jobId);

				long t = System.currentTimeMillis();

				log.info("Starting Sinlge Record Match with maxSingle = "
						+ params.getMaxSingle());

				// final file
				IMatchRecord2Sink mSink =
					oabaConfig.getCompositeMatchSink(jobId);

				// run OABA on the staging data set.
				handleStageBatch(data, oabaConfig, mSink, batchJob, params);

				log.info("Time in dedup stage "
						+ (System.currentTimeMillis() - t));
				t = System.currentTimeMillis();

				// run single record match between stage and master.
				handleSingleMatching(data, oabaConfig, mSink, params);

				log.info("Time in single matching "
						+ (System.currentTimeMillis() - t));

				batchJob.setDescription(mSink.getInfo());

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (Exception e) {
			log.severe(e.toString());
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
		return;
	} // onMessage(Message)

	/**
	 * This method performs batch matching on only the staging source.
	 *
	 * @param data
	 */
	private void handleStageBatch(StartData data, OABAConfiguration oabaConfig,
			IMatchRecord2Sink mSinkFinal, BatchJob batchJob,
			BatchParameters params) throws Exception {

		final long jobId = data.jobID;
		final String modelConfigId = params.getModelConfigurationName();
		oabaConfig = new OABAConfiguration(jobId);
		IProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);

		EJBConfiguration configuration = EJBConfiguration.getInstance();
		OabaProcessing processingEntry =
			configuration.getProcessingLog(em, data);

		RecordIDTranslator2 translator =
			new RecordIDTranslator2(oabaConfig.getTransIDFactory());

		// OABA parameters
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

		// create rec_id, val_id files
		RecValService2 rvService =
			new RecValService2(params.getStageRs(), null, stageModel, null,
					oabaConfig.getRecValFactory(), translator, processingEntry);
		rvService.runService();
		data.numBlockFields = rvService.getNumBlockingFields();

		ValidatorBase validator = new ValidatorBase(true, translator);
		data.validator = validator;

		// blocking
		// using BlockGroup to speed up dedup later
		BlockGroup bGroup =
			new BlockGroup(oabaConfig.getBlockGroupFactory(), maxBlock);

		IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
		IBlockSink osSpecial = osFactory.getNextSink();

		// Start blocking
		OABABlockingService blockingService =
			new OABABlockingService(maxBlock, bGroup,
					oabaConfig.getOversizedGroupFactory(), osSpecial, null,
					oabaConfig.getRecValFactory(), data.numBlockFields,
					data.validator, processingEntry, batchJob, minFields, maxOversized);
		blockingService.runService();
		log.info("Done blocking " + blockingService.getTimeElapsed());
		log.info("Num Blocks " + blockingService.getNumBlocks());

		// start block dedup
		IBlockSink bSink = oabaConfig.getBlockFactory().getNextSink();
		BlockDedupService dedupService =
			new BlockDedupService(bGroup, bSink, maxBlock, processingEntry);
		dedupService.runService();
		log.info("Done block dedup " + dedupService.getTimeElapsed());
		log.info("Num Blocks Before " + dedupService.getNumBlocksIn());
		log.info("Num Blocks After " + dedupService.getNumBlocksOut());

		// start oversized dedup
		IBlockSource osSource = osFactory.getSource(osSpecial);
		IBlockSink osDedup = osFactory.getNextSink();

		OversizedDedupService osDedupService =
			new OversizedDedupService(osSource, osDedup,
					oabaConfig.getOversizedTempFactory(), processingEntry, batchJob);
		osDedupService.runService();
		log.info("Done oversized dedup " + osDedupService.getTimeElapsed());
		log.info("Num OS Before " + osDedupService.getNumBlocksIn());
		log.info("Num OS After Exact " + osDedupService.getNumAfterExact());
		log.info("Num OS Done " + osDedupService.getNumBlocksOut());
		sendToUpdateStatus(data.jobID, 30);

		// create the proper block source
		IBlockSinkSourceFactory bFactory = oabaConfig.getBlockFactory();
		bSink = bFactory.getNextSink();
		IBlockSource source = bFactory.getSource(bSink);

		// create the proper oversized source
		IBlockSource source2 = osFactory.getSource(osDedup);

		// create chunks
		ChunkService2 chunkService =
			new ChunkService2(source, source2, params.getStageRs(), null, stageModel,
					null, translator, oabaConfig.getChunkIDFactory(),
					oabaConfig.getStageDataFactory(),
					oabaConfig.getMasterDataFactory(),
					oabaConfig.getCGFactory(), maxChunk, processingEntry);

		chunkService.runService();
		log.info("Number of chunks " + chunkService.getNumChunks());
		log.info("Done creating chunks " + chunkService.getTimeElapsed());

		translator.cleanUp();

		data.numChunks = chunkService.getNumChunks();

		// match sink
		IMatchRecord2SinkSourceFactory mFactory =
			oabaConfig.getMatchTempFactory();
		IMatchRecord2Sink mSink = mFactory.getNextSink();

		// matcher is the code that does the matching.
		BlockMatcher2 matcher = new BlockMatcher2();

		MatchingService2 matchingService =
			new MatchingService2(oabaConfig.getStageDataFactory(),
					oabaConfig.getMasterDataFactory(),
					oabaConfig.getCGFactory(), stageModel, null, mSink,
					matcher, params.getLowThreshold(),
					params.getHighThreshold(), maxBlock, processingEntry);
		matchingService.runService();
		log.info("Done matching " + matchingService.getTimeElapsed());

		// dedup match file
		IMatchRecord2Source mSource = mFactory.getSource(mSink);

		MatchDedupService2 mDedupService =
			new MatchDedupService2(mSource, mSinkFinal, mFactory, maxMatch,
					processingEntry);
		mDedupService.runService();
		log.info("Done match dedup " + mDedupService.getTimeElapsed());
	}

	/**
	 * This method takes one record at a time from the staging source and
	 * performs matching against the master source. It's basically like
	 * findMatches.
	 *
	 * @param data
	 * @throws Exception
	 */
	private void handleSingleMatching(StartData data,
			OABAConfiguration oabaConfig, IMatchRecord2Sink mSinkFinal,
			BatchParameters params) throws Exception {

		final String modelConfigId = params.getModelConfigurationName();
		IProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String msg = "Invalid probability accessProvider: " + modelConfigId;
			log.severe(msg);
			throw new BlockingException(msg);
		}

		EJBConfiguration configuration = EJBConfiguration.getInstance();
		new CountsUpdate().cacheCounts(configuration.getDataSource());

		RecordDecisionMaker dm = new RecordDecisionMaker();
		CMExtension dbaExt =
			CMPlatformUtils.getExtension(DATABASE_ACCESSOR, (String) model
					.properties().get(DATABASE_ACCESSOR));
		DatabaseAccessor databaseAccessor =
			(DatabaseAccessor) dbaExt.getConfigurationElements()[0]
					.createExecutableExtension("class");
		databaseAccessor.setCondition("");
		databaseAccessor.setDataSource(configuration.getDataSource());

		RecordSource stage = params.getStageRs();
		assert stage.getModel() == model;
//		stage.setModel(model);

		try {
			stage.open();
			mSinkFinal.append();
			MatchCandidateFactory matchCandidateFactory =
				(MatchCandidateFactory) CMPlatformUtils.getExtension(
						MATCH_CANDIDATE,
						"com.choicemaker.cm.core.beanMatchCandidate")
						.getConfigurationElements()[0]
						.createExecutableExtension("class");
			log.fine("MatchCandidateFactory class: "
					+ matchCandidateFactory.getClass().getName());

			while (stage.hasNext()) {
				Record q = stage.getNext();
				AutomatedBlocker rs = new Blocker2(databaseAccessor, model, q);
				log.fine(q.getId() + " " + rs + " " + model);

				SortedSet<Match> s =
					dm.getMatches(q, rs, model, params.getLowThreshold(), params.getHighThreshold());
				Iterator<Match> iS = s.iterator();
				while (iS.hasNext()) {
					Match match = iS.next();
					char type = 'M';
					if (match.probability < params.getHighThreshold())
						type = 'H';
					final String noteInfo =
						MatchRecord2.getNotesAsDelimitedString(match.ac, model);
					MatchRecord2 mr2 =
						new MatchRecord2(q.getId(), match.id, 'D',
								match.probability, type, noteInfo);

					// write match candidate to file.
					mSinkFinal.writeMatch(mr2);
				}

			}

		} finally {
			stage.close();
			mSinkFinal.close();
		}

		// mark as done
		sendToUpdateStatus(data.jobID, 100);
	}

	private void sendToUpdateStatus(long jobID, int percentComplete)
			throws NamingException {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

}
