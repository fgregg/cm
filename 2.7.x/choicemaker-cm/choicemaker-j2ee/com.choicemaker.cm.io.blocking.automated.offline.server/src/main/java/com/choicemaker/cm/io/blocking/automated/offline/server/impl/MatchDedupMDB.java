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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.PCT_DONE_OABA;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
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
	private OabaJobControllerBean jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@EJB
	private ServerConfigurationController serverController;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue transitivityQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupEachQueue")
	private Queue matchDedupEachQueue;

	@Inject
	private JMSContext jmsContext;

	// This counts the number of messages sent to MatchDedupEachMDB and number of
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

		log.fine("MatchDedupMDB In onMessage");

		OabaJob oabaJob = null;
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
					oabaJob = jobController.findOabaJob(jobId);
					handleDedupEach(data, oabaJob);

				} else if (o instanceof MatchWriterMessage) {
					// coming in from MatchDedupEachMDB
					// need to merge the deduped temp files when all the
					// processors are done
					MatchWriterMessage data = (MatchWriterMessage) o;
					long jobId = data.jobID;
					oabaJob = jobController.findOabaJob(jobId);
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

//		} catch (JMSException e) {
//			log.severe(e.toString());
//			mdc.setRollbackOnly();
//		} catch (BlockingException e) {
//			log.severe(e.toString());
//			if (batchJob != null)
//				batchJob.markAsFailed();
		} catch (Exception e) {
			log.severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
//			mdc.setRollbackOnly();
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method handles merging individual processor match files.
	 */
	private void handleMerge(final MatchWriterMessage d) throws BlockingException {

		final long jobId = d.jobID;
		final OabaJob oabaJob = jobController.findOabaJob(jobId);
		final OabaParameters params =
			paramsController.findBatchParamsByJobId(jobId);
		final ServerConfiguration serverConfig =
			serverController.findServerConfigurationByJobId(jobId);
		final OabaProcessing processingEntry =
				processingController.findProcessingLogByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s =
				"No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		if (BatchJobStatus.ABORT_REQUESTED.equals(oabaJob.getStatus())) {
			MessageBeanUtils.stopJob(oabaJob, processingEntry);

		} else {
			processingEntry.setCurrentProcessingEvent(OabaEvent.MERGE_DEDUP_MATCHES);
			mergeMatches(numProcessors, jobId, oabaJob);

			// mark as done
			oabaJob.markAsCompleted();
			sendToUpdateStatus(d.jobID, PCT_DONE_OABA);
			processingEntry.setCurrentProcessingEvent(OabaEvent.DONE_OABA);
//			publishStatus(d.jobID);
		}
	}
	
	/**
	 * This method sends messages to MatchDedupEachMDB to dedup individual match
	 * files.
	 */
	private void handleDedupEach(final OabaJobMessage data, final OabaJob oabaJob)
			throws RemoteException, FinderException, BlockingException,
			NamingException, JMSException {

		final long jobId = oabaJob.getId();
		final OabaParameters params =
			paramsController.findBatchParamsByJobId(jobId);
		final ServerConfiguration serverConfig =
			serverController.findServerConfigurationByJobId(jobId);
		final OabaProcessing processingEntry =
				processingController.findProcessingLogByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		final ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s =
				"No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		if (BatchJobStatus.ABORT_REQUESTED.equals(oabaJob.getStatus())) {
			MessageBeanUtils.stopJob(oabaJob, processingEntry);

		} else {
			countMessages = numProcessors;
			for (int i = 1; i <= numProcessors; i++) {
				// send to parallelized match dedup each bean
				OabaJobMessage d2 = new OabaJobMessage(data);
				d2.ind = i;
				sendToMatchDedupEach(d2);
				log.info("outstanding messages: " + i);
			}
		} // end if aborted
	}

	/**
	 * This method merges all the sorted and dedups matches files from the
	 * previous step.
	 */
	private <T extends Comparable<T>> void mergeMatches(final int num, final long jobId,
			final BatchJob oabaJob) throws BlockingException {

		long t = System.currentTimeMillis();

		@SuppressWarnings("unchecked")
		IMatchRecord2SinkSourceFactory<T> factory =
			OabaFileUtils.getMatchTempFactory(oabaJob);
		List<IComparableSink<MatchRecord2<T>>> tempSinks = new ArrayList<>();

		// the match files start with 1, not 0.
		for (int i = 1; i <= num; i++) {
			IMatchRecord2Sink<T> mSink = factory.getSink(i);
			IComparableSink<MatchRecord2<T>> sink = new ComparableMRSink<T>(mSink);
			tempSinks.add(sink);
			log.info("merging file " + sink.getInfo());
		}

		@SuppressWarnings("unchecked")
		IMatchRecord2Sink<T> mSink = OabaFileUtils.getCompositeMatchSink(oabaJob);
		IComparableSink<MatchRecord2<T>> sink = new ComparableMRSink<T>(mSink);

		IComparableSinkSourceFactory<MatchRecord2<T>> mFactory =
			new ComparableMRSinkSourceFactory<T>(factory);

		int i = GenericDedupService.mergeFiles(tempSinks, sink, mFactory, true);

		// FIXME HACK
		log.info("Number of Distinct matches after merge: " + i);
		oabaJob.setDescription(mSink.getInfo());
		// END FIXME HACK

		t = System.currentTimeMillis() - t;

		log.info("Time in merge dedup " + t);
	}

	private void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	private void sendToMatchDedupEach(OabaJobMessage d) {
		MessageBeanUtils.sendStartData(d, jmsContext, matchDedupEachQueue, log);
	}

}
