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
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
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
public class ChunkOABA2 implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(ChunkOABA2.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + ChunkOABA2.class.getName());

	@PersistenceContext (unitName = "oaba")
	private EntityManager em;

	@Resource
	private MessageDrivenContext mdc;

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
		StartData data = null;
		BatchJob batchJob = null;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		log.fine("ChunkOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				final long jobId = data.jobID;
				batchJob =
					configuration.findBatchJobById(em, BatchJobBean.class,
							data.jobID);

				// init values
				BatchParameters params =
					configuration.findBatchParamsByJobId(em, batchJob.getId());
				final String modelConfigId = params.getModelConfigurationName();
				IProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No model corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				OABAConfiguration oabaConfig =
					new OABAConfiguration(params.getModelConfigurationName(),
							jobId);

				// get the status
				OabaProcessing processingEntry =
					configuration.getProcessingLog(em, data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					MessageBeanUtils.stopJob (batchJob, processingEntry, oabaConfig);

				} else {
					String temp = (String) model.properties().get("maxChunkSize");
					int maxChunk = Integer.parseInt(temp);

					//get the number of processors
					temp = (String) model.properties().get("numProcessors");
					int numProcessors = Integer.parseInt(temp);

					//get the maximum number of chunk files
					temp = (String) model.properties().get("maxChunkFiles");
					int maxChunkFiles = Integer.parseInt(temp);

					RecordIDTranslator2 translator = new RecordIDTranslator2 (oabaConfig.getTransIDFactory());
					//recover the translator
					translator.recover();
					translator.close();

					//create the os block source.
					IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
					osFactory.getNextSource(); //the deduped OS file is file 2.
					IDSetSource source2 = new IDSetSource (osFactory.getNextSource());

					//create the tree transformer.
					TreeTransformer tTransformer = new TreeTransformer (translator,
						oabaConfig.getComparisonTreeGroupFactory(data.stageType, numProcessors));

					//create the oversized block transformer
					Transformer transformerO = new Transformer (translator,
						oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors));

					ChunkService3 chunkService =
						new ChunkService3(oabaConfig.getTreeSetSource(),
								source2, params.getStageRs(),
								params.getMasterRs(), model,
								oabaConfig.getChunkIDFactory(),
								oabaConfig.getStageDataFactory(),
								oabaConfig.getMasterDataFactory(),
								translator.getSplitIndex(), tTransformer,
								transformerO, maxChunk, maxChunkFiles, processingEntry,
								batchJob);
					chunkService.runService();
					log.info( "Number of chunks " + chunkService.getNumChunks());
					log.info( "Done creating chunks " + chunkService.getTimeElapsed());

					//transitivity needs the translator
					//translator.cleanUp();

					data.numChunks = chunkService.getNumChunks();
					data.numRegularChunks = chunkService.getNumRegularChunks();

					sendToUpdateStatus (data.jobID, 50);
					sendToMatch (data);
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (JMSException e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
		} catch (BlockingException e) {
			log.severe(e.toString());
			assert batchJob != null;
			batchJob.markAsFailed();
		} catch (Exception e) {
			log.severe(e.toString());
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToMatch (StartData data) throws NamingException, JMSException{
		MessageBeanUtils.sendStartData(data, jmsContext, matchSchedulerQueue, log);
	}

}
