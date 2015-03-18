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

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.MessageListener;
import javax.jms.Queue;

import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.OperationalPropertyController;
import com.choicemaker.cm.batch.ProcessingController;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.io.blocking.automated.offline.core.IComparableSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.IMatchRecord2Sink;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2;
import com.choicemaker.cm.io.blocking.automated.offline.data.MatchRecord2Factory;
import com.choicemaker.cm.io.blocking.automated.offline.impl.ComparableMRSink;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.MatchWriterMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJobController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaParametersController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaSettingsController;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationController;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.AbstractMatcher;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaFileUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.util.MessageBeanUtils;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityParametersController;

/**
 * This is the Matcher for the Transitivity Engine. It is called by
 * TransMatchSchedulerMDB.
 * 
 * @author pcheung
 *
 */
@SuppressWarnings({
	"rawtypes", "unchecked" })
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/transMatcherQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class TransMatcherMDB extends AbstractMatcher implements MessageListener {

	private static final long serialVersionUID = 271L;
	private static final Logger log = Logger.getLogger(TransMatcherMDB.class
			.getName());
	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ TransMatcherMDB.class.getName());

	// -- Injected instance data

	@EJB
	private OabaJobController jobController;

	@EJB
	private OabaSettingsController oabaSettingsController;

	@EJB
	private OabaParametersController paramsController;

	@EJB
	TransitivityParametersController transitivityParametersController;

	@EJB
	private ProcessingController processingController;

	@EJB
	private ServerConfigurationController serverController;

	@EJB
	private OperationalPropertyController propController;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:/choicemaker/urm/jms/transMatchSchedulerQueue")
	private Queue transMatchSchedulerQueue;

	// -- Call-back methods

	@Override
	protected OabaJobController getJobController() {
		return jobController;
	}

	@Override
	protected OabaParametersController getOabaParametersController() {
		return new CombinedParametersController(paramsController,
				transitivityParametersController);
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
		
		 //write matches to this file.
		 sink.append();
		 sink.writeComparables(matches.iterator());
		
		 //write the separator
		 MatchRecord2 mr = (MatchRecord2) matches.get(0);
		 mr = MatchRecord2Factory.getSeparator(mr.getRecordID1());
		 sink.writeComparable(mr);
		
		 sink.close();
	}

	@Override
	protected void sendToScheduler(MatchWriterMessage data) {
		MessageBeanUtils.sendMatchWriterData(data, getJMSContext(),
				transMatchSchedulerQueue, log);
	}

}
