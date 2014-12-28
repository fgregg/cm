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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_CREATE_CHUNK_DATA;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.io.blocking.automated.offline.utils.TreeTransformer;

/**
 * This bean handles the creation of chunks, including chunk data files and
 * their corresponding block files.
 *
 * @author pcheung
 *
 */
@Deprecated
public class ChunkMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(ChunkMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ ChunkMDB.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaParametersControllerBean paramsController;

	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private PersistableRecordSourceController rsController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	private JMSContext jmsContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage data = null;
		OabaJob oabaJob = null;

		log.info("ChunkMDB In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				oabaJob = jobController.findOabaJob(jobId);
				OabaParameters params =
					paramsController.findBatchParamsByJobId(jobId);
				OabaProcessing processingEntry =
					processingController.findProcessingLogByJobId(jobId);
				final String modelConfigId = params.getModelConfigurationName();
				IProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}

				if (BatchJobStatus.ABORT_REQUESTED.equals(oabaJob.getStatus())) {
					oabaJob.markAsAborted();

					if (oabaJob.getDescription().equals(BatchJob.MAGIC_DESCRIPTION_CLEAR)) {
						processingEntry
								.setCurrentProcessingEvent(OabaEvent.DONE_OABA);
						OabaFileUtils.removeTempDir(oabaJob);
					}
				} else {
					String temp =
						(String) model.properties().get("maxChunkSize");
					int maxChunk = Integer.parseInt(temp);

					// get the maximum number of chunk files
					temp = (String) model.properties().get("maxChunkFiles");
					int maxChunkFiles = Integer.parseInt(temp);

					RecordIDTranslator2 translator =
						new RecordIDTranslator2(
								OabaFileUtils.getTransIDFactory(oabaJob));
					// recover the translator
					translator.recover();
					translator.close();

					// create the os block source.
					IBlockSinkSourceFactory osFactory =
						OabaFileUtils.getOversizedFactory(oabaJob);
					osFactory.getNextSource(); // the deduped OS file is file 2.
					IDSetSource source2 =
						new IDSetSource(osFactory.getNextSource());

					// create the tree transformer.
					TreeTransformer tTransformer =
						new TreeTransformer(translator,
								OabaFileUtils.getComparisonTreeFactory(oabaJob,
										data.stageType));

					// create the oversized block transformer
					Transformer transformerO =
						new Transformer(translator,
								OabaFileUtils
										.getComparisonArrayFactoryOS(oabaJob));

					ISerializableRecordSource staging =
						rsController.getStageRs(params);
					ISerializableRecordSource master =
						rsController.getMasterRs(params);
					ChunkService3 chunkService =
						new ChunkService3(
								OabaFileUtils.getTreeSetSource(oabaJob),
								source2, staging, master, model,
								OabaFileUtils.getChunkIDFactory(oabaJob),
								OabaFileUtils.getStageDataFactory(oabaJob,
										model),
								OabaFileUtils.getMasterDataFactory(oabaJob,
										model), translator.getSplitIndex(),
								tTransformer, transformerO, maxChunk,
								maxChunkFiles, processingEntry, oabaJob);
					chunkService.runService();
					log.info("Number of chunks " + chunkService.getNumChunks());
					log.info("Done creating chunks "
							+ chunkService.getTimeElapsed());

					translator.cleanUp();

					data.numChunks = chunkService.getNumChunks();

					sendToUpdateStatus(data.jobID, PCT_DONE_CREATE_CHUNK_DATA);
					sendToMatch(data);
				}

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
	}

	private void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToMatch(OabaJobMessage data) throws NamingException,
			JMSException {
		MessageBeanUtils.sendStartData(data, jmsContext, matchSchedulerQueue,
				log);
	}

}
