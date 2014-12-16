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

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractScheduler;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaParametersControllerBean;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaProcessingControllerBean;

/**
 * This is the match scheduler for the Transitivity Engine.
 * 
 * @author pcheung
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "maxSession",
				propertyValue = "1"), // Singleton (JBoss only)
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/transMatchSchedulerQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class TransMatchScheduler extends AbstractScheduler {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger
			.getLogger(TransMatchScheduler.class.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatchScheduler.class.getName());

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

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchDedupQueue")
	private Queue transMatchDedupQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/updateTransQueue")
	private Queue updateTransQueue;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatcherQueue")
	private Queue transMatcherQueue;

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

	@Override
	protected void cleanUp (OabaJobMessage sd) throws BlockingException {
		log.fine("cleanUp");

		throw new Error("not yet implemented");
//		final long jobId = sd.jobID;
//		TransitivityParameters params = em.find(OabaParametersEntity.class, jobId);
//		final String modelConfigId  = params.getModelConfigurationName();
//		ImmutableProbabilityModel stageModel = PMManager.getModelInstance(modelConfigId);				
//		//get the number of processors
//		String temp = (String) stageModel.properties().get("numProcessors");
//		int numProcessors = Integer.parseInt(temp);
//
//		//remove the data
//		OabaFileUtils oabaConfig = new OabaFileUtils(jobId);
//		IChunkDataSinkSourceFactory stageFactory = oabaConfig.getStageDataFactory();
//		IChunkDataSinkSourceFactory masterFactory= oabaConfig.getMasterDataFactory();
//		stageFactory.removeAllSinks(sd.numChunks);
//		masterFactory.removeAllSinks(sd.numChunks);
//		
//		//oversized
//		ComparisonArrayGroupSinkSourceFactory factoryOS =
//			oabaConfig.getComparisonArrayGroupFactoryOS(numProcessors);
//		
//		//there is always 1 chunk to remove.
//		int c = sd.numChunks;
//		if (c == 0) c = 1;
//		
//		for (int i=0; i<c; i++) {
//			for (int j=1; j<=numProcessors; j++) {
//				IComparisonArraySource sourceOS = factoryOS.getSource(i, j);
//				sourceOS.remove();
//				log.fine("removing " + sourceOS.getInfo());
//			}
//		}
	}

	protected void sendToMatchDebup (OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, transMatchDedupQueue, log);
	} 

	protected void sendToMatcher (OabaJobMessage sd) {
		MessageBeanUtils.sendStartData(sd, jmsContext, transMatcherQueue, log);
	} 

	protected void sendToUpdateStatus (long jobID, int percentComplete) {
		MessageBeanUtils.sendUpdateStatus(jobID, percentComplete, jmsContext,
				updateTransQueue, log);
	}

}
