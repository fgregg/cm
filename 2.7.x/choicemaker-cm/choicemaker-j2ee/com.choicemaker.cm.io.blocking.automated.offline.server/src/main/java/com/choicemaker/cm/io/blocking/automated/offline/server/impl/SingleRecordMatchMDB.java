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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_BLOCKING_FIELD_COUNT;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_OABA_CACHED_RESULTS_FILE;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.sql.DataSource;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ChoiceMakerExtensionPoint;
import com.choicemaker.cm.core.DatabaseException;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.Record;
import com.choicemaker.cm.core.RecordSource;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.Match;
import com.choicemaker.cm.core.base.MatchCandidateFactory;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.core.base.RecordDecisionMaker;
import com.choicemaker.cm.io.blocking.automated.AbaStatistics;
import com.choicemaker.cm.io.blocking.automated.AutomatedBlocker;
import com.choicemaker.cm.io.blocking.automated.DatabaseAccessor;
import com.choicemaker.cm.io.blocking.automated.base.Blocker2;
import com.choicemaker.cm.io.blocking.automated.base.db.DbbCountsCreator;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.MutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_SOURCE_ROLE;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecordUtils;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockMatcher2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ValidatorBase;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.services.BlockDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchDedupService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.MatchingService2;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;
import com.choicemaker.cm.io.blocking.automated.offline.services.OversizedDedupService;
import com.choicemaker.cm.io.blocking.automated.offline.services.RecValService2;
import com.choicemaker.cm.io.db.base.DatabaseAbstraction;
import com.choicemaker.cm.io.db.base.DatabaseAbstractionManager;
import com.choicemaker.e2.CMConfigurationElement;
import com.choicemaker.e2.CMExtension;
import com.choicemaker.e2.E2Exception;
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
public class SingleRecordMatchMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger
			.getLogger(SingleRecordMatchMDB.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ SingleRecordMatchMDB.class.getName());

	public static final String DATABASE_ACCESSOR =
		ChoiceMakerExtensionPoint.CM_IO_BLOCKING_AUTOMATED_BASE_DATABASEACCESSOR;

	public static final String MATCH_CANDIDATE =
		ChoiceMakerExtensionPoint.CM_CORE_MATCHCANDIDATE;

	@Override
	protected void processOabaMessage(OabaJobMessage data, BatchJob batchJob,
			OabaParameters params, OabaSettings settings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		log.info("Starting Single Record Match with maxSingle = "
				+ settings.getMaxSingle());

		// final file
		IMatchRecord2Sink mSink = OabaFileUtils.getCompositeMatchSink(batchJob);

		// run OABA on the staging data set.
		long t = System.currentTimeMillis();
		handleStageBatch(mSink, batchJob, params, settings, processingLog,
				serverConfig, model);
		log.info("Msecs in dedup stage " + (System.currentTimeMillis() - t));

		// run single record match between stage and master.
		t = System.currentTimeMillis();
		handleSingleMatching(data, mSink, batchJob, params, settings);
		log.info("Msecs in single matching " + (System.currentTimeMillis() - t));

		String cachedFileName = mSink.getInfo();
		log.info("Cached results file: " + cachedFileName);
		getPropertyController().setJobProperty(batchJob,
				PN_OABA_CACHED_RESULTS_FILE, cachedFileName);

	}

	/**
	 * This method performs batch matching on only the staging source.
	 *
	 * @param data
	 */
	private void handleStageBatch(IMatchRecord2Sink mSinkFinal,
			BatchJob batchJob, OabaParameters params, OabaSettings settings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		// final long jobId = data.jobID;
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);

		ProcessingEventLog processingEntry =
			getProcessingController().getProcessingLog(batchJob);

		final MutableRecordIdTranslator mutableTranslator =
			getRecordIdController().createMutableRecordIdTranslator(batchJob);

		final int maxBlock = settings.getMaxBlockSize();
		final int maxOversized = settings.getMaxOversized();
		final int minFields = settings.getMinFields();
		final int maxChunk = settings.getMaxChunkSize();
		final int maxMatch = settings.getMaxMatches();

		// create rec_id, val_id files
		ISerializableRecordSource staging = null;
		try {
			staging = getRecordSourceController().getStageRs(params);
		} catch (Exception e1) {
			String msg = "Unable to get staging record source: " + e1;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		RecValService2 rvService =
			new RecValService2(staging, null, stageModel, null,
					OabaFileUtils.getRecValFactory(batchJob),
					mutableTranslator, processingEntry);
		rvService.runService();
		final int numBlockFields = rvService.getNumBlockingFields();
		getPropertyController().setJobProperty(batchJob,
				PN_BLOCKING_FIELD_COUNT, String.valueOf(numBlockFields));

		final ImmutableRecordIdTranslator immutableTranslator =
			getRecordIdController().toImmutableTranslator(mutableTranslator);
		ValidatorBase validator = new ValidatorBase(true, immutableTranslator);
		// data.validator = validator;

		// blocking
		// using BlockGroup to speed up dedup later
		BlockGroup bGroup =
			new BlockGroup(OabaFileUtils.getBlockGroupFactory(batchJob),
					maxBlock);
		IBlockSinkSourceFactory osFactory =
			OabaFileUtils.getOversizedFactory(batchJob);
		IBlockSink osSpecial = osFactory.getNextSink();

		// Start blocking
		OABABlockingService blockingService;
		try {
			blockingService =
				new OABABlockingService(maxBlock, bGroup,
						OabaFileUtils.getOversizedGroupFactory(batchJob),
						osSpecial, null,
						OabaFileUtils.getRecValFactory(batchJob),
						numBlockFields, validator, processingEntry, batchJob,
						minFields, maxOversized);
		} catch (IOException e) {
			String msg = "Unable to create blocking service: " + e;
			log.severe(msg);
			throw new BlockingException(msg, e);
		}
		blockingService.runService();
		log.info("Done blocking " + blockingService.getTimeElapsed());
		log.info("Num Blocks " + blockingService.getNumBlocks());

		// start block dedup
		IBlockSink bSink =
			OabaFileUtils.getBlockFactory(batchJob).getNextSink();
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
					OabaFileUtils.getOversizedTempFactory(batchJob),
					processingEntry, batchJob);
		osDedupService.runService();
		log.info("Done oversized dedup " + osDedupService.getTimeElapsed());
		log.info("Num OS Before " + osDedupService.getNumBlocksIn());
		log.info("Num OS After Exact " + osDedupService.getNumAfterExact());
		log.info("Num OS Done " + osDedupService.getNumBlocksOut());
		sendToUpdateStatus(batchJob, OabaProcessingEvent.DONE_DEDUP_OVERSIZED,
				new Date(), null);

		// create the proper block source
		IBlockSinkSourceFactory bFactory =
			OabaFileUtils.getBlockFactory(batchJob);
		bSink = bFactory.getNextSink();
		IBlockSource source = bFactory.getSource(bSink);

		// create the proper oversized source
		IBlockSource source2 = osFactory.getSource(osDedup);

		// Get a source of records from the staging database.
		// These record will be blocked against themselves.
		ISerializableRecordSource stagingRs = null;
		try {
			stagingRs = getRecordSourceController().getStageRs(params);
		} catch (Exception e) {
			String msg = "Unable to get staging record source: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		assert stagingRs != null;

		// Create chunks of staging records
		ChunkService2 chunkService =
			new ChunkService2(source, source2, stagingRs, null, stageModel,
					null, immutableTranslator,
					OabaFileUtils.getChunkIDFactory(batchJob),
					OabaFileUtils.getStageDataFactory(batchJob, model),
					OabaFileUtils.getMasterDataFactory(batchJob, model),
					OabaFileUtils.getCGFactory(batchJob), maxChunk,
					processingEntry);
		try {
			chunkService.runService();
		} catch (XmlConfException e) {
			String msg = "Unable to create chunks from staging records: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		log.info("Done creating chunks from staging records: "
				+ chunkService.getTimeElapsed());

		mutableTranslator.cleanUp();

		final int numChunks = chunkService.getNumChunks();
		log.info("Number of chunks " + numChunks);
		getPropertyController().setJobProperty(batchJob, PN_CHUNK_FILE_COUNT,
				String.valueOf(numChunks));

		// Match the staging records against themselves
		BlockMatcher2 matcher = new BlockMatcher2();
		IMatchRecord2SinkSourceFactory mFactory =
			OabaFileUtils.getMatchTempFactory(batchJob);
		IMatchRecord2Sink mSink = mFactory.getNextSink();
		MatchingService2 matchingService =
			new MatchingService2(OabaFileUtils.getStageDataFactory(batchJob,
					model),
					OabaFileUtils.getMasterDataFactory(batchJob, model),
					OabaFileUtils.getCGFactory(batchJob), stageModel, null,
					mSink, matcher, params.getLowThreshold(),
					params.getHighThreshold(), maxBlock, processingEntry);
		try {
			matchingService.runService();
		} catch (XmlConfException e) {
			String msg = "Unable to run matching service: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		log.info("Msecs in matching " + matchingService.getTimeElapsed());

		// Deduplicate the staging match file
		IMatchRecord2Source mSource = mFactory.getSource(mSink);

		MatchDedupService2 mDedupService =
			new MatchDedupService2(mSource, mSinkFinal, mFactory, maxMatch,
					processingEntry);
		mDedupService.runService();
		log.info("Msecs in staging match dedup: "
				+ mDedupService.getTimeElapsed());
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
			IMatchRecord2Sink mSinkFinal, BatchJob batchJob,
			OabaParameters params, OabaSettings settings)
			throws BlockingException {

		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String msg = "Invalid probability accessProvider: " + modelConfigId;
			log.severe(msg);
			throw new BlockingException(msg);
		}

		// Get the data sources for ABA queries
		DataSource stageDS = null;
		try {
			stageDS = getSqlRecordSourceController().getStageDataSource(params);
		} catch (BlockingException e) {
			String msg = "Unable to acquire data source: " + e;
			log.severe(msg);
			throw e;
		}
		assert stageDS != null;

		DataSource masterDS = null;
		try {
			masterDS =
				getSqlRecordSourceController().getMasterDataSource(params);
		} catch (BlockingException e) {
			String msg = "Unable to acquire data source: " + e;
			log.severe(msg);
			throw e;
		}
		// assert masterDS != null;

		// Staging is used for ABA statistics only if the master source
		// isn't being used (i.e. it is null)
		if (masterDS != null) {
			// Cache ABA statistics for field-value counts from master
			log.info("Caching ABA statistic for master records..");
			try {
				DatabaseAbstractionManager mgr =
					new AggregateDatabaseAbstractionManager();
				DatabaseAbstraction dba =
					mgr.lookupDatabaseAbstraction(masterDS);
				DbbCountsCreator cc = new DbbCountsCreator();
				cc.setCacheCountSources(masterDS, dba,
						getAbaStatisticsController());
			} catch (SQLException | DatabaseException e) {
				String msg = "Unable to cache master ABA statistics: " + e;
				log.severe(msg);
				throw new BlockingException(msg);
			}
			log.info("... finished caching ABA statistics for master records.");

		} else {
			// Cache ABA statistics for field-value counts from staging
			log.info("Caching ABA statistic for staging records..");
			try {
				DatabaseAbstractionManager mgr =
					new AggregateDatabaseAbstractionManager();
				DatabaseAbstraction dba =
					mgr.lookupDatabaseAbstraction(stageDS);
				DbbCountsCreator cc = new DbbCountsCreator();
				cc.setCacheCountSources(stageDS, dba,
						getAbaStatisticsController());
			} catch (SQLException | DatabaseException e) {
				String msg = "Unable to cache staging ABA statistics: " + e;
				log.severe(msg);
				throw new BlockingException(msg);
			}
			log.info("... finished caching ABA statistics for staging records.");
		}

		String dbaName = model.getDatabaseAccessorName();
		CMExtension dbaExt =
			CMPlatformUtils.getExtension(DATABASE_ACCESSOR, dbaName);
		DatabaseAccessor databaseAccessor = null;
		try {
			databaseAccessor =
				(DatabaseAccessor) dbaExt.getConfigurationElements()[0]
						.createExecutableExtension("class");
		} catch (E2Exception e) {
			String msg = "Unable to construct database accessor: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		assert databaseAccessor != null;
		databaseAccessor.setCondition("");
		if (masterDS != null) {
			databaseAccessor.setDataSource(masterDS);
		} else {
			databaseAccessor.setDataSource(stageDS);
		}

		RecordSource stage = null;
		try {
			stage = getRecordSourceController().getStageRs(params);
		} catch (Exception e) {
			String msg = "Unable to get staging record source: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}
		assert stage.getModel() == model;
		// stage.setModel(modelId);

		MatchCandidateFactory matchCandidateFactory = null;
		try {
			CMExtension ext =
				CMPlatformUtils.getExtension(MATCH_CANDIDATE,
						"com.choicemaker.cm.core.beanMatchCandidate");
			CMConfigurationElement[] configs = ext.getConfigurationElements();
			CMConfigurationElement config = configs[0];
			matchCandidateFactory =
				(MatchCandidateFactory) config
						.createExecutableExtension("class");
			// matchCandidateFactory = (MatchCandidateFactory)
			// CMPlatformUtils.getExtension(
			// MATCH_CANDIDATE,
			// "com.choicemaker.cm.core.beanMatchCandidate")
			// .getConfigurationElements()[0]
			// .createExecutableExtension("class");
		} catch (E2Exception e) {
			String msg = "Unable to create Match Candidate factory: " + e;
			log.severe(msg);
			throw new BlockingException(msg);
		}

		RecordDecisionMaker dm = new RecordDecisionMaker();
		try {
			log.info("Finding matches of master records to staging records...");
			stage.open();
			mSinkFinal.append();
			log.fine("MatchCandidateFactory class: "
					+ matchCandidateFactory.getClass().getName());

			while (stage.hasNext()) {
				Record q = stage.getNext();
				AbaStatistics stats =
					getAbaStatisticsController().getStatistics(model);
				AutomatedBlocker rs =
					new Blocker2(databaseAccessor, model, q, settings, stats);
				log.fine(q.getId() + " " + rs + " " + model);

				SortedSet<Match> s =
					dm.getMatches(q, rs, model, params.getLowThreshold(),
							params.getHighThreshold());
				Iterator<Match> iS = s.iterator();
				while (iS.hasNext()) {
					Match m = iS.next();
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
			log.info("...finished finding matches of master records to staging records...");

		} catch (IOException x) {
			String msg = "Unable to read staging records from source: " + x;
			log.severe(msg);
			throw new BlockingException(msg);
		} finally {
			if (stage != null) {
				try {
					stage.close();
				} catch (Exception e) {
					String msg = "Unable to close staging record source: " + e;
					log.severe(msg);
				}
			}
			if (mSinkFinal != null) {
				mSinkFinal.close();
			}
		}

		// mark as done
		sendToUpdateStatus(batchJob, BatchProcessingEvent.DONE, new Date(),
				null);
	}

	private void sendToUpdateStatus(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJmsTrace() {
		return jmsTrace;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		// No further processing, so no notification
	}

	@Override
	protected BatchProcessingEvent getCompletionEvent() {
		return BatchProcessingEvent.DONE;
	}

}
