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
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.ISerializableRecordSource;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.PersistableRecordSourceController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.io.blocking.automated.offline.utils.TreeTransformer;

/**
 * This bean handles the creation of chunks, including chunk data files and their corresponding block files.
 *
 * In this version, a chunk has multiple tree or array files so mutiple beans are process the
 * same chunk at the same time.
 *
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/chunkQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class Chunk2MDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(Chunk2MDB.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + Chunk2MDB.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private PersistableRecordSourceController rsController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Inject
	private JMSContext jmsContext;

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJobMessage data = null;
		OabaJob oabaJob = null;

		log.fine("ChunkMDB In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				oabaJob = jobController.findOabaJob(jobId);
				OabaParameters params =
					paramsController.findBatchParamsByJobId(jobId);
				OabaSettings oabaSettings =
						oabaSettingsController.findOabaSettingsByJobId(jobId);
				OabaProcessing processingEntry =
						processingController.findProcessingLogByJobId(jobId);
				ServerConfiguration serverConfig = serverController.findServerConfigurationByJobId(jobId);
				if (oabaJob == null || params == null || oabaSettings == null || serverConfig == null) {
					String s = "Unable to find a job, parameters, settings or server configuration for " + jobId;
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				final String modelConfigId = params.getModelConfigurationName();
				ImmutableProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No modelId corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(oabaJob.getStatus())) {
					MessageBeanUtils.stopJob (oabaJob, processingEntry);

				} else {
					int maxChunk = oabaSettings.getMaxChunkSize();
					int numProcessors = serverConfig.getMaxChoiceMakerThreads();
					int maxChunkFiles = serverConfig.getMaxOabaChunkFileCount();
					log.info("Maximum chunk size: " + maxChunk);
					log.info("Number of processors: " + numProcessors);
					log.info("Maximum chunk files: " + maxChunkFiles);

					RecordIDTranslator2 translator = new RecordIDTranslator2 (OabaFileUtils.getTransIDFactory(oabaJob));
					//recover the translator
					translator.recover();
					translator.close();
					log.info("Record translator: " + translator);

					//create the os block source.
					final IBlockSinkSourceFactory osFactory = OabaFileUtils.getOversizedFactory(oabaJob);
					log.info("Oversized factory: " + osFactory);
					osFactory.getNextSource(); //the deduped OS file is file 2.
					final IDSetSource source2 = new IDSetSource (osFactory.getNextSource());
					log.info("Deduped oversized source: " + source2);

					//create the tree transformer.
					final TreeTransformer tTransformer = new TreeTransformer (translator,
							OabaFileUtils.getComparisonTreeGroupFactory(oabaJob, data.stageType, numProcessors));

					//create the oversized block transformer
					final Transformer transformerO = new Transformer (translator,
							OabaFileUtils.getComparisonArrayGroupFactoryOS(oabaJob, numProcessors));

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
					log.info("Chunk service: " + chunkService);
					chunkService.runService();
					log.info( "Number of chunks " + chunkService.getNumChunks());
					log.info( "Number of regular chunks " + chunkService.getNumRegularChunks());
					log.info( "Done creating chunks " + chunkService.getTimeElapsed());

					//transitivity needs the translator
					//translator.cleanUp();

					data.numChunks = chunkService.getNumChunks();
					data.numRegularChunks = chunkService.getNumRegularChunks();

					sendToUpdateStatus (data.jobID, PCT_DONE_CREATE_CHUNK_DATA);
					sendToMatch (data);
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

	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToMatch (OabaJobMessage data) throws NamingException, JMSException{
		MessageBeanUtils.sendStartData(data, jmsContext, matchSchedulerQueue, log);
	}

}
