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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
//import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.args.TransitivityParameters;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.BatchJobStatus;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.OabaJob;
import com.choicemaker.cm.io.blocking.automated.offline.server.ejb.ServerConfigurationException;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.MessageBeanUtils;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.OabaJobEntity;
import com.choicemaker.cm.io.blocking.automated.offline.server.impl.ServerConfigurationEntity;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityJob;
import com.choicemaker.cm.transitivity.server.ejb.TransitivityService;

/**
 * @author pcheung
 */
@Stateless
public class TransitivityServiceBean implements TransitivityService {

	private static final Logger log = Logger
			.getLogger(TransitivityServiceBean.class.getName());

	private static final String SOURCE_CLASS = TransitivityServiceBean.class
			.getSimpleName();

	protected static void logStartParameters(String externalID,
			TransitivityParameters tp, OabaJob oabaJob, ServerConfiguration sc) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		pw.println("External id: " + externalID);
		pw.println(TransitivityParametersEntity.dump(tp));
		pw.println(OabaJobEntity.dump(oabaJob));
		pw.println(ServerConfigurationEntity.dump(sc));
		String msg = sw.toString();
		log.info(msg);
	}

	protected static void validateStartParameters(String externalID,
			TransitivityParameters tp, OabaJob oabaJob, ServerConfiguration sc) {

		// Create an empty list of invalid parameters
		List<String> validityErrors = new LinkedList<>();
		assert validityErrors.isEmpty();

		if (tp == null) {
			validityErrors.add("null batch parameters");
		}
		if (oabaJob == null) {
			validityErrors.add("null OABA job");
		}
		if (sc == null) {
			validityErrors.add("null server configuration");
		}
		if (!validityErrors.isEmpty()) {
			String msg =
				"Invalid parameters to OabaService.startOABA: "
						+ validityErrors.toString();
			log.severe(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	@EJB
	TransitivityJobControllerBean jobController;

	@Resource(name = "jms/transitivityQueue",
			lookup = "java:/choicemaker/urm/jms/transitivityQueue")
	private Queue queue;

	@Inject
	private JMSContext context;

	@Override
	public long startTransitivity(String externalID,
			TransitivityParameters batchParams, OabaJob oabaJob,
			ServerConfiguration serverConfiguration)
			throws ServerConfigurationException {

		final String METHOD = "startTransitivity";
		log.entering(SOURCE_CLASS, METHOD);

		logStartParameters(externalID, batchParams, oabaJob,
				serverConfiguration);
		validateStartParameters(externalID, batchParams, oabaJob,
				serverConfiguration);

		// Create and persist a transitivity job and its associated objects
		TransitivityJob transJob =
			jobController.createPersistentTransitivityJob(externalID,
					batchParams, oabaJob, serverConfiguration);
		final long retVal = transJob.getId();
		assert BatchJob.INVALID_ID != retVal;

		// Mark the job as queued and start processing by the
		// StartTransitivityMDB EJB
		transJob.markAsQueued();
		sendToTransitivity(retVal);

		log.exiting(SOURCE_CLASS, METHOD, retVal);
		return retVal;
	}

	@Override
	public BatchJobStatus getStatus(long jobID) {
		// TODO FIXME not yet re-implemented
		throw new Error("not yet implemented");
		//
		// TransitivityJob transJob = jobController.findTransitivityJob(jobID);
		// TransitivityJobStatus status =
		// new TransitivityJobStatus(transJob.getId(), transJob.getStatus(),
		// transJob.getStarted(), transJob.getCompleted());
		//
		// return status;
	}

	/**
	 * This method puts the request on the Transitivity Engine's message queue.
	 *
	 * @param d
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void sendToTransitivity(long jobID) {
		OabaJobMessage data = new OabaJobMessage(jobID);
		MessageBeanUtils.sendStartData(data, context, queue, log);
	}

}
