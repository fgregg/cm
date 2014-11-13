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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.FinderException;
import javax.ejb.MessageDriven;
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
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.EJBConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterData;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OABAConfiguration;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.StartData;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.BatchParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.TransitivityJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This message bean handles the deduping of match records.
 * 
 * This version loads one chunk data into memory and different processors handle
 * different trees of the same chunk. There are N matches files, where N is the
 * number of processors.
 * 
 * For each of the N files, we need to dedup it, then merge all the dedup files
 * together.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
// Singleton: maxSession = 1 (JBoss only)
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "maxSession",
				propertyValue = "1"), // Singleton (JBoss only)
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matchDedupQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class MatchDedupOABA2 implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(MatchDedupOABA2.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchDedupOABA2.class.getName());

	@PersistenceContext(unitName = "oaba")
	private EntityManager em;

//	@Resource
//	private MessageDrivenContext mdc;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue transitivityQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupEachQueue")
	private Queue matchDedupEachQueue;

	@Inject
	private JMSContext jmsContext;

	// This counts the number of messages sent to MatchDedupEach and number of
	// done messages got back. Requires a Singleton message driven bean
	private int countMessages;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		EJBConfiguration configuration = EJBConfiguration.getInstance();

		log.fine("MatchDedupOABA2 In onMessage");

		BatchJob batchJob = null;
		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof StartData) {
					// coming in from MatchScheduler2
					// need to dedup each of the temp files from the processors
					countMessages = 0;
					StartData data = (StartData) o;
					long jobId = data.jobID;
					batchJob =
						configuration.findBatchJobById(em, BatchJobBean.class,
								jobId);
					handleDedupEach(data, batchJob);

				} else if (o instanceof MatchWriterData) {
					// coming in from MatchDedupEach
					// need to merge the deduped temp files when all the
					// processors are done
					MatchWriterData data = (MatchWriterData) o;
					long jobId = data.jobID;
					batchJob =
						configuration.findBatchJobById(em, BatchJobBean.class,
								jobId);
					countMessages--;
					if (countMessages == 0) {
						handleMerge(data);
					}

				} else {
					log.warning("wrong message body: " + o.getClass().getName());
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

//		} catch (JMSException e) {
//			log.severe(e.toString());
//			mdc.setRollbackOnly();
//		} catch (BlockingException e) {
//			log.severe(e.toString());
//			if (batchJob != null)
//				batchJob.markAsFailed();
		} catch (Exception e) {
			log.severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
//			mdc.setRollbackOnly();
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method handles merging individual processor match files.
	 */
	private void handleMerge(final MatchWriterData d) throws BlockingException {

		EJBConfiguration configuration = EJBConfiguration.getInstance();
		final long jobId = d.jobID;
		BatchJob batchJob =
			configuration.findBatchJobById(em, BatchJobBean.class, d.jobID);
		OABAConfiguration oabaConfig = new OABAConfiguration(jobId);

		// init values
		BatchParameters params =
			configuration.findBatchParamsByJobId(em, batchJob.getId());
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		if (stageModel == null) {
			String s = "No model corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		oabaConfig =
			new OABAConfiguration(params.getModelConfigurationName(), jobId);

		// get the status
		OabaProcessing processingEntry =
			configuration.getProcessingLog(em, d.jobID);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, processingEntry, oabaConfig);

		} else {
			processingEntry.setCurrentProcessingEvent(OabaEvent.MERGE_DEDUP_MATCHES);

			// get the number of processors
			String temp = (String) stageModel.properties().get("numProcessors");
			int numProcessors = Integer.parseInt(temp);

			// now merge them all together
			mergeMatches(numProcessors, jobId, batchJob);

			// mark as done
			sendToUpdateStatus(d.jobID, 100);
			processingEntry.setCurrentProcessingEvent(OabaEvent.DONE_OABA);
//			publishStatus(d.jobID);

			// send to transitivity
			log.info("runTransitivity " + params.getTransitivity());
			if (params.getTransitivity()) {
				StartData startTransivityData = createStartDataForTransitivityAnalysis(d.jobID);
				sendToTransitivity(startTransivityData);
			}
		}
	}
	
	private StartData createStartDataForTransitivityAnalysis(
			final long batchJobId) {
		EJBConfiguration configuration = EJBConfiguration.getInstance();
		BatchParameters batchParams = configuration.findBatchParamsByJobId(em, batchJobId);
		BatchJob batchJob = em.find(BatchJobBean.class, batchJobId);
		TransitivityJob job = new TransitivityJobBean(batchParams, batchJob);
		em.persist(job);
		final long transJobId = job.getId();

		// Create a new processing entry
		OabaProcessing processing =
			configuration.createProcessingLog(em, transJobId);

		// Log the job info
		log.fine("BatchJob: " + batchJob.toString());
		log.fine("BatchParameters: " + batchParams.toString());
		log.fine("Processing entry: " + processing.toString());
		log.fine("TransitivityJob: " + job.toString());

		StartData retVal = new StartData(transJobId);
		return retVal;
	}

	/**
	 * This method sends messages to MatchDedupEach to dedup individual match
	 * files.
	 */
	private void handleDedupEach(final StartData data, final BatchJob batchJob)
			throws RemoteException, FinderException, BlockingException,
			NamingException, JMSException {

		EJBConfiguration configuration = EJBConfiguration.getInstance();
		final long jobId = batchJob.getId();
		OABAConfiguration oabaConfig = new OABAConfiguration(jobId);

		// init values
		BatchParameters params =
			configuration.findBatchParamsByJobId(em, batchJob.getId());
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		if (stageModel == null) {
			String s = "No model corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		oabaConfig =
			new OABAConfiguration(params.getModelConfigurationName(), jobId);

		OabaProcessing processingEntry =
			configuration.getProcessingLog(em, jobId);

		if (BatchJob.STATUS_ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, processingEntry, oabaConfig);

		} else {
			// get the number of processors
			String temp = (String) stageModel.properties().get("numProcessors");
			int numProcessors = Integer.parseInt(temp);

			// max number of match in a temp file
			// temp = (String) stageModel.properties.get("maxMatchSize");
			// int maxMatches = Integer.parseInt(temp);

			// the match files start with 1, not 0.
			for (int i = 1; i <= numProcessors; i++) {
				// send to parallelized match dedup each bean
				StartData d2 = new StartData(data);
				d2.ind = i;
				sendToMatchDedupEach(d2);
			}

			countMessages = numProcessors;

		} // end if aborted
	}

	/**
	 * This method merges all the sorted and dedups matches files from the
	 * previous step.
	 * 
	 *
	 */
	private void mergeMatches(final int num, final long jobId,
			final BatchJob batchJob) throws BlockingException {

		long t = System.currentTimeMillis();

		OABAConfiguration oabaConfig = new OABAConfiguration(jobId);
		IMatchRecord2SinkSourceFactory factory =
			oabaConfig.getMatchTempFactory();
		List<IComparableSink> tempSinks = new ArrayList<>();

		// the match files start with 1, not 0.
		for (int i = 1; i <= num; i++) {
			IMatchRecord2Sink mSink = factory.getSink(i);
			IComparableSink sink = new ComparableMRSink(mSink);
			tempSinks.add(sink);

			log.info("merging file " + sink.getInfo());
		}

		IMatchRecord2Sink mSink = oabaConfig.getCompositeMatchSink(jobId);
		IComparableSink sink = new ComparableMRSink(mSink);

		ComparableMRSinkSourceFactory mFactory =
			new ComparableMRSinkSourceFactory(factory);

		int i = GenericDedupService.mergeFiles(tempSinks, sink, mFactory, true);

		log.info("Number of Distinct matches after merge: " + i);
		batchJob.setDescription(mSink.getInfo());

		t = System.currentTimeMillis() - t;

		log.info("Time in merge dedup " + t);
	}

	private void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToMatchDedupEach(StartData d) {
		MessageBeanUtils.sendStartData(d, jmsContext, matchDedupEachQueue, log);
	}

	private void sendToTransitivity(StartData d) {
		MessageBeanUtils.sendStartData(d, jmsContext, transitivityQueue, log);
	}

}
