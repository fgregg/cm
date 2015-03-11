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

import static com.choicemaker.cm.args.OperationalPropertyNames.PN_BLOCKING_FIELD_COUNT;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Queue;

import com.choicemaker.cm.args.OabaParameters;
import com.choicemaker.cm.args.OabaSettings;
import com.choicemaker.cm.args.ServerConfiguration;
import com.choicemaker.cm.batch.BatchJob;
import com.choicemaker.cm.batch.ProcessingEventLog;
import com.choicemaker.cm.core.BlockingException;
import com.choicemaker.cm.core.ImmutableProbabilityModel;
import com.choicemaker.cm.io.blocking.automated.offline.core.IBlockSink;
import com.choicemaker.cm.io.blocking.automated.offline.core.OabaProcessingEvent;
import com.choicemaker.cm.io.blocking.automated.offline.impl.BlockGroup;
import com.choicemaker.cm.io.blocking.automated.offline.server.data.OabaJobMessage;
import com.choicemaker.cm.io.blocking.automated.offline.services.OABABlockingService;

//import com.choicemaker.cm.core.ImmutableProbabilityModel;

/**
 * This message bean performs OABA blocking and trimming of over-sized blocks.
 *
 * @author pcheung (Original implementation)
 * @author rphall (Migration to EJB3)
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup",
				propertyValue = "java:/choicemaker/urm/jms/blockQueue"),
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Queue") })
public class BlockingMDB extends AbstractOabaMDB {

	private static final long serialVersionUID = 271L;

	private static final Logger log = Logger.getLogger(BlockingMDB.class
			.getName());

	private static final Logger jmsTrace = Logger.getLogger("jmstrace."
			+ BlockingMDB.class.getName());

	@Resource(lookup = "java:/choicemaker/urm/jms/dedupQueue")
	private Queue dedupQueue;

	@Override
	protected void processOabaMessage(OabaJobMessage data, BatchJob batchJob,
			OabaParameters params, OabaSettings oabaSettings,
			ProcessingEventLog processingLog, ServerConfiguration serverConfig,
			ImmutableProbabilityModel model) throws BlockingException {

		// Start blocking
		final int maxBlock = oabaSettings.getMaxBlockSize();
		final int maxOversized = oabaSettings.getMaxOversized();
		final int minFields = oabaSettings.getMinFields();
		final BlockGroup bGroup =
			new BlockGroup(OabaFileUtils.getBlockGroupFactory(batchJob),
					maxBlock);
		final IBlockSink osSpecial =
			OabaFileUtils.getOversizedFactory(batchJob).getNextSink();
		OABABlockingService blockingService;
		try {
			final String _numBlockFields =
				getPropertyController().getJobProperty(batchJob,
						PN_BLOCKING_FIELD_COUNT);
			final int numBlockFields = Integer.valueOf(_numBlockFields);
			blockingService =
				new OABABlockingService(maxBlock, bGroup,
						OabaFileUtils.getOversizedGroupFactory(batchJob),
						osSpecial, null,
						OabaFileUtils.getRecValFactory(batchJob),
						numBlockFields, data.validator, processingLog, batchJob,
						minFields, maxOversized);
		} catch (IOException e) {
			throw new BlockingException(e.getMessage(), e);
		}
		blockingService.runService();

		final int numBlocks = blockingService.getNumBlocks();
		final int numOS = blockingService.getNumOversized();
		final int numInvalid = blockingService.getNumInvalid();
		log.info("num Blocks " + numBlocks);
		log.info("num OS " + numOS);
		log.info("num Invalid: " + numInvalid);
		if (numInvalid > numBlocks) {
			String msg =
				"Number of invalid blocks (" + numInvalid + ") > "
						+ "number of valid blocks (" + numBlocks + ")";
			log.warning(msg);
		}
		log.info("Done Blocking: " + blockingService.getTimeElapsed());

		// clean up
		blockingService = null;
		System.gc();
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
	protected void notifyProcessingCompleted(OabaJobMessage data) {
		MessageBeanUtils.sendStartData(data, getJmsContext(), dedupQueue,
				getLogger());
	}

	@Override
	protected OabaProcessingEvent getCompletionEvent() {
		return OabaProcessingEvent.DONE_OVERSIZED_TRIMMING;
	}

}
