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

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJobController;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

/**
 * This match dedup bean is used by the Transitivity Engine. It dedups the
 * temporary match results and merge them with the orginal OABA results.
 *
 * @author pcheung
 *
 */
// Singleton: maxSession = 1 (JBoss only)
@MessageDriven(
		activationConfig = {
				@ActivationConfigProperty(propertyName = "maxSession",
						propertyValue = "1"), // Singleton (JBoss only)
				@ActivationConfigProperty(
						propertyName = "destinationLookup",
						propertyValue = "java:/choicemaker/urm/jms/transMatchDedupQueue"),
				@ActivationConfigProperty(propertyName = "destinationType",
						propertyValue = "javax.jms.Queue") })
public class TransMatchDedupMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 2711L;
	private static final Logger log = Logger.getLogger(TransMatchDedupMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatchDedupMDB.class.getName());

	@EJB
	private TransitivityJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private TransitivityParametersController paramsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propController;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchDedupEachQueue")
	private Queue transMatchDedupEachQueue;

//	@Inject
//	private JMSContext jmsContext;

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
					OabaJobMessage data = (OabaJobMessage) o;
					long jobId = data.jobID;
					batchJob = jobController.findTransitivityJob(jobId);
					handleMerge(batchJob);
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
	private void handleMerge(final BatchJob transJob) throws BlockingException {

		log.fine("in handleMerge");

		final long jobId = transJob.getId();
//		final TransitivityParameters params =
//			paramsController.findTransitivityParametersByJobId(jobId);
		final ServerConfiguration serverConfig =
			serverController.findServerConfigurationByJobId(jobId);
		final ProcessingEventLog processingEntry =
			processingController.getProcessingLog(transJob);
//		final String modelConfigId = params.getModelConfigurationName();
//		ImmutableProbabilityModel stageModel =
//			PMManager.getModelInstance(modelConfigId);

		// get the number of processors
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// now merge them all together
		mergeMatches(numProcessors, transJob);

		// mark as done
		final Date now = new Date();
		final String info = null;
		sendToUpdateStatus(transJob, BatchProcessingEvent.DONE, now, info);
		processingEntry.setCurrentProcessingEvent(BatchProcessingEvent.DONE);

	}

	/**
	 * This method does the following: 1. concat all the MatchRecord2 files from
	 * the processors. 2. Merge in the size 2 equivalence classes
	 * MatchRecord2's.
	 *
	 * The output file contains MatchRecord2 with separator records.
	 *
	 */
//	@SuppressWarnings({
//			"rawtypes", "unchecked" })
	protected void mergeMatches(final int num, final BatchJob transJob)
			throws BlockingException {

		throw new Error("not yet implemented");
//		final long jobID = transJob.getId();
//
//		// final sink
//		IMatchRecord2Sink finalSink =
//				TransitivityFileUtils.getCompositeTransMatchSink(jobID);
//
//		IMatchRecord2SinkSourceFactory factory =
//				TransitivityFileUtils.getMatchChunkFactory();
//		ArrayList tempSinks = new ArrayList();
//
//		// the match files start with 1, not 0.
//		for (int i = 1; i <= num; i++) {
//			IMatchRecord2Sink mSink = factory.getSink(i);
//			tempSinks.add(mSink);
//
//			log.info("concatenating file " + mSink.getInfo());
//		}
//
//		// concat all the other chunk MatchRecord2 sinks.
//		finalSink.append();
//		Comparable C = null;
//
//		for (int i = 0; i < tempSinks.size(); i++) {
//			IMatchRecord2Sink mSink = (IMatchRecord2Sink) tempSinks.get(i);
//
//			IMatchRecord2Source mSource = factory.getSource(mSink);
//			if (mSource.exists()) {
//				mSource.open();
//				while (mSource.hasNext()) {
//					MatchRecord2 mr = (MatchRecord2) mSource.next();
//					finalSink.writeMatch(mr);
//
//					if (C == null) {
//						C = mr.getRecordID1();
//					}
//				}
//				mSource.close();
//
//				// clean up
//				mSource.remove();
//			} // end if
//		}
//
//		// finally concat the size-two EC file
//		IMatchRecord2Source mSource =
//				TransitivityFileUtils.getSet2MatchFactory().getNextSource();
//		MatchRecord2 separator = null;
//		if (C != null)
//			separator = MatchRecord2Factory.getSeparator(C);
//
//		if (mSource.exists()) {
//			mSource.open();
//			int i = 0;
//			while (mSource.hasNext()) {
//				i++;
//				MatchRecord2 mr = (MatchRecord2) mSource.next();
//				if (C == null) {
//					C = mr.getRecordID1();
//					separator = MatchRecord2Factory.getSeparator(C);
//				}
//				finalSink.writeMatch(mr);
//				finalSink.writeMatch(separator);
//			}
//			mSource.close();
//			log.info("Num of size 2s read in " + i);
//
//			mSource.remove();
//		}
//
//		finalSink.close();
//
//		log.info("final output " + finalSink.getInfo());
//
//		try {
//			transJob.setDescription(finalSink.getInfo());
//		} catch (Exception e) {
//			log.severe(e.toString());
//		}
	}

	// @Override
	protected void sendToUpdateStatus(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		processingController.updateStatusWithNotification(job, event,
				timestamp, info);
	}

}
