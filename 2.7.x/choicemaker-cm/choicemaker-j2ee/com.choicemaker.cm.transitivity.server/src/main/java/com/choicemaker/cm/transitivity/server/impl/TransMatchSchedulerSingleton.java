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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_CHUNK_FILE_COUNT;
import static com.choicemaker.cm.args.OperationalPropertyNames.PN_REGULAR_CHUNK_FILE_COUNT;

import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.args.ProcessingEvent;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.core.base.PMManager;
import com.choicemaker.cm.io.blocking.automated.offline.core.IChunkDataSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparisonArraySource;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractSchedulerSingleton;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

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

	// -- Injected data

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	TransitivityParametersController transitivityParametersController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propertyController;

	@EJB
	private ProcessingController processingController;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matcherQueue")
	private Queue matcherQueue;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchDedupQueue")
	private Queue transMatchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatcherQueue")
	private Queue transMatcherQueue;

	// -- Callbacks

	@Override
	protected OabaJobController getJobController() {
		return jobController;
	}

	@Override
	protected OabaParametersController getParametersController() {
		return paramsController;
	}

	@Override
	protected ServerConfigurationController getServerController() {
		return serverController;
	}

	@Override
	protected OabaSettingsController getSettingsController() {
		return oabaSettingsController;
	}

	@Override
	protected OperationalPropertyController getPropertyController() {
		return propertyController;
	}

	@Override
	protected ProcessingController getProcessingController() {
		return processingController;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJMSTrace() {
		return jmsTrace;
	}

	protected TransitivityParametersController getTransitivityParametersController() {
		return transitivityParametersController;
	}

	@Override
	protected void cleanUp(BatchJob batchJob, OabaJobMessage sd)
			throws BlockingException {
		log.info("cleanUp");

		final long jobId = batchJob.getId();
		TransitivityParameters params =
			getTransitivityParametersController()
					.findTransitivityParametersByJobId(jobId);
		ServerConfiguration serverConfig =
			getServerController().findServerConfigurationByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		ImmutableProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s = "No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}

		int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// remove the data
		final String _numChunks =
			getPropertyController().getJobProperty(batchJob,
					PN_CHUNK_FILE_COUNT);
		final int numChunks = Integer.valueOf(_numChunks);

		final String _numRegularChunks =
			getPropertyController().getJobProperty(batchJob,
					PN_REGULAR_CHUNK_FILE_COUNT);
		final int numRegularChunks = Integer.valueOf(_numRegularChunks);

		IChunkDataSinkSourceFactory stageFactory =
			OabaFileUtils.getStageDataFactory(batchJob, model);
		IChunkDataSinkSourceFactory masterFactory =
			OabaFileUtils.getMasterDataFactory(batchJob, model);
		stageFactory.removeAllSinks(numChunks);
		masterFactory.removeAllSinks(numChunks);

		final int numOS = numChunks - numRegularChunks;
		assert numOS > 0;

		// remove the oversized array files
		ComparisonArrayGroupSinkSourceFactory factoryOS =
			OabaFileUtils.getComparisonArrayGroupFactoryOS(batchJob,
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
