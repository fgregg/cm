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

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;

/**
 * This message bean compares the pairs given to it and sends a list of matches
 * to the match writer bean.
 *
 * In this version, there is only one chunk data in memory and different
 * processors work on different trees/arrays of this chunk.
 *
 * @author pcheung
 *
 * @param <T>
 *            the type of record identifier
 */
@SuppressWarnings({
		"rawtypes", "unchecked" })
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/matcherQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class MatcherMDB extends AbstractMatcher {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(MatcherMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ MatcherMDB.class.getName());

	// -- Injected instance data

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propController;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/matchSchedulerQueue")
	private Queue matchSchedulerQueue;

	// -- Call-back methods

	@Override
	protected OabaJobController getJobController() {
		return jobController;
	}

	@Override
	protected OabaParametersController getOabaParametersController() {
		return paramsController;
	}

	@Override
	protected ProcessingController getProcessingController() {
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
	protected OperationalPropertyController getPropertyController() {
		return propController;
	}

	@Override
	protected JMSContext getJMSContext() {
		return jmsContext;
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
	protected void writeMatches(OabaJobMessage data, List<MatchRecord2> matches)
			throws BlockingException {

		// first figure out the correct file for this processor
		final long jobId = data.jobID;
		BatchJob batchJob = getJobController().findBatchJob(jobId);
		IMatchRecord2Sink mSink =
			OabaFileUtils.getMatchChunkFactory(batchJob).getSink(data.treeIndex);
		IComparableSink sink = new ComparableMRSink(mSink);

		// write matches to this file.
		sink.append();
		sink.writeComparables(matches.iterator());
		sink.close();
	}

	@Override
	protected void sendToScheduler(MatchWriterMessage data) {
		MessageBeanUtils.sendMatchWriterData(data, getJMSContext(),
				matchSchedulerQueue, getLogger());
	}

}
