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

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
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
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonArrayGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparisonTreeGroupSinkSourceFactory;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;

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
@SuppressWarnings({ "rawtypes" })
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "maxSession",
				propertyValue = "1"), // Singleton (JBoss only)
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matchSchedulerQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class MatchSchedulerMDB extends AbstractScheduler {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(MatchSchedulerMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatchSchedulerMDB.class.getName());

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

	@Resource(lookup = "java:/choicemaker/urm/jms/matchDedupQueue")
	private Queue matchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateQueue")
	private Queue updateQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/matcherQueue")
	private Queue matcherQueue;

	@Inject
	private JMSContext jmsContext;

	@Override
	protected OabaJobControllerBean getJobController() {
		return jobController;
	}

	@Override
	protected OabaParametersControllerBean getParametersController() {
		return paramsController;
	}

	@Override
	protected OabaProcessingControllerBean getProcessingController() {
		return processingController;
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
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected Logger getJMSTrace() {
		return jmsTrace;
	}

	/**
	 * This method cleans up the chunk files.
	 */
	@Override
	protected void cleanUp(OabaJobMessage data) throws BlockingException {
		log.info("cleanUp");

		final long jobId = data.jobID;
		OabaJob oabaJob = getJobController().findOabaJob(jobId);
		OabaParameters params =
			getParametersController().findBatchParamsByJobId(jobId);
		ServerConfiguration serverConfig =
			getServerController().findServerConfigurationByJobId(jobId);
		final String modelConfigId = params.getModelConfigurationName();
		IProbabilityModel model =
			PMManager.getModelInstance(modelConfigId);
		if (model == null) {
			String s =
				"No modelId corresponding to '" + modelConfigId + "'";
			log.severe(s);
			throw new IllegalArgumentException(s);
		}

		int numProcessors = serverConfig.getMaxChoiceMakerThreads();

		// remove the data
		IChunkDataSinkSourceFactory stageFactory =
			OabaFileUtils.getStageDataFactory(oabaJob, model);
		IChunkDataSinkSourceFactory masterFactory =
			OabaFileUtils.getMasterDataFactory(oabaJob, model);
		stageFactory.removeAllSinks(data.numChunks);
		masterFactory.removeAllSinks(data.numChunks);

		// remove the trees
		ComparisonTreeGroupSinkSourceFactory factory =
			OabaFileUtils.getComparisonTreeGroupFactory(oabaJob, data.stageType,
					numProcessors);
		for (int i = 0; i < data.numRegularChunks; i++) {
			for (int j = 1; j <= numProcessors; j++) {
				IComparisonTreeSource source = factory.getSource(i, j);
				source.delete();
			}
		}

		int numOS = data.numChunks - data.numRegularChunks;

		// remove the oversized array files
		ComparisonArrayGroupSinkSourceFactory factoryOS =
			OabaFileUtils.getComparisonArrayGroupFactoryOS(oabaJob, numProcessors);
		for (int i = 0; i < numOS; i++) {
			for (int j = 1; j <= numProcessors; j++) {
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
	protected void sendToUpdateStatus(long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateQueue, log);
	}

	@Override
	protected void sendToMatchDebup(OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, matchDedupQueue, log);
	}

}
