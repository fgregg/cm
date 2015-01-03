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

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEventLog;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This bean deduplicates the temporary match file produced by a processor. It
 * is called by MatchDedupMDB and it calls it back when it is done.
 *
 * @author pcheung
 *
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
@MessageDriven(
		activationConfig = {
				@ActivationConfigProperty(
						propertyName = "destinationLookup",
						propertyValue = "java:/choicemaker/urm/jms/matchDedupEachQueue"),
				@ActivationConfigProperty(propertyName = "destinationType",
						propertyValue = "javax.jms.Queue") })
public class MatchDedupEachMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger.getLogger(MatchDedupEachMDB.class
			.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchDedupEachMDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, OabaJob oabaJob,
			OabaParameters params, OabaSettings oabaSettings,
			OabaEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		if (processingLog.getCurrentOabaEventId() != OabaProcessing.EVT_MERGE_DEDUP_MATCHES) {
			int maxMatches = oabaSettings.getMaxMatches();
			dedupEach(data.ind, maxMatches, oabaJob);
		}

	}

	/**
	 * This method deduplicates the Nth temporary match file.
	 *
	 * @param num
	 *            - The Nth match temp file
	 * @param maxMatches
	 *            - maximum number of matches to hold in memory
	 * @throws OABABlockingException
	 */
	private void dedupEach(int num, int maxMatches, OabaJob oabaJob)
			throws BlockingException {
		long t = System.currentTimeMillis();
		IMatchRecord2Sink mSink =
			OabaFileUtils.getMatchChunkFactory(oabaJob).getSink(num);
		IMatchRecord2Source mSource =
			OabaFileUtils.getMatchChunkFactory(oabaJob).getSource(mSink);
		ComparableMRSource source = new ComparableMRSource(mSource);

		mSink = OabaFileUtils.getMatchTempFactory(oabaJob).getSink(num);
		IComparableSink sink = new ComparableMRSink(mSink);

		log.info("source " + mSource.getInfo() + " sink " + mSink.getInfo());

		IMatchRecord2SinkSourceFactory factory =
			OabaFileUtils.getMatchTempFactory(oabaJob, num);
		ComparableMRSinkSourceFactory mFactory =
			new ComparableMRSinkSourceFactory(factory);

		if (source.exists()) {
			GenericDedupService service =
				new GenericDedupService(source, sink, mFactory, maxMatches,
						oabaJob);
			service.runDedup();
			int before = service.getNumBefore();
			int after = service.getNumAfter();
			log.info("numBefore " + before + " numAfter " + after);
		} else {
			log.warning(mSource.getInfo() + " does not exist.");
			sink.open();
			sink.close();
		}

		t = System.currentTimeMillis() - t;

		log.info("Time in dedup each " + t);
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
	protected OabaEvent getCompletionEvent() {
		// Used only during invocation of sendToUpdateStatus(..),
		// which does nothing in this class
		return null;
	}

	@Override
	protected void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		assert event == null;
	}

	@Override
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MatchWriterMessage d = new MatchWriterMessage(data);
		MessageBeanUtils.sendMatchWriterData(d, getJmsContext(),
				matchDedupQueue, getLogger());
	}

}
