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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.transitivity.core.TransitivityEvent.DONE_TRANS_DEDUP_OVERSIZED;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.OperationalPropertyController;
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
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.result.MatchToBlockTransformer2;
import com.choicemaker.cm.io.blocking.automated.offline.result.Size2MatchProducer;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaProcessingController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordIdController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.RecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;

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
public class StartTransitivityMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(StartTransitivityMDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ StartTransitivityMDB.class.getName());

	// @EJB
	private TransitivityJobController jobController;

	// @EJB
	// private OabaSettingsController settingsController;

	// @EJB
	private OabaParametersController paramsController;

	// @EJB
	private OabaProcessingController processingController;

	// @EJB
	private RecordSourceController rsController;

	// @EJB
	private RecordIdController ridController;

	// @EJB
	private OperationalPropertyController propController;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		TransitivityJob transJob = null;

		log.fine("StartTransitivityMDB In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				OabaJobMessage d = (OabaJobMessage) o;
				final long jobId = d.jobID;
				transJob = jobController.findTransitivityJob(jobId);
				transJob.markAsStarted();
				OabaParameters params =
					paramsController.findOabaParametersByJobId(jobId);

				removeOldFiles(transJob);
				createChunks(transJob, params);

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (transJob != null) {
				transJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/*
	 * This method calls MatchToBlockTransformer to create blocks for the
	 * equivalence classes. It then calls ChunkService3 to create chunks.
	 */
	private void createChunks(TransitivityJob transJob, OabaParameters params)
			throws Exception {

		// get the match record source
		IMatchRecord2Source mSource =
			OabaFileUtils.getCompositeMatchSource(transJob);

		// get the transitivity block sink
		IBlockSink bSink =
			TransitivityFileUtils.getTransitivityBlockFactory(transJob)
					.getNextSink();

		// recover the translator
		ImmutableRecordIdTranslator translator =
			ridController.findRecordIdTranslator(transJob);

		// Create blocks
		IMatchRecord2SinkSourceFactory mFactory =
			OabaFileUtils.getMatchTempFactory(transJob);
		IRecordIdSinkSourceFactory idFactory =
			ridController.getRecordIdSinkSourceFactory(transJob);
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

		// clean up
		idSink.remove();

		IBlockSource bSource =
			TransitivityFileUtils.getTransitivityBlockFactory(transJob)
					.getSource(bSink);
		IDSetSource source2 = new IDSetSource(bSource);

		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model = PMManager.getModelInstance(modelConfigId);
		String temp = (String) model.properties().get("maxChunkSize");
		int maxChunk = Integer.parseInt(temp);

		//
		if (transformer.getMaxEC() > maxChunk)
			throw new RuntimeException("There is an equivalence class of size "
					+ transformer.getMaxEC()
					+ ", which is bigger than the max chunk size of "
					+ maxChunk + ".");

		// get the number of processors
		temp = (String) model.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		// get the number of processors
		temp = (String) model.properties().get("maxChunkFiles");
		int numFiles = Integer.parseInt(temp);

		// create the oversized block transformer
		Transformer transformerO =
			new Transformer(translator,
					OabaFileUtils.getComparisonArrayGroupFactoryOS(transJob,
							numProcessors));

		// set the correct status for chunk could run.
		final long jobId = transJob.getId();
		// FIXME null transJob, wrong log type
		OabaEventLog status = processingController.getProcessingLog(null);
		// status.setCurrentProcessingEvent(TransitivityEvent.EVT_DONE_TRANS_DEDUP_OVERSIZED);
		// HACK
		assert DONE_TRANS_DEDUP_OVERSIZED.eventId == OabaEvent.DONE_DEDUP_OVERSIZED.eventId;
		status.setCurrentOabaEvent(OabaEvent.DONE_DEDUP_OVERSIZED);
		// END HACK

		ISerializableRecordSource staging = rsController.getStageRs(params);
		ISerializableRecordSource master = rsController.getMasterRs(params);
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
		propController.setJobProperty(transJob, PN_CHUNK_FILE_COUNT,
				String.valueOf(numChunks));

		// this is important because in transitivity, there are only OS chunks.
		final int numRegularChunks = 0;
		log.info("Number of regular chunks " + numChunks);
		propController.setJobProperty(transJob, PN_REGULAR_CHUNK_FILE_COUNT,
				String.valueOf(numRegularChunks));

		// clean up translator
		// translator.cleanUp();

		log.info("send to transitivity matcher");
		OabaJobMessage data = new OabaJobMessage(jobId);
		sendToTransMatch(data);
		sendToUpdateTransStatus(data.jobID, 30);
	}

	/**
	 * This method removes the transMatch* files, because the pairs are conact
	 * 
	 * @param jobID
	 */
	private void removeOldFiles(TransitivityJob transJob)
			throws BlockingException {
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

	/**
	 * This method sends a message to the UpdateStatusMDB message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateTransStatus(long jobID, int percentComplete)
			throws NamingException, JMSException {
		throw new Error("not yet re-implemented");
		// Queue queue = configuration.getUpdateTransMessageQueue();
		// TransitivityNotification data = new TransitivityNotification(jobID, percentComplete);
		// log.info ("send to updateTransQueue " + jobID + " " +
		// percentComplete);
		// configuration.sendMessage(queue, data);
	}

	/**
	 * This method sends the message to the match dedup bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	private void sendToTransMatch(OabaJobMessage data) throws NamingException,
			JMSException {
		throw new Error("Not yet re-implemented");
		// Queue queue = configuration.getTransMatchSchedulerMessageQueue();
		// configuration.sendMessage(queue, data);
	}

}
