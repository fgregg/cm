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
import java.rmi.RemoteException;
import java.util.Date;
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

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;

/**
 * This match dedup bean is used by the Transitivity Engine. It dedups the
 * temporary match results and merge them with the orginal OABA results.
 *
 * @author pcheung
 *
 */
//Singleton: maxSession = 1 (JBoss only)
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "maxSession",
				propertyValue = "1"),
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/transMatchDedupQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class TransMatchDedupMDB implements MessageListener, Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(TransMatchDedupMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatchDedupMDB.class.getName());
	
	// @PersistenceContext(unitName = "oaba")
//	private EntityManager em;
	
	// @EJB
	TransitivityJobControllerBean jobController;

	// @EJB
	OabaParametersControllerBean paramsController;

	// @EJB
	OabaProcessingControllerBean processingController;

//	@Resource
//	protected MessageDrivenContext mdc;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateTransQueue")
	private Queue updateTransQueue;

	@Inject
	protected JMSContext jmsContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;

		log.fine("MatchDedupMDB In onMessage");

		TransitivityJob batchJob = null;
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
//			mdc.setRollbackOnly();
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method handles merging individual processor match files.
	 *
	 * @param o
	 * @throws FinderException
	 * @throws RemoteException
	 * @throws BlockingException
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void handleMerge(final BatchJob oabaJob) throws BlockingException {

		log.fine("in handleMerge");

		final long jobId = oabaJob.getId();
		OabaParameters params = paramsController.findBatchParamsByJobId(jobId);

		// init values
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel stageModel =
			PMManager.getModelInstance(modelConfigId);
		// FIXME null transJob, wrong log type
		@SuppressWarnings("unused")
		OabaEventLog processingEntry =
			processingController.getProcessingLog(null);

		// get the number of processors
		String temp = (String) stageModel.properties().get("numProcessors");
		int numProcessors = Integer.parseInt(temp);

		// now merge them all together
		mergeMatches(numProcessors, oabaJob);

		// mark as done
		throw new Error("not yet re-implemented");
//		sendToUpdateTransStatus(jobId,
//				TransitivityProcessing.PCT_DONE_TRANSANALYSIS);
//		// status.setCurrentProcessingEvent( OabaEvent.DONE_TRANSANALYSIS);
//		// HACK
//		assert OabaEvent.DONE_OABA.eventId == TransitivityEvent.DONE_TRANSANALYSIS.eventId;
//		processingEntry.setCurrentOabaEvent(OabaEvent.DONE_OABA);
//		// END HACK

	}

	/**
	 * This method does the following: 1. concat all the MatchRecord2 files from
	 * the processors. 2. Merge in the size 2 equivalence classes
	 * MatchRecord2's.
	 *
	 * The output file contains MatchRecord2 with separator records.
	 *
	 */
	protected void mergeMatches(final int num,
			final BatchJob oabaJob) throws BlockingException {

		throw new Error("not yet implemented");
//		final long jobID = oabaJob.getId();
//		OabaFileUtils oabaConfig = new OabaFileUtils(jobID);
//
//		// final sink
//		IMatchRecord2Sink finalSink =
//			oabaConfig.getCompositeTransMatchSink(jobID);
//
//		IMatchRecord2SinkSourceFactory factory =
//			oabaConfig.getMatchChunkFactory();
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
//					MatchRecord2 mr = mSource.getNext();
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
//		// finally concat the size two EC file
//		IMatchRecord2Source mSource =
//			oabaConfig.getSet2MatchFactory().getNextSource();
//		MatchRecord2 separator = null;
//		if (C != null)
//			separator = MatchRecord2Factory.getSeparator(C);
//
//		if (mSource.exists()) {
//			mSource.open();
//			int i = 0;
//			while (mSource.hasNext()) {
//				i++;
//				MatchRecord2 mr = mSource.getNext();
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
//			TransitivityJob transJob = em.find(TransitivityJobEntity.class, jobID);
//			transJob.setDescription(finalSink.getInfo());
//		} catch (Exception e) {
//			log.severe(e.toString());
//		}
	}

//	@Override
	protected void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		processingController.updateOabaProcessingStatus(job, event, timestamp, info);
	}

}
