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

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.naming.NamingException;

import java.util.logging.Logger;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.XmlConfException;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IRecordIDSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IStatus;
import com.choicemaker.cm.io.blocking.automated.offline.impl.IDSetSource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.RecordIDTranslator2;
import com.choicemaker.cm.io.blocking.automated.offline.result.MatchToBlockTransformer2;
import com.choicemaker.cm.io.blocking.automated.offline.result.Size2MatchProducer;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.UpdateData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.ChunkService3;
import com.choicemaker.cm.io.blocking.automated.offline.utils.Transformer;

/**
 * This message bean starts the Transitivity Engine.
 * It assumes that it can access the translator file.
 *
 * @author pcheung
 *
 */
public class TransitivityBean implements MessageDrivenBean, MessageListener {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransitivityBean.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace." + TransitivityBean.class.getName());

	private transient MessageDrivenContext mdc;
	private transient EJBConfiguration configuration;
	private transient TransitivityJob transJob;
	private transient OABAConfiguration oabaConfig;
	private StartData data;


	public void ejbCreate() {
		try {
			this.configuration = EJBConfiguration.getInstance();
		} catch (Exception e) {
			log.severe(e.toString());
		}
	}


	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException {
	}

	/* (non-Javadoc)
	 * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
	 */
	public void setMessageDrivenContext(MessageDrivenContext mdc) throws EJBException {
		this.mdc = mdc;
	}

	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.fine("TransitivityBean In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				data = (StartData) o;
				transJob = configuration.getTransitivityJob(data.jobID);
				transJob.markAsStarted();
				transJob.setModel(data.stageModelName);
				transJob.setDiffer(data.low);
				transJob.setMatch(data.high);
				transJob.setDescription("");

				data.jobID = transJob.getId().intValue();

				oabaConfig = new OABAConfiguration (data.stageModelName, data.jobID);

				// clean up
				removeOldFiles ();

				createChunks ();

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			mdc.setRollbackOnly();
			try {
				if (transJob != null) transJob.markAsFailed();
			} catch (RemoteException e1) {
				log.severe(e1.toString());
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}



	/* This method calls MatchToBlockTransformer to create blocks for the equivalence classes.
	 * It then calls ChunkService3 to create chunks.
	 *
	 */
	private void createChunks ()
		throws RemoteException, FinderException, XmlConfException, BlockingException, NamingException, JMSException {

		//get the match record source
		IMatchRecord2Source mSource = oabaConfig.getCompositeMatchSource (data.jobID);

		//get the transitivity block sink
		IBlockSink bSink = oabaConfig.getTransitivityBlockFactory().getNextSink();

		//recover the translator
		RecordIDTranslator2 translator = new RecordIDTranslator2 (oabaConfig.getTransIDFactory());
		translator.recover();
		translator.close();

		//Create blocks
		IMatchRecord2SinkSourceFactory mFactory = oabaConfig.getMatchTempFactory();
		IRecordIDSinkSourceFactory idFactory = oabaConfig.getRecordIDFactory();
		IRecordIDSink idSink = idFactory.getNextSink();
		MatchToBlockTransformer2 transformer = new MatchToBlockTransformer2(mSource,
			mFactory, translator, bSink, idSink);
		int numRecords = transformer.process();
		log.fine("Number of records: " + numRecords);

		//build a MatchRecord2Sink for all pairs belonging to the size 2 sets.
		Size2MatchProducer producer = new Size2MatchProducer(mSource,
			idFactory.getSource (idSink), oabaConfig.getSet2MatchFactory().getNextSink());
		int twos = producer.process();
		log.info("number of size 2 EC: " + twos);

		//clean up
		idSink.remove();


		IBlockSource bSource = oabaConfig.getTransitivityBlockFactory().getSource(bSink);
		IDSetSource source2 = new IDSetSource (bSource);

		IProbabilityModel stageModel = PMManager.getModelInstance(data.stageModelName);
		IProbabilityModel masterModel = PMManager.getModelInstance(data.masterModelName);
		String temp = (String) stageModel.properties().get("maxChunkSize");
		int maxChunk = Integer.parseInt(temp);

		//
		if (transformer.getMaxEC() > maxChunk)
			throw new RuntimeException
			("There is an equivalence class of size " + transformer.getMaxEC() +
			", which is bigger than the max chunk size of " + maxChunk + ".");

		//get the number of processors
		temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		//get the number of processors
		temp = (String) stageModel.properties().get("maxChunkFiles");
		int numFiles = Integer.parseInt(temp);

		//create the oversized block transformer
		Transformer transformerO = new Transformer (translator,
			oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors));

		//set the correct status for chunk could run.
		IStatus status = configuration.getStatusLog(data);
		status.setStatus(IStatus.DONE_DEDUP_OVERSIZED);

		ChunkService3 chunkService = new ChunkService3 (
			source2,
			null,
			data.staging, data.master,
			stageModel, masterModel,
			oabaConfig.getChunkIDFactory(),
			oabaConfig.getStageDataFactory(), oabaConfig.getMasterDataFactory(),
			translator.getSplitIndex(),
			transformerO, null, maxChunk, numFiles, status, transJob );
		chunkService.runService();

		data.numChunks = chunkService.getNumChunks();

		//this is important because in transitivity, there is only OS chunks.
		data.numRegularChunks = 0;

		log.info( "Number of chunks " + chunkService.getNumChunks());
		log.info( "Done creating chunks " + chunkService.getTimeElapsed());

		//clean up translator
		//translator.cleanUp();

		log.info("send to matcher");
		sendToTransMatch (data);

		sendToUpdateTransStatus (data.jobID, 30);
	}


	/**
	 *  This method removes the transMatch* files, because the pairs are conact
	 * @param jobID
	 */
	private void removeOldFiles () throws BlockingException{
		try {
			//final sink
			IMatchRecord2Source finalSource =
				oabaConfig.getCompositeTransMatchSource(data.jobID);
			if (finalSource.exists()) {
				log.info("removing old transMatch files: " + finalSource.getInfo());
				finalSource.remove();
			}
		} catch (IllegalArgumentException e) {
			//this is expected if the source was never created.
			log.info("No old transMatch files to remove");
		}
	}


	/** This method sends a message to the UpdateStatus message bean.
	 *
	 * @param jobID
	 * @param percentComplete
	 * @throws NamingException
	 */
	private void sendToUpdateTransStatus (long jobID, int percentComplete) throws NamingException, JMSException {
		Queue queue = configuration.getUpdateTransMessageQueue();

		UpdateData data = new UpdateData();
		data.jobID = jobID;
		data.percentComplete = percentComplete;

		log.info ("send to updateTransQueue " + jobID + " " + percentComplete);

		configuration.sendMessage(queue, data);
	}



	/** This method sends the message to the match dedup bean.
	 *
	 * @param data
	 * @throws NamingException
	 */
	private void sendToTransMatch (StartData data) throws NamingException, JMSException{
		Queue queue = configuration.getTransMatchSchedulerMessageQueue();
		configuration.sendMessage(queue, data);
	}


}
