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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_TRANSITIVITY_CACHED_PAIRS_FILE;
import static com.choicemaker.cm.transitivity.core.TransitivityProcessingEvent.DONE_TRANSITIVITY_PAIRWISE;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import com.choicemaker.cm.args.BatchProcessingEvent;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
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
public class TransMatchDedupMDB extends AbstractTransitivityMDB {

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

	@Resource(lookup = "java:/choicemaker/urm/jms/transSerializationQueue")
	private Queue transSerializationQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, BatchJob batchJob,
			TransitivityParameters params, OabaSettings oabaSettings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {
		handleMerge(batchJob, serverConfig, processingLog);
	}

	private void handleMerge(final BatchJob transJob,
			final ServerConfiguration serverConfig,
			final ProcessingEventLog processingEntry) throws BlockingException {

		log.fine("in handleMerge");

		// get the number of processors
		final int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// now merge them all together
		mergeMatches(numProcessors, transJob);

		// mark as done
		final Date now = new Date();
		final String info = null;
		sendToUpdateStatus(transJob, DONE_TRANSITIVITY_PAIRWISE, now, info);
		processingEntry.setCurrentProcessingEvent(DONE_TRANSITIVITY_PAIRWISE);

	}

	/**
	 * This method does the following: 1. concat all the MatchRecord2 files from
	 * the processors. 2. Merge in the size 2 equivalence classes
	 * MatchRecord2's.
	 *
	 * The output file contains MatchRecord2 with separator records.
	 *
	 */
	@SuppressWarnings({
			"rawtypes", "unchecked" })
	protected void mergeMatches(final int num, final BatchJob transJob)
			throws BlockingException {

		// final sink
		IMatchRecord2Sink finalSink =
			TransitivityFileUtils.getCompositeTransMatchSink(transJob);

		IMatchRecord2SinkSourceFactory factory =
			OabaFileUtils.getMatchChunkFactory(transJob);
		ArrayList tempSinks = new ArrayList();

		// the match files start with 1, not 0.
		for (int i = 1; i <= num; i++) {
			IMatchRecord2Sink mSink = factory.getSink(i);
			tempSinks.add(mSink);

			log.info("concatenating file " + mSink.getInfo());
		}

		// concatenate all the other chunk MatchRecord2 sinks.
		finalSink.append();
		Comparable C = null;

		for (int i = 0; i < tempSinks.size(); i++) {
			IMatchRecord2Sink mSink = (IMatchRecord2Sink) tempSinks.get(i);

			IMatchRecord2Source mSource = factory.getSource(mSink);
			if (mSource.exists()) {
				mSource.open();
				while (mSource.hasNext()) {
					MatchRecord2 mr = (MatchRecord2) mSource.next();
					finalSink.writeMatch(mr);

					if (C == null) {
						C = mr.getRecordID1();
					}
				}
				mSource.close();

				// clean up
				mSource.delete();
				;
			} // end if
		}

		// finally concatenate the size-two EC file
		IMatchRecord2Source mSource =
			OabaFileUtils.getSet2MatchFactory(transJob).getNextSource();
		MatchRecord2 separator = null;
		if (C != null)
			separator = MatchRecord2Factory.getSeparator(C);

		if (mSource.exists()) {
			mSource.open();
			int i = 0;
			while (mSource.hasNext()) {
				i++;
				MatchRecord2 mr = (MatchRecord2) mSource.next();
				if (C == null) {
					C = mr.getRecordID1();
					separator = MatchRecord2Factory.getSeparator(C);
				}
				finalSink.writeMatch(mr);
				finalSink.writeMatch(separator);
			}
			mSource.close();
			log.info("Num of size 2s read in " + i);

			mSource.delete();
		}

		finalSink.close();

		log.info("final output " + finalSink.getInfo());

		try {
			String cachedFileName = finalSink.getInfo();
			log.info("Cached results file: " + cachedFileName);
			propController.setJobProperty(transJob,
					PN_TRANSITIVITY_CACHED_PAIRS_FILE, cachedFileName);
		} catch (Exception e) {
			log.severe(e.toString());
		}
	}

	protected void sendToUpdateStatus(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		processingController.updateStatusWithNotification(job, event,
				timestamp, info);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJmsTrace() {
		return jmsTrace;
	}

	@Override
	protected BatchProcessingEvent getCompletionEvent() {
		return DONE_TRANSITIVITY_PAIRWISE;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(),
				transSerializationQueue, getLogger());
	}

}
