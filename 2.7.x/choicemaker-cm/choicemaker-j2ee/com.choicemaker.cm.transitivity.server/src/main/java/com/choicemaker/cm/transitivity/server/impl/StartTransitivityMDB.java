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
package com.choicemaker.cm.transitivity.server.impl;

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.transitivity.core.TransitivityProcessingEvent.DONE_CREATE_CHUNK_DATA;
import static com.choicemaker.cm.transitivity.core.TransitivityProcessingEvent.DONE_TRANS_DEDUP_OVERSIZED;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIdSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.ImmutableRecordIdTranslator;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.result.MatchToBlockTransformer2;
import com.choicemaker.cm.io.blocking.automated.offline.result.Size2MatchProducer;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.transitivity.core.TransitivityProcessingEvent;

/**
 * This message bean starts the Transitivity Engine. It assumes that it can
 * access the translator file.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({ "rawtypes" })
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/transitivityQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class StartTransitivityMDB extends AbstractTransitivityMDB {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger
			.getLogger(StartTransitivityMDB.class.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ StartTransitivityMDB.class.getName());

	// @Resource(lookup = "java:/choicemaker/urm/jms/updateTransQueue")
	// private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchSchedulerQueue")
	private Queue transMatchSchedulerQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, BatchJob batchJob,
			TransitivityParameters params, OabaSettings oabaSettings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {
		// this.updateTransivitityProcessingStatus(batchJob, event, timestamp,
		// info)
		// }
		//
		// public void _onMessage(Message inMessage) {
		// jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		// ObjectMessage msg = null;
		// BatchJob transJob = null;

		// log.fine("StartTransitivityMDB In onMessage");

		// try {
		// if (inMessage instanceof ObjectMessage) {
		// msg = (ObjectMessage) inMessage;
		// Object o = msg.getObject();
		//
		// OabaJobMessage d = (OabaJobMessage) o;
		// final long jobId = data.jobID;
		// transJob = null ; // jobController.findTransitivityJob(jobId);
		// transJob.markAsStarted();
		batchJob.markAsStarted();

		removeOldFiles(batchJob);
		createChunks(batchJob, params, oabaSettings, serverConfig);

		// } else {
		// log.warning("wrong type: " + inMessage.getClass().getName());
		// }

		// } catch (Exception e) {
		// log.severe(e.toString());
		// if (transJob != null) {
		// transJob.markAsFailed();
		// }
		// }
		// jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/*
	 * This method calls MatchToBlockTransformer to create blocks for the
	 * equivalence classes. It then calls ChunkService3 to create chunks.
	 */
	private void createChunks(BatchJob transJob, TransitivityParameters params,
			OabaSettings oabaSettings, ServerConfiguration serverConfig)
			throws BlockingException {
	
		// Get the parent/predecessor OABA job
		long oabaJobId = transJob.getBatchParentId();
		BatchJob oabaJob = this.getOabaJobController().findOabaJob(oabaJobId);

		// Get the match record source from the OABA job
		IMatchRecord2Source mSource =
			OabaFileUtils.getCompositeMatchSource(oabaJob);

		// Recover the translator from the OABA job
		ImmutableRecordIdTranslator translator =
			this.getRecordIdController().findRecordIdTranslator(oabaJob);

		// Create a block sink for the Transitivity job
		IBlockSink bSink =
			TransitivityFileUtils.getTransitivityBlockFactory(transJob)
					.getNextSink();

		// Create blocks for the Transitivity job
		IMatchRecord2SinkSourceFactory mFactory =
			OabaFileUtils.getMatchTempFactory(transJob);
		IRecordIdSinkSourceFactory idFactory =
			this.getRecordIdController().getRecordIdSinkSourceFactory(transJob);
		IRecordIdSink idSink = idFactory.getNextSink();
		MatchToBlockTransformer2 transformer =
			new MatchToBlockTransformer2(mSource, mFactory, translator, bSink,
					idSink);
		int numRecords = transformer.process();
		log.fine("Number of records: " + numRecords);

		// build a MatchRecord2Sink for all pairs belonging to the size 2 sets.
		Size2MatchProducer producer =
			new Size2MatchProducer(mSource, idFactory.getSource(idSink),
					OabaFileUtils.getSet2MatchFactory(transJob).getNextSink());
		int twos = producer.process();
		log.info("number of size 2 EC: " + twos);

		// Clean up the Transitivity job
		idSink.remove();

		IBlockSource bSource =
			TransitivityFileUtils.getTransitivityBlockFactory(transJob)
					.getSource(bSink);
		IDSetSource source2 = new IDSetSource(bSource);

		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);

		int maxChunk = oabaSettings.getMaxChunkSize();
		if (transformer.getMaxEC() > maxChunk)
			throw new RuntimeException("There is an equivalence class of size "
					+ transformer.getMaxEC()
					+ ", which is bigger than the max chunk size of "
					+ maxChunk + ".");

		// get the number of processors
		int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// get the number of chunk files
		int numFiles = serverConfig.getMaxOabaChunkFileCount();

		// create the oversized block transformer
		Transformer transformerO =
			new Transformer(translator,
					OabaFileUtils.getComparisonArrayGroupFactoryOS(transJob,
							numProcessors));

		// Cast the current transitivity parameters as OABA parameters.
		OabaParameters oabaParams = params.asOabaParameters();

		// Set the source for the staging records
		ISerializableRecordSource staging = null;
		try {
			staging = this.getRecordSourceController().getStageRs(oabaParams);
		} catch (Exception e) {
			String msg = "Unable to staging record source: " + e.toString();
			getLogger().severe(msg);
			throw new BlockingException(msg, e);
		}

		// Set the source for the master records
		ISerializableRecordSource master = null;
		try {
			master = this.getRecordSourceController().getMasterRs(oabaParams);
		} catch (Exception e) {
			String msg = "Unable to master record source: " + e.toString();
			getLogger().severe(msg);
			throw new BlockingException(msg, e);
		}

		// Set the correct processing status prior to chunk creation.
		// (This is a potential, but unlikely, race condition. A cleaner
		// approach would be to use a local stack implementation of
		// ProcessingEventLog, and then set the persistent value after the
		// chunk service completes.)
		ProcessingEventLog status =
			this.getProcessingController().getProcessingLog(transJob);
		status.setCurrentProcessingEvent(DONE_TRANS_DEDUP_OVERSIZED);

		ChunkService3 chunkService =
			new ChunkService3(source2, null, staging, master, model,
					OabaFileUtils.getChunkIDFactory(transJob),
					OabaFileUtils.getStageDataFactory(transJob, model),
					OabaFileUtils.getMasterDataFactory(transJob, model),
					translator.getSplitIndex(), transformerO, null, maxChunk,
					numFiles, status, transJob);
		chunkService.runService();
		log.info("Done creating chunks " + chunkService.getTimeElapsed());

		final int numChunks = chunkService.getNumChunks();
		log.info("Number of chunks " + numChunks);
		this.getPropertyController().setJobProperty(transJob,
				PN_CHUNK_FILE_COUNT, String.valueOf(numChunks));

		// this is important because in transitivity, there are only OS chunks.
		final int numRegularChunks = 0;
		log.info("Number of regular chunks " + numChunks);
		this.getPropertyController().setJobProperty(transJob,
				PN_REGULAR_CHUNK_FILE_COUNT, String.valueOf(numRegularChunks));

		// clean up translator
		// translator.cleanUp();

		// log.info("send to transitivity matcher");
		// OabaJobMessage data = new OabaJobMessage(jobId);
		// sendToTransMatch(data);
		// sendToUpdateTransStatus(data.jobID, PCT_DONE_DEDUP_OVERSIZED);
	}

	/**
	 * This method removes any pre-existing transMatch* files
	 * 
	 * @param jobID
	 */
	private void removeOldFiles(BatchJob transJob) throws BlockingException {
		try {
			// final sink
			IMatchRecord2Source finalSource =
				TransitivityFileUtils.getCompositeTransMatchSource(transJob);
			if (finalSource.exists()) {
				log.info("removing old transMatch files: "
						+ finalSource.getInfo());
				finalSource.delete();
			}
		} catch (IllegalArgumentException e) {
			// this is expected if the source was never created.
			log.info("No old transMatch files to remove");
		}
	}

	// /**
	// * This method sends a message to the UpdateStatusMDB message bean.
	// *
	// * @param jobID
	// * @param percentComplete
	// * @throws NamingException
	// */
	// private void sendToUpdateTransStatus(long jobID, int percentComplete)
	// throws NamingException, JMSException {
	// throw new Error("not re-implemented");
	// // Queue queue = configuration.getUpdateTransMessageQueue();
	// // TransitivityNotification data = new TransitivityNotification(jobID,
	// // percentComplete);
	// // log.info ("send to updateTransQueue " + jobID + " " +
	// // percentComplete);
	// // configuration.sendMessage(queue, data);
	// }

	// /**
	// * This method sends the message to the match dedup bean.
	// *
	// * @param data
	// * @throws NamingException
	// */
	// private void sendToTransMatch(OabaJobMessage data) throws
	// NamingException,
	// JMSException {
	// throw new Error("not re-implemented");
	// // MessageBeanUtils.sendStartData(data, getJmsContext(),
	// transMatchSchedulerQueue,
	// // getLogger());
	// }

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJmsTrace() {
		return jmsTrace;
	}

	@Override
	protected TransitivityProcessingEvent getCompletionEvent() {
		return DONE_CREATE_CHUNK_DATA;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(),
				transMatchSchedulerQueue, getLogger());
	}

}
