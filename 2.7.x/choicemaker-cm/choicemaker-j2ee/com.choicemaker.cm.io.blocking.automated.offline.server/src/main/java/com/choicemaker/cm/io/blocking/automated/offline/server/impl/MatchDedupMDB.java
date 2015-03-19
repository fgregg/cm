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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_OABA_CACHED_RESULTS_FILE;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
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

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This message bean deduplicates match records.
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
// Singleton: maxSession = 1 (JBoss only)
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "maxSession",
				propertyValue = "1"), // Singleton (JBoss only)
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matchDedupQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class MatchDedupMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(MatchDedupMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchDedupMDB.class.getName());

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupEachQueue")
	private Queue matchDedupEachQueue;

	@Inject
	private JMSContext jmsContext;

	// This counts the number of messages sent to MatchDedupEachMDB and number
	// of messages received back. Requires a Singleton message driven bean
	private int countMessages;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.fine("MatchDedupMDB In onMessage");

		BatchJob batchJob = null;
		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				Object o = msg.getObject();

				if (o instanceof OabaJobMessage) {
					// coming in from MatchSchedulerMDB
					// need to dedup each of the temp files from the processors
					countMessages = 0;
					OabaJobMessage data = (OabaJobMessage) o;
					long jobId = data.jobID;
					batchJob = jobController.findBatchJob(jobId);
					handleDedupEach(data, batchJob);

				} else if (o instanceof MatchWriterMessage) {
					// coming in from MatchDedupEachMDB
					// need to merge the deduped temp files when all the
					// processors are done
					MatchWriterMessage data = (MatchWriterMessage) o;
					long jobId = data.jobID;
					batchJob = jobController.findBatchJob(jobId);
					countMessages--;
					log.info("outstanding messages: " + countMessages);
					if (countMessages == 0) {
						handleMerge(data);
					}

				} else {
					log.warning("wrong message body: " + o.getClass().getName());
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (batchJob != null) {
				batchJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method handles merging individual processor match files.
	 */
	private void handleMerge(final MatchWriterMessage d)
			throws BlockingException {

		final long jobId = d.jobID;
		final BatchJob batchJob = jobController.findBatchJob(jobId);
		final OabaParameters params =
			paramsController.findOabaParametersByBatchJobId(jobId);
		final ServerConfiguration serverConfig =
			serverController.findServerConfigurationByJobId(jobId);
		final ProcessingEventLog processingEntry =
			processingController.getProcessingLog(batchJob);
		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s = "No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		if (BatchJobStatus.ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, propController, processingEntry);

		} else {
			processingEntry.setCurrentProcessingEvent(OabaProcessingEvent.MERGE_DEDUP_MATCHES);
			mergeMatches(numProcessors, jobId, batchJob);

			// mark as done
			batchJob.markAsCompleted();
			sendToUpdateStatus(batchJob, BatchProcessingEvent.DONE, new Date(), null);
			processingEntry.setCurrentProcessingEvent(BatchProcessingEvent.DONE);
			// publishStatus(d.jobID);
		}
	}

	/**
	 * This method sends messages to MatchDedupEachMDB to dedup individual match
	 * files.
	 */
	private void handleDedupEach(final OabaJobMessage data,
			final BatchJob batchJob) throws RemoteException, FinderException,
			BlockingException, NamingException, JMSException {

		final long jobId = batchJob.getId();
		final OabaParameters params =
			paramsController.findOabaParametersByBatchJobId(jobId);
		final ServerConfiguration serverConfig =
			serverController.findServerConfigurationByJobId(jobId);
		final ProcessingEventLog processingEntry =
			processingController.getProcessingLog(batchJob);
		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s = "No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		if (BatchJobStatus.ABORT_REQUESTED.equals(batchJob.getStatus())) {
			MessageBeanUtils.stopJob(batchJob, propController, processingEntry);

		} else {
			countMessages = numProcessors;
			for (int i = 1; i <= numProcessors; i++) {
				// send to parallelized match dedup each bean
				OabaJobMessage d2 = new OabaJobMessage(data);
				d2.processingIndex = i;
				sendToMatchDedupEach(d2);
				log.info("outstanding messages: " + i);
			}
		} // end if aborted
	}

	/**
	 * This method merges all the sorted and dedups matches files from the
	 * previous step.
	 */
	private <T extends Comparable<T>> void mergeMatches(final int num,
			final long jobId, final BatchJob oabaJob) throws BlockingException {

		long t = System.currentTimeMillis();

		@SuppressWarnings("unchecked")
		IMatchRecord2SinkSourceFactory<T> factory =
			OabaFileUtils.getMatchTempFactory(oabaJob);
		List<IComparableSink<MatchRecord2<T>>> tempSinks = new ArrayList<>();

		// the match files start with 1, not 0.
		for (int i = 1; i <= num; i++) {
			IMatchRecord2Sink<T> mSink = factory.getSink(i);
			IComparableSink<MatchRecord2<T>> sink =
				new ComparableMRSink<T>(mSink);
			tempSinks.add(sink);
			log.info("merging file " + sink.getInfo());
		}

		@SuppressWarnings("unchecked")
		IMatchRecord2Sink<T> mSink =
			OabaFileUtils.getCompositeMatchSink(oabaJob);
		IComparableSink<MatchRecord2<T>> sink = new ComparableMRSink<T>(mSink);

		IComparableSinkSourceFactory<MatchRecord2<T>> mFactory =
			new ComparableMRSinkSourceFactory<T>(factory);

		int i = GenericDedupService.mergeFiles(tempSinks, sink, mFactory, true);
		log.info("Number of Distinct matches after merge: " + i);

		String cachedFileName = mSink.getInfo();
		log.info("Cached results file: " + cachedFileName);
		propController.setJobProperty(oabaJob, PN_OABA_CACHED_RESULTS_FILE,
				cachedFileName);

		t = System.currentTimeMillis() - t;
		log.info("Time in merge dedup " + t);
	}

	private void sendToUpdateStatus(BatchJob job, BatchProcessingEvent event,
			Date timestamp, String info) {
		processingController.updateStatusWithNotification(job, event,
				timestamp, info);
	}

	private void sendToMatchDedupEach(OabaJobMessage d) {
		MessageBeanUtils.sendStartData(d, jmsContext, matchDedupEachQueue, log);
	}

}
