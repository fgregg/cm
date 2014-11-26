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
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2SinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Source;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessing;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSource;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParameters;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettings;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.SettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.services.GenericDedupService;

/**
 * This bean dedups the temporary match file produces by a processor. It is
 * called by MatchDedupOABA2 and it calls it back when it is done.
 *
 * @author pcheung
 *
 */
@SuppressWarnings("rawtypes")
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matchDedupEachQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class MatchDedupEach implements MessageListener, Serializable {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(MatchDedupEach.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchDedupEach.class.getName());

	@EJB
	private OabaJobControllerBean jobController;

	@EJB
	private SettingsController settingsController;

	@EJB
	private OabaParametersControllerBean paramsController;
	
	@EJB
	private OabaProcessingControllerBean processingController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Inject
	private JMSContext jmsContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message inMessage) {
		jmsTrace.info("Entering onMessage for " + this.getClass().getName());
		ObjectMessage msg = null;
		OabaJob oabaJob = null;

		log.fine("MatchDedupEach In onMessage");

		try {
			if (inMessage instanceof ObjectMessage) {
				msg = (ObjectMessage) inMessage;
				OabaJobMessage data = (OabaJobMessage) msg.getObject();

				final long jobId = data.jobID;
				oabaJob = jobController.find(jobId);
				final OabaParameters params =
					paramsController.findBatchParamsByJobId(jobId);
				final OabaProcessing processingEntry =
					processingController.findProcessingLogByJobId(jobId);
				final String modelConfigId = params.getModelConfigurationName();
				final IProbabilityModel model =
					PMManager.getModelInstance(modelConfigId);
				if (model == null) {
					String s =
						"No model corresponding to '" + modelConfigId + "'";
					log.severe(s);
					throw new IllegalArgumentException(s);
				}
				final OabaSettings settings =
					settingsController.findOabaSettingsByJobId(jobId);
				
				if (BatchJob.STATUS_ABORT_REQUESTED
						.equals(oabaJob.getStatus())) {
					MessageBeanUtils.stopJob(oabaJob, processingEntry);

				} else {
					if (processingEntry.getCurrentProcessingEventId() != OabaProcessing.EVT_MERGE_DEDUP_MATCHES) {
						int maxMatches = settings.getMaxMatches();
						dedupEach(data.ind, maxMatches, oabaJob);
					}
					MatchWriterMessage d = new MatchWriterMessage(data);
					sendToMatchDedupOABA2(d);
				}

			} else {
				log.warning("wrong type: " + inMessage.getClass().getName());
			}

		} catch (Exception e) {
			log.severe(e.toString());
			if (oabaJob != null) {
				oabaJob.markAsFailed();
			}
		}
		jmsTrace.info("Exiting onMessage for " + this.getClass().getName());
	}

	/**
	 * This method dedups the Nth match temp file.
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

	private void sendToMatchDedupOABA2(MatchWriterMessage d) {
		MessageBeanUtils.sendMatchWriterData(d, jmsContext, matchDedupQueue, log);
	}

}
