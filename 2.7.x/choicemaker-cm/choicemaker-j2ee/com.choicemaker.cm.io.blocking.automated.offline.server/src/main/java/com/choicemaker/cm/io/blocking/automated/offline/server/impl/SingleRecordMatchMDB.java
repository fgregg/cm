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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_BLOCKING_FIELD_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_OABA_CACHED_RESULTS_FILE;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
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
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecordUtils;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
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
@SuppressWarnings({
		"rawtypes", "unchecked" })
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/singleMatchQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class SingleRecordMatchMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(SingleRecordMatchMDB.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ SingleRecordMatchMDB.class.getName());

	public static final String DATABASE_ACCESSOR =
		ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;

	public static final String MATCH_CANDIDATE =
		ChoiceMakerExtensionPoint.CM_CORE_MATCHCANDIDATE;

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private OabaProcessingController processingController;

	@EJB
	private RecordSourceController rsController;

	@EJB
	private RecordIdController ridController;

	@EJB
	private OperationalPropertyController propController;

	// @Inject
	// private JMSContext jmsContext;

	@Override
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage data;
		OabaJob oabaJob = null;

		try {

			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();
				final long jobId = data.jobID;
				oabaJob = jobController.findOabaJob(jobId);
				OabaParameters params =
					paramsController.findOabaParametersByJobId(jobId);
				OabaSettings settings =
					oabaSettingsController.findOabaSettingsByJobId(jobId);

				log.info("Starting Sinlge Record Match with maxSingle = "
						+ settings.getMaxSingle());

				// final file
				IMatchRecord2Sink mSink =
					OabaFileUtils.getCompositeMatchSink(oabaJob);

				// run OABA on the staging data set.
				long t = System.currentTimeMillis();
				handleStageBatch(data, mSink, oabaJob, params);
				log.info("Time in dedup stage "
						+ (System.currentTimeMillis() - t));

				// run single record match between stage and master.
				t = System.currentTimeMillis();
				handleSingleMatching(data, mSink, oabaJob, params);
				log.info("Time in single matching "
						+ (System.currentTimeMillis() - t));

				String cachedFileName = mSink.getInfo();
				log.info("Cached results file: " + cachedFileName);
				propController.setJobProperty(oabaJob,
						PN_OABA_CACHED_RESULTS_FILE, cachedFileName);

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	} // onMessage(Message)

	/**
	 * This method performs batch matching on only the staging source.
	 *
	 * @param data
	 */
	private void handleStageBatch(OabaJobMessage data,
			IMatchRecord2Sink mSinkFinal, OabaJob oabaJob, OabaParameters params)
			throws Exception {

		// final long jobId = data.jobID;
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);

		OabaEventLog processingEntry =
			processingController.getProcessingLog(oabaJob);

		final MutableRecordIdTranslator mutableTranslator =
			ridController.createMutableRecordIdTranslator(oabaJob);

		// FIXME
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
		ISerializableRecordSource staging = rsController.getStageRs(params);
		RecValService2 rvService =
			new RecValService2(staging, null, stageModel, null,
					OabaFileUtils.getRecValFactory(oabaJob), mutableTranslator,
					processingEntry);
		rvService.runService();
		final int numBlockFields = rvService.getNumBlockingFields();
		propController.setJobProperty(oabaJob, PN_BLOCKING_FIELD_COUNT,
				String.valueOf(numBlockFields));

		final ImmutableRecordIdTranslator immutableTranslator =
			ridController.toImmutableTranslator(mutableTranslator);
		ValidatorBase validator = new ValidatorBase(true, immutableTranslator);
		data.validator = validator;

		// blocking
		// using BlockGroup to speed up dedup later
		BlockGroup bGroup =
			new BlockGroup(OabaFileUtils.getBlockGroupFactory(oabaJob),
					maxBlock);
		IBlockSinkSourceFactory osFactory =
			OabaFileUtils.getOversizedFactory(oabaJob);
		IBlockSink osSpecial = osFactory.getNextSink();

		// Start blocking
		OABABlockingService blockingService =
			new OABABlockingService(maxBlock, bGroup,
					OabaFileUtils.getOversizedGroupFactory(oabaJob), osSpecial,
					null, OabaFileUtils.getRecValFactory(oabaJob),
					numBlockFields, data.validator, processingEntry, oabaJob,
					minFields, maxOversized);
		blockingService.runService();
		log.info("Done blocking " + blockingService.getTimeElapsed());
		log.info("Num Blocks " + blockingService.getNumBlocks());

		// start block dedup
		IBlockSink bSink = OabaFileUtils.getBlockFactory(oabaJob).getNextSink();
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
					OabaFileUtils.getOversizedTempFactory(oabaJob),
					processingEntry, oabaJob);
		osDedupService.runService();
		log.info("Done oversized dedup " + osDedupService.getTimeElapsed());
		log.info("Num OS Before " + osDedupService.getNumBlocksIn());
		log.info("Num OS After Exact " + osDedupService.getNumAfterExact());
		log.info("Num OS Done " + osDedupService.getNumBlocksOut());
		sendToUpdateStatus(oabaJob, OabaEvent.DONE_DEDUP_OVERSIZED, new Date(),
				null);

		// create the proper block source
		IBlockSinkSourceFactory bFactory =
			OabaFileUtils.getBlockFactory(oabaJob);
		bSink = bFactory.getNextSink();
		IBlockSource source = bFactory.getSource(bSink);

		// create the proper oversized source
		IBlockSource source2 = osFactory.getSource(osDedup);

		// create chunks
		ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		ISerializableRecordSource stagingRs = rsController.getStageRs(params);
		ChunkService2 chunkService =
			new ChunkService2(source, source2, stagingRs, null, stageModel,
					null, immutableTranslator,
					OabaFileUtils.getChunkIDFactory(oabaJob),
					OabaFileUtils.getStageDataFactory(oabaJob, model),
					OabaFileUtils.getMasterDataFactory(oabaJob, model),
					OabaFileUtils.getCGFactory(oabaJob), maxChunk,
					processingEntry);
		chunkService.runService();
		log.info("Done creating chunks " + chunkService.getTimeElapsed());

		mutableTranslator.cleanUp();

		final int numChunks = chunkService.getNumChunks();
		log.info("Number of chunks " + numChunks);
		propController.setJobProperty(oabaJob, PN_CHUNK_FILE_COUNT,
				String.valueOf(numChunks));

		// match sink
		IMatchRecord2SinkSourceFactory mFactory =
			OabaFileUtils.getMatchTempFactory(oabaJob);
		IMatchRecord2Sink mSink = mFactory.getNextSink();

		// matcher is the code that does the matching.
		BlockMatcher2 matcher = new BlockMatcher2();

		MatchingService2 matchingService =
			new MatchingService2(OabaFileUtils.getStageDataFactory(oabaJob,
					model), OabaFileUtils.getMasterDataFactory(oabaJob, model),
					OabaFileUtils.getCGFactory(oabaJob), stageModel, null,
					mSink, matcher, params.getLowThreshold(),
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
	private void handleSingleMatching(OabaJobMessage data,
			IMatchRecord2Sink mSinkFinal, OabaJob oabaJob, OabaParameters params)
			throws Exception {

		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
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

		RecordSource stage = rsController.getStageRs(params);
		assert stage.getModel() == model;
		// stage.setModel(modelId);

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
					dm.getMatches(q, rs, model, params.getLowThreshold(),
							params.getHighThreshold());
				Iterator<Match> iS = s.iterator();
				while (iS.hasNext()) {
					Match m = iS.next();
					// // BUG 2015-01-10 rphall
					// // FIXME Doesn't handle rules correctly
					// char type = 'M';
					// if (match.probability < params.getHighThreshold())
					// type = 'H';
					// // END BUG
					final String noteInfo =
						MatchRecordUtils.getNotesAsDelimitedString(m.ac, model);
					MatchRecord2 mr2 =
						new MatchRecord2(q.getId(), m.id,
								RECORD_SOURCE_ROLE.MASTER, m.probability,
								m.decision, noteInfo);

					// write match candidate to file.
					mSinkFinal.writeMatch(mr2);
				}

			}

		} finally {
			stage.close();
			mSinkFinal.close();
		}

		// mark as done
		sendToUpdateStatus(oabaJob, OabaEvent.DONE_OABA, new Date(), null);
	}

	private void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		processingController.updateStatusWithNotification(job, event,
				timestamp, info);
	}

}
