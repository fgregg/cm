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

import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_RECORD_ID_TYPE;
import static com.choicemaker.cm.io.blocking.automated.offline.core.OabaOperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.IProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonTreeSource;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaEvent;
import com.choicemaker.cm.io.blocking.automated.offline.core.RECORD_ID_TYPE;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;

/**
 * This bean delegates the different chunks to different matcher message beans.
 * It listens for done messages from the matchers bean and when every chunk is
 * done, it calls the MatchDedup bean.
 * 
 * This version reads in one chunk at a time and splits the trees for processing
 * by different MatcherMDB beans.
 * 
 * @author pcheung
 *
 */
@Singleton
public class MatchSchedulerSingleton extends AbstractSchedulerSingleton {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger
			.getLogger(MatchSchedulerSingleton.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchSchedulerSingleton.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matcherQueue")
	private Queue matcherQueue;

	@Inject
	private JMSContext jmsContext;

	// -- Callbacks

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJMSTrace() {
		return jmsTrace;
	}

	/** Remove up the chunk files */
	@Override
	protected void cleanUp(OabaJob oabaJob, OabaJobMessage sd)
			throws BlockingException {
		log.info("cleanUp");

		final long jobId = oabaJob.getId();
		OabaParameters params =
			getParametersController().findOabaParametersByJobId(jobId);
		ServerConfiguration serverConfig =
			getServerController().findServerConfigurationByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		IProbabilityModel model = PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s = "No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}

		int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// remove the data
		final String _numChunks =
			getPropertyController()
					.getJobProperty(oabaJob, PN_CHUNK_FILE_COUNT);
		final int numChunks = Integer.valueOf(_numChunks);

		final String _numRegularChunks =
			getPropertyController().getJobProperty(oabaJob,
					PN_REGULAR_CHUNK_FILE_COUNT);
		final int numRegularChunks = Integer.valueOf(_numRegularChunks);

		IChunkDataSinkSourceFactory stageFactory =
			OabaFileUtils.getStageDataFactory(oabaJob, model);
		IChunkDataSinkSourceFactory masterFactory =
			OabaFileUtils.getMasterDataFactory(oabaJob, model);
		stageFactory.removeAllSinks(numChunks);
		masterFactory.removeAllSinks(numChunks);

		// remove the trees
		final String _recordIdType =
			getPropertyController().getJobProperty(oabaJob, PN_RECORD_ID_TYPE);
		final RECORD_ID_TYPE recordIdType =
			RECORD_ID_TYPE.valueOf(_recordIdType);
		ComparisonTreeGroupSinkSourceFactory factory =
			OabaFileUtils.getComparisonTreeGroupFactory(oabaJob, recordIdType,
					numProcessors);
		for (int i = 0; i < numRegularChunks; i++) {
			for (int j = 1; j <= numProcessors; j++) {
				@SuppressWarnings("rawtypes")
				IComparisonTreeSource source = factory.getSource(i, j);
				source.delete();
			}
		}

		final int numOS = numChunks - numRegularChunks;

		// remove the oversized array files
		ComparisonArrayGroupSinkSourceFactory factoryOS =
			OabaFileUtils.getComparisonArrayGroupFactoryOS(oabaJob,
					numProcessors);
		for (int i = 0; i < numOS; i++) {
			for (int j = 1; j <= numProcessors; j++) {
				@SuppressWarnings("rawtypes")
				IComparisonArraySource sourceOS = factoryOS.getSource(i, j);
				sourceOS.delete();
			}
		}
	}

	@Override
	protected void sendToMatcher(OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, matcherQueue, log);
	}

	@Override
	protected void sendToUpdateStatus(OabaJob job, OabaEvent event,
			Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

	@Override
	protected void sendToMatchDebup(OabaJob job, OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, matchDedupQueue, log);
	}

}
