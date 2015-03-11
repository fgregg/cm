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

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractSchedulerSingleton;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MessageBeanUtils;

/**
 * This is the match scheduler for the Transitivity Engine.
 * 
 * @author pcheung
 *
 */
@Singleton
public class TransMatchSchedulerSingleton extends AbstractSchedulerSingleton {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(TransMatchSchedulerSingleton.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatchSchedulerSingleton.class.getName());
	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchDedupQueue")
	private Queue transMatchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateTransQueue")
	private Queue updateTransQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatcherQueue")
	private Queue transMatcherQueue;

	@Inject
	private JMSContext jmsContext;

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJMSTrace() {
		return jmsTrace;
	}

	@Override
	protected void cleanUp(BatchJob job, OabaJobMessage sd)
			throws BlockingException {
		log.fine("cleanUp");

		throw new Error("not yet implemented");
		// final long jobId = sd.jobID;
		// TransitivityParameters params = em.find(OabaParametersEntity.class,
		// jobId);
		// final String modelConfigId = params.getModelConfigurationName();
		// ImmutableProbabilityModel stageModel =
		// PMManager.getModelInstance(modelConfigId);
		// //get the number of processors
		// String temp = (String) stageModel.properties().get("numProcessors");
		// int numProcessors = Integer.parseInt(temp);
		//
		// //remove the data
		// TransitivityFileUtils oabaConfig = new TransitivityFileUtils(jobId);
		// IChunkDataSinkSourceFactory stageFactory =
		// oabaConfig.getStageDataFactory();
		// IChunkDataSinkSourceFactory masterFactory=
		// oabaConfig.getMasterDataFactory();
		// stageFactory.removeAllSinks(sd.numChunks);
		// masterFactory.removeAllSinks(sd.numChunks);
		//
		// //oversized
		// ComparisonArrayGroupSinkSourceFactory factoryOS =
		// oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors);
		//
		// //there is always 1 chunk to remove.
		// int c = sd.numChunks;
		// if (c == 0) c = 1;
		//
		// for (int i=0; i<c; i++) {
		// for (int j=1; j<=numProcessors; j++) {
		// IComparisonArraySource sourceOS = factoryOS.getSource(i, j);
		// sourceOS.remove();
		// log.fine("removing " + sourceOS.getInfo());
		// }
		// }
	}

	@Override
	protected void sendToMatchDebup(BatchJob job, OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, transMatchDedupQueue,
				log);
	}

	@Override
	protected void sendToMatcher(OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, transMatcherQueue, log);
	}

	@Override
	protected void sendToUpdateStatus(BatchJob job, ProcessingEvent event,
			Date timestamp, String info) {
		getProcessingController().updateStatusWithNotification(job, event,
				timestamp, info);
	}

}
