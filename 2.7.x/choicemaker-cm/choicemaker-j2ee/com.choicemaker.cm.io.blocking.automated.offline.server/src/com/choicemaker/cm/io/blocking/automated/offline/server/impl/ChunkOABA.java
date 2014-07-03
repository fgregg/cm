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
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;
import com.choicemaker.cm.io.blocking.automated.offline.utils.TreeTransformer;

/**
 * This bean handles the creation of chunks, including chunk data files and their corresponding block files.
 *
 * @author pcheung
 *
 */
public class ChunkOABA implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ChunkOABA.class);
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + ChunkOABA.class.getName());

	private transient MessageDrivenContext mdc = null;
	private transient EJBConfiguration configuration = null;
//	private transient OABAConfiguration oabaConfig = null;

	public void ejbCreate() {
//	log.debug("starting ejbCreate...");
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
//	log.debug("...finished ejbCreate");
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

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		StartData data = null;
		BatchJob batchJob = null;

		log.info("ChunkOABA In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				data = (StartData) msg.getObject();

				batchJob = configuration.findBatchJobById(data.jobID);

				//init values
				IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
				IProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);
				OABAConfiguration oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);

				//get the status
//				Status status = data.status;
				IStatus status = configuration.getStatusLog(data);

				if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
					batchJob.markAsAborted();

					if (batchJob.getDescription().equals(BatchJob.CLEAR)) {
						status.setStatus (IStatus.DONE_PROGRAM);
						oabaConfig.removeTempDir();
					}
				} else {
					String temp = (String) stageModel.properties().get("maxChunkSize");
					int maxChunk = Integer.parseInt(temp);

					//get the maximum number of chunk files
					temp = (String) stageModel.properties().get("maxChunkFiles");
					int maxChunkFiles = Integer.parseInt(temp);

					RecordIDTranslator2 translator = new RecordIDTranslator2 (oabaConfig.getTransIDFactory());
					//recover the translator
					translator.recover();
					translator.close();
/*
					//create the proper block source
					IBlockSinkSourceFactory bFactory = oabaConfig.getBlockFactory();
					IBlockSink bSink = bFactory.getNextSink();
					IBlockSource source = bFactory.getSource(bSink);

					//create the proper oversized source
					IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
					IBlockSink osDedup = osFactory.getNextSink();
					osDedup = osFactory.getNextSink();
					IBlockSource source2 = osFactory.getSource(osDedup);

					ChunkService2 chunkService = new ChunkService2 (source, source2, data.staging, data.master,
						stageModel, masterModel, translator,
						oabaConfig.getChunkIDFactory(),
						oabaConfig.getStageDataFactory(), oabaConfig.getMasterDataFactory(),
						oabaConfig.getCGFactory(), maxChunk, status );

					chunkService.runService();
					log.info( "Number of chunks " + chunkService.getNumChunks());
					log.info( "Done creating chunks " + chunkService.getTimeElapsed());
*/

					//create the os block source.
					IBlockSinkSourceFactory osFactory = oabaConfig.getOversizedFactory();
					osFactory.getNextSource(); //the deduped OS file is file 2.
					IDSetSource source2 = new IDSetSource (osFactory.getNextSource());

					//create the tree transformer.
					TreeTransformer tTransformer = new TreeTransformer (translator,
						oabaConfig.getComparisonTreeFactory(data.stageType));

					//create the oversized block transformer
					Transformer transformerO = new Transformer (translator,
						oabaConfig.getComparisonArrayFactoryOS());

					ChunkService3 chunkService = new ChunkService3 (
						oabaConfig.getTreeSetSource(),
						source2,
						data.staging, data.master,
						stageModel, masterModel,
						oabaConfig.getChunkIDFactory(),
						oabaConfig.getStageDataFactory(), oabaConfig.getMasterDataFactory(),
						translator.getSplitIndex(),
						tTransformer, transformerO, maxChunk, maxChunkFiles,
						status, batchJob );
					chunkService.runService();
					log.info( "Number of chunks " + chunkService.getNumChunks());
					log.info( "Done creating chunks " + chunkService.getTimeElapsed());

					translator.cleanUp();

					data.numChunks = chunkService.getNumChunks();

					sendToUpdateStatus (data.jobID, 50);
					sendToMatch (data);
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
			e.printStackTrace();
		}

		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}


	/** This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		Queue queue = configuration.getUpdateMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		configuration.sendMessage(queue, data);
	}


	/** This method sends a message to the DedupOABA message bean.
	 *
	 * @param request
	 * @throws NamingException
	 */
	private void sendToMatch (StartData data) throws NamingException, JMSException{
//		Queue queue = configuration.getMatchingMessageQueue();
		Queue queue = configuration.getMatchSchedulerMessageQueue();

		log.info("queue " + queue.getQueueName());

		configuration.sendMessage(queue, data);
	}


}
